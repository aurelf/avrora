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

import avrora.arch.legacy.LegacyInstr;
import cck.text.StringUtil;
import cck.util.Util;
import java.util.*;

/**
 * The <code>Program</code> class represents a complete program of AVR instructions. It stores the actual
 * instructions and initialized data of the program in one instr4 segment, as well as storing the data space
 * and eeprom space requirements for the program. It contains a map of labels (strings) to addresses, which
 * can be either case sensitive (GAS style) or case insensitive (Atmel style).
 *
 * @author Ben L. Titzer
 * @see LegacyInstr
 * @see ControlFlowGraph
 * @see ProcedureMap
 */
public class Program {

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
     * eepromiwswcbimh segment.
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
    protected final LegacyInstr[] flash_instrs;

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

        int size = program_end - program_start;
        flash_data = new byte[size];
        flash_instrs = new LegacyInstr[size];
        Arrays.fill(flash_data, (byte)0xff);

        indirectEdges = new HashMap();
    }

    /**
     * The <code>writeInstr()</code> method is used to write an instruction to the internal representation of
     * the program at the given address. No attempt to assemble the instruction machine code is made; thus the
     * raw data (if any) at that location in the program will not be modified.
     *
     * @param i       the instruction to write
     * @param address the byte address to write the instruction to that must be aligned on a 2-byte boundary.
     * @throws cck.util.Util.InternalError if the address is not within the limits put on the program instance when
     *                              it was created.
     */
    public void writeInstr(LegacyInstr i, int address) {
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
     * @return the <code>LegacyInstr</code> instance at that address if that address is valid code from creation of
     *         the <code>Program</code> instance; null if the instruction is misaligned or only raw data is
     *         present at that location.
     * @throws cck.util.Util.InternalError if the address is not within the limits put on the program instance when
     *                              it was created.
     */
    public LegacyInstr readInstr(int address) {
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
     * The <code>checkAddress()</code> method simply checks an address against the bounds of the program and
     * throws an error if the address is not within the bounds.
     *
     * @param addr the byte address to check
     * @throws cck.util.Util.InternalError if the address is not within the limits of the program segment
     */
    protected void checkAddress(int addr) {
        // TODO: throw correct error type
        if (addr < program_start || addr >= program_end)
            throw Util.failure("address out of range: " + StringUtil.addrToString(addr));
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
            throw Util.failure("no next PC after: " + StringUtil.addrToString(pc));
        LegacyInstr i = readInstr(pc);
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

}
