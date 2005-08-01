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
 * The <code>ExprVisitor</code> interface is part of the visitor pattern for expressions within the program.
 * It allows clients to visit nodes of the abstract syntax tree that represent expressions within the
 * program.
 *
 * @author Ben L. Titzer
 */
public interface ExprVisitor {

    public void visit(Arith.BinOp e);

    public void visit(Arith.UnOp e);

    public void visit(BitExpr e);

    public void visit(BitRangeExpr e);

    public void visit(CallExpr e);

    public void visit(ConversionExpr e);

    public void visit(Literal e);

    public void visit(Logical.BinOp e);

    public void visit(Logical.UnOp e);

    public void visit(MapExpr e);

    public void visit(VarExpr e);

    public void visit(DotExpr e);

    /**
     * The <code>DepthFirst</code> class is a base implementation of the <code>ExprVisitor</code> interface
     * that visits the tree in depth-first order.
     *
     * @author Ben L. Titzer
     */
    public class DepthFirst implements ExprVisitor {

        public void visit(Arith.BinOp e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Arith.UnOp e) {
            e.operand.accept(this);
        }

        public void visit(BitExpr e) {
            e.expr.accept(this);
            e.bit.accept(this);
        }

        public void visit(BitRangeExpr e) {
            e.operand.accept(this);
        }

        public void visit(CallExpr e) {
            for ( Expr a : e.args ) {
                a.accept(this);
            }
        }

        public void visit(ConversionExpr e) {
            e.expr.accept(this);
        }

        public void visit(Literal e) {
            // terminal node in the tree
        }

        public void visit(Logical.BinOp e) {
            e.left.accept(this);
            e.right.accept(this);
        }

        public void visit(Logical.UnOp e) {
            e.operand.accept(this);
        }

        public void visit(MapExpr e) {
            e.index.accept(this);
        }

        public void visit(VarExpr e) {
            // terminal node in the tree
        }

        public void visit(DotExpr e) {
            // terminal node in the tree
        }
    }
}
