/**
 * Copyright (c) 2005, Regents of the University of California
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
 *
 * Creation date: Oct 31, 2005
 */

package avrora.arch.avr;

import cck.util.Util;
import cck.util.Arithmetic;
import avrora.sim.*;
import avrora.sim.clock.MainClock;
import avrora.sim.mcu.MicrocontrollerProperties;
import avrora.sim.mcu.RegisterSet;
import avrora.core.Program;

/**
 * @author Ben L. Titzer
 */
public class AVRInterpreter extends AVRInstrInterpreter {

    //=============================================================
    // C O N S T A N T S   F O R   S T A T U S   R E G I S T E R
    //=============================================================
    public static final int SREG_I = 7;
    public static final int SREG_T = 6;
    public static final int SREG_H = 5;
    public static final int SREG_S = 4;
    public static final int SREG_V = 3;
    public static final int SREG_N = 2;
    public static final int SREG_Z = 1;
    public static final int SREG_C = 0;

    private static final int SREG_I_MASK = 1 << SREG_I;
    private static final int SREG_T_MASK = 1 << SREG_T;
    private static final int SREG_H_MASK = 1 << SREG_H;
    private static final int SREG_S_MASK = 1 << SREG_S;
    private static final int SREG_V_MASK = 1 << SREG_V;
    private static final int SREG_N_MASK = 1 << SREG_N;
    private static final int SREG_Z_MASK = 1 << SREG_Z;
    private static final int SREG_C_MASK = 1;

    private class SREG_reg implements ActiveRegister {

        /**
         * The <code>read()</code> method reads the 8-bit value of the IO register as a byte. For simple
         * <code>RWIOReg</code> instances, this simply returns the internally stored value.
         *
         * @return the value of the register as a byte
         */
        public byte read() {
            int value = 0;
            if (I) value |= SREG_I_MASK;
            if (T) value |= SREG_T_MASK;
            if (H) value |= SREG_H_MASK;
            if (S) value |= SREG_S_MASK;
            if (V) value |= SREG_V_MASK;
            if (N) value |= SREG_N_MASK;
            if (Z) value |= SREG_Z_MASK;
            if (C) value |= SREG_C_MASK;
            return (byte) value;
        }

        /**
         * The <code>write()</code> method writes an 8-bit value to the IO register as a byte. For simple
         * <code>RWIOReg</code> instances, this simply writes the internally stored value.
         *
         * @param val the value to write
         */
        public void write(byte val) {
            if ((val & SREG_I_MASK) != 0) enableInterrupts();
            else disableInterrupts();
            T = (val & SREG_T_MASK) != 0;
            H = (val & SREG_H_MASK) != 0;
            S = (val & SREG_S_MASK) != 0;
            V = (val & SREG_V_MASK) != 0;
            N = (val & SREG_N_MASK) != 0;
            Z = (val & SREG_Z_MASK) != 0;
            C = (val & SREG_C_MASK) != 0;
        }

        /**
         * The <code>readBit()</code> method reads a single bit from the IO register.
         *
         * @param num the number of the bit to read
         * @return the value of the bit as a boolean
         */
        public boolean readBit(int num) {
            switch (num) {
                case SREG_I: return I;
                case SREG_T: return T;
                case SREG_H: return H;
                case SREG_S: return S;
                case SREG_V: return V;
                case SREG_N: return N;
                case SREG_Z: return Z;
                case SREG_C: return C;
            }
            throw Util.failure("bit out of range: " + num);
        }

        public void writeBit(int num, boolean value) {
            switch (num) {
                case SREG_I:
                    if (value) enableInterrupts();
                    else disableInterrupts();
                    break;
                case SREG_T: T = value; break;
                case SREG_H: H = value; break;
                case SREG_S: S = value; break;
                case SREG_V: V = value; break;
                case SREG_N: N = value; break;
                case SREG_Z: Z = value; break;
                case SREG_C: C = value; break;
                default:
                    throw Util.failure("bit out of range: " + num);
            }
        }
    }

    protected int RAMPZ; // location of the RAMPZ IO register
    protected final MainClock clock;
    protected final RegisterSet registers;
    protected final AVRInstr[] shared_instr;
    protected final RWRegister SPL_reg;
    protected final RWRegister SPH_reg;
    protected boolean innerLoop;
    protected boolean shouldRun;
    protected boolean sleeping;

    /**
     * The constructor for the <code>AVRInterpreter</code> class creates a new interpreter
     * and initializes all of the state. This includes allocating memory and segments to
     * represent the SRAM, flash, interrupt table, IO registers, etc.
     */
    public AVRInterpreter(Simulator simulator, Program p, MicrocontrollerProperties pr) {
        // this class and its methods are performance critical
        // observed speedup with this call on Hotspot
        Compiler.compileClass(this.getClass());

        // set up the reference to the simulator
        this.simulator = simulator;

        // set up reference to the main clock
        this.clock = simulator.getClock();

        // get the number of the status register
        SREG = pr.getIOReg("SREG");

        // look for the RAMPZ register
        if ( pr.hasIOReg("RAMPZ") ) RAMPZ = pr.getIOReg("RAMPZ");
        else RAMPZ = -1;

        // if program will not fit onto hardware, error
        if (p.program_end > pr.flash_size)
            throw Util.failure("program will not fit into " + pr.flash_size + " bytes");

        // initialize IO register set to default values
        registers = simulator.getMicrocontroller().getRegisterSet();

        // get a reference to the ioregs array
        ioregs = registers.share();

        // compute the total size of SRAM
        int sram_total = NUM_REGS + pr.ioreg_size + pr.sram_size;

        // allocate SRAM
        sram = new AVRDataSegment(sram_total, ioregs);

        // set reference to registers
        regs = sram.exposeRegisters();

        // allocate the status register
        SREG_reg = ioregs[SREG] = new SREG_reg();

        // get references to stack pointer registers
        SPL_reg = (RWRegister) ioregs[pr.getIOReg("SPL")];
        SPH_reg = (RWRegister) ioregs[pr.getIOReg("SPH")];

        // TODO: 1. allocate code space (flash)
        // TODO: 2. set up correct error reporter for SRAM, flash
        // TODO: 3. share instr array correctly (for performance)
        // TODO: 4. allocate interrupt table
        // allocate FLASH
        //Segment.ErrorReporter reporter = new Segment.ErrorReporter();
        //flash = props.codeSegmentFactory.newCodeSegment("flash", this, reporter, p);
        //reporter.segment = flash;
        // for performance, we share a reference to the Instr[] array representing flash
        //shared_instr = flash.shareCode(null);
        // initialize the interrupt table
        //interrupts = new InterruptTable(this, props.num_interrupts);
        shared_instr = null;
    }

    public int getSP() {
        byte low = SPL_reg.value;
        byte high = SPH_reg.value;
        return Arithmetic.uword(low, high);
    }

    protected void setSP(int val) {
        SPL_reg.value = (Arithmetic.low(val));
        SPH_reg.value = (Arithmetic.high(val));
    }

    protected int popByte() {
        int address = getSP() + 1;
        setSP(address);
        return sram.read(address);
    }

    protected void pushByte(int val) {
        int address = getSP();
        setSP(address - 1);
        sram.write(address, (byte)val);
    }

    protected int extended(int addr) {
        if ( RAMPZ > 0 ) return (ioregs[RAMPZ].read() & 1) << 16 | addr;
        else return addr;
    }

    protected void enableInterrupts() {
        I = true;
        interrupts.enableAll();
    }

    protected void disableInterrupts() {
        I = true;
        interrupts.disableAll();
    }

    protected void enterSleepMode() {
        sleeping = true;
        innerLoop = false;
        simulator.getMicrocontroller().sleep();
    }

    protected void storeProgramMemory() {
        flash.update();
    }

    protected void stop() {
        shouldRun = false;
        innerLoop = false;
    }

    protected void skip() {
        AVRInstr i = null;
        int size = i.getSize();
        if ( size == 2 ) cycles += 1;
        else cycles += 2;
        nextpc += size;
    }

    protected boolean getIORbit(int ior, int bit) {
        return ioregs[ior].readBit(bit);
    }

    protected void setIORbit(int ior, int bit, boolean v) {
        ioregs[ior].writeBit(bit, v);
    }


}
