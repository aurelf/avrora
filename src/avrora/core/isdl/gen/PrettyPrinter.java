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

import avrora.core.isdl.ast.*;
import avrora.core.isdl.gen.PrettyPrinter.MapRep;
import avrora.core.isdl.parser.Token;
import avrora.util.Printer;

import java.util.Iterator;
import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class PrettyPrinter implements StmtVisitor, CodeVisitor {
    protected final Printer printer;

    public void visit(CallExpr e) {
        printer.print(getMethod(e.method.image) + '(');
        visitExprList(e.args);
        printer.print(")");
    }

    public void visit(MapExpr e) {
        MapRep mr = getMapRep(e.mapname.image);
        mr.generateRead(e.index);
    }

    public void visit(ConversionExpr e) {
        inner(e.expr, Expr.PREC_TERM);
        printer.print(':' + e.typename.image);
    }

    public void visit(VarExpr e) {
        printer.print(getVariable(e.variable));
    }

    public void visit(MapAssignStmt s) {
        MapRep mr = getMapRep(s.mapname.image);
        mr.generateWrite(s.index, s.expr);
    }

    public void visit(MapBitAssignStmt s) {
        MapRep mr = getMapRep(s.mapname.image);
        mr.generateBitWrite(s.index, s.bit, s.expr);
    }

    public void visit(MapBitRangeAssignStmt s) {
        MapRep mr = getMapRep(s.mapname.image);
        mr.generateBitRangeWrite(s.index, s.low_bit, s.high_bit, s.expr);
    }

    protected String getMethod(String s) {
        return s;
    }

    protected abstract class MapRep {
        public abstract void generateWrite(Expr ind, Expr val);

        public abstract void generateBitWrite(Expr ind, Expr b, Expr val);

        public abstract void generateRead(Expr ind);

        public abstract void generateBitRead(Expr ind, Expr b);

        public abstract void generateBitRangeWrite(Expr ind, int l, int h, Expr val);
    }

    protected class DefaultMapRep extends MapRep {
        String name;

        DefaultMapRep(String n) {
            name = n;
        }

        public void generateWrite(Expr ind, Expr val) {
            printer.print('$' + name + '(');
            ind.accept(PrettyPrinter.this);
            printer.print(") = ");
            val.accept(PrettyPrinter.this);
            printer.println(";");
        }

        public void generateBitWrite(Expr ind, Expr b, Expr val) {
            printer.print('$' + name + '(');
            ind.accept(PrettyPrinter.this);
            printer.print(")[");
            b.accept(PrettyPrinter.this);
            printer.print("] = ");
            val.accept(PrettyPrinter.this);
            printer.println(";");
        }

        public void generateRead(Expr ind) {
            printer.print('$' + name + '(');
            ind.accept(PrettyPrinter.this);
            printer.print(")");
        }

        public void generateBitRead(Expr ind, Expr b) {
            printer.print('$' + name + '(');
            ind.accept(PrettyPrinter.this);
            printer.print(")[");
            b.accept(PrettyPrinter.this);
            printer.print("]");
        }

        public void generateBitRangeWrite(Expr ind, int l, int h, Expr val) {
            printer.print('$' + name + '(');
            ind.accept(PrettyPrinter.this);
            printer.print(")[" + l + ':' + h + "] = ");
            val.accept(PrettyPrinter.this);
            printer.println(";");
        }
    }


    public PrettyPrinter(Printer p) {
        printer = p;
    }

    public void visit(CallStmt s) {
        printer.print(getMethod(s.method.image) + '(');
        visitExprList(s.args);
        printer.println(");");
    }

    public void visit(CommentStmt s) {
        printer.println(s.toString());
    }

    public void visit(IfStmt s) {
        printer.print("if ( ");
        s.cond.accept(this);
        printer.print(" ) ");
        printer.startblock();
        visitStmtList(s.trueBranch);
        printer.endblock();
        printer.startblock("else");
        visitStmtList(s.falseBranch);
        printer.endblock();
    }

    public void visit(ReturnStmt s) {
        printer.print("return ");
        s.expr.accept(this);
        printer.println(";");
    }

    public void visit(VarBitAssignStmt s) {
        String var = getVariable(s.variable);
        printer.print(var + '[');
        s.bit.accept(this);
        printer.print("] = ");
        s.expr.accept(this);
        printer.println(";");
    }

    public void visit(VarBitRangeAssignStmt s) {
        String var = getVariable(s.variable);
        printer.print(var + '[' + s.low_bit + ':' + s.high_bit + "] = ");
        s.expr.accept(this);
        printer.println(";");
    }

    protected String getVariable(Token v) {
        return v.toString();
    }

    protected MapRep getMapRep(String name) {
        return new DefaultMapRep(name);
    }

    public void visit(DeclStmt s) {
        printer.print(s.type.image + ' ' + getVariable(s.name) + " = ");
        s.init.accept(this);
        printer.println(";");
    }

    public void visit(VarAssignStmt s) {
        String var = getVariable(s.variable);
        printer.print(var + " = ");
        s.expr.accept(this);
        printer.println(";");
    }

    protected void inner(Expr e, int outerPrecedence) {
        if (e.getPrecedence() < outerPrecedence) {
            printer.print("(");
            e.accept(this);
            printer.print(")");
        } else {
            e.accept(this);
        }
    }

    protected void binop(String op, Expr left, Expr right, int p) {
        inner(left, p);
        printer.print(' ' + op + ' ');
        inner(right, p);
    }

    public void visit(Arith.AddExpr e) {
        binop("+", e.left, e.right, e.getPrecedence());
    }

    public void visit(Arith.AndExpr e) {
        binop("&", e.left, e.right, e.getPrecedence());
    }

    public void visit(Arith.CompExpr e) {
        printer.print(e.operation);
        inner(e.operand, e.getPrecedence());
    }

    public void visit(Arith.DivExpr e) {
        binop("/", e.left, e.right, e.getPrecedence());
    }

    public void visit(Arith.MulExpr e) {
        binop("*", e.left, e.right, e.getPrecedence());
    }

    public void visit(Arith.NegExpr e) {
        printer.print(e.operation);
        inner(e.operand, e.getPrecedence());
    }

    public void visit(Arith.OrExpr e) {
        binop("|", e.left, e.right, e.getPrecedence());
    }

    public void visit(Arith.ShiftLeftExpr e) {
        binop("<<", e.left, e.right, e.getPrecedence());
    }

    public void visit(Arith.ShiftRightExpr e) {
        binop(">>", e.left, e.right, e.getPrecedence());
    }

    public void visit(Arith.SubExpr e) {
        binop("-", e.left, e.right, e.getPrecedence());
    }

    public void visit(Arith.XorExpr e) {
        binop("^", e.left, e.right, e.getPrecedence());
    }

    public void visit(Literal.BoolExpr e) {
        printer.print(e.toString());
    }

    public void visit(Literal.IntExpr e) {
        printer.print(e.toString());
    }

    public void visit(Logical.AndExpr e) {
        binop("and", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.EquExpr e) {
        binop("==", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.GreaterEquExpr e) {
        binop(">=", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.GreaterExpr e) {
        binop(">", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.LessEquExpr e) {
        binop("<=", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.LessExpr e) {
        binop("<", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.NequExpr e) {
        binop("!=", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.NotExpr e) {
        printer.print("!");
        inner(e.operand, e.getPrecedence());
    }

    public void visit(Logical.OrExpr e) {
        binop("or", e.left, e.right, e.getPrecedence());
    }

    public void visit(Logical.XorExpr e) {
        binop("xor", e.left, e.right, e.getPrecedence());
    }

    public void visitStmtList(List l) {
        Iterator i = l.iterator();
        // visit all the statements in the block
        while (i.hasNext()) {
            Stmt t = (Stmt)i.next();
            t.accept(this);
        }
    }

    public void visit(BitExpr e) {
        if (e.expr.isMap()) {
            MapExpr me = (MapExpr)e.expr;
            MapRep mr = getMapRep(me.mapname.image);
            mr.generateBitRead(me.index, e.bit);
        } else {
            if (e.expr.getPrecedence() < e.getPrecedence()) {
                printer.print("(");
                e.expr.accept(this);
                printer.print(")");
            } else {
                e.expr.accept(this);
            }
            printer.print("[");
            e.bit.accept(this);
            printer.print("]");
        }
    }

    public void visit(BitRangeExpr e) {
        if (e.operand.getPrecedence() < e.getPrecedence()) {
            printer.print("(");
            e.operand.accept(this);
            printer.print(")");
        } else {
            e.operand.accept(this);
        }
        printer.print("[" + e.low_bit + ':' + e.high_bit + ']');
    }

    protected void visitExprList(List l) {
        Iterator i = l.iterator();
        while (i.hasNext()) {
            Expr a = (Expr)i.next();
            a.accept(this);
            if (i.hasNext()) printer.print(", ");
        }
    }


}
