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

package jintgen.isdl;

import avrora.util.Util;
import jintgen.jigir.*;
import jintgen.gen.Inliner;
import jintgen.isdl.parser.Token;
import avrora.util.Printer;
import avrora.util.StringUtil;
import avrora.util.Verbose;
import avrora.util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>Architecture</code> class represents a collection of instructions, encodings, operands, and
 * subroutines that describe an instruction set architecture.
 *
 * @author Ben L. Titzer
 */
public class Architecture {

    public static boolean INLINE = true;

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

    /**
     * The <code>InstrVisitor</code> interface is a simple interface that can be used to iterate
     * over the instructions declared in the instruction set specification. A class implementing
     * this interface can call the <code>accept()</code> method of <code>Architecture</code> and
     * pass itself as a parameter.
     */
    public interface InstrVisitor {
        public void visit(InstrDecl d);
    }

    /**
     * The <code>SubroutineVisitor</code> interface is a simple interface that can be used to iterate
     * over the subroutines declared in the instruction set specification. A class implementing
     * this interface can call the <code>accept()</code> method of <code>Architecture</code> and
     * pass itself as a parameter.
     */
    public interface SubroutineVisitor {
        public void visit(SubroutineDecl d);
    }

    /**
     * The <code>OperandVisitor</code> interface is a simple interface that can be used to iterate
     * over the operands declared in the instruction set specification. A class implementing
     * this interface can call the <code>accept()</code> method of <code>Architecture</code> and
     * pass itself as a parameter.
     */
    public interface OperandVisitor {
        public void visit(OperandTypeDecl d);
    }

    /**
     * The <code>EncodingVisitor</code> interface is a simple interface that can be used to iterate
     * over the encodings declared in the instruction set specification. A class implementing
     * this interface can call the <code>accept()</code> method of <code>Architecture</code> and
     * pass itself as a parameter.
     */
    public interface EncodingVisitor {
        public void visit(EncodingDecl d);
    }

    /**
     * The <code>Visitor</code> class represents a visitor over the elements of the architecture description.
     * It has methods to visit each subroutine, instruction, operand, and encoding declared in the
     * specification.
     */
    public interface Visitor extends InstrVisitor, SubroutineVisitor, OperandVisitor, EncodingVisitor {
    }

    /**
     * The constructor for the <code>Architecture</code> class creates an instance with the specified
     * name that is empty and ready to receive new instruction declarations, encodings, etc.
     * @param n
     */
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

    public void verify() {
        verifyEncodings();
        verifySubroutines();
        verifyInstructions();
    }

    private void verifyEncodings() {
        Iterator i = getEncodingIterator();
        while (i.hasNext()) {
            EncodingDecl d = (EncodingDecl)i.next();
            if (printer.enabled) {
                printer.print("processing encoding " + d.name.image + ' ');
            }

            if (d instanceof EncodingDecl.Derived) {
                EncodingDecl.Derived dd = (EncodingDecl.Derived)d;
                EncodingDecl parent = (EncodingDecl)encodingMap.get(dd.pname.image);
                dd.setParent(parent);
            }

            printer.println("-> result: " + d.getBitWidth() + " bits");
        }
    }

    private void verifySubroutines() {
        Iterator i = subroutines.iterator();
        while (i.hasNext()) {
            SubroutineDecl sd = (SubroutineDecl)i.next();
            printer.print("processing subroutine " + sd.name + ' ');

            // find operand decl
            Iterator oi = sd.getOperandIterator();
            while (oi.hasNext()) {
                CodeRegion.Operand od = (CodeRegion.Operand)oi.next();
                OperandTypeDecl opdec = getOperandDecl(od.type.image);
                if (opdec != null)
                    od.setOperandType(opdec);
            }

            if (printer.enabled) {
                new PrettyPrinter(printer).visitStmtList(sd.getCode());
            }

        }
    }

    private void verifyInstructions() {
        Iterator i = getInstrIterator();
        while (i.hasNext()) {
            InstrDecl id = (InstrDecl)i.next();
            printer.print("processing instruction " + id.name + ' ');

            optimizeCode(id);
            verifyEncodings(id);
            verifyOperandTypes(id);
            verifyTiming(id);

            if (printer.enabled) {
                new PrettyPrinter(printer).visitStmtList(id.getCode());
            }

        }
    }

    private void verifyTiming(InstrDecl id) {
        // check that cycles make sense
        if (id.getCycles() < 0)
            throw Util.failure("instruction " + id.name.image + " has negative cycle count");
    }

    private void optimizeCode(InstrDecl id) {
        // inline and optimize the body of the instruction
        List code = id.getCode();
        code = new Inliner(this).process(code);

        id.setCode(code);
    }

    private void verifyOperandTypes(InstrDecl id) {
        // find operand decl
        Iterator oi = id.getOperandIterator();
        while (oi.hasNext()) {
            CodeRegion.Operand od = (CodeRegion.Operand)oi.next();
            OperandTypeDecl opdec = getOperandDecl(od.type.image);
            if (opdec == null)
                throw Util.failure("operand type undefined " + StringUtil.quote(od.type.image));
            od.setOperandType(opdec);
        }
    }

    private void verifyEncodings(InstrDecl id) {
        // for each of the declared encodings, find the parent and verify the size
        Iterator ei = id.encodingList.iterator();
        while ( ei.hasNext() ) {
            EncodingDecl encoding = (EncodingDecl)ei.next();
            if (encoding instanceof EncodingDecl.Derived) {
                // find parent encoding
                EncodingDecl.Derived dd = (EncodingDecl.Derived)encoding;
                EncodingDecl parent = (EncodingDecl)encodingMap.get(dd.pname.image);
                dd.setParent(parent);
            }

            int encodingSize = encoding.getBitWidth();
            if (encodingSize <= 0 || encodingSize % 16 != 0)
                throw Util.failure("encoding not word aligned: " + id.name.image + " is " + encodingSize + " bits");
            id.setEncodingSize(encodingSize);
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

    public void addOperand(OperandTypeDecl d) {
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
     * The <code>accept()</code> method implements part of the visitor pattern that allows a visitor to visit
     * each part of the architecture description.
     *
     * @param v the visitor to accept
     */
    public void accept(Visitor v) {

        accept((OperandVisitor)v);
        accept((EncodingVisitor)v);
        accept((SubroutineVisitor)v);
        accept((InstrVisitor)v);
    }

    public void accept(OperandVisitor v) {
        Iterator i;
        i = operands.iterator();
        while (i.hasNext()) v.visit((OperandTypeDecl)i.next());
    }

    public void accept(EncodingVisitor v) {
        Iterator i;
        i = encodings.iterator();
        while (i.hasNext()) v.visit((EncodingDecl)i.next());
    }

    public void accept(SubroutineVisitor v) {
        Iterator i;
        i = subroutines.iterator();
        while (i.hasNext()) v.visit((SubroutineDecl)i.next());
    }

    public void accept(InstrVisitor v) {
        Iterator i;
        i = instructions.iterator();
        while (i.hasNext()) v.visit((InstrDecl)i.next());
    }

    public InstrDecl getInstruction(String name) {
        return (InstrDecl)instructionMap.get(name);
    }

    public SubroutineDecl getSubroutine(String name) {
        return (SubroutineDecl)subroutineMap.get(name);
    }

    public OperandTypeDecl getOperandDecl(String name) {
        return (OperandTypeDecl)operandMap.get(name);
    }


    public class PrettyPrinter extends StmtVisitor.DepthFirst {

        final Printer p;

        PrettyPrinter(Printer p) {
            this.p = p;
        }

        public void visitStmtList(List s) {
            p.startblock();
            if (s == null) {
                p.println(" // empty body");
            } else {
                Iterator i = s.iterator();
                while (i.hasNext()) {
                    Stmt st = (Stmt)i.next();
                    st.accept(this);
                }
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
