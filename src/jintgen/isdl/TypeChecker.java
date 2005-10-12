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

import cck.util.Arithmetic;
import cck.util.Util;
import jintgen.jigir.*;
import jintgen.types.*;
import jintgen.isdl.parser.Token;

import java.util.*;

/**
 * The <code>TypeChecker</code> implements typecheck of JIGIR code. It visits each
 * expression and statement and checks that the types are not violated. After it has
 * processed the code, all expressions will be given types that can be used later
 * in generating Java code implementing the interpreter and analysis tools.
 *
 * @author Ben L. Titzer
 */
public class TypeChecker implements CodeAccumulator<Type, Environment>, StmtAccumulator<Environment, Environment> {

    final JIGIRTypeEnv typeEnv;
    final JIGIRErrorReporter ERROR;
    Type retType;

    TypeChecker(JIGIRErrorReporter er, JIGIRTypeEnv env) {
        ERROR = er;
        typeEnv = env;
    }

    public void typeCheck(List<Stmt> s, Type retType, Environment env) {
        this.retType = retType;
        for ( Stmt st : s ) typeCheck(st, env);
        this.retType = null;
    }

    public void typeCheck(Stmt s, Environment env) {
        s.accept(this, env);
    }

    public Environment visit(CallStmt s, Environment env) {
        typeCheckCall(env, s.method, s.args);
        return env;
    }

    public Environment visit(WriteStmt s, Environment env) {
        OperandTypeDecl d = operandTypeOf(s.operand, env);
        Type t = s.type.resolve(typeEnv);
        typeCheck("write", s.expr, t, env);
        return env;
    }

    private SubroutineDecl typeCheckCall(Environment env, Token method, List<Expr> args) {
        SubroutineDecl d = env.resolveMethod(method.image);
        if ( d == null ) ERROR.UnresolvedSubroutine(method);
        Iterator<Expr> eiter = args.iterator();
        if ( d.getParams().size() != args.size() )
            ERROR.ArityMismatch(method);
        for ( SubroutineDecl.Parameter p : d.getParams() ) {
            Type t = p.type.resolve(typeEnv);
            typeCheck("invocation", eiter.next(), t, env);
        }
        return d;
    }

    public Environment visit(CommentStmt s, Environment env) {
        return env;
    }

    public Environment visit(DeclStmt s, Environment env) {
        if ( env.isDefinedLocally(s.name.image) ) ERROR.RedefinedLocal(s.name);
        Type t = s.type.resolve(typeEnv);
        typeCheck("initialization", s.init, t, env);
        env.addVariable(s.name.image, t);
        return env;
    }

    public Environment visit(IfStmt s, Environment env) {
        typeCheck("if condition", s.cond, typeEnv.BOOLEAN, env);
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

    public Environment visit(ReturnStmt s, Environment env) {
        if ( retType == null ) ERROR.ReturnStmtNotInSubroutine(s);
        typeCheck("return", s.expr, retType, env);
        return env;
    }

    public Environment visit(AssignStmt s, Environment env) {
        if ( !s.dest.isLvalue() ) ERROR.NotAnLvalue(s.dest);
        Type lt = typeOf(s.dest, env);
        typeCheck("assignment", s.expr, lt, env);
        return env;
    }

    public Type visit(BinOpExpr e, Environment env) {
        Type lt = typeOf(e.left, env);
        Type rt = typeOf(e.right, env);
        TypeCon.BinOp binop = typeEnv.resolveBinOp(lt, rt, e.operation.image);
        if ( binop == null ) ERROR.UnresolvedOperator(e.operation, lt, rt);
        return binop.typeCheck(typeEnv, e.left, e.right);
    }

    public Type visit(IndexExpr e, Environment env) {
        Type lt = typeOf(e.expr, env);
        if ( lt.isBasedOn("int") ) {
            Type rt = intTypeOf(e.index, env);
            return typeEnv.BOOLEAN;
        }
        else if ( lt.isBasedOn("map") ) {
            Type rt = typeOf(e.index, env);
            List<TypeRef> pm = (List<TypeRef>)lt.getDimension("types");
            Type it = pm.get(0).resolve(typeEnv);
            Type et = pm.get(1).resolve(typeEnv);
            typeCheck("indexing", e.index, it, env);
            return et;
        } else {
            ERROR.TypeDoesNotSupportIndex(e.expr);
            return null;
        }
    }

    public Type visit(FixedRangeExpr e, Environment env) {
        Type lt = intTypeOf(e.operand, env);
        return typeEnv.newIntType(false, e.high_bit - e.low_bit + 1);
    }

    public List<Type> visitExprList(List<Expr> l, Environment env) {
        List<Type> lt = new LinkedList<Type>();
        for ( Expr e : l ) lt.add(typeOf(e, env));
        return lt;
    }

    public Type visit(CallExpr e, Environment env) {
        SubroutineDecl d = typeCheckCall(env, e.method, e.args);
        return d.ret.resolve(typeEnv);
    }

    public Type visit(ReadExpr e, Environment env) {
        OperandTypeDecl d = operandTypeOf(e.operand, env);
        if ( e.type != null ) {
            return e.type.resolve(typeEnv);
        } else return typeEnv.newIntType(false, 16);
    }

    public Type visit(ConversionExpr e, Environment env) { return e.typename.resolve(typeEnv); }

    public Type visit(Literal.BoolExpr e, Environment env) { return typeEnv.BOOLEAN; }

    public Type visit(Literal.IntExpr e, Environment env) {
        int val = e.value;
        boolean signed = val < 0;
        int width = Arithmetic.highestBit(val);
        return typeEnv.newIntType(signed, width);
    }

    public Type visit(UnOpExpr e, Environment env) {
        Type lt = typeOf(e.operand, env);
        TypeCon.UnOp unop = typeEnv.resolveUnOp(lt, e.operation.image);
        if ( unop == null ) ERROR.UnresolvedOperator(e.operation, lt);
        return unop.typeCheck(typeEnv, e.operand);
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
        if ( t == null ) throw Util.failure("null type at "+e.getSourcePoint());
        e.setType(t);
        return t;
    }

    protected Type intTypeOf(Expr e, Environment env) {
        Type t = e.accept(this, env);
        e.setType(t);
        if (!t.isBasedOn("int"))
            ERROR.IntTypeExpected("expression", e);
        return t;
    }

    protected OperandTypeDecl operandTypeOf(Token o, Environment env) {
        Type t = env.resolveVariable(o.image);
        if ( t == null ) ERROR.UnresolvedVariable(o);
        TypeCon tc = t.getTypeCon();
        if (!(tc instanceof JIGIRTypeEnv.TYPE_operand))
            ERROR.OperandTypeExpected(o, t);
        return ((JIGIRTypeEnv.TYPE_operand)tc).decl;
    }

    protected Type typeCheck(String what, Expr e, Type exp, Environment env) {
        Type t = typeOf(e, env);
        if (!isAssignableTo(t, exp))
            ERROR.TypeMismatch("expression", e, exp);
        return t;
    }

    private boolean isAssignableTo(Type t, Type exp) {
        return typeEnv.ASSIGNABLE.contains(t.getTypeCon(), exp.getTypeCon());
    }

    int max(int a, int b) {
        return a > b ? a : b;
    }
}
