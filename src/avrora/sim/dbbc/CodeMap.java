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

package avrora.sim.dbbc;

import avrora.core.Instr;
import avrora.core.InstrVisitor;
import avrora.core.isdl.CodeRegion;
import avrora.core.isdl.ast.*;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Ben L. Titzer
 */
public class CodeMap {

    protected final static CodeBuilder builder = new CodeBuilder();
    protected final static HashMap codeMap = new HashMap();

    public static CodeRegion getCodeForInstr(int addr, Instr i) {
        builder.nextPC = addr + i.getSize();
        i.accept(builder);
        return builder.result;
    }

    protected static class CodeBuilder implements InstrVisitor {
        CodeRegion result;
        int nextPC;

//--BEGIN CODEBUILDER GENERATOR--
        public void visit(Instr.ADC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("uregs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("uregs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new CallExpr("bit", tolist1(new VarExpr("C"))));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.AddExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "int", new BitRangeExpr(new VarExpr("tmp_0"), 0, 3));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "int", new BitRangeExpr(new VarExpr("tmp_1"), 0, 3));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new BitExpr(new Arith.BinOp.AddExpr(new Arith.BinOp.AddExpr(new VarExpr("tmp_4"), new VarExpr("tmp_5")), new VarExpr("tmp_2")), new Literal.IntExpr(4)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(8)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new VarExpr("tmp_7")), new Logical.UnOp.NotExpr(new VarExpr("tmp_8"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_6")), new Logical.UnOp.NotExpr(new VarExpr("tmp_7"))), new VarExpr("tmp_8"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_9"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ADD i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("uregs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("uregs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Literal.IntExpr(0));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.AddExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "int", new BitRangeExpr(new VarExpr("tmp_0"), 0, 3));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "int", new BitRangeExpr(new VarExpr("tmp_1"), 0, 3));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new BitExpr(new Arith.BinOp.AddExpr(new Arith.BinOp.AddExpr(new VarExpr("tmp_4"), new VarExpr("tmp_5")), new VarExpr("tmp_2")), new Literal.IntExpr(4)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(8)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new VarExpr("tmp_7")), new Logical.UnOp.NotExpr(new VarExpr("tmp_8"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_6")), new Logical.UnOp.NotExpr(new VarExpr("tmp_7"))), new VarExpr("tmp_8"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_9"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ADIW i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("wregs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Arith.BinOp.AddExpr(new VarExpr("tmp_0"), new Literal.IntExpr(i.imm1)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(15)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(15)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_2")), new VarExpr("tmp_3")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_2"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_3")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new BitRangeExpr(new VarExpr("tmp_1"), 0, 15), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_1"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.AND i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AndExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ANDI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AndExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ASR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "byte", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.ShiftRightExpr(new Arith.BinOp.AndExpr(new VarExpr("tmp_1"), new Literal.IntExpr(255)), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new VarBitAssignStmt("tmp_3", new Literal.IntExpr(7), new VarExpr("tmp_2"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_2"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("C")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_4"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BCLR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapBitAssignStmt("ioregs", new VarExpr("SREG"), new Literal.IntExpr(i.imm1), new Literal.BoolExpr(false));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BLD i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapBitAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new Literal.IntExpr(i.imm1), new VarExpr("T"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRBC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm2));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new BitExpr(new MapExpr("ioregs", new VarExpr("SREG")), new Literal.IntExpr(i.imm1))), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRBS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm2));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new BitExpr(new MapExpr("ioregs", new VarExpr("SREG")), new Literal.IntExpr(i.imm1)), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRCC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new VarExpr("C")), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRCS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new VarExpr("C"), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BREAK i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new CallStmt("stop", new LinkedList());
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BREQ i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new VarExpr("Z"), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRGE i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new VarExpr("S")), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRHC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new VarExpr("H")), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRHS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new VarExpr("H"), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRID i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new VarExpr("I")), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRIE i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new VarExpr("I"), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRLO i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new VarExpr("C"), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRLT i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new VarExpr("S"), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRMI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new VarExpr("N"), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRNE i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new VarExpr("Z")), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRPL i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new VarExpr("N")), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRSH i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new VarExpr("C")), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRTC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new VarExpr("T")), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRTS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new VarExpr("T"), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRVC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new VarExpr("V")), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BRVS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new VarExpr("tmp_0"));
            list1.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new IfStmt(new VarExpr("V"), list1, list2);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BSET i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapBitAssignStmt("ioregs", new VarExpr("SREG"), new Literal.IntExpr(i.imm1), new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.BST i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("T", new BitExpr(new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())), new Literal.IntExpr(i.imm1)));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CALL i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(nextPC));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("tmp_0", new Arith.BinOp.DivExpr(new VarExpr("tmp_0"), new Literal.IntExpr(2)));
            list0.addLast(stmt);
            stmt = new CallStmt("pushByte", tolist1(new CallExpr("low", tolist1(new VarExpr("tmp_0")))));
            list0.addLast(stmt);
            stmt = new CallStmt("pushByte", tolist1(new CallExpr("high", tolist1(new VarExpr("tmp_0")))));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CBI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapBitAssignStmt("ioregs", new Literal.IntExpr(i.imm1), new Literal.IntExpr(i.imm2), new Literal.BoolExpr(false));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CBR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Arith.UnOp.CompExpr(new Literal.IntExpr(i.imm1)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AndExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CLC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CLH i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CLI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new CallStmt("disableInterrupts", new LinkedList());
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CLN i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CLR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new CallExpr("low", tolist1(new Literal.IntExpr(0))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CLS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CLT i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("T", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CLV i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CLZ i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.COM i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Arith.BinOp.SubExpr(new Literal.IntExpr(255), new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber()))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_0"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new CallExpr("low", tolist1(new VarExpr("tmp_0"))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CP i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Literal.IntExpr(0));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.SubExpr(new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_7")), new VarExpr("tmp_8")), new Logical.BinOp.AndExpr(new VarExpr("tmp_8"), new VarExpr("tmp_9"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_9"), new Logical.UnOp.NotExpr(new VarExpr("tmp_7")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new Logical.BinOp.AndExpr(new VarExpr("tmp_5"), new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new Logical.UnOp.NotExpr(new VarExpr("tmp_4")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_6"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_4"), new Logical.UnOp.NotExpr(new VarExpr("tmp_5"))), new Logical.UnOp.NotExpr(new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new VarExpr("tmp_6"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_10", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CPC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new CallExpr("bit", tolist1(new VarExpr("C"))));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.SubExpr(new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_7")), new VarExpr("tmp_8")), new Logical.BinOp.AndExpr(new VarExpr("tmp_8"), new VarExpr("tmp_9"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_9"), new Logical.UnOp.NotExpr(new VarExpr("tmp_7")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new Logical.BinOp.AndExpr(new VarExpr("tmp_5"), new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new Logical.UnOp.NotExpr(new VarExpr("tmp_4")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_6"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.AndExpr(new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)), new VarExpr("Z")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_4"), new Logical.UnOp.NotExpr(new VarExpr("tmp_5"))), new Logical.UnOp.NotExpr(new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new VarExpr("tmp_6"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_10", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CPI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Literal.IntExpr(0));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.SubExpr(new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_7")), new VarExpr("tmp_8")), new Logical.BinOp.AndExpr(new VarExpr("tmp_8"), new VarExpr("tmp_9"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_9"), new Logical.UnOp.NotExpr(new VarExpr("tmp_7")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new Logical.BinOp.AndExpr(new VarExpr("tmp_5"), new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new Logical.UnOp.NotExpr(new VarExpr("tmp_4")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_6"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_4"), new Logical.UnOp.NotExpr(new VarExpr("tmp_5"))), new Logical.UnOp.NotExpr(new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new VarExpr("tmp_6"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_10", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.CPSE i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new VarExpr("tmp_0"));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new VarExpr("tmp_1"));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "int", new Literal.IntExpr(0));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "int", new Arith.BinOp.SubExpr(new Arith.BinOp.SubExpr(new VarExpr("tmp_2"), new VarExpr("tmp_3")), new VarExpr("tmp_4")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_5"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "boolean", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_10", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_11", "boolean", new BitExpr(new VarExpr("tmp_5"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_9")), new VarExpr("tmp_10")), new Logical.BinOp.AndExpr(new VarExpr("tmp_10"), new VarExpr("tmp_11"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_11"), new Logical.UnOp.NotExpr(new VarExpr("tmp_9")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_6")), new VarExpr("tmp_7")), new Logical.BinOp.AndExpr(new VarExpr("tmp_7"), new VarExpr("tmp_8"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_8"), new Logical.UnOp.NotExpr(new VarExpr("tmp_6")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_8"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_5"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new Logical.UnOp.NotExpr(new VarExpr("tmp_7"))), new Logical.UnOp.NotExpr(new VarExpr("tmp_8"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_6")), new VarExpr("tmp_7")), new VarExpr("tmp_8"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_12", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_5"))));
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_13", "int", new MapExpr("isize", new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new Arith.BinOp.AddExpr(new Literal.IntExpr(nextPC), new VarExpr("tmp_13")));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(2)));
            list2.addLast(stmt);
            LinkedList list3 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list3.addLast(stmt);
            stmt = new IfStmt(new Logical.BinOp.EquExpr(new VarExpr("tmp_13"), new Literal.IntExpr(4)), list2, list3);
            list1.addLast(stmt);
            LinkedList list4 = new LinkedList();
            stmt = new IfStmt(new Logical.BinOp.EquExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), list1, list4);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.DEC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("uregs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "byte", new CallExpr("low", tolist1(new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new Literal.IntExpr(1)))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new VarExpr("tmp_1"), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.EquExpr(new VarExpr("tmp_0"), new Literal.IntExpr(128)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_1"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.EICALL i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.EIJMP i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ELPM i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("wregs", new Literal.IntExpr(30)));
            list0.addLast(stmt);
            stmt = new VarBitRangeAssignStmt("tmp_0", 16, 23, new MapExpr("ioregs", new VarExpr("RAMPZ")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(0), new MapExpr("program", new VarExpr("tmp_0")));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ELPMD i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("wregs", new Literal.IntExpr(30)));
            list0.addLast(stmt);
            stmt = new VarBitRangeAssignStmt("tmp_0", 16, 23, new MapExpr("ioregs", new VarExpr("RAMPZ")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("program", new VarExpr("tmp_0")));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ELPMPI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("wregs", new Literal.IntExpr(30)));
            list0.addLast(stmt);
            stmt = new VarBitRangeAssignStmt("tmp_0", 16, 23, new MapExpr("ioregs", new VarExpr("RAMPZ")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("program", new VarExpr("tmp_0")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(30), new Arith.BinOp.AddExpr(new VarExpr("tmp_0"), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.EOR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "byte", new CallExpr("low", tolist1(new Arith.BinOp.XorExpr(new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())), new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber()))))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new VarExpr("tmp_0"), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_0"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.FMUL i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Arith.BinOp.ShiftLeftExpr(new Arith.BinOp.MulExpr(new MapExpr("uregs", new Literal.IntExpr(i.r1.getNumber())), new MapExpr("uregs", new Literal.IntExpr(i.r2.getNumber()))), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new BitRangeExpr(new VarExpr("tmp_0"), 0, 15), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(16)));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(0), new CallExpr("low", tolist1(new VarExpr("tmp_0"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(1), new CallExpr("high", tolist1(new VarExpr("tmp_0"))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.FMULS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Arith.BinOp.ShiftLeftExpr(new Arith.BinOp.MulExpr(new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())), new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber()))), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new BitRangeExpr(new VarExpr("tmp_0"), 0, 15), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(16)));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(0), new CallExpr("low", tolist1(new VarExpr("tmp_0"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(1), new CallExpr("high", tolist1(new VarExpr("tmp_0"))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.FMULSU i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Arith.BinOp.ShiftLeftExpr(new Arith.BinOp.MulExpr(new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())), new MapExpr("uregs", new Literal.IntExpr(i.r2.getNumber()))), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new BitRangeExpr(new VarExpr("tmp_0"), 0, 15), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(16)));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(0), new CallExpr("low", tolist1(new VarExpr("tmp_0"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(1), new CallExpr("high", tolist1(new VarExpr("tmp_0"))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ICALL i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(nextPC));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("tmp_0", new Arith.BinOp.DivExpr(new VarExpr("tmp_0"), new Literal.IntExpr(2)));
            list0.addLast(stmt);
            stmt = new CallStmt("pushByte", tolist1(new CallExpr("low", tolist1(new VarExpr("tmp_0")))));
            list0.addLast(stmt);
            stmt = new CallStmt("pushByte", tolist1(new CallExpr("high", tolist1(new VarExpr("tmp_0")))));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("wregs", new Literal.IntExpr(30)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.IJMP i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("wregs", new Literal.IntExpr(30)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Arith.BinOp.MulExpr(new VarExpr("tmp_0"), new Literal.IntExpr(2)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_1"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.IN i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("ioregs", new Literal.IntExpr(i.imm1)));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.INC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("uregs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "byte", new CallExpr("low", tolist1(new Arith.BinOp.AddExpr(new VarExpr("tmp_0"), new Literal.IntExpr(1)))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new VarExpr("tmp_1"), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.EquExpr(new VarExpr("tmp_0"), new Literal.IntExpr(127)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_1"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.JMP i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Arith.BinOp.MulExpr(new VarExpr("tmp_0"), new Literal.IntExpr(2)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_1"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LD i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("sram", new MapExpr("wregs", new Literal.IntExpr(i.r2.getNumber()))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LDD i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("sram", new Arith.BinOp.AddExpr(new MapExpr("wregs", new Literal.IntExpr(i.r2.getNumber())), new Literal.IntExpr(i.imm1))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LDI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new CallExpr("low", tolist1(new Literal.IntExpr(i.imm1))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LDPD i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Arith.BinOp.SubExpr(new MapExpr("wregs", new Literal.IntExpr(i.r2.getNumber())), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("sram", new VarExpr("tmp_0")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(i.r2.getNumber()), new VarExpr("tmp_0"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LDPI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("wregs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("sram", new VarExpr("tmp_0")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(i.r2.getNumber()), new Arith.BinOp.AddExpr(new VarExpr("tmp_0"), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LDS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("sram", new Literal.IntExpr(i.imm1)));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LPM i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(0), new MapExpr("program", new MapExpr("wregs", new Literal.IntExpr(30))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LPMD i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("program", new MapExpr("wregs", new Literal.IntExpr(30))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LPMPI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("wregs", new Literal.IntExpr(30)));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("program", new VarExpr("tmp_0")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(30), new Arith.BinOp.AddExpr(new VarExpr("tmp_0"), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LSL i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "boolean", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.ShiftLeftExpr(new VarExpr("tmp_0"), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new VarBitAssignStmt("tmp_2", new Literal.IntExpr(0), new VarExpr("tmp_1"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(4)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(8)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("C")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.LSR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "boolean", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.ShiftRightExpr(new Arith.BinOp.AndExpr(new VarExpr("tmp_0"), new Literal.IntExpr(255)), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new VarBitAssignStmt("tmp_2", new Literal.IntExpr(7), new VarExpr("tmp_1"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_1"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("C")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.MOV i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.MOVW i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(i.r1.getNumber()), new MapExpr("wregs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.MUL i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Arith.BinOp.MulExpr(new MapExpr("uregs", new Literal.IntExpr(i.r1.getNumber())), new MapExpr("uregs", new Literal.IntExpr(i.r2.getNumber()))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(15)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new BitRangeExpr(new VarExpr("tmp_0"), 0, 15), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(0), new VarExpr("tmp_0"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.MULS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Arith.BinOp.MulExpr(new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())), new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber()))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(15)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new BitRangeExpr(new VarExpr("tmp_0"), 0, 15), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(0), new VarExpr("tmp_0"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.MULSU i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Arith.BinOp.MulExpr(new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())), new MapExpr("uregs", new Literal.IntExpr(i.r2.getNumber()))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(15)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new BitRangeExpr(new VarExpr("tmp_0"), 0, 15), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(0), new VarExpr("tmp_0"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.NEG i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(0));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Literal.IntExpr(0));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.SubExpr(new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_7")), new VarExpr("tmp_8")), new Logical.BinOp.AndExpr(new VarExpr("tmp_8"), new VarExpr("tmp_9"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_9"), new Logical.UnOp.NotExpr(new VarExpr("tmp_7")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new Logical.BinOp.AndExpr(new VarExpr("tmp_5"), new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new Logical.UnOp.NotExpr(new VarExpr("tmp_4")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_6"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_4"), new Logical.UnOp.NotExpr(new VarExpr("tmp_5"))), new Logical.UnOp.NotExpr(new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new VarExpr("tmp_6"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_10", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_10"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.NOP i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.OR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.OrExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ORI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.OrExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.OUT i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("ioregs", new Literal.IntExpr(i.imm1), new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.POP i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new CallExpr("popByte", new LinkedList()));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.PUSH i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new CallStmt("pushByte", tolist1(new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber()))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.RCALL i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(nextPC));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("tmp_0", new Arith.BinOp.DivExpr(new VarExpr("tmp_0"), new Literal.IntExpr(2)));
            list0.addLast(stmt);
            stmt = new CallStmt("pushByte", tolist1(new CallExpr("low", tolist1(new VarExpr("tmp_0")))));
            list0.addLast(stmt);
            stmt = new CallStmt("pushByte", tolist1(new CallExpr("high", tolist1(new VarExpr("tmp_0")))));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_1"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.RET i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "byte", new CallExpr("popByte", new LinkedList()));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "byte", new CallExpr("popByte", new LinkedList()));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.MulExpr(new CallExpr("uword", tolist2(new VarExpr("tmp_1"), new VarExpr("tmp_0"))), new Literal.IntExpr(2)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.RETI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "byte", new CallExpr("popByte", new LinkedList()));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "byte", new CallExpr("popByte", new LinkedList()));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.MulExpr(new CallExpr("uword", tolist2(new VarExpr("tmp_1"), new VarExpr("tmp_0"))), new Literal.IntExpr(2)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_2"));
            list0.addLast(stmt);
            stmt = new CallStmt("enableInterrupts", new LinkedList());
            list0.addLast(stmt);
            stmt = new VarAssignStmt("justReturnedFromInterrupt", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.RJMP i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Arith.BinOp.AddExpr(new Arith.BinOp.MulExpr(new VarExpr("tmp_0"), new Literal.IntExpr(2)), new Literal.IntExpr(nextPC)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new VarExpr("tmp_1"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ROL i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("uregs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "boolean", new VarExpr("C"));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.ShiftLeftExpr(new VarExpr("tmp_0"), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new VarBitAssignStmt("tmp_2", new Literal.IntExpr(0), new VarExpr("tmp_1"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(4)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(8)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("C")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ROR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "boolean", new VarExpr("C"));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.ShiftRightExpr(new Arith.BinOp.AndExpr(new VarExpr("tmp_0"), new Literal.IntExpr(255)), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new VarBitAssignStmt("tmp_2", new Literal.IntExpr(7), new VarExpr("tmp_1"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_1"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("C")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SBC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new CallExpr("bit", tolist1(new VarExpr("C"))));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.SubExpr(new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_7")), new VarExpr("tmp_8")), new Logical.BinOp.AndExpr(new VarExpr("tmp_8"), new VarExpr("tmp_9"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_9"), new Logical.UnOp.NotExpr(new VarExpr("tmp_7")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new Logical.BinOp.AndExpr(new VarExpr("tmp_5"), new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new Logical.UnOp.NotExpr(new VarExpr("tmp_4")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_6"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.AndExpr(new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)), new VarExpr("Z")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_4"), new Logical.UnOp.NotExpr(new VarExpr("tmp_5"))), new Logical.UnOp.NotExpr(new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new VarExpr("tmp_6"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_10", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_10"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SBCI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new CallExpr("bit", tolist1(new VarExpr("C"))));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.SubExpr(new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_7")), new VarExpr("tmp_8")), new Logical.BinOp.AndExpr(new VarExpr("tmp_8"), new VarExpr("tmp_9"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_9"), new Logical.UnOp.NotExpr(new VarExpr("tmp_7")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new Logical.BinOp.AndExpr(new VarExpr("tmp_5"), new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new Logical.UnOp.NotExpr(new VarExpr("tmp_4")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_6"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.AndExpr(new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)), new VarExpr("Z")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_4"), new Logical.UnOp.NotExpr(new VarExpr("tmp_5"))), new Logical.UnOp.NotExpr(new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new VarExpr("tmp_6"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_10", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_10"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SBI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapBitAssignStmt("ioregs", new Literal.IntExpr(i.imm1), new Literal.IntExpr(i.imm2), new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SBIC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("isize", new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new Arith.BinOp.AddExpr(new Literal.IntExpr(nextPC), new VarExpr("tmp_0")));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(2)));
            list2.addLast(stmt);
            LinkedList list3 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list3.addLast(stmt);
            stmt = new IfStmt(new Logical.BinOp.EquExpr(new VarExpr("tmp_0"), new Literal.IntExpr(4)), list2, list3);
            list1.addLast(stmt);
            LinkedList list4 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new BitExpr(new MapExpr("ioregs", new Literal.IntExpr(i.imm1)), new Literal.IntExpr(i.imm2))), list1, list4);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SBIS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("isize", new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new Arith.BinOp.AddExpr(new Literal.IntExpr(nextPC), new VarExpr("tmp_0")));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(2)));
            list2.addLast(stmt);
            LinkedList list3 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list3.addLast(stmt);
            stmt = new IfStmt(new Logical.BinOp.EquExpr(new VarExpr("tmp_0"), new Literal.IntExpr(4)), list2, list3);
            list1.addLast(stmt);
            LinkedList list4 = new LinkedList();
            stmt = new IfStmt(new BitExpr(new MapExpr("ioregs", new Literal.IntExpr(i.imm1)), new Literal.IntExpr(i.imm2)), list1, list4);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SBIW i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("wregs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new Literal.IntExpr(i.imm1)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(15)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(15)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.AndExpr(new VarExpr("tmp_2"), new Logical.UnOp.NotExpr(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_3"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new BitRangeExpr(new VarExpr("tmp_1"), 0, 15), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.AndExpr(new VarExpr("tmp_3"), new Logical.UnOp.NotExpr(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_1"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SBR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Arith.BinOp.OrExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_2"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_2"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_2"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_3"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SBRC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("isize", new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new Arith.BinOp.AddExpr(new Literal.IntExpr(nextPC), new VarExpr("tmp_0")));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(2)));
            list2.addLast(stmt);
            LinkedList list3 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list3.addLast(stmt);
            stmt = new IfStmt(new Logical.BinOp.EquExpr(new VarExpr("tmp_0"), new Literal.IntExpr(4)), list2, list3);
            list1.addLast(stmt);
            LinkedList list4 = new LinkedList();
            stmt = new IfStmt(new Logical.UnOp.NotExpr(new BitExpr(new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())), new Literal.IntExpr(i.imm1))), list1, list4);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SBRS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            LinkedList list1 = new LinkedList();
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("isize", new Literal.IntExpr(nextPC)));
            list1.addLast(stmt);
            stmt = new VarAssignStmt("nextPC", new Arith.BinOp.AddExpr(new Literal.IntExpr(nextPC), new VarExpr("tmp_0")));
            list1.addLast(stmt);
            LinkedList list2 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(2)));
            list2.addLast(stmt);
            LinkedList list3 = new LinkedList();
            stmt = new VarAssignStmt("cyclesConsumed", new Arith.BinOp.AddExpr(new VarExpr("cyclesConsumed"), new Literal.IntExpr(1)));
            list3.addLast(stmt);
            stmt = new IfStmt(new Logical.BinOp.EquExpr(new VarExpr("tmp_0"), new Literal.IntExpr(4)), list2, list3);
            list1.addLast(stmt);
            LinkedList list4 = new LinkedList();
            stmt = new IfStmt(new BitExpr(new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())), new Literal.IntExpr(i.imm1)), list1, list4);
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SEC i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SEH i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SEI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new CallStmt("enableInterrupts", new LinkedList());
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SEN i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SER i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new CallExpr("low", tolist1(new Literal.IntExpr(255))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SES i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SET i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("T", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SEV i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SEZ i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Literal.BoolExpr(true));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SLEEP i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new CallStmt("enterSleepMode", new LinkedList());
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SPM i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.ST i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("sram", new MapExpr("wregs", new Literal.IntExpr(i.r1.getNumber())), new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.STD i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("sram", new Arith.BinOp.AddExpr(new MapExpr("wregs", new Literal.IntExpr(i.r1.getNumber())), new Literal.IntExpr(i.imm1)), new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.STPD i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new Arith.BinOp.SubExpr(new MapExpr("wregs", new Literal.IntExpr(i.r1.getNumber())), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("sram", new VarExpr("tmp_0"), new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_0"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.STPI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("wregs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("sram", new VarExpr("tmp_0"), new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("wregs", new Literal.IntExpr(i.r1.getNumber()), new Arith.BinOp.AddExpr(new VarExpr("tmp_0"), new Literal.IntExpr(1)));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.STS i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new MapAssignStmt("sram", new Literal.IntExpr(i.imm1), new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SUB i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new MapExpr("regs", new Literal.IntExpr(i.r2.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Literal.IntExpr(0));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.SubExpr(new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_7")), new VarExpr("tmp_8")), new Logical.BinOp.AndExpr(new VarExpr("tmp_8"), new VarExpr("tmp_9"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_9"), new Logical.UnOp.NotExpr(new VarExpr("tmp_7")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new Logical.BinOp.AndExpr(new VarExpr("tmp_5"), new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new Logical.UnOp.NotExpr(new VarExpr("tmp_4")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_6"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_4"), new Logical.UnOp.NotExpr(new VarExpr("tmp_5"))), new Logical.UnOp.NotExpr(new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new VarExpr("tmp_6"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_10", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_10"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SUBI i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Literal.IntExpr(i.imm1));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_2", "int", new Literal.IntExpr(0));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_3", "int", new Arith.BinOp.SubExpr(new Arith.BinOp.SubExpr(new VarExpr("tmp_0"), new VarExpr("tmp_1")), new VarExpr("tmp_2")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_4", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_5", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_6", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_7", "boolean", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_8", "boolean", new BitExpr(new VarExpr("tmp_1"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_9", "boolean", new BitExpr(new VarExpr("tmp_3"), new Literal.IntExpr(3)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("H", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_7")), new VarExpr("tmp_8")), new Logical.BinOp.AndExpr(new VarExpr("tmp_8"), new VarExpr("tmp_9"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_9"), new Logical.UnOp.NotExpr(new VarExpr("tmp_7")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("C", new Logical.BinOp.OrExpr(new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new Logical.BinOp.AndExpr(new VarExpr("tmp_5"), new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new VarExpr("tmp_6"), new Logical.UnOp.NotExpr(new VarExpr("tmp_4")))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new VarExpr("tmp_6"));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_3"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Logical.BinOp.OrExpr(new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new VarExpr("tmp_4"), new Logical.UnOp.NotExpr(new VarExpr("tmp_5"))), new Logical.UnOp.NotExpr(new VarExpr("tmp_6"))), new Logical.BinOp.AndExpr(new Logical.BinOp.AndExpr(new Logical.UnOp.NotExpr(new VarExpr("tmp_4")), new VarExpr("tmp_5")), new VarExpr("tmp_6"))));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_10", "byte", new CallExpr("low", tolist1(new VarExpr("tmp_3"))));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new VarExpr("tmp_10"));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.SWAP i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("uregs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_1", "int", new Literal.IntExpr(0));
            list0.addLast(stmt);
            stmt = new VarBitRangeAssignStmt("tmp_1", 0, 3, new BitRangeExpr(new VarExpr("tmp_0"), 4, 7));
            list0.addLast(stmt);
            stmt = new VarBitRangeAssignStmt("tmp_1", 4, 7, new BitRangeExpr(new VarExpr("tmp_0"), 0, 3));
            list0.addLast(stmt);
            stmt = new MapAssignStmt("regs", new Literal.IntExpr(i.r1.getNumber()), new CallExpr("low", tolist1(new VarExpr("tmp_1"))));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.TST i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            stmt = new DeclStmt("tmp_0", "int", new MapExpr("regs", new Literal.IntExpr(i.r1.getNumber())));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("V", new Literal.BoolExpr(false));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("Z", new Logical.BinOp.EquExpr(new CallExpr("low", tolist1(new VarExpr("tmp_0"))), new Literal.IntExpr(0)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("N", new BitExpr(new VarExpr("tmp_0"), new Literal.IntExpr(7)));
            list0.addLast(stmt);
            stmt = new VarAssignStmt("S", new Logical.BinOp.XorExpr(new VarExpr("N"), new VarExpr("V")));
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        public void visit(Instr.WDR i) {
            Stmt stmt;
            LinkedList list0 = new LinkedList();
            stmt = new CommentStmt("===== " + i.getName() + ' ' + i.getOperands() + " ==========================================");
            list0.addLast(stmt);
            result = new CodeRegion(new LinkedList(), list0);
        }

        protected LinkedList tolist1(Object o1) {
            LinkedList retlist = new LinkedList();
            retlist.addLast(o1);
            return retlist;
        }

        protected LinkedList tolist2(Object o1, Object o2) {
            LinkedList retlist = new LinkedList();
            retlist.addLast(o1);
            retlist.addLast(o2);
            return retlist;
        }
//--END CODEBUILDER GENERATOR--
    }

}
