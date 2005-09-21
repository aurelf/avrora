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

package jintgen.isdl;

import jintgen.jigir.*;
import java.util.List;
import java.util.LinkedList;
import cck.util.Arithmetic;
import cck.util.Util;

/**
 * The <code>TypeChecker</code> implements typecheck of JIGIR code. It visits each
 * expression and statement and checks that the types are not violated. After it has
 * processed the code, all expressions will be given types that can be used later
 * in generating Java code implementing the interpreter and analysis tools.
 *
 * @author Ben L. Titzer
 */
public class TypeChecker implements CodeAccumulator<Type, Object> {

    final ErrorReporter ERROR;

    TypeChecker(ErrorReporter er) {
        ERROR = er;
    }

    public Type visit(Arith.AddExpr e, Object env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.AndExpr e, Object env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.CompExpr e, Object env) { return null; }

    public Type visit(Arith.DivExpr e, Object env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.MulExpr e, Object env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.NegExpr e, Object env) { return null; }

    public Type visit(Arith.OrExpr e, Object env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.ShiftLeftExpr e, Object env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.ShiftRightExpr e, Object env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.SubExpr e, Object env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.XorExpr e, Object env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        return null;
    }

    public Type visit(BitExpr e, Object env) { return null; }

    public Type visit(BitRangeExpr e, Object env) { return null; }

    public List<Type> visitExprList(List<Expr> l, Object env) {
        List<Type> lt = new LinkedList<Type>();
        for ( Expr e : l ) lt.add(typeOf(e, env));
        return lt;
    }

    public Type visit(CallExpr e, Object env) {
        List<Type> lt = visitExprList(e.args, env);
        return null;
    }

    public Type visit(ConversionExpr e, Object env) { return e.typename; }

    public Type visit(Literal.BoolExpr e, Object env) { return Type.BOOLEAN; }

    public Type visit(Literal.IntExpr e, Object env) {
        int val = e.value;
        boolean signed = val < 0;
        int width = Arithmetic.highestBit(val);
        return new Type(signed, "int", width);
    }

    public Type visit(Logical.AndExpr e, Object env) {
        typeCheck(e.left, env, Type.BOOLEAN);
        typeCheck(e.right, env, Type.BOOLEAN);
        return Type.BOOLEAN;
    }

    public Type visit(Logical.EquExpr e, Object env) {
        typeCheck(e.left, env, Type.BOOLEAN);
        typeCheck(e.right, env, Type.BOOLEAN);
        return Type.BOOLEAN;
    }

    public Type visit(Logical.GreaterEquExpr e, Object env) { return Type.BOOLEAN; }

    public Type visit(Logical.GreaterExpr e, Object env) { return Type.BOOLEAN; }

    public Type visit(Logical.LessEquExpr e, Object env) { return Type.BOOLEAN; }

    public Type visit(Logical.LessExpr e, Object env) { return Type.BOOLEAN; }

    public Type visit(Logical.NequExpr e, Object env) { return Type.BOOLEAN; }

    public Type visit(Logical.NotExpr e, Object env) {
        typeCheck(e.operand, env, Type.BOOLEAN);
        return Type.BOOLEAN;
    }

    public Type visit(Logical.OrExpr e, Object env) {
        typeCheck(e.left, env, Type.BOOLEAN);
        typeCheck(e.right, env, Type.BOOLEAN);
        return Type.BOOLEAN;
    }

    public Type visit(Logical.XorExpr e, Object env) {
        typeCheck(e.left, env, Type.BOOLEAN);
        typeCheck(e.right, env, Type.BOOLEAN);
        return Type.BOOLEAN;
    }

    public Type visit(MapExpr e, Object env) { return null; }

    public Type visit(VarExpr e, Object env) { return null; }

    public Type visit(DotExpr e, Object env) { return null; }

    protected Type typeOf(Expr e, Object env) {
        Type t = e.accept(this, env);
        e.setType(t);
        return t;
    }

    protected void typeCheck(Expr e, Object env, Type exp) {
        Type t = typeOf(e, env);
        if (!exp.isAssignableFrom(t))
            ERROR.TypeMismatch("expression", exp, t);
    }
}
