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

import avrora.core.Instr;
import avrora.core.Program;
import avrora.core.InstrVisitor;
import avrora.Avrora;
import avrora.util.Arithmetic;

/**
 * @author Ben L. Titzer
 */
public class FIFInterpreter extends BaseInterpreter {

    protected abstract static class FIFInstr {

        public final int pc;
        public final Instr instr;
        public int r1;
        public int r2;
        public int imm1;
        public int imm2;

        private boolean breakPoint;
        private boolean breakFired;

        public Simulator.Probe probe;

        public FIFInstr next;
        public FIFInstr other;

        public FIFInstr(Instr i, int pc_) {
            instr = i;
            pc = pc_;
        }

        public abstract void execute(FIFInterpreter interp);
    }

    protected FIFInstr fifMap[];

    protected FIFInstr curInstr;
    protected FIFInstr nextInstr;

    public FIFInterpreter(Simulator s, Program p, int fs, int is, int ss) {
        super(s, p, fs, is, ss);
        fifMap = new FIFInstr[p.program_end];
        buildFIFMap();
        curInstr = fifMap[0];
    }

    public void insertProbe(Simulator.Probe p) {
        throw Avrora.unimplemented();
    }

    public void insertProbe(Simulator.Probe p, int prog_addr) {
        throw Avrora.unimplemented();
    }

    public void removeProbe(Simulator.Probe p) {
        throw Avrora.unimplemented();
    }

    public void removeProbe(Simulator.Probe p, int prog_addr) {
        throw Avrora.unimplemented();
    }

    public void insertBreakPoint(int addr) {
        throw Avrora.unimplemented();
    }

    public void removeBreakPoint(int addr) {
        throw Avrora.unimplemented();
    }

    protected void runLoop() {

        nextInstr = curInstr;
        cyclesConsumed = 0;

        while (shouldRun) {

            if (justReturnedFromInterrupt) {
                // don't process the interrupt if we just returned from
                // an interrupt handler, because the hardware manual says
                // that at least one instruction is executed after
                // returning from an interrupt.
                justReturnedFromInterrupt = false;
            } else if (I) {

                // check if there are any pending (posted) interrupts
                if (postedInterrupts != 0) {
                    // the lowest set bit is the highest priority posted interrupt
                    int lowestbit = Arithmetic.lowestBit(postedInterrupts);

                    // fire the interrupt (update flag register(s) state)
                    simulator.triggerInterrupt(lowestbit);

                    // store the return address
                    pushPC(nextInstr.pc);

                    // set PC to interrupt handler
                    nextInstr = fifMap[simulator.getInterruptVectorAddress(lowestbit)];
                    curInstr = nextInstr;

                    // disable interrupts
                    I = false;

                    // process any timed events
                    advanceCycles(4);
                    sleeping = false;

                }
            }

            if (sleeping)
                sleepLoop();
            else {
                if (activeProbe.isEmpty())
                    fastLoop();
                else
                    instrumentedLoop();
            }
        }
    }

    private void sleepLoop() {
        innerLoop = true;
        while (innerLoop) {
            long delta = eventQueue.getHeadDelta();
            if (delta <= 0) delta = 1;
            advanceCycles(delta);
        }
    }

    private void fastLoop() {
        innerLoop = true;
        while (innerLoop) {
            // visit the actual instruction (or probe)
            step();
        }
    }

    private void instrumentedLoop() {
        innerLoop = true;
        while (innerLoop) {
            // get the current instruction
            int curPC = nextInstr.pc; // at this point pc == nextPC
            Instr i = nextInstr.instr;

            // visit the actual instruction (or probe)
            activeProbe.fireBefore(i, curPC, this);
            step();
            activeProbe.fireAfter(i, curPC, this);
        }
    }

    private void step() {
        nextInstr = nextInstr.next;
        curInstr.execute(this);
        curInstr = nextInstr;
        advanceCycles(cyclesConsumed);
    }

    public int getPC() {
        return curInstr.pc;
    }

    private void buildFIFMap() {
        FIFInstr last = null;
        FIFBuilder builder = new FIFBuilder();

        for (int cntr = 0; cntr < simulator.program.program_end; cntr += 2) {
            Instr i = simulator.program.readInstr(cntr);
            if (i == null) continue;
            FIFInstr cur = builder.build(cntr, i);
            if (last != null) {
                if (last.pc + last.instr.getSize() == cntr)
                    last.next = cur;
            }
            last = cur;
            fifMap[cntr] = cur;
        }
    }

//--BEGIN FIF GENERATOR--
    protected class FIFBuilder implements InstrVisitor {
        private FIFInstr instr;
        private int pc;

        protected FIFInstr build(int pc, Instr i) {
            this.pc = pc;
            i.accept(this);
            return instr;
        }

        public void visit(Instr.ADC i) {
            instr = new FIFInstr_ADC(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.ADD i) {
            instr = new FIFInstr_ADD(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.ADIW i) {
            instr = new FIFInstr_ADIW(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.AND i) {
            instr = new FIFInstr_AND(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.ANDI i) {
            instr = new FIFInstr_ANDI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.ASR i) {
            instr = new FIFInstr_ASR(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.BCLR i) {
            instr = new FIFInstr_BCLR(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BLD i) {
            instr = new FIFInstr_BLD(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRBC i) {
            instr = new FIFInstr_BRBC(i, pc);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }

        public void visit(Instr.BRBS i) {
            instr = new FIFInstr_BRBS(i, pc);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }

        public void visit(Instr.BRCC i) {
            instr = new FIFInstr_BRCC(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRCS i) {
            instr = new FIFInstr_BRCS(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BREAK i) {
            instr = new FIFInstr_BREAK(i, pc);
        }

        public void visit(Instr.BREQ i) {
            instr = new FIFInstr_BREQ(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRGE i) {
            instr = new FIFInstr_BRGE(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRHC i) {
            instr = new FIFInstr_BRHC(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRHS i) {
            instr = new FIFInstr_BRHS(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRID i) {
            instr = new FIFInstr_BRID(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRIE i) {
            instr = new FIFInstr_BRIE(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRLO i) {
            instr = new FIFInstr_BRLO(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRLT i) {
            instr = new FIFInstr_BRLT(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRMI i) {
            instr = new FIFInstr_BRMI(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRNE i) {
            instr = new FIFInstr_BRNE(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRPL i) {
            instr = new FIFInstr_BRPL(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRSH i) {
            instr = new FIFInstr_BRSH(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRTC i) {
            instr = new FIFInstr_BRTC(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRTS i) {
            instr = new FIFInstr_BRTS(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRVC i) {
            instr = new FIFInstr_BRVC(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BRVS i) {
            instr = new FIFInstr_BRVS(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BSET i) {
            instr = new FIFInstr_BSET(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.BST i) {
            instr = new FIFInstr_BST(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.CALL i) {
            instr = new FIFInstr_CALL(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.CBI i) {
            instr = new FIFInstr_CBI(i, pc);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }

        public void visit(Instr.CBR i) {
            instr = new FIFInstr_CBR(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.CLC i) {
            instr = new FIFInstr_CLC(i, pc);
        }

        public void visit(Instr.CLH i) {
            instr = new FIFInstr_CLH(i, pc);
        }

        public void visit(Instr.CLI i) {
            instr = new FIFInstr_CLI(i, pc);
        }

        public void visit(Instr.CLN i) {
            instr = new FIFInstr_CLN(i, pc);
        }

        public void visit(Instr.CLR i) {
            instr = new FIFInstr_CLR(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.CLS i) {
            instr = new FIFInstr_CLS(i, pc);
        }

        public void visit(Instr.CLT i) {
            instr = new FIFInstr_CLT(i, pc);
        }

        public void visit(Instr.CLV i) {
            instr = new FIFInstr_CLV(i, pc);
        }

        public void visit(Instr.CLZ i) {
            instr = new FIFInstr_CLZ(i, pc);
        }

        public void visit(Instr.COM i) {
            instr = new FIFInstr_COM(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.CP i) {
            instr = new FIFInstr_CP(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.CPC i) {
            instr = new FIFInstr_CPC(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.CPI i) {
            instr = new FIFInstr_CPI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.CPSE i) {
            instr = new FIFInstr_CPSE(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.DEC i) {
            instr = new FIFInstr_DEC(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.EICALL i) {
            instr = new FIFInstr_EICALL(i, pc);
        }

        public void visit(Instr.EIJMP i) {
            instr = new FIFInstr_EIJMP(i, pc);
        }

        public void visit(Instr.ELPM i) {
            instr = new FIFInstr_ELPM(i, pc);
        }

        public void visit(Instr.ELPMD i) {
            instr = new FIFInstr_ELPMD(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.ELPMPI i) {
            instr = new FIFInstr_ELPMPI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.EOR i) {
            instr = new FIFInstr_EOR(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.FMUL i) {
            instr = new FIFInstr_FMUL(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.FMULS i) {
            instr = new FIFInstr_FMULS(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.FMULSU i) {
            instr = new FIFInstr_FMULSU(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.ICALL i) {
            instr = new FIFInstr_ICALL(i, pc);
        }

        public void visit(Instr.IJMP i) {
            instr = new FIFInstr_IJMP(i, pc);
        }

        public void visit(Instr.IN i) {
            instr = new FIFInstr_IN(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.INC i) {
            instr = new FIFInstr_INC(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.JMP i) {
            instr = new FIFInstr_JMP(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.LD i) {
            instr = new FIFInstr_LD(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.LDD i) {
            instr = new FIFInstr_LDD(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.LDI i) {
            instr = new FIFInstr_LDI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.LDPD i) {
            instr = new FIFInstr_LDPD(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.LDPI i) {
            instr = new FIFInstr_LDPI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.LDS i) {
            instr = new FIFInstr_LDS(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.LPM i) {
            instr = new FIFInstr_LPM(i, pc);
        }

        public void visit(Instr.LPMD i) {
            instr = new FIFInstr_LPMD(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.LPMPI i) {
            instr = new FIFInstr_LPMPI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.LSL i) {
            instr = new FIFInstr_LSL(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.LSR i) {
            instr = new FIFInstr_LSR(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.MOV i) {
            instr = new FIFInstr_MOV(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.MOVW i) {
            instr = new FIFInstr_MOVW(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.MUL i) {
            instr = new FIFInstr_MUL(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.MULS i) {
            instr = new FIFInstr_MULS(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.MULSU i) {
            instr = new FIFInstr_MULSU(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.NEG i) {
            instr = new FIFInstr_NEG(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.NOP i) {
            instr = new FIFInstr_NOP(i, pc);
        }

        public void visit(Instr.OR i) {
            instr = new FIFInstr_OR(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.ORI i) {
            instr = new FIFInstr_ORI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.OUT i) {
            instr = new FIFInstr_OUT(i, pc);
            instr.imm1 = i.imm1;
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.POP i) {
            instr = new FIFInstr_POP(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.PUSH i) {
            instr = new FIFInstr_PUSH(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.RCALL i) {
            instr = new FIFInstr_RCALL(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.RET i) {
            instr = new FIFInstr_RET(i, pc);
        }

        public void visit(Instr.RETI i) {
            instr = new FIFInstr_RETI(i, pc);
        }

        public void visit(Instr.RJMP i) {
            instr = new FIFInstr_RJMP(i, pc);
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.ROL i) {
            instr = new FIFInstr_ROL(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.ROR i) {
            instr = new FIFInstr_ROR(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.SBC i) {
            instr = new FIFInstr_SBC(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.SBCI i) {
            instr = new FIFInstr_SBCI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.SBI i) {
            instr = new FIFInstr_SBI(i, pc);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }

        public void visit(Instr.SBIC i) {
            instr = new FIFInstr_SBIC(i, pc);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }

        public void visit(Instr.SBIS i) {
            instr = new FIFInstr_SBIS(i, pc);
            instr.imm1 = i.imm1;
            instr.imm2 = i.imm2;
        }

        public void visit(Instr.SBIW i) {
            instr = new FIFInstr_SBIW(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.SBR i) {
            instr = new FIFInstr_SBR(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.SBRC i) {
            instr = new FIFInstr_SBRC(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.SBRS i) {
            instr = new FIFInstr_SBRS(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.SEC i) {
            instr = new FIFInstr_SEC(i, pc);
        }

        public void visit(Instr.SEH i) {
            instr = new FIFInstr_SEH(i, pc);
        }

        public void visit(Instr.SEI i) {
            instr = new FIFInstr_SEI(i, pc);
        }

        public void visit(Instr.SEN i) {
            instr = new FIFInstr_SEN(i, pc);
        }

        public void visit(Instr.SER i) {
            instr = new FIFInstr_SER(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.SES i) {
            instr = new FIFInstr_SES(i, pc);
        }

        public void visit(Instr.SET i) {
            instr = new FIFInstr_SET(i, pc);
        }

        public void visit(Instr.SEV i) {
            instr = new FIFInstr_SEV(i, pc);
        }

        public void visit(Instr.SEZ i) {
            instr = new FIFInstr_SEZ(i, pc);
        }

        public void visit(Instr.SLEEP i) {
            instr = new FIFInstr_SLEEP(i, pc);
        }

        public void visit(Instr.SPM i) {
            instr = new FIFInstr_SPM(i, pc);
        }

        public void visit(Instr.ST i) {
            instr = new FIFInstr_ST(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.STD i) {
            instr = new FIFInstr_STD(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.STPD i) {
            instr = new FIFInstr_STPD(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.STPI i) {
            instr = new FIFInstr_STPI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.STS i) {
            instr = new FIFInstr_STS(i, pc);
            instr.imm1 = i.imm1;
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.SUB i) {
            instr = new FIFInstr_SUB(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.r2 = i.r2.getNumber();
        }

        public void visit(Instr.SUBI i) {
            instr = new FIFInstr_SUBI(i, pc);
            instr.r1 = i.r1.getNumber();
            instr.imm1 = i.imm1;
        }

        public void visit(Instr.SWAP i) {
            instr = new FIFInstr_SWAP(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.TST i) {
            instr = new FIFInstr_TST(i, pc);
            instr.r1 = i.r1.getNumber();
        }

        public void visit(Instr.WDR i) {
            instr = new FIFInstr_WDR(i, pc);
        }
    }

    protected class FIFInstr_ADC extends FIFInstr {
        FIFInstr_ADC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterUnsigned(r1);
            int tmp_1 = getRegisterUnsigned(r2);
            int tmp_2 = bit(interp.C);
            int tmp_3 = tmp_0 + tmp_1 + tmp_2;
            int tmp_4 = (tmp_0 & 0x0000000F);
            int tmp_5 = (tmp_1 & 0x0000000F);
            boolean tmp_6 = ((tmp_0 & 128) != 0);
            boolean tmp_7 = ((tmp_1 & 128) != 0);
            boolean tmp_8 = ((tmp_3 & 128) != 0);
            interp.H = ((tmp_4 + tmp_5 + tmp_2 & 16) != 0);
            interp.C = ((tmp_3 & 256) != 0);
            interp.N = ((tmp_3 & 128) != 0);
            interp.Z = low(tmp_3) == 0;
            interp.V = tmp_6 && tmp_7 && !tmp_8 || !tmp_6 && !tmp_7 && tmp_8;
            interp.S = xor(interp.N, interp.V);
            byte tmp_9 = low(tmp_3);
            setRegisterByte(r1, tmp_9);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_ADD extends FIFInstr {
        FIFInstr_ADD(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterUnsigned(r1);
            int tmp_1 = getRegisterUnsigned(r2);
            int tmp_2 = 0;
            int tmp_3 = tmp_0 + tmp_1 + tmp_2;
            int tmp_4 = (tmp_0 & 0x0000000F);
            int tmp_5 = (tmp_1 & 0x0000000F);
            boolean tmp_6 = ((tmp_0 & 128) != 0);
            boolean tmp_7 = ((tmp_1 & 128) != 0);
            boolean tmp_8 = ((tmp_3 & 128) != 0);
            interp.H = ((tmp_4 + tmp_5 + tmp_2 & 16) != 0);
            interp.C = ((tmp_3 & 256) != 0);
            interp.N = ((tmp_3 & 128) != 0);
            interp.Z = low(tmp_3) == 0;
            interp.V = tmp_6 && tmp_7 && !tmp_8 || !tmp_6 && !tmp_7 && tmp_8;
            interp.S = xor(interp.N, interp.V);
            byte tmp_9 = low(tmp_3);
            setRegisterByte(r1, tmp_9);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_ADIW extends FIFInstr {
        FIFInstr_ADIW(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(r1);
            int tmp_1 = tmp_0 + imm1;
            boolean tmp_2 = ((tmp_1 & 32768) != 0);
            boolean tmp_3 = ((tmp_0 & 32768) != 0);
            interp.C = !tmp_2 && tmp_3;
            interp.N = tmp_2;
            interp.V = !tmp_3 && tmp_2;
            interp.Z = (tmp_1 & 0x0000FFFF) == 0;
            interp.S = xor(interp.N, interp.V);
            setRegisterWord(r1, tmp_1);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_AND extends FIFInstr {
        FIFInstr_AND(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = getRegisterByte(r2);
            int tmp_2 = tmp_0 & tmp_1;
            interp.N = ((tmp_2 & 128) != 0);
            interp.Z = low(tmp_2) == 0;
            interp.V = false;
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_ANDI extends FIFInstr {
        FIFInstr_ANDI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = imm1;
            int tmp_2 = tmp_0 & tmp_1;
            interp.N = ((tmp_2 & 128) != 0);
            interp.Z = low(tmp_2) == 0;
            interp.V = false;
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_ASR extends FIFInstr {
        FIFInstr_ASR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            byte tmp_0 = getRegisterByte(r1);
            int tmp_1 = tmp_0;
            boolean tmp_2 = ((tmp_0 & 128) != 0);
            int tmp_3 = (tmp_1 & 255) >> 1;
            tmp_3 = Arithmetic.setBit(tmp_3, 7, tmp_2);
            interp.C = ((tmp_1 & 1) != 0);
            interp.N = tmp_2;
            interp.Z = low(tmp_3) == 0;
            interp.V = xor(interp.N, interp.C);
            interp.S = xor(interp.N, interp.V);
            byte tmp_4 = low(tmp_3);
            setRegisterByte(r1, tmp_4);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BCLR extends FIFInstr {
        FIFInstr_BCLR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            getIOReg(interp.SREG).writeBit(imm1, false);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BLD extends FIFInstr {
        FIFInstr_BLD(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, Arithmetic.setBit(getRegisterByte(r1), imm1, interp.T));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRBC extends FIFInstr {
        FIFInstr_BRBC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!getIOReg(interp.SREG).readBit(imm1)) {
                int tmp_0 = imm2;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRBS extends FIFInstr {
        FIFInstr_BRBS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (getIOReg(interp.SREG).readBit(imm1)) {
                int tmp_0 = imm2;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRCC extends FIFInstr {
        FIFInstr_BRCC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!interp.C) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRCS extends FIFInstr {
        FIFInstr_BRCS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (interp.C) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BREAK extends FIFInstr {
        FIFInstr_BREAK(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            stop();
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BREQ extends FIFInstr {
        FIFInstr_BREQ(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (interp.Z) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRGE extends FIFInstr {
        FIFInstr_BRGE(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!interp.S) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRHC extends FIFInstr {
        FIFInstr_BRHC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!interp.H) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRHS extends FIFInstr {
        FIFInstr_BRHS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (interp.H) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRID extends FIFInstr {
        FIFInstr_BRID(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!interp.I) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRIE extends FIFInstr {
        FIFInstr_BRIE(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (interp.I) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRLO extends FIFInstr {
        FIFInstr_BRLO(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (interp.C) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRLT extends FIFInstr {
        FIFInstr_BRLT(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (interp.S) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRMI extends FIFInstr {
        FIFInstr_BRMI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (interp.N) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRNE extends FIFInstr {
        FIFInstr_BRNE(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!interp.Z) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRPL extends FIFInstr {
        FIFInstr_BRPL(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!interp.N) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRSH extends FIFInstr {
        FIFInstr_BRSH(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!interp.C) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRTC extends FIFInstr {
        FIFInstr_BRTC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!interp.T) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRTS extends FIFInstr {
        FIFInstr_BRTS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (interp.T) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRVC extends FIFInstr {
        FIFInstr_BRVC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!interp.V) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BRVS extends FIFInstr {
        FIFInstr_BRVS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (interp.V) {
                int tmp_0 = imm1;
                int tmp_1 = tmp_0;
                int tmp_2 = tmp_1 * 2 + nextInstr.pc;
                nextInstr = fifMap[tmp_2];
                interp.cyclesConsumed = interp.cyclesConsumed + 1;
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BSET extends FIFInstr {
        FIFInstr_BSET(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            getIOReg(interp.SREG).writeBit(imm1, true);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_BST extends FIFInstr {
        FIFInstr_BST(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.T = Arithmetic.getBit(getRegisterByte(r1), imm1);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CALL extends FIFInstr {
        FIFInstr_CALL(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = nextInstr.pc;
            tmp_0 = tmp_0 / 2;
            pushByte(low(tmp_0));
            pushByte(high(tmp_0));
            int tmp_1 = imm1;
            int tmp_2 = tmp_1 * 2;
            nextInstr = fifMap[tmp_2];
            cyclesConsumed += 4;
        }
    }

    protected class FIFInstr_CBI extends FIFInstr {
        FIFInstr_CBI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            getIOReg(imm1).writeBit(imm2, false);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_CBR extends FIFInstr {
        FIFInstr_CBR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = ~imm1;
            int tmp_2 = tmp_0 & tmp_1;
            interp.N = ((tmp_2 & 128) != 0);
            interp.Z = low(tmp_2) == 0;
            interp.V = false;
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CLC extends FIFInstr {
        FIFInstr_CLC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.C = false;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CLH extends FIFInstr {
        FIFInstr_CLH(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.H = false;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CLI extends FIFInstr {
        FIFInstr_CLI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            disableInterrupts();
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CLN extends FIFInstr {
        FIFInstr_CLN(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.N = false;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CLR extends FIFInstr {
        FIFInstr_CLR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.S = false;
            interp.V = false;
            interp.N = false;
            interp.Z = true;
            setRegisterByte(r1, low(0));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CLS extends FIFInstr {
        FIFInstr_CLS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.S = false;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CLT extends FIFInstr {
        FIFInstr_CLT(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.T = false;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CLV extends FIFInstr {
        FIFInstr_CLV(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.V = false;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CLZ extends FIFInstr {
        FIFInstr_CLZ(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.Z = false;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_COM extends FIFInstr {
        FIFInstr_COM(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = 255 - getRegisterByte(r1);
            interp.C = true;
            interp.N = ((tmp_0 & 128) != 0);
            interp.Z = low(tmp_0) == 0;
            interp.V = false;
            interp.S = xor(interp.N, interp.V);
            setRegisterByte(r1, low(tmp_0));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CP extends FIFInstr {
        FIFInstr_CP(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = getRegisterByte(r2);
            int tmp_2 = 0;
            int tmp_3 = tmp_0 - tmp_1 - tmp_2;
            boolean tmp_4 = ((tmp_0 & 128) != 0);
            boolean tmp_5 = ((tmp_1 & 128) != 0);
            boolean tmp_6 = ((tmp_3 & 128) != 0);
            boolean tmp_7 = ((tmp_0 & 8) != 0);
            boolean tmp_8 = ((tmp_1 & 8) != 0);
            boolean tmp_9 = ((tmp_3 & 8) != 0);
            interp.H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
            interp.C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
            interp.N = tmp_6;
            interp.Z = low(tmp_3) == 0;
            interp.V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
            interp.S = xor(interp.N, interp.V);
            byte tmp_10 = low(tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CPC extends FIFInstr {
        FIFInstr_CPC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = getRegisterByte(r2);
            int tmp_2 = bit(interp.C);
            int tmp_3 = tmp_0 - tmp_1 - tmp_2;
            boolean tmp_4 = ((tmp_0 & 128) != 0);
            boolean tmp_5 = ((tmp_1 & 128) != 0);
            boolean tmp_6 = ((tmp_3 & 128) != 0);
            boolean tmp_7 = ((tmp_0 & 8) != 0);
            boolean tmp_8 = ((tmp_1 & 8) != 0);
            boolean tmp_9 = ((tmp_3 & 8) != 0);
            interp.H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
            interp.C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
            interp.N = tmp_6;
            interp.Z = low(tmp_3) == 0 && interp.Z;
            interp.V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
            interp.S = xor(interp.N, interp.V);
            byte tmp_10 = low(tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CPI extends FIFInstr {
        FIFInstr_CPI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = imm1;
            int tmp_2 = 0;
            int tmp_3 = tmp_0 - tmp_1 - tmp_2;
            boolean tmp_4 = ((tmp_0 & 128) != 0);
            boolean tmp_5 = ((tmp_1 & 128) != 0);
            boolean tmp_6 = ((tmp_3 & 128) != 0);
            boolean tmp_7 = ((tmp_0 & 8) != 0);
            boolean tmp_8 = ((tmp_1 & 8) != 0);
            boolean tmp_9 = ((tmp_3 & 8) != 0);
            interp.H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
            interp.C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
            interp.N = tmp_6;
            interp.Z = low(tmp_3) == 0;
            interp.V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
            interp.S = xor(interp.N, interp.V);
            byte tmp_10 = low(tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_CPSE extends FIFInstr {
        FIFInstr_CPSE(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = getRegisterByte(r2);
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
            interp.H = !tmp_9 && tmp_10 || tmp_10 && tmp_11 || tmp_11 && !tmp_9;
            interp.C = !tmp_6 && tmp_7 || tmp_7 && tmp_8 || tmp_8 && !tmp_6;
            interp.N = tmp_8;
            interp.Z = low(tmp_5) == 0;
            interp.V = tmp_6 && !tmp_7 && !tmp_8 || !tmp_6 && tmp_7 && tmp_8;
            interp.S = xor(interp.N, interp.V);
            byte tmp_12 = low(tmp_5);
            if (tmp_0 == tmp_1) {
                int tmp_13 = getInstrSize(nextInstr.pc);
                nextInstr = fifMap[nextInstr.pc + tmp_13];
                if (tmp_13 == 4) {
                    interp.cyclesConsumed = interp.cyclesConsumed + 2;
                } else {
                    interp.cyclesConsumed = interp.cyclesConsumed + 1;
                }
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_DEC extends FIFInstr {
        FIFInstr_DEC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterUnsigned(r1);
            byte tmp_1 = low(tmp_0 - 1);
            interp.N = ((tmp_1 & 128) != 0);
            interp.Z = tmp_1 == 0;
            interp.V = tmp_0 == 128;
            interp.S = xor(interp.N, interp.V);
            setRegisterByte(r1, tmp_1);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_EICALL extends FIFInstr {
        FIFInstr_EICALL(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            cyclesConsumed += 4;
        }
    }

    protected class FIFInstr_EIJMP extends FIFInstr {
        FIFInstr_EIJMP(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_ELPM extends FIFInstr {
        FIFInstr_ELPM(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(interp.RZ);
            tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(interp.RAMPZ) & 0x000000FF) << 16);
            setRegisterByte(interp.R0, getProgramByte(tmp_0));
            cyclesConsumed += 3;
        }
    }

    protected class FIFInstr_ELPMD extends FIFInstr {
        FIFInstr_ELPMD(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(interp.RZ);
            tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(interp.RAMPZ) & 0x000000FF) << 16);
            setRegisterByte(r1, getProgramByte(tmp_0));
            cyclesConsumed += 3;
        }
    }

    protected class FIFInstr_ELPMPI extends FIFInstr {
        FIFInstr_ELPMPI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(interp.RZ);
            tmp_0 = (tmp_0 & 0xFF00FFFF) | ((getIORegisterByte(interp.RAMPZ) & 0x000000FF) << 16);
            setRegisterByte(r1, getProgramByte(tmp_0));
            setRegisterWord(interp.RZ, tmp_0 + 1);
            cyclesConsumed += 3;
        }
    }

    protected class FIFInstr_EOR extends FIFInstr {
        FIFInstr_EOR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            byte tmp_0 = low(getRegisterByte(r1) ^ getRegisterByte(r2));
            interp.N = ((tmp_0 & 128) != 0);
            interp.Z = tmp_0 == 0;
            interp.V = false;
            interp.S = xor(interp.N, interp.V);
            setRegisterByte(r1, tmp_0);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_FMUL extends FIFInstr {
        FIFInstr_FMUL(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterUnsigned(r1) * getRegisterUnsigned(r2) << 1;
            interp.Z = (tmp_0 & 0x0000FFFF) == 0;
            interp.C = ((tmp_0 & 65536) != 0);
            setRegisterByte(interp.R0, low(tmp_0));
            setRegisterByte(interp.R1, high(tmp_0));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_FMULS extends FIFInstr {
        FIFInstr_FMULS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1) * getRegisterByte(r2) << 1;
            interp.Z = (tmp_0 & 0x0000FFFF) == 0;
            interp.C = ((tmp_0 & 65536) != 0);
            setRegisterByte(interp.R0, low(tmp_0));
            setRegisterByte(interp.R1, high(tmp_0));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_FMULSU extends FIFInstr {
        FIFInstr_FMULSU(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1) * getRegisterUnsigned(r2) << 1;
            interp.Z = (tmp_0 & 0x0000FFFF) == 0;
            interp.C = ((tmp_0 & 65536) != 0);
            setRegisterByte(interp.R0, low(tmp_0));
            setRegisterByte(interp.R1, high(tmp_0));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_ICALL extends FIFInstr {
        FIFInstr_ICALL(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = nextInstr.pc;
            tmp_0 = tmp_0 / 2;
            pushByte(low(tmp_0));
            pushByte(high(tmp_0));
            int tmp_1 = getRegisterWord(interp.RZ);
            int tmp_2 = tmp_1 * 2;
            nextInstr = fifMap[tmp_2];
            cyclesConsumed += 3;
        }
    }

    protected class FIFInstr_IJMP extends FIFInstr {
        FIFInstr_IJMP(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(interp.RZ);
            int tmp_1 = tmp_0 * 2;
            nextInstr = fifMap[tmp_1];
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_IN extends FIFInstr {
        FIFInstr_IN(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, getIORegisterByte(imm1));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_INC extends FIFInstr {
        FIFInstr_INC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterUnsigned(r1);
            byte tmp_1 = low(tmp_0 + 1);
            interp.N = ((tmp_1 & 128) != 0);
            interp.Z = tmp_1 == 0;
            interp.V = tmp_0 == 127;
            interp.S = xor(interp.N, interp.V);
            setRegisterByte(r1, tmp_1);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_JMP extends FIFInstr {
        FIFInstr_JMP(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = imm1;
            int tmp_1 = tmp_0 * 2;
            nextInstr = fifMap[tmp_1];
            cyclesConsumed += 3;
        }
    }

    protected class FIFInstr_LD extends FIFInstr {
        FIFInstr_LD(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, getDataByte(getRegisterWord(r2)));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_LDD extends FIFInstr {
        FIFInstr_LDD(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, getDataByte(getRegisterWord(r2) + imm1));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_LDI extends FIFInstr {
        FIFInstr_LDI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, low(imm1));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_LDPD extends FIFInstr {
        FIFInstr_LDPD(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(r2) - 1;
            setRegisterByte(r1, getDataByte(tmp_0));
            setRegisterWord(r2, tmp_0);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_LDPI extends FIFInstr {
        FIFInstr_LDPI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(r2);
            setRegisterByte(r1, getDataByte(tmp_0));
            setRegisterWord(r2, tmp_0 + 1);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_LDS extends FIFInstr {
        FIFInstr_LDS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, getDataByte(imm1));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_LPM extends FIFInstr {
        FIFInstr_LPM(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(interp.R0, getProgramByte(getRegisterWord(interp.RZ)));
            cyclesConsumed += 3;
        }
    }

    protected class FIFInstr_LPMD extends FIFInstr {
        FIFInstr_LPMD(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, getProgramByte(getRegisterWord(interp.RZ)));
            cyclesConsumed += 3;
        }
    }

    protected class FIFInstr_LPMPI extends FIFInstr {
        FIFInstr_LPMPI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(interp.RZ);
            setRegisterByte(r1, getProgramByte(tmp_0));
            setRegisterWord(interp.RZ, tmp_0 + 1);
            cyclesConsumed += 3;
        }
    }

    protected class FIFInstr_LSL extends FIFInstr {
        FIFInstr_LSL(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            boolean tmp_1 = false;
            int tmp_2 = tmp_0 << 1;
            tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
            interp.H = ((tmp_2 & 16) != 0);
            interp.C = ((tmp_2 & 256) != 0);
            interp.N = ((tmp_2 & 128) != 0);
            interp.Z = low(tmp_2) == 0;
            interp.V = xor(interp.N, interp.C);
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_LSR extends FIFInstr {
        FIFInstr_LSR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            boolean tmp_1 = false;
            int tmp_2 = (tmp_0 & 255) >> 1;
            tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
            interp.C = ((tmp_0 & 1) != 0);
            interp.N = tmp_1;
            interp.Z = low(tmp_2) == 0;
            interp.V = xor(interp.N, interp.C);
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_MOV extends FIFInstr {
        FIFInstr_MOV(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, getRegisterByte(r2));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_MOVW extends FIFInstr {
        FIFInstr_MOVW(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterWord(r1, getRegisterWord(r2));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_MUL extends FIFInstr {
        FIFInstr_MUL(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterUnsigned(r1) * getRegisterUnsigned(r2);
            interp.C = ((tmp_0 & 32768) != 0);
            interp.Z = (tmp_0 & 0x0000FFFF) == 0;
            setRegisterWord(interp.R0, tmp_0);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_MULS extends FIFInstr {
        FIFInstr_MULS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1) * getRegisterByte(r2);
            interp.C = ((tmp_0 & 32768) != 0);
            interp.Z = (tmp_0 & 0x0000FFFF) == 0;
            setRegisterWord(interp.R0, tmp_0);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_MULSU extends FIFInstr {
        FIFInstr_MULSU(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1) * getRegisterUnsigned(r2);
            interp.C = ((tmp_0 & 32768) != 0);
            interp.Z = (tmp_0 & 0x0000FFFF) == 0;
            setRegisterWord(interp.R0, tmp_0);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_NEG extends FIFInstr {
        FIFInstr_NEG(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = 0;
            int tmp_1 = getRegisterByte(r1);
            int tmp_2 = 0;
            int tmp_3 = tmp_0 - tmp_1 - tmp_2;
            boolean tmp_4 = ((tmp_0 & 128) != 0);
            boolean tmp_5 = ((tmp_1 & 128) != 0);
            boolean tmp_6 = ((tmp_3 & 128) != 0);
            boolean tmp_7 = ((tmp_0 & 8) != 0);
            boolean tmp_8 = ((tmp_1 & 8) != 0);
            boolean tmp_9 = ((tmp_3 & 8) != 0);
            interp.H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
            interp.C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
            interp.N = tmp_6;
            interp.Z = low(tmp_3) == 0;
            interp.V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
            interp.S = xor(interp.N, interp.V);
            byte tmp_10 = low(tmp_3);
            setRegisterByte(r1, tmp_10);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_NOP extends FIFInstr {
        FIFInstr_NOP(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_OR extends FIFInstr {
        FIFInstr_OR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = getRegisterByte(r2);
            int tmp_2 = tmp_0 | tmp_1;
            interp.N = ((tmp_2 & 128) != 0);
            interp.Z = low(tmp_2) == 0;
            interp.V = false;
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_ORI extends FIFInstr {
        FIFInstr_ORI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = imm1;
            int tmp_2 = tmp_0 | tmp_1;
            interp.N = ((tmp_2 & 128) != 0);
            interp.Z = low(tmp_2) == 0;
            interp.V = false;
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_OUT extends FIFInstr {
        FIFInstr_OUT(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setIORegisterByte(imm1, getRegisterByte(r1));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_POP extends FIFInstr {
        FIFInstr_POP(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, popByte());
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_PUSH extends FIFInstr {
        FIFInstr_PUSH(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            pushByte(getRegisterByte(r1));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_RCALL extends FIFInstr {
        FIFInstr_RCALL(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = nextInstr.pc;
            tmp_0 = tmp_0 / 2;
            pushByte(low(tmp_0));
            pushByte(high(tmp_0));
            int tmp_1 = imm1;
            int tmp_2 = tmp_1 * 2 + nextInstr.pc;
            nextInstr = fifMap[tmp_2];
            cyclesConsumed += 3;
        }
    }

    protected class FIFInstr_RET extends FIFInstr {
        FIFInstr_RET(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            byte tmp_0 = popByte();
            byte tmp_1 = popByte();
            int tmp_2 = uword(tmp_1, tmp_0) * 2;
            nextInstr = fifMap[tmp_2];
            cyclesConsumed += 4;
        }
    }

    protected class FIFInstr_RETI extends FIFInstr {
        FIFInstr_RETI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            byte tmp_0 = popByte();
            byte tmp_1 = popByte();
            int tmp_2 = uword(tmp_1, tmp_0) * 2;
            nextInstr = fifMap[tmp_2];
            enableInterrupts();
            interp.justReturnedFromInterrupt = true;
            cyclesConsumed += 4;
        }
    }

    protected class FIFInstr_RJMP extends FIFInstr {
        FIFInstr_RJMP(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = imm1;
            int tmp_1 = tmp_0 * 2 + nextInstr.pc;
            nextInstr = fifMap[tmp_1];
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_ROL extends FIFInstr {
        FIFInstr_ROL(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterUnsigned(r1);
            boolean tmp_1 = interp.C;
            int tmp_2 = tmp_0 << 1;
            tmp_2 = Arithmetic.setBit(tmp_2, 0, tmp_1);
            interp.H = ((tmp_2 & 16) != 0);
            interp.C = ((tmp_2 & 256) != 0);
            interp.N = ((tmp_2 & 128) != 0);
            interp.Z = low(tmp_2) == 0;
            interp.V = xor(interp.N, interp.C);
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_ROR extends FIFInstr {
        FIFInstr_ROR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            boolean tmp_1 = interp.C;
            int tmp_2 = (tmp_0 & 255) >> 1;
            tmp_2 = Arithmetic.setBit(tmp_2, 7, tmp_1);
            interp.C = ((tmp_0 & 1) != 0);
            interp.N = tmp_1;
            interp.Z = low(tmp_2) == 0;
            interp.V = xor(interp.N, interp.C);
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SBC extends FIFInstr {
        FIFInstr_SBC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = getRegisterByte(r2);
            int tmp_2 = bit(interp.C);
            int tmp_3 = tmp_0 - tmp_1 - tmp_2;
            boolean tmp_4 = ((tmp_0 & 128) != 0);
            boolean tmp_5 = ((tmp_1 & 128) != 0);
            boolean tmp_6 = ((tmp_3 & 128) != 0);
            boolean tmp_7 = ((tmp_0 & 8) != 0);
            boolean tmp_8 = ((tmp_1 & 8) != 0);
            boolean tmp_9 = ((tmp_3 & 8) != 0);
            interp.H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
            interp.C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
            interp.N = tmp_6;
            interp.Z = low(tmp_3) == 0 && interp.Z;
            interp.V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
            interp.S = xor(interp.N, interp.V);
            byte tmp_10 = low(tmp_3);
            setRegisterByte(r1, tmp_10);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SBCI extends FIFInstr {
        FIFInstr_SBCI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = imm1;
            int tmp_2 = bit(interp.C);
            int tmp_3 = tmp_0 - tmp_1 - tmp_2;
            boolean tmp_4 = ((tmp_0 & 128) != 0);
            boolean tmp_5 = ((tmp_1 & 128) != 0);
            boolean tmp_6 = ((tmp_3 & 128) != 0);
            boolean tmp_7 = ((tmp_0 & 8) != 0);
            boolean tmp_8 = ((tmp_1 & 8) != 0);
            boolean tmp_9 = ((tmp_3 & 8) != 0);
            interp.H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
            interp.C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
            interp.N = tmp_6;
            interp.Z = low(tmp_3) == 0 && interp.Z;
            interp.V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
            interp.S = xor(interp.N, interp.V);
            byte tmp_10 = low(tmp_3);
            setRegisterByte(r1, tmp_10);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SBI extends FIFInstr {
        FIFInstr_SBI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            getIOReg(imm1).writeBit(imm2, true);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_SBIC extends FIFInstr {
        FIFInstr_SBIC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!getIOReg(imm1).readBit(imm2)) {
                int tmp_0 = getInstrSize(nextInstr.pc);
                nextInstr = fifMap[nextInstr.pc + tmp_0];
                if (tmp_0 == 4) {
                    interp.cyclesConsumed = interp.cyclesConsumed + 2;
                } else {
                    interp.cyclesConsumed = interp.cyclesConsumed + 1;
                }
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SBIS extends FIFInstr {
        FIFInstr_SBIS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (getIOReg(imm1).readBit(imm2)) {
                int tmp_0 = getInstrSize(nextInstr.pc);
                nextInstr = fifMap[nextInstr.pc + tmp_0];
                if (tmp_0 == 4) {
                    interp.cyclesConsumed = interp.cyclesConsumed + 2;
                } else {
                    interp.cyclesConsumed = interp.cyclesConsumed + 1;
                }
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SBIW extends FIFInstr {
        FIFInstr_SBIW(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(r1);
            int tmp_1 = tmp_0 - imm1;
            boolean tmp_2 = ((tmp_0 & 32768) != 0);
            boolean tmp_3 = ((tmp_1 & 32768) != 0);
            interp.V = tmp_2 && !tmp_3;
            interp.N = tmp_3;
            interp.Z = (tmp_1 & 0x0000FFFF) == 0;
            interp.C = tmp_3 && !tmp_2;
            interp.S = xor(interp.N, interp.V);
            setRegisterWord(r1, tmp_1);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_SBR extends FIFInstr {
        FIFInstr_SBR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = imm1;
            int tmp_2 = tmp_0 | tmp_1;
            interp.N = ((tmp_2 & 128) != 0);
            interp.Z = low(tmp_2) == 0;
            interp.V = false;
            interp.S = xor(interp.N, interp.V);
            byte tmp_3 = low(tmp_2);
            setRegisterByte(r1, tmp_3);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SBRC extends FIFInstr {
        FIFInstr_SBRC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (!Arithmetic.getBit(getRegisterByte(r1), imm1)) {
                int tmp_0 = getInstrSize(nextInstr.pc);
                nextInstr = fifMap[nextInstr.pc + tmp_0];
                if (tmp_0 == 4) {
                    interp.cyclesConsumed = interp.cyclesConsumed + 2;
                } else {
                    interp.cyclesConsumed = interp.cyclesConsumed + 1;
                }
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SBRS extends FIFInstr {
        FIFInstr_SBRS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            if (Arithmetic.getBit(getRegisterByte(r1), imm1)) {
                int tmp_0 = getInstrSize(nextInstr.pc);
                nextInstr = fifMap[nextInstr.pc + tmp_0];
                if (tmp_0 == 4) {
                    interp.cyclesConsumed = interp.cyclesConsumed + 2;
                } else {
                    interp.cyclesConsumed = interp.cyclesConsumed + 1;
                }
            } else {
            }
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SEC extends FIFInstr {
        FIFInstr_SEC(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.C = true;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SEH extends FIFInstr {
        FIFInstr_SEH(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.H = true;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SEI extends FIFInstr {
        FIFInstr_SEI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            enableInterrupts();
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SEN extends FIFInstr {
        FIFInstr_SEN(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.N = true;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SER extends FIFInstr {
        FIFInstr_SER(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setRegisterByte(r1, low(255));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SES extends FIFInstr {
        FIFInstr_SES(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.S = true;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SET extends FIFInstr {
        FIFInstr_SET(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.T = true;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SEV extends FIFInstr {
        FIFInstr_SEV(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.V = true;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SEZ extends FIFInstr {
        FIFInstr_SEZ(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            interp.Z = true;
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SLEEP extends FIFInstr {
        FIFInstr_SLEEP(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            enterSleepMode();
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SPM extends FIFInstr {
        FIFInstr_SPM(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_ST extends FIFInstr {
        FIFInstr_ST(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setDataByte(getRegisterWord(r1), getRegisterByte(r2));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_STD extends FIFInstr {
        FIFInstr_STD(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setDataByte(getRegisterWord(r1) + imm1, getRegisterByte(r2));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_STPD extends FIFInstr {
        FIFInstr_STPD(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(r1) - 1;
            setDataByte(tmp_0, getRegisterByte(r2));
            setRegisterWord(r1, tmp_0);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_STPI extends FIFInstr {
        FIFInstr_STPI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterWord(r1);
            setDataByte(tmp_0, getRegisterByte(r2));
            setRegisterWord(r1, tmp_0 + 1);
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_STS extends FIFInstr {
        FIFInstr_STS(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            setDataByte(imm1, getRegisterByte(r1));
            cyclesConsumed += 2;
        }
    }

    protected class FIFInstr_SUB extends FIFInstr {
        FIFInstr_SUB(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = getRegisterByte(r2);
            int tmp_2 = 0;
            int tmp_3 = tmp_0 - tmp_1 - tmp_2;
            boolean tmp_4 = ((tmp_0 & 128) != 0);
            boolean tmp_5 = ((tmp_1 & 128) != 0);
            boolean tmp_6 = ((tmp_3 & 128) != 0);
            boolean tmp_7 = ((tmp_0 & 8) != 0);
            boolean tmp_8 = ((tmp_1 & 8) != 0);
            boolean tmp_9 = ((tmp_3 & 8) != 0);
            interp.H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
            interp.C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
            interp.N = tmp_6;
            interp.Z = low(tmp_3) == 0;
            interp.V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
            interp.S = xor(interp.N, interp.V);
            byte tmp_10 = low(tmp_3);
            setRegisterByte(r1, tmp_10);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SUBI extends FIFInstr {
        FIFInstr_SUBI(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            int tmp_1 = imm1;
            int tmp_2 = 0;
            int tmp_3 = tmp_0 - tmp_1 - tmp_2;
            boolean tmp_4 = ((tmp_0 & 128) != 0);
            boolean tmp_5 = ((tmp_1 & 128) != 0);
            boolean tmp_6 = ((tmp_3 & 128) != 0);
            boolean tmp_7 = ((tmp_0 & 8) != 0);
            boolean tmp_8 = ((tmp_1 & 8) != 0);
            boolean tmp_9 = ((tmp_3 & 8) != 0);
            interp.H = !tmp_7 && tmp_8 || tmp_8 && tmp_9 || tmp_9 && !tmp_7;
            interp.C = !tmp_4 && tmp_5 || tmp_5 && tmp_6 || tmp_6 && !tmp_4;
            interp.N = tmp_6;
            interp.Z = low(tmp_3) == 0;
            interp.V = tmp_4 && !tmp_5 && !tmp_6 || !tmp_4 && tmp_5 && tmp_6;
            interp.S = xor(interp.N, interp.V);
            byte tmp_10 = low(tmp_3);
            setRegisterByte(r1, tmp_10);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_SWAP extends FIFInstr {
        FIFInstr_SWAP(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterUnsigned(r1);
            int tmp_1 = 0;
            tmp_1 = (tmp_1 & 0xFFFFFFF0) | ((((tmp_0 >> 4) & 0x0000000F) & 0x0000000F));
            tmp_1 = (tmp_1 & 0xFFFFFF0F) | (((tmp_0 & 0x0000000F) & 0x0000000F) << 4);
            setRegisterByte(r1, low(tmp_1));
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_TST extends FIFInstr {
        FIFInstr_TST(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            int tmp_0 = getRegisterByte(r1);
            interp.V = false;
            interp.Z = low(tmp_0) == 0;
            interp.N = ((tmp_0 & 128) != 0);
            interp.S = xor(interp.N, interp.V);
            cyclesConsumed += 1;
        }
    }

    protected class FIFInstr_WDR extends FIFInstr {
        FIFInstr_WDR(Instr i, int pc) {
            super(i, pc);
        }

        public void execute(FIFInterpreter interp) {
            cyclesConsumed += 1;
        }
    }
//--END FIF GENERATOR--

    //
    //  U T I L I T I E S
    // ------------------------------------------------------------
    //
    //  These are utility functions for expressing instructions
    //  more concisely. They are private and can be inlined by
    //  the JIT compiler or javac -O.
    //
    //

    public static final int R0 = 0;
    public static final int RZ = 30;
    public static final int R1 = 1;

    private void pushPC(int npc) {
        npc = npc / 2;
        pushByte(Arithmetic.low(npc));
        pushByte(Arithmetic.high(npc));
    }

    private boolean xor(boolean a, boolean b) {
        return (a && !b) || (b && !a);
    }

    private byte low(int val) {
        return (byte) val;
    }

    private byte high(int val) {
        return (byte) (val >> 8);
    }

    private byte bit(boolean val) {
        if (val) return 1;
        return 0;
    }

    private int uword(byte low, byte high) {
        return Arithmetic.uword(low, high);
    }

    private void enterSleepMode() {
        sleeping = true;
        innerLoop = false;
    }

    protected int getRegisterUnsigned(int reg) {
        return regs[reg];
    }

    protected byte getRegisterByte(int reg) {
        return regs[reg];
    }

    protected void setRegisterByte(int reg, byte val) {
        regs[reg] = val;
    }

    protected int getRegisterWord(int reg) {
        return Arithmetic.uword(regs[reg], regs[reg + 1]);
    }

    protected void setRegisterWord(int reg, int val) {
        regs[reg] = low(val);
        regs[reg + 1] = high(val);
    }

}
