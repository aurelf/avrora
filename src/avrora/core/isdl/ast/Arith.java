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
 * @author Ben L. Titzer
 */
public abstract class Arith extends Expr {

    public abstract static class BinOp extends Arith {
        public final String operation;
        public final Expr left;
        public final Expr right;

        public BinOp(Expr l, String o, Expr r) {
            left = l;
            right = r;
            operation = o;
        }
    }

    public abstract static class UnOp extends Arith {
        public final String operation;
        public final Expr operand;

        public UnOp(String op, Expr o) {
            operand = o;
            operation = op;
        }
    }

    public static class AddExpr extends BinOp {
        public AddExpr(Expr left, Expr right) {
            super(left, "+", right);
        }
    }

    public static class SubExpr extends BinOp {
        public SubExpr(Expr left, Expr right) {
            super(left, "-", right);
        }
    }

    public static class MulExpr extends BinOp {
        public MulExpr(Expr left, Expr right) {
            super(left, "*", right);
        }
    }

    public static class DivExpr extends BinOp {
        public DivExpr(Expr left, Expr right) {
            super(left, "/", right);
        }
    }

    public static class AndExpr extends BinOp {
        public AndExpr(Expr left, Expr right) {
            super(left, "&", right);
        }
    }

    public static class OrExpr extends BinOp {
        public OrExpr(Expr left, Expr right) {
            super(left, "|", right);
        }
    }

    public static class XorExpr extends BinOp {
        public XorExpr(Expr left, Expr right) {
            super(left, "^", right);
        }
    }

    public static class ShiftLeftExpr extends BinOp {
        public ShiftLeftExpr(Expr left, Expr right) {
            super(left, "<<", right);
        }
    }

    public static class ShiftRightExpr extends BinOp {
        public ShiftRightExpr(Expr left, Expr right) {
            super(left, ">>", right);
        }
    }

    public static class CompExpr extends UnOp {
        public CompExpr(Expr l) {
            super("~", l);
        }
    }

    public static class NegExpr extends UnOp {
        public NegExpr(Expr l) {
            super("-", l);
        }
    }
}
