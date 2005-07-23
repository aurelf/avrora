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

package avrora.syntax;

import avrora.core.Register;
import avrora.util.ErrorReporter;
import avrora.util.StringUtil;

/**
 * The <code>AVRErrorReporter</code> contains one method per compilation error. The method constructs a
 * <code>SimplifierError</code> object that represents the error and throws it. One method per type of error
 * provides a convenient interface and allows pinpointing the generation of each type of error within the
 * verifier.
 *
 * @author Ben L. Titzer
 */
public class AVRErrorReporter extends ErrorReporter {

    private ProgramPoint point(AbstractToken t) {
        return new ProgramPoint(t.file, t.beginLine, t.beginColumn, t.endColumn);
    }

    private ProgramPoint point(ASTNode n) {
        AbstractToken l = n.getLeftMostToken();
        AbstractToken r = n.getRightMostToken();
        return new ProgramPoint(l.file, l.beginLine, l.beginColumn, r.endColumn);
    }

    public void UnknownRegister(AbstractToken reg) {
        String report = "unknown register " + StringUtil.quote(reg);
        error(report, "UnknownRegister", point(reg));
    }

    public void InstructionCannotBeInSegment(String seg, AbstractToken instr) {
        String report = "instructions cannot be declared in " + seg + " cseg";
        error(report, "InstructionCannotBeInSegment", point(instr));
    }

    public void UnknownInstruction(AbstractToken instr) {
        String report = "unknown instruction " + StringUtil.quote(instr);
        error(report, "UnknownInstruction", point(instr));
    }

    public void RegisterExpected(SyntacticOperand o) {
        String report = "register expected";
        error(report, "RegisterExpected", point(o));
    }

    public void IncorrectRegister(SyntacticOperand o, Register reg, String expect) {
        String report = "incorrected register " + StringUtil.quote(reg) + ", expected one of " + expect;
        error(report, "IncorrectRegister", point(o));
    }

    public void ConstantExpected(SyntacticOperand o) {
        String report = "constant expected";
        error(report, "ConstantExpected", point(o));
    }

    public void ConstantOutOfRange(SyntacticOperand o, int value, String range) {
        String report = "constant " + StringUtil.quote("" + value) + " out of expected range " + range;
        error(report, "ConstantOutOfRange", "" + value, point(o));
    }

    public void WrongNumberOfOperands(AbstractToken instr, int seen, int expected) {
        String report = "wrong number of operands to instruction " + StringUtil.quote(instr) + ", expected "
                + expected + " and found " + seen;
        error(report, "WrongNumberOfOperands", point(instr));
    }

    public void UnknownVariable(AbstractToken name) {
        String report = "unknown variable or label " + StringUtil.quote(name.image);
        error(report, "UnknownVariable", name.image, point(name));
    }

    public void DataCannotBeInSegment(String seg, ASTNode loc) {
        String report = "initialized data cannot be in " + seg + " segment";
        error(report, "DataCannotBeInSegment", seg, point(loc));
    }

    public void IncludeFileNotFound(AbstractToken tok) {
        String report = "include file not found " + tok;
        error(report, "IncludeFileNotFound", point(tok));
    }
}
