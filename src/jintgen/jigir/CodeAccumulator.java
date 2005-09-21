/**
 * Copyright (c) 2005, Regents of the University of California
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
 *
 * Creation date: Sep 21, 2005
 */

package jintgen.jigir;

import java.util.List;

/**
 * The <code>CodeAccumulator</code> interface represents a traversal over the code
 * that computes some result value (such as a type, a constant, or a new expression).
 * This interface can be implemented by code traversals that implement some type
 * of analysis using the visitor pattern.
 *
 * @author Ben L. Titzer
 */
public interface CodeAccumulator<Res, Env> {

    public Res visit(Arith.AddExpr e, Env env);

    public Res visit(Arith.AndExpr e, Env env);

    public Res visit(Arith.CompExpr e, Env env);

    public Res visit(Arith.DivExpr e, Env env);

    public Res visit(Arith.MulExpr e, Env env);

    public Res visit(Arith.NegExpr e, Env env);

    public Res visit(Arith.OrExpr e, Env env);

    public Res visit(Arith.ShiftLeftExpr e, Env env);

    public Res visit(Arith.ShiftRightExpr e, Env env);

    public Res visit(Arith.SubExpr e, Env env);

    public Res visit(Arith.XorExpr e, Env env);

    public Res visit(BitExpr e, Env env);

    public Res visit(BitRangeExpr e, Env env);

    List<Res> visitExprList(List<Expr> l, Env env);

    public Res visit(CallExpr e, Env env);

    public Res visit(ConversionExpr e, Env env);

    public Res visit(Literal.BoolExpr e, Env env);

    public Res visit(Literal.IntExpr e, Env env);

    public Res visit(Logical.AndExpr e, Env env);

    public Res visit(Logical.EquExpr e, Env env);

    public Res visit(Logical.GreaterEquExpr e, Env env);

    public Res visit(Logical.GreaterExpr e, Env env);

    public Res visit(Logical.LessEquExpr e, Env env);

    public Res visit(Logical.LessExpr e, Env env);

    public Res visit(Logical.NequExpr e, Env env);

    public Res visit(Logical.NotExpr e, Env env);

    public Res visit(Logical.OrExpr e, Env env);

    public Res visit(Logical.XorExpr e, Env env);

    public Res visit(MapExpr e, Env env);

    public Res visit(VarExpr e, Env env);

    public Res visit(DotExpr e, Env env);
}
