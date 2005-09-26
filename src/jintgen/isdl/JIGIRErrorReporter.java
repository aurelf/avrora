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

import cck.text.StringUtil;
import cck.util.Util;
import cck.parser.ErrorReporter;
import jintgen.isdl.parser.Token;
import jintgen.jigir.*;

/**
 * @author Ben L. Titzer
 */
public class JIGIRErrorReporter extends ErrorReporter {

    public void ExtraOperandInAddressingModeUnification(Token addrSet, Token addrMode, Token operand) {
        String report = "Cannot unify addressing mode " + StringUtil.quote(addrMode) + " into set " + StringUtil.quote(addrSet) + " at " + pos(addrMode) + " because it defines an operand " + StringUtil.quote(operand) + " not in the other addressing modes";
        error("ExtraOperandInAddressingModeUnification", addrMode.asSourcePoint(), report);
    }

    public void MissingOperandInAddressingModeUnification(Token addrSet, Token addrMode, String operand) {
        String report = "Cannot unify addressing mode " + StringUtil.quote(addrMode) + " into set " + StringUtil.quote(addrSet) + " at " + pos(addrMode) + " because it does not define an operand " + StringUtil.quote(operand) + " present in the other addressing modes";
        error("MissingOperandInAddressingModeUnification", addrMode.asSourcePoint(), report);
    }

    public void UnresolvedOperandType(Token t) {
        unresolved("OperandType", "operand type", t);
    }

    public void UnresolvedEnum(Token t) {
        unresolved("Enum", "enumeration", t);
    }

    public void UnresolvedEncodingFormat(Token t) {
        unresolved("EncodingFormat", "encoding format", t);
    }

    public void UnresolvedType(Token t) {
        unresolved("Type", "type", t);
    }

    public void UnresolvedAddressingMode(Token t) {
        unresolved("AddressingMode", "addressing mode", t);
    }

    public void UnresolvedVariable(Token t) {
        unresolved("Variable", "variable", t);
    }

    public void RedefinedInstruction(Token prevdecl, Token newdecl) {
        redefined("Instruction", "Instruction", prevdecl, newdecl);
    }

    public void RedefinedEncoding(Token prevdecl, Token newdecl) {
        redefined("Encoding", "Encoding format", prevdecl, newdecl);
    }

    public void RedefinedEnum(Token prevdecl, Token newdecl) {
        redefined("Enum", "Enumeration", prevdecl, newdecl);
    }

    public void RedefinedAddressingMode(Token prevdecl, Token newdecl) {
        redefined("AddressingMode", "Addressing mode", prevdecl, newdecl);
    }

    public void RedefinedAddressingModeSet(Token prevdecl, Token newdecl) {
        redefined("AddressingModeSet", "Addressing mode set", prevdecl, newdecl);
    }

    public void RedefinedLocal(Token prevdecl, Token newdecl) {
        redefined("Local", "Local variable", prevdecl, newdecl);
    }

    public void RedefinedOperand(Token prevdecl, Token newdecl) {
        redefined("Operand", "Operand", prevdecl, newdecl);
    }

    public void RedefinedOperandType(Token prevdecl, Token newdecl) {
        redefined("OperandType", "Operand Type", prevdecl, newdecl);
    }

    public void RedefinedSubroutine(Token prevdecl, Token newdecl) {
        redefined("Subroutine", "Subroutine", prevdecl, newdecl);
    }

    public void RedefinedSymbol(Token prevdecl, Token newdecl) {
        redefined("Symbol", "Symbol", prevdecl, newdecl);
    }

    public void CannotComputeSizeOfVariable(Token t) {
        String report = "Cannot compute size of variable "+StringUtil.quote(t.image);
        error("CannotComputeSizeOfVariable", t.asSourcePoint(), report);
    }

    public void CannotComputeSizeOfExpression(Expr e) {
        String report = "Cannot compute size of expression";
        error("CannotComputeSizeOfExpression", e.getSourcePoint(), report);
    }

    public void CannotComputeSizeOfLiteral(Literal l) {
        String report = "Cannot compute size of literal "+StringUtil.quote(l.token);
        error("CannotComputeSizeOfLiteral", l.getSourcePoint(), report);
    }

    public void TypeMismatch(String what, Type exp, Expr e) {
        String report = "Type mismatch in " + what + ": expected " + exp + ", found " + e.getType();
        error("TypeMismatch", e.getSourcePoint(), report);
    }

    public void TypesCannotBeCompared(Type exp, Type got) {
        String report = "Types cannot be compared: " + exp + " and " + got;
        throw Util.failure(report);
    }

    public void IntTypeExpected(String what, Expr e) {
        String report = "Integer type expected in " + what + ", found " + e.getType();
        error("IntTypeExpected", e.getSourcePoint(), report);
    }

    void redefined(String type, String thing, Token prevdecl, Token newdecl) {
        type = "Redefined"+type;
        String report = thing + " " + StringUtil.quote(prevdecl.image) + " previously defined at " + pos(prevdecl);
        error(type, newdecl.asSourcePoint(), report, prevdecl.image);
    }

    void unresolved(String type, String thing, Token where) {
        type = "Unresolved"+type;
        String report = "Unresolved " + thing + " " + StringUtil.quote(where.image);
        error(type, where.asSourcePoint(), report, where.image);
    }

    private String pos(Token t) {
        return t.beginLine+":"+t.beginColumn;
    }

}
