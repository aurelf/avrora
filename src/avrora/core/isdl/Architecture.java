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

package avrora.core.isdl;

import avrora.Avrora;
import avrora.core.isdl.ast.*;
import avrora.core.isdl.parser.ISDLParserConstants;
import avrora.core.isdl.parser.Token;
import avrora.util.Printer;
import avrora.util.StringUtil;
import avrora.util.Verbose;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>Architecture</code> class represents a collection of
 * instructions, encodings, operands, and subroutines that describe
 * an instruction set architecture.
 *
 * @author Ben L. Titzer
 */
public class Architecture {

    Verbose.Printer printer = Verbose.getVerbosePrinter("isdl");

    public final Token name;

    HashMap subroutineMap;
    HashMap instructionMap;
    HashMap operandMap;
    HashMap encodingMap;

    List subroutines;
    List instructions;
    List operands;
    List encodings;

    public interface InstrVisitor {
        public void visit(InstrDecl d);
    }

    public interface SubroutineVisitor {
        public void visit(SubroutineDecl d);
    }

    public interface OperandVisitor {
        public void visit(OperandDecl d);
    }

    public interface EncodingVisitor {
        public void visit(EncodingDecl d);
    }

    /**
     * The <code>Visitor</code> class represents a visitor over the elements of
     * the architecture description. It has methods to visit each subroutine,
     * instruction, operand, and encoding declared in the specification.
     */
    public interface Visitor extends InstrVisitor, SubroutineVisitor, OperandVisitor, EncodingVisitor {
    }

    public Architecture(Token n) {
        name = n;

        subroutineMap = new HashMap();
        instructionMap = new HashMap();
        operandMap = new HashMap();
        encodingMap = new HashMap();

        subroutines = new LinkedList();
        instructions = new LinkedList();
        operands = new LinkedList();
        encodings = new LinkedList();
    }

    public void process() {
        processEncodings();
        processSubroutines();
        processInstructions();
    }

    private void processEncodings() {
        Iterator i = getEncodingIterator();
        while (i.hasNext()) {
            EncodingDecl d = (EncodingDecl) i.next();
            if (printer.enabled) {
                printer.print("processing encoding " + d.name.image + " ");
            }

            if (d instanceof EncodingDecl.Derived) {
                EncodingDecl.Derived dd = (EncodingDecl.Derived) d;
                EncodingDecl parent = (EncodingDecl) encodingMap.get(dd.pname.image);
                dd.setParent(parent);
            }

            printer.println("-> result: " + d.getBitWidth() + " bits");
        }
    }

    private void processSubroutines() {
        Iterator i = subroutines.iterator();
        while (i.hasNext()) {
            SubroutineDecl sd = (SubroutineDecl) i.next();
            printer.print("processing subroutine " + sd.name + " ");

            // find operand decl
            Iterator oi = sd.getOperandIterator();
            while (oi.hasNext()) {
                CodeRegion.Operand od = (CodeRegion.Operand) oi.next();
                OperandDecl opdec = getOperandDecl(od.type.image);
                if (opdec != null)
                    od.setOperandType(opdec);
            }

            if (printer.enabled) {
                new PrettyPrinter(printer).visitStmtList(sd.getCode());
            }

        }
    }

    private void processInstructions() {
        Iterator i = getInstrIterator();
        while (i.hasNext()) {
            InstrDecl id = (InstrDecl) i.next();
            printer.print("processing instruction " + id.name + " ");

            // inline and optimize the body of the instruction
            List code = id.getCode();
            code = new Inliner().visitStmtList(code);
            code = new Optimizer(code).optimize();

            id.setCode(code);

            // find parent encoding
            if (id.encoding instanceof EncodingDecl.Derived) {
                EncodingDecl.Derived dd = (EncodingDecl.Derived) id.encoding;
                EncodingDecl parent = (EncodingDecl) encodingMap.get(dd.pname.image);
                dd.setParent(parent);
            }

            int encodingSize = id.getEncodingSize();
            if (encodingSize % 16 != 0)
                throw Avrora.failure("encoding not word aligned: " + id.name.image + " is " + encodingSize + " bits");

            // find operand decl
            Iterator oi = id.getOperandIterator();
            while (oi.hasNext()) {
                CodeRegion.Operand od = (CodeRegion.Operand) oi.next();
                OperandDecl opdec = getOperandDecl(od.type.image);
                if (opdec == null)
                    throw Avrora.failure("operand type undefined " + StringUtil.quote(od.type.image));
                od.setOperandType(opdec);
            }

            // check that cycles make sense
            if (id.cycles < 0)
                throw Avrora.failure("instruction " + id.name.image + " has negative cycle count");

            if (printer.enabled) {
                new PrettyPrinter(printer).visitStmtList(code);
            }

        }
    }

    public Iterator getInstrIterator() {
        return instructions.iterator();
    }

    public Iterator getEncodingIterator() {
        return encodings.iterator();
    }

    public Iterator getSubroutineIterator() {
        return subroutines.iterator();
    }

    public void addSubroutine(SubroutineDecl d) {
        printer.println("loading subroutine " + d.name.image + "...");
        subroutineMap.put(d.name.image, d);
        subroutines.add(d);
    }

    public void addInstruction(InstrDecl i) {
        printer.println("loading instruction " + i.name.image + "...");
        instructionMap.put(i.name.image, i);
        instructions.add(i);
    }

    public void addOperand(OperandDecl d) {
        printer.println("loading operand declaration " + d.name.image + "...");
        operandMap.put(d.name.image, d);
        operands.add(d);
    }

    public void addEncoding(EncodingDecl d) {
        printer.println("loading encoding format " + d.name.image + "...");
        encodingMap.put(d.name.image, d);
        encodings.add(d);
    }

    /**
     * The <code>accept()</code> method implements part of the visitor pattern that
     * allows a visitor to visit each part of the architecture description.
     * @param v the visitor to accept
     */
    public void accept(Visitor v) {

        accept((OperandVisitor) v);
        accept((EncodingVisitor) v);
        accept((SubroutineVisitor) v);
        accept((InstrVisitor) v);
    }

    public void accept(OperandVisitor v) {
        Iterator i;
        i = operands.iterator();
        while (i.hasNext()) v.visit((OperandDecl) i.next());
    }

    public void accept(EncodingVisitor v) {
        Iterator i;
        i = encodings.iterator();
        while (i.hasNext()) v.visit((EncodingDecl) i.next());
    }

    public void accept(SubroutineVisitor v) {
        Iterator i;
        i = subroutines.iterator();
        while (i.hasNext()) v.visit((SubroutineDecl) i.next());
    }

    public void accept(InstrVisitor v) {
        Iterator i;
        i = instructions.iterator();
        while (i.hasNext()) v.visit((InstrDecl) i.next());
    }

    public InstrDecl getInstruction(String name) {
        return (InstrDecl) instructionMap.get(name);
    }

    public SubroutineDecl getSubroutine(String name) {
        return (SubroutineDecl) subroutineMap.get(name);
    }

    public OperandDecl getOperandDecl(String name) {
        return (OperandDecl) operandMap.get(name);
    }


    /**
     * The <code>Inliner</code> class implements a visitor over the code that
     * inlines calls to known subroutines. This produces code that is free
     * of calls to the subroutines declared within the architecture description
     * and therefore is ready for constant and copy propagation optimizations.
     *
     * The <code>Inliner</code> will aggressively inline all calls, therefore
     * it cannot detect recursion. It assumes that return statements are at
     * the end of subroutines and do not occur in branches. This is not
     * enforced by any checking, which should be done in the future.
     *
     * @author Ben L. Titzer
     */
    class Inliner extends StmtRebuilder.DepthFirst {
        final Inliner parent;
        List newStmts;
        HashMap varMap;
        int tmpCount;
        String retVal;
        SubroutineDecl curSubroutine;

        private Inliner(Inliner p, List ns) {
            parent = p;
            newStmts = ns;
            varMap = new HashMap();
        }

        private Inliner(Inliner p) {
            parent = p;
            newStmts = new LinkedList();
            varMap = new HashMap();
        }

        Inliner() {
            parent = null;
            newStmts = new LinkedList();
            varMap = new HashMap();
        }

        public List visitStmtList(List l) {

            Iterator i = l.iterator();
            while (i.hasNext()) {
                Stmt a = (Stmt) i.next();
                Stmt na = a.accept(this);
                if (na != null) newStmts.add(na);
            }

            return newStmts;
        }

        public Stmt visit(CallStmt s) {
            SubroutineDecl d = getSubroutine(s.method.image);
            if (shouldNotInline(d)) {
                return super.visit(s);
            } else {
                inlineCall(s.method, d, s.args);
                return null;
            }
        }

        public Stmt visit(VarAssignStmt s) {
            String nv = varName(s.variable);
            return new VarAssignStmt(newToken(nv), s.expr.accept(this));
        }

        public Stmt visit(VarBitAssignStmt s) {
            String nv = varName(s.variable);
            return (new VarBitAssignStmt(newToken(nv), s.bit.accept(this), s.expr.accept(this)));
        }

        public Stmt visit(VarBitRangeAssignStmt s) {
            String nv = varName(s.variable);
            return (new VarBitRangeAssignStmt(newToken(nv), s.low_bit, s.high_bit, s.expr.accept(this)));
        }

        public Stmt visit(DeclStmt s) {
            String nv = newTemp(s.name.image);
            return (new DeclStmt(newToken(nv), s.type, s.init.accept(this)));
        }

        public Stmt visit(ReturnStmt s) {
            if (curSubroutine == null)
                throw Avrora.failure("return not within subroutine!");

            retVal = newTemp(null);
            return (new DeclStmt(newToken(retVal), curSubroutine.ret, s.expr.accept(this)));
        }


        public Stmt visit(IfStmt s) {
            Expr nc = s.cond.accept(this);
            List nt = new Inliner(this).visitStmtList(s.trueBranch);
            List nf = new Inliner(this).visitStmtList(s.falseBranch);
            return (new IfStmt(nc, nt, nf));
        }

        protected String newTemp(String orig) {
            String nn;
            if (parent != null)
                nn = parent.newTemp(null);
            else
                nn = "tmp_" + (tmpCount++);

            if (orig != null) varMap.put(orig, nn);
            return nn;
        }

        protected String inlineCall(Token m, SubroutineDecl d, List args) {
            if (d.numOperands() != args.size())
                Avrora.failure("arity mismatch in call to " + m.image + " @ " + m.beginLine + ":" + m.beginColumn);

            Inliner bodyBuilder = new Inliner(this, newStmts);

            Iterator formal_iter = d.getOperandIterator();
            Iterator arg_iter = args.iterator();

            while (formal_iter.hasNext()) {
                CodeRegion.Operand f = (CodeRegion.Operand) formal_iter.next();
                Expr e = (Expr) arg_iter.next();

                // get a new temporary
                String nn = newTemp(null);

                // put the arguments in the alpha-rename map for the body
                bodyBuilder.varMap.put(f.name.image, nn);

                // alpha-rename expression that is argument
                Expr ne = e.accept(this);
                newStmts.add(new DeclStmt(nn, f.type, ne));
            }

            // set the current subroutine
            bodyBuilder.curSubroutine = d;
            // process body
            bodyBuilder.visitStmtList(d.getCode());

            return bodyBuilder.retVal;
        }


        public Expr visit(CallExpr v) {
            SubroutineDecl d = getSubroutine(v.method.image);
            if (shouldNotInline(d)) {
                return super.visit(v);
            } else {
                String result = inlineCall(v.method, d, v.args);
                return new VarExpr(result);
            }
        }

        protected boolean shouldNotInline(SubroutineDecl d) {
            if (d == null || !d.inline || !d.hasBody()) return true;
            return false;
        }

        public Expr visit(VarExpr v) {
            // alpha rename all variables
            return new VarExpr(varName(v.variable));
        }

        protected String varName(String n) {
            String nn = (String) varMap.get(n);
            if (nn == null && parent != null) nn = parent.varName(n);
            if (nn == null) return n;
            return nn;
        }

        protected String varName(Token n) {
            return varName(n.image);
        }

        protected Token newToken(String t) {
            Token tk = new Token();
            tk.image = t;
            tk.kind = ISDLParserConstants.IDENTIFIER;
            return tk;
        }
    }

    public class PrettyPrinter extends StmtVisitor.DepthFirst {

        final Printer p;

        PrettyPrinter(Printer p) {
            this.p = p;
        }

        public void visitStmtList(List s) {
            p.startblock();
            Iterator i = s.iterator();
            while (i.hasNext()) {
                Stmt st = (Stmt) i.next();
                st.accept(this);
            }
            p.endblock();
        }

        public void visit(IfStmt s) {
            p.print("if ( ");
            p.print(s.cond.toString());
            p.print(" ) ");
            visitStmtList(s.trueBranch);
            p.print("else ");
            visitStmtList(s.falseBranch);
        }

        public void visit(CallStmt s) {
            p.println(s.toString());
        }

        public void visit(DeclStmt s) {
            p.println(s.toString());
        }

        public void visit(MapAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(MapBitAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(MapBitRangeAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(ReturnStmt s) {
            p.println(s.toString());
        }

        public void visit(VarAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(VarBitAssignStmt s) {
            p.println(s.toString());
        }

        public void visit(VarBitRangeAssignStmt s) {
            p.println(s.toString());
        }

    }

}
