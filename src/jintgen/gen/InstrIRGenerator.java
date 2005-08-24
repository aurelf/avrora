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
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

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
    String opvisitorClassName;
    String builderClassName;
    String symbolClassName;

    LinkedList<String> hashMapImport;

    protected final Option.Str CLASS_FILE = options.newOption("class-template", "Instr.java",
            "This option specifies the name of the file that contains a template for generating the " +
            "instruction classes.");

    public void generate() throws Exception {

        instrClassName = className("Instr");
        operandClassName = className("Operand");
        opvisitorClassName = className("OperandVisitor");
        visitorClassName = className("InstrVisitor");
        builderClassName = className("InstrBuilder");
        symbolClassName = className("Symbol");

        hashMapImport = new LinkedList<String>();
        hashMapImport.add("java.util.HashMap");

        //generateRegSets();
        //generateChecks();
        generateOperandClasses();
        generateVisitor();
        generateInstrClasses();
        generateEnumerations();
        generateBuilder();
    }

    //=========================================================================================
    // CODE TO EMIT VISITOR CLASS
    //=========================================================================================

    private void generateVisitor() throws IOException {
        printer = newInterfacePrinter(visitorClassName, null, null,
                "The <code>"+visitorClassName+"</code> interface allows user code that implements " +
                "the interface to easily dispatch on the type of an instruction without casting using " +
                "the visitor pattern.");
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
        printer = newClassPrinter(instrClassName, null, null,
                "The <code>"+instrClassName+"</code> class is a container (almost a namespace) for " +
                "all of the instructions in this architecture. Each inner class represents an instruction " +
                "in the architecture and also extends the outer class.");
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

    private void generateEnumerations() throws IOException {
        printer = newClassPrinter(symbolClassName, hashMapImport, null,
                "The <code>"+symbolClassName+"</code> class represents a symbol (or an enumeration as " +
                "declared in the instruction set description) relevant to the instruction set architecture. " +
                "For example register names, status bit names, etc are given here. This class provides a " +
                "type-safe enumeration for such symbolic names.");
        generateEnumHeader();

        for ( EnumDecl d : arch.enums ) {
            generateEnumClass(d);
        }

        printer.endblock();
        printer.close();
    }

    private void generateEnumHeader() {
        printer.println("public final String symbol;");
        printer.println("public final int value;");
        printer.println("");
        printer.println(symbolClassName+"(String sym, int v) { symbol = sym;  value = v; }");
        printer.println("public int getValue() { return value; }");
        printer.println("public int getEncodingValue() { return value; }");
        printer.println("");
    }

    private void generateEnumClass(EnumDecl d) {
        SymbolMapping m = d.map;
        String n = m.name.image;
        if ( d instanceof EnumDecl.Subset ) {
            // this enumeration is a subset of another enumeration, but with possibly different
            // encoding
            EnumDecl.Subset sd = (EnumDecl.Subset)d;
            printer.startblock("public static class "+n+" extends "+sd.pname.image);
            printer.println("public final int encoding;");
            printer.println("public int getEncodingValue() { return encoding; }");
            printer.println("private static HashMap set = new HashMap();");
            printer.println("private static "+n+" new"+n+"(String n, int v, int ev) {");
            printer.println("    "+n+" obj = new "+n+"(n, v, ev);");
            printer.println("    set.put(n, obj);");
            printer.println("    return obj;");
            printer.println("}");

            printer.println(n+"(String sym, int v, int ev) { super(sym, v); encoding = ev; }");

            for ( SymbolMapping.Entry e : m.getEntries() ) {
                String en = e.name;
                String EN = en.toUpperCase();
                SymbolMapping.Entry se = sd.parent.map.get(e.name);
                printer.println("public static final "+n+" "+EN+" = new"+n+"(\""+en+"\", "+se.value+", "+e.value+");");
            }
        } else {
            // this enumeration is NOT a subset of another enumeration
            printer.startblock("public static class "+n+" extends "+symbolClassName);
            printer.println("private static HashMap set = new HashMap();");

            printer.println("private static "+n+" new"+n+"(String n, int v) {");
            printer.println("    "+n+" obj = new "+n+"(n, v);");
            printer.println("    set.put(n, obj);");
            printer.println("    return obj;");
            printer.println("}");

            printer.println(n+"(String sym, int v) { super(sym, v); }");

            for ( SymbolMapping.Entry e : m.getEntries() ) {
                String en = e.name;
                String EN = en.toUpperCase();
                printer.println("public static final "+n+" "+EN+" = new"+n+"(\""+en+"\", "+e.value+");");
            }
        }

        printer.endblock();
        printer.println("");

        genGetMethod(n);

        printer.println("");
    }

    private void genGetMethod(String n) {
        printer.println("public static "+n+" get_"+n+"(String name) {");
        printer.println("    return ("+n+")"+n+".set.get(name);");
        printer.println("}");
    }

    //=========================================================================================
    // CODE TO EMIT OPERAND TYPES
    //=========================================================================================
    private void generateOperandClasses() throws IOException {
        printer = newInterfacePrinter(operandClassName, hashMapImport, null,
                "The <code>"+operandClassName+"</code> interface represents operands that are allowed to " +
                "instructions in this architecture. Inner classes of this interface enumerate the possible " +
                "operand types to instructions and their constructors allow for dynamic checking of " +
                "correctness constraints as expressed in the instruction set description.");

        printer.println("public void accept("+opvisitorClassName+" v);");

        Printer vprinter = newInterfacePrinter(opvisitorClassName, hashMapImport, null,
                "The <code>"+opvisitorClassName+"</code> interface allows clients to use the Visitor pattern to " +
                "resolve the types of operands to instructions.");
        // generate union operand types
        HashMap<String, HashSet<String>> interfaces = new HashMap<String, HashSet<String>>();
        for ( AddrModeSetDecl d : arch.addrSets ) {
            for ( AddrModeDecl.Operand o : d.unionOperands ) {
                OperandTypeDecl.Union d1 = (OperandTypeDecl.Union)o.getOperandType();
                printer.println("public interface "+d1.name.image+" extends "+operandClassName+" { }");
                for ( OperandTypeDecl ut : d1.types ) {
                    HashSet<String>set = interfaces.get(ut.name.image);
                    if ( set == null ) {
                        set = new HashSet<String>();
                        interfaces.put(ut.name.image, set);
                    }
                    set.add(d1.name.image);
                }
            }
        }

        // generate all the explicitly declared operand types
        for ( OperandTypeDecl d : arch.operandTypes )
            generateOperandType(d, vprinter, interfaces);

        printer.endblock();
        printer.close();
        vprinter.endblock();
        vprinter.close();
    }

    private void generateOperandType(OperandTypeDecl d, Printer vprinter, HashMap<String, HashSet<String>> interfaces) {
        String otname = d.name.image;
        // generate visit method inside visitor
        vprinter.println("public void visit("+operandClassName+"."+otname+" o);");
        printer.print("public class "+otname+" implements "+operandClassName);
        HashSet<String> intf = interfaces.get(otname);
        if ( intf != null ) for ( String str : intf ) {
            printer.print(", "+str);
        }
        printer.startblock(" ");
        if ( d.isValue() ) {
            generateSimpleType((OperandTypeDecl.Value)d);
        } else if ( d.isCompound() ) {
            generateCompoundType((OperandTypeDecl.Compound)d);
        }
        // generate accept method in operand class
        printer.startblock("public void accept("+opvisitorClassName+" v)");
        printer.println("v.visit(this);");
        printer.endblock();
        printer.endblock();
        printer.println("");
    }

    private void generateSimpleType(OperandTypeDecl.Value d) {
        String k = d.kind.image;
        EnumDecl ed = arch.getEnum(k);
        if ( ed != null ) {
            String kn = symbolClassName+"."+k;
            printer.println("public final "+kn+" symbol;");
            printer.startblock(d.name.image+"(String s)");
            printer.println("symbol = "+symbolClassName+".get_"+k+"(s);");
            printer.println("if ( symbol == null ) throw new Error();");
            printer.endblock();
            printer.startblock(d.name.image+"("+kn+" sym)");
            printer.println("symbol = sym;");
            printer.endblock();
        } else {
            printer.println("public static final int low = "+d.low+";");
            printer.println("public static final int high = "+d.high+";");
            printer.println("public final int value;");
            printer.startblock(d.name.image+"(int val)");
            printer.println("value = "+builderClassName+".checkValue(val, low, high);");
            printer.endblock();
        }
    }

    private void generateCompoundType(OperandTypeDecl.Compound d) {
        // generate fields of compound operand
        for ( AddrModeDecl.Operand o : d.subOperands ) {
            printer.println("public final "+o.type.image+" "+o.name.image+";");
        }
        printer.beginList(d.name.image+"(");
        for ( AddrModeDecl.Operand o : d.subOperands ) {
            printer.print(o.type.image+" "+o.name.image);
        }
        printer.endList(") ");
        printer.startblock();
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

    private String getOperandTypeName(OperandTypeDecl d) {
        return operandClassName+"."+d.name.image;
    }

    private void emitParams(InstrDecl d) {
        printer.beginList();
        boolean first = true;
        for (AddrModeDecl.Operand o : d.getOperands()) {
            printer.print(getOperandTypeName(o.getOperandType()) + ' ' + o.name.toString());
        }
        printer.endList();
    }

    private void generateBuilder() throws IOException {
        printer = newClassPrinter(builderClassName, hashMapImport, null, null);

        printer.startblock("public static abstract class Single");
        printer.println("public abstract "+instrClassName+" build("+operandClassName+"[] operands);");
        printer.endblock();

        printer.println("static final HashMap builders = new HashMap();");

        printer.startblock("static Single addSingle(String name, Single s)");
        printer.println("builders.put(name, s);");
        printer.println("return s;");
        printer.endblock();

        for ( InstrDecl d : arch.instructions ) {
            printer.startblock("public static final Single "+d.innerClassName+" = addSingle("+d.name+", new Single()");
            printer.startblock("public "+instrClassName+" build("+operandClassName+"[] operands)");
            List<AddrModeDecl.Operand> operands = d.getOperands();
            printer.println("// assert operands.length == "+operands.size()+";");
            int cntr = 0;
            printer.beginList("return new "+instrClassName+"."+d.innerClassName+"(");
            for ( AddrModeDecl.Operand o : operands ) {
                printer.print("("+operandClassName+"."+o.type+")operands["+cntr+"]");
                cntr++;
            }
            printer.endListln(");");
            printer.endblock();
            printer.endblock(");");
        }

        printer.startblock("public static int checkValue(int val, int low, int high)");
        printer.startblock("if ( val < low || val > high )");
        printer.println("throw new Error();");
        printer.endblock();
        printer.println("return val;");
        printer.endblock();
        
        printer.endblock();
        printer.close();
    }
}
