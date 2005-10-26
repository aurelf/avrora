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
 * Creation date: Oct 24, 2005
 */

package jintgen.gen;

import jintgen.jigir.*;
import jintgen.types.Type;
import jintgen.types.TypeRef;
import jintgen.isdl.SubroutineDecl;
import jintgen.isdl.OperandTypeDecl;
import jintgen.isdl.parser.Token;
import jintgen.isdl.Architecture;
import java.util.*;
import cck.util.Util;

/**
 * The <code>CodeSimplifier</code> class simplifies ISDL code by translating code that has
 * bit accesses, reads, writes, conversions, etc. into simpler code that only
 * contains masks, shifts, and subroutine calls.
 *
 * @author Ben L. Titzer
 */
public class CodeSimplifier extends StmtRebuilder<CGEnv> {

    protected final Architecture arch;
    protected final Type INT;
    protected final Type LONG;

    public CodeSimplifier(Architecture a) {
        arch = a;
        INT = arch.typeEnv.newIntType(true, 32);
        LONG = arch.typeEnv.newIntType(true, 64);
    }

    public void genAccessMethods() {
        for ( OperandTypeDecl ot : arch.operandTypes ) {
            for ( OperandTypeDecl.AccessMethod m : ot.readDecls ) {
                List<SubroutineDecl.Parameter> p = new LinkedList<SubroutineDecl.Parameter>();
                Canonicalizer canon = new Canonicalizer();
                p.add(new SubroutineDecl.Parameter(token("_this"), newTypeRef(ot)));
                canon.renameVariable("this", "_this.value");
                Token name = token("$read_" + getTypeString(m.type));
                List<Stmt> stmts = canon.process(m.code.getStmts());
                SubroutineDecl d = new SubroutineDecl(true, name, p, m.typeRef, stmts);
                arch.addSubroutine(d);
                m.setSubroutine(d);
            }
            for ( OperandTypeDecl.AccessMethod m : ot.writeDecls ) {
                List<SubroutineDecl.Parameter> p = new LinkedList<SubroutineDecl.Parameter>();
                Canonicalizer canon = new Canonicalizer();
                canon.renameVariable("this", "_this.value");
                p.add(new SubroutineDecl.Parameter(token("_this"), newTypeRef(ot)));
                p.add(new SubroutineDecl.Parameter(token("value"), m.typeRef));
                Token name = token("$write_" + getTypeString(m.type));
                List<Stmt> stmts = canon.process(m.code.getStmts());
                SubroutineDecl d = new SubroutineDecl(true, name, p, newTypeRef(token("void")), stmts);
                arch.addSubroutine(d);
                m.setSubroutine(d);
            }
        }
    }

    private TypeRef newTypeRef(OperandTypeDecl ot) {
        Token name = ot.name;
        return newTypeRef(name);
    }

    private TypeRef newTypeRef(Token name) {
        TypeRef ref = new TypeRef(name);
        ref.resolve(arch.typeEnv);
        return ref;
    }

    protected String getTypeString(Type t) {
        if ( t instanceof JIGIRTypeEnv.TYPE_int ) {
            JIGIRTypeEnv.TYPE_int it = (JIGIRTypeEnv.TYPE_int)t;
            String base = it.isSigned() ? "int" : "uint";
            return base+it.getSize();
        } else return t.getTypeCon().getName();
    }

    public Expr visit(BinOpExpr e, CGEnv env) {
        BinOpExpr.BinOpImpl b = e.getBinOp();
        // TODO: catch special cases of shifting, masking, etc
        // TODO: transform boolean operations to Java operations
        Expr nl = promote(e.left);
        Expr nr = promote(e.right);
        return shift(rebuild(e, nl, nr), env.shift);
    }

    public Expr visit(UnOpExpr e, CGEnv env) {
        UnOpExpr.UnOpImpl unop = e.getUnOp();
        if ( unop instanceof Arith.UNSIGN ) {
            // TODO: dunno if this is correct
            Expr no = promote(e.expr, INT, env.shift);
            JIGIRTypeEnv.TYPE_int it = (JIGIRTypeEnv.TYPE_int)e.expr.getType();
            int mask = mask_val(env.shift, it.getSize() + env.shift - 1);
            return newAnd(no, mask, env.expect);
        } else {
            Expr no = promote(e.expr, env.expect, 0);
            return shift(rebuild(e, no), env.shift);
        }
    }

    public Expr visit(IndexExpr e, CGEnv env) {
        if ( InterpreterGenerator.isMap(e.expr) ) {
            // translate map access into a call to map_get
            CallExpr ce = newCall(token("map_get"), promote(e.expr), promote(e.index));
            return shift(ce, env.shift);
        } else {
            // translate bit access into a mask and shift
            // TODO: transform bit access more efficiently
            CallExpr ce = newCall(token("bit_get"), promote(e.expr), promote(e.index));
            return shift(ce, env.shift);
        }
    }

    public Expr visit(FixedRangeExpr e, CGEnv env) {
        JIGIRTypeEnv.TYPE_int t = (JIGIRTypeEnv.TYPE_int)env.expect;
        int width = e.high_bit - e.low_bit + 1;
        if ( t.getSize() < width ) width = t.getSize();
        int mask = (-1 >>> (32 - width)) << env.shift;
        Expr ne = shift(e.expr, e.low_bit - env.shift);
        return newAnd(ne, mask, e.getType());
    }

    private BinOpExpr newAnd(Expr l, int mask, Type t) {
        BinOpExpr binop = new BinOpExpr(l, token("&"), new Literal.IntExpr(mask));
        binop.setBinOp(arch.typeEnv.AND);
        binop.setType(t);
        return binop;
    }

    public List<Expr> visitExprList(List<Expr> l, CGEnv env) {
        throw Util.failure("should not rebuild expr list directly");
    }

    public Expr visit(CallExpr e, CGEnv env) {
        List<Expr> na = rebuildParams(e.getDecl().params, e.args);
        return shift(rebuild(e, na), env.shift);
    }

    private List<Expr> rebuildParams(List<SubroutineDecl.Parameter> params, List<Expr> args) {
        List<Expr> na = new LinkedList<Expr>();
        Iterator<SubroutineDecl.Parameter> pi = params.iterator();
        for ( Expr a : args ) {
            SubroutineDecl.Parameter p = pi.next();
            na.add(promote(a, p.type.resolve(arch.typeEnv), 0));
        }
        return na;
    }

    public Expr visit(ReadExpr e, CGEnv env) {
        SubroutineDecl s = e.getAccessor().getSubroutine();
        List<Expr> l = new LinkedList<Expr>();
        l.add(new VarExpr(e.operand));
        Token name = s == null ? token("read_XXX") : s.name;
        CallExpr ce = new CallExpr(name, l);
        ce.setDecl(s);
        return ce;
    }

    public Expr visit(ConversionExpr e, CGEnv env) {
        // TODO: fix narrowing / widening conversions
        return promote(e.expr, e.getType(), env.shift);
    }

    public Expr visit(Literal.BoolExpr e, CGEnv env) {
        if ( env.expect == arch.typeEnv.BOOLEAN ) {
            if ( env.shift != 0 ) throw Util.failure("invalid expected shift of boolean at "+e.getSourcePoint());
            return e;
        }
        else if ( env.expect.isBasedOn("int") ) {
            Literal.IntExpr ne = new Literal.IntExpr(1 << env.shift);
            ne.setType(env.expect);
            return ne;
        } else
            throw Util.failure("unexpected promotion type for boolean literal at "+e.getSourcePoint());
    }

    public Expr visit(Literal.IntExpr e, CGEnv env) {
        if ( env.expect.isBasedOn("int") ) {
            Literal.IntExpr ne = new Literal.IntExpr(e.value << env.shift);
            ne.setType(env.expect);
            return ne;
        } else
            throw Util.failure("unexpected promotion type for int literal at "+e.getSourcePoint());        }

    public Expr visit(VarExpr e, CGEnv env) {
        return shift(e, env.shift);
    }

    public Expr visit(DotExpr e, CGEnv env) {
        Expr ne = promote(e.expr);
        return shift(ne, env.shift);
    }

    public Expr shift(Expr e, int shift) {
        if ( shift == 0 ) return e;
        else {
            Token t = token(shift > 0 ? "<<" : ">>");
            BinOpExpr.BinOpImpl impl = shift > 0 ? arch.typeEnv.SHL : arch.typeEnv.SHR;
            int dist = shift < 0 ? -shift : shift;
            BinOpExpr ne = new BinOpExpr(e, t, new Literal.IntExpr(dist));
            ne.setBinOp(impl);
            ne.setType(e.getType());
            return ne;
        }
    }

    public Stmt visit(WriteStmt s, CGEnv env) {
        OperandTypeDecl.Accessor accessor = s.getAccessor();
        SubroutineDecl sub = accessor.getSubroutine();
        List<Expr> l = new LinkedList<Expr>();
        l.add(new VarExpr(s.operand));
        l.add(promote(s.expr, machineType(accessor.type), 0));
        Token method = sub == null ? token("write_XXX") : sub.name;
        CallStmt cs = new CallStmt(method, l);
        cs.setDecl(sub);
        return cs;
    }

    public Stmt visit(CallStmt s, CGEnv env) {
        List<Expr> na = rebuildParams(s.getDecl().params, s.args);
        CallStmt cs = new CallStmt(s.method, na);
        cs.setDecl(s.getDecl());
        return cs;
    }

    public Stmt visit(AssignStmt s, CGEnv env) {
        throw Util.failure("Assignment statement not canonicalized");
    }

    public Stmt visit(AssignStmt.Var s, CGEnv env) {
        Expr ne = promote(s.expr, s.dest.getType(), 0);
        return new AssignStmt.Var(s.dest, ne);
    }

    public Stmt visit(AssignStmt.Map s, CGEnv env) {
        List<Expr> a = new LinkedList<Expr>();
        a.add(promote(s.map));
        a.add(promote(s.index));
        a.add(promote(s.expr));
        return new CallStmt(token("map_set"), a);
    }

    private Expr promote(Expr expr) {
        return promote(expr, expr.getType(), 0);
    }

    public Stmt visit(AssignStmt.Bit s, CGEnv env) {
        Expr nb = promote(s.bit);
        if ( nb.isLiteral() ) {
            int shift = ((Literal.IntExpr)nb).value;
            Expr ne = promote(s.expr, INT, shift);
            CallExpr ce = newCall(token("bit_update"), s.dest, mask(shift, shift), ne);
            return new AssignStmt.Var(s.dest, ce);
        } else {
            Expr ne = promote(s.expr, INT, 0);
            CallExpr ce = newCall(token("bit_set"), s.dest, nb, ne);
            return new AssignStmt.Var(s.dest, ce);
        }
    }

    public Stmt visit(AssignStmt.FixedRange s, CGEnv env) {
        Expr ne = promote(s.expr, INT, s.low_bit);
        CallExpr ce = newCall(token("bit_update"), s.dest, mask(s.low_bit, s.high_bit), ne);
        return new AssignStmt.Var(s.dest, ce);
    }

    private Expr promote(Expr e, Type t, int shift) {
        return e.accept(this, new CGEnv(machineType(t), shift));
    }

    private Type machineType(Type t) {
        if ( t instanceof JIGIRTypeEnv.TYPE_int ) {
            JIGIRTypeEnv.TYPE_int it = (JIGIRTypeEnv.TYPE_int)t;
            if ( it.getSize() > 32 )
                return LONG;
            else return INT;
        } else {
            return t;
        }
    }

    protected Expr visitExpr(Expr e, CGEnv env) {
        Expr ne;
        if ( env.expect == null ) {
            env.expect = e.getType();
            ne = e.accept(this, env);
            env.expect = null;
        } else {
            ne = e.accept(this, env);
        }
        if ( ne.getType() == null ) ne.setType(e.getType());
        return ne;
    }

    private Token token(String s) {
        Token t = new Token();
        t.image = s;
        return t;
    }

    private CallExpr newCall(SubroutineDecl d, Expr... e) {
        Token name = d.name;
        CallExpr ce = newCall(name, e);
        ce.setDecl(d);
        return ce;
    }

    private CallExpr newCall(Token name, Expr... e) {
        List<Expr> l = new LinkedList<Expr>();
        for ( Expr a : e ) l.add(a);
        CallExpr ce = new CallExpr(name, l);
        return ce;
    }

    private Literal.IntExpr mask(int low_bit, int high_bit) {
        int val = mask_val(low_bit, high_bit);
        return new Literal.IntExpr(val);
    }

    private int mask_val(int low_bit, int high_bit) {
        if ( low_bit < 0 ) low_bit = 0;
        int width = (high_bit - low_bit + 1);
        int val = (-1 >>> (32 - width)) << low_bit;
        return val;
    }
}
