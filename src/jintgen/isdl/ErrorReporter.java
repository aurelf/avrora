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

package jintgen.isdl;

import avrora.util.StringUtil;
import avrora.util.Util;
import jintgen.isdl.parser.Token;
import jintgen.jigir.Expr;
import jintgen.jigir.Literal;

/**
 * @author Ben L. Titzer
 */
public class ErrorReporter {

    public void ExtraOperandInAddressingModeUnification(Token addrSet, Token addrMode, Token operand) {
        throw Util.failure("Cannot unify addressing mode "+StringUtil.quote(addrMode)+" into set "+StringUtil.quote(addrSet)+ " at "+pos(addrMode)
        +" because it defines an operand "+StringUtil.quote(operand)+" not in the other addressing modes");
    }

    public void MissingOperandInAddressingModeUnification(Token addrSet, Token addrMode, String operand) {
        throw Util.failure("Cannot unify addressing mode "+StringUtil.quote(addrMode)+" into set "+StringUtil.quote(addrSet)+ " at "+pos(addrMode)
        +" because it does not define an operand "+StringUtil.quote(operand)+" present in the other addressing modes");
    }

    public void UnresolvedOperandType(Token t) {
        unresolved("operand type", t);
    }

    public void UnresolvedEnum(Token t) {
        unresolved("enumeration", t);
    }

    public void UnresolvedEncodingFormat(Token t) {
        unresolved("encoding format", t);
    }

    public void UnresolvedType(Token t) {
        unresolved("type", t);
    }

    public void UnresolvedAddressingMode(Token t) {
        unresolved("addressing mode", t);
    }

    public void RedefinedInstruction(Token t) {
        redefined("Instruction", t);
    }

    public void RedefinedEncoding(Token t) {
        redefined("Encoding format", t);
    }

    public void RedefinedEnum(Token t) {
        redefined("Enumeration", t);
    }

    public void RedefinedAddressingMode(Token t) {
        redefined("Addressing mode", t);
    }

    public void RedefinedAddressingModeSet(Token t) {
        redefined("Addressing mode set", t);
    }

    public void RedefinedLocal(Token t) {
        redefined("Local variable", t);
    }

    public void RedefinedOperand(Token t) {
        redefined("Operand", t);
    }

    public void RedefinedOperandType(Token t) {
        redefined("Operand Type", t);
    }

    public void RedefinedSubroutine(Token t) {
        redefined("Subroutine", t);
    }

    public void RedefinedSymbol(Token t) {
        redefined("Symbol", t);
    }

    public void RedefinedValue(Token t) {
        redefined("Simple", t);
    }

    public void CannotComputeSizeOfVariable(Token t) {
        throw Util.failure("Cannot compute size of variable "+StringUtil.quote(t)+" at "+pos(t));
    }

    public void CannotComputeSizeOfExpression(Expr e) {
        throw Util.failure("Cannot compute size of expression "+StringUtil.quote(e));
    }

    public void CannotComputeSizeOfLiteral(Literal l) {
        throw Util.failure("Cannot compute size of literal "+StringUtil.quote(l)+" at "+pos(l.token));
    }

    void redefined(String thing, Token where) {
        throw Util.failure(thing+" "+StringUtil.quote(where.image)+" previously defined at "+pos(where));
    }

    void unresolved(String thing, Token where) {
        throw Util.failure("Unresolved "+thing+" "+StringUtil.quote(where.image)+" at "+pos(where));
    }



    private String pos(Token t) {
        return t.beginLine+":"+t.beginColumn;
    }
}