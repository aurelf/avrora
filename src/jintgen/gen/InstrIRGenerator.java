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

import avrora.util.*;
import jintgen.isdl.*;
import jintgen.jigir.CodeRegion;
import jintgen.Main;

import java.io.PrintStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * The <code>ClassGenerator</code> class generates a set of classes that represent instructions in an
 * architecture. It will generate an outer class <code>Instr</code> that contains as inner classes, the
 * individual instructions contained in the architecture description.
 *
 * @author Ben L. Titzer
 */
public class InstrIRGenerator extends Generator {

    private Printer printer;

    String instrClassName;
    String operandClassName;
    String visitorClassName;
    String builderClassName;
    String symbolClassName;

    protected final Option.Str CLASS_FILE = options.newOption("class-template", "Instr.java",
            "This option specifies the name of the file that contains a template for generating the " +
            "instruction classes.");

    public void generate() throws Exception {

        instrClassName = className("Instr");
        operandClassName = className("Operand");
        visitorClassName = className("InstrVisitor");
        builderClassName = className("InstrBuilder");
        symbolClassName = className("Symbol");

        //generateRegSets();
        //generateChecks();
        generateOperandTypes();
        generateVisitor();
        generateInstrClasses();
        generateSymbolSets();
    }

    private void emitPackage() {
        String pname = this.DEST_PACKAGE.get();
        if ( !"".equals(pname) ) {
            printer.println("package "+pname+";");
            printer.nextln();
        }
    }

    //=========================================================================================
    // CODE TO EMIT VISITOR CLASS
    //=========================================================================================

    private void generateVisitor() throws IOException {
        File f = new File(visitorClassName + ".java");
        printer = new Printer(new PrintStream(f));

        emitPackage();
        printer.startblock("public interface "+visitorClassName);
        for (InstrDecl d : arch.instructions) emitVisitMethod(d);
        printer.endblock();
        printer.close();
    }

    private void emitVisitMethod(InstrDecl d) {
        printer.println("public void visit("+instrClassName+"."+d.innerClassName+" i);");
    }

    //=========================================================================================
    // CODE TO EMIT INSTR CLASSES
    //=========================================================================================

    private void generateInstrClasses() throws IOException {
        File f = new File(instrClassName + ".java");
        printer = new Printer(new PrintStream(f));

        emitPackage();
        printer.startblock("public abstract class "+instrClassName);
        for (InstrDecl d : arch.instructions) emitClass(d);
        printer.endblock();
        printer.close();
    }

    private void emitClass(InstrDecl d) {
        String cName = d.getInnerClassName();
        cName = StringUtil.trimquotes(cName.toUpperCase());
        printer.startblock("public static class " + cName + " extends "+instrClassName);

        //emitStaticProperties(d);
        //emitPrototype(cName, d);
        emitFields(d);
        emitConstructor(cName, d);
        emitAcceptMethod();

        printer.endblock();
        printer.println("");
    }

    private void emitFields(InstrDecl d) {
        // emit the declaration of the fields
        for (AddrModeDecl.Operand o : d.getOperands()) {
            printer.print("public final ");
            printer.print(getOperandTypeName(o.getOperandType()) + ' ');
            printer.println(o.name.toString() + ';');
        }
    }

    private void emitConstructor(String cName, InstrDecl d) {
        // emit the declaration of the constructor
        printer.print(cName + '(');
        emitParams(d);
        printer.startblock(")");

        // emit the initialization code for each field
        for (AddrModeDecl.Operand o : d.getOperands()) {
            String n = o.name.image;
            printer.println("this." + n + " = " + n + ';');
        }

        printer.endblock();
    }

    private void emitAcceptMethod() {
        printer.println("public void accept("+visitorClassName+" v) { v.visit(this); }");
    }

    //=========================================================================================
    // CODE TO EMIT SYMBOL SETS
    //=========================================================================================

    private void generateSymbolSets() throws IOException {
        File f = new File(symbolClassName + ".java");
        printer = new Printer(new PrintStream(f));

        generateSymbolSetHeader();
        for ( SymbolMapping m : arch.enums ) {
            generateSymbolClass(m);

        }

        printer.endblock();
        printer.close();
    }

    private void generateSymbolSetHeader() {
        emitPackage();
        printer.println("import java.util.HashMap;");
        printer.startblock("public class "+symbolClassName);
        printer.println("public final String symbol;");
        printer.println("public final int value;");
        printer.println("");
        printer.println(symbolClassName+"(String sym, int v) { symbol = sym;  value = v; }");
        printer.println("public int getValue() { return value; }");
        printer.println("");
    }

    private void generateSymbolClass(SymbolMapping m) {
        String n = m.name.image;
        printer.startblock("public static class "+n+" extends "+symbolClassName);
        printer.println(n+"(String sym, int v) { super(sym, v); }");

        printer.println("private static HashMap "+n+"_set = new HashMap();");

        printer.println("private static "+n+" new"+n+"(String n, int v) {");
        printer.println("    "+n+" obj = new "+n+"(n, v);");
        printer.println("    "+n+"_set.put(n, obj);");
        printer.println("    return obj;");
        printer.println("}");

        printer.println("public static "+n+" get(String name) {");
        printer.println("    return ("+n+")"+n+"_set.get(name);");
        printer.println("}");

        for ( SymbolMapping.Entry e : m.getEntries() ) {
            String en = e.name;
            String EN = en.toUpperCase();
            printer.println("public static final "+n+" "+EN+" = new"+n+"(\""+en+"\", "+e.value+");");
        }

        printer.endblock();
        printer.println("");
    }

    //=========================================================================================
    // CODE TO EMIT OPERAND TYPES
    //=========================================================================================
    private void generateOperandTypes() throws IOException {
        File f = new File(operandClassName + ".java");
        printer = new Printer(new PrintStream(f));
        emitPackage();
        printer.println("import java.util.HashMap;");
        printer.startblock("public class "+operandClassName);
        // generate all the explicitly declared operand types
        for ( OperandTypeDecl d : arch.operandTypes )
            generateOperandType(d);
        // generate all the explicitly declared operand types
        for ( AddrModeSetDecl d : arch.addrSets ) {
            for ( AddrModeDecl.Operand o : d.unionOperands )
            generateOperandType(o.getOperandType());
        }

        printer.startblock("private static int checkValue(int val, int low, int high)");
        printer.startblock("if ( val < low || val > high )");
        printer.println("throw new Error();");
        printer.endblock();
        printer.println("return val;");
        printer.endblock();
        printer.endblock();
        printer.close();
    }

    private void generateOperandType(OperandTypeDecl d) {
        printer.startblock("public static class "+d.name.image+" extends "+operandClassName);
        if ( d.isSymbol() ) {
            generateSymbolType((OperandTypeDecl.SymbolSet)d);
        } else if ( d.isValue() ) {
            generateValueType((OperandTypeDecl.Value)d);
        } else if ( d.isCompound() ) {
            generateCompoundType((OperandTypeDecl.Compound)d);
        } else if ( d.isUnion() ) {
            generateUnionType((OperandTypeDecl.Union)d);
        }
        printer.endblock();
        printer.println("");
    }

    private void generateSymbolType(OperandTypeDecl.SymbolSet d) {
        printer.println("public final "+symbolClassName+" symbol;");
        printer.println("public static final HashMap set = new HashMap();");
        printer.startblock("static");
        printer.endblock();
        printer.startblock(d.name.image+"(String s)");
        printer.println("symbol = null;");
        printer.endblock();
        printer.startblock(d.name.image+"("+symbolClassName+" sym)");
        printer.println("symbol = null;");
        printer.endblock();
    }

    private void generateValueType(OperandTypeDecl.Value d) {
        printer.println("public static final int low = "+d.low+";");
        printer.println("public static final int high = "+d.high+";");
        printer.println("public final int value;");
        printer.startblock(d.name.image+"(int val)");
        printer.println("value = checkValue(val, low, high);");
        printer.endblock();
    }

    private void generateCompoundType(OperandTypeDecl.Compound d) {
        // generate fields of compound operand
        for ( AddrModeDecl.Operand o : d.subOperands ) {
            printer.println("public final "+o.type.image+" "+o.name.image+";");
        }
        printer.print(d.name.image+"(");
        boolean previous = false;
        for ( AddrModeDecl.Operand o : d.subOperands ) {
            if ( previous ) printer.print(", ");
            printer.print(o.type.image+" "+o.name.image);
            previous = true;
        }
        printer.startblock(")");
        for ( AddrModeDecl.Operand o : d.subOperands ) {
            printer.println("this."+o.name.image+" = "+o.name.image+";");
        }
        printer.endblock();
    }

    private void generateUnionType(OperandTypeDecl.Union d) {
        HashSet<String> seen = new HashSet<String>();
        printer.println("public final "+operandClassName+" operand;");
        for ( OperandTypeDecl ot : d.types ) {
            if ( seen.contains(ot.name.image) ) continue;
            seen.add(ot.name.image);
            printer.println(d.name.image+"("+ot.name.image+" o) { operand = o; }");
        }
    }

    //=========================================================================================
    // CODE TO EMIT OTHER STUFF
    //=========================================================================================

    private void generateConstructors() {
        printer.println("");
        printer.println("//----------------------------------------------------------------");
        printer.println("// GENERATED: Static factory methods for individual instructions ");
        printer.println("//----------------------------------------------------------------");

        for (InstrDecl d : arch.instructions) emitConstructor(d);
    }

    private void generateChecks() {
        printer.println("");
        printer.println("//-------------------------------------------------------------------");
        printer.println("// GENERATED: Methods to check the validity of individual operands ");
        printer.println("//-------------------------------------------------------------------");

        for (OperandTypeDecl d : arch.operandTypes) emitChecks(d);
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

    private String getOperandTypeName(OperandTypeDecl d) {
        return operandClassName+"."+d.name.image;
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
        for (AddrModeDecl.Operand o : d.getOperands()) {
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
        for (AddrModeDecl.Operand o : d.getOperands()) {
            if (cntr++ != 0) printer.print(", ");
            String n = o.name.toString();
            printer.print("check_" + o.type.image + '(' + cntr + ", " + n + ')');
        }
        printer.println(");");
        printer.endblock();
    }


    private void emitParams(InstrDecl d) {
        boolean first = true;
        for (AddrModeDecl.Operand o : d.getOperands()) {
            if (!first) printer.print(", ");
            printer.print(getOperandTypeName(o.getOperandType()) + ' ');
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
