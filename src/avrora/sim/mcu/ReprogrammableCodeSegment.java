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

package avrora.sim.mcu;

import avrora.sim.*;
import avrora.sim.util.MulticastProbe;
import avrora.util.Arithmetic;
import avrora.util.StringUtil;
import avrora.core.*;
import avrora.Avrora;

/**
 * @author Ben L. Titzer
 */
public class ReprogrammableCodeSegment extends CodeSegment {

    private static final double ERASE_MS_MIN = 3.7;
    private static final double WRITE_MS_MIN = 3.7;
    private static final double ERASE_MS_MAX = 4.5;
    private static final double WRITE_MS_MAX = 4.5;
    private static final int SPM_TIMEOUT = 4;

    private static final int STATE_NONE = 0;
    private static final int STATE_PGERASE = 1 << 1 | 1;
    private static final int STATE_RWWSRE  = 1 << 4 | 1;
    private static final int STATE_BLBSET  = 1 << 3 | 1;
    private static final int STATE_FILL    = 1;
    private static final int STATE_PGWRITE = 1 << 2 | 1;

    private static final int SPM_READY = 35;
    private static final int SPMCSR_LOWERBITS = 0x1f;

    private static final byte DEFAULT_VALUE = (byte)0xff;

    /**
     * The <code>ReprogrammableCodeSegment.Factory</code> class represents a class capable of creating a new
     * code segment for a new interpreter.
     */
    public static class Factory implements CodeSegment.Factory {
        final int pagesize;
        final int size;

        Factory(int size, int pagesize) {
            this.size = size;
            this.pagesize = pagesize;
        }

        public CodeSegment newCodeSegment(String name, BaseInterpreter bi, ErrorReporter er, Program p) {
            CodeSegment cs;
            if ( p != null ) {
                cs = new ReprogrammableCodeSegment(name, p.program_end, bi, er, pagesize);
                cs.load(p);
            } else {
                cs = new ReprogrammableCodeSegment(name, size, bi, er, pagesize);
            }
            return cs;
        }
    }

    /**
     * The <code>SPMCSR_reg</code> class represents an instanceof the <code>ActiveRegister</code> interface
     * that is used to represent the SPMCSR register on the ATMega family microcontrollers. This register
     * is used in reprogramming the flash memory from within the program.
     */
    private class SPMCSR_reg extends RWRegister {
        ResetEvent reset = new ResetEvent();

        public void write(byte val) {

            int lower = val & SPMCSR_LOWERBITS;
            switch ( lower ) {
                case STATE_PGERASE:
                case STATE_RWWSRE:
                case STATE_BLBSET:
                case STATE_FILL:
                case STATE_PGWRITE:
                    mainClock.removeEvent(reset);
                    mainClock.insertEvent(reset, SPM_TIMEOUT+2);
                    break;
                default:
                    lower = STATE_NONE;
            }

            this.value = (byte)(val & (~ SPMCSR_LOWERBITS) | lower);

            if ( Arithmetic.getBit(value, 7) && !Arithmetic.getBit(value, 0) ) {
                // if SPMIE is set and SPMEN bit is not set
                interpreter.postInterrupt(SPM_READY);
            } else {
                interpreter.unpostInterrupt(SPM_READY);
            }

        }

        public void writeBit(int bit, boolean val) {
            int nvalue = Arithmetic.setBit(value, bit, val);
            write((byte)nvalue);
        }

        class ResetEvent implements Simulator.Event {
            public void fire() {
                reset();
            }

        }

        int getState() {
            return value & SPMCSR_LOWERBITS;
        }

        void reset() {
            write((byte)(value & (~ SPMCSR_LOWERBITS)));
        }
    }

    /**
     * The <code>disassembler</code> field stores a reference to a disassembler for this segment.
     * This is needed because disassemblers are currently not re-entrant.
     */
    Disassembler disassembler = new Disassembler();

    /**
     * The <code>buffer</code> field stores a reference to the bytes in the temporary page buffer
     * which is used to rewrite the flash memory.
     */
    byte[] buffer;

    /**
     * The <code>SPMCSR</code> field stores a reference to the SPMCSR register which is an IO register
     * that the program uses to select which flash operations to perform.
     */
    final SPMCSR_reg SPMCSR;

    /**
     * The <code>ERASE_CYCLES</code> field stores the number of cycles needed to complete an erase operation.
     */
    final int ERASE_CYCLES; // from hardware manual

    /**
     * The <code>WRITE_CYCLES</code> field stores the number of cycles needed to complete a write operation.
     */
    final int WRITE_CYCLES; // from hardware manual

    /**
     * The <code>pagesize</code> field stores the number of bits in the page offset field of an address; i.e.
     * it is the log of the size of a page in words.
     */
    final int pagesize;

    /**
     * The <code>addressMask</code> field stores an integer used to mask out the page offset of an address.
     */
    final int addressMask;

    /**
     * The <code>mainClock</code> method stores a reference to the main clock signal of the chip.
     */
    final MainClock mainClock;

    /**
     * The constructor for the <code>ReprogrammableCodeSegment</code> creates a new instance with the specified
     * name, with the specified size, connected to the specified microcontroller, with the given page size.
     * @param name the name of the segment as a string
     * @param size the size of the segment in bytes
     * @param bi the the interpreter the code segment is attached to
     * @param er the error reporter consulted for out of bounds accesses
     * @param pagesize the size of the page offset field of an address into the flash
     */
    public ReprogrammableCodeSegment(String name, int size, BaseInterpreter bi, ErrorReporter er, int pagesize) {
        super(name, size, bi, er);
        SPMCSR = new SPMCSR_reg();
        mainClock = bi.getMainClock();
        this.pagesize = pagesize;
        this.addressMask = Arithmetic.getBitRangeMask(0, pagesize);
        resetBuffer();

        ERASE_CYCLES = (int)((1000*mainClock.getHZ()) / ERASE_MS_MAX);
        WRITE_CYCLES = (int)((1000*mainClock.getHZ()) / WRITE_MS_MAX);
    }

    /**
     * The <code>update()</code> method is called by the interpreter when the program executes an instruction
     * that updates the program memory. For example, the SPM instruction.
     */
    public void update() {
        // TODO: check that PC is in the bootloader section
        int pc = interpreter.getPC();
        int Z = interpreter.getRegisterWord(Register.Z);
        int pageoffset = 2 * (Z & addressMask);
        int pagenum = Z >> pagesize;
        // do not update the ReprogrammableCodeSegment register yet
        int state = SPMCSR.getState();
        switch ( state ) {
            case STATE_PGERASE:
                pageErase(pagenum, pageoffset);
                break;
            case STATE_RWWSRE:
                mainClock.removeEvent(SPMCSR.reset);
                break;
            case STATE_BLBSET:
                mainClock.removeEvent(SPMCSR.reset);
                break;
            case STATE_FILL:
                fillBuffer(pagenum, pageoffset);
                break;
            case STATE_PGWRITE:
                pageWrite(pagenum, pageoffset);
                break;
            default:
        }
    }

    private void pageErase(int pagenum, int pageoffset) {
        mainClock.removeEvent(SPMCSR.reset);
        mainClock.insertEvent(new EraseEvent(pagenum), ERASE_CYCLES);
        resetBuffer();
    }

    private void pageWrite(int pagenum, int pageoffset) {
        mainClock.removeEvent(SPMCSR.reset);
        mainClock.insertEvent(new WriteEvent(pagenum, buffer), WRITE_CYCLES);
        resetBuffer();
    }

    private void fillBuffer(int pagenum, int pageoffset) {
        // write the word in R0:R1 into the buffer
        byte r0 = interpreter.getRegisterByte(Register.R0);
        byte r1 = interpreter.getRegisterByte(Register.R1);
        SPMCSR.reset();
        buffer[pageoffset] = r0;
        buffer[pageoffset+1] = r1;
        mainClock.removeEvent(SPMCSR.reset);
    }

    /**
     * The <code>EraseEvent</code> class is used as an event to schedule the timing of erasing a page.
     * When a page erase operation is begun, this event is inserted into the queue of the simulator
     * so that when it fires, the page in the code segment is erased.
     */
    class EraseEvent implements Simulator.Event {
        int pagenum;

        EraseEvent(int pagenum) {
            this.pagenum = pagenum;
        }

        public void fire() {
            // erase the page
            int size = bufferSize();
            int addr = pagenum * size;
            for ( int offset = 0; offset < size; offset++) {
                int baddr = addr + offset;
                write(baddr, buffer[offset]);
                if ( (offset & 1) == 0)
                    replaceInstr(baddr, new DisassembleInstr(baddr));
            }
            SPMCSR.reset();
        }
    }

    /**
     * The <code>WriteEvent</code> class is used as an event to schedule the timing of writing a page.
     * When a page write operation is begun, this event is inserted into the queue of the simulator so
     * that when it fires, the page in the code segment is written with the contents of the temporary
     * buffer.
      */
    class WriteEvent implements Simulator.Event {
        int pagenum;
        byte[] buffer;

        WriteEvent(int pagenum, byte[] buf) {
            this.pagenum = pagenum;
            this.buffer = buf;
        }

        public void fire() {
            // write the page
            int size = bufferSize();
            int addr = pagenum * size;
            for ( int offset = 0; offset < size; offset++) {
                int baddr = addr + offset;
                write(baddr, DEFAULT_VALUE);
                if ( (offset & 1) == 0)
                    replaceInstr(baddr, new DisassembleInstr(baddr));
            }
            SPMCSR.reset();
        }
    }

    /**
     * The <code>resetBuffer()</code> method resets the temporary buffer used for the SPM instruction
     * to its default value.
     */
    protected void resetBuffer() {
        buffer = new byte[bufferSize()];
        for ( int cntr = 0; cntr < buffer.length; cntr++) {
            buffer[cntr] = DEFAULT_VALUE;
        }
    }

    private int bufferSize() {
        // pagesize stores the number of bits representing the word offset in an address
        return 2 << pagesize;
    }

    /**
     * The <code>DisasssembleInstr</code> class represents an instruction that is used by the
     * interpreter to support dynamic code update. Whenever machine code is altered, this
     * instruction will replace the instruction(s) at that location so that when the program
     * attempts to execute the instruction, it will first be disassembled and then it will
     * be executed.
     */
    public class DisassembleInstr extends Instr {

        protected final int address;

        DisassembleInstr(int addr) {
            super(null);
            address = addr;
        }

        public void accept(InstrVisitor v) {
            try {
                Instr i = disassembler.disassemble(0, segment_data, address);
                replaceInstr(address, i);
                i.accept(v);
            } catch (Disassembler.InvalidInstruction e) {
                throw Avrora.failure("invalid instruction at "+StringUtil.addrToString(address));
            }
        }

        public Instr build(int address, Operand[] ops) {
            throw Avrora.failure("DisassembleInstr should be confined to BaseInterpreter");
        }

        public String getOperands() {
            throw Avrora.failure("DisassembleInstr has no operands");
        }

        public Instr asInstr() {
            try {
                Instr i = disassembler.disassemble(0, segment_data, address);
                replaceInstr(address, i);
                return i;
            } catch (Disassembler.InvalidInstruction e) {
                return null;
            }
        }
    }

}
