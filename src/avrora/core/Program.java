/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package avrora.core;

import avrora.Avrora;
import avrora.util.Printer;
import avrora.util.StringUtil;

import java.util.*;

/**
 * The <code>Program</code> class represents a complete program of AVR instructions. It stores the actual
 * instructions and initialized data of the program in one instr4 segment, as well as storing the data space
 * and eeprom space requirements for the program. It contains a map of labels (strings) to addresses, which
 * can be either case sensitive (GAS style) or case insensitive (Atmel style).
 *
 * @author Ben L. Titzer
 * @see Instr
 * @see ControlFlowGraph
 * @see ProcedureMap
 */
public class Program {

    /**
     * The <code>LOCATION_COMPARATOR</code> comparator is used in order to sort locations
     * in the program from lowest address to highest address.
     */
    public static Comparator LOCATION_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            Location l1 = (Location)o1;
            Location l2 = (Location)o2;

            if (l1.address == l2.address) {
                if (l1.name == null) return 1;
                if (l2.name == null) return -1;
                return l1.name.compareTo(l2.name);
            }
            return l1.address - l2.address;
        }
    };

    /**
     * The <code>Location</code> class represents a location in the program; either named by
     * a label, or an unnamed integer address. The location may refer to any of the code, data,
     * or eeprom segments.
     */
    public class Location {
        /**
         * The <code>address</code> field records the address of this label as a byte address.
         */
        public final int address;
        /**
         * The <code>name</code> field records the name of this label.
         */
        public final String name;

        /**
         * The constructor for the <code>Location</code> class creates a new location for the
         * specified lable and address. It is used internally to create labels.
         * @param n the name of the label as a string
         * @param addr the integer address of the location
         */
        Location(String n, int addr) {
            name = n;
            address = addr;
        }

        /**
         * The <code>hashCode()</code> method computes the hash code of this location so that
         * it can be used in any of the standard collection libraries.
         * @return an integer value that represents the hash code
         */
        public int hashCode() {
            if (name == null)
                return address;
            else
                return name.hashCode();
        }

        /**
         * The <code>equals()</code> method compares this location against another object. It will return
         * true if and only if the specified object is an instance of <code>Location</code>, the addresses
         * match, and the names match.
         * @param o the other object to test this location for equality
         * @return true if the other object is equal to this label; false otherwise
         */
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Location)) return false;
            Location l = ((Location)o);
            return l.name.equals(this.name) && l.address == this.address;
        }

        /**
         * The <code>isProgramSegment()</code> method returns whether this label refers to the program
         * segment.
         *
         * @return true if this label refers to the program segment; false otherwise
         */
        public boolean isProgramSegment() {
            return false;
        }

        /**
         * The <code>isDataSegment()</code>  method returns whether this label refers to the data segment.
         *
         * @return true if this label refers to the data segment; false otherwise
         */
        public boolean isDataSegment() {
            return false;
        }

        /**
         * The <code>isEEPromSegment()</code>  method returns whether this label refers to the eeprom
         * segment.
         *
         * @return true if this label refers to the eeprom segment; false otherwise
         */
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
            return seg + '_' + StringUtil.toHex(address, 4);
        }

    }

    /**
     * The <code>ProgramLabel</code> class represents a label within the program that refers to the program
     * segment.
     */
    public class ProgramLabel extends Location {

        ProgramLabel(String n, int a) {
            super(n, a);
        }

        /**
         * The <code>isProgramSegment()</code> method returns whether this label refers to the program
         * segment. For instances of <code>ProgramLabel</code>, this method always returns true.
         *
         * @return true
         */
        public boolean isProgramSegment() {
            return true;
        }
    }

    /**
     * The <code>DataLabel</code> class represents a label within the program that refers to the data
     * segment.
     */
    public class DataLabel extends Location {

        DataLabel(String n, int a) {
            super(n, a);
        }

        /**
         * The <code>isDataSegment()</code> method returns whether this label refers to the data segment. For
         * instances of <code>DataLabel</code>, this method always returns true.
         *
         * @return true
         */
        public boolean isDataSegment() {
            return true;
        }
    }

    /**
     * The <code>EEPromLabel</code> class represents a label within the program that refers to the eeprom
     * segment.
     */
    public class EEPromLabel extends Location {

        EEPromLabel(String n, int a) {
            super(n, a);
        }

        /**
         * The <code>isEEPromSegment()</code> method returns whether this label refers to the eeprom segment.
         * For instances of <code>EEPromLabel</code>, this method always returns true.
         *
         * @return true
         */
        public boolean isEEPromSegment() {
            return true;
        }
    }

    private final HashMap labels;

    private final HashMap indirectEdges;

    private SourceMapping sourceMapping;

    /**
     * The <code>program_start</code> field records the lowest address in the program segment that contains
     * valid code or data.
     */
    public final int program_start;

    /**
     * The <code>program_end</code> field records the address following the highest address in the program
     * segment that contains valid code or data.
     */
    public final int program_end;

    /**
     * The <code>program_length</code> field records the size of the program (the difference between
     * <code>program_start</code> and <code>program_end</code>.
     */
    public final int program_length;

    /**
     * The <code>data_start</code> field records the lowest address of declared, labelled memory in the data
     * segment.
     */
    public final int data_start;

    /**
     * The <code>data_end</code> field records the address following the highest address in the program with
     * declared, labelled memory in the data segment.
     */
    public final int data_end;

    /**
     * The <code>eeprom_start</code> field records the lowest address of declared, labelled memory in the
     * eeprom segment.
     */
    public final int eeprom_start;

    /**
     * The <code>eeprom_end</code> field records the address following the highest address in the program with
     * declared, labelled memory in the eeprom segment.
     */
    public final int eeprom_end;

    /**
     * The <code>flash_data</code> field stores a reference to the array that contains the raw data (bytes) of the
     * program segment. NO EFFORT IS MADE IN THIS CLASS TO KEEP THIS CONSISTENT WITH THE INSTRUCTION
     * REPRESENTATIONS.
     */
    protected final byte[] flash_data;

    /**
     * The <code>flash_instrs</code> field stores a reference to the array that contains the instruction
     * representations of the program segment. NO EFFORT IS MADE IN THIS CLASS TO KEEP THIS CONSISTENT WITH
     * THE RAW DATA OF THE PROGRAM SEGMENT.
     */
    protected final Instr[] flash_instrs;

    /**
     * The <code>caseSensitive</code> field controls whether label searching is case sensitive or not. Some
     * program representations use case sensitive labels, and some do not.
     */
    public boolean caseSensitive;

    /**
     * The constructor of the <code>Program</code> class builds an internal representation of the program that
     * is initially empty, but has the given parameters in terms of how big segments are and where they
     * start.
     *
     * @param pstart the start of the program segment
     * @param pend   the end of the program segment
     * @param dstart the start of the data segment
     * @param dend   the end of the data segment
     * @param estart the start of the eeprom segment
     * @param eend   the end of the eeprom segment
     */
    public Program(int pstart, int pend, int dstart, int dend, int estart, int eend) {
        program_start = pstart;
        program_end = pend;
        program_length = pend - pstart;
        data_start = dstart;
        data_end = dend;
        eeprom_start = estart;
        eeprom_end = eend;

        flash_data = new byte[program_end - program_start];
        flash_instrs = new Instr[program_end - program_start];

        labels = new HashMap();
        indirectEdges = new HashMap();
    }

    /**
     * The <code>writeInstr()</code> method is used to write an instruction to the internal representation of
     * the program at the given address. No attempt to assemble the instruction machine code is made; thus the
     * raw data (if any) at that location in the program will not be modified.
     *
     * @param i       the instruction to write
     * @param address the byte address to write the instruction to that must be aligned on a 2-byte boundary.
     * @throws Avrora.InternalError if the address is not within the limits put on the program instance when
     *                              it was created.
     */
    public void writeInstr(Instr i, int address) {
        int size = i.getSize();
        checkAddress(address);
        checkAddress(address + size - 1);

        flash_instrs[address - program_start] = i;
        for (int cntr = 1; cntr < size; cntr++) {
            flash_instrs[address - program_start + cntr] = null;
        }
    }

    /**
     * The <code>readInstr()</code> method reads an instruction from the specified address in the program. No
     * attempt to disassemble raw data into usable instructions is made, and unaligned accesses will return
     * null.
     *
     * @param address the byte address in the program
     * @return the <code>Instr</code> instance at that address if that address is valid code from creation of
     *         the <code>Program</code> instance; null if the instruction is misaligned or only raw data is
     *         present at that location.
     * @throws Avrora.InternalError if the address is not within the limits put on the program instance when
     *                              it was created.
     */
    public Instr readInstr(int address) {
        checkAddress(address);
        return flash_instrs[address - program_start];
    }

    /**
     * The <code>readProgramByte()</code> method reads a byte into the program segment at the specified byte
     * address. If the address overlaps with an instruction, no effort is made to get the correct encoded byte
     * of the instruction.
     *
     * @param address the program address from which to read the byte
     * @return the byte value of the program segment at that location
     */
    public byte readProgramByte(int address) {
        checkAddress(address);
        return flash_data[address - program_start];
    }

    /**
     * The <code>writeProgramByte()</code> method writes a byte into the program segment at the specified byte
     * address. If the address overlaps with an instruction, no effort is made to keep the instruction
     * representation up to date.
     *
     * @param val         the value to write
     * @param byteAddress the byte address in the program segment to write the byte value to
     */
    public void writeProgramByte(byte val, int byteAddress) {
        checkAddress(byteAddress);
        int offset = byteAddress - program_start;
        writeByteInto(val, offset);
    }

    private void writeByteInto(byte val, int offset) {
        flash_data[offset] = val;
    }

    /**
     * The <code>writeProgramBytes()</code> method writes an array of bytes into the program segment at the
     * specified byte address. If the range of addresses modified overlaps with any instructions, no effort is
     * made to keep the instruction representations up to date.
     *
     * @param val         the byte values to write
     * @param byteAddress the byte address to begin writing the values to
     */
    public void writeProgramBytes(byte[] val, int byteAddress) {
        checkAddress(byteAddress);
        checkAddress(byteAddress + val.length - 1);
        int offset = byteAddress - program_start;
        for (int cntr = 0; cntr < val.length; cntr++)
            writeByteInto(val[cntr], offset + cntr);
    }

    /**
     * The <code>newProgramLabel()</code> method creates a label in the program segment with the specified
     * name at the specified byte address.
     *
     * @param name        the name of the label
     * @param byteAddress the byte address to associate with the label
     * @return an instance of the <code>ProgramLabel</code> class corresponding to this new label
     */
    public ProgramLabel newProgramLabel(String name, int byteAddress) {
        ProgramLabel label = new ProgramLabel(name, byteAddress);
        labels.put(labelName(name), label);
        return label;
    }

    /**
     * The <code>newDataLabel()</code> method creates a label in the data segment with the specified name at
     * the specified byte address.
     *
     * @param name        the name of the label
     * @param byteAddress the byte address to associate with the label
     * @return an instance of the <code>DataLabel</code> class corresponding to this new label
     */
    public DataLabel newDataLabel(String name, int byteAddress) {
        DataLabel label = new DataLabel(name, byteAddress);
        labels.put(labelName(name), label);
        return label;
    }

    /**
     * The <code>newEEPromLabel()</code> method creates a label in the eeprom segment with the specified name
     * at the specified byte address.
     *
     * @param name        the name of the label
     * @param byteAddress the byte address to associate with the label
     * @return an instance of the <code>EEPromLabel</code> class corresponding to this new label
     */
    public EEPromLabel newEEPromLabel(String name, int byteAddress) {
        EEPromLabel label = new EEPromLabel(name, byteAddress);
        labels.put(labelName(name), label);
        return label;
    }

    /**
     * The <code>getLabel()</code> method searches for a label with a given name within the program, in any
     * section.
     *
     * @param name the string name of the label
     * @return an instance of <code>Label</code> if the label exists; null otherwise
     */
    public Location getLabel(String name) {
        return (Location)labels.get(labelName(name));
    }

    /**
     * The <code>getProgramLocation()</code> method will convert the specified string into a program location,
     * i.e. a location in the program segment.
     * If the string begins with "0x", then it is considered a hexadecimal address and a location will be
     * returned that corresponds to that address in the program segment.
     * Otherwise, the string is considered to be a string and the method will attempt to look for a label with
     * that name.
     * @param s the string name to look up in the program segment
     * @return an instance of the <code>Location</code> representing that location in the program segment if the
     * string was either a hexadecimal constant or a valid label in the program segment; null otherwise
     */
    public Location getProgramLocation(String s) {
        if (s.startsWith("0x") | s.startsWith("0X"))
            return new ProgramLabel(null, StringUtil.evaluateIntegerLiteral(s));
        Location l = getLabel(s);
        if (l != null && !l.isProgramSegment()) return null;
        return l;
    }

    private String labelName(String n) {
        if (caseSensitive)
            return n;
        else
            return n.toLowerCase();
    }

    /**
     * The <code>checkAddress()</code> method simply checks an address against the bounds of the program and
     * throws an error if the address is not within the bounds.
     *
     * @param addr the byte address to check
     * @throws Avrora.InternalError if the address is not within the limits of the program segment
     */
    protected void checkAddress(int addr) {
        // TODO: throw correct error type
        if (addr < program_start || addr >= program_end)
            throw Avrora.failure("address out of range: " + StringUtil.addrToString(addr));
    }

    /**
     * The <code>getNextPC()</code> method computes the program counter value of the next instruction
     * following the instruction referenced by the given program counter value. Thus, it simply adds the size
     * of the instruction at the specified pc to the pc. It is useful as a commonly-used utility method.
     *
     * @param pc the program counter location of the current instruction
     * @return the program counter value of the instruction following the specified instruction in program
     *         order
     */
    public int getNextPC(int pc) {
        // TODO: better error checking
        if (pc > program_end)
            throw Avrora.failure("no next PC after: " + StringUtil.addrToString(pc));
        Instr i = readInstr(pc);
        if (i == null) return pc + 2;
        return pc + i.getSize();
    }

    /**
     * The <code>getIndirectEdges</code> returns a list of integers representing the possible target program
     * locations for a given callsite. This is auxilliary information that is supplied at the command line
     * which is used for a variety of analysis questions.
     *
     * @param callsite the program counter location of an indirect branch or call
     * @return a list of <code>java.lang.Integer</code> objects that represent the possible targets of the
     *         call or branch instruction
     */
    public List getIndirectEdges(int callsite) {
        return (List)indirectEdges.get(new Integer(callsite));
    }

    /**
     * The <code>addIndirectEdge</code> adds an indirect edge between a callsite and possible target. This is
     * auxilliary information that is supplied at the command line which is used for a variety of analysis
     * questions.
     *
     * @param callsite the program counter location of the call or branch instruction
     * @param target   the possible target of the call or branch instruction
     */
    public void addIndirectEdge(int callsite, int target) {
        Integer c = new Integer(callsite);
        Integer t = new Integer(target);

        List l = (List)indirectEdges.get(c);

        if (l == null) {
            l = new LinkedList();
            l.add(t);
            indirectEdges.put(c, l);
        } else {
            l.add(t);
        }

    }

    public SourceMapping getSourceMapping() {
        return sourceMapping;
    }

    public void setSourceMapping(SourceMapping s) {
        sourceMapping = s;
    }

    private ControlFlowGraph cfg;

    /**
     * The <code>getCFG()</code> method returns a reference to the control flow graph of the program. This is
     * an instance of <code>ControlFlowGraph</code> that is constructed lazily--i.e. the first time this
     * method is called. No effort is made to keep the control flow graph up to date with a changing program
     * representation; adding instructions or writing bytes into the program segment of the program will not
     * alter the CFG once it has been constructed.
     *
     * @return a reference to the <code>ControlFlowGraph</code> instance that represents the control flow
     *         graph for this program
     */
    public synchronized ControlFlowGraph getCFG() {
        if (cfg == null) {
            cfg = new CFGBuilder(this).buildCFG();
        }
        return cfg;
    }

    public HashMap getLabels() {
        return labels;
    }
}
