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
 * The <code>StmtVisitor</code> interface implements the visitor pattern so that clients can visit the
 * abstract syntax tree nodes representing statements in the program.
 *
 * @author Ben L. Titzer
 */
public interface StmtRebuilder extends CodeRebuilder {

    public Stmt visit(CallStmt s);

    public Stmt visit(DeclStmt s);

    public Stmt visit(IfStmt s);

    public Stmt visit(MapAssignStmt s);

    public Stmt visit(MapBitAssignStmt s);

    public Stmt visit(MapBitRangeAssignStmt s);

    public Stmt visit(ReturnStmt s);

    public Stmt visit(VarAssignStmt s);

    public Stmt visit(VarBitAssignStmt s);

    public Stmt visit(VarBitRangeAssignStmt s);

    /**
     * The <code>DepthFirst</code> class is a base implementation of the <code>StmtVisitor</code> interface
     * that visits the tree in depth-first order.
     *
     * @author Ben L. Titzer
     */
    public class DepthFirst extends CodeRebuilder.DepthFirst implements StmtRebuilder {
        public Stmt visit(CallStmt s) {
            List na = visitExprList(s.args);
            if (na != s.args)
                return new CallStmt(s.method, na);
            else
                return s;
        }

        public Stmt visit(DeclStmt s) {
            Expr ni = s.init.accept(this);
            if (ni != s.init)
                return new DeclStmt(s.name, s.type, ni);
            else
                return s;
        }

        public Stmt visit(IfStmt s) {
            Expr nc = s.cond.accept(this);
            List nt = visitStmtList(s.trueBranch);
            List nf = visitStmtList(s.falseBranch);

            if (nc != s.cond || nt != s.trueBranch || nf != s.falseBranch)
                return new IfStmt(nc, nt, nf);
            else
                return s;
        }

        public List visitStmtList(List l) {
            List nl = new LinkedList();
            boolean changed = false;

            Iterator i = l.iterator();
            while (i.hasNext()) {
                Stmt a = (Stmt)i.next();
                Stmt na = a.accept(this);
                if (na != a) changed = true;
                nl.add(na);
            }

            if (changed) return nl;
            return l;
        }

        public Stmt visit(MapAssignStmt s) {
            Expr ni = s.index.accept(this);
            Expr ne = s.expr.accept(this);
            if (ni != s.index || ne != s.expr)
                return new MapAssignStmt(s.mapname, ni, ne);
            else
                return s;
        }

        public Stmt visit(MapBitAssignStmt s) {
            Expr ni = s.index.accept(this);
            Expr nb = s.bit.accept(this);
            Expr ne = s.expr.accept(this);
            if (ni != s.index || nb != s.bit || ne != s.expr)
                return new MapBitAssignStmt(s.mapname, ni, nb, ne);
            else
                return s;
        }

        public Stmt visit(MapBitRangeAssignStmt s) {
            Expr ni = s.index.accept(this);
            Expr ne = s.expr.accept(this);
            if (ni != s.index || ne != s.expr)
                return new MapBitRangeAssignStmt(s.mapname, ni, s.low_bit, s.high_bit, ne);
            else
                return s;
        }

        public Stmt visit(ReturnStmt s) {
            Expr ne = s.expr.accept(this);
            if (ne != s.expr)
                return new ReturnStmt(ne);
            else
                return s;
        }

        public Stmt visit(VarAssignStmt s) {
            Expr ne = s.expr.accept(this);
            if (ne != s.expr)
                return new VarAssignStmt(s.variable, ne);
            else
                return s;
        }

        public Stmt visit(VarBitAssignStmt s) {
            Expr ne = s.expr.accept(this);
            Expr nb = s.bit.accept(this);
            if (ne != s.expr || nb != s.bit)
                return new VarBitAssignStmt(s.variable, nb, ne);
            else
                return s;
        }

        public Stmt visit(VarBitRangeAssignStmt s) {
            Expr ne = s.expr.accept(this);
            if (ne != s.expr)
                return new VarBitRangeAssignStmt(s.variable, s.low_bit, s.high_bit, ne);
            else
                return s;
        }

    }
}
