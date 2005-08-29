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

package jintgen.gen.disassembler;

import avrora.util.Util;
import jintgen.isdl.*;
import jintgen.isdl.parser.Token;
import jintgen.jigir.*;
import jintgen.gen.disassembler.DTBuilder;
import jintgen.gen.Generator;
import jintgen.gen.ConstantPropagator;
import avrora.util.*;

import java.util.*;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

/**
 * The <code>DisassemblerGenerator</code> class is a generator that can create a Java class
 * from an architecture description that disassembles binary instructions into the instruction
 * IR generated by the <code>InstrIRGenerator</code>.
 *
 * @author Ben L. Titzer
 */
public class DisassemblerGenerator extends Generator {

    static int WORD_SIZE;
    static boolean LITTLE_ENDIAN;
    static boolean LITTLE_BIT_ENDIAN;

    protected final Option.Bool MULTI_TREE = options.newOption("multiple-trees", false,
            "This option selects whether the disassembler generator will create multiple decode trees " +
            "(i.e. one per priority level) or whether it will create a single, unified tree. In some " +
            "instances one large tree is more efficient, while in others multiple smaller trees may be " +
            "more efficient.");
    protected final Option.Bool PARALLEL_TREE = options.newOption("parallel-trees", false,
            "This option selects whether the disassembler generator will create multiple decode trees " +
            "that are applied in parallel to resolve both the addressing mode and instruction. For " +
            "complex architectures, this can result in tremendously reduced tree sizes. For small architecture, " +
            "the result can be less efficient.");
    protected final Option.Bool CHAINED = options.newOption("chained-trees", false,
            "This option selects whether the disassembler generator will chain the decoders from multiple " +
            "priority levels together into one larger tree. This can reduce the complexity of the main " +
            "decoder loop, but is only supported for non-parallel decoder implementations.");
    protected final Option.Long WORD = options.newOption("word-size", 16,
            "This option controls the word size (in bits) that is used when generating the disassembler " +
            "code. The disassembler reads fields from individual words of the instruction stream. This " +
            "option tunes whether the disassembler will read 8, 16, 32, etc bits from the instruction " +
            "stream at a time.");
    protected final Option.Str ENDIAN = options.newOption("endian", "little",
            "This option controls whether the generated disassembler assumes big-endian or little-endian " +
            "ordering of bytes within words.");
    protected final Option.Str BIT_ENDIAN = options.newOption("bit-endian", "big",
            "This option controls whether the generated disassembler assumes big-endian or little-endian " +
            "ordering of bits within words. This is important for the description of encodings of " +
            "instructions. When set to \"big\", the disassembler generator assumes that the first logical " +
            "bit of an encoding description is the most significant bit of the word.");

    Verbose.Printer verbose = Verbose.getVerbosePrinter("jintgen.disassem");
    Verbose.Printer verboseDump = Verbose.getVerbosePrinter("jintgen.disassem.tree");
    Verbose.Printer dotDump = Verbose.getVerbosePrinter("jintgen.disassem.dot");

    int numEncodings = 0;
    int encodingInstances = 0;
    int instrs = 0;
    int pseudoInstrs = 0;
    int minInstrLength = Integer.MAX_VALUE;
    int maxInstrLength = 0;

    ReaderImplementation reader;
    Decoder implementation;

    public void generate() throws Exception {
        List<String> imports = new LinkedList<String>();
        imports.add("java.util.Arrays");
        initStatics();
        setPrinter(newClassPrinter("disassembler", imports, null,
                tr("The <code>$disassembler</code> class decodes bit patterns into instructions. It has " +
                "been generated automatically by jIntGen from a file containing a description of the instruction " +
                "set and their encodings.\n\n" +
                "The following options have been specified to tune this implementation:\n\n" +
                "</p>-word-size=$1\n"+
                "</p>-parallel-trees=$2\n"+
                "</p>-multiple-trees=$3\n"+
                "</p>-chained-trees=$4\n",
                        WORD.get(), PARALLEL_TREE.get(), MULTI_TREE.get(), CHAINED.get())));

        generateHeader();
        generateDecodeTables();
        int maxprio = getMaxPriority();
        if ( PARALLEL_TREE.get() )
            implementation = new Decoder.Parallel(this, maxprio);
        else
            implementation = new Decoder.Serial(this, maxprio);

        reader = new ReaderImplementation(this);
        visitInstructions();
        reader.generateOperandReaders();
        implementation.compute();
        implementation.generate();
        if ( verboseDump.enabled ) {
            implementation.print(verboseDump);
        }
        if ( dotDump.enabled ) {
            implementation.dotDump();
        }
        Terminal.nextln();
        TermUtil.reportQuantity("Instructions", instrs, "");
        TermUtil.reportQuantity("Pseudo-instructions", pseudoInstrs, "");
        TermUtil.reportQuantity("Encodings", numEncodings, "");
        TermUtil.reportQuantity("Encoding Instances", encodingInstances, "");
        TermUtil.reportQuantity("Decoding Trees", implementation.numTrees, "");
        TermUtil.reportQuantity("Nodes", implementation.treeNodes, "");
        endblock();
    }

    private void initStatics() {
        properties.setProperty("addr", className("AddrMode"));
        properties.setProperty("instr", className("Instr"));
        properties.setProperty("operand", className("Operand"));
        properties.setProperty("opvisitor", className("OperandVisitor"));
        properties.setProperty("visitor", className("InstrVisitor"));
        properties.setProperty("builder", className("InstrBuilder"));
        properties.setProperty("symbol", className("Symbol"));
        properties.setProperty("disassembler", className("Disassembler"));

        WORD_SIZE = (int)WORD.get();
        LITTLE_ENDIAN = "little".equals(ENDIAN.get());
        LITTLE_BIT_ENDIAN = "little".equals(BIT_ENDIAN.get());
    }

    private int getMaxPriority() {
        int maxprio = 0;
        for ( InstrDecl d : arch.instructions ) {
            if (!d.pseudo) {
                for ( AddrModeDecl am : d.addrMode.addrModes ) {
                    for ( EncodingDecl ed : am.encodings ) {
                        int prio = ed.getPriority();
                        if ( prio > maxprio ) maxprio = prio;
                    }
                }
            }
        }
        return maxprio;
    }

    private void visitInstructions() {
        for ( InstrDecl d : arch.instructions ) {
            if ( d.pseudo ) {
                pseudoInstrs++;
            } else {
                instrs++;
                for ( AddrModeDecl am : d.addrMode.addrModes ) {
                    int cntr = 0;
                    for ( EncodingDecl ed : am.encodings ) {
                        addEncodingInfo(d, am, ed);
                        String eName = encodingName(am, cntr);
                        reader.addEncoding(eName, ed, am);
                        cntr++;
                    }
                }
            }
        }
    }

    private String encodingName(AddrModeDecl am, int cntr) {
        return javaName(am.name+"_"+cntr);
    }

    private void generateHeader() {
        startblock("public static class InvalidInstruction extends Exception");
        startblock("InvalidInstruction(int pc) ");
        println("super(\"Invalid instruction at \"+pc);");
        endblock();
        endblock();
    }

    private void addEncodingInfo(InstrDecl d, AddrModeDecl am, EncodingDecl ed) {
        EncodingInst ei = new EncodingInst(d, am, ed);
        implementation.add(ei);
        encodingInstances++;
    }

    private void generateDecodeTables() {
        for ( EnumDecl d : arch.enums ) {
            generateEnumDecodeTable(d);
        }
    }

    void generateEnumDecodeTable(EnumDecl d) {
        int max = getTableSize(d);
        String[] symbol = new String[max+1];
        for ( SymbolMapping.Entry e : d.map.getEntries() ) {
            symbol[e.value] = e.name;
        }
        String tablename = d.name+"_table";
        startblock("static final $symbol.$1[] $2 =", d.name, tablename);
        for ( int cntr = 0; cntr < symbol.length; cntr++ ) {
            if ( symbol[cntr] == null ) print("null");
            else print("$symbol.$1.$2", d.name, symbol[cntr].toUpperCase());
            if ( cntr != symbol.length - 1) print(", ");
            emitDecodeComment(max, cntr, symbol);
            nextln();
        }
        endblock(";");
    }

    private void emitDecodeComment(int max, int cntr, String[] symbol) {
        int lb = Arithmetic.highestBit(max);
        if ( lb <= 0 ) lb = 0;
        print(" // $1 (0b$2) -> $3", cntr, StringUtil.toBin(cntr, lb+1), symbol[cntr]);
    }

    private int getTableSize(EnumDecl d) {
        int max = 0;
        for ( SymbolMapping.Entry e : d.map.getEntries() ) {
            if ( e.value > max ) max = e.value;
        }
        if ( max > 64 && max > (d.map.size() * 2) ) {
            throw Util.failure("Enumeration "+StringUtil.quote(d.name)+" too sparse");
        }
        return max;
    }

    abstract class DTNodeImpl {
        String def;
        String action;
        final String nname;
        final int left;
        final int mask;
        final int length;

        DTNodeImpl(DTNode dt, String act, String d) {
            nname = nodeName(dt);
            def = d;
            length = dt.right_bit - dt.left_bit + 1;
            left = DisassemblerGenerator.nativeBitOrder(dt.left_bit, length);
            mask = -1 >>> (32 - length);
            action = act;
        }
        abstract void add(int value, String nname);
        abstract void generate();
    }

    class DTArrayNodeImpl extends DTNodeImpl {
        int current;
        String[] vals;

        DTArrayNodeImpl(DTNode dt, String action, String d) {
            super(dt, action, d);
            vals = new String[1 << length];
        }
        void add(int value, String nname) {
            if ( value == -1 ) def = nname;
            else {
                vals[value] = nname;
            }
        }
        void generate() {
            beginList("DTNode $1 = new DTArrayNode($2, $3, $4, new DTNode[] {", nname, action, left, mask);
            for ( String str : vals ) {
                if ( str == null ) print(def);
                else print(str);
            }
            endListln("});");
        }

    }

    class DTSortedNodeImpl extends DTNodeImpl {
        LinkedList<String> init = new LinkedList<String>();
        LinkedList<Integer> values = new LinkedList<Integer>();
        DTSortedNodeImpl(DTNode dt, String act, String d) {
            super(dt, act, d);
        }
        void add(int value, String nname) {
            if ( value == -1 ) def = nname;
            else {
                init.add(nname);
                values.add(value);
            }
        }

        void generate() {
            beginList("DTNode $1 = new DTSortedNode($2, $3, $4, new int[] {", nname, action, left, mask);
            for ( Integer i : values ) {
                print(i.toString());
            }
            endList("}, ");
            beginList("new DTNode[] {");
            for ( String str : init ) {
                print(str);
            }
            endListln("}, $1);", def);
        }
    }

    String nodeName(DTNode cdt) {
        if ( cdt.isLeaf() ) return "T"+cdt.number;
        else return "N"+cdt.number;
    }

    DTNodeImpl newDTNode(DTNode dt, String action, String def) {
        int size = 1 << (dt.right_bit - dt.left_bit);
        if ( size > 16 && (dt.getChildren().size() < (size/2)) ) return new DTSortedNodeImpl(dt, action, def);
        return new DTArrayNodeImpl(dt, action, def);
    }

    public static int nativeBitOrder(int left_bit, int length) {
        if ( !LITTLE_BIT_ENDIAN ) return WORD_SIZE - left_bit - length;
        return left_bit;
    }
}
