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

import avrora.util.Printer;
import avrora.util.StringUtil;
import jintgen.isdl.*;
import jintgen.jigir.CodeRegion;

/**
 * The <code>ClassGenerator</code> class generates a set of classes that represent instructions in an
 * architecture. It will generate an outer class <code>Instr</code> that contains as inner classes, the
 * individual instructions contained in the architecture description.
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
        generateInstrSet();
        generateRegSets();
        generateChecks();
        generateConstructors();
        generateClasses();
        printer.unindent();
    }

    private void generateClasses() {
        printer.println("");
        printer.println("//-------------------------------------------------------");
        printer.println("// GENERATED: Class definitions individual instructions ");
        printer.println("//-------------------------------------------------------");

        for (InstrDecl d : architecture.getInstructions()) emitClass(d);
    }

    private void generateConstructors() {
        printer.println("");
        printer.println("//----------------------------------------------------------------");
        printer.println("// GENERATED: Static factory methods for individual instructions ");
        printer.println("//----------------------------------------------------------------");

        for (InstrDecl d : architecture.getInstructions()) emitConstructor(d);
    }

    private void generateChecks() {
        printer.println("");
        printer.println("//-------------------------------------------------------------------");
        printer.println("// GENERATED: Methods to check the validity of individual operands ");
        printer.println("//-------------------------------------------------------------------");

        for (OperandTypeDecl d : architecture.getOperandTypes()) emitChecks(d);
    }

    private void generateRegSets() {
        printer.println("");
        printer.println("//-------------------------------------------------------");
        printer.println("// GENERATED: Sets of registers to check constraints ");
        printer.println("//-------------------------------------------------------");

        for (OperandTypeDecl d : architecture.getOperandTypes()) emitRegSet(d);
    }

    private void generateInstrSet() {
        printer.println("");
        printer.println("//-------------------------------------------------------------------");
        printer.println("// GENERATED: Method to initialize the instruction set mapping ");
        printer.println("//-------------------------------------------------------------------");

        printer.startblock("private static void initializeInstrSet()");

        for (InstrDecl d : architecture.getInstructions())
            printer.println("instructions.put(" + d.name + ", " + getClassName(d) + ".prototype);");

        printer.endblock();
    }

    public void emitClass(InstrDecl d) {
        String cName = d.name.image;
        cName = StringUtil.trimquotes(cName.toUpperCase());
        printer.startblock("public static class " + cName + " extends Instr");

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
        printer.print(StringUtil.commalist(d.name, "null", "" + d.getEncodingSize() / 8, "" + d.getCycles()));
        printer.println(");");
    }

    private void emitPrototype(String cName, InstrDecl d) {
        printer.startblock("public static final InstrPrototype prototype = new InstrPrototype(props)");
        printer.startblock("public Instr build(Operand[] ops)");
        printer.println("return Instr.new" + cName + "(ops);");
        printer.endblock();
        printer.endblock();
    }

    private void emitFields(InstrDecl d) {
        // emit the declaration of the fields
        for (AddressingModeDecl.Operand o : d.getOperands()) {
            printer.print("public final ");
            printer.print(o.getOperandType().toString() + ' ');
            printer.println(o.name.toString() + ';');
        }
    }

    private void emitConstructor(String cName, InstrDecl d) {
        // emit the declaration of the constructor
        printer.print("private " + cName + '(');
        emitParams(d);
        printer.startblock(")");

        // emit the initialization code for each field
        for (AddressingModeDecl.Operand o : d.getOperands()) {
            String n = o.name.image;
            printer.println("this." + n + " = " + n + ';');
        }

        printer.endblock();
    }

    public void emitConstructor(InstrDecl d) {
        String cName = getClassName(d);
        emitArrayMethod(cName, d);
        emitSpecificMethod(cName, d);

    }

    private void emitArrayMethod(String cName, InstrDecl d) {
        printer.startblock("public static Instr." + cName + " new" + cName + "(Operand[] ops)");
        printer.println("count(ops, " + d.getOperands().size() + ");");

        printer.print("return new " + cName + '(');
        // emit the checking code for each operand
        int cntr = 0;
        for (AddressingModeDecl.Operand o : d.getOperands()) {
            if (cntr++ != 0) printer.print(", ");
            OperandTypeDecl ot = o.getOperandType();
            String asMeth = ot.isSymbol() ? "asSym" : "asImm";
            printer.print("check_" + o.type.image + '(' + cntr + ", " + asMeth + '(' + cntr + ", ops))");
        }
        printer.println(");");
        printer.endblock();
    }

    private void emitSpecificMethod(String cName, InstrDecl d) {
        printer.print("public static Instr." + cName + " new" + cName + '(');
        emitParams(d);
        printer.startblock(")");

        printer.print("return new " + cName + '(');
        // emit the checking code for each operand
        int cntr = 0;
        for (AddressingModeDecl.Operand o : d.getOperands()) {
            if (cntr++ != 0) printer.print(", ");
            String n = o.name.toString();
            printer.print("check_" + o.type.image + '(' + cntr + ", " + n + ')');
        }
        printer.println(");");
        printer.endblock();
    }


    private void emitParams(InstrDecl d) {
        boolean first = true;
        for (AddressingModeDecl.Operand o : d.getOperands()) {
            if (!first) printer.print(", ");
            printer.print(o.getOperandType().toString() + ' ');
            printer.print(o.name.toString());
            first = false;
        }
    }

    public void emitRegSet(OperandTypeDecl d) {
        if (!d.isSymbol()) return;

        OperandTypeDecl.SymbolSet rset = (OperandTypeDecl.SymbolSet) d;

        String type = d.name.image;
        printer.println("private static final Register[] " + type + "_array = {");
        printer.indent();


        int cntr = 0;
        for (SymbolMapping.Entry renc : rset.map.getEntries()) {
            if (cntr++ != 0) printer.print(", ");
            printer.print("Register." + renc.name.toUpperCase());
            if (cntr != 1 && (cntr % 4) == 1) printer.nextln();
        }

        printer.unindent();
        printer.nextln();
        printer.println("};");
        printer.println("private static final Register.Set " + type + "_set = new Register.Set(" + type + "_array);");
    }


    public void emitChecks(OperandTypeDecl d) {
        String type = d.name.toString();
        String ptype = d.isSymbol() ? "Register" : "int";
        printer.startblock("private static " + ptype + " check_" + type + "(int n, " + ptype + " v)");

        if (d.isSymbol()) {
            printer.println("return checkRegSet(n, v, " + type + "_set);");
        } else {
            OperandTypeDecl.Value imm = (OperandTypeDecl.Value) d;
            printer.println("return checkRange(n, v, " + imm.low + ", " + imm.high + ");");
        }

        printer.endblock();
    }


    private static String getClassName(InstrDecl d) {
        String cName = d.name.image;
        cName = StringUtil.trimquotes(cName.toUpperCase());
        return cName;
    }


}
