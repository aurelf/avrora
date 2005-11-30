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

package avrora.arch.legacy;

import avrora.core.Program;
import avrora.arch.avr.AVRProperties;
import avrora.sim.*;
import avrora.sim.mcu.MCUProperties;
import cck.util.Arithmetic;

/**
 * The <code>LegacyInterpreter</code> class is largely generated from the instruction specification. The
 * framework around the generated code (utilities) has been written by hand, but most of the code for each
 * instruction is generated. Therefore it is not recommended to edit this code extensively.
 *
 * @author Ben L. Titzer
 */
public class LegacyInterpreter extends AtmelInterpreter implements LegacyInstrVisitor {

    public static final Factory FACTORY = new Factory();

    public static class Factory extends InterpreterFactory {
        public Interpreter newInterpreter(Simulator s, Program p, MCUProperties pr) {
            return new LegacyInterpreter(s, p, (AVRProperties)pr);
        }
    }

    public static final LegacyRegister R0 = LegacyRegister.R0;
    public static final LegacyRegister R1 = LegacyRegister.R1;
    public static final LegacyRegister R2 = LegacyRegister.R2;
    public static final LegacyRegister R3 = LegacyRegister.R3;
    public static final LegacyRegister R4 = LegacyRegister.R4;
    public static final LegacyRegister R5 = LegacyRegister.R5;
    public static final LegacyRegister R6 = LegacyRegister.R6;
    public static final LegacyRegister R7 = LegacyRegister.R7;
    public static final LegacyRegister R8 = LegacyRegister.R8;
    public static final LegacyRegister R9 = LegacyRegister.R9;
    public static final LegacyRegister R10 = LegacyRegister.R10;
    public static final LegacyRegister R11 = LegacyRegister.R11;
    public static final LegacyRegister R12 = LegacyRegister.R12;
    public static final LegacyRegister R13 = LegacyRegister.R13;
    public static final LegacyRegister R14 = LegacyRegister.R14;
    public static final LegacyRegister R15 = LegacyRegister.R15;
    public static final LegacyRegister R16 = LegacyRegister.R16;
    public static final LegacyRegister R17 = LegacyRegister.R17;
    public static final LegacyRegister R18 = LegacyRegister.R18;
    public static final LegacyRegister R19 = LegacyRegister.R19;
    public static final LegacyRegister R20 = LegacyRegister.R20;
    public static final LegacyRegister R21 = LegacyRegister.R21;
    public static final LegacyRegister R22 = LegacyRegister.R22;
    public static final LegacyRegister R23 = LegacyRegister.R23;
    public static final LegacyRegister R24 = LegacyRegister.R24;
    public static final LegacyRegister R25 = LegacyRegister.R25;
    public static final LegacyRegister R26 = LegacyRegister.R26;
    public static final LegacyRegister R27 = LegacyRegister.R27;
    public static final LegacyRegister R28 = LegacyRegister.R28;
    public static final LegacyRegister R29 = LegacyRegister.R29;
    public static final LegacyRegister R30 = LegacyRegister.R30;
    public static final LegacyRegister R31 = LegacyRegister.R31;

    public static final LegacyRegister RX = LegacyRegister.X;
    public static final LegacyRegister RY = LegacyRegister.Y;
    public static final LegacyRegister RZ = LegacyRegister.Z;

    /**
     * The constructor for the <code>Interpreter</code> class builds the internal data structures needed to
     * store the complete state of the machine, including registers, IO registers, the SRAM, and the flash.
     * All IO registers are initialized to be instances of <code>RWIOReg</code>. Reserved and special IO
     * registers must be inserted by the <code>getIOReg()</code> and <code>writeIOReg()</code> methods.
     *
     * @param s The simulator attached to this interpreter
     * @param p the program to construct the state for
     * @param pr the properties of the microcontroller being simulated
     */
    protected LegacyInterpreter(Simulator s, Program p, AVRProperties pr) {
        super(s, p, pr);
        // this class and its methods are performance critical
        // observed speedup with this call on Hotspot
        Compiler.compileClass(this.getClass());
    }

    protected void runLoop() {

        pc = bootPC;
        nextPC = pc;
        cyclesConsumed = 0;

        while (shouldRun) {

            // TODO: would a "mode" and switch be faster than several branches?
            if (delayCycles > 0) {
                advanceClock(delayCycles);
                delayCycles = 0;
            }

            // TODO: do this with an event fired after the RETI instruction?
            if (justReturnedFromInterrupt) {
                // don't process the interrupt if we just returned from
                // an interrupt handler, because the hardware manual says
                // that at least one instruction is executed after
                // returning from an interrupt.
                justReturnedFromInterrupt = false;
            } else if (I) {

                // check if there are any pending (posted) interrupts
                long pendingInterrupts = interrupts.getPendingInterrupts();
                if (pendingInterrupts != 0) {
                    invokeInterrupt(pendingInterrupts);
                }
            }

            if (sleeping)
                sleepLoop();
            else {
                if (globalProbe.isEmpty())
                    fastLoop();
                else
                    instrumentedLoop();
            }
        }
    }

    public int step() {
        nextPC = pc;

        // process any delays
        if (delayCycles > 0) {
            advanceClock(1);
            delayCycles--;
            return 1;
        }

        // handle any interrupts
        if (justReturnedFromInterrupt) {
            // don't process the interrupt if we just returned from
            // an interrupt handler, because the hardware manual says
            // that at least one instruction is executed after
            // returning from an interrupt.
            justReturnedFromInterrupt = false;
        } else if (I) {

            // check if there are any pending (posted) interrupts
            long pendingInterrupts = interrupts.getPendingInterrupts();
            if (pendingInterrupts != 0) {
                return stepInterrupt(pendingInterrupts);
            }
        }

        // are we sleeping?
        if ( sleeping ) {
            advanceClock(1);
            return 1;
        }

        return stepInstruction();
    }

    private int stepInstruction() {
        int cycles;
        // global probes?
        if ( globalProbe.isEmpty() ) {
            LegacyInstr i = shared_instr[nextPC];

            // visit the actual instruction (or probe)
            i.accept(this);
            // NOTE: commit() might be called twice, but this is ok
            cycles = cyclesConsumed;
            commit();
        } else {
            // get the current instruction
            int curPC = nextPC; // at this point pc == nextPC
            LegacyInstr i = shared_instr[nextPC];

            // visit the actual instruction (or probe)
            globalProbe.fireBefore(state, curPC);
            i.accept(this);
            cycles = cyclesConsumed;
            commit();
            globalProbe.fireAfter(state, curPC);
        }
        return cycles;
    }

    private int stepInterrupt(long pendingInterrupts) {
        // the lowest set bit is the highest priority posted interrupt
        int inum = Arithmetic.lowestBit(pendingInterrupts);

        // fire the interrupt (update flag register(s) state)
        interrupts.beforeInvoke(inum);

        // store the return address
        pushPC(nextPC);

        // set PC to interrupt handler
        nextPC = getInterruptVectorAddress(inum);
        pc = nextPC;

        // disable interrupts
        I = false;

        // advance by just one cycle
        advanceClock(1);

        int cycles = 3; // there are some cycles left-over to delay by
        //time to wake up
        if (sleeping) {
            cycles += simulator.getMicrocontroller().wakeup();
            sleeping = false;
            innerLoop = false;
        }

        delay(cycles);
        // TODO: what about interrupts.afterInvoke(inum)?

        return 1;
    }

    private void invokeInterrupt(long pendingInterrupts) {
        // the lowest set bit is the highest priority posted interrupt
        int lowestbit = Arithmetic.lowestBit(pendingInterrupts);

        // fire the interrupt (update flag register(s) state)
        interrupts.beforeInvoke(lowestbit);

        //time to wake up
        if (sleeping)
            leaveSleepMode();

        // store the return address
        pushPC(nextPC);

        // set PC to interrupt handler
        nextPC = getInterruptVectorAddress(lowestbit);
        pc = nextPC;

        // disable interrupts
        I = false;

        // process any timed events
        advanceClock(4);

        interrupts.afterInvoke(lowestbit);
    }

    private void sleepLoop() {
        innerLoop = true;
        while (innerLoop) {
            clock.skipAhead();
        }
    }

    private void fastLoop() {
        innerLoop = true;
        while (innerLoop) {
            LegacyInstr i = shared_instr[nextPC];

            // visit the actual instruction (or probe)
            i.accept(this);
            // NOTE: commit() might be called twice, but this is ok
            commit();
        }
    }

    private void instrumentedLoop() {
        innerLoop = true;
        while (innerLoop) {
            // get the current instruction
            int curPC = nextPC; // at this point pc == nextPC
            LegacyInstr i = shared_instr[nextPC];

            // visit the actual instruction (or probe)
            globalProbe.fireBefore(state, curPC);
            i.accept(this);
            commit();
            globalProbe.fireAfter(state, curPC);
        }
    }

//--BEGIN INTERPRETER GENERATOR--
    public void visit(LegacyInstr.ADC i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        int tmp_1 = getRegisterUnsigned(i.r2);
        int tmp_2 = bit(C);
        int tmp_3 = tmp_0 + tmp_1 + tmp_2;
        int tmp_4 = (tmp_0 & 0x0000000F);
        int tmp_5 = (tmp_1 & 0x0000000F);
        boolean tmp_6 = ((tmp_0 & 128) != 0);
        boolean tmp_7 = ((tmp_1 & 128) != 0);
        boolean tmp_8 = ((tmp_3 & 128) != 0);
        H = ((tmp_4 + tmp_5 + tmp_2 & 16) != 0);
        C = ((tmp_3 & 256) != 0);
        N = ((tmp_3 & 128) != 0);
        Z = low(tmp_3) == 0;
        V = tmp_6 && tmp_7 && !tmp_8 || !tmp_6 && !tmp_7 && tmp_8;
        S = (N != V);
        byte tmp_9 = low(tmp_3);
        writeRegisterByte(i.r1, tmp_9);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.ADD i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        int tmp_1 = getRegisterUnsigned(i.r2);
        int tmp_2 = 0;
        int tmp_3 = tmp_0 + tmp_1 + tmp_2;
        int tmp_4 = (tmp_0 & 0x0000000F);
        int tmp_5 = (tmp_1 & 0x0000000F);
        boolean tmp_6 = ((tmp_0 & 128) != 0);
        boolean tmp_7 = ((tmp_1 & 128) != 0);
        boolean tmp_8 = ((tmp_3 & 128) != 0);
        H = ((tmp_4 + tmp_5 + tmp_2 & 16) != 0);
        C = ((tmp_3 & 256) != 0);
        N = ((tmp_3 & 128) != 0);
        Z = low(tmp_3) == 0;
        V = tmp_6 && tmp_7 && !tmp_8 || !tmp_6 && !tmp_7 && tmp_8;
        S = (N != V);
        byte tmp_9 = low(tmp_3);
        writeRegisterByte(i.r1, tmp_9);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.ADIW i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1);
        int tmp_1 = tmp_0 + i.imm1;
        boolean tmp_2 = ((tmp_1 & 32768) != 0);
        boolean tmp_3 = ((tmp_0 & 32768) != 0);
        C = !tmp_2 && tmp_3;
        N = tmp_2;
        V = !tmp_3 && tmp_2;
        Z = (tmp_1 & 0x0000FFFF) == 0;
        S = (N != V);
        writeRegisterWord(i.r1, tmp_1);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.AND i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = tmp_0 & tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.ANDI i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_0 & tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.ASR i) {
        nextPC = pc + 2;
        byte tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = tmp_0;
        boolean tmp_2 = ((tmp_0 & 128) != 0);
        int tmp_3 = (tmp_1 & 255) >> 1;
        tmp_3 = Arithmetic.setBit(tmp_3, 7, tmp_2);
        C = ((tmp_1 & 1) != 0);
        N = tmp_2;
        Z = low(tmp_3) == 0;
        V = (N != C);
        S = (N != V);
        byte tmp_4 = low(tmp_3);
        writeRegisterByte(i.r1, tmp_4);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BCLR i) {
        nextPC = pc + 2;
        getIOReg(SREG).writeBit(i.imm1, false);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BLD i) {
        nextPC = pc + 2;
        writeRegisterByte(i.r1, Arithmetic.setBit(getRegisterByte(i.r1), i.imm1, T));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRBC i) {
        nextPC = pc + 2;
        if (!getIOReg(SREG).readBit(i.imm1)) {
            int tmp_0 = i.imm2;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRBS i) {
        nextPC = pc + 2;
        if (getIOReg(SREG).readBit(i.imm1)) {
            int tmp_0 = i.imm2;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRCC i) {
        nextPC = pc + 2;
        if (!C) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRCS i) {
        nextPC = pc + 2;
        if (C) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BREAK i) {
        nextPC = pc + 2;
        stop();
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BREQ i) {
        nextPC = pc + 2;
        if (Z) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRGE i) {
        nextPC = pc + 2;
        if (!S) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRHC i) {
        nextPC = pc + 2;
        if (!H) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRHS i) {
        nextPC = pc + 2;
        if (H) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRID i) {
        nextPC = pc + 2;
        if (!I) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRIE i) {
        nextPC = pc + 2;
        if (I) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRLO i) {
        nextPC = pc + 2;
        if (C) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRLT i) {
        nextPC = pc + 2;
        if (S) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRMI i) {
        nextPC = pc + 2;
        if (N) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRNE i) {
        nextPC = pc + 2;
        if (!Z) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRPL i) {
        nextPC = pc + 2;
        if (!N) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRSH i) {
        nextPC = pc + 2;
        if (!C) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRTC i) {
        nextPC = pc + 2;
        if (!T) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRTS i) {
        nextPC = pc + 2;
        if (T) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRVC i) {
        nextPC = pc + 2;
        if (!V) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BRVS i) {
        nextPC = pc + 2;
        if (V) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BSET i) {
        nextPC = pc + 2;
        getIOReg(SREG).writeBit(i.imm1, true);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.BST i) {
        nextPC = pc + 2;
        T = Arithmetic.getBit(getRegisterByte(i.r1), i.imm1);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CALL i) {
        nextPC = pc + 4;
        int tmp_0 = nextPC;
        tmp_0 = tmp_0 / 2;
        pushByte(low(tmp_0));
        pushByte(high(tmp_0));
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_1 * 2;
        nextPC = tmp_2;
        cyclesConsumed += 4;
    }

    public void visit(LegacyInstr.CBI i) {
        nextPC = pc + 2;
        getIOReg(i.imm1).writeBit(i.imm2, false);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.CBR i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = ~i.imm1;
        int tmp_2 = tmp_0 & tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CLC i) {
        nextPC = pc + 2;
        C = false;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CLH i) {
        nextPC = pc + 2;
        H = false;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CLI i) {
        nextPC = pc + 2;
        disableInterrupts();
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CLN i) {
        nextPC = pc + 2;
        N = false;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CLR i) {
        nextPC = pc + 2;
        S = false;
        V = false;
        N = false;
        Z = true;
        writeRegisterByte(i.r1, low(0));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CLS i) {
        nextPC = pc + 2;
        S = false;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CLT i) {
        nextPC = pc + 2;
        T = false;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CLV i) {
        nextPC = pc + 2;
        V = false;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CLZ i) {
        nextPC = pc + 2;
        Z = false;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.COM i) {
        nextPC = pc + 2;
        int tmp_0 = 255 - getRegisterByte(i.r1);
        C = true;
        N = ((tmp_0 & 128) != 0);
        Z = low(tmp_0) == 0;
        V = false;
        S = (N != V);
        writeRegisterByte(i.r1, low(tmp_0));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CP i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = (N != V);
        byte tmp_10 = low(tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CPC i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = bit(C);
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0 && Z;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = (N != V);
        byte tmp_10 = low(tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CPI i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = (N != V);
        byte tmp_10 = low(tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.CPSE i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = tmp_0;
        int tmp_3 = tmp_1;
        int tmp_4 = 0;
        int tmp_5 = tmp_2 - tmp_3 - tmp_4;
        boolean tmp_6 = ((tmp_2 & 128) != 0);
        boolean tmp_7 = ((tmp_3 & 128) != 0);
        boolean tmp_8 = ((tmp_5 & 128) != 0);
        boolean tmp_9 = ((tmp_2 & 8) != 0);
        boolean tmp_10 = ((tmp_3 & 8) != 0);
        boolean tmp_11 = ((tmp_5 & 8) != 0);
        H = !tmp_9 && tmp_10 || tmp_10 && tmp_11 || tmp_11 && !tmp_9;
        C = !tmp_6 && tmp_7 || tmp_7 && tmp_8 || tmp_8 && !tmp_6;
        N = tmp_8;
        Z = low(tmp_5) == 0;
        V = tmp_6 && !tmp_7 && !tmp_8 || !tmp_6 && tmp_7 && tmp_8;
        S = (N != V);
        byte tmp_12 = low(tmp_5);
        if (tmp_0 == tmp_1) {
            int tmp_13 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_13;
            if (tmp_13 == 4) {
                cyclesConsumed = cyclesConsumed + 2;
            } else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.DEC i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        byte tmp_1 = low(tmp_0 - 1);
        N = ((tmp_1 & 128) != 0);
        Z = tmp_1 == 0;
        V = tmp_0 == 128;
        S = (N != V);
        writeRegisterByte(i.r1, tmp_1);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.EICALL i) {
        nextPC = pc + 2;
        cyclesConsumed += 4;
    }

    public void visit(LegacyInstr.EIJMP i) {
        nextPC = pc + 2;
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.ELPM i) {
        nextPC = pc + 2;
        int tmp_0 = extended(getRegisterWord(RZ));
        writeRegisterByte(R0, getFlashByte(tmp_0));
        cyclesConsumed += 3;
    }

    public void visit(LegacyInstr.ELPMD i) {
        nextPC = pc + 2;
        int tmp_0 = extended(getRegisterWord(RZ));
        writeRegisterByte(i.r1, getFlashByte(tmp_0));
        cyclesConsumed += 3;
    }

    public void visit(LegacyInstr.ELPMPI i) {
        nextPC = pc + 2;
        int tmp_0 = extended(getRegisterWord(RZ));
        writeRegisterByte(i.r1, getFlashByte(tmp_0));
        writeRegisterWord(RZ, tmp_0 + 1);
        cyclesConsumed += 3;
    }

    public void visit(LegacyInstr.EOR i) {
        nextPC = pc + 2;
        byte tmp_0 = low(getRegisterByte(i.r1) ^ getRegisterByte(i.r2));
        N = ((tmp_0 & 128) != 0);
        Z = tmp_0 == 0;
        V = false;
        S = (N != V);
        writeRegisterByte(i.r1, tmp_0);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.FMUL i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1) * getRegisterUnsigned(i.r2) << 1;
        Z = (tmp_0 & 0x0000FFFF) == 0;
        C = ((tmp_0 & 65536) != 0);
        writeRegisterByte(R0, low(tmp_0));
        writeRegisterByte(R1, high(tmp_0));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.FMULS i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterByte(i.r2) << 1;
        Z = (tmp_0 & 0x0000FFFF) == 0;
        C = ((tmp_0 & 65536) != 0);
        writeRegisterByte(R0, low(tmp_0));
        writeRegisterByte(R1, high(tmp_0));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.FMULSU i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterUnsigned(i.r2) << 1;
        Z = (tmp_0 & 0x0000FFFF) == 0;
        C = ((tmp_0 & 65536) != 0);
        writeRegisterByte(R0, low(tmp_0));
        writeRegisterByte(R1, high(tmp_0));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.ICALL i) {
        nextPC = pc + 2;
        int tmp_0 = nextPC;
        tmp_0 = tmp_0 / 2;
        pushByte(low(tmp_0));
        pushByte(high(tmp_0));
        int tmp_1 = getRegisterWord(RZ);
        int tmp_2 = tmp_1 * 2;
        nextPC = tmp_2;
        cyclesConsumed += 3;
    }

    public void visit(LegacyInstr.IJMP i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        int tmp_1 = tmp_0 * 2;
        nextPC = tmp_1;
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.IN i) {
        nextPC = pc + 2;
        writeRegisterByte(i.r1, getIORegisterByte(i.imm1));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.INC i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        byte tmp_1 = low(tmp_0 + 1);
        N = ((tmp_1 & 128) != 0);
        Z = tmp_1 == 0;
        V = tmp_0 == 127;
        S = (N != V);
        writeRegisterByte(i.r1, tmp_1);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.JMP i) {
        nextPC = pc + 4;
        int tmp_0 = i.imm1;
        int tmp_1 = tmp_0 * 2;
        nextPC = tmp_1;
        cyclesConsumed += 3;
    }

    public void visit(LegacyInstr.LD i) {
        nextPC = pc + 2;
        writeRegisterByte(i.r1, getDataByte(getRegisterWord(i.r2)));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.LDD i) {
        nextPC = pc + 2;
        writeRegisterByte(i.r1, getDataByte(getRegisterWord(i.r2) + i.imm1));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.LDI i) {
        nextPC = pc + 2;
        writeRegisterByte(i.r1, low(i.imm1));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.LDPD i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r2) - 1;
        writeRegisterByte(i.r1, getDataByte(tmp_0));
        writeRegisterWord(i.r2, tmp_0);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.LDPI i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r2);
        writeRegisterByte(i.r1, getDataByte(tmp_0));
        writeRegisterWord(i.r2, tmp_0 + 1);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.LDS i) {
        nextPC = pc + 4;
        writeRegisterByte(i.r1, getDataByte(i.imm1));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.LPM i) {
        nextPC = pc + 2;
        writeRegisterByte(R0, getFlashByte(getRegisterWord(RZ)));
        cyclesConsumed += 3;
    }

    public void visit(LegacyInstr.LPMD i) {
        nextPC = pc + 2;
        writeRegisterByte(i.r1, getFlashByte(getRegisterWord(RZ)));
        cyclesConsumed += 3;
    }

    public void visit(LegacyInstr.LPMPI i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        writeRegisterByte(i.r1, getFlashByte(tmp_0));
        writeRegisterWord(RZ, tmp_0 + 1);
        cyclesConsumed += 3;
    }

    public void visit(LegacyInstr.LSL i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        boolean tmp_1 = false;
        int tmp_2 = tmp_0 << 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
        H = ((tmp_2 & 16) != 0);
        C = ((tmp_2 & 256) != 0);
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = (N != C);
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.LSR i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        boolean tmp_1 = false;
        int tmp_2 = (tmp_0 & 255) >> 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
        C = ((tmp_0 & 1) != 0);
        N = tmp_1;
        Z = low(tmp_2) == 0;
        V = (N != C);
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.MOV i) {
        nextPC = pc + 2;
        writeRegisterByte(i.r1, getRegisterByte(i.r2));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.MOVW i) {
        nextPC = pc + 2;
        writeRegisterWord(i.r1, getRegisterWord(i.r2));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.MUL i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1) * getRegisterUnsigned(i.r2);
        C = ((tmp_0 & 32768) != 0);
        Z = (tmp_0 & 0x0000FFFF) == 0;
        writeRegisterWord(R0, tmp_0);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.MULS i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterByte(i.r2);
        C = ((tmp_0 & 32768) != 0);
        Z = (tmp_0 & 0x0000FFFF) == 0;
        writeRegisterWord(R0, tmp_0);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.MULSU i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterUnsigned(i.r2);
        C = ((tmp_0 & 32768) != 0);
        Z = (tmp_0 & 0x0000FFFF) == 0;
        writeRegisterWord(R0, tmp_0);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.NEG i) {
        nextPC = pc + 2;
        int tmp_0 = 0;
        int tmp_1 = getRegisterByte(i.r1);
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = (N != V);
        byte tmp_10 = low(tmp_3);
        writeRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.NOP i) {
        nextPC = pc + 2;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.OR i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = tmp_0 | tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.ORI i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_0 | tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.OUT i) {
        nextPC = pc + 2;
        writeIORegisterByte(i.imm1, getRegisterByte(i.r1));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.POP i) {
        nextPC = pc + 2;
        writeRegisterByte(i.r1, popByte());
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.PUSH i) {
        nextPC = pc + 2;
        pushByte(getRegisterByte(i.r1));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.RCALL i) {
        nextPC = pc + 2;
        int tmp_0 = nextPC;
        tmp_0 = tmp_0 / 2;
        pushByte(low(tmp_0));
        pushByte(high(tmp_0));
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_1 * 2 + nextPC;
        nextPC = tmp_2;
        cyclesConsumed += 3;
    }

    public void visit(LegacyInstr.RET i) {
        nextPC = pc + 2;
        byte tmp_0 = popByte();
        byte tmp_1 = popByte();
        int tmp_2 = uword(tmp_1, tmp_0) * 2;
        nextPC = tmp_2;
        cyclesConsumed += 4;
    }

    public void visit(LegacyInstr.RETI i) {
        nextPC = pc + 2;
        byte tmp_0 = popByte();
        byte tmp_1 = popByte();
        int tmp_2 = uword(tmp_1, tmp_0) * 2;
        nextPC = tmp_2;
        enableInterrupts();
        justReturnedFromInterrupt = true;
        cyclesConsumed += 4;
    }

    public void visit(LegacyInstr.RJMP i) {
        nextPC = pc + 2;
        int tmp_0 = i.imm1;
        int tmp_1 = tmp_0 * 2 + nextPC;
        nextPC = tmp_1;
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.ROL i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        boolean tmp_1 = C;
        int tmp_2 = tmp_0 << 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
        H = ((tmp_2 & 16) != 0);
        C = ((tmp_2 & 256) != 0);
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = (N != C);
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.ROR i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        boolean tmp_1 = C;
        int tmp_2 = (tmp_0 & 255) >> 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
        C = ((tmp_0 & 1) != 0);
        N = tmp_1;
        Z = low(tmp_2) == 0;
        V = (N != C);
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SBC i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = bit(C);
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0 && Z;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = (N != V);
        byte tmp_10 = low(tmp_3);
        writeRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SBCI i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = bit(C);
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0 && Z;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = (N != V);
        byte tmp_10 = low(tmp_3);
        writeRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SBI i) {
        nextPC = pc + 2;
        getIOReg(i.imm1).writeBit(i.imm2, true);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.SBIC i) {
        nextPC = pc + 2;
        if (!getIOReg(i.imm1).readBit(i.imm2)) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if (tmp_0 == 4) {
                cyclesConsumed = cyclesConsumed + 2;
            } else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SBIS i) {
        nextPC = pc + 2;
        if (getIOReg(i.imm1).readBit(i.imm2)) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if (tmp_0 == 4) {
                cyclesConsumed = cyclesConsumed + 2;
            } else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SBIW i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1);
        int tmp_1 = tmp_0 - i.imm1;
        boolean tmp_2 = ((tmp_0 & 32768) != 0);
        boolean tmp_3 = ((tmp_1 & 32768) != 0);
        V = tmp_2 && !tmp_3;
        N = tmp_3;
        Z = (tmp_1 & 0x0000FFFF) == 0;
        C = tmp_3 && !tmp_2;
        S = (N != V);
        writeRegisterWord(i.r1, tmp_1);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.SBR i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_0 | tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = (N != V);
        byte tmp_3 = low(tmp_2);
        writeRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SBRC i) {
        nextPC = pc + 2;
        if (!Arithmetic.getBit(getRegisterByte(i.r1), i.imm1)) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if (tmp_0 == 4) {
                cyclesConsumed = cyclesConsumed + 2;
            } else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SBRS i) {
        nextPC = pc + 2;
        if (Arithmetic.getBit(getRegisterByte(i.r1), i.imm1)) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if (tmp_0 == 4) {
                cyclesConsumed = cyclesConsumed + 2;
            } else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        } else {
        }
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SEC i) {
        nextPC = pc + 2;
        C = true;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SEH i) {
        nextPC = pc + 2;
        H = true;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SEI i) {
        nextPC = pc + 2;
        enableInterrupts();
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SEN i) {
        nextPC = pc + 2;
        N = true;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SER i) {
        nextPC = pc + 2;
        writeRegisterByte(i.r1, low(255));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SES i) {
        nextPC = pc + 2;
        S = true;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SET i) {
        nextPC = pc + 2;
        T = true;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SEV i) {
        nextPC = pc + 2;
        V = true;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SEZ i) {
        nextPC = pc + 2;
        Z = true;
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SLEEP i) {
        nextPC = pc + 2;
        enterSleepMode();
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SPM i) {
        nextPC = pc + 2;
        storeProgramMemory();
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.ST i) {
        nextPC = pc + 2;
        writeDataByte(getRegisterWord(i.r1), getRegisterByte(i.r2));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.STD i) {
        nextPC = pc + 2;
        writeDataByte(getRegisterWord(i.r1) + i.imm1, getRegisterByte(i.r2));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.STPD i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1) - 1;
        writeDataByte(tmp_0, getRegisterByte(i.r2));
        writeRegisterWord(i.r1, tmp_0);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.STPI i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1);
        writeDataByte(tmp_0, getRegisterByte(i.r2));
        writeRegisterWord(i.r1, tmp_0 + 1);
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.STS i) {
        nextPC = pc + 4;
        writeDataByte(i.imm1, getRegisterByte(i.r1));
        cyclesConsumed += 2;
    }

    public void visit(LegacyInstr.SUB i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = (N != V);
        byte tmp_10 = low(tmp_3);
        writeRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SUBI i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = 0;
        int tmp_3 = tmp_0 - tmp_1 - tmp_2;
        boolean tmp_4 = ((tmp_0 & 128) != 0);
        boolean tmp_5 = ((tmp_1 & 128) != 0);
        boolean tmp_6 = ((tmp_3 & 128) != 0);
        boolean tmp_7 = ((tmp_0 & 8) != 0);
        boolean tmp_8 = ((tmp_1 & 8) != 0);
        boolean tmp_9 = ((tmp_3 & 8) != 0);
        H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
        C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
        N = tmp_6;
        Z = low(tmp_3) == 0;
        V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
        S = (N != V);
        byte tmp_10 = low(tmp_3);
        writeRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.SWAP i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        int tmp_1 = 0;
        tmp_1 = (tmp_1 & 0xFFFFFFF0) | ((((tmp_0 >> 4) & 0x0000000F) & 0x0000000F));
        tmp_1 = (tmp_1 & 0xFFFFFF0F) | (((tmp_0 & 0x0000000F) & 0x0000000F) << 4);
        writeRegisterByte(i.r1, low(tmp_1));
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.TST i) {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        V = false;
        Z = low(tmp_0) == 0;
        N = ((tmp_0 & 128) != 0);
        S = (N != V);
        cyclesConsumed += 1;
    }

    public void visit(LegacyInstr.WDR i) {
        nextPC = pc + 2;
        cyclesConsumed += 1;
    }
//--END INTERPRETER GENERATOR--

    //
    //  U T I L I T I E S
    // ------------------------------------------------------------
    //
    //  These are utility functions for expressing instructions
    //  more concisely. They are private and can be inlined by
    //  the JIT compiler or javac -O.
    //
    //

    public void pushPC(int npc) {
        npc = npc / 2;
        pushByte(Arithmetic.low(npc));
        pushByte(Arithmetic.high(npc));
    }

    public int popPC() {
        byte high = popByte();
        byte low = popByte();
        return Arithmetic.uword(low, high) * 2;
    }

    public static byte low(int val) {
        return (byte)val;
    }

    public static byte high(int val) {
        return (byte)(val >> 8);
    }

    public static byte bit(boolean val) {
        if (val) return 1;
        return 0;
    }

    public static int uword(byte low, byte high) {
        return Arithmetic.uword(low, high);
    }

    public int extended(int addr) {
        if ( RAMPZ > 0 ) return (getIORegisterByte(RAMPZ) & 1) << 16 | addr;
        else return addr;
    }

    /**
     * send the node to sleep
     */
    public void enterSleepMode() {
        sleeping = true;
        innerLoop = false;
        simulator.getMicrocontroller().sleep();
    }

    /**
     * time to wake up
     */
    public void leaveSleepMode() {
        sleeping = false;
        innerLoop = false;
        advanceClock(simulator.getMicrocontroller().wakeup());
    }
}
