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

import avrora.core.isdl.*;
import avrora.util.Printer;
import avrora.util.StringUtil;

import java.util.Iterator;

/**
 * The <code>ClassGenerator</code> class generates a set of classes that
 * represent instructions in an architecture. It will generate an outer
 * class <code>Instr</code> that contains as inner classes, the individual
 * instructions contained in the architecture description.
 *
 * @author Ben L. Titzer
 */
public class ClassGenerator {

    private final Architecture architecture;
    private final Printer printer;

    public ClassGenerator(Architecture a, Printer p) {
        architecture = a;
        printer = p;
    }

    public void generate() {
        printer.indent();
        new InstrSetEmitter().generate(architecture);
        new RegSetEmitter().generate(architecture);
        new CheckEmitter().generate(architecture);
        new ConstructorEmitter().generate(architecture);
        new ClassEmitter().generate(architecture);
        printer.unindent();
    }

    private class ClassEmitter implements Architecture.InstrVisitor {
        public void generate(Architecture a) {
            printer.println("");
            printer.println("//-------------------------------------------------------");
            printer.println("// GENERATED: Class definitions individual instructions ");
            printer.println("//-------------------------------------------------------");

            a.accept(this);
        }

        public void visit(InstrDecl d) {
            String cName = (d.variant == null) ? d.name.image : d.variant.image;
            cName = StringUtil.trimquotes(cName.toUpperCase());
            printer.startblock("public static class "+cName+" extends Instr");

            emitStaticProperties(d);
            emitPrototype(cName, d);
            emitFields(d);
            emitConstructor(cName, d);
            emitAcceptMethod();

            printer.endblock();
        }

        private void emitAcceptMethod() {
            printer.println("public void accept(InstrVisitor v) { v.visit(this); }");
        }

        private void emitStaticProperties(InstrDecl d) {
            printer.print("private static final InstrProperties props = new InstrProperties(");
            printer.print(StringUtil.commalist(d.name, d.variant, ""+d.getEncodingSize()/8, ""+d.cycles));
            printer.println(");");
        }

        private void emitPrototype(String cName, InstrDecl d) {
            printer.startblock("public static final InstrPrototype prototype = new InstrPrototype(props)");
            printer.startblock("public Instr build(Operand[] ops)");
            printer.println("return Instr.new"+cName+"(ops);");
            printer.endblock();
            printer.endblock();
        }

        private void emitFields(InstrDecl d) {
            // emit the declaration of the fields
            Iterator i = d.getOperandIterator();
            while ( i.hasNext() ) {
                printer.print("public final ");
                CodeRegion.Operand o = (CodeRegion.Operand)i.next();
                printer.print(o.getType()+" ");
                printer.println(o.name.toString()+";");
            }
        }

        private void emitConstructor(String cName, InstrDecl d) {
            // emit the declaration of the constructor
            printer.print("private "+cName+"(");
            emitParams(d);
            printer.startblock(")");

            // emit the initialization code for each field
            Iterator i = d.getOperandIterator();
            while ( i.hasNext() ) {
                CodeRegion.Operand o = (CodeRegion.Operand)i.next();
                String n = o.name.toString();
                printer.println("this."+n+" = "+n+";");
            }

            printer.endblock();
        }

    }

    private class ConstructorEmitter implements Architecture.InstrVisitor {
        public void generate(Architecture a) {
            printer.println("");
            printer.println("//----------------------------------------------------------------");
            printer.println("// GENERATED: Static factory methods for individual instructions ");
            printer.println("//----------------------------------------------------------------");

            a.accept(this);
        }

        public void visit(InstrDecl d) {
            String cName = getClassName(d);
            emitArrayMethod(cName, d);
            emitSpecificMethod(cName, d);

        }

        private void emitArrayMethod(String cName, InstrDecl d) {
            printer.startblock("public static Instr."+cName+" new"+cName+"(Operand[] ops)");
            printer.println("count(ops, "+d.operands.size()+");");

            printer.print("return new "+cName+"(");
            // emit the checking code for each operand
            Iterator i = d.getOperandIterator();
            int cntr = 0;
            while ( i.hasNext() ) {
                CodeRegion.Operand o = (CodeRegion.Operand)i.next();
                String asMeth = o.isRegister() ? "asReg" : "asImm";
                printer.print("check_"+o.type.image+"("+cntr+", "+asMeth+"("+cntr+", ops))");
                if ( i.hasNext() ) printer.print(", ");
                cntr++;
            }
            printer.println(");");
            printer.endblock();
        }

        private void emitSpecificMethod(String cName, InstrDecl d) {
            printer.print("public static Instr."+cName+" new"+cName+"(");
            emitParams(d);
            printer.startblock(")");

            printer.print("return new "+cName+"(");
            // emit the checking code for each operand
            Iterator i = d.getOperandIterator();
            int cntr = 0;
            while ( i.hasNext() ) {
                CodeRegion.Operand o = (CodeRegion.Operand)i.next();
                String n = o.name.toString();
                printer.print("check_"+o.type.image+"("+cntr+", "+n+")");
                if ( i.hasNext() ) printer.print(", ");
                cntr++;
            }
            printer.println(");");
            printer.endblock();
        }
    }

    private void emitParams(InstrDecl d) {
        Iterator i = d.getOperandIterator();
        while ( i.hasNext() ) {
            CodeRegion.Operand o = (CodeRegion.Operand)i.next();
            printer.print(o.getType()+" ");
            printer.print(o.name.toString());
            if ( i.hasNext()) printer.print(", ");
        }
    }

    private class RegSetEmitter implements Architecture.OperandVisitor {
        public void generate(Architecture a) {
            printer.println("");
            printer.println("//-------------------------------------------------------");
            printer.println("// GENERATED: Sets of registers to check constraints ");
            printer.println("//-------------------------------------------------------");

            a.accept(this);
        }

        public void visit(OperandDecl d) {
            if ( !d.isRegister() ) return;

            OperandDecl.RegisterSet rset = (OperandDecl.RegisterSet)d;

            String type = d.name.image;
            printer.println("private static final Register[] "+type+"_array = {");
            printer.indent();

            Iterator i = rset.members.iterator();
            int cntr = 0;
            while ( i.hasNext() ) {
                OperandDecl.RegisterEncoding renc = (OperandDecl.RegisterEncoding)i.next();
                printer.print("Register."+renc.name.image.toUpperCase());
                if ( i.hasNext() ) printer.print(", ");
                cntr++;
                if ( cntr != 0 && (cntr % 4) == 0) printer.nextln();
            }

            printer.unindent();
            printer.nextln();
            printer.println("};");
            printer.println("private static final Register.Set "+type+"_set = new Register.Set("+type+"_array);");
        }
    }

    private class InstrSetEmitter implements Architecture.InstrVisitor {
        public void generate(Architecture a) {
            printer.println("");
            printer.println("//-------------------------------------------------------------------");
            printer.println("// GENERATED: Method to initialize the instruction set mapping ");
            printer.println("//-------------------------------------------------------------------");

            printer.startblock("private static void initializeInstrSet()");

            a.accept(this);

            printer.endblock();
        }

        public void visit(InstrDecl d) {
            printer.println("instructions.put("+d.name+", "+getClassName(d)+".prototype);");
        }
    }

    private class CheckEmitter implements Architecture.OperandVisitor {
        public void generate(Architecture a) {
            printer.println("");
            printer.println("//-------------------------------------------------------------------");
            printer.println("// GENERATED: Methods to check the validity of individual operands ");
            printer.println("//-------------------------------------------------------------------");

            a.accept(this);
        }

        public void visit(OperandDecl d) {
            String type = d.name.toString();
            String ptype = d.isRegister() ? "Register" : "int";
            printer.startblock("private static "+ptype+" check_"+type+"(int n, "+ptype+" v)");

            if ( d.isRegister() ) {
                printer.println("return checkRegSet(n, v, "+type+"_set);");
            } else {
                OperandDecl.Immediate imm = (OperandDecl.Immediate)d;
                printer.println("return checkRange(n, v, "+imm.low+", "+imm.high+");");
            }

            printer.endblock();
        }
    }

    private static String getClassName(InstrDecl d) {
        String cName = (d.variant == null) ? d.name.image : d.variant.image;
        cName = StringUtil.trimquotes(cName.toUpperCase());
        return cName;
    }


}
