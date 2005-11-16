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
 * Creation date: Nov 11, 2005
 */

package avrora.arch.msp430;

import avrora.core.Program;
import avrora.sim.Segment;
import avrora.sim.Simulator;
import avrora.sim.clock.MainClock;
import avrora.sim.mcu.RegisterSet;
import cck.util.Util;

/**
 * @author Ben L. Titzer
 */
public class MSP430Interpreter extends MSP430InstrInterpreter {

    protected int RAMPZ; // location of the RAMPZ IO register
    protected final MainClock clock;
    protected final RegisterSet registers;
    protected final MSP430Instr[] shared_instr;
    protected boolean innerLoop;
    protected boolean shouldRun;
    protected boolean sleeping;

    /**
     * The constructor for the <code>AVRInterpreter</code> class creates a new interpreter
     * and initializes all of the state. This includes allocating memory and segments to
     * represent the SRAM, flash, interrupt table, IO registers, etc.
     */
    public MSP430Interpreter(Simulator simulator, Program p, MSP430Properties pr) {
        // this class and its methods are performance critical
        // observed speedup with this call on Hotspot
        Compiler.compileClass(this.getClass());

        // set up the reference to the simulator
        this.simulator = simulator;

        this.clock = simulator.getClock();

        // if program will not fit onto hardware, error
        if (p.program_end > MSP430DataSegment.DATA_SIZE)
            throw Util.failure("program will not fit into " + MSP430DataSegment.DATA_SIZE + " bytes");

        // allocate register file
        regs = new char[NUM_REGS];

        // initialize IO registers to default values
        registers = simulator.getMicrocontroller().getRegisterSet();

        // allocate SRAM
        sram = new MSP430DataSegment(pr.sram_size, pr.code_start, registers.share(), this);


        // TODO: 1. allocate code space (flash)
        // TODO: 2. set up correct error reporter for SRAM, flash
        // TODO: 3. share instr array correctly (for performance)
        // TODO: 4. allocate interrupt table
        // allocate FLASH
        //Segment.ErrorReporter reporter = new Segment.ErrorReporter();
        //flash = props.codeSegmentFactory.newCodeSegment("flash", this, reporter, p);
        //reporter.segment = flash;
        // for performance, we share a reference to the LegacyInstr[] array representing flash
        //shared_instr = flash.shareCode(null);
        // initialize the interrupt table
        //interrupts = new InterruptTable(this, props.num_interrupts);
        shared_instr = null;
    }


    protected int bit(boolean b) {
        return b ? 1 : 0;
    }
    protected int popByte() {
        int sp = getSP();
        byte b = sram.read(sp);
        regs[SP_REG] = (char)(sp + 1);
        return b;
    }

    protected void pushByte(int b) {
        int sp = getSP();
        int nsp = sp - 1;
        sram.write(nsp, (byte)b);
        regs[SP_REG] = (char)nsp;
    }

    protected void disableInterrupts() {

    }

    protected void enableInterrupts() {

    }

    protected int popWord() {
        byte b1 = (byte)popByte();
        byte b2 = (byte)popByte();
        return uword(b1, b2);
    }

    protected void pushWord(int b) {
        pushByte(high(b));
        pushByte(low(b));
    }
}
