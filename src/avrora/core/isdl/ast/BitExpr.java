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

/**
 * The <code>BitExpr</code> class represents an access of an individual
 * bit within a value. In the IR, individual bits of values can be
 * addressed for both reading and writing.
 *
 * @author Ben L. Titzer
 */
public class BitExpr extends Expr {

    /**
     * The <code>expr</code> field stores a reference to the expression whose
     * value the bit will be extracted from.
     */
    public final Expr expr;

    /**
     * The <code>bit</code> field stores a reference to an expression that
     * when evaluated indicates which bit to read.
     */
    public final Expr bit;

    /**
     * The constructor of the <code>BitExpr</code> class simply initializes
     * the references to the expression and the bit.
     * @param e the expression representing the value to extract the bit from
     * @param b the expression representing the number of the bit to extract
     */
    public BitExpr(Expr e, Expr b) {
        expr = e;
        bit = b;
    }

    public int getBitWidth() {
        return 1;
    }

    public boolean isConstantExpr() {
        return expr.isConstantExpr() && bit.isConstantExpr();
    }
}