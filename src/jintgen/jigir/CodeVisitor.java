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

import java.util.Iterator;

/**
 * The <code>CodeVisitor</code> interface represents a visitor that is more specific than the
 * <code>ExprVisitor</code> visitor, in that it contains visit methods for every type of arithmetic and
 * logical operation in the IR.
 *
 * @author Ben L. Titzer
 */
public interface CodeVisitor {

    public void visit(Arith.AddExpr e);

    public void visit(Arith.AndExpr e);

    public void visit(Arith.CompExpr e);

    public void visit(Arith.DivExpr e);

    public void visit(Arith.MulExpr e);

    public void visit(Arith.NegExpr e);

    public void visit(Arith.OrExpr e);

    public void visit(Arith.ShiftLeftExpr e);

    public void visit(Arith.ShiftRightExpr e);

    public void visit(Arith.SubExpr e);

    public void visit(Arith.XorExpr e);

    public void visit(BitExpr e);

    public void visit(BitRangeExpr e);

    public void visit(CallExpr e);

    public void visit(ConversionExpr e);

    public void visit(Literal.BoolExpr e);

    public void visit(Literal.IntExpr e);

    public void visit(Logical.AndExpr e);

    public void visit(Logical.EquExpr e);

    public void visit(Logical.GreaterEquExpr e);

    public void visit(Logical.GreaterExpr e);

    public void visit(Logical.LessEquExpr e);

    public void visit(Logical.LessExpr e);

    public void visit(Logical.NequExpr e);

    public void visit(Logical.NotExpr e);

    public void visit(Logical.OrExpr e);

    public void visit(Logical.XorExpr e);

    public void visit(MapExpr e);

    public void visit(VarExpr e);


    /**
     * The <code>DepthFirst</code> class is a base implementation of the <code>CodeVisitor</code> interface
     * that visits the tree in depth-first order.
     *
     * @author Ben L. Titzer
     */
    public class DepthFirst implements CodeVisitor {

        public void visit(Arith.AddExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Arith.AndExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Arith.CompExpr e) {
            e.operand.accept(this);
        }

        public void visit(Arith.DivExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Arith.MulExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Arith.NegExpr e) {
            e.operand.accept(this);
        }

        public void visit(Arith.OrExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Arith.ShiftLeftExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Arith.ShiftRightExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Arith.SubExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Arith.XorExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }


        public void visit(BitExpr e) {
            e.expr.accept(this);
            e.bit.accept(this);
        }

        public void visit(BitRangeExpr e) {
            e.operand.accept(this);
        }

        public void visit(CallExpr e) {
            Iterator i = e.args.iterator();
            while (i.hasNext()) {
                Expr a = (Expr)i.next();
                a.accept(this);
            }
        }

        public void visit(ConversionExpr e) {
            e.expr.accept(this);
        }

        public void visit(Literal.BoolExpr e) {
            // terminal node in the tree
        }

        public void visit(Literal.IntExpr e) {
            // terminal node in the tree
        }

        public void visit(Logical.AndExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Logical.EquExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Logical.GreaterEquExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Logical.GreaterExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Logical.LessEquExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Logical.LessExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Logical.NequExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Logical.NotExpr e) {
            e.operand.accept(this);
        }

        public void visit(Logical.OrExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Logical.XorExpr e) {
            e.left.accept(this);
            e.right.accept(this);
        }


        public void visit(MapExpr e) {
            e.index.accept(this);
        }

        public void visit(VarExpr e) {
            // terminal node in the tree
        }

    }

    /**
     * The <code>DepthFirst</code> class is a base implementation of the <code>CodeVisitor</code> interface
     * that visits the tree in depth-first order.
     *
     * @author Ben L. Titzer
     */
    public abstract class Default implements CodeVisitor {

        public abstract void error(Expr e);

        public void visit(Arith.AddExpr e) {
            error(e);
        }

        public void visit(Arith.AndExpr e) {
            error(e);
        }

        public void visit(Arith.CompExpr e) {
            error(e);
        }

        public void visit(Arith.DivExpr e) {
            error(e);
        }

        public void visit(Arith.MulExpr e) {
            error(e);
        }

        public void visit(Arith.NegExpr e) {
            error(e);
        }

        public void visit(Arith.OrExpr e) {
            error(e);
        }

        public void visit(Arith.ShiftLeftExpr e) {
            error(e);
        }

        public void visit(Arith.ShiftRightExpr e) {
            error(e);
        }

        public void visit(Arith.SubExpr e) {
            error(e);
        }

        public void visit(Arith.XorExpr e) {
            error(e);
        }


        public void visit(BitExpr e) {
            error(e);
        }

        public void visit(BitRangeExpr e) {
            error(e);
        }

        public void visit(CallExpr e) {
            error(e);
        }

        public void visit(ConversionExpr e) {
            error(e);
        }

        public void visit(Literal.BoolExpr e) {
            error(e);
        }

        public void visit(Literal.IntExpr e) {
            error(e);
        }

        public void visit(Logical.AndExpr e) {
            error(e);
        }

        public void visit(Logical.EquExpr e) {
            error(e);
        }

        public void visit(Logical.GreaterEquExpr e) {
            error(e);
        }

        public void visit(Logical.GreaterExpr e) {
            error(e);
        }

        public void visit(Logical.LessEquExpr e) {
            error(e);
        }

        public void visit(Logical.LessExpr e) {
            error(e);
        }

        public void visit(Logical.NequExpr e) {
            error(e);
        }

        public void visit(Logical.NotExpr e) {
            error(e);
        }

        public void visit(Logical.OrExpr e) {
            error(e);
        }

        public void visit(Logical.XorExpr e) {
            error(e);
        }


        public void visit(MapExpr e) {
            error(e);
        }

        public void visit(VarExpr e) {
            error(e);
        }

    }
}
