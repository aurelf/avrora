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
 * The <code>Logical</code> class is a container for classes that represent
 * expressions that produce booleans in the IR. For example, the
 * class <code>Logical.AndExpr</code> represents an expression that is the
 * logical AND of two boolean values. The result
 * of all operations on integers are boolean; therefore, every expression
 * that is a subclass of <code>Logical</code> has a result type of boolean.
 *
 * @author Ben L. Titzer
 */
public abstract class Logical {

    /**
     * The <code>BinOp</code> inner class represents an operation on
     * two values with an infix binary operation that produces a boolean.
     * For example, logical XOR and integer comparison and such operations
     * are binary infix and therefore subclasses of this class.
     */
    public abstract static class BinOp extends Arith {

        /**
         * The <code>operation</code> field stores the string name of the
         * operation of this binary operation. For example, 'and' represents
         * logical AND.
         */
        public final String operation;

        /**
         * The <code>left</code> field stores a reference to the expression
         * that is the left operand of the binary operation.
         */
        public final Expr left;

        /**
         * The <code>left</code> field stores a reference to the expression
         * that is the right operand of the binary operation.
         */
        public final Expr right;

        /**
         * The constructor of the <code>BinOp</code> class initializes the
         * public final fields that form the structure of this expression.
         * @param l the left expression operand
         * @param o the string name of the operation
         * @param r the right expression operand
         */
        public BinOp(Expr l, String o, Expr r) {
            left = l;
            right = r;
            operation = o;
        }

        public boolean isConstantExpr() {
            return left.isConstantExpr() && right.isConstantExpr();
        }

    }

    /**
     * The <code>UnOp</code> inner class represents an operation on a
     * single boolean value. For example, the logical negation is an operation
     * on a single boolean that produce a single boolean result.
     */
    public abstract static class UnOp extends Arith {
        /**
         * The <code>operation</code> field stores the string name of the
         * operation being performed on the expression. For example, '!'
         * represents logical negation.
         */
        public final String operation;

        /**
         * The <code>operand</code> field stores a reference to the
         * expression operand of this operation.
         */
        public final Expr operand;

        /**
         * The constructor of the <code>UnOp</code> class initializes the
         * public final fields that form the structure of this expression.
         * @param op the string name of the operation
         * @param o the operand of this operation
         */
        public UnOp(String op, Expr o) {
            operand = o;
            operation = op;
        }

        public boolean isConstantExpr() {
            return operand.isConstantExpr();
        }
    }

    /**
     * The <code>AndExpr</code> inner class represents the logical AND
     * of two boolean values that produces a new boolean value.
     */
    public static class AndExpr extends BinOp {
        public AndExpr(Expr left, Expr right) {
            super(left, "and", right);
        }
    }

    /**
     * The <code>OrExpr</code> inner class represents the logical OR
     * of two boolean values that produces a new boolean value.
     */
    public static class OrExpr extends BinOp {
        public OrExpr(Expr left, Expr right) {
            super(left, "or", right);
        }
    }

    /**
     * The <code>XorExpr</code> inner class represents the logical XOR
     * of two boolean values that produces a new boolean value.
     */
    public static class XorExpr extends BinOp {
        public XorExpr(Expr left, Expr right) {
            super(left, "xor", right);
        }
    }

    /**
     * The <code>EquExpr</code> inner class represents a comparison
     * for equality of two integer values that produces a single boolean
     * value.
     */
    public static class EquExpr extends BinOp {
        public EquExpr(Expr left, Expr right) {
            super(left, "==", right);
        }
    }

    /**
     * The <code>NequExpr</code> inner class represents a comparison
     * for inequality of two integer values that produces a single boolean
     * value.
     */
    public static class NequExpr extends BinOp {
        public NequExpr(Expr left, Expr right) {
            super(left, "!=", right);
        }
    }

    /**
     * The <code>LessExpr</code> inner class represents a comparison
     * of two integer values that produces a single boolean
     * value that is true if and only if the first operand is less than
     * the second operand.
     */
    public static class LessExpr extends BinOp {
        public LessExpr(Expr left, Expr right) {
            super(left, "<", right);
        }
    }

    /**
     * The <code>LessEquExpr</code> inner class represents a comparison
     * of two integer values that produces a single boolean
     * value that is true if and only if the first operand is less than
     * or equal to the second operand.
     */
    public static class LessEquExpr extends BinOp {
        public LessEquExpr(Expr left, Expr right) {
            super(left, "<=", right);
        }
    }

    /**
     * The <code>GreaterExpr</code> inner class represents a comparison
     * of two integer values that produces a single boolean
     * value that is true if and only if the first operand is greater than
     * the second operand.
     */
    public static class GreaterExpr extends BinOp {
        public GreaterExpr(Expr left, Expr right) {
            super(left, ">", right);
        }
    }

    /**
     * The <code>GreaterEquExpr</code> inner class represents a comparison
     * of two integer values that produces a single boolean
     * value that is true if and only if the first operand is greater than
     * or equal to the second operand.
     */
    public static class GreaterEquExpr extends BinOp {
        public GreaterEquExpr(Expr left, Expr right) {
            super(left, ">=", right);
        }
    }

    /**
     * The <code>NotExpr</code> inner class represents the logical negation
     * of a single boolean value that produces a new integer value.
     */
    public static class NotExpr extends UnOp {
        public NotExpr(Expr l) {
            super("!", l);
        }
    }
}
