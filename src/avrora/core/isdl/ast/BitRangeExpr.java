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

package avrora.core.isdl.ast;
import avrora.core.isdl.Token;

/**
 * The <code>BitRangeExpr</code> class represents an expression whose
 * value is the result of extracting a range of the bits from
 * another expression. In the IR, ranges of bits can be specified
 * for reading and writing, allowing cleaner expression of sub-byte
 * fields. The bounds of these ranges are static: the endpoints must
 * be constants.
 *
 * @author Ben L. Titzer
 */
public class BitRangeExpr extends Expr {
    /**
     * The <code>operand</code> field stores a reference to the expression
     * that is the operand of the bit range expression, i.e. the value
     * from which the range of bits will be extracted.
     */
    public final Expr operand;

    /**
     * The <code>low_bit</code> field represents the lowest bit in the range
     * to be extracted, inclusive.
     */
    public final int low_bit;

    /**
     * The <code>high_bit</code> field represents the highest bit in the range
     * to be extracted, inclusive.
     */
    public final int high_bit;

    /**
     * The constructor of the <code>BitRangeExpr</code> class simply initializes
     * the references to the operands of the bit range expression.
     * @param o a reference to the expression operand
     * @param l the lowest bit in the range, inclusive
     * @param h the highest bit in the range, inclusive
     */
    public BitRangeExpr(Expr o, Token l, Token h) {
        operand = o;
        int low = Expr.tokenToInt(l);
        int high = Expr.tokenToInt(h);

        low_bit = low < high ? low : high;
        high_bit = low > high ? low : high;
    }

    /**
     * The constructor of the <code>BitRangeExpr</code> class simply initializes
     * the references to the operands of the bit range expression.
     * @param o a reference to the expression operand
     * @param l the lowest bit in the range, inclusive
     * @param h the highest bit in the range, inclusive
     */
    public BitRangeExpr(Expr o, int l, int h) {
        operand = o;
        low_bit = l;
        high_bit = h;
    }

    public int getBitWidth() {
        int diff = (high_bit - low_bit);
        if ( diff < 0 ) diff = -diff;
        return diff + 1;
    }

    public boolean isConstantExpr() {
        return operand.isConstantExpr();
    }

    public boolean isBitRangeExpr() {
        return true;
    }

    /**
     * The <code>accept()</code> method implements one half of the visitor
     * pattern so that client visitors can traverse the syntax tree easily
     * and in an extensible way.
     * @param v the visitor to accept
     */
    public void accept(ExprVisitor v) {
        v.visit(this);
    }

    /**
     * The <code>accept()</code> method implements one half of the visitor
     * pattern so that client visitors can traverse the syntax tree easily
     * and in an extensible way.
     * @param v the visitor to accept
     */
    public void accept(CodeVisitor v) {
        v.visit(this);
    }

    public Expr accept(CodeRebuilder r) {
        return r.visit(this);
    }

    public String toString() {
        return innerString(operand) + "[" + low_bit + ":" + high_bit + "]";
    }

    public int getPrecedence() {
        return PREC_TERM;
    }
}
