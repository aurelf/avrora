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

package avrora.core;

/**
 * @author Ben L. Titzer
 */
public interface InstrVisitor {
//--BEGIN INSTRVISITOR GENERATOR--
    public void visit(Instr.ADC i); // add register to register with carry
    public void visit(Instr.ADD i); // add register to register
    public void visit(Instr.ADIW i); // add immediate to word register
    public void visit(Instr.AND i); // and register with register
    public void visit(Instr.ANDI i); // and register with immediate
    public void visit(Instr.ASR i); // arithmetic shift right
    public void visit(Instr.BCLR i); // clear bit in status register
    public void visit(Instr.BLD i); // load bit from T flag into register
    public void visit(Instr.BRBC i); // branch if bit in status register is clear
    public void visit(Instr.BRBS i); // branch if bit in status register is set
    public void visit(Instr.BRCC i); // branch if carry flag is clear
    public void visit(Instr.BRCS i); // branch if carry flag is set
    public void visit(Instr.BREAK i); // break
    public void visit(Instr.BREQ i); // branch if equal
    public void visit(Instr.BRGE i); // branch if greater or equal (signed)
    public void visit(Instr.BRHC i); // branch if H flag is clear
    public void visit(Instr.BRHS i); // branch if H flag is set
    public void visit(Instr.BRID i); // branch if interrupts are disabled
    public void visit(Instr.BRIE i); // branch if interrupts are enabled
    public void visit(Instr.BRLO i); // branch if lower
    public void visit(Instr.BRLT i); // branch if less than zero (signed)
    public void visit(Instr.BRMI i); // branch if minus
    public void visit(Instr.BRNE i); // branch if not equal
    public void visit(Instr.BRPL i); // branch if positive
    public void visit(Instr.BRSH i); // branch if same or higher
    public void visit(Instr.BRTC i); // branch if T flag is clear
    public void visit(Instr.BRTS i); // branch if T flag is set
    public void visit(Instr.BRVC i); // branch if V flag is clear
    public void visit(Instr.BRVS i); // branch if V flag is set
    public void visit(Instr.BSET i); // set flag in status register
    public void visit(Instr.BST i); // store bit in register into T flag
    public void visit(Instr.CALL i); // call absolute address
    public void visit(Instr.CBI i); // clear bit in IO register
    public void visit(Instr.CBR i); // clear bits in register
    public void visit(Instr.CLC i); // clear C flag
    public void visit(Instr.CLH i); // clear H flag
    public void visit(Instr.CLI i); // clear I flag
    public void visit(Instr.CLN i); // clear N flag
    public void visit(Instr.CLR i); // clear register (set to zero)
    public void visit(Instr.CLS i); // clear S flag
    public void visit(Instr.CLT i); // clear T flag
    public void visit(Instr.CLV i); // clear V flag
    public void visit(Instr.CLZ i); // clear Z flag
    public void visit(Instr.COM i); // one's compliment register
    public void visit(Instr.CP i); // compare registers
    public void visit(Instr.CPC i); // compare registers with carry
    public void visit(Instr.CPI i); // compare register with immediate
    public void visit(Instr.CPSE i); // compare registers and skip if equal
    public void visit(Instr.DEC i); // decrement register by one
    public void visit(Instr.EICALL i); // extended indirect call
    public void visit(Instr.EIJMP i); // extended indirect jump
    public void visit(Instr.ELPM i); // extended load program memory to r0
    public void visit(Instr.ELPMD i); // extended load program memory to register
    public void visit(Instr.ELPMPI i); // extended load program memory to register and post-increment
    public void visit(Instr.EOR i); // exclusive or register with register
    public void visit(Instr.FMUL i); // fractional multiply register with register to r0
    public void visit(Instr.FMULS i); // signed fractional multiply register with register to r0
    public void visit(Instr.FMULSU i); // signed/unsigned fractional multiply register with register to r0
    public void visit(Instr.ICALL i); // indirect call through Z register
    public void visit(Instr.IJMP i); // indirect jump through Z register
    public void visit(Instr.IN i); // read from IO register into register
    public void visit(Instr.INC i); // increment register by one
    public void visit(Instr.JMP i); // absolute jump
    public void visit(Instr.LD i); // load from SRAM
    public void visit(Instr.LDD i); // load from SRAM with displacement
    public void visit(Instr.LDI i); // load immediate into register
    public void visit(Instr.LDPD i); // load from SRAM with pre-decrement
    public void visit(Instr.LDPI i); // load from SRAM with post-increment
    public void visit(Instr.LDS i); // load direct from SRAM
    public void visit(Instr.LPM i); // load program memory into r0
    public void visit(Instr.LPMD i); // load program memory into register
    public void visit(Instr.LPMPI i); // load program memory into register and post-increment
    public void visit(Instr.LSL i); // logical shift left
    public void visit(Instr.LSR i); // logical shift right
    public void visit(Instr.MOV i); // copy register to register
    public void visit(Instr.MOVW i); // copy two registers to two registers
    public void visit(Instr.MUL i); // multiply register with register to r0
    public void visit(Instr.MULS i); // signed multiply register with register to r0
    public void visit(Instr.MULSU i); // signed/unsigned multiply register with register to r0
    public void visit(Instr.NEG i); // two's complement register
    public void visit(Instr.NOP i); // do nothing operation
    public void visit(Instr.OR i); // or register with register
    public void visit(Instr.ORI i); // or register with immediate
    public void visit(Instr.OUT i); // write from register to IO register
    public void visit(Instr.POP i); // pop from the stack to register
    public void visit(Instr.PUSH i); // push register to the stack
    public void visit(Instr.RCALL i); // relative call
    public void visit(Instr.RET i); // return to caller
    public void visit(Instr.RETI i); // return from interrupt
    public void visit(Instr.RJMP i); // relative jump
    public void visit(Instr.ROL i); // rotate left through carry flag
    public void visit(Instr.ROR i); // rotate right through carry flag
    public void visit(Instr.SBC i); // subtract register from register with carry
    public void visit(Instr.SBCI i); // subtract immediate from register with carry
    public void visit(Instr.SBI i); // set bit in IO register
    public void visit(Instr.SBIC i); // skip if bit in IO register is clear
    public void visit(Instr.SBIS i); // skip if bit in IO register is set
    public void visit(Instr.SBIW i); // subtract immediate from word 
    public void visit(Instr.SBR i); // set bits in register
    public void visit(Instr.SBRC i); // skip if bit in register cleared
    public void visit(Instr.SBRS i); // skip if bit in register set
    public void visit(Instr.SEC i); // set C (carry) flag
    public void visit(Instr.SEH i); // set H (half carry) flag
    public void visit(Instr.SEI i); // set I (interrupt enable) flag
    public void visit(Instr.SEN i); // set N (negative) flag
    public void visit(Instr.SER i); // set bits in register
    public void visit(Instr.SES i); // set S (signed) flag
    public void visit(Instr.SET i); // set T flag
    public void visit(Instr.SEV i); // set V (overflow) flag
    public void visit(Instr.SEZ i); // set Z (zero) flag
    public void visit(Instr.SLEEP i); // enter sleep mode
    public void visit(Instr.SPM i); // store to program memory from r0
    public void visit(Instr.ST i); // store from register to SRAM
    public void visit(Instr.STD i); // store from register to SRAM with displacement
    public void visit(Instr.STPD i); // store from register to SRAM with pre-decrement
    public void visit(Instr.STPI i); // store from register to SRAM with post-increment
    public void visit(Instr.STS i); // store direct to SRAM
    public void visit(Instr.SUB i); // subtract register from register
    public void visit(Instr.SUBI i); // subtract immediate from register
    public void visit(Instr.SWAP i); // swap nibbles in register
    public void visit(Instr.TST i); // test for zero or minus
    public void visit(Instr.WDR i); // watchdog timer reset
//--END INSTRVISITOR GENERATOR--
}
