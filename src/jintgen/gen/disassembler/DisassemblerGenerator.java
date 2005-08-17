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
import jintgen.gen.disassembler.DecodingTree;
import jintgen.gen.Generator;
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

    protected static final int LARGEST_INSTR = 15;
    protected static final int WORD_SIZE = 16;

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

    Printer printer;
    Verbose.Printer verbose = Verbose.getVerbosePrinter("jintgen.disassem");
    Verbose.Printer verboseDump = Verbose.getVerbosePrinter("jintgen.disassem.tree");
    Verbose.Printer dotDump = Verbose.getVerbosePrinter("jintgen.disassem.dot");
    static String instrClassName;
    static String symbolClassName;

    int indent;

    int methods;
    int encodingNumber = 0;
    int instrs = 0;
    int treeNodes = 0;
    int numTrees = 0;
    int pseudoInstrs = 0;

    HashSet<EncodingInfo> pseudo;

    DecoderImplementation implementation;

    class DecoderImplementation {
        boolean multiple;
        boolean parallel;

        final HashMap<String, DecodingTree> finalTrees;

        final DecodingTree[] completeTree;

        DecoderImplementation(int maxprio) {
            parallel = PARALLEL_TREE.get();
            multiple = MULTI_TREE.get();
            completeTree = new DecodingTree[maxprio+1];
            finalTrees = new HashMap<String, DecodingTree>();
        }

        void compute() {
            for ( int cntr = 0; cntr < completeTree.length; cntr++ ) {
                DecodingTree dt = completeTree[cntr];
                if ( dt == null ) continue;
                dt.compute(0);
                if ( parallel ) {
                    labelTreeWithInstrs(dt);
                    addFinalTree("instr"+cntr, new TreeFactorer(dt).getNewTree());
                    labelTreeWithAddrModes(dt);
                    addFinalTree("addr"+cntr, new TreeFactorer(dt).getNewTree());
                } else {
                    addFinalTree("root"+cntr, dt);
                }
            }
        }

        void addFinalTree(String n, DecodingTree t) {
            finalTrees.put(n, t);
            treeNodes += DGUtil.numberNodes(t);
        }

        void print(Printer p) {
            for ( DecodingTree dt : finalTrees.values() )
                DGUtil.printTree(p, dt);
        }

        void dotDump() throws Exception {
            for ( Map.Entry<String, DecodingTree> e : finalTrees.entrySet() ) {
                String name = e.getKey();
                FileOutputStream fos = new FileOutputStream(name+".dot");
                Printer p = new Printer(new PrintStream(fos));
                DGUtil.printDotTree(name, e.getValue(), p);
            }
        }

        void add(EncodingInfo ei) {
            int priority = ei.encoding.getPriority();
            if ( !multiple ) priority = 0;
            DecodingTree dt = completeTree[priority];
            if ( dt == null ) dt = completeTree[priority] = new DecodingTree();
            dt.addEncoding(ei);
        }

        void generate() {
            for ( Map.Entry<String, DecodingTree> e : finalTrees.entrySet() ) {
                generateDecodingTree("make_"+e.getKey(), e.getValue());
            }
        }
    }

    public DisassemblerGenerator() {
        pseudo = new HashSet<EncodingInfo>();
    }

    public void generate() throws Exception {
        List<String> imports = new LinkedList<String>();
        imports.add("avrora.util.Arithmetic");
        imports.add("java.util.Arrays");
        printer = newClassPrinter(className("Disassembler"), imports, null);
        instrClassName = className("Instr");
        symbolClassName = className("Symbol");

        generateHeader();
        generateDecodeTables();
        generateNodeClasses();
        int maxprio = getMaxPriority();
        implementation = new DecoderImplementation(maxprio);
        addInstructions();
        implementation.compute();
        implementation.generate();
        generateRoot();
        if ( verboseDump.enabled ) {
            implementation.print(verboseDump);
        }
        if ( dotDump.enabled ) {
            implementation.dotDump();
        }
        Terminal.nextln();
        TermUtil.reportQuantity("Instructions", instrs, "");
        TermUtil.reportQuantity("Pseudo-instructions", pseudoInstrs, "");
        TermUtil.reportQuantity("Encodings", encodingNumber, "");
        TermUtil.reportQuantity("Decoding Trees", numTrees, "");
        TermUtil.reportQuantity("Nodes", treeNodes, "");
        printer.endblock();
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

    private void addInstructions() {
        for ( InstrDecl d : arch.instructions ) visitInstr(d);
    }

    private void generateHeader() {
        printer.startblock("static class InvalidInstruction extends Exception");
        printer.startblock("InvalidInstruction(int word1, int pc) ");
        printer.println("super(\"Invalid instruction at \"+pc);");
        printer.endblock();
        printer.endblock();
    }

    private void generateRoot() {
        printer.startblock(instrClassName+" decode_root(int word1) throws InvalidInstruction ");
        printer.println(instrClassName+" i = null;");
        invalidInstr();
        printer.endblock();
    }

    public void visitInstr(InstrDecl d) {
        // for now, we ignore pseudo instructions.
        if ( d.pseudo ) {
            pseudoInstrs++;
        } else {
            instrs++;
            for ( AddrModeDecl am : d.addrMode.addrModes ) {
                for ( EncodingDecl ed : am.encodings )
                    addEncodingInfo(d, am, ed);
            }
        }
    }

    private void addEncodingInfo(InstrDecl d, AddrModeDecl am, EncodingDecl ed) {
        EncodingInfo ei = new EncodingInfo(d, am, encodingNumber, ed);
        implementation.add(ei);
        encodingNumber++;
    }

    private void invalidInstr() {
        printer.println("throw new InvalidInstruction(word1, pc);");
    }

    private void returnNull() {
        printer.println("return null;");
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
        String symname = symbolClassName+"."+d.name;
        printer.startblock("static final "+symname+"[] "+tablename+" =");
        for ( int cntr = 0; cntr < symbol.length; cntr++ ) {
            if ( symbol[cntr] == null ) printer.print("null");
            else printer.print(symname+"."+symbol[cntr].toUpperCase());
            if ( cntr != symbol.length - 1) printer.print(", ");
            emitDecodeComment(max, cntr, symbol);
            printer.nextln();
        }
        printer.endblock(";");
    }

    private void emitDecodeComment(int max, int cntr, String[] symbol) {
        int lb = Arithmetic.highestBit(max);
        if ( lb <= 0 ) lb = 0;
        String binaryRep = StringUtil.toBin(cntr, lb+1);
        printer.print(" // "+cntr+" (0b"+binaryRep+") -> "+symbol[cntr]);
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

    public static void generateRead(Printer p, int left_bit, int right_bit) {
        int high_bit = nativeBitOrder(left_bit);
        int low_bit = nativeBitOrder(right_bit);
        int mask = Arithmetic.getBitRangeMask(low_bit, high_bit);

        int word = 1 + (left_bit / WORD_SIZE);

        if ( low_bit > 0 )
            p.print("((word"+word+" >> "+low_bit+") & "+StringUtil.to0xHex(mask, 5)+")");
        else
            p.print("(word"+word+" & "+StringUtil.to0xHex(mask, 5)+")");
    }

    public void generateNodeClasses() {
        printer.startblock("static abstract class DTNode");
        printer.println("final int left_bit;");
        printer.println("final int mask;");
        printer.println("DTNode(int lb, int msk) { left_bit = lb; mask = msk; }");
        printer.println("abstract DTNode move(int val);");
        printer.endblock();

        printer.startblock("static class DTArrayNode extends DTNode");
        printer.println("final DTNode[] nodes;");
        printer.startblock("DTArrayNode(int lb, int msk, DTNode[] n)");
        printer.println("super(lb, msk);");
        printer.println("nodes = n;");
        printer.endblock();
        printer.startblock("DTNode move(int val)");
        printer.println("return nodes[val];");
        printer.endblock();
        printer.endblock();

        printer.startblock("static class DTSortedNode extends DTNode");
        printer.println("final DTNode def;");
        printer.println("final DTNode[] nodes;");
        printer.println("final int[] values;");
        printer.startblock("DTArrayNode(int lb, int msk, int[] v, DTNode[] n, DTNode d)");
        printer.println("super(lb, msk);");
        printer.println("values = v;");
        printer.println("nodes = n;");
        printer.println("def = d;");
        printer.endblock();
        printer.startblock("DTNode move(int val)");
        printer.println("int ind = Arrays.binarySearch(values, val);");
        printer.println("if ( ind < values.length && values[ind] == val );");
        printer.println("    return nodes[ind];");
        printer.println("else");
        printer.println("    return def;");
        printer.endblock();
        printer.endblock();
    }

    public void generateDecodingTree(String methname, DecodingTree dt) {
        printer.startblock("static DTNode "+methname+"()");
        HashSet<DecodingTree> set = new HashSet<DecodingTree>();
        String n = generateDecodingNode(dt, set);
        printer.println("return "+n+";");
        printer.endblock();
    }

    abstract class DTNodeImpl {
        String def = "ERROR";
        final String nname;
        final int left;
        final int mask;
        LinkedList<String> init;
        DTNodeImpl(DecodingTree dt) {
            nname = "node"+dt.node_num;
            left = dt.left_bit;
            mask = -1 >>> (32 - (dt.right_bit - dt.left_bit + 1));
            init = new LinkedList<String>();
        }
        abstract void add(int value, String nname);
        abstract void generate();

        protected void printInits() {
            boolean first2 = true;
            for ( String str : init ) {
                if ( !first2 ) {
                    printer.print(", ");
                }
                printer.print(str);
                first2 = false;
            }
        }
    }

    class DTArrayNodeImpl extends DTNodeImpl {
        int current;

        DTArrayNodeImpl(DecodingTree dt) {
            super(dt);
        }
        void add(int value, String nname) {
            if ( value == -1 ) def = nname;
            else {
                while ( current < value ) {
                    current++;
                    init.add(def);
                }
                current++;
                init.add(nname);
            }
        }
        void generate() {
            printer.print("DTNode "+nname+" = new DTArrayNode("+left+", "+mask+", new DTNode[] {");
            boolean first = true;
            printInits();
            printer.println("});");
        }
    }

    class DTSortedNodeImpl extends DTNodeImpl {
        LinkedList<Integer> values = new LinkedList<Integer>();
        DTSortedNodeImpl(DecodingTree dt) {
            super(dt);
        }
        void add(int value, String nname) {
            if ( value == -1 ) def = nname;
            else {
                init.add(nname);
                values.add(value);
            }
        }
        void generate() {
            printer.print("DTNode "+nname+" = new DTSortedNode("+left+", "+mask+", new int[] {");
            boolean first = true;
            for ( Integer i : values ) {
                if ( !first ) {
                    printer.print(", ");
                }
                printer.print(i.toString());
                first = false;
            }
            printer.print("}, new DTNode[] {");
            printInits();
            printer.println(", "+def+"});");
        }

    }

    private String generateDecodingNode(DecodingTree dt, HashSet<DecodingTree> set) {
        DTNodeImpl nodeImpl = newDTNode(dt);
        List<Map.Entry<Integer, DecodingTree>> children = new LinkedList<Map.Entry<Integer, DecodingTree>>(dt.children.entrySet());
        Collections.sort(children, ENTRY_COMPARATOR);
        for ( Map.Entry<Integer, DecodingTree> e : children ) {
            int value = e.getKey();
            DecodingTree cdt = e.getValue();
            if ( !set.contains(cdt) ) generateDecodingNode(cdt, set);
            nodeImpl.add(value, "node"+cdt.node_num);
        }
        nodeImpl.generate();
        set.add(dt);
        return nodeImpl.nname;
    }

    private DTNodeImpl newDTNode(DecodingTree dt) {
        int size = 1 << (dt.right_bit - dt.left_bit);
        if ( size > 16 && (dt.children.size() < (size/2)) ) return new DTSortedNodeImpl(dt);
        return new DTArrayNodeImpl(dt);
    }

    public static Comparator<Map.Entry<Integer, DecodingTree>> ENTRY_COMPARATOR = new Comparator<Map.Entry<Integer, DecodingTree>>() {
        public int compare(Map.Entry<Integer, DecodingTree> a, Map.Entry<Integer, DecodingTree> b) {
            return a.getKey() - b.getKey();
        }
    };

    public static int nativeBitOrder(int bit) {
        return 15-(bit % WORD_SIZE);
    }

    public static String getInstrClassName(EncodingInfo ei) {
        return instrClassName+"."+ei.instr.getInnerClassName();
    }

    public static void labelTreeWithInstrs(DecodingTree dt) {
        HashSet<InstrDecl> instrs = new HashSet<InstrDecl>();
        for ( EncodingInfo ei : dt.highPrio )
            instrs.add(ei.instr);
        for ( EncodingInfo ei : dt.lowPrio )
            instrs.add(ei.instr);

        // label all the children
        if ( instrs.size() == 1 ) {
            dt.setLabel(instrs.iterator().next().innerClassName);
            // label all the children
            for ( DecodingTree cdt : dt.children.values() )
            labelTree("*", cdt);
        } else {
            dt.setLabel("-");
            for ( DecodingTree cdt : dt.children.values() )
                labelTreeWithInstrs(cdt);
        }
    }

    public static void labelTreeWithAddrModes(DecodingTree dt) {
        HashSet<AddrModeDecl> addrs = new HashSet<AddrModeDecl>();
        for ( EncodingInfo ei : dt.highPrio )
            addrs.add(ei.addrMode);
        for ( EncodingInfo ei : dt.lowPrio )
            addrs.add(ei.addrMode);

        if ( addrs.size() == 1 ) {
            // label all the children
            dt.setLabel(addrs.iterator().next().name.image);
            for ( DecodingTree cdt : dt.children.values() )
            labelTree("*", cdt);
        } else {
            // label all the children
            dt.setLabel("-");
            for ( DecodingTree cdt : dt.children.values() )
                labelTreeWithAddrModes(cdt);
        }
    }

    public static void labelTree(String l, DecodingTree dt) {
        dt.setLabel(l);
        for ( DecodingTree cdt : dt.children.values() )
            labelTree(l, cdt);
    }

    public static Comparator<InstrDecl> INSTR_COMPARATOR = new Comparator<InstrDecl>() {
        public int compare(InstrDecl a, InstrDecl b) {
            return a.name.image.compareTo(b.name.image);
        }
    };

    public static Comparator<AddrModeDecl> ADDR_COMPARATOR = new Comparator<AddrModeDecl>() {
        public int compare(AddrModeDecl a, AddrModeDecl b) {
            return a.name.image.compareTo(b.name.image);
        }
    };
}
