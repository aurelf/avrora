/**
 * Copyright (c) 2004, Regents of the University of California
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

package avrora.sim;

import avrora.core.*;
import avrora.Avrora;
import avrora.sim.util.MulticastProbe;
import avrora.util.Arithmetic;

/**
 * The <code>GenInterpreter</code> class is largely generated from the
 * instruction specification. The framework around the generated code
 * (utilities) has been written by hand, but most of the code for
 * each instruction is generated. Therefore it is not recommended
 * to edit this code extensively.
 *
 * @author Ben L. Titzer
 */
public class GenInterpreter extends BaseInterpreter {

    public static final Register R0 = Register.R0;
    public static final Register R1 = Register.R1;
    public static final Register R2 = Register.R2;
    public static final Register R3 = Register.R3;
    public static final Register R4 = Register.R4;
    public static final Register R5 = Register.R5;
    public static final Register R6 = Register.R6;
    public static final Register R7 = Register.R7;
    public static final Register R8 = Register.R8;
    public static final Register R9 = Register.R9;
    public static final Register R10 = Register.R10;
    public static final Register R11 = Register.R11;
    public static final Register R12 = Register.R12;
    public static final Register R13 = Register.R13;
    public static final Register R14 = Register.R14;
    public static final Register R15 = Register.R15;
    public static final Register R16 = Register.R16;
    public static final Register R17 = Register.R17;
    public static final Register R18 = Register.R18;
    public static final Register R19 = Register.R19;
    public static final Register R20 = Register.R20;
    public static final Register R21 = Register.R21;
    public static final Register R22 = Register.R22;
    public static final Register R23 = Register.R23;
    public static final Register R24 = Register.R24;
    public static final Register R25 = Register.R25;
    public static final Register R26 = Register.R26;
    public static final Register R27 = Register.R27;
    public static final Register R28 = Register.R28;
    public static final Register R29 = Register.R29;
    public static final Register R30 = Register.R30;
    public static final Register R31 = Register.R31;

    public static final Register RX = Register.X;
    public static final Register RY = Register.Y;
    public static final Register RZ = Register.Z;

    /**
     * The constructor for the <code>Interpreter</code> class builds the internal data
     * structures needed to store the complete state of the machine, including registers,
     * IO registers, the SRAM, and the flash. All IO registers are initialized to be
     * instances of <code>RWIOReg</code>. Reserved and special IO registers must be
     * inserted by the <code>getIOReg()</code> and <code>setIOReg()</code>
     * methods.
     *
     * @param p          the program to construct the state for
     * @param flash_size the size of the flash (program) memory in bytes
     * @param ioreg_size the number of IO registers
     * @param sram_size  the size of the SRAM in bytes
     */
    protected GenInterpreter(Simulator s, Program p, int flash_size, int ioreg_size, int sram_size) {
        super(s, p, flash_size, ioreg_size, sram_size);
    }

    protected void runLoop() {

        nextPC = pc;
        cyclesConsumed = 0;

        while (shouldRun) {

            if (justReturnedFromInterrupt) {
                // don't process the interrupt if we just returned from
                // an interrupt handler, because the hardware manual says
                // that at least one instruction is executed after
                // returning from an interrupt.
                justReturnedFromInterrupt = false;
            } else if ( I ) {

                // check if there are any pending (posted) interrupts
                if (postedInterrupts != 0) {
                    // the lowest set bit is the highest priority posted interrupt
                    int lowestbit = Arithmetic.lowestBit(postedInterrupts);

                    // fire the interrupt (update flag register(s) state)
                    simulator.triggerInterrupt(lowestbit);

                    // store the return address
                    pushPC(nextPC);

                    // set PC to interrupt handler
                    nextPC = simulator.getInterruptVectorAddress(lowestbit);
                    pc = nextPC;

                    // disable interrupts
                    I = false;

                    // process any timed events
                    advanceCycles(4);
                    sleeping = false;

                }
            }

            if ( sleeping ) sleepLoop();
            else {
                if ( activeProbe.isEmpty() ) fastLoop();
                else instrumentedLoop();
            }
        }
    }

    private void sleepLoop() {
        innerLoop = true;
        while ( innerLoop ) {
            long delta = eventQueue.getHeadDelta();
            if ( delta <= 0 ) delta = 1;
            advanceCycles(delta);
        }
    }

    private void fastLoop() {
        innerLoop = true;
        while ( innerLoop ) {
            Instr i = impression.readInstr(nextPC);

            // visit the actual instruction (or probe)
            i.accept(this);
            pc = nextPC;
            advanceCycles(cyclesConsumed);
        }
    }

    private void instrumentedLoop() {
        innerLoop = true;
        while ( innerLoop ) {
            // get the current instruction
            int curPC = nextPC; // at this point pc == nextPC
            Instr i = impression.readInstr(nextPC);

            // visit the actual instruction (or probe)
            activeProbe.fireBefore(i, curPC, this);
            i.accept(this);
            pc = nextPC;
            advanceCycles(cyclesConsumed);
            activeProbe.fireAfter(i, curPC, this);
        }
    }

    protected void insertProbe(Simulator.Probe p, int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null)
            pi.add(p);
        else {
            pi = new ProbedInstr(impression.readInstr(addr), addr, p);
            impression.writeInstr(pi, addr);
        }
    }

    protected void removeProbe(Simulator.Probe p, int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null) {
            pi.remove(p);
            if (pi.isEmpty())
                impression.writeInstr(pi.instr, pi.address);
        }
    }

    protected void insertBreakPoint(int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null)
            pi.setBreakPoint();
        else {
            pi = new ProbedInstr(impression.readInstr(addr), addr, null);
            impression.writeInstr(pi, addr);
            pi.setBreakPoint();
        }
    }

    protected void removeBreakPoint(int addr) {
        ProbedInstr pi = getProbedInstr(addr);
        if (pi != null) pi.unsetBreakPoint();
    }

    private ProbedInstr getProbedInstr(int addr) {
        Instr i = impression.readInstr(addr);
        if (i instanceof ProbedInstr)
            return ((ProbedInstr) i);
        else
            return null;
    }


//--BEGIN INTERPRETER GENERATOR--
    public void visit(Instr.ADC i)  {
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
        S = xor(N, V);
        byte tmp_9 = low(tmp_3);
        setRegisterByte(i.r1, tmp_9);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ADD i)  {
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
        S = xor(N, V);
        byte tmp_9 = low(tmp_3);
        setRegisterByte(i.r1, tmp_9);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ADIW i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1);
        int tmp_1 = tmp_0 + i.imm1;
        boolean tmp_2 = ((tmp_1 & 32768) != 0);
        boolean tmp_3 = ((tmp_0 & 32768) != 0);
        C = !tmp_2 && tmp_3;
        N = tmp_2;
        V = !tmp_3 && tmp_2;
        Z = (tmp_1 & 0x0000FFFF) == 0;
        S = xor(N, V);
        setRegisterWord(i.r1, tmp_1);
        cyclesConsumed += 2;
    }
    public void visit(Instr.AND i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = tmp_0 & tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ANDI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_0 & tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ASR i)  {
        nextPC = pc + 2;
        byte tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = tmp_0;
        boolean tmp_2 = ((tmp_0 & 128) != 0);
        int tmp_3 = (tmp_1 & 255) >> 1;
        tmp_3 = Arithmetic.setBit(tmp_3, 7, tmp_2);
        C = ((tmp_1 & 1) != 0);
        N = tmp_2;
        Z = low(tmp_3) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_4 = low(tmp_3);
        setRegisterByte(i.r1, tmp_4);
        cyclesConsumed += 1;
    }
    public void visit(Instr.BCLR i)  {
        nextPC = pc + 2;
        getIOReg(SREG).writeBit(i.imm1, false);
        cyclesConsumed += 1;
    }
    public void visit(Instr.BLD i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, Arithmetic.setBit(getRegisterByte(i.r1), i.imm1, T));
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRBC i)  {
        nextPC = pc + 2;
        if ( !getIOReg(SREG).readBit(i.imm1) ) {
            int tmp_0 = i.imm2;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRBS i)  {
        nextPC = pc + 2;
        if ( getIOReg(SREG).readBit(i.imm1) ) {
            int tmp_0 = i.imm2;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRCC i)  {
        nextPC = pc + 2;
        if ( !C ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRCS i)  {
        nextPC = pc + 2;
        if ( C ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BREAK i)  {
        nextPC = pc + 2;
        stop();
        cyclesConsumed += 1;
    }
    public void visit(Instr.BREQ i)  {
        nextPC = pc + 2;
        if ( Z ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRGE i)  {
        nextPC = pc + 2;
        if ( !S ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRHC i)  {
        nextPC = pc + 2;
        if ( !H ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRHS i)  {
        nextPC = pc + 2;
        if ( H ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRID i)  {
        nextPC = pc + 2;
        if ( !I ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRIE i)  {
        nextPC = pc + 2;
        if ( I ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRLO i)  {
        nextPC = pc + 2;
        if ( C ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRLT i)  {
        nextPC = pc + 2;
        if ( S ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRMI i)  {
        nextPC = pc + 2;
        if ( N ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRNE i)  {
        nextPC = pc + 2;
        if ( !Z ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRPL i)  {
        nextPC = pc + 2;
        if ( !N ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRSH i)  {
        nextPC = pc + 2;
        if ( !C ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRTC i)  {
        nextPC = pc + 2;
        if ( !T ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRTS i)  {
        nextPC = pc + 2;
        if ( T ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRVC i)  {
        nextPC = pc + 2;
        if ( !V ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BRVS i)  {
        nextPC = pc + 2;
        if ( V ) {
            int tmp_0 = i.imm1;
            int tmp_1 = tmp_0;
            int tmp_2 = tmp_1 * 2 + nextPC;
            nextPC = tmp_2;
            cyclesConsumed = cyclesConsumed + 1;
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.BSET i)  {
        nextPC = pc + 2;
        getIOReg(SREG).writeBit(i.imm1, true);
        cyclesConsumed += 1;
    }
    public void visit(Instr.BST i)  {
        nextPC = pc + 2;
        T = Arithmetic.getBit(getRegisterByte(i.r1), i.imm1);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CALL i)  {
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
    public void visit(Instr.CBI i)  {
        nextPC = pc + 2;
        getIOReg(i.imm1).writeBit(i.imm2, false);
        cyclesConsumed += 2;
    }
    public void visit(Instr.CBR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = ~i.imm1;
        int tmp_2 = tmp_0 & tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLC i)  {
        nextPC = pc + 2;
        C = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLH i)  {
        nextPC = pc + 2;
        H = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLI i)  {
        nextPC = pc + 2;
        disableInterrupts();
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLN i)  {
        nextPC = pc + 2;
        N = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLR i)  {
        nextPC = pc + 2;
        S = false;
        V = false;
        N = false;
        Z = true;
        setRegisterByte(i.r1, low(0));
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLS i)  {
        nextPC = pc + 2;
        S = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLT i)  {
        nextPC = pc + 2;
        T = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLV i)  {
        nextPC = pc + 2;
        V = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.CLZ i)  {
        nextPC = pc + 2;
        Z = false;
        cyclesConsumed += 1;
    }
    public void visit(Instr.COM i)  {
        nextPC = pc + 2;
        int tmp_0 = 255 - getRegisterByte(i.r1);
        C = true;
        N = ((tmp_0 & 128) != 0);
        Z = low(tmp_0) == 0;
        V = false;
        S = xor(N, V);
        setRegisterByte(i.r1, low(tmp_0));
        cyclesConsumed += 1;
    }
    public void visit(Instr.CP i)  {
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
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CPC i)  {
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
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CPI i)  {
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
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.CPSE i)  {
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
        S = xor(N, V);
        byte tmp_12 = low(tmp_5);
        if ( tmp_0 == tmp_1 ) {
            int tmp_13 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_13;
            if ( tmp_13 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.DEC i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        byte tmp_1 = low(tmp_0 - 1);
        N = ((tmp_1 & 128) != 0);
        Z = tmp_1 == 0;
        V = tmp_0 == 128;
        S = xor(N, V);
        setRegisterByte(i.r1, tmp_1);
        cyclesConsumed += 1;
    }
    public void visit(Instr.EICALL i)  {
        nextPC = pc + 2;
        cyclesConsumed += 4;
    }
    public void visit(Instr.EIJMP i)  {
        nextPC = pc + 2;
        cyclesConsumed += 2;
    }
    public void visit(Instr.ELPM i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(RAMPZ) & 0x000000FF) << 16);
        setRegisterByte(R0, getProgramByte(tmp_0));
        cyclesConsumed += 3;
    }
    public void visit(Instr.ELPMD i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(RAMPZ) & 0x000000FF) << 16);
        setRegisterByte(i.r1, getProgramByte(tmp_0));
        cyclesConsumed += 3;
    }
    public void visit(Instr.ELPMPI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(RAMPZ) & 0x000000FF) << 16);
        setRegisterByte(i.r1, getProgramByte(tmp_0));
        setRegisterWord(RZ, tmp_0 + 1);
        cyclesConsumed += 3;
    }
    public void visit(Instr.EOR i)  {
        nextPC = pc + 2;
        byte tmp_0 = low(getRegisterByte(i.r1) ^ getRegisterByte(i.r2));
        N = ((tmp_0 & 128) != 0);
        Z = tmp_0 == 0;
        V = false;
        S = xor(N, V);
        setRegisterByte(i.r1, tmp_0);
        cyclesConsumed += 1;
    }
    public void visit(Instr.FMUL i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1) * getRegisterUnsigned(i.r2) << 1;
        Z = (tmp_0 & 0x0000FFFF) == 0;
        C = ((tmp_0 & 65536) != 0);
        setRegisterByte(R0, low(tmp_0));
        setRegisterByte(R1, high(tmp_0));
        cyclesConsumed += 2;
    }
    public void visit(Instr.FMULS i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterByte(i.r2) << 1;
        Z = (tmp_0 & 0x0000FFFF) == 0;
        C = ((tmp_0 & 65536) != 0);
        setRegisterByte(R0, low(tmp_0));
        setRegisterByte(R1, high(tmp_0));
        cyclesConsumed += 2;
    }
    public void visit(Instr.FMULSU i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterUnsigned(i.r2) << 1;
        Z = (tmp_0 & 0x0000FFFF) == 0;
        C = ((tmp_0 & 65536) != 0);
        setRegisterByte(R0, low(tmp_0));
        setRegisterByte(R1, high(tmp_0));
        cyclesConsumed += 2;
    }
    public void visit(Instr.ICALL i)  {
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
    public void visit(Instr.IJMP i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        int tmp_1 = tmp_0 * 2;
        nextPC = tmp_1;
        cyclesConsumed += 2;
    }
    public void visit(Instr.IN i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getIORegisterByte(i.imm1));
        cyclesConsumed += 1;
    }
    public void visit(Instr.INC i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        byte tmp_1 = low(tmp_0 + 1);
        N = ((tmp_1 & 128) != 0);
        Z = tmp_1 == 0;
        V = tmp_0 == 127;
        S = xor(N, V);
        setRegisterByte(i.r1, tmp_1);
        cyclesConsumed += 1;
    }
    public void visit(Instr.JMP i)  {
        nextPC = pc + 4;
        int tmp_0 = i.imm1;
        int tmp_1 = tmp_0 * 2;
        nextPC = tmp_1;
        cyclesConsumed += 3;
    }
    public void visit(Instr.LD i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getDataByte(getRegisterWord(i.r2)));
        cyclesConsumed += 2;
    }
    public void visit(Instr.LDD i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getDataByte(getRegisterWord(i.r2) + i.imm1));
        cyclesConsumed += 2;
    }
    public void visit(Instr.LDI i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, low(i.imm1));
        cyclesConsumed += 1;
    }
    public void visit(Instr.LDPD i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r2) - 1;
        setRegisterByte(i.r1, getDataByte(tmp_0));
        setRegisterWord(i.r2, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.LDPI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r2);
        setRegisterByte(i.r1, getDataByte(tmp_0));
        setRegisterWord(i.r2, tmp_0 + 1);
        cyclesConsumed += 2;
    }
    public void visit(Instr.LDS i)  {
        nextPC = pc + 4;
        setRegisterByte(i.r1, getDataByte(i.imm1));
        cyclesConsumed += 2;
    }
    public void visit(Instr.LPM i)  {
        nextPC = pc + 2;
        setRegisterByte(R0, getProgramByte(getRegisterWord(RZ)));
        cyclesConsumed += 3;
    }
    public void visit(Instr.LPMD i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getProgramByte(getRegisterWord(RZ)));
        cyclesConsumed += 3;
    }
    public void visit(Instr.LPMPI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(RZ);
        setRegisterByte(i.r1, getProgramByte(tmp_0));
        setRegisterWord(RZ, tmp_0 + 1);
        cyclesConsumed += 3;
    }
    public void visit(Instr.LSL i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        boolean tmp_1 = false;
        int tmp_2 = tmp_0 << 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
        H = ((tmp_2 & 16) != 0);
        C = ((tmp_2 & 256) != 0);
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.LSR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        boolean tmp_1 = false;
        int tmp_2 = (tmp_0 & 255) >> 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
        C = ((tmp_0 & 1) != 0);
        N = tmp_1;
        Z = low(tmp_2) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.MOV i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, getRegisterByte(i.r2));
        cyclesConsumed += 1;
    }
    public void visit(Instr.MOVW i)  {
        nextPC = pc + 2;
        setRegisterWord(i.r1, getRegisterWord(i.r2));
        cyclesConsumed += 1;
    }
    public void visit(Instr.MUL i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1) * getRegisterUnsigned(i.r2);
        C = ((tmp_0 & 32768) != 0);
        Z = (tmp_0 & 0x0000FFFF) == 0;
        setRegisterWord(R0, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.MULS i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterByte(i.r2);
        C = ((tmp_0 & 32768) != 0);
        Z = (tmp_0 & 0x0000FFFF) == 0;
        setRegisterWord(R0, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.MULSU i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1) * getRegisterUnsigned(i.r2);
        C = ((tmp_0 & 32768) != 0);
        Z = (tmp_0 & 0x0000FFFF) == 0;
        setRegisterWord(R0, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.NEG i)  {
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
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.NOP i)  {
        nextPC = pc + 2;
        cyclesConsumed += 1;
    }
    public void visit(Instr.OR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = getRegisterByte(i.r2);
        int tmp_2 = tmp_0 | tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ORI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_0 | tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.OUT i)  {
        nextPC = pc + 2;
        setIORegisterByte(i.imm1, getRegisterByte(i.r1));
        cyclesConsumed += 1;
    }
    public void visit(Instr.POP i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, popByte());
        cyclesConsumed += 2;
    }
    public void visit(Instr.PUSH i)  {
        nextPC = pc + 2;
        pushByte(getRegisterByte(i.r1));
        cyclesConsumed += 2;
    }
    public void visit(Instr.RCALL i)  {
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
    public void visit(Instr.RET i)  {
        nextPC = pc + 2;
        byte tmp_0 = popByte();
        byte tmp_1 = popByte();
        int tmp_2 = uword(tmp_1, tmp_0) * 2;
        nextPC = tmp_2;
        cyclesConsumed += 4;
    }
    public void visit(Instr.RETI i)  {
        nextPC = pc + 2;
        byte tmp_0 = popByte();
        byte tmp_1 = popByte();
        int tmp_2 = uword(tmp_1, tmp_0) * 2;
        nextPC = tmp_2;
        enableInterrupts();
        justReturnedFromInterrupt = true;
        cyclesConsumed += 4;
    }
    public void visit(Instr.RJMP i)  {
        nextPC = pc + 2;
        int tmp_0 = i.imm1;
        int tmp_1 = tmp_0 * 2 + nextPC;
        nextPC = tmp_1;
        cyclesConsumed += 2;
    }
    public void visit(Instr.ROL i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        boolean tmp_1 = C;
        int tmp_2 = tmp_0 << 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
        H = ((tmp_2 & 16) != 0);
        C = ((tmp_2 & 256) != 0);
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.ROR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        boolean tmp_1 = C;
        int tmp_2 = (tmp_0 & 255) >> 1;
        tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
        C = ((tmp_0 & 1) != 0);
        N = tmp_1;
        Z = low(tmp_2) == 0;
        V = xor(N, C);
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBC i)  {
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
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBCI i)  {
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
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBI i)  {
        nextPC = pc + 2;
        getIOReg(i.imm1).writeBit(i.imm2, true);
        cyclesConsumed += 2;
    }
    public void visit(Instr.SBIC i)  {
        nextPC = pc + 2;
        if ( !getIOReg(i.imm1).readBit(i.imm2) ) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if ( tmp_0 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBIS i)  {
        nextPC = pc + 2;
        if ( getIOReg(i.imm1).readBit(i.imm2) ) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if ( tmp_0 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBIW i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1);
        int tmp_1 = tmp_0 - i.imm1;
        boolean tmp_2 = ((tmp_0 & 32768) != 0);
        boolean tmp_3 = ((tmp_1 & 32768) != 0);
        V = tmp_2 && !tmp_3;
        N = tmp_3;
        Z = (tmp_1 & 0x0000FFFF) == 0;
        C = tmp_3 && !tmp_2;
        S = xor(N, V);
        setRegisterWord(i.r1, tmp_1);
        cyclesConsumed += 2;
    }
    public void visit(Instr.SBR i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        int tmp_1 = i.imm1;
        int tmp_2 = tmp_0 | tmp_1;
        N = ((tmp_2 & 128) != 0);
        Z = low(tmp_2) == 0;
        V = false;
        S = xor(N, V);
        byte tmp_3 = low(tmp_2);
        setRegisterByte(i.r1, tmp_3);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBRC i)  {
        nextPC = pc + 2;
        if ( !Arithmetic.getBit(getRegisterByte(i.r1), i.imm1) ) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if ( tmp_0 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.SBRS i)  {
        nextPC = pc + 2;
        if ( Arithmetic.getBit(getRegisterByte(i.r1), i.imm1) ) {
            int tmp_0 = getInstrSize(nextPC);
            nextPC = nextPC + tmp_0;
            if ( tmp_0 == 4 ) {
                cyclesConsumed = cyclesConsumed + 2;
            }
            else {
                cyclesConsumed = cyclesConsumed + 1;
            }
        }
        else {
        }
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEC i)  {
        nextPC = pc + 2;
        C = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEH i)  {
        nextPC = pc + 2;
        H = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEI i)  {
        nextPC = pc + 2;
        enableInterrupts();
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEN i)  {
        nextPC = pc + 2;
        N = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SER i)  {
        nextPC = pc + 2;
        setRegisterByte(i.r1, low(255));
        cyclesConsumed += 1;
    }
    public void visit(Instr.SES i)  {
        nextPC = pc + 2;
        S = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SET i)  {
        nextPC = pc + 2;
        T = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEV i)  {
        nextPC = pc + 2;
        V = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SEZ i)  {
        nextPC = pc + 2;
        Z = true;
        cyclesConsumed += 1;
    }
    public void visit(Instr.SLEEP i)  {
        nextPC = pc + 2;
        enterSleepMode();
        cyclesConsumed += 1;
    }
    public void visit(Instr.SPM i)  {
        nextPC = pc + 2;
        cyclesConsumed += 1;
    }
    public void visit(Instr.ST i)  {
        nextPC = pc + 2;
        setDataByte(getRegisterWord(i.r1), getRegisterByte(i.r2));
        cyclesConsumed += 2;
    }
    public void visit(Instr.STD i)  {
        nextPC = pc + 2;
        setDataByte(getRegisterWord(i.r1) + i.imm1, getRegisterByte(i.r2));
        cyclesConsumed += 2;
    }
    public void visit(Instr.STPD i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1) - 1;
        setDataByte(tmp_0, getRegisterByte(i.r2));
        setRegisterWord(i.r1, tmp_0);
        cyclesConsumed += 2;
    }
    public void visit(Instr.STPI i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterWord(i.r1);
        setDataByte(tmp_0, getRegisterByte(i.r2));
        setRegisterWord(i.r1, tmp_0 + 1);
        cyclesConsumed += 2;
    }
    public void visit(Instr.STS i)  {
        nextPC = pc + 4;
        setDataByte(i.imm1, getRegisterByte(i.r1));
        cyclesConsumed += 2;
    }
    public void visit(Instr.SUB i)  {
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
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SUBI i)  {
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
        S = xor(N, V);
        byte tmp_10 = low(tmp_3);
        setRegisterByte(i.r1, tmp_10);
        cyclesConsumed += 1;
    }
    public void visit(Instr.SWAP i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterUnsigned(i.r1);
        int tmp_1 = 0;
        tmp_1 = (tmp_1 & 0xFFFFFFF0) | ((((tmp_0 >> 4) & 0x0000000F) & 0x0000000F));
        tmp_1 = (tmp_1 & 0xFFFFFF0F) | (((tmp_0 & 0x0000000F) & 0x0000000F) << 4);
        setRegisterByte(i.r1, low(tmp_1));
        cyclesConsumed += 1;
    }
    public void visit(Instr.TST i)  {
        nextPC = pc + 2;
        int tmp_0 = getRegisterByte(i.r1);
        V = false;
        Z = low(tmp_0) == 0;
        N = ((tmp_0 & 128) != 0);
        S = xor(N, V);
        cyclesConsumed += 1;
    }
    public void visit(Instr.WDR i)  {
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

    private void pushPC(int npc) {
        npc = npc / 2;
        pushByte(Arithmetic.low(npc));
        pushByte(Arithmetic.high(npc));
    }

    private int popPC() {
        byte high = popByte();
        byte low = popByte();
        return Arithmetic.uword(low, high) * 2;
    }

    private boolean xor(boolean a, boolean b) {
        return (a && !b) || (b && !a);
    }

    private byte low(int val) {
        return (byte)val;
    }

    private byte high(int val) {
        return (byte)(val >> 8);
    }

    private byte bit(boolean val) {
        if ( val ) return 1;
        return 0;
    }

    private int uword(byte low, byte high) {
        return Arithmetic.uword(low, high);
    }

    private void stop() {
        shouldRun = false;
        innerLoop = false;
    }

    private void enterSleepMode() {
        sleeping = true;
        innerLoop = false;
    }

}
