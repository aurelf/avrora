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
        architecture.accept(new ConstructorEmitter());
        architecture.accept(new ClassEmitter());
        printer.unindent();
    }

    private class ClassEmitter implements Architecture.InstrVisitor {
        public void visit(InstrDecl d) {
            String cName = (d.variant == null) ? d.name.image : d.variant.image;
            cName = StringUtil.trimquotes(cName.toUpperCase());
            printer.startblock("public class "+cName);

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
        public void visit(InstrDecl d) {
            String cName = (d.variant == null) ? d.name.image : d.variant.image;
            cName = StringUtil.trimquotes(cName.toUpperCase());
            emitArrayMethod(cName, d);
            emitSpecificMethod(cName, d);

        }

        private void emitArrayMethod(String cName, InstrDecl d) {
            printer.startblock("public Instr."+cName+" new"+cName+"(Operand[] ops)");
            printer.println("count(ops, "+d.operands.size()+");");

            printer.print("return new "+cName+"(");
            // emit the checking code for each operand
            Iterator i = d.getOperandIterator();
            int cntr = 0;
            while ( i.hasNext() ) {
                CodeRegion.Operand o = (CodeRegion.Operand)i.next();
                printer.print("check_"+o.type.image+"(ops["+cntr+"])");
                if ( i.hasNext() ) printer.print(", ");
                cntr++;
            }
            printer.println(");");
            printer.endblock();
        }

        private void emitSpecificMethod(String cName, InstrDecl d) {
            printer.print("public Instr."+cName+" new"+cName+"(");
            emitParams(d);
            printer.startblock(")");

            printer.print("return new "+cName+"(");
            // emit the checking code for each operand
            Iterator i = d.getOperandIterator();
            while ( i.hasNext() ) {
                CodeRegion.Operand o = (CodeRegion.Operand)i.next();
                String n = o.name.toString();
                printer.print("check_"+o.type.image+"("+n+")");
                if ( i.hasNext() ) printer.print(", ");
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

}
