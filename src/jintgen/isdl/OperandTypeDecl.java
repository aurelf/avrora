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

package jintgen.isdl;

import jintgen.isdl.parser.Token;
import jintgen.jigir.CodeRegion;
import avrora.util.StringUtil;
import avrora.util.Util;

import java.util.List;
import java.util.LinkedList;

/**
 * The <code>OperandDecl</code> class represents the declaration of a set of values (or registers) that can
 * serve as an operand to a particular instruction. For example, an operand declaration might be the set of
 * all general purpose registers, or it might be the set of high general purpose registers, or the set of
 * address registers, etc.
 *
 * @author Ben L. Titzer
 */
public abstract class OperandTypeDecl extends Item {

    public final List<AddrModeDecl.Operand> subOperands;
    public CodeRegion readMethod;
    public CodeRegion writeMethod;

    protected OperandTypeDecl(Token n) {
        super(n);
        subOperands = new LinkedList<AddrModeDecl.Operand>();
    }

    /**
     * The <code>Simple</code> class represents an operand to an instruction that is
     * a value such as an immediate or an absolute address.
     */
    public static class Simple extends OperandTypeDecl {

        public final int low;
        public final int high;
        public final Token kind;
        public final int size;

        public Simple(Token n, Token b, Token k, Token l, Token h) {
            super(n);
            kind = k;
            size = StringUtil.evaluateIntegerLiteral(b.image);
            low = l == null ? -1 : StringUtil.evaluateIntegerLiteral(l.image);
            high = h == null ? -1 : StringUtil.evaluateIntegerLiteral(h.image);
        }

        public boolean isValue() {
            return true;
        }

        public void addSubOperand(AddrModeDecl.Operand o) {
            throw Util.failure("Suboperands are not allowed to Simple operands");
        }
    }

    /**
     * The <code>SymbolSet</code> class represents an operand to an instruction that
     * is a symbol from a particular set, such as the names of general purpose registers.
     */
    public static class SymbolSet extends OperandTypeDecl {
        public final SymbolMapping map;
        public final int size;

        public SymbolSet(Token n, Token b, SymbolMapping m) {
            super(n);
            size = StringUtil.evaluateIntegerLiteral(b.image);
            map = m;
        }

        public boolean isSymbol() {
            return true;
        }

        public void addSubOperand(AddrModeDecl.Operand o) {
            throw Util.failure("Suboperands are not allowed to SymbolSet operands");
        }
    }

    /**
     * The <code>Compound</code> class represents an operand declaration that consists of
     * multiple sub-operands.
     */
    public static class Compound extends OperandTypeDecl {

        public Compound(Token n) {
            super(n);
        }

        public boolean isCompound() {
            return true;
        }
    }

    /**
     * The <code>Union</code> class represents an operand type that is the union of multiple
     * operand types.
     */
    public static class Union extends OperandTypeDecl {
        public final List<OperandTypeDecl> types;

        public Union(Token n) {
            super(n);
            types = new LinkedList<OperandTypeDecl>();
        }

        public boolean isUnion() {
            return true;
        }

        public void addType(OperandTypeDecl d) {
            types.add(d);
        }
    }

    public CodeRegion getReadMethod() {
        return readMethod;
    }

    public CodeRegion getWriteMethod() {
        return writeMethod;
    }

    public boolean isCompound() {
        return false;
    }

    public boolean isSymbol() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public boolean isUnion() {
        return false;
    }

    public void addSubOperand(AddrModeDecl.Operand o) {
        subOperands.add(o);
    }
}
