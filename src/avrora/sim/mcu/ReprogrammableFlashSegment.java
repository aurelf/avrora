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
public abstract class ReprogrammableFlashSegment extends FlashSegment {

    private static final int ERASE_CYCLES = 27280; // from hardware manual
    private static final int WRITE_CYCLES = 27280; // from hardware manual
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

    Disassembler disassembler = new Disassembler();

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

    byte[] buffer;

    final SPMCSR_reg SPMCSR;
    final int pagesize;
    final int addressMask;
    final MainClock mainClock;

    public ReprogrammableFlashSegment(String name, int size, AtmelMicrocontroller mcu, ErrorReporter er, int pagesize) {
        super(name, size, mcu.getSimulator().getInterpreter(), er);
        SPMCSR = new SPMCSR_reg();
        mainClock = mcu.getClockDomain().getMainClock();
        this.pagesize = pagesize;
        this.addressMask = Arithmetic.getBitRangeMask(0, pagesize);
        resetBuffer();
    }

    public void update() {
        // TODO: check that PC is in the bootloader section
        int pc = interpreter.getPC();
        int Z = interpreter.getRegisterWord(Register.Z);
        int pageoffset = 2 * (Z & addressMask);
        int pagenum = Z >> pagesize;
        // do not update the ReprogrammableFlashSegment register yet
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

    void replaceInstr(int address, Instr i) {
        throw Avrora.unimplemented();
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
