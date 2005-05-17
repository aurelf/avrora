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

package avrora.stack.isea;

import avrora.core.InstrVisitor;
import avrora.core.Instr;
import avrora.core.Register;
import avrora.util.Arithmetic;

/**
 * The <code>ISEInterpreter</code> class implements an abstract interpreter for intraprocedural
 * side effect analysis. This abstract interpreter simply keeps tracks of which register values
 * have been written, read, or overwritten, as well as the values on the stack.
 *
 * @author Ben L. Titzer
 */
public abstract class ISEInterpreter implements InstrVisitor {

    abstract byte readRegister(Register r);
    abstract void writeRegister(Register r, byte val);
    abstract byte getSREG();
    abstract void setSREG(byte val);
    abstract byte readIORegister(int ioreg);
    abstract void writeIORegister(int ioreg, byte val);

    abstract int relative(int addr);
    abstract void branch(int addr);
    abstract byte popByte();
    abstract void pushByte(byte val);

    private void mult(Register r1, Register r2) {
        int tmp_0 = readRegister(r1);
        int tmp_1 = readRegister(r2);
        setSREG(ISEValue.UNKNOWN);
        writeRegister(Register.R0, ISEValue.UNKNOWN);
        writeRegister(Register.R1, ISEValue.UNKNOWN);
    }


    void binop(Register r1, Register r2) {
        readRegister(r1);
        readRegister(r2);
        setSREG(ISEValue.UNKNOWN);
        writeRegister(r1, ISEValue.UNKNOWN);
    }

    void unop(Register r1) {
        readRegister(r1);
        setSREG(ISEValue.UNKNOWN);
        writeRegister(r1, ISEValue.UNKNOWN);
    }

    public void visit(Instr.ADC i) {
        binop(i.r1, i.r2);
    }

    public void visit(Instr.ADD i) {
        binop(i.r1, i.r2);
    }

    public void visit(Instr.ADIW i) {
        readRegister(i.r1);
        readRegister(i.r1.nextRegister());
        setSREG(ISEValue.UNKNOWN);
        writeRegister(i.r1, ISEValue.UNKNOWN);
        writeRegister(i.r1.nextRegister(), ISEValue.UNKNOWN);
    }

    public void visit(Instr.AND i) {
        binop(i.r1, i.r2);
    }

    public void visit(Instr.ANDI i) {
        unop(i.r1);
    }

    public void visit(Instr.ASR i) {
        unop(i.r1);
    }

    public void visit(Instr.BCLR i) {
        writeIORegister(i.imm1, ISEValue.UNKNOWN);
        //getIOReg(SREG).writeBit(i.imm1, false);
    }

    public void visit(Instr.BLD i) {
        readRegister(i.r1);
        writeRegister(i.r1, ISEValue.UNKNOWN);
        //writeRegister(i.r1, Arithmetic.setBit(readRegister(i.r1), i.imm1, T));
    }

    public void visit(Instr.BRBC i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRBS i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRCC i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRCS i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BREAK i) {
    }

    public void visit(Instr.BREQ i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRGE i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRHC i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRHS i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRID i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRIE i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRLO i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRLT i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRMI i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRNE i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRPL i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRSH i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRTC i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRTS i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRVC i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BRVS i) {
        branch(relative(i.imm1));
    }

    public void visit(Instr.BSET i) {
        writeIORegister(i.imm1, ISEValue.UNKNOWN);
        // getIOReg(SREG).writeBit(i.imm1, true);
    }

    public void visit(Instr.BST i) {
        byte tmp0 = readRegister(i.r1);
        setSREG(ISEValue.UNKNOWN);
        //T = Arithmetic.getBit(tmp0, i.imm1);
    }

    public void visit(Instr.CALL i) {
        // TODO: implement calls
    }

    public void visit(Instr.CBI i) {
        writeIORegister(i.imm1, ISEValue.UNKNOWN);
        //getIOReg(i.imm1).writeBit(i.imm2, false);
    }

    public void visit(Instr.CBR i) {
        unop(i.r1);
    }

    public void visit(Instr.CLC i) {
        setSREG(ISEValue.UNKNOWN);
        // C = false;
    }

    public void visit(Instr.CLH i) {
        setSREG(ISEValue.UNKNOWN);
        // H = false;
    }

    public void visit(Instr.CLI i) {
        setSREG(ISEValue.UNKNOWN);
        // disableInterrupts();
    }

    public void visit(Instr.CLN i) {
        setSREG(ISEValue.UNKNOWN);
        // N = false;
    }

    public void visit(Instr.CLR i) {
        unop(i.r1);
    }

    public void visit(Instr.CLS i) {
        setSREG(ISEValue.UNKNOWN);
        // S = false;
    }

    public void visit(Instr.CLT i) {
        setSREG(ISEValue.UNKNOWN);
        // T = false;
    }

    public void visit(Instr.CLV i) {
        setSREG(ISEValue.UNKNOWN);
        // V = false;
    }

    public void visit(Instr.CLZ i) {
        setSREG(ISEValue.UNKNOWN);
        // Z = false;
    }

    public void visit(Instr.COM i) {
        unop(i.r1);
    }

    public void visit(Instr.CP i) {
        readRegister(i.r1);
        readRegister(i.r2);
        setSREG(ISEValue.UNKNOWN);
    }

    public void visit(Instr.CPC i) {
        readRegister(i.r1);
        readRegister(i.r2);
        setSREG(ISEValue.UNKNOWN);
    }

    public void visit(Instr.CPI i) {
        readRegister(i.r1);
        setSREG(ISEValue.UNKNOWN);
    }

    public void visit(Instr.CPSE i) {
        readRegister(i.r1);
        readRegister(i.r2);
        setSREG(ISEValue.UNKNOWN);
        // TODO: implement skip
    }

    public void visit(Instr.DEC i) {
        unop(i.r1);
    }

    public void visit(Instr.EICALL i) {
        // TODO: implement calls
    }

    public void visit(Instr.EIJMP i) {
        // TODO: implement jmps
    }

    public void visit(Instr.ELPM i) {
        readRegister(Register.Z);
        readRegister(Register.Z.nextRegister());
        writeRegister(Register.R0, ISEValue.UNKNOWN);
    }

    public void visit(Instr.ELPMD i) {
        readRegister(Register.Z);
        readRegister(Register.Z.nextRegister());
        writeRegister(i.r1, ISEValue.UNKNOWN);
    }

    public void visit(Instr.ELPMPI i) {
        readRegister(Register.Z);
        readRegister(Register.Z.nextRegister());
        writeRegister(i.r1, ISEValue.UNKNOWN);
        writeRegister(Register.Z, ISEValue.UNKNOWN);
        writeRegister(Register.Z.nextRegister(), ISEValue.UNKNOWN);
    }

    public void visit(Instr.EOR i) {
        binop(i.r1, i.r2);
    }

    public void visit(Instr.FMUL i) {
        mult(i.r1, i.r2);
    }

    public void visit(Instr.FMULS i) {
        mult(i.r1, i.r2);
    }

    public void visit(Instr.FMULSU i) {
        mult(i.r1, i.r2);
    }

    public void visit(Instr.ICALL i) {
        // TODO: implement calls
    }

    public void visit(Instr.IJMP i) {
        // TODO: implement jumps
    }

    public void visit(Instr.IN i) {
        writeRegister(i.r1, readIORegister(i.imm1));
    }

    public void visit(Instr.INC i) {
        unop(i.r1);
    }

    public void visit(Instr.JMP i) {
        // TODO: implement jumps
    }

    public void visit(Instr.LD i) {
        writeRegister(i.r1, ISEValue.UNKNOWN);
    }

    public void visit(Instr.LDD i) {
        writeRegister(i.r1, ISEValue.UNKNOWN);
    }

    public void visit(Instr.LDI i) {
        writeRegister(i.r1, ISEValue.UNKNOWN);
    }

    public void visit(Instr.LDPD i) {
        writeRegister(i.r1, ISEValue.UNKNOWN);
        writeRegister(i.r2, ISEValue.UNKNOWN);
        writeRegister(i.r2.nextRegister(), ISEValue.UNKNOWN);
    }

    public void visit(Instr.LDPI i) {
        writeRegister(i.r1, ISEValue.UNKNOWN);
        writeRegister(i.r2, ISEValue.UNKNOWN);
        writeRegister(i.r2.nextRegister(), ISEValue.UNKNOWN);
    }

    public void visit(Instr.LDS i) {
        writeRegister(i.r1, ISEValue.UNKNOWN);
    }

    public void visit(Instr.LPM i) {
        readRegister(Register.Z);
        readRegister(Register.Z.nextRegister());
        writeRegister(Register.R0, ISEValue.UNKNOWN);
    }

    public void visit(Instr.LPMD i) {
        readRegister(Register.Z);
        readRegister(Register.Z.nextRegister());
        writeRegister(i.r1, ISEValue.UNKNOWN);
    }

    public void visit(Instr.LPMPI i) {
        readRegister(Register.Z);
        readRegister(Register.Z.nextRegister());
        writeRegister(i.r1, ISEValue.UNKNOWN);
        writeRegister(Register.Z, ISEValue.UNKNOWN);
        writeRegister(Register.Z.nextRegister(), ISEValue.UNKNOWN);
    }

    public void visit(Instr.LSL i) {
        unop(i.r1);
    }

    public void visit(Instr.LSR i) {
        unop(i.r1);
    }

    public void visit(Instr.MOV i) {
        writeRegister(i.r1, readRegister(i.r2));
    }

    public void visit(Instr.MOVW i) {
        writeRegister(i.r1, readRegister(i.r2));
        writeRegister(i.r1.nextRegister(), readRegister(i.r2.nextRegister()));
    }

    public void visit(Instr.MUL i) {
        mult(i.r1, i.r2);
    }

    public void visit(Instr.MULS i) {
        mult(i.r1, i.r2);
    }

    public void visit(Instr.MULSU i) {
        mult(i.r1, i.r2);
    }

    public void visit(Instr.NEG i) {
        unop(i.r1);
    }

    public void visit(Instr.NOP i) {
        // do nothing.
    }

    public void visit(Instr.OR i) {
        binop(i.r1, i.r2);
    }

    public void visit(Instr.ORI i) {
        unop(i.r1);
    }

    public void visit(Instr.OUT i) {
        writeIORegister(i.imm1, readRegister(i.r1));
    }

    public void visit(Instr.POP i) {
        // TODO: implement pop
        writeRegister(i.r1, popByte());
    }

    public void visit(Instr.PUSH i) {
        // TODO: implement push
        pushByte(readRegister(i.r1));
    }

    public void visit(Instr.RCALL i) {
        // TODO: implement calls
    }

    public void visit(Instr.RET i) {
        // TODO: implement returns
    }

    public void visit(Instr.RETI i) {
        // TODO: implement returns
    }

    public void visit(Instr.RJMP i) {
        // TODO: implement jumps
    }

    public void visit(Instr.ROL i) {
        unop(i.r1);
    }

    public void visit(Instr.ROR i) {
        unop(i.r1);
    }

    public void visit(Instr.SBC i) {
        binop(i.r1, i.r2);
    }

    public void visit(Instr.SBCI i) {
        unop(i.r1);
    }

    public void visit(Instr.SBI i) {
        writeIORegister(i.imm1, ISEValue.UNKNOWN);
        //getIOReg(i.imm1).writeBit(i.imm2, true);
    }

    public void visit(Instr.SBIC i) {
        // TODO: implement skips
    }

    public void visit(Instr.SBIS i) {
        // TODO: implement skips
    }

    public void visit(Instr.SBIW i) {
        readRegister(i.r1);
        readRegister(i.r1.nextRegister());
        setSREG(ISEValue.UNKNOWN);
        writeRegister(i.r1, ISEValue.UNKNOWN);
        writeRegister(i.r1.nextRegister(), ISEValue.UNKNOWN);
    }

    public void visit(Instr.SBR i) {
        unop(i.r1);
    }

    public void visit(Instr.SBRC i) {
        // TODO: implement skips
    }

    public void visit(Instr.SBRS i) {
        // TODO: implement skips
    }

    public void visit(Instr.SEC i) {
        setSREG(ISEValue.UNKNOWN);
        // C = true;
    }

    public void visit(Instr.SEH i) {
        setSREG(ISEValue.UNKNOWN);
        // H = true;
    }

    public void visit(Instr.SEI i) {
        setSREG(ISEValue.UNKNOWN);
        // enableInterrupts();
    }

    public void visit(Instr.SEN i) {
        setSREG(ISEValue.UNKNOWN);
        // N = true;
    }

    public void visit(Instr.SER i) {
        writeRegister(i.r1, ISEValue.UNKNOWN);
    }

    public void visit(Instr.SES i) {
        setSREG(ISEValue.UNKNOWN);
        // S = true;
    }

    public void visit(Instr.SET i) {
        setSREG(ISEValue.UNKNOWN);
        // T = true;
    }

    public void visit(Instr.SEV i) {
        setSREG(ISEValue.UNKNOWN);
        // V = true;
    }

    public void visit(Instr.SEZ i) {
        setSREG(ISEValue.UNKNOWN);
        // Z = true;
    }

    public void visit(Instr.SLEEP i) {
        // do nothing.
    }

    public void visit(Instr.SPM i) {
        readRegister(Register.R0);
        readRegister(Register.R1);
        readRegister(Register.Z);
        readRegister(Register.Z.nextRegister());
    }

    public void visit(Instr.ST i) {
        readRegister(i.r1);
        readRegister(i.r1.nextRegister());
        readRegister(i.r2);
    }

    public void visit(Instr.STD i) {
        readRegister(i.r1);
        readRegister(i.r1.nextRegister());
        readRegister(i.r2);
    }

    public void visit(Instr.STPD i) {
        readRegister(i.r1);
        readRegister(i.r1.nextRegister());
        readRegister(i.r2);
        writeRegister(i.r1, ISEValue.UNKNOWN);
        writeRegister(i.r1.nextRegister(), ISEValue.UNKNOWN);
    }

    public void visit(Instr.STPI i) {
        readRegister(i.r1);
        readRegister(i.r1.nextRegister());
        readRegister(i.r2);
        writeRegister(i.r1, ISEValue.UNKNOWN);
        writeRegister(i.r1.nextRegister(), ISEValue.UNKNOWN);
    }

    public void visit(Instr.STS i) {
        readRegister(i.r1);
    }

    public void visit(Instr.SUB i) {
        binop(i.r1, i.r2);
    }

    public void visit(Instr.SUBI i) {
        unop(i.r1);
    }

    public void visit(Instr.SWAP i) {
        unop(i.r1);
    }

    public void visit(Instr.TST i) {
        readRegister(i.r1);
        setSREG(ISEValue.UNKNOWN);
    }

    public void visit(Instr.WDR i) {
        // do nothing.
    }
}
