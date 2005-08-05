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
 */

package jintgen.arch.avr;

import avrora.syntax.AVRErrorReporter;

/**
 * @author Ben L. Titzer
 */
public class AVRInstrBuilder {
    private final AVRErrorReporter ERROR;

    public AVRInstrBuilder(AVRErrorReporter er) {
        ERROR = er;
    }

    //===============================================================
    // Instruction factory methods: one newXXX() method per instruction
    //===============================================================

    public AVRInstr.ADC newADC(AVRSymbol rd, AVRSymbol rr) {
        // parameter types will either be int, AVRSymbol, or AVROperand
        return new AVRInstr.ADC(checkGPR(rd), checkGPR(rr));
    }

    public AVRInstr.LDI newLDI(AVRSymbol rd, int imm) {
        return new AVRInstr.LDI(checkHGPR(rd), checkIMM8(imm));
    }

    //===============================================================
    // Operand types: one checkXXX method per base operand type
    //                and one checkXXX method per operand union type
    //===============================================================

    protected AVROperand.GPR checkGPR(AVRSymbol e) {
        // this method has to know the representation type and valid set
        return null;
    }

    protected AVROperand.HGPR checkHGPR(AVRSymbol e) {
        // this method has to know the representation type and valid set
        return null;
    }

    protected AVROperand.IMM8 checkIMM8(int imm) {
        // this method has to know the representation type and the valid range
        return null;
    }

}
