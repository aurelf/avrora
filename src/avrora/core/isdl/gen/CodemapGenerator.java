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

package avrora.core.isdl.gen;

import avrora.core.Register;
import avrora.core.isdl.Architecture;
import avrora.core.isdl.CodeRegion;
import avrora.core.isdl.InstrDecl;
import avrora.core.isdl.ast.*;
import avrora.core.isdl.parser.Token;
import avrora.util.Printer;
import avrora.util.StringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class CodemapGenerator implements Architecture.InstrVisitor {
    private final Architecture architecture;
    private final Printer printer;

    HashMap registerMap = new HashMap();

    public CodemapGenerator(Architecture a, Printer p) {
        architecture = a;
        printer = p;
    }

    public void generate() {
        initializeRegisterMap();
        printer.indent();
        architecture.accept(this);
        generateHelperMethods();
        printer.unindent();
    }

    public void initializeRegisterMap() {
        // TODO: this is not portable
        for (int cntr = 0; cntr < 32; cntr++) {
            registerMap.put("R" + cntr, new Integer(cntr));
        }
        registerMap.put("RX", new Integer(Register.X.getNumber()));
        registerMap.put("RY", new Integer(Register.Y.getNumber()));
        registerMap.put("RZ", new Integer(Register.Z.getNumber()));
    }

    static class Operand {
        boolean integer;
        String name;
    }

    public void visit(InstrDecl d) {
        printer.println("public void visit(" + d.getClassName() + " i) {");
        printer.indent();
        printer.println("Stmt stmt;");

        sgen.lastblock = -1;
        egen.operands = new HashMap();

        int regcount = 0;
        int immcount = 0;
        Iterator i = d.getOperandIterator();
        while (i.hasNext()) {
            Operand op = new Operand();
            CodeRegion.Operand o = (CodeRegion.Operand)i.next();
            if (o.isRegister()) {
                op.name = "r" + (++regcount);
                op.integer = false;
            } else {
                op.name = "imm" + (++immcount);
                op.integer = true;
            }
            egen.operands.put(o.name.toString(), op);
        }

        String bname = generateBlock(d.getCode(), "\"===== \"+i.getName()+\" \"+i.getOperands()+" +
                "\" ==========================================\"");

        printer.print("result = new CodeRegion(new LinkedList(), " + bname + ");");
        printer.unindent();
        printer.nextln();
        printer.println("}");
    }

    public final ExprGenerator egen = new ExprGenerator();
    public final StmtGenerator sgen = new StmtGenerator();

    protected int biggestList;

    protected String generateBlock(List stmts, String comment) {
        String lname = "list" + (++sgen.lastblock);

        printer.println("LinkedList " + lname + " = new LinkedList();");

        if (comment != null) {
            printer.println("stmt = new CommentStmt(" + comment + ");");
            printer.println(lname + ".addLast(stmt);");
        }

        Iterator i = stmts.iterator();
        while (i.hasNext()) {
            Stmt s = (Stmt)i.next();
            s.accept(sgen);
            printer.println(lname + ".addLast(stmt);");
        }

        return lname;
    }

    protected void generateHelperMethods() {
        for (int cntr = 1; cntr <= biggestList; cntr++) {
            printer.print("protected LinkedList tolist" + cntr + '(');
            for (int var = 1; var <= cntr; var++) {
                printer.print("Object o" + var);
                if (var < cntr) printer.print(", ");
            }
            printer.println(") {");
            printer.indent();
            printer.println("LinkedList retlist = new LinkedList();");
            for (int var = 1; var <= cntr; var++) {
                printer.println("retlist.addLast(o" + var + ");");
            }
            printer.println("return retlist;");
            printer.unindent();
            printer.nextln();
            printer.println("}");
        }
    }

    protected void generateExprList(List exprs) {
        int len = exprs.size();
        String hname = "tolist" + len;
        if (len > biggestList) biggestList = len;
        if (len == 0) {
            printer.print("new LinkedList()");
        } else {
            printer.print(hname + '(');
            Iterator i = exprs.iterator();
            while (i.hasNext()) {
                Expr e = (Expr)i.next();
                e.accept(egen);
                if (i.hasNext())
                    printer.print(", ");
            }
            printer.print(")");
        }
    }

    protected class StmtGenerator implements StmtVisitor {
        int lastblock;

        public void visit(CallStmt s) {
            printer.print("stmt = new CallStmt(");
            generate(s.method);
            printer.print(", ");
            generateExprList(s.args);
            printer.println(");");
        }

        public void visit(CommentStmt s) {
            printer.println(s.toString());
        }

        public void visit(DeclStmt s) {
            generate("DeclStmt", s.name, s.type, s.init);
        }

        public void visit(IfStmt s) {
            String ltrue = generateBlock(s.trueBranch, null);
            String lfalse = generateBlock(s.falseBranch, null);
            generate("IfStmt", s.cond, ltrue, lfalse);
        }

        public void visit(MapAssignStmt s) {
            generate("MapAssignStmt", s.mapname, s.index, s.expr);
        }

        public void visit(MapBitAssignStmt s) {
            generate("MapBitAssignStmt", s.mapname, s.index, s.bit, s.expr);
        }

        public void visit(MapBitRangeAssignStmt s) {
            printer.print("stmt = new " + "MapBitRangeAssignStmt" + '(');
            generate(s.mapname);
            printer.print(", ");
            generate(s.index);
            printer.print(", ");
            generate(s.low_bit);
            printer.print(", ");
            generate(s.high_bit);
            printer.print(", ");
            generate(s.expr);
            printer.println(");");
        }

        public void visit(ReturnStmt s) {
            printer.print("stmt = new ReturnStmt(");
            generate(s.expr);
            printer.println(");");
        }

        public void visit(VarAssignStmt s) {
            generate("VarAssignStmt", s.variable, s.expr);
        }

        public void visit(VarBitAssignStmt s) {
            generate("VarBitAssignStmt", s.variable, s.bit, s.expr);
        }

        public void visit(VarBitRangeAssignStmt s) {
            printer.print("stmt = new " + "VarBitRangeAssignStmt" + '(');
            generate(s.variable);
            printer.print(", ");
            generate(s.low_bit);
            printer.print(", ");
            generate(s.high_bit);
            printer.print(", ");
            generate(s.expr);
            printer.println(");");
        }

        private void generate(String clname, Object o1, Object o2, Object o3, Object o4) {
            printer.print("stmt = new " + clname + '(');
            generate(o1);
            printer.print(", ");
            generate(o2);
            printer.print(", ");
            generate(o3);
            printer.print(", ");
            generate(o4);
            printer.println(");");
        }

        private void generate(String clname, Object o1, Object o2, Object o3) {
            printer.print("stmt = new " + clname + '(');
            generate(o1);
            printer.print(", ");
            generate(o2);
            printer.print(", ");
            generate(o3);
            printer.println(");");
        }

        private void generate(String clname, Object o1, Object o2) {
            printer.print("stmt = new " + clname + '(');
            generate(o1);
            printer.print(", ");
            generate(o2);
            printer.println(");");
        }

        private void generate(Object o) {
            if (o instanceof Expr)
                ((Expr)o).accept(egen);
            else if (o instanceof Token)
                printer.print(StringUtil.quote(o));
            else
                printer.print(o.toString());
        }

        private void generate(int i) {
            printer.print("" + i);
        }
    }

    protected class ExprGenerator implements CodeVisitor {
        HashMap operands;

        public void generate(Arith.BinOp e, String clname) {
            printer.print("new Arith.BinOp." + clname + '(');
            e.left.accept(this);
            printer.print(", ");
            e.right.accept(this);
            printer.print(")");
        }

        private void generate(Arith.UnOp e, String clname) {
            printer.print("new Arith.UnOp." + clname + '(');
            e.operand.accept(this);
            printer.print(")");
        }

        public void generate(Logical.BinOp e, String clname) {
            printer.print("new Logical.BinOp." + clname + '(');
            e.left.accept(this);
            printer.print(", ");
            e.right.accept(this);
            printer.print(")");
        }

        private void generate(Logical.UnOp e, String clname) {
            printer.print("new Logical.UnOp." + clname + '(');
            e.operand.accept(this);
            printer.print(")");
        }

        //- Begin real visitor code

        public void visit(Arith.AddExpr e) {
            generate(e, "AddExpr");
        }

        public void visit(Arith.AndExpr e) {
            generate(e, "AndExpr");
        }

        public void visit(Arith.CompExpr e) {
            generate(e, "CompExpr");
        }

        public void visit(Arith.DivExpr e) {
            generate(e, "DivExpr");
        }

        public void visit(Arith.MulExpr e) {
            generate(e, "MulExpr");
        }

        public void visit(Arith.NegExpr e) {
            generate(e, "NegExpr");
        }

        public void visit(Arith.OrExpr e) {
            generate(e, "OrExpr");
        }

        public void visit(Arith.ShiftLeftExpr e) {
            generate(e, "ShiftLeftExpr");
        }

        public void visit(Arith.ShiftRightExpr e) {
            generate(e, "ShiftRightExpr");
        }

        public void visit(Arith.SubExpr e) {
            generate(e, "SubExpr");
        }

        public void visit(Arith.XorExpr e) {
            generate(e, "XorExpr");
        }

        public void visit(BitExpr e) {
            printer.print("new BitExpr(");
            e.expr.accept(this);
            printer.print(", ");
            e.bit.accept(this);
            printer.print(")");
        }

        public void visit(BitRangeExpr e) {
            printer.print("new BitRangeExpr(");
            e.operand.accept(this);
            printer.print(", " + e.low_bit + ", " + e.high_bit + ')');
        }

        public void visit(CallExpr e) {
            printer.print("new CallExpr(" + StringUtil.quote(e.method) + ", ");
            generateExprList(e.args);
            printer.print(")");
        }

        public void visit(ConversionExpr e) {
            printer.print("new ConversionExpr(");
            e.expr.accept(this);
            printer.print(", " + StringUtil.quote(e.typename) + ", ");
            printer.print(")");
        }

        public void visit(Literal.BoolExpr e) {
            printer.print("new Literal.BoolExpr(" + e.value + ')');
        }

        public void visit(Literal.IntExpr e) {
            printer.print("new Literal.IntExpr(" + e.value + ')');
        }

        public void visit(Logical.AndExpr e) {
            generate(e, "AndExpr");
        }

        public void visit(Logical.EquExpr e) {
            generate(e, "EquExpr");
        }

        public void visit(Logical.GreaterEquExpr e) {
            generate(e, "GreaterEquExpr");
        }

        public void visit(Logical.GreaterExpr e) {
            generate(e, "GreaterExpr");
        }

        public void visit(Logical.LessEquExpr e) {
            generate(e, "LessEquExpr");
        }

        public void visit(Logical.LessExpr e) {
            generate(e, "LessExpr");
        }

        public void visit(Logical.NequExpr e) {
            generate(e, "NequExpr");
        }

        public void visit(Logical.NotExpr e) {
            generate(e, "NotExpr");
        }

        public void visit(Logical.OrExpr e) {
            generate(e, "OrExpr");
        }

        public void visit(Logical.XorExpr e) {
            generate(e, "XorExpr");
        }

        public void visit(MapExpr e) {
            printer.print("new MapExpr(" + StringUtil.quote(e.mapname) + ", ");
            e.index.accept(this);
            printer.print(")");
        }

        public void visit(VarExpr e) {
            String name = e.variable.toString();
            Operand op = (Operand)operands.get(name);
            if (op != null) {
                generateOperandUse(op);
            } else {
                Integer i = getRegister(name);
                if (i != null) {
                    printer.print("new Literal.IntExpr(" + i.intValue() + ')');
                } else
                    generateVarUse(e);
            }
        }

        private void generateVarUse(VarExpr e) {
            if ("nextPC".equals(e.variable.toString()))
                printer.print("new Literal.IntExpr(nextPC)");
            else
                printer.print("new VarExpr(" + StringUtil.quote(e.variable) + ')');
        }

        private void generateOperandUse(Operand op) {
            if (op.integer) {
                printer.print("new Literal.IntExpr(i." + op.name + ')');
            } else {
                printer.print("new Literal.IntExpr(i." + op.name + ".getNumber())");
            }
        }


        protected Integer getRegister(String name) {
            return (Integer)registerMap.get(name);
        }

    }
}
