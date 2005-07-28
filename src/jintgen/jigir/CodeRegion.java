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

package jintgen.jigir;

import jintgen.isdl.parser.Token;
import jintgen.isdl.OperandTypeDecl;

import java.util.Iterator;
import java.util.List;

/**
 * The <code>CodeRegion</code> class represents a piece of code that has external inputs. For example, a
 * subroutine is a piece of code that has a list of statements and a list of formal parameters. An instruction
 * declaration is a code region where the external inputs are the operands to the instruction.
 *
 * @author Ben L. Titzer
 */
public class CodeRegion {

    public static class Operand {
        public final Token name;
        public final Token type;
        protected OperandTypeDecl operandType;

        public Operand(Token n, Token t) {
            name = n;
            type = t;
        }

        public void setOperandType(OperandTypeDecl d) {
            operandType = d;
        }

        public boolean isRegister() {
            return operandType.isSymbol();
        }

        public boolean isImmediate() {
            return operandType.isValue();
        }

        public String getType() {
            if (operandType != null)
                return isRegister() ? "Register" : "int";
            else
                return type.image;
        }

        public OperandTypeDecl getOperandDecl() {
            return operandType;
        }
    }

    public final List operands;
    protected List stmts;

    public CodeRegion(List o, List s) {
        operands = o;
        stmts = s;
    }

    public int numOperands() {
        return operands.size();
    }

    public List getOperands() {
        return operands;
    }

    public Iterator getOperandIterator() {
        return operands.iterator();
    }

    public List getCode() {
        return stmts;
    }

    public void setCode(List s) {
        stmts = s;
    }

    public boolean hasBody() {
        return stmts != null;
    }

}
