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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>CodeVisitor</code> interface represents a visitor that is more specific than the
 * <code>ExprVisitor</code> visitor, in that it contains visit methods for every type of arithmetic and
 * logical operation in the IR.
 *
 * @author Ben L. Titzer
 */
public interface CodeRebuilder {

    public Expr visit(Arith.AddExpr e);

    public Expr visit(Arith.AndExpr e);

    public Expr visit(Arith.CompExpr e);

    public Expr visit(Arith.DivExpr e);

    public Expr visit(Arith.MulExpr e);

    public Expr visit(Arith.NegExpr e);

    public Expr visit(Arith.OrExpr e);

    public Expr visit(Arith.ShiftLeftExpr e);

    public Expr visit(Arith.ShiftRightExpr e);

    public Expr visit(Arith.SubExpr e);

    public Expr visit(Arith.XorExpr e);

    public Expr visit(BitExpr e);

    public Expr visit(BitRangeExpr e);

    public Expr visit(CallExpr e);

    public Expr visit(Literal.BoolExpr e);

    public Expr visit(Literal.IntExpr e);

    public Expr visit(Logical.AndExpr e);

    public Expr visit(Logical.EquExpr e);

    public Expr visit(Logical.GreaterEquExpr e);

    public Expr visit(Logical.GreaterExpr e);

    public Expr visit(Logical.LessEquExpr e);

    public Expr visit(Logical.LessExpr e);

    public Expr visit(Logical.NequExpr e);

    public Expr visit(Logical.NotExpr e);

    public Expr visit(Logical.OrExpr e);

    public Expr visit(Logical.XorExpr e);

    public Expr visit(MapExpr e);

    public Expr visit(VarExpr e);


    /**
     * The <code>DepthFirst</code> class is a base implementation of the <code>CodeVisitor</code> interface
     * that visits the tree in depth-first order.
     *
     * @author Ben L. Titzer
     */
    public class DepthFirst implements CodeRebuilder {

        public Expr visit(Arith.AddExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Arith.AddExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.AndExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Arith.AndExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.CompExpr e) {
            Expr o = e.operand.accept(this);
            if (o != e.operand) return new Arith.CompExpr(o);
            return e;
        }

        public Expr visit(Arith.DivExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Arith.DivExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.MulExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Arith.MulExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.NegExpr e) {
            Expr o = e.operand.accept(this);
            if (o != e.operand) return new Arith.NegExpr(o);
            return e;
        }

        public Expr visit(Arith.OrExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Arith.OrExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.ShiftLeftExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Arith.ShiftLeftExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.ShiftRightExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Arith.ShiftRightExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.SubExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Arith.SubExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.XorExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Arith.XorExpr(l, r);
            else
                return e;
        }


        public Expr visit(BitExpr e) {
            Expr i = e.expr.accept(this);
            Expr j = e.bit.accept(this);
            if (i != e.expr || j != e.bit)
                return new BitExpr(i, j);
            else
                return e;
        }

        public Expr visit(BitRangeExpr e) {
            Expr o = e.operand.accept(this);
            if (o != e.operand) return new BitRangeExpr(o, e.low_bit, e.high_bit);
            return e;
        }

        public List visitExprList(List l) {
            List nl = new LinkedList();
            boolean changed = false;

            Iterator i = l.iterator();
            while (i.hasNext()) {
                Expr a = (Expr)i.next();
                Expr na = a.accept(this);
                if (na != a) changed = true;
                nl.add(na);
            }

            if (changed) return nl;
            return l;
        }

        public Expr visit(CallExpr e) {
            List nargs = visitExprList(e.args);
            if (nargs != e.args)
                return new CallExpr(e.method, nargs);
            else
                return e;
        }

        public Expr visit(Literal.BoolExpr e) {
            // terminal node in the tree
            return e;
        }

        public Expr visit(Literal.IntExpr e) {
            // terminal node in the tree
            return e;
        }

        public Expr visit(Logical.AndExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Logical.AndExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.EquExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Logical.EquExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.GreaterEquExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Logical.GreaterEquExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.GreaterExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Logical.GreaterExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.LessEquExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Logical.LessEquExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.LessExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Logical.LessExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.NequExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Logical.NequExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.NotExpr e) {
            Expr ne = e.operand.accept(this);
            if (ne != e.operand) return new Logical.NotExpr(ne);
            return e;
        }

        public Expr visit(Logical.OrExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Logical.OrExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.XorExpr e) {
            Expr l = e.left.accept(this);
            Expr r = e.right.accept(this);
            if (l != e.left || r != e.right)
                return new Logical.XorExpr(l, r);
            else
                return e;
        }


        public Expr visit(MapExpr e) {
            Expr ne = e.index.accept(this);
            if (ne != e.index) return new MapExpr(e.mapname, ne);
            return e;
        }

        public Expr visit(VarExpr e) {
            // terminal node in the tree
            return e;
        }

    }
}
