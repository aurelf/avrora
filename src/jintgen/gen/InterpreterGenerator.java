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

import cck.text.StringUtil;
import cck.util.Arithmetic;
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
        for (SubroutineDecl d : arch.subroutines)
            visit(d);
        for (InstrDecl d : arch.instructions)
            visit(d);
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

        startblock("int map_get(int[] m, int ind)");
        println("return m[ind];");
        endblock();

        startblock("void map_set(int[] m, int ind, int v)");
        println("m[ind] = v;");
        endblock();

        startblock("int map_get(byte[] m, int ind)");
        println("return m[ind];");
        endblock();

        startblock("void map_set(byte[] m, int ind, int v)");
        println("m[ind] = (byte)v;");
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

        protected void emitBinOp(Expr e, String op, int p, int val) {
            print("(");
            this.inner(e, p);
            print(' ' + op + ' ' + val + ')');
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

        protected void emitCall(String s, String e1, Expr e2, Expr e3) {
            print(s + '(' + e1 + ", ");
            e2.accept(this);
            print(", ");
            e3.accept(this);
            print(")");
        }

        public void visit(ConversionExpr e) {
            print("("+renderType(e.typename)+")");
            inner(e.expr, Expr.PREC_TERM);
        }


        public void visit(IndexExpr e) {
            Expr expr = e.expr;
            if ( isMap(expr) ) emitCall("map_get", expr, e.index);
            else emitCall("bit_get", expr, e.index);
        }

        private boolean isMap(Expr expr) {
            return expr.getType().isBasedOn("map");
        }

        public void visit(FixedRangeExpr e) {
            int mask = Arithmetic.getBitRangeMask(e.low_bit, e.high_bit);
            int low = e.low_bit;
            if (low != 0) {
                print("(");
                emitBinOp(e.operand, ">>", Expr.PREC_A_SHIFT, low);
                print(" & " + StringUtil.to0xHex(mask, 8) + ')');
            } else {
                print("(");
                inner(e.operand, Expr.PREC_A_AND);
                print(" & " + StringUtil.to0xHex(mask, 8) + ')');
            }
        }

        public void visit(BinOpExpr e) {
            String operation = e.operation.image;
            if ( e.getBinOp() instanceof Logical.AND ) operation = "&&";
            if ( e.getBinOp() instanceof Logical.OR ) operation = "||";
            if ( e.getBinOp() instanceof Logical.XOR ) operation = "!=";
            binop(operation, e.left, e.right, e.getPrecedence());
        }

        public void visit(UnOpExpr e) {
            if ( e.getUnOp() instanceof Arith.UNSIGN ) {
                JIGIRTypeEnv.TYPE_int it = (JIGIRTypeEnv.TYPE_int) e.operand.getType();
                int size = it.getSize();
                int mask = 0xffffffff >>> (32-size);
                printer.print("(");
                inner(e.operand, Expr.PREC_A_AND);
                printer.print("& "+StringUtil.to0xHex(mask, 8)+")");
            } else {
                printer.print(e.operation.image);
                inner(e.operand, e.getPrecedence());
            }
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

    protected class NCodeGen implements CodeAccumulator<Type, Type> {
        public Type visit(BinOpExpr e, Type expect) {
            return expect;
        }

        public Type visit(UnOpExpr e, Type expect) {
            return expect;
        }

        public Type visit(IndexExpr e, Type expect) {
            return expect;
        }

        public Type visit(FixedRangeExpr e, Type expect) {
            return expect;
        }

        public List<Type> visitExprList(List<Expr> l, Type expect) {
            return null;
        }

        public Type visit(CallExpr e, Type expect) {
            return expect;
        }

        public Type visit(ReadExpr e, Type expect) {
            return expect;
        }

        public Type visit(ConversionExpr e, Type expect) {
            Type t = e.expr.accept(this, expect);
            return expect;
        }

        public Type visit(Literal.BoolExpr e, Type expect) {
            if ( e.value ) print("1");
            else print("0");
            return expect;
        }

        public Type visit(Literal.IntExpr e, Type expect) {
            return expect;
        }

        public Type visit(VarExpr e, Type expect) {
            return expect;
        }

        public Type visit(DotExpr e, Type expect) {
            return expect;
        }

        protected void convert_int(Expr e, JIGIRTypeEnv.TYPE_int from, JIGIRTypeEnv.TYPE_int to) {

        }

    }


}
