package vpc.mach.avr;

import vpc.VPCBase;
import vpc.CompilationError;
import vpc.mach.avr.sir.*;
import vpc.mach.avr.syntax.Expr;
import vpc.mach.avr.syntax.ExprList;
import vpc.mach.avr.syntax.Context;
import vpc.mach.avr.syntax.atmel.AtmelParser;
import vpc.mach.avr.syntax.atmel.ParseException;
import vpc.core.ProgramPoint;
import vpc.core.AbstractToken;
import vpc.core.AbstractParseException;

import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * The <code>Module</code> class collects together the instructions and data
 * into an AVR assembly program.
 * @author Ben L. Titzer
 */
public class Module extends VPCBase implements Context {

    public final HashMap definitions;
    public final HashMap constants;
    public final HashMap labels;
    public final AVRErrorReporter ERROR;

    private Segment segment;
    private ProgramSegment programSegment;
    private DataSegment dataSegment;
    private EEPROMSegment eepromSegment;

    public Program newprogram;

    private Item head;
    private Item tail;

    /**
     *  I T E M   C L A S S E S
     * --------------------------------------------------------
     *
     *  These item classes represent the various parts of the assembly
     *  file that are recorded in the module.
     *
     *
     */
    private abstract class Item {

        private Item next;

        abstract void build();

        Segment getSegment() {
            if (next != null) return next.getSegment();
            throw failure("unknown cseg");
        }

    }

    private class EquItem extends Item {
        private final AbstractToken name;
        private final Expr value;

        EquItem(AbstractToken n, Expr v) {
            name = n;
            value = v;
        }

        void build() {
            int result = value.evaluate(Module.this);
            verboseln("(PASS 2) Adding constant: " + name.image + " = " + result);
            constants.put(name.image.toLowerCase(), new Integer(result));
        }

        public String toString() {
            return ".equ " + name;
        }
    }

    private class DefItem extends Item {
        private final AbstractToken name;
        private final AbstractToken register;

        DefItem(AbstractToken n, AbstractToken r) {
            name = n;
            register = r;
        }

        void build() {
            Register reg = Register.getRegisterByName(register.image);
            if (reg == null) ERROR.UnknownRegister(register);
            verboseln("(PASS 2) Adding definition: " + name.image + " = " + reg);
            definitions.put(name.image.toLowerCase(), reg);
        }

        public String toString() {
            return ".def " + name + " = " + register;
        }
    }

    private abstract class AddressableItem extends Item {
        protected Segment cseg;
        protected int address;

        AddressableItem(Segment s, int addr) {
            cseg = s;
            address = addr;
        }

        Segment getSegment() {
            return cseg;
        }

        int getAddress() {
            // the program cseg is word-addressable.
            if (cseg == programSegment)
                return address >> 1;
            return address;
        }

        int getByteAddress() {
            return address;
        }
    }

    private class InstrItem extends AddressableItem {
        private final String variant;
        private final AbstractToken name;
        private final Operand[] operands;
        private final InstrPrototype proto;

        InstrItem(Segment s, int a, String v, AbstractToken n, InstrPrototype p, Operand[] ops) {
            super(s, a);
            variant = v;
            name = n;
            operands = ops;
            proto = p;
        }

        void build() {
            int address = getAddress();
            verbose("(PASS 2) Simplifying: " + variant + " @ " + address);

            for (int cntr = 0; cntr < operands.length; cntr++)
                simplifyOperand(operands[cntr]);

            verboseln("");

            try {
                Instr instr = proto.build(address, operands);

                newprogram.writeInstr(instr, getByteAddress());

            } catch (Instr.ImmediateRequired e) {
                ERROR.ConstantExpected(e.operand);
            } catch (Instr.InvalidImmediate e) {
                ERROR.ConstantOutOfRange(operands[e.number - 1], e.value, interval(e.low, e.high));
            } catch (Instr.InvalidRegister e) {
                ERROR.IncorrectRegister(operands[e.number - 1], e.register, e.set.toString());
            } catch (Instr.RegisterRequired e) {
                ERROR.RegisterExpected(e.operand);
            } catch (Instr.WrongNumberOfOperands e) {
                ERROR.WrongNumberOfOperands(name, e.found, e.expected);
            }
        }

        void simplifyOperand(Operand o) {
            if (o.isRegister()) {
                Operand.Register or = (Operand.Register) o;
                or.setRegister(getRegister(or.name));
                verbose(", " + or.name);
            } else {
                Operand.Constant oc = (Operand.Constant) o;
                int value = oc.expr.evaluate(Module.this);
                oc.setValue(value);
                verbose(", " + value);
            }
        }

        public String toString() {
            return "instr: " + variant + " @ " + address;
        }
    }

    private class LabelItem extends AddressableItem {
        private final AbstractToken name;

        LabelItem(Segment s, int a, AbstractToken n) {
            super(s, a);
            name = n;
        }

        void build() {
            if (cseg == programSegment)
                newprogram.newProgramLabel(name.image, getAddress());
            if (cseg == dataSegment)
                newprogram.newDataLabel(name.image, getAddress());
            if (cseg == eepromSegment)
                newprogram.newEEPromLabel(name.image, getAddress());
        }

        public String toString() {
            return "label: " + name + " in " + cseg.getName() + " @ " + address;
        }
    }

    private class DataItem extends AddressableItem {

        private final ExprList list;
        private final int width;

        DataItem(Segment s, int a, ExprList l, int w) {
            super(s, a);
            list = l;
            width = w;
        }

        void build() {
            int cursor = getByteAddress();
            verbose("simplifying data @ " + cursor + " to " + cseg.getName());

            for (ExprList.ExprItem item = list.head; item != null; item = item.next) {
                Expr e = item.expr;
                if (e instanceof Expr.StringLiteral) {
                    String str = ((Expr.StringLiteral) e).value;
                    verbose(", str: " + quote(str));
                    cseg.writeBytes(str.getBytes(), cursor);
                    cursor = align(cursor, width);
                } else {
                    int val = e.evaluate(Module.this);
                    verbose(", val:" + val);
                    for (int cntr = 0; cntr < width; cntr++) {
                        cseg.writeByte((byte) val, cursor);
                        val = val >> 8;
                        cursor++;
                    }
                }
            }
            verboseln("");
        }

        public String toString() {
            return "data @ " + getByteAddress() + " to " + cseg.getName();
        }
    }

    private class ReserveItem extends AddressableItem {
        private final int length;

        ReserveItem(Segment s, int a, int l) {
            super(s, a);
            length = l;
        }

        void build() {
            // nothing to build with a reserve item
        }

        public String toString() {
            return "reserve " + length + " in " + cseg.getName();
        }
    }

    private abstract class Segment {

        int lowest_address;
        int highest_address;

        int origin;
        int cursor;

        Segment(int org) {
            lowest_address = highest_address = origin = cursor = org;
        }

        void setOrigin(int org) {
            origin = org;
            if (org < lowest_address) lowest_address = org;
            if (org > highest_address) highest_address = org;
        }

        Item addInstruction(String variant, AbstractToken name, Operand[] ops) {
            InstrPrototype p = InstructionSet.getPrototype(variant);
            if (p == null) ERROR.UnknownInstruction(name);
            InstrItem i = new InstrItem(this, cursor, variant, name, p, ops);
            advance(p.getSize());
            return i;
        }

        Item addBytes(ExprList l) {
            Item i = new DataItem(this, cursor, l, 1);
            addListSize(l, 1);
            return i;
        }

        Item addWords(ExprList l) {
            Item i = new DataItem(this, cursor, l, 2);
            addListSize(l, 2);
            return i;
        }

        Item addDoubleWords(ExprList l) {
            Item i = new DataItem(this, cursor, l, 4);
            addListSize(l, 4);
            return i;
        }

        private void addListSize(ExprList l, int itemsize) {
            int total = 0;

            for (ExprList.ExprItem ei = l.head; ei != null; ei = ei.next) {
                Expr e = ei.expr;
                if (e instanceof Expr.StringLiteral) {
                    total += ((Expr.StringLiteral) e).value.length();
                    total = align(total, itemsize);
                } else
                    total += itemsize;

            }

            advance(total);
        }

        Item reserveBytes(int count) {
            Item i = new ReserveItem(this, cursor, count);
            advance(count);
            return i;
        }

        void advance(int dist) {
            cursor += dist;
            if (cursor > highest_address) highest_address = cursor;
        }

        int getCurrentPosition() {
            return cursor;
        }

        abstract String getName();

        abstract void writeByte(byte val, int address);

        abstract void writeBytes(byte[] val, int address);
    }

    private class ProgramSegment extends Segment {
        ProgramSegment() {
            super(0);
        }

        void advance(int dist) {
            cursor = align(cursor + dist, 2);
            if (cursor > highest_address) highest_address = cursor;
        }

        void writeByte(byte val, int address) {
            newprogram.writeProgramByte(val, address);
        }

        void writeBytes(byte[] val, int address) {
            newprogram.writeProgramBytes(val, address);
        }

        String getName() {
            return "program";
        }
    }

    private class DataSegment extends Segment {
        DataSegment() {
            super(32);
        }

        Item addInstruction(String variant, AbstractToken name, Operand[] ops) {
            ERROR.InstructionCannotBeInSegment("data", name);
            throw failure("instruction cannot be in data cseg");
        }

        Item addBytes(ExprList l) {
            ERROR.DataCannotBeInSegment(l);
            throw failure("initialized data cannot be in data cseg");
        }

        Item addWords(ExprList l) {
            ERROR.DataCannotBeInSegment(l);
            throw failure("initialized data cannot be in data cseg");
        }

        Item addDoubleWords(ExprList l) {
            ERROR.DataCannotBeInSegment(l);
            throw failure("initialized data cannot be in data cseg");
        }

        void writeByte(byte val, int address) {
            // TODO: define correct error for this.
            throw VPCBase.failure("cannot write initialized data into data segment @ " + address);
        }

        void writeBytes(byte[] val, int address) {
            // TODO: define correct error for this.
            throw VPCBase.failure("cannot write initialized data into data segment @ " + address);
        }

        String getName() {
            return "data";
        }
    }

    private class EEPROMSegment extends Segment {
        EEPROMSegment() {
            super(0);
        }

        Item addInstruction(String variant, AbstractToken name, Operand[] ops) {
            ERROR.InstructionCannotBeInSegment("eeprom", name);
            throw failure("instruction cannot be in eeprom cseg");
        }

        void writeByte(byte val, int address) {
            // TODO: define correct error for this.
            throw VPCBase.failure("cannot write initialized data into eeprom segment @ " + address);
        }

        void writeBytes(byte[] val, int address) {
            // TODO: define correct error for this.
            throw VPCBase.failure("cannot write initialized data into eeprom segment @ " + address);
        }

        String getName() {
            return "eeprom";
        }
    }

    public Module() {
        definitions = new HashMap();
        constants = new HashMap();
        labels = new HashMap();

        programSegment = new ProgramSegment();
        dataSegment = new DataSegment();
        eepromSegment = new EEPROMSegment();

        segment = programSegment;

        addGlobalConstants();

        ERROR = new AVRErrorReporter();
    }

    private void addGlobalConstants() {
        // TODO: pull out machine-specific constants to somewhere.
        constant("RAMEND", 4095);

        // TODO: double check these IO register numbers.
        ioreg("UCSR1C", 0x9D);
        ioreg("UDR1", 0x9C);
        ioreg("UCSR1A", 0x9B);
        ioreg("UCSR1B", 0x9A);
        ioreg("UBRR1L", 0x99);
        ioreg("UBRR1H", 0x98);

        ioreg("UCSR0C", 0x95);

        ioreg("UBRR0H", 0x90);

        ioreg("TCCR3C", 0x8C);
        ioreg("TCCR3A", 0x8B);
        ioreg("TCCR3B", 0x8A);
        ioreg("TCNT3H", 0x89);
        ioreg("TCNT3L", 0x88);
        ioreg("OCR3AH", 0x87);
        ioreg("OCR3AL", 0x86);
        ioreg("OCR3BH", 0x85);
        ioreg("OCR3BL", 0x84);
        ioreg("OCR3CH", 0x83);
        ioreg("OCR3CL", 0x82);
        ioreg("ICR3H", 0x81);
        ioreg("ICR3L", 0x80);

        ioreg("ETIMSK", 0x7D);
        ioreg("ETIFR", 0x7C);

        ioreg("TCCR1C", 0x7A);
        ioreg("OCR1CH", 0x79);
        ioreg("OCR1CL", 0x78);

        ioreg("TWCR", 0x74);
        ioreg("TWDR", 0x73);
        ioreg("TWAR", 0x72);
        ioreg("TWSR", 0x71);
        ioreg("TWBR", 0x70);
        ioreg("OSCCAL", 0x6F);

        ioreg("XMCRA", 0x6D);
        ioreg("XMCRB", 0x6C);

        ioreg("EICRA", 0x6A);

        ioreg("SPMCSR", 0x68);

        ioreg("PORTG", 0x65);
        ioreg("DDRG", 0x64);
        ioreg("PING", 0x63);
        ioreg("PORTF", 0x62);
        ioreg("DDRF", 0x61);

        ioreg("SREG", 0x3F);
        ioreg("SPH", 0x3E);
        ioreg("SPL", 0x3D);
        ioreg("XDIV", 0x3C);
        ioreg("RAMPZ", 0x3B);
        ioreg("EICRB", 0x3A);
        ioreg("EIMSK", 0x39);
        ioreg("EIFR", 0x38);
        ioreg("TIMSK", 0x37);
        ioreg("TIFR", 0x36);
        ioreg("MCUCR", 0x35);
        ioreg("MCUCSR", 0x34);
        ioreg("TCCR0", 0x33);
        ioreg("TCNT0", 0x32);
        ioreg("OCR0", 0x31);
        ioreg("ASSR", 0x30);
        ioreg("TCCR1A", 0x2F);
        ioreg("TCCR1B", 0x2E);
        ioreg("TCNT1H", 0x2D);
        ioreg("TCNT1L", 0x2C);
        ioreg("OCR1AH", 0x2B);
        ioreg("OCR1AL", 0x2A);
        ioreg("OCR1BH", 0x29);
        ioreg("OCR1BL", 0x28);
        ioreg("ICR1H", 0x27);
        ioreg("ICR1L", 0x26);
        ioreg("TCCR2", 0x25);
        ioreg("TCNT2", 0x24);
        ioreg("OCR2", 0x23);
        ioreg("OCDR", 0x22);
        ioreg("WDTCR", 0x21);
        ioreg("SFIOR", 0x20);
        ioreg("EEARH", 0x1F);
        ioreg("EEARL", 0x1E);
        ioreg("EEDR", 0x1D);
        ioreg("EECR", 0x1C);
        ioreg("PORTA", 0x1B);
        ioreg("DDRA", 0x1A);
        ioreg("PINA", 0x19);
        ioreg("PORTB", 0x18);
        ioreg("DDRB", 0x17);
        ioreg("PINB", 0x16);
        ioreg("PORTC", 0x15);
        ioreg("DDRC", 0x14);
        ioreg("PINC", 0x13);
        ioreg("PORTD", 0x12);
        ioreg("DDRD", 0x11);
        ioreg("PIND", 0x10);
        ioreg("SPDR", 0x0F);
        ioreg("SPSR", 0x0E);
        ioreg("SPCR", 0x0D);
        ioreg("UDR0", 0x0C);
        ioreg("UCSR0A", 0x0B);
        ioreg("UCSR0B", 0x0A);
        ioreg("UBRR0L", 0x09);
        ioreg("ACSR", 0x08);
        ioreg("ADMUX", 0x07);
        ioreg("ADCSRA", 0x06);
        ioreg("ADCH", 0x05);
        ioreg("ADCL", 0x04);
        ioreg("PORTE", 0x03);
        ioreg("DDRE", 0x02);
        ioreg("PINE", 0x01);
        ioreg("PINF", 0x00);


    }

    private void constant(String name, int value) {
        constants.put(name.toLowerCase(), new Integer(value));
    }

    private void ioreg(String name, int offset) {
        constants.put(name.toLowerCase(), new Integer(offset));
    }

    // .def directive
    public void addDefinition(AbstractToken name, AbstractToken rtok) {
        addItem(new DefItem(name, rtok));
    }

    // .equ directive
    public void addConstant(AbstractToken name, Expr val) {
        addItem(new EquItem(name, val));
    }

    // .dseg directive
    public void enterDataSegment() {
        segment = dataSegment;
    }

    // .cseg directive
    public void enterProgramSegment() {
        segment = programSegment;
    }

    // .eseg directive
    public void enterEEPROMSegment() {
        segment = eepromSegment;
    }

    // .db directive
    public void addDataBytes(ExprList l) {
        addItem(segment.addBytes(l));
    }

    // .dw directive
    public void addDataWords(ExprList l) {
        addItem(segment.addWords(l));
    }

    // .dd directive
    public void addDataDoubleWords(ExprList l) {
        addItem(segment.addDoubleWords(l));
    }

    // .org directive
    public void setOrigin(Expr.Constant c) {
        segment.setOrigin(c.evaluate(this));
    }

    // .byte directive
    public void reserveBytes(Expr e, Expr f) {
        // TODO: fill section with particular value
        verboseln("(PASS 1) Reserving bytes...");
        segment.reserveBytes(e.evaluate(this));
    }

    // .include directive
    public void includeFile(AbstractToken fname) throws AbstractParseException {
        try {
            String fn = trimquotes(fname.image);
            verboseln("(PASS 1) attempting to include file: "+fn);
            AtmelParser parser = new AtmelParser(new FileInputStream(fn), this, fn);
            // TODO: handle infinite include recursion possibility
            parser.Module();
        } catch ( FileNotFoundException e ) {
            ERROR.IncludeFileNotFound(fname);
        }
    }


    // <instruction>
    public void addInstruction(String variant, AbstractToken name) {
        Operand[] o = {};
        addItem(segment.addInstruction(variant, name, o));
    }

    // <instruction> <operand>
    public void addInstruction(String variant, AbstractToken name, Operand o1) {
        Operand[] o = {o1};
        addItem(segment.addInstruction(variant, name, o));
    }

    // <instruction> <operand> <operand>
    public void addInstruction(String variant, AbstractToken name, Operand o1, Operand o2) {
        Operand[] o = {o1, o2};
        addItem(segment.addInstruction(variant, name, o));
    }

    // <instruction> <operand> <operand> <operand>
    public void addInstruction(String variant, AbstractToken name, Operand o1, Operand o2, Operand o3) {
        Operand[] o = {o1, o2, o3};
        addItem(segment.addInstruction(variant, name, o));
    }

    // <label>
    public void addLabel(AbstractToken name) {
        LabelItem li = new LabelItem(segment, segment.getCurrentPosition(), name);
        addItem(li);
        labels.put(name.image.toLowerCase(), li);
    }

    public Program build() {
        newprogram = new Program(programSegment.lowest_address, programSegment.highest_address,
                dataSegment.lowest_address, dataSegment.highest_address,
                eepromSegment.lowest_address, eepromSegment.highest_address);

        for (Item pos = head; pos != null; pos = pos.next) {
            pos.build();
        }

        return newprogram;
    }


    public Register getRegister(AbstractToken tok) {
        String name = tok.image.toLowerCase();
        Register reg = Register.getRegisterByName(name);
        if (reg == null)
            reg = (Register) definitions.get(name);

        if (reg == null) ERROR.UnknownRegister(tok);
        return reg;
    }

    public int getVariable(AbstractToken tok) {
        String name = tok.image.toLowerCase();

        Integer v = (Integer) constants.get(name);
        if (v == null) {
            LabelItem li = (LabelItem) labels.get(name);
            if (li == null) ERROR.UnknownVariable(tok);
            return li.getAddress();
        } else
            return v.intValue();
    }

    private void addItem(Item i) {
        if (head == null)
            head = i;
        else
            tail.next = i;
        tail = i;

        verboseln("(PASS 1) Adding item: " + i);
    }

    public static int align(int val, int width) {
        if (val % width == 0) return val;
        return val + (width - (val % width));
    }

}
