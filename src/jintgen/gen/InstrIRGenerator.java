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

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;

/**
 * The <code>ClassGenerator</code> class generates a set of classes that represent instructions in an
 * architecture. It will generate an outer class <code>Instr</code> that contains as inner classes, the
 * individual instructions contained in the architecture description.
 *
 * @author Ben L. Titzer
 */
public class InstrIRGenerator extends Generator {

    LinkedList<String> hashMapImport;

    protected final Option.Str CLASS_FILE = options.newOption("class-template", "Instr.java",
            "This option specifies the name of the file that contains a template for generating the " +
            "instruction classes.");

    public void generate() throws Exception {

        properties.setProperty("instr", className("Instr"));
        properties.setProperty("operand", className("Operand"));
        properties.setProperty("opvisitor", className("OperandVisitor"));
        properties.setProperty("visitor", className("InstrVisitor"));
        properties.setProperty("builder", className("Builder"));
        properties.setProperty("symbol", className("Symbol"));

        hashMapImport = new LinkedList<String>();
        hashMapImport.add("java.util.HashMap");

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
        setPrinter(newInterfacePrinter("visitor", null, null,
                tr("The <code>$visitor</code> interface allows user code that implements " +
                "the interface to easily dispatch on the type of an instruction without casting using " +
                "the visitor pattern.")));
        for (InstrDecl d : arch.instructions) emitVisitMethod(d);
        p.endblock();
        close();
    }

    private void emitVisitMethod(InstrDecl d) {
        println("public void visit($instr.$1 i);", d.innerClassName);
    }

    //=========================================================================================
    // CODE TO EMIT INSTR CLASSES
    //=========================================================================================

    private void generateInstrClasses() throws IOException {
        setPrinter(newClassPrinter("instr", null, null,
                tr("The <code>$instr</code> class is a container (almost a namespace) for " +
                "all of the instructions in this architecture. Each inner class represents an instruction " +
                "in the architecture and also extends the outer class.")));
        for (InstrDecl d : arch.instructions) emitClass(d);
        endblock();
        close();
    }

    private void emitClass(InstrDecl d) {
        String cName = d.getInnerClassName();
        cName = StringUtil.trimquotes(cName.toUpperCase());
        startblock("public static class $1 extends $instr", cName);

        emitFields(d);
        emitConstructor(cName, d);
        emitAcceptMethod();

        endblock();
        println("");
    }

    private void emitFields(InstrDecl d) {
        // emit the declaration of the fields
        for (AddrModeDecl.Operand o : d.getOperands()) {
            println("public final $operand.$1 $2;", o.getOperandType().name, o.name);
        }
    }

    private void emitConstructor(String cName, InstrDecl d) {
        // emit the declaration of the constructor
        print(cName + '(');
        emitParams(d);
        startblock(")");

        // emit the initialization code for each field
        for (AddrModeDecl.Operand o : d.getOperands()) {
            String n = o.name.image;
            println("this.$1 = $1;", n);
        }

        endblock();
    }

    private void emitAcceptMethod() {
        println("public void accept($visitor v) { v.visit(this); }");
    }

    //=========================================================================================
    // CODE TO EMIT SYMBOL SETS
    //=========================================================================================

    private void generateEnumerations() throws IOException {
        setPrinter(newClassPrinter("symbol", hashMapImport, null,
                tr("The <code>$symbol</code> class represents a symbol (or an enumeration as " +
                "declared in the instruction set description) relevant to the instruction set architecture. " +
                "For example register names, status bit names, etc are given here. This class provides a " +
                "type-safe enumeration for such symbolic names.")));
        generateEnumHeader();

        for ( EnumDecl d : arch.enums ) {
            generateEnumClass(d);
        }

        endblock();
        close();
    }

    private void generateEnumHeader() {
        println("public final String symbol;");
        println("public final int value;");
        println("");
        println("$symbol(String sym, int v) { symbol = sym;  value = v; }");
        println("public int getValue() { return value; }");
        println("public int getEncodingValue() { return value; }");
        println("");
    }

    private void generateEnumClass(EnumDecl d) {
        SymbolMapping m = d.map;
        String n = m.name.image;
        properties.setProperty("enum", n);
        if ( d instanceof EnumDecl.Subset ) {
            // this enumeration is a subset of another enumeration, but with possibly different
            // encoding
            EnumDecl.Subset sd = (EnumDecl.Subset)d;
            startblock("public static class $enum extends $1", sd.pname.image);
            println("public final int encoding;");
            println("public int getEncodingValue() { return encoding; }");
            println("private static HashMap set = new HashMap();");
            startblock("private static $enum new$enum(String n, int v, int ev)");
            println("$enum obj = new $enum(n, v, ev);");
            println("set.put(n, obj);");
            println("return obj;");
            endblock();

            println("$enum(String sym, int v, int ev) { super(sym, v); encoding = ev; }");

            for ( SymbolMapping.Entry e : m.getEntries() ) {
                String en = e.name;
                String EN = en.toUpperCase();
                SymbolMapping.Entry se = sd.parent.map.get(e.name);
                println("public static final $enum "+EN+" = new"+n+"(\""+en+"\", "+se.value+", "+e.value+");");
            }
        } else {
            // this enumeration is NOT a subset of another enumeration
            startblock("public static class $1 extends $symbol", n);
            println("private static HashMap set = new HashMap();");

            startblock("private static $enum new$enum(String n, int v)");
            println("$enum obj = new $enum(n, v);");
            println("set.put(n, obj);");
            println("return obj;");
            endblock();

            println("$enum(String sym, int v) { super(sym, v); }");

            for ( SymbolMapping.Entry e : m.getEntries() ) {
                String en = e.name;
                String EN = en.toUpperCase();
                println("public static final $enum "+EN+" = new"+n+"(\""+en+"\", "+e.value+");");
            }
        }

        endblock();
        println("");

        genGetMethod();

        println("");
    }

    private void genGetMethod() {
        startblock("public static $enum get_$enum(String name)");
        println("return ($enum)$enum.set.get(name);");
        endblock();
    }

    //=========================================================================================
    // CODE TO EMIT OPERAND TYPES
    //=========================================================================================
    private void generateOperandClasses() throws IOException {
        setPrinter(newInterfacePrinter("operand", hashMapImport, null,
                tr("The <code>$operand</code> interface represents operands that are allowed to " +
                "instructions in this architecture. Inner classes of this interface enumerate the possible " +
                "operand types to instructions and their constructors allow for dynamic checking of " +
                "correctness constraints as expressed in the instruction set description.")));

        p.println(tr("public void accept($opvisitor v);"));

        Printer vprinter = newInterfacePrinter("opvisitor", hashMapImport, null,
                tr("The <code>$opvisitor</code> interface allows clients to use the Visitor pattern to " +
                "resolve the types of operands to instructions."));
        // generate union operand types
        HashMap<String, HashSet<String>> interfaces = new HashMap<String, HashSet<String>>();
        for ( AddrModeSetDecl d : arch.addrSets ) {
            for ( AddrModeDecl.Operand o : d.unionOperands ) {
                OperandTypeDecl.Union d1 = (OperandTypeDecl.Union)o.getOperandType();
                println("public interface $1 extends $operand { }", d1.name);
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

        endblock();
        close();
        vprinter.endblock();
        vprinter.close();
    }

    private void generateOperandType(OperandTypeDecl d, Printer vprinter, HashMap<String, HashSet<String>> interfaces) {
        String otname = d.name.image;
        // generate visit method inside visitor
        vprinter.println(tr("public void visit($operand.$1 o);", otname));
        print("public class $1 implements $operand", otname);
        HashSet<String> intf = interfaces.get(otname);
        if ( intf != null ) for ( String str : intf ) {
            print(", "+str);
        }
        startblock(" ");
        if ( d.isValue() ) {
            generateSimpleType((OperandTypeDecl.Value)d);
        } else if ( d.isCompound() ) {
            generateCompoundType((OperandTypeDecl.Compound)d);
        }
        // generate accept method in operand class
        startblock("public void accept($opvisitor v)");
        println("v.visit(this);");
        endblock();
        endblock();
        println("");
    }

    private void generateSimpleType(OperandTypeDecl.Value d) {
        EnumDecl ed = arch.getEnum(d.kind.image);
        properties.setProperty("oname", d.name.image);
        properties.setProperty("kind", d.kind.image);
        if ( ed != null ) {
            println("public final $symbol.$kind symbol;");
            startblock("$oname(String s)");
            println("symbol = $symbol.get_$kind(s);");
            println("if ( symbol == null ) throw new Error();");
            endblock();
            startblock("$oname($symbol.$kind sym)");
            println("symbol = sym;");
            endblock();
        } else {
            println("public static final int low = "+d.low+";");
            println("public static final int high = "+d.high+";");
            println("public final int value;");
            startblock("$oname(int val)");
            println("value = $builder.checkValue(val, low, high);");
            endblock();
        }
    }

    private void generateCompoundType(OperandTypeDecl.Compound d) {
        // generate fields of compound operand
        for ( AddrModeDecl.Operand o : d.subOperands )
            println("public final $1 $2;", o.type, o.name);

        beginList("$1(", d.name);
        for ( AddrModeDecl.Operand o : d.subOperands ) {
            print("$1 $2", o.type, o.name);
        }
        endList(") ");
        startblock();
        for ( AddrModeDecl.Operand o : d.subOperands ) {
            println("this.$1 = $1;", o.name);
        }
        endblock();
    }

    //=========================================================================================
    // CODE TO EMIT OTHER STUFF
    //=========================================================================================

    private void emitParams(InstrDecl d) {
        beginList();
        for (AddrModeDecl.Operand o : d.getOperands()) {
            print("$operand.$1 $2", o.getOperandType().name, o.name);
        }
        endList();
    }

    private void generateBuilder() throws IOException {
        setPrinter(newClassPrinter("builder", hashMapImport, null, null));

        startblock("public static abstract class Single");
        println("public abstract $instr build($operand[] operands);");
        endblock();

        println("static final HashMap builders = new HashMap();");

        startblock("static Single addSingle(String name, Single s)");
        println("builders.put(name, s);");
        println("return s;");
        endblock();

        for ( InstrDecl d : arch.instructions ) {
            startblock("public static final Single $1  = addSingle($2, new Single()", d.innerClassName, d.name);
            startblock("public $instr build($operand[] operands)");
            List<AddrModeDecl.Operand> operands = d.getOperands();
            println("// assert operands.length == "+operands.size()+";");
            int cntr = 0;
            beginList("return new $instr.$1(", d.innerClassName);
            for ( AddrModeDecl.Operand o : operands ) {
                print("($operand.$1)operands[$2]", o.type, cntr);
                cntr++;
            }
            endListln(");");
            endblock();
            endblock(");");
        }

        startblock("public static int checkValue(int val, int low, int high)");
        startblock("if ( val < low || val > high )");
        println("throw new Error();");
        endblock();
        println("return val;");
        endblock();
        
        endblock();
        close();
    }
}
