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

package avrora.core.isdl.gen;

import avrora.Avrora;
import avrora.core.isdl.*;
import avrora.core.isdl.ast.*;
import avrora.core.isdl.parser.Token;
import avrora.util.Printer;
import avrora.util.StringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * The <code>InterpreterGenerator</code> class is a visitor over the code of an instruction declaration or
 * subroutine that generates the appropriate Java code that implements an interpreter for the architecture.
 *
 * @author Ben L. Titzer
 */
public class InterpreterGenerator extends PrettyPrinter implements Architecture.Visitor {

    protected final Architecture architecture;

    protected final HashMap mapMap;
    protected HashMap variableMap;

    protected class GetterSetterMap extends MapRep {

        public final String readMeth;
        public final String writeMeth;

        GetterSetterMap(String r, String w) {
            readMeth = r;
            writeMeth = w;
        }

        public void generateWrite(Expr ind, Expr val) {
            emitCall(getMethod(writeMeth), ind, val);
            printer.println(";");
        }

        public void generateBitWrite(Expr ind, Expr b, Expr val) {
            // TODO: extract out index value if it is not a simple expression
            printer.print(getMethod(writeMeth) + "(");
            ind.accept(InterpreterGenerator.this);
            printer.print(", Arithmetic.setBit(" + readMeth + "(");
            ind.accept(InterpreterGenerator.this);
            printer.print("), ");
            b.accept(InterpreterGenerator.this);
            printer.print(", ");
            val.accept(InterpreterGenerator.this);
            printer.println("));");
        }

        public void generateRead(Expr ind) {
            emitCall(getMethod(readMeth), ind);
        }

        public void generateBitRead(Expr ind, Expr b) {
            printer.print("Arithmetic.getBit(" + getMethod(readMeth) + "(");
            ind.accept(InterpreterGenerator.this);
            printer.print("), ");
            b.accept(InterpreterGenerator.this);
            printer.print(")");
        }

        public void generateBitRangeWrite(Expr ind, int l, int h, Expr val) {
            if (ind.isVariable() || ind.isLiteral()) {
                String var = (String)variableMap.get(ind.toString());
                if (var == null) var = ind.toString();
                printer.print(writeMeth + "(" + var + ", ");
                int mask = getBitRangeMask(l, h);
                int smask = mask << l;
                int imask = ~smask;
                printer.print("(" + readMeth + "(" + var + ")" + andString(imask) + ")");
                printer.print(" | (");
                emitAnd(val, mask);
                if (l != 0) printer.print(" << " + l);
                printer.println(");");
            } else {
                throw Avrora.failure("non-constant index into map in bit-range assignment");
            }
        }
    }

    protected class IORegMap extends GetterSetterMap {
        IORegMap() {
            super("getIORegisterByte", "setIORegisterByte");
        }

        public void generateBitWrite(Expr ind, Expr b, Expr val) {
            printer.print(getMethod("getIOReg")+"(");
            ind.accept(InterpreterGenerator.this);
            printer.print(").writeBit(");
            b.accept(InterpreterGenerator.this);
            printer.print(", ");
            val.accept(InterpreterGenerator.this);
            printer.println(");");
        }

        public void generateBitRead(Expr ind, Expr b) {
            printer.print(getMethod("getIOReg")+"(");
            ind.accept(InterpreterGenerator.this);
            printer.print(").readBit(");
            b.accept(InterpreterGenerator.this);
            printer.print(")");
        }
    }


    /**
     * The constructor for the <code>InterpreterGenerator</code> class builds an object capable of generating
     * the interpreter for a particular architecture that outputs to the specified printer. In this
     * implementation, the interpreter generator simply outputs visit() methods for each instruction that are
     * meant to be pasted into a template file containing the rest of the interpreter. This can be done by
     * constructing a <code>SectionFile</code> instance.
     *
     * @param a the architecture to generate an interrupter for
     * @param p a printer to output the code implementing the interpreter
     */
    public InterpreterGenerator(Architecture a, Printer p) {
        super(p);
        architecture = a;
        mapMap = new HashMap();

        initializeMaps();
    }

    protected void initializeMaps() {
        mapMap.put("regs", new GetterSetterMap("getRegisterByte", "setRegisterByte"));
        mapMap.put("uregs", new GetterSetterMap("getRegisterUnsigned", "setRegisterByte"));
        mapMap.put("wregs", new GetterSetterMap("getRegisterWord", "setRegisterWord"));
        mapMap.put("sram", new GetterSetterMap("getDataByte", "setDataByte"));
        mapMap.put("ioregs", new IORegMap());
        mapMap.put("program", new GetterSetterMap("getProgramByte", "setProgramByte"));
        mapMap.put("isize", new GetterSetterMap("getInstrSize", "---"));
    }

    public void generateCode() {
        printer.indent();
        architecture.accept(this);
        printer.unindent();
    }

    public void visit(OperandDecl d) {
        // don't care about operand declarations
    }

    public void visit(EncodingDecl d) {
        // don't care about operand declarations
    }

    public void visit(InstrDecl d) {
        printer.startblock("public void visit(" + d.getClassName() + " i) ");
        // emit the default next pc computation
        printer.println("nextPC = pc + " + (d.getEncodingSize() / 8) + ";");

        // initialize the map of local variables to operands
        initializeOperandMap(d);
        // emit the code of the body
        visitStmtList(d.getCode());
        // emit the cycle count update
        printer.println("cyclesConsumed += " + d.cycles + ";");
        printer.endblock();
    }

    protected void initializeOperandMap(CodeRegion cr) {
        variableMap = new HashMap();
        Iterator i = cr.getOperandIterator();
        while (i.hasNext()) {
            CodeRegion.Operand o = (CodeRegion.Operand)i.next();

            String image = o.name.image;
            if (cr instanceof InstrDecl)
                image = "i." + image;

            variableMap.put(o.name.image, image);
        }
    }

    public void visit(SubroutineDecl d) {
        if (d.inline || !d.hasBody()) return;
        printer.print("public " + d.ret.image + " " + d.name.image + "(");
        Iterator i = d.getOperandIterator();
        while (i.hasNext()) {
            CodeRegion.Operand o = (CodeRegion.Operand)i.next();
            printer.print(o.type.image + " " + o.name.image);
            if (i.hasNext()) printer.print(", ");
        }
        printer.print(") ");
        printer.startblock();
        // initialize the map of local variables to operands
        initializeOperandMap(d);
        visitStmtList(d.getCode());
        printer.endblock();
    }


    protected MapRep getMapRep(String n) {
        MapRep mr = (MapRep)mapMap.get(n);
        if (mr == null)
            throw Avrora.failure("unknown map " + StringUtil.quote(n));
        return mr;
    }

    public void visit(VarBitAssignStmt s) {
        String var = getVariable(s.variable);
        printer.print(var + " = ");
        emitCall("Arithmetic.setBit", var, s.bit, s.expr);
        printer.println(";");
    }

    public void visit(VarBitRangeAssignStmt s) {
        String var = getVariable(s.variable);
        int mask = getBitRangeMask(s.low_bit, s.high_bit);
        int smask = mask << s.low_bit;
        int imask = ~smask;
        printer.print(var + " = (" + var + andString(imask) + ")");
        printer.print(" | (");
        emitAnd(s.expr, mask);
        if (s.low_bit != 0) printer.print(" << " + s.low_bit);
        printer.println(");");
    }

    protected String getVariable(Token variable) {
        // TODO: get rid of direct register references
        String var = (String)variableMap.get(variable.image);
        if (var == null) var = variable.image;
        return var;
    }

    protected void emitBinOp(Expr e, String op, int p, int val) {
        printer.print("(");
        this.inner(e, p);
        printer.print(" " + op + " " + val + ")");
    }

    protected String andString(int mask) {
        return " & 0x" + StringUtil.toHex(mask, 8);
    }

    protected void emitAnd(Expr e, int val) {
        printer.print("(");
        this.inner(e, Expr.PREC_A_AND);
        printer.print(andString(val) + ")");
    }

    protected void emitCall(String s, Expr e) {
        printer.print(s + "(");
        e.accept(this);
        printer.print(")");
    }

    protected void emitCall(String s, Expr e1, Expr e2) {
        printer.print(s + "(");
        e1.accept(this);
        printer.print(", ");
        e2.accept(this);
        printer.print(")");
    }

    protected void emitCall(String s, String e1, Expr e2, Expr e3) {
        printer.print(s + "(" + e1 + ", ");
        e2.accept(this);
        printer.print(", ");
        e3.accept(this);
        printer.print(")");
    }

        public void visit(BitExpr e) {
            if (e.expr.isMap()) {
                MapExpr me = (MapExpr)e.expr;
                MapRep mr = getMapRep(me.mapname.image);
                mr.generateBitRead(me.index, e.bit);
            } else {
                if (e.bit.isLiteral()) {
                    int mask = getSingleBitMask(((Literal.IntExpr)e.bit).value);
                    printer.print("((");
                    inner(e.expr, Expr.PREC_A_ADD);
                    printer.print(" & " + mask + ") != 0");
                    printer.print(")");
                } else {
                    emitCall("Arithmetic.getBit", e.expr, e.bit);
                }
            }
        }

        public void visit(BitRangeExpr e) {
            int mask = getBitRangeMask(e.low_bit, e.high_bit);
            int low = e.low_bit;
            if (low != 0) {
                printer.print("(");
                emitBinOp(e.operand, ">>", Expr.PREC_A_SHIFT, low);
                printer.print(" & 0x" + StringUtil.toHex(mask, 8) + ")");
            } else {
                emitAnd(e.operand, mask);
            }
        }

    public void visit(Logical.AndExpr e) {
        binop("&&", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.OrExpr e) {
        binop("||", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.XorExpr e) {
        emitCall(getMethod("xor"), e.left, e.right);
    }

    public void visit(Arith.XorExpr e) {
        binop("^", e.left, e.right, e.getPrecedence());
    }


    protected int getSingleBitMask(int bit) {
        return 1 << bit;
    }

    protected int getSingleInverseBitMask(int bit) {
        return ~(1 << bit);
    }

    protected int getBitRangeMask(int low, int high) {
        return (0xffffffff >>> (31 - (high - low)));
    }

    protected int getInverseBitRangeMask(int low, int high) {
        return ~getBitRangeMask(low, high);
    }

}
