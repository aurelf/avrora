package avrora.core;

import avrora.Avrora;
import avrora.util.Printer;
import avrora.util.StringUtil;

import java.util.HashMap;
import java.util.Iterator;

/**
 * The <code>Program</code> class represents a complete program of AVR
 * instructions. It stores the actual instructions and initialized data
 * of the program in one instr4 segment, as well as storing the data
 * space and eeprom space requirements for the program. It contains
 * a map of labels (strings) to addresses, which can be either
 * case sensitive (GAS style) or case insensitive (Atmel style).
 *
 * @see Instr 
 * @author Ben L. Titzer
 */
public class Program {

    public abstract class Label {

        public final String name;
        public final int address;

        Label(String n, int a) {
            name = n;
            address = a;
        }

        public boolean isProgramSegment() {
            return false;
        }

        public boolean isDataSegment() {
            return false;
        }

        public boolean isEEPromSegment() {
            return false;
        }

        public String toString() {
            String seg = "unknown";
            if (isProgramSegment())
                seg = "program";
            else if (isDataSegment())
                seg = "data";
            else if (isEEPromSegment()) seg = "eeprom";
            return seg + "_" + StringUtil.toHex(address, 4);
        }
    }

    public class ProgramLabel extends Label {

        ProgramLabel(String n, int a) {
            super(n, a);
        }

        public boolean isProgramSegment() {
            return true;
        }
    }

    public class DataLabel extends Label {

        DataLabel(String n, int a) {
            super(n, a);
        }

        public boolean isDataSegment() {
            return true;
        }
    }

    public class EEPromLabel extends Label {

        EEPromLabel(String n, int a) {
            super(n, a);
        }

        public boolean isEEPromSegment() {
            return true;
        }
    }

    private final HashMap labels;

    public final int program_start;
    public final int program_end;
    public final int program_length;
    public final int data_start;
    public final int data_end;
    public final int eeprom_start;
    public final int eeprom_end;

    protected final Elem[] program;

    public boolean caseSensitive;

    public Program(int pstart, int pend, int dstart, int dend, int estart, int eend) {
        program_start = pstart;
        program_end = pend;
        program_length = pend - pstart;
        data_start = dstart;
        data_end = dend;
        eeprom_start = estart;
        eeprom_end = eend;

        program = new Elem[program_end - program_start];

        for (int cntr = 0; cntr < program_length; cntr++)
            program[cntr] = Elem.UNINIT;

        labels = new HashMap();
    }

    public void writeInstr(Instr i, int address) {
        int size = i.getSize();
        checkAddress(address);
        checkAddress(address + size - 1);

        program[address - program_start] = i;
        for (int cntr = 1; cntr < size; cntr++) {
            program[address - program_start + cntr] = Elem.INSTR_MIDDLE;
        }
    }

    public Instr readInstr(int address) {
        checkAddress(address);
        return program[address - program_start].asInstr(address);
    }

    public void writeProgramByte(byte val, int address) {
        checkAddress(address);
        int offset = address - program_start;
        writeByteInto(val, offset);
    }

    private void writeByteInto(byte val, int offset) {
        Elem e = program[offset];
        if (e == Elem.UNINIT) {
            Data d = new Data(val);
            program[offset] = d;
        } else if (e.isInstr()) {
            // TODO: throw correct error
            throw Avrora.failure("cannot overwrite instruction");
        } else {
            Data d = (Data) e;
            d.value = val;
        }
    }

    public void writeProgramBytes(byte[] val, int address) {
        checkAddress(address);
        checkAddress(address + val.length - 1);
        int offset = address - program_start;
        for (int cntr = 0; cntr < val.length; cntr++)
            writeByteInto(val[cntr], offset + cntr);
    }

    public Label newProgramLabel(String name, int address) {
        Label label = new ProgramLabel(name, address * 2);
        labels.put(labelName(name), label);
        return label;
    }

    public Label newDataLabel(String name, int address) {
        Label label = new DataLabel(name, address);
        labels.put(labelName(name), label);
        return label;
    }

    public Label newEEPromLabel(String name, int address) {
        Label label = new EEPromLabel(name, address);
        labels.put(labelName(name), label);
        return label;
    }

    public Label getLabel(String name) {
        return (Label) labels.get(labelName(name));
    }

    private String labelName(String n) {
        if ( caseSensitive )
            return n;
        else return n.toLowerCase();
    }

    public Elem[] makeImpression(int size) {
        if (size < program_end)
            throw Avrora.failure("program will not fit into " + size + " bytes, requires " + program_end);

        Elem[] elems = new Elem[size];

        int cntr = 0;

        for (; cntr < program_start; cntr++)
            elems[cntr] = Elem.UNINIT;
        for (; cntr < program_end; cntr++) {
            Elem e = program[cntr - program_start];
            // make a copy of initialized data.
            Elem d = e.isData() ? new Data(e.asData(cntr).value) : e;
            elems[cntr] = d;
        }
        for (; cntr < size; cntr++)
            elems[cntr] = Elem.UNINIT;

        return elems;
    }

    public Elem[] makeImpression() {
        return makeImpression(program_end);
    }

    protected void checkAddress(int addr) {
        // TODO: throw correct error type
        if (addr < program_start || addr >= program_end)
            throw Avrora.failure("address out of range: "+addr);
    }

    public void dump() {
        Printer p = Printer.STDOUT;

        dumpProgram(p);

        dumpLabels(p);
    }

    private void dumpProgram(Printer p) {
        p.println("; -----------------------------------");
        p.println(";  Dump of program segment: ");
        p.println(";    low = 0x" + StringUtil.toHex(program_start, 4) +
                ", high = 0x" + StringUtil.toHex(program_end, 4));
        p.println("; -----------------------------------");
        p.println(".cseg");

        for (int cursor = 0; cursor < program_length;) {
            cursor += outputRow(p, cursor);
        }

        p.println("");
    }

    private int outputRow(Printer p, int cursor) {
        p.print("program_" + StringUtil.toHex(cursor + program_start, 4) + ": ");

        Elem e = program[cursor];

        if (e.isInstr()) {
            Instr i = (Instr) e;

            p.println(i.getVariant() + " " + i.getOperands());

            return i.getSize();
        } else {
            p.print(".db ");
            int count;

            for (count = 1; count < 16 && cursor + count < program_length; count++) {
                if (program[cursor + count].isInstr()) break;
            }

            for (int cntr = 0; cntr < count; cntr++) {
                int address = cursor + cntr + program_start;
                Elem d = program[cursor + cntr];
                p.print("0x" + StringUtil.toHex(d.asData(address).value, 2));
                if (cntr != count - 1) p.print(", ");
            }

            p.println("");

            return count;
        }
    }

    private void dumpLabels(Printer p) {

        Iterator i = labels.values().iterator();
        while (i.hasNext()) {
            Label l = (Label) i.next();
            p.println("; label " + l.name + " = " + l.toString());
        }
    }

}
