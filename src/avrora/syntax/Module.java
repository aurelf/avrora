package avrora.syntax;

import avrora.core.*;
import avrora.syntax.Context;
import avrora.syntax.Expr;
import avrora.syntax.ExprList;
import avrora.syntax.atmel.AtmelParser;
import avrora.util.StringUtil;
import avrora.util.Verbose;
import avrora.syntax.AbstractParseException;
import avrora.syntax.AbstractToken;
import avrora.AVRErrorReporter;
import avrora.Avrora;
import avrora.core.Operand;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * The <code>Module</code> class collects together the instructions and data
 * into an AVR assembly program.
 *
 * @author Ben L. Titzer
 */
public class Module implements Context {

    public final HashMap definitions;
    public final HashMap constants;
    public final HashMap labels;
    public final AVRErrorReporter ERROR;

    public final boolean caseSensitivity;
    public final boolean useByteAddresses;

    private Seg segment;
    private Seg programSegment;
    private Seg dataSegment;
    private Seg eepromSegment;

    public Program newprogram;

    private List itemList;

    static Verbose.Printer modulePrinter = Verbose.getVerbosePrinter("module");

    private static final SyntacticOperand[] NO_OPERANDS = {};


    class Seg {
        private final String name;
        private final boolean acceptsInstrs;
        private final boolean acceptsData;

        int lowest_address;
        int highest_address;

        int origin;
        int cursor;
        final int align;

        Seg(String n, int a, int o, boolean i, boolean d) {
            name = n;
            acceptsInstrs = i;
            acceptsData = d;
            align = a;
            origin = lowest_address = highest_address = cursor = o;
        }

        public Module getModule() {
            return Module.this;
        }

        public String getName() {
            return name;
        }

        public void writeDataBytes(ASTNode loc, int baddr, byte[] b) {
            if ( !acceptsData ) ERROR.DataCannotBeInSegment(name, loc);
            else  newprogram.writeProgramBytes(b, baddr);
        }
        public void writeDataByte(ASTNode loc, int baddr, byte b) {
            if ( !acceptsData ) ERROR.DataCannotBeInSegment(name, loc);
            else  newprogram.writeProgramByte(b, baddr);
        }

        public void writeInstr(AbstractToken loc, int baddr, Instr i) {
            if ( !acceptsInstrs ) ERROR.InstructionCannotBeInSegment(name, loc);
            else newprogram.writeInstr(i, baddr);
        }

        public void addLabel(int baddr, String labelname) {
            if ( name.equals("program")) newprogram.newProgramLabel(labelname, baddr);
            if ( name.equals("eeprom")) newprogram.newEEPromLabel(labelname, baddr);
            if ( name.equals("data")) newprogram.newDataLabel(labelname, baddr);
        }

        void setOrigin(int org) {
            origin = org;
            if (org < lowest_address) lowest_address = org;
            if (org > highest_address) highest_address = org;
        }

        int getCurrentAddress() {
            return cursor;
        }

        void advance(int dist) {
            cursor = align(cursor + dist, align);
            if ( cursor > highest_address ) highest_address = cursor;
        }
    }


    public Module(boolean cs, boolean ba) {
        caseSensitivity = cs;
        useByteAddresses = ba;

        definitions = new HashMap();
        constants = new HashMap();
        labels = new HashMap();

        programSegment = new Seg("program", 2, 0, true, true);
        dataSegment = new Seg("data", 1, 32, false, false);
        eepromSegment = new Seg("eeprom", 1, 0, false, false);

        segment = programSegment;
        itemList = new LinkedList();

        addGlobalConstants();

        ERROR = new AVRErrorReporter();
    }


    // .def directive
    public void addDefinition(AbstractToken name, AbstractToken rtok) {
        modulePrinter.println(".def "+labelName(name)+" = "+labelName(rtok));
        addItem(new Item.RegisterAlias(segment, name, rtok));
    }

    // .equ directive
    public void addConstant(AbstractToken name, Expr val) {
        modulePrinter.println(".equ "+labelName(name)+" = "+val);
        addItem(new Item.NamedConstant(segment, name, val));
    }

    // .dseg directive
    public void enterDataSegment() {
        modulePrinter.println("enter segment: data");
        segment = dataSegment;
    }

    // .cseg directive
    public void enterProgramSegment() {
        modulePrinter.println("enter segment: program");
        segment = programSegment;
    }

    // .eseg directive
    public void enterEEPROMSegment() {
        modulePrinter.println("enter segment: eeprom");
        segment = eepromSegment;
    }

    // .db directive
    public void addDataBytes(ExprList l) {
        modulePrinter.println("addDataBytes");
        addItem(new Item.InitializedData(segment, l, 1));
    }

    // .dw directive
    public void addDataWords(ExprList l) {
        modulePrinter.println("addDataWords");
        addItem(new Item.InitializedData(segment, l, 2));
    }

    // .dd directive
    public void addDataDoubleWords(ExprList l) {
        modulePrinter.println("addDataDoubleWords");
        addItem(new Item.InitializedData(segment, l, 4));
    }

    // .org directive
    public void setOrigin(Expr.Constant c) {
        int result = c.evaluate(segment.getCurrentAddress(), this);
        modulePrinter.println("setOrigin("+c+") -> "+result);
        segment.setOrigin(result);
    }

    // .byte directive
    public void reserveBytes(Expr e, Expr f) {
        // TODO: fill section with particular value
        int result = e.evaluate(segment.getCurrentAddress(), this);
        modulePrinter.println("reserveBytes("+e+") -> "+result);
        addItem(new Item.UninitializedData(segment, result));
    }

    // .include directive
    public void includeFile(AbstractToken fname) throws AbstractParseException {
        try {
            modulePrinter.println("includeFile("+fname.image+")");
            String fn = StringUtil.trimquotes(fname.image);
            AtmelParser parser = new AtmelParser(new FileInputStream(fn), this, fn);
            // TODO: handle infinite include recursion possibility
            parser.Module();
        } catch (FileNotFoundException e) {
            ERROR.IncludeFileNotFound(fname);
        }
    }


    // <instruction>
    public void addInstruction(String variant, AbstractToken name) {
        String v = StringUtil.quote(variant);
        modulePrinter.println(StringUtil.embed("addInstr", v));
        SyntacticOperand[] o = NO_OPERANDS;
        makeInstr(variant, name, o);
    }

    // <instruction> <operand>
    public void addInstruction(String variant, AbstractToken name, SyntacticOperand o1) {
        String v = StringUtil.quote(variant);
        modulePrinter.println(StringUtil.embed("addInstr", v, o1));
        SyntacticOperand[] o = {o1};
        makeInstr(variant, name, o);
    }

    // <instruction> <operand> <operand>
    public void addInstruction(String variant, AbstractToken name, SyntacticOperand o1, SyntacticOperand o2) {
        String v = StringUtil.quote(variant);
        modulePrinter.println(StringUtil.embed("addInstr", v, o1, o2));
        SyntacticOperand[] o = {o1, o2};
        makeInstr(variant, name, o);
    }

    // <instruction> <operand> <operand> <operand>
    public void addInstruction(String variant, AbstractToken name, SyntacticOperand o1, SyntacticOperand o2, SyntacticOperand o3) {
        String v = StringUtil.quote(variant);
        modulePrinter.println(StringUtil.embed("addInstr", v, o1, o2, o3));
        SyntacticOperand[] o = {o1, o2, o3};
        makeInstr(variant, name, o);
    }

    // <label>
    public void addLabel(AbstractToken name) {
        Item.Label li = new Item.Label(segment, name);
        addItem(li);
        labels.put(name.image.toLowerCase(), li);
    }

    private void makeInstr(String variant, AbstractToken name, SyntacticOperand[] o) {
        InstrPrototype proto = InstructionSet.getPrototype(variant);
        addItem(new Item.Instruction(segment, variant, name, proto, o));
    }


    public Program build() {
        newprogram = new Program(programSegment.lowest_address, programSegment.highest_address,
                dataSegment.lowest_address, dataSegment.highest_address,
                eepromSegment.lowest_address, eepromSegment.highest_address);

        Iterator i = itemList.iterator();
        while (i.hasNext()) {
            Item pos = (Item)i.next();
            simplify(pos);
        }

        return newprogram;
    }

    private void simplify(Item i) {
        i.simplify();
    }

    public void addVariable(String name, int value) {
        constants.put(labelName(name), new Integer(value));
    }

    public void addRegisterName(String name, String reg) {
        // TODO: error check for invalid register name
        definitions.put(labelName(name), Register.getRegisterByName(reg));
    }

    public Register getRegister(AbstractToken tok) {
        String name = labelName(tok);
        Register reg = Register.getRegisterByName(name);
        if (reg == null)
            reg = (Register) definitions.get(name);

        if (reg == null) ERROR.UnknownRegister(tok);
        return reg;
    }

    public int getVariable(AbstractToken tok) {
        String name = labelName(tok);

        Integer v = (Integer) constants.get(name);
        if (v == null) {
            Item.Label li = (Item.Label) labels.get(name);
            if (li == null) ERROR.UnknownVariable(tok);
            if (li.segment == programSegment && !useByteAddresses )  return li.getByteAddress() >> 1;
            else return li.getByteAddress();
        } else
            return v.intValue();
    }

    private void addItem(Item i) {
        itemList.add(i);
        segment.advance(i.itemSize());
    }

    public static int align(int val, int width) {
        if (val % width == 0) return val;
        return val + (width - (val % width));
    }

    private String labelName(AbstractToken tok) {
        if ( caseSensitivity ) return tok.image;
        else return tok.image.toLowerCase();
    }

    private String labelName(String n) {
        if ( caseSensitivity ) return n;
        else return n.toLowerCase();
    }



    private void addGlobalConstants() {
        // TODO: pull out machine-specific constants to somewhere.
        constant("RAMEND", 4095);

        // TODO: use numbers definition in IORegisterConstants
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

}