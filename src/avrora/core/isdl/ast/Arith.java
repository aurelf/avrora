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
 * The <code>Arith</code> class is a container for classes that represent
 * integer arithmetic in the IR. For example, the class <code>Arith.AddExpr</code>
 * represents an expression that is the addition of two integers. The result
 * of all operations on integers are integers, therefore, every expression
 * that is a subclass of <code>Arith</code> has a result type of integer.
 *
 * @author Ben L. Titzer
 */
public abstract class Arith extends Expr {

    /**
     * The <code>BinOp</code> inner class represents an operation on
     * two integers with an infix binary operation. For example, addition,
     * multiplication, bitwise and, and such operations are binary infix
     * and therefore subclasses of this class.
     */
    public abstract static class BinOp extends Arith {
        /**
         * The <code>operation</code> field stores the string name of the
         * operation of this binary operation. For example, '+' represents
         * addition.
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

        /**
         * The <code>accept()</code> method implements one half of the visitor
         * pattern so that client visitors can traverse the syntax tree easily
         * and in an extensible way.
         * @param v the visitor to accept
         */
        public void accept(ExprVisitor v) {
            v.visit(this);
        }
    }

    /**
     * The <code>UnOp</code> inner class represents an operation on a
     * single integer value. For example, the bitwise complement and the
     * negation of an integer are operations on a single integer that
     * produce a single integer result.
     */
    public abstract static class UnOp extends Arith {
        /**
         * The <code>operation</code> field stores the string name of the
         * operation being performed on the expression. For example, '~'
         * represents bitwise negation.
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

        /**
         * The <code>accept()</code> method implements one half of the visitor
         * pattern so that client visitors can traverse the syntax tree easily
         * and in an extensible way.
         * @param v the visitor to accept
         */
        public void accept(ExprVisitor v) {
            v.visit(this);
        }
    }

    /**
     * The <code>AddExpr</code> inner class represents the addition
     * of two integer values that produces a new integer value.
     */
    public static class AddExpr extends BinOp {
        public AddExpr(Expr left, Expr right) {
            super(left, "+", right);
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
    }

    /**
     * The <code>SubExpr</code> inner class represents the subtraction
     * of one integer value from another that results in a new integer value.
     */
    public static class SubExpr extends BinOp {
        public SubExpr(Expr left, Expr right) {
            super(left, "-", right);
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
    }

    /**
     * The <code>MulExpr</code> inner class represents the multiplication
     * of two integer values which produces a single integer result.
     */
    public static class MulExpr extends BinOp {
        public MulExpr(Expr left, Expr right) {
            super(left, "*", right);
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
    }

    /**
     * The <code>DivExpr</code> inner class represents a division operation
     * on two integer values which produces a single integer result.
     */
    public static class DivExpr extends BinOp {
        public DivExpr(Expr left, Expr right) {
            super(left, "/", right);
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
    }

    /**
     * The <code>AndExpr</code> class represents the bitwise and of two
     * integer values that produces a single integer result.
     */
    public static class AndExpr extends BinOp {
        public AndExpr(Expr left, Expr right) {
            super(left, "&", right);
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
    }

    /**
     * The <code>OrExpr</code> class represents the bitwise inclusive or of two
     * integer values that produces a single integer result.
     */
    public static class OrExpr extends BinOp {
        public OrExpr(Expr left, Expr right) {
            super(left, "|", right);
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
    }

    /**
     * The <code>XorExpr</code> class represents the bitwise exclusive or of two
     * integer values that produces a single integer result.
     */
    public static class XorExpr extends BinOp {
        public XorExpr(Expr left, Expr right) {
            super(left, "^", right);
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
    }

    /**
     * The <code>ShiftLeftExpr</code> class represents the shift left of an
     * integer value that produces a single integer result.
     */
    public static class ShiftLeftExpr extends BinOp {
        public ShiftLeftExpr(Expr left, Expr right) {
            super(left, "<<", right);
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
    }

    /**
     * The <code>ShiftRightExpr</code> class represents the shift left of an
     * integer value that produces a single integer result.
     */
    public static class ShiftRightExpr extends BinOp {
        public ShiftRightExpr(Expr left, Expr right) {
            super(left, ">>", right);
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
    }

    /**
     * The <code>CompExpr</code> class represents the bitwise complement of
     * an integer value that produces a single integer result.
     */
    public static class CompExpr extends UnOp {
        public CompExpr(Expr l) {
            super("~", l);
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
    }

    /**
     * The <code>NegExpr</code> class represents the negation (sign reversal)
     * of an integer value that produces a single integer result.
     */
    public static class NegExpr extends UnOp {
        public NegExpr(Expr l) {
            super("-", l);
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
    }
}
