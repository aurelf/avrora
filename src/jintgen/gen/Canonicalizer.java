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

package jintgen.gen;

import jintgen.jigir.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class Canonicalizer extends StmtRebuilder.DepthFirst {

    int tempcount;

    public Expr visit(MapExpr e, Object env) {
        Expr ni = liftExpr(e.index, env);
        return extractExpr(new MapExpr(e.mapname, ni));
    }

    public Expr visit(CallExpr e, Object env) {
        List ne = visitExprList(e.args, env);
        return extractExpr(new CallExpr(e.method, ne));
    }

    public Stmt visit(MapAssignStmt e, Object env) {
        Expr ni = liftExpr(e.index, env);
        Expr nv = liftExpr(e.expr, env);
        return new MapAssignStmt(e.mapname, ni, nv);
    }

    public Stmt visit(MapBitAssignStmt e, Object env) {
        Expr ni = liftExpr(e.index, env);
        Expr nb = liftExpr(e.bit, env);
        Expr nv = liftExpr(e.expr, env);
        return new MapBitAssignStmt(e.mapname, ni, nb, nv);
    }

    public Stmt visit(MapBitRangeAssignStmt e, Object env) {
        Expr ni = liftExpr(e.index, env);
        Expr nv = liftExpr(e.expr, env);
        return new MapBitRangeAssignStmt(e.mapname, ni, e.low_bit, e.high_bit, nv);
    }

    protected Expr liftExpr(Expr e, Object env) {
        Expr ne = e.accept(this, env);

        if (ne.isVariable()) return ne;
        if (ne.isLiteral()) return ne;

        return extractExpr(ne);

    }

    private Expr extractExpr(Expr ne) {
        String tmpname = "_canon_tmp_" + (tempcount++);

        // TODO: get correct type!
        addStmt(new DeclStmt(tmpname, "int", ne));
        return new VarExpr(tmpname);
    }

    public LinkedList process(LinkedList stmts) {
        LinkedList s = (LinkedList)visitStmtList(stmts, null);
        return s;
    }


}
