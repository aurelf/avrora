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
        properties.setProperty("addr", className("AddrMode"));
        properties.setProperty("addrvisitor", className("AddrModeVisitor"));
        properties.setProperty("operand", className("Operand"));
        properties.setProperty("opvisitor", className("OperandVisitor"));
        properties.setProperty("visitor", className("InstrVisitor"));
        properties.setProperty("builder", className("InstrBuilder"));
        properties.setProperty("symbol", className("Symbol"));

        hashMapImport = new LinkedList<String>();
        hashMapImport.add("java.util.HashMap");

        generateOperandClasses();
        generateVisitor();
        generateInstrClasses();
        generateEnumerations();
        generateBuilder();
        generateAddrModeClasses();
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
        setPrinter(newAbstractClassPrinter("instr", null, null,
                tr("The <code>$instr</code> class is a container (almost a namespace) for " +
                "all of the instructions in this architecture. Each inner class represents an instruction " +
                "in the architecture and also extends the outer class.")));

        println("public abstract void accept($visitor v);");
        println("public abstract void accept($addrvisitor v);");

        generateJavaDoc("The <code>name</code> field stores a reference to the name of the instruction as a " +
                "string.");
        println("public final String name;");

        generateJavaDoc("The <code>size</code> field stores the size of the instruction in bytes.");
        println("public final int size;");

        startblock("protected $instr(String name, int size)");
        println("this.name = name;");
        println("this.size = size;");
        endblock();

        for (AddrModeDecl d : arch.addrModes) emitAddrInstrClass(d);
        for (AddrModeSetDecl d : arch.addrSets) emitAddrSetClass(d);

        for (InstrDecl d : arch.instructions) emitClass(d);
        endblock();
        close();
    }

    private void emitAddrInstrClass(AddrModeDecl d) {
        startblock("public abstract static class $1_Instr extends $instr", d.name);
        for (AddrModeDecl.Operand o : d.operands)
            println("public final $operand.$1 $2;", o.type, o.name);
        startblock("protected $1_Instr(String name, int size, $addr.$1 am)", d.name);

        println("super(name, size);");
        initFields("this.$1 = am.$1;", d.operands);
        endblock();
        // emit the accept method for the addressing mode visitor
        startblock("public void accept($addrvisitor v)");
        beginList("v.visit_$1(", d.name);
        for (AddrModeDecl.Operand o : d.operands) print(o.name.image);
        endListln(");");
        endblock();
        endblock();
    }

    private void emitAddrSetClass(AddrModeSetDecl d) {
        startblock("public abstract static class $1_Instr extends $instr", d.name);
        println("public final $addr.$1 am;", d.name);
        for (AddrModeDecl.Operand o : d.unionOperands)
            println("public final $operand $1;", o.name);
        startblock("protected $1_Instr(String name, int size, $addr.$1 am)", d.name);

        println("super(name, size);");
        println("this.am = am;", d.name);
        initFields("this.$1 = am.get_$1();", d.unionOperands);
        endblock();
        // emit the accept method for the addressing mode visitor
        startblock("public void accept($addrvisitor v)");
        println("am.accept(v);");
        endblock();
        endblock();
    }

    private void emitClass(InstrDecl d) {
        String cName = d.getInnerClassName();
        cName = StringUtil.trimquotes(cName.toUpperCase());
        boolean hasSuper = d.addrMode.localDecl == null;
        String sup = hasSuper ? addrModeName(d) + "_Instr" : tr("$instr");
        startblock("public static class $1 extends $2", cName, sup);

        emitFields(d, hasSuper);
        emitConstructor(cName, d, hasSuper);
        println("public void accept($visitor v) { v.visit(this); }");
        if ( !hasSuper ) {
            startblock("public void accept($addrvisitor v)");
            beginList("v.visit_$1(", addrModeName(d));
            for (AddrModeDecl.Operand o : d.getOperands()) print(o.name.image);
            endListln(");");
            endblock();
        }

        endblock();
        println("");
    }

    private void emitFields(InstrDecl d, boolean hasSuper) {
        // emit the declaration of the fields
        if ( !hasSuper ) {
            for (AddrModeDecl.Operand o : d.getOperands())
                println("public final $operand.$1 $2;", o.type, o.name);
        }
    }

    private void emitConstructor(String cName, InstrDecl d, boolean hasSuper) {
        // emit the declaration of the constructor
        startblock("$1(int size, $addr.$2 am)", cName, addrModeName(d));

        if ( hasSuper ) {
            println("super($1, size, am);", d.name);
        } else {
            println("super($1, size);", d.name);
            initFields("this.$1 = am.$1;", d.getOperands());
        }
        endblock();
    }

    private void initFields(String format, List<AddrModeDecl.Operand> list) {
        for (AddrModeDecl.Operand o : list) {
            String n = o.name.image;
            println(format, n);
        }
    }

    private String addrModeName(InstrDecl d) {
        if ( d.addrMode.localDecl != null ) return javaName(d.name.image);
        else return d.addrMode.ref.image;
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
        initFields("this.$1 = $1;", d.subOperands);
        endblock();
    }

    //=========================================================================================
    // CODE TO EMIT OTHER STUFF
    //=========================================================================================

    private void generateBuilder() throws IOException {
        setPrinter(newAbstractClassPrinter("builder", hashMapImport, null, null));

        println("public abstract $instr build(int size, $addr am);");

        println("static final HashMap builders = new HashMap();");

        startblock("static $builder add(String name, $builder b)");
        println("builders.put(name, b);");
        println("return b;");
        endblock();

        for ( InstrDecl d : arch.instructions ) {
            startblock("public static class $1_builder extends $builder", d.innerClassName);
            startblock("public $instr build(int size, $addr am)");
            println("return new $instr.$1(size, ($addr.$2)am);", d.innerClassName, addrModeName(d));
            endblock();
            endblock();
        }

        for ( InstrDecl d : arch.instructions ) {
            println("public static final $builder $1 = add($2, new $1_builder());", d.innerClassName, d.name);
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

    void generateAddrModeClasses() throws IOException {

        setPrinter(newInterfacePrinter("addr", hashMapImport, null,
                tr("The <code>$addr</code> class represents an addressing mode for this architecture. An " +
                "addressing mode fixes the number and type of operands, the syntax, and the encoding format " +
                "of the instruction.")));

        println("public void accept($addrvisitor v);");

        for ( AddrModeSetDecl as : arch.addrSets ) {
            startblock("public interface $1 extends $addr", as.name);
            for ( AddrModeDecl.Operand o : as.unionOperands )
                println("public $operand get_$1();", o.name);
            endblock();
        }

        List<AddrModeDecl> list = new LinkedList<AddrModeDecl>();
        for ( AddrModeDecl am : arch.addrModes ) list.add(am);
        for ( InstrDecl id : arch.instructions ) {
            // for each addressing mode declared locally
            if ( id.addrMode.localDecl != null )
                list.add(id.addrMode.localDecl);
        }
        for ( AddrModeDecl am : list ) emitAddrMode(am);
        endblock();
        close();

        emitAddrModeVisitor(list);
    }

    private void emitAddrModeVisitor(List<AddrModeDecl> list) throws IOException {
        setPrinter(newInterfacePrinter("addrvisitor", hashMapImport, null,
                tr("The <code>$addrvisitor</code> interface implements the visitor pattern for addressing modes.")));
        for ( AddrModeDecl am : list ) {
            beginList("public void visit_$1(", javaName(am.name.image));
            for ( AddrModeDecl.Operand o : am.operands ) {
                OperandTypeDecl d = o.getOperandType();
                print("$operand.$1 $2", d.name, o.name);
            }
            endListln(");");
        }
        endblock();
        close();
    }

    private void emitAddrMode(AddrModeDecl am) {
        String amName = javaName(am.name.image);
        beginList("public static class $1 implements ", amName);
        print("$addr");
        for ( AddrModeSetDecl as : am.sets ) print(as.name.image);
        endList(" ");
        // generate fields
        startblock();
        emitOperandFields(am.operands);
        // generate constructor
        beginList("public $1(", amName);
        for ( AddrModeDecl.Operand o : am.operands ) {
            OperandTypeDecl d = o.getOperandType();
            print("$operand.$1 $2", d.name, o.name);
        }
        endList(") ");
        // generate field writes
        startblock();
        initFields("this.$1 = $1;", am.operands);
        endblock();
        // generate accept method
        startblock("public void accept($addrvisitor v)");
        beginList("v.visit_$1(", amName);
        for ( AddrModeDecl.Operand o : am.operands ) {
            print(o.name.image);
        }
        endListln(");");
        endblock();
        for ( AddrModeDecl.Operand o : am.operands ) {
            OperandTypeDecl d = o.getOperandType();
            println("public $operand get_$2() { return $2; }", d.name, o.name);
        }
        endblock();
    }

    private void emitOperandFields(List<AddrModeDecl.Operand> list) {
        for ( AddrModeDecl.Operand o : list ) {
            OperandTypeDecl d = o.getOperandType();
            println("public $operand.$1 $2;", d.name, o.name);
        }
    }

}
