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

import cck.util.Util;
import jintgen.isdl.*;
import jintgen.isdl.parser.Token;
import jintgen.jigir.*;
import jintgen.types.*;
import jintgen.Main;

import java.io.IOException;
import java.util.*;

/**
 * The <code>InterpreterGenerator</code> class is a visitor over the code of an instruction declaration or
 * subroutine that generates the appropriate Java code that implements an interpreter for the architecture.
 *
 * @author Ben L. Titzer
 */
public class InterpreterGenerator extends Generator {

    protected CodeGen codeGen;

    public void generate() throws IOException {
        initStatics();
        List<String> impl = new LinkedList<String>();
        impl.add(tr("$visitor"));
        setPrinter(newAbstractClassPrinter("interpreter", null, tr("$state"), impl, null));
        codeGen = new CodeGen();
        generateUtilities();
        for (SubroutineDecl d : arch.subroutines) visit(d);
        for (InstrDecl d : arch.instructions) visit(d);
        endblock();
        close();
    }

    private void initStatics() {
        properties.setProperty("addr", className("AddrMode"));
        properties.setProperty("instr", className("Instr"));
        properties.setProperty("operand", className("Operand"));
        properties.setProperty("opvisitor", className("OperandVisitor"));
        properties.setProperty("visitor", className("InstrVisitor"));
        properties.setProperty("builder", className("InstrBuilder"));
        properties.setProperty("symbol", className("Symbol"));
        properties.setProperty("interpreter", className("Interpreter"));
        properties.setProperty("state", className("State"));
    }

    void generateUtilities() {
        startblock("boolean bit_get(int v, int bit)");
        println("return (v & (1 << bit)) != 0;");
        endblock();

        startblock("int bit_set(int v, int bit, boolean value)");
        println("if ( value ) return v | (1 << bit);");
        println("else return v & ~(1 << bit);");
        endblock();

    }

    public void visit(InstrDecl d) {
        startblock("public void visit($instr.$1 i) ", d.innerClassName);
        // initialize the map of local variables to operands
        codeGen.variableMap = new HashMap<String, String>();
        for (AddrModeDecl.Operand o : d.getOperands()) {
            codeGen.variableMap.put(o.name.image, "i." + o.name.image);
        }
        // emit the code of the body
        generateCode(d.code.getStmts());
        endblock();
    }

    public void visit(SubroutineDecl d) {
        if ( !d.code.hasBody()) {
            print("protected abstract " + codeGen.renderType(d.ret) + ' ' + d.name.image);
            beginList("(");
            for (SubroutineDecl.Parameter p : d.getParams()) {
                print(codeGen.renderType(p.type) + ' ' + p.name.image);
            }
            endListln(");");
            return;
        }
        if (d.inline && Main.INLINE.get()) return;
        print("public " + codeGen.renderType(d.ret) + ' ' + d.name.image);
        beginList("(");
        for (SubroutineDecl.Parameter p : d.getParams()) {
            print(codeGen.renderType(p.type) + ' ' + p.name.image);
        }
        endList(") ");
        startblock();
        // initialize the map of local variables to operands
        codeGen.variableMap = new HashMap<String, String>();
        for (SubroutineDecl.Parameter p : d.getParams()) {
            String image = p.name.image;
            codeGen.variableMap.put(image, image);
        }
        generateCode(d.code.getStmts());
        endblock();
    }

    void generateCode(List<Stmt> stmts) {
        NCodeGen ncg = new NCodeGen();
        stmts = ncg.visitStmtList(stmts, new CGEnv());
        codeGen.visitStmtList(stmts);
    }

    protected class CodeGen extends PrettyPrinter {

        protected HashMap<String, String> variableMap;

        CodeGen() {
            super(p);
        }

        protected String getVariable(Token variable) {
            String var = variableMap.get(variable.image);
            if (var == null) var = variable.image;
            return var;
        }

        protected void emitCall(String s, Expr... exprs) {
            print(s + '(');
            boolean first = true;
            for ( Expr e : exprs ) {
                if ( !first ) print(", ");
                e.accept(this);
                first = false;
            }
            print(")");
        }

        public void visit(ConversionExpr e) {
            print("("+renderType(e.typename)+")");
            inner(e.expr, Expr.PREC_TERM);
        }

        public void visit(BinOpExpr e) {
            String operation = e.operation.image;
            if ( e.getBinOp() instanceof Logical.AND ) operation = "&&";
            if ( e.getBinOp() instanceof Logical.OR ) operation = "||";
            if ( e.getBinOp() instanceof Logical.XOR ) operation = "!=";
            binop(operation, e.left, e.right, e.getPrecedence());
        }

        public String renderType(Type t) {
            // TODO: compute the correct java type depending on the situation
            return renderTypeCon(t.getTypeCon());
        }

        private String renderTypeCon(TypeCon tc) {
            if ( tc instanceof JIGIRTypeEnv.TYPE_enum )
                return tr("$symbol.$1", tc.getName());
            else if ( tc instanceof JIGIRTypeEnv.TYPE_operand )
                return tr("$operand.$1", tc.getName());
            else return tc.toString();
        }

        public String renderType(TypeRef t) {
            // TODO: compute the correct java type depending on the situation
            return renderTypeCon(t.resolveTypeCon(arch.typeEnv));
        }

        public void visit(AssignStmt s) {
            if ( s.dest instanceof IndexExpr ) {
                IndexExpr ind = (IndexExpr)s.dest;
                if ( isMap(ind.expr)) {
                    emitCall("map_set", ind.expr, ind.index, s.expr);
                    println(";");
                } else {
                    // TODO: of course this is not correct!
                    emitCall("bit_set", ind.expr, ind.index, s.expr);
                    println(";");
                }
            } else if ( s.dest instanceof FixedRangeExpr ) {

            } else if ( s.dest instanceof VarExpr ) {
                s.dest.accept(this);
                print(" = ");
                s.expr.accept(this);
                println(";");
            }
        }
    }

    class CGEnv {
        Type expect;
        int shift;
    }

    protected class NCodeGen extends StmtRebuilder<CGEnv> {
        protected Type INT = arch.typeEnv.newIntType(true, 32);
        protected Type LONG = arch.typeEnv.newIntType(true, 64);

        public Expr visit(BinOpExpr e, CGEnv env) {
            BinOpExpr.BinOpImpl b = e.getBinOp();
            // TODO: catch special cases of shifting, masking, etc
            // TODO: transform boolean operations to Java operations
            Expr nl = promoteToMachineWidth(e.left, e.left.getType(), 0);
            Expr nr = promoteToMachineWidth(e.right, e.right.getType(), 0);
            return shift(rebuild(e, nl, nr), env.shift);
        }

        public Expr visit(UnOpExpr e, CGEnv env) {
            Expr no = promoteToMachineWidth(e.operand, env.expect, 0);
            // TODO: transform UNSIGN operation
            return shift(rebuild(e, no), env.shift);
        }

        public Expr visit(IndexExpr e, CGEnv env) {
            if ( isMap(e.expr) ) {
                // translate map access into a call to map_get
                List<Expr> l = new LinkedList<Expr>();
                l.add(promoteToMachineWidth(e.expr, e.expr.getType(), 0));
                l.add(promoteToMachineWidth(e.index, e.index.getType(), 0));
                CallExpr ce = new CallExpr(token("map_get"), l);
                return shift(ce, env.shift);
            } else {
                // translate bit access into a mask and shift
                // TODO: transform bit access
                List<Expr> l = new LinkedList<Expr>();
                l.add(promoteToMachineWidth(e.expr, e.expr.getType(), 0));
                l.add(promoteToMachineWidth(e.index, e.index.getType(), 0));
                CallExpr ce = new CallExpr(token("bit_get"), l);
                return shift(ce, env.shift);
            }
        }

        public Expr visit(FixedRangeExpr e, CGEnv env) {
            JIGIRTypeEnv.TYPE_int t = (JIGIRTypeEnv.TYPE_int)env.expect;
            int width = e.high_bit - e.low_bit + 1;
            if ( t.getSize() < width ) width = t.getSize();
            int mask = (-1 >>> (32 - width)) << env.shift;
            Expr ne = shift(e.operand, e.low_bit - env.shift);
            return newAnd(ne, mask, e.getType());
        }

        private BinOpExpr newAnd(Expr l, int mask, Type t) {
            BinOpExpr binop = new BinOpExpr(l, token("&"), new Literal.IntExpr(mask));
            binop.setBinOp(arch.typeEnv.AND);
            binop.setType(t);
            return binop;
        }
    
        public List<Expr> visitExprList(List<Expr> l, CGEnv env) {
            // TODO: transform expression lists
            return l;
        }

        public Expr visit(CallExpr e, CGEnv env) {
            List<Expr> na = new LinkedList<Expr>();
            List<SubroutineDecl.Parameter> params = e.getDecl().params;
            Iterator<SubroutineDecl.Parameter> pi = params.iterator();
            for ( Expr a : e.args ) {
                SubroutineDecl.Parameter p = pi.next();
                na.add(promoteToMachineWidth(a, p.type.resolve(arch.typeEnv), 0));
            }
            return shift(rebuild(e, na), env.shift);
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
            return promoteToMachineWidth(e.expr, e.getType(), env.shift);
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
            return shift(e, env.shift);
        }

        public Expr shift(Expr e, int shift) {
            if ( shift == 0 ) return e;
            else {
                Token t = token(shift < 0 ? "<<" : ">>");
                BinOpExpr.BinOpImpl impl = shift < 0 ? arch.typeEnv.SHL : arch.typeEnv.SHR;
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
            l.add(promoteToMachineWidth(s.expr, machineType(accessor.type), 0));
            Token method = sub == null ? token("write_XXX") : sub.name;
            CallStmt cs = new CallStmt(method, l);
            cs.setDecl(sub);
            return cs;
        }

        public Stmt visit(AssignStmt s) {
            // TODO: deal with assignments
            return s;
        }

        private Expr promoteToMachineWidth(Expr e, Type t, int shift) {
            CGEnv env = new CGEnv();
            env.expect = machineType(t);
            env.shift = shift;
            return e.accept(this, env);
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
            return ne;
        }

        private Token token(String s) {
            Token t = new Token();
            t.image = s;
            return t;
        }
    }

    private static boolean isMap(Expr expr) {
        return expr.getType().isBasedOn("map");
    }


}
