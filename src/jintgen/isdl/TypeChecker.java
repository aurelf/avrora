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
public class TypeChecker implements CodeAccumulator<Type, Environment>, StmtAccumulator<Environment, Environment> {

    final ErrorReporter ERROR;

    TypeChecker(ErrorReporter er) {
        ERROR = er;
    }

    public void typeCheck(List<Stmt> s, Environment env) {
        for ( Stmt st : s ) typeCheck(st, env);
    }

    public void typeCheck(Stmt s, Environment env) {
        s.accept(this, env);
    }

    public Environment visit(CallStmt s, Environment env) {
        return env;
    }

    public Environment visit(CommentStmt s, Environment env) {
        return env;
    }

    public Environment visit(DeclStmt s, Environment env) {
        typeCheck("initialization", s.init, s.type, env);
        env.addVariable(s.name.image, s.type);
        return env;
    }

    public Environment visit(IfStmt s, Environment env) {
        typeCheck("if condition", s.cond, Type.BOOLEAN, env);
        Environment te = new Environment(env);
        visitStmtList(s.trueBranch, te);
        Environment fe = new Environment(env);
        visitStmtList(s.falseBranch, fe);
        return env;
    }

    public List<Environment> visitStmtList(List<Stmt> l, Environment env) {
        for ( Stmt s : l ) typeCheck(s, env);
        return null;
    }

    public Environment visit(MapAssignStmt s, Environment env) {
        return env;
    }

    public Environment visit(MapBitAssignStmt s, Environment env) {
        return env;
    }

    public Environment visit(MapBitRangeAssignStmt s, Environment env) {
        return env;
    }

    public Environment visit(ReturnStmt s, Environment env) {
        return env;
    }

    public Environment visit(VarAssignStmt s, Environment env) {
        Type t = env.resolveVariable(s.variable.image);
        if ( t == null ) ERROR.UnresolvedVariable(s.variable);
        typeCheck("assignment", s.expr, t, env);
        return env;
    }

    public Environment visit(VarBitAssignStmt s, Environment env) {
        Type t = env.resolveVariable(s.variable.image);
        if ( t == null ) ERROR.UnresolvedVariable(s.variable);
        typeCheck("bit assignment", s.expr, t, env);
        // TODO: fix me
        return env;
    }

    public Environment visit(VarBitRangeAssignStmt s, Environment env) {
        Type t = env.resolveVariable(s.variable.image);
        if ( t == null ) ERROR.UnresolvedVariable(s.variable);
        typeCheck("bit range assignment", s.expr, new Type(false, "int", s.high_bit - s.low_bit + 1), env);
        return env;
    }


    public Type visit(Arith.AddExpr e, Environment env) {
        Type lt = intTypeOf(e.left, env);
        Type rt = intTypeOf(e.right, env);
        return new Type(lt.isSigned() || rt.isSigned(), "int", max(lt, rt) + 1);
    }

    public Type visit(Arith.AndExpr e, Environment env) {
        return bitwise(e, env);
    }

    private Type bitwise(Arith.BinOp e, Environment env) {
        Type lt = intTypeOf(e.left, env);
        Type rt = intTypeOf(e.right, env);
        return new Type(false, "int", max(lt, rt));
    }

    public Type visit(Arith.CompExpr e, Environment env) {
        Type lt = intTypeOf(e.operand, env);
        return lt;
    }

    public Type visit(Arith.DivExpr e, Environment env) {
        Type lt = intTypeOf(e.left, env);
        Type rt = intTypeOf(e.right, env);
        return lt;
    }

    public Type visit(Arith.MulExpr e, Environment env) {
        Type lt = intTypeOf(e.left, env);
        Type rt = intTypeOf(e.right, env);
        return new Type(lt.isSigned() || rt.isSigned(), "int", lt.getWidth() + rt.getWidth());
    }

    public Type visit(Arith.NegExpr e, Environment env) {
        Type lt = intTypeOf(e.operand, env);
        return new Type(lt.isSigned(), "int", lt.getWidth() + 1);
    }

    public Type visit(Arith.OrExpr e, Environment env) {
        return bitwise(e, env);
    }

    public Type visit(Arith.ShiftLeftExpr e, Environment env) {
        Type lt = intTypeOf(e.left, env);
        Type rt = intTypeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.ShiftRightExpr e, Environment env) {
        Type lt = intTypeOf(e.left, env);
        Type rt = intTypeOf(e.right, env);
        return null;
    }

    public Type visit(Arith.SubExpr e, Environment env) {
        Type lt = intTypeOf(e.left, env);
        Type rt = intTypeOf(e.right, env);
        return new Type(true, "int", max(lt, rt) + 1);
    }

    public Type visit(Arith.XorExpr e, Environment env) {
        return bitwise(e, env);
    }

    public Type visit(BitExpr e, Environment env) {
        Type lt = intTypeOf(e.expr, env);
        Type rt = intTypeOf(e.bit, env);
        return Type.BOOLEAN;
    }

    public Type visit(BitRangeExpr e, Environment env) {
        Type lt = intTypeOf(e.operand, env);
        return new Type(false, "int", e.high_bit - e.low_bit + 1);
    }

    public List<Type> visitExprList(List<Expr> l, Environment env) {
        List<Type> lt = new LinkedList<Type>();
        for ( Expr e : l ) lt.add(typeOf(e, env));
        return lt;
    }

    public Type visit(CallExpr e, Environment env) {
        List<Type> lt = visitExprList(e.args, env);
        return null;
    }

    public Type visit(ConversionExpr e, Environment env) { return e.typename; }

    public Type visit(Literal.BoolExpr e, Environment env) { return Type.BOOLEAN; }

    public Type visit(Literal.IntExpr e, Environment env) {
        int val = e.value;
        boolean signed = val < 0;
        int width = Arithmetic.highestBit(val);
        return new Type(signed, "int", width);
    }

    public Type visit(Logical.AndExpr e, Environment env) {
        typeCheck("logical and", e.left, Type.BOOLEAN, env);
        typeCheck("logical and", e.right, Type.BOOLEAN, env);
        return Type.BOOLEAN;
    }

    public Type visit(Logical.EquExpr e, Environment env) {
        return equal(e, env);
    }

    private Type equal(Logical.BinOp e, Environment env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        if ( !lt.isComparableTo(rt) )
            ERROR.TypesCannotBeCompared(lt, rt);
        return Type.BOOLEAN;
    }

    public Type visit(Logical.GreaterEquExpr e, Environment env) {
        return compare(e, env);
    }

    private Type compare(Logical.BinOp e, Environment env) {
        Type lt = intTypeOf(e.left, env);
        Type rt = intTypeOf(e.right, env);
        return Type.BOOLEAN;
    }

    public Type visit(Logical.GreaterExpr e, Environment env) {
        return compare(e, env);
    }

    public Type visit(Logical.LessEquExpr e, Environment env) {
        return compare(e, env);
    }

    public Type visit(Logical.LessExpr e, Environment env) {
        return compare(e, env);
    }

    public Type visit(Logical.NequExpr e, Environment env) {
        return equal(e, env);
    }

    public Type visit(Logical.NotExpr e, Environment env) {
        typeCheck("logical not", e.operand, Type.BOOLEAN, env);
        return Type.BOOLEAN;
    }

    public Type visit(Logical.OrExpr e, Environment env) {
        typeCheck("logical or", e.left, Type.BOOLEAN, env);
        typeCheck("logical or", e.right, Type.BOOLEAN, env);
        return Type.BOOLEAN;
    }

    public Type visit(Logical.XorExpr e, Environment env) {
        typeCheck("logical xor", e.left, Type.BOOLEAN, env);
        typeCheck("logical xor", e.right, Type.BOOLEAN, env);
        return Type.BOOLEAN;
    }

    public Type visit(MapExpr e, Environment env) {
        Object o = env.resolveMap(e.mapname.image);
        if ( o == null ) ERROR.UnresolvedVariable(e.mapname);
        return null;
    }

    public Type visit(VarExpr e, Environment env) {
        Type t = env.resolveVariable(e.variable.image);
        if ( t == null ) ERROR.UnresolvedVariable(e.variable);
        return t;
    }

    public Type visit(DotExpr e, Environment env) {
        Object o = env.resolveVariable(e.operand.image);
        if ( o == null ) ERROR.UnresolvedVariable(e.operand);
        return null;
    }

    protected Type typeOf(Expr e, Environment env) {
        Type t = e.accept(this, env);
        if ( t == null ) throw Util.failure("null type at "+e.getLocation());
        e.setType(t);
        return t;
    }

    protected Type intTypeOf(Expr e, Environment env) {
        Type t = e.accept(this, env);
        e.setType(t);
        if (!t.isBasedOn("int"))
            ERROR.IntTypeExpected("expression", t);
        return t;
    }

    protected Type typeCheck(String what, Expr e, Type exp, Environment env) {
        Type t = typeOf(e, env);
        if (!exp.isAssignableFrom(t))
            ERROR.TypeMismatch("expression", exp, e);
        return t;
    }

    int max(int a, int b) {
        return a > b ? a : b;
    }

    int max(Type at, Type bt) {
        return max(at.getWidth(), bt.getWidth());
    }
}
