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
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>CodeVisitor</code> interface represents a visitor that is more specific than the
 * <code>ExprVisitor</code> visitor, in that it contains visit methods for every type of arithmetic and
 * logical operation in the IR.
 *
 * @author Ben L. Titzer
 */
public interface CodeRebuilder<Env> {

    public Expr visit(Arith.AddExpr e, Env env);

    public Expr visit(Arith.AndExpr e, Env env);

    public Expr visit(Arith.CompExpr e, Env env);

    public Expr visit(Arith.DivExpr e, Env env);

    public Expr visit(Arith.MulExpr e, Env env);

    public Expr visit(Arith.NegExpr e, Env env);

    public Expr visit(Arith.OrExpr e, Env env);

    public Expr visit(Arith.ShiftLeftExpr e, Env env);

    public Expr visit(Arith.ShiftRightExpr e, Env env);

    public Expr visit(Arith.SubExpr e, Env env);

    public Expr visit(Arith.XorExpr e, Env env);

    public Expr visit(BitExpr e, Env env);

    public Expr visit(BitRangeExpr e, Env env);

    public Expr visit(CallExpr e, Env env);

    public Expr visit(ConversionExpr e, Env env);

    public Expr visit(Literal.BoolExpr e, Env env);

    public Expr visit(Literal.IntExpr e, Env env);

    public Expr visit(Logical.AndExpr e, Env env);

    public Expr visit(Logical.EquExpr e, Env env);

    public Expr visit(Logical.GreaterEquExpr e, Env env);

    public Expr visit(Logical.GreaterExpr e, Env env);

    public Expr visit(Logical.LessEquExpr e, Env env);

    public Expr visit(Logical.LessExpr e, Env env);

    public Expr visit(Logical.NequExpr e, Env env);

    public Expr visit(Logical.NotExpr e, Env env);

    public Expr visit(Logical.OrExpr e, Env env);

    public Expr visit(Logical.XorExpr e, Env env);

    public Expr visit(MapExpr e, Env env);

    public Expr visit(VarExpr e, Env env);

    public Expr visit(DotExpr e, Env env);

    public List<Expr> visitExprList(List<Expr> l, Env env);


    /**
     * The <code>DepthFirst</code> class is a base implementation of the <code>CodeVisitor</code> interface
     * that visits the tree in depth-first order.
     *
     * @author Ben L. Titzer
     */
    public class DepthFirst<Env> implements CodeRebuilder<Env> {

        public Expr visit(Arith.AddExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Arith.AddExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.AndExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Arith.AndExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.CompExpr e, Env env) {
            Expr o = e.operand.accept(this, env);
            if (o != e.operand) return new Arith.CompExpr(o);
            return e;
        }

        public Expr visit(Arith.DivExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Arith.DivExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.MulExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Arith.MulExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.NegExpr e, Env env) {
            Expr o = e.operand.accept(this, env);
            if (o != e.operand) return new Arith.NegExpr(o);
            return e;
        }

        public Expr visit(Arith.OrExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Arith.OrExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.ShiftLeftExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Arith.ShiftLeftExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.ShiftRightExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Arith.ShiftRightExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.SubExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Arith.SubExpr(l, r);
            else
                return e;
        }

        public Expr visit(Arith.XorExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Arith.XorExpr(l, r);
            else
                return e;
        }


        public Expr visit(BitExpr e, Env env) {
            Expr i = e.expr.accept(this, env);
            Expr j = e.bit.accept(this, env);
            if (i != e.expr || j != e.bit)
                return new BitExpr(i, j);
            else
                return e;
        }

        public Expr visit(BitRangeExpr e, Env env) {
            Expr o = e.operand.accept(this, env);
            if (o != e.operand) return new BitRangeExpr(o, e.low_bit, e.high_bit);
            return e;
        }

        public List<Expr> visitExprList(List<Expr> l, Env env) {
            List<Expr> nl = new LinkedList<Expr>();
            boolean changed = false;

            for ( Expr a : l ) {
                Expr na = a.accept(this, env);
                if (na != a) changed = true;
                nl.add(na);
            }

            if (changed) return nl;
            return l;
        }

        public Expr visit(CallExpr e, Env env) {
            List<Expr> nargs = visitExprList(e.args, env);
            if (nargs != e.args)
                return new CallExpr(e.method, nargs);
            else
                return e;
        }

        public Expr visit(ConversionExpr e, Env env) {
            Expr ne = e.expr.accept(this, env);
            if (ne != e.expr) return new ConversionExpr(ne, e.typename);
            return e;
        }

        public Expr visit(Literal.BoolExpr e, Env env) {
            // terminal node in the tree
            return e;
        }

        public Expr visit(Literal.IntExpr e, Env env) {
            // terminal node in the tree
            return e;
        }

        public Expr visit(Logical.AndExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Logical.AndExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.EquExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Logical.EquExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.GreaterEquExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Logical.GreaterEquExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.GreaterExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Logical.GreaterExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.LessEquExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Logical.LessEquExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.LessExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Logical.LessExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.NequExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Logical.NequExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.NotExpr e, Env env) {
            Expr ne = e.operand.accept(this, env);
            if (ne != e.operand) return new Logical.NotExpr(ne);
            return e;
        }

        public Expr visit(Logical.OrExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Logical.OrExpr(l, r);
            else
                return e;
        }

        public Expr visit(Logical.XorExpr e, Env env) {
            Expr l = e.left.accept(this, env);
            Expr r = e.right.accept(this, env);
            if (l != e.left || r != e.right)
                return new Logical.XorExpr(l, r);
            else
                return e;
        }


        public Expr visit(MapExpr e, Env env) {
            Expr ne = e.index.accept(this, env);
            if (ne != e.index) return new MapExpr(e.mapname, ne);
            return e;
        }

        public Expr visit(VarExpr e, Env env) {
            // terminal node in the tree
            return e;
        }

        public Expr visit(DotExpr e, Env env) {
            // terminal node in the tree
            return e;
        }

    }
}
