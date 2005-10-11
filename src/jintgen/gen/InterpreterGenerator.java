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
import cck.util.Util;
import jintgen.isdl.*;
import jintgen.isdl.parser.Token;
import jintgen.jigir.*;
import jintgen.types.Type;
import jintgen.types.TypeRef;
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



    public void visit(InstrDecl d) {
        startblock("public void visit($instr.$1 i) ", d.innerClassName);
        // emit the default next pc computation
        //println("nextpc = pc + " + (d.getEncodingSize() / 8) + ';');

        // initialize the map of local variables to operands
        codeGen.variableMap = new HashMap<String, String>();
        for (AddrModeDecl.Operand o : d.getOperands()) {
            codeGen.variableMap.put(o.name.image, "i." + o.name.image);
        }
        // emit the code of the body
        generateCode(d.code.getStmts());
        // emit the cycle count update
        println("cycles += " + d.getCycles() + ';');
        endblock();
    }

    public void visit(SubroutineDecl d) {
        if ( !d.code.hasBody()) {
            print("protected abstract " + javaType(d.ret) + ' ' + d.name.image);
            beginList("(");
            for (SubroutineDecl.Parameter p : d.getParams()) {
                print(javaType(p.type) + ' ' + p.name.image);
            }
            endListln(");");
            return;
        }
        if (d.inline) return;
        print("public " + javaType(d.ret) + ' ' + d.name.image);
        beginList("(");
        for (SubroutineDecl.Parameter p : d.getParams()) {
            print(javaType(p.type) + ' ' + p.name.image);
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
        ConstantPropagator cp = new ConstantPropagator();
        stmts = cp.process(stmts);
        codeGen.visitStmtList(stmts);
    }

    protected class CodeGen extends PrettyPrinter {

        protected final HashMap<String, PrettyPrinter.MapRep> mapMap;
        protected HashMap<String, String> variableMap;

        CodeGen() {
            super(p);
            mapMap = new HashMap<String, PrettyPrinter.MapRep>();
            mapMap.put("regs", new ArrayMap("sram"));
            mapMap.put("uregs", new GetterSetterMap("getRegisterUnsigned", "writeRegisterByte"));
            mapMap.put("wregs", new GetterSetterMap("getRegisterWord", "writeRegisterWord"));
            mapMap.put("aregs", new GetterSetterMap("getRegisterWord", "writeRegisterWord"));
            mapMap.put("sram", new GetterSetterMap("data.read", "data.write"));
            mapMap.put("ioregs", new IORegMap());
            mapMap.put("flash", new GetterSetterMap("getFlash", "---"));
            mapMap.put("isize", new GetterSetterMap("getInstrSize", "---"));
        }

        protected class GetterSetterMap extends PrettyPrinter.MapRep {

            public final String readMeth;
            public final String writeMeth;

            GetterSetterMap(String r, String w) {
                readMeth = r;
                writeMeth = w;
            }

            public void generateWrite(Expr ind, Expr val) {
                emitCall(getMethod(writeMeth), ind, val);
                println(";");
            }

            public void generateBitWrite(Expr ind, Expr b, Expr val) {
                // TODO: extract out expr value if it is not a simple expression
                print(getMethod(writeMeth) + '(');
                ind.accept(codeGen);
                print(", Arithmetic.setBit(" + readMeth + '(');
                ind.accept(codeGen);
                print("), ");
                b.accept(codeGen);
                print(", ");
                val.accept(codeGen);
                println("));");
            }

            public void generateRead(Expr ind) {
                emitCall(getMethod(readMeth), ind);
            }

            public void generateBitRead(Expr ind, Expr b) {
                print("Arithmetic.getBit(" + getMethod(readMeth) + '(');
                ind.accept(codeGen);
                print("), ");
                b.accept(codeGen);
                print(")");
            }

            public void generateBitRangeWrite(Expr ind, int l, int h, Expr val) {
                if (ind.isVariable() || ind.isLiteral()) {
                    String var = variableMap.get(ind.toString());
                    if (var == null) var = ind.toString();
                    print(writeMeth + '(' + var + ", ");
                    int mask = Arithmetic.getBitRangeMask(l, h);
                    int smask = mask << l;
                    int imask = ~smask;
                    print('(' + readMeth + '(' + var + ')' + andString(imask) + ')');
                    print(" | (");
                    emitAnd(val, mask);
                    if (l != 0) print(" << " + l);
                    println(");");
                } else {
                    throw Util.failure("non-constant expr into map in bit-range assignment");
                }
            }
        }

        protected class ArrayMap extends PrettyPrinter.MapRep {
            public final String varname;
            public final Token token;

            ArrayMap(String v) {
                varname = v;
                token = new Token();
                token.image = varname;
            }

            public void generateWrite(Expr ind, Expr val) {
                print(getVariable(token) + '[');
                ind.accept(codeGen);
                print("] = ");
                val.accept(codeGen);
                println(";");
            }

            public void generateBitWrite(Expr ind, Expr b, Expr val) {
                // TODO: extract out expr value if it is not a simple expression
                print(getVariable(token) + '[');
                ind.accept(codeGen);
                print("] = Arithmetic.getBit(" + getVariable(token) + '[');
                ind.accept(codeGen);
                print("], ");
                b.accept(codeGen);
                print(", ");
                val.accept(codeGen);
                println("));");
            }

            public void generateRead(Expr ind) {
                print(getVariable(token) + '[');
                ind.accept(codeGen);
                print("]");
            }

            public void generateBitRead(Expr ind, Expr b) {
                print("Arithmetic.getBit(" + getVariable(token) + '[');
                ind.accept(codeGen);
                print("], ");
                b.accept(codeGen);
                print(")");
            }

            public void generateBitRangeWrite(Expr ind, int l, int h, Expr val) {
                if (ind.isVariable() || ind.isLiteral()) {
                    Token t = new Token();
                    t.image = ind.toString();
                    String var = getVariable(t);
                    if (var == null) var = ind.toString();
                    print(getVariable(token) + '[' + var + "] = ");
                    int mask = Arithmetic.getBitRangeMask(l, h);
                    int smask = mask << l;
                    int imask = ~smask;
                    print(getVariable(token) + '[' + var + ']' + andString(imask) + ')');
                    print(" | (");
                    emitAnd(val, mask);
                    if (l != 0) print(" << " + l);
                    println(");");
                } else {
                    throw Util.failure("non-constant expr into map in bit-range assignment");
                }
            }
        }

        protected class IORegMap extends GetterSetterMap {
            IORegMap() {
                super("getIORegisterByte", "writeIORegisterByte");
            }

            public void generateBitWrite(Expr ind, Expr b, Expr val) {
                print(getMethod("getIOReg") + '(');
                ind.accept(codeGen);
                print(").writeBit(");
                b.accept(codeGen);
                print(", ");
                val.accept(codeGen);
                println(");");
            }

            public void generateBitRead(Expr ind, Expr b) {
                print(getMethod("getIOReg") + '(');
                ind.accept(codeGen);
                print(").readBit(");
                b.accept(codeGen);
                print(")");
            }
        }


        protected MapRep getMapRep(String n) {
            MapRep mr = mapMap.get(n);
            if (mr == null) throw Util.failure("unknown map " + StringUtil.quote(n));
            return mr;
        }

        protected String getVariable(Token variable) {
            // TODO: get rid of direct register references
            String var = variableMap.get(variable.image);
            if (var == null) var = variable.image;
            return var;
        }

        protected void emitBinOp(Expr e, String op, int p, int val) {
            print("(");
            this.inner(e, p);
            print(' ' + op + ' ' + val + ')');
        }

        protected String andString(int mask) {
            return " & " + StringUtil.to0xHex(mask, 8);
        }

        protected void emitAnd(Expr e, int val) {
            print("(");
            this.inner(e, Expr.PREC_A_AND);
            print(andString(val) + ')');
        }

        protected void emitCall(String s, Expr e) {
            print(s + '(');
            e.accept(this);
            print(")");
        }

        protected void emitCall(String s, Expr e1, Expr e2) {
            print(s + '(');
            e1.accept(this);
            print(", ");
            e2.accept(this);
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
            print("("+javaType(e.typename)+")");
            inner(e.expr, Expr.PREC_TERM);
        }


        public void visit(IndexExpr e) {
            if (e.expr.isMap()) {
                MapExpr me = (MapExpr) e.expr;
                MapRep mr = getMapRep(me.mapname.image);
                mr.generateBitRead(me.index, e.index);
            } else {
                if (e.index.isLiteral()) {
                    int mask = Arithmetic.getSingleBitMask(((Literal.IntExpr) e.index).value);
                    print("((");
                    inner(e.expr, Expr.PREC_A_AND);
                    print(" & " + mask + ") != 0");
                    print(")");
                } else {
                    emitCall("Arithmetic.getBit", e.expr, e.index);
                }
            }
        }

        public void visit(FixedRangeExpr e) {
            int mask = Arithmetic.getBitRangeMask(e.low_bit, e.high_bit);
            int low = e.low_bit;
            if (low != 0) {
                print("(");
                emitBinOp(e.operand, ">>", Expr.PREC_A_SHIFT, low);
                print(" & " + StringUtil.to0xHex(mask, 8) + ')');
            } else {
                emitAnd(e.operand, mask);
            }
        }

        public void visit(BinOpExpr e) {
            binop(e.operation.image, e.left, e.right, e.getPrecedence());
        }

    }

    protected String javaType(Type t) {
        throw Util.unimplemented();
    }

    protected String javaType(TypeRef t) {
        throw Util.unimplemented();
    }

}
