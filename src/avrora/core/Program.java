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
 * @author Ben L. Titzer
 * @see Instr
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

    private static final Instr NOP = new Instr.NOP(0);

    public class Impression {

        private Instr[] imp_instrs;
        private byte[] imp_data;

        private int imp_start;
        private int imp_max;

        Impression(int max) {
            imp_start = program_start;
            imp_max = max;

            realloc(data, instrs, imp_start, program_end);
        }

        public byte readProgramByte(int address) {
            try {
                return imp_data[address - imp_start];
            } catch (ArrayIndexOutOfBoundsException e ) {
                return 0;
            }
        }

        public void writeProgramByte(byte val, int address) {
            try {
                imp_data[address - imp_start] = val;
            } catch ( ArrayIndexOutOfBoundsException e ) {
                // writing beyond the end of flash is ignored
                if ( address < 0 || address >= imp_max ) return;
                resize(address);
                imp_data[address - imp_start] = val;
            }
        }

        private void resize(int address) {
            if ( address < imp_start )
                realloc(imp_data, imp_instrs, address, imp_start + imp_data.length);
            else {
                // allocate 256 more bytes at the end
                address += 256;
                if ( address > imp_max ) address = imp_max;
                realloc(imp_data, imp_instrs, imp_start, address);

            }
        }

        public Instr readInstr(int address) {
            try {
                return imp_instrs[address - imp_start];
            } catch (ArrayIndexOutOfBoundsException e ) {
                return NOP;
            }
        }

        public void writeInstr(Instr i, int address) {
            try {
                imp_instrs[address - imp_start] = i;
            } catch ( ArrayIndexOutOfBoundsException e ) {
                // writing beyond the end of flash is ignored
                if ( address < 0 || address >= imp_max ) return;
                resize(address);
                imp_instrs[address - imp_start] = i;
            }
        }

        private void realloc(byte[] orig_data, Instr[] orig_instrs, int new_start, int new_end) {
            imp_instrs = new Instr[new_end - new_start];
            System.arraycopy(orig_instrs, 0, imp_instrs, imp_start - new_start, orig_instrs.length);
            imp_data = new byte[new_end - new_start];
            System.arraycopy(orig_data, 0, imp_data, imp_start - new_start, orig_data.length);
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

    protected final byte[]  data;
    protected final Instr[] instrs;

    public boolean caseSensitive;

    public Program(int pstart, int pend, int dstart, int dend, int estart, int eend) {
        program_start = pstart;
        program_end = pend;
        program_length = pend - pstart;
        data_start = dstart;
        data_end = dend;
        eeprom_start = estart;
        eeprom_end = eend;

        data = new byte[program_end - program_start];
        instrs = new Instr[program_end - program_start];

        labels = new HashMap();
    }

    public void writeInstr(Instr i, int address) {
        int size = i.getSize();
        checkAddress(address);
        checkAddress(address + size - 1);

        instrs[address - program_start] = i;
        // TODO: fixme, misaligned instructions!
        for (int cntr = 1; cntr < size; cntr++) {
            instrs[address - program_start + cntr] = null;
        }
    }

    public Instr readInstr(int address) {
        checkAddress(address);
        return instrs[address - program_start];
    }

    public void writeProgramByte(byte val, int byteAddress) {
        checkAddress(byteAddress);
        int offset = byteAddress - program_start;
        writeByteInto(val, offset);
    }

    private void writeByteInto(byte val, int offset) {
        data[offset] = val;
    }

    public void writeProgramBytes(byte[] val, int byteAddress) {
        checkAddress(byteAddress);
        checkAddress(byteAddress + val.length - 1);
        int offset = byteAddress - program_start;
        for (int cntr = 0; cntr < val.length; cntr++)
            writeByteInto(val[cntr], offset + cntr);
    }

    public Label newProgramLabel(String name, int byteAddress) {
        Label label = new ProgramLabel(name, byteAddress);
        labels.put(labelName(name), label);
        return label;
    }

    public Label newDataLabel(String name, int byteAddress) {
        Label label = new DataLabel(name, byteAddress);
        labels.put(labelName(name), label);
        return label;
    }

    public Label newEEPromLabel(String name, int byteAddress) {
        Label label = new EEPromLabel(name, byteAddress);
        labels.put(labelName(name), label);
        return label;
    }

    public Label getLabel(String name) {
        return (Label) labels.get(labelName(name));
    }

    private String labelName(String n) {
        if (caseSensitive)
            return n;
        else
            return n.toLowerCase();
    }

    public Impression makeNewImpression(int program_max) {
        return new Impression(program_max);
    }

    protected void checkAddress(int addr) {
        // TODO: throw correct error type
        if (addr < program_start || addr >= program_end)
            throw Avrora.failure("address out of range: " + addr);
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

        Instr i = instrs[cursor];

        if ( i != null ) {
            p.println(i.getVariant() + " " + i.getOperands());

            return i.getSize();
        } else {
            p.print(".db ");
            int count;

            for (count = 1; count < 16 && cursor + count < program_length; count++) {
                if (instrs[cursor + count] != null) break;
            }

            for (int cntr = 0; cntr < count; cntr++) {
                int address = cursor + cntr + program_start;
                byte v = data[cursor + cntr];
                p.print("0x" + StringUtil.toHex(v, 2));
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
