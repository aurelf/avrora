/**
 * Copyright (c) 2005, Regents of the University of California
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

import avrora.util.Printer;
import avrora.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * @author Ben L. Titzer
 */
class DecoderImplementation {
    boolean multiple;
    boolean parallel;
    int treeNodes = 0;
    int numTrees = 0;

    final HashMap<String, DTNode> finalTrees;

    final DTBuilder[] completeTree;
    private DisassemblerGenerator dGen;
    Printer p;

    DecoderImplementation(DisassemblerGenerator disassemblerGenerator, int maxprio) {
        this.dGen = disassemblerGenerator;
        parallel = disassemblerGenerator.PARALLEL_TREE.get();
        multiple = disassemblerGenerator.MULTI_TREE.get();
        completeTree = new DTBuilder[maxprio+1];
        finalTrees = new HashMap<String, DTNode>();
        p = dGen.printer;
    }

    void compute() {
        for ( int cntr = 0; cntr < completeTree.length; cntr++ ) {
            DTBuilder dt = completeTree[cntr];
            if ( dt == null ) continue;
            DTNode root = dt.compute();
            if ( parallel ) {
                addFinalTree("instr"+cntr, optimizeInstrs(root));
                addFinalTree("addr"+cntr, optimizeAddrs(root));
            } else {
                addFinalTree("root"+cntr, root);
            }
        }
    }

    private DTNode optimizeAddrs(DTNode root) {
        dGen.labelTreeWithEncodings(root);
        DTNode newTree = new TreeFactorer(root).getNewTree();
        return newTree;
    }

    private DTNode optimizeInstrs(DTNode root) {
        dGen.labelTreeWithInstrs(root);
        root = DGUtil.removeAll(root, "*");
        DTNode newTree = new TreeFactorer(root).getNewTree();
        return newTree;
    }

    void addFinalTree(String n, DTNode t) {
        finalTrees.put(n, t);
        treeNodes += DGUtil.numberNodes(t);
        numTrees++;
    }

    void print(Printer p) {
        for ( DTNode dt : finalTrees.values() )
            DGUtil.printTree(p, dt);
    }

    void dotDump() throws Exception {
        for ( Map.Entry<String, DTNode> e : finalTrees.entrySet() ) {
            String name = e.getKey();
            FileOutputStream fos = new FileOutputStream(name+".dot");
            Printer p = new Printer(new PrintStream(fos));
            DGUtil.printDotTree(name, e.getValue(), p);
        }
    }

    void add(EncodingInst ei) {
        int priority = ei.encoding.getPriority();
        if ( !multiple ) priority = 0;
        DTBuilder dt = completeTree[priority];
        if ( dt == null ) dt = completeTree[priority] = new DTBuilder();
        dt.addEncoding(ei);
    }

    void generate() {
        generateNodeClasses();
        generateFields();
        generateTreeBuilderMethods();
        generateTreeFields();
        generateEntryPoints();
        if ( parallel ) {
            generateParallelRoot();
        } else {
            generateRoot();
        }

    }

    private void generateTreeFields() {
        for ( Map.Entry<String, DTNode> e : finalTrees.entrySet() ) {
            String treeName = e.getKey();
            dGen.generateJavaDoc("The <code>"+treeName+"</code> field stores a reference to the root of " +
                    "a decoding tree. It is the starting point for decoding a bit pattern.");
            p.println("private static final DTNode "+treeName+" = make_"+treeName+"();");
        }
    }

    private void generateTreeBuilderMethods() {
        for ( Map.Entry<String, DTNode> e : finalTrees.entrySet() ) {
            String treeName = e.getKey();
            ActionGetter ag;
            if ( treeName.startsWith("instr") )
                ag = new InstrActionGetter();
            else ag = new AddrModeActionGetter();
            generateDecodingTree("make_"+treeName, ag, e.getValue());
        }
    }

    public void generateNodeClasses() {
        dGen.generateJavaDoc("The <code>DTNode</code> class represents a node in a decoding graph. Each node " +
                "compares a range of bits and branches to other nodes based on the value. Each node may " +
                "also have an action (such as fixing the addressing mode or instruction) that is " +
                "executed when the node is reached. Actions on the root node are not executed.");
        p.startblock("static abstract class DTNode");
        p.println("final int left_bit;");
        p.println("final int mask;");
        p.println("final Action action;");
        p.println("DTNode(Action a, int lb, int msk) { action = a; left_bit = lb; mask = msk; }");
        p.println("abstract DTNode move(int val);");
        p.endblock();

        dGen.generateJavaDoc("The <code>DTArrayNode</code> implementation is used for small (less than 32) " +
                "and dense (more than 50% full) edge lists. It uses an array of indices that is " +
                "directly indexed by the bits extracted from the stream.");
        p.startblock("static class DTArrayNode extends DTNode");
        p.println("final DTNode[] nodes;");
        p.startblock("DTArrayNode(Action a, int lb, int msk, DTNode[] n)");
        p.println("super(a, lb, msk);");
        p.println("nodes = n;");
        p.endblock();
        p.startblock("DTNode move(int val)");
        p.println("return nodes[val];");
        p.endblock();
        p.endblock();

        dGen.generateJavaDoc("The DTSortedNode implementation is used for sparse edge lists. It uses a " +
                "sorted array of indices and uses binary search on the value of the bits.");
        p.startblock("static class DTSortedNode extends DTNode");
        p.println("final DTNode def;");
        p.println("final DTNode[] nodes;");
        p.println("final int[] values;");
        p.startblock("DTSortedNode(Action a, int lb, int msk, int[] v, DTNode[] n, DTNode d)");
        p.println("super(a, lb, msk);");
        p.println("values = v;");
        p.println("nodes = n;");
        p.println("def = d;");
        p.endblock();
        p.startblock("DTNode move(int val)");
        p.println("int ind = Arrays.binarySearch(values, val);");
        p.println("if ( ind < values.length && values[ind] == val )");
        p.println("    return nodes[ind];");
        p.println("else");
        p.println("    return def;");
        p.endblock();
        p.endblock();

        dGen.generateJavaDoc("The <code>DTTerminal</code> class represents a terminal node in the decoding " +
                "tree. Terminal nodes are reached when decoding is finished, and represent either " +
                "successful decoding (meaning instruction and addressing mode were discovered) or " +
                "unsucessful decoding (meaning the bit pattern does not encode a valid instruction.");
        p.startblock("static class DTTerminal extends DTNode");
        p.startblock("DTTerminal(Action a)");
        p.println("super(a, 0, 0);");
        p.endblock();
        p.startblock("DTNode move(int val)");
        p.println("return null;");
        p.endblock();
        p.endblock();

        dGen.generateJavaDoc("The <code>ERROR</code> node is reached for incorrectly encoded instructions and indicates " +
                "that the bit pattern was an incorrectly encoded instruction.");
        p.println("public static final DTTerminal ERROR = new DTTerminal(new ErrorAction());");

        generateActionClasses();

        dGen.generateJavaDoc("The <code>OperandReader</code> class is an object that is capable of reading the " +
                "operands from the bit pattern of an instruction, once the addressing mode is known. One " +
                "of these classes is generated for each addressing mode. When the addressing mode is " +
                "finally known, an action will fire that sets the operand reader which is used to read " +
                "the operands from the bit pattern.");
        p.startblock("static abstract class OperandReader");
        p.println("abstract "+DisassemblerGenerator.operandClassName+"[] read("+DisassemblerGenerator.disassemblerClassName+" d);");
        p.endblock();
    }

    private void generateActionClasses() {
        dGen.generateJavaDoc("The <code>Action</code> class represents an action that can happen when the " +
                "decoder reaches a particular node in the tree. The action may be to fix the instruction " +
                "or addressing mode, or to signal an error.");
        p.startblock("static abstract class Action");
        p.println("abstract void execute("+dGen.disassemblerClassName+" d) throws InvalidInstruction;");
        p.endblock();

        dGen.generateJavaDoc("The <code>ErrorAction</code> class is an action that is fired when the decoding tree " +
                "reaches a state which indicates the bit pattern is not a valid instruction.");
        p.startblock("static class ErrorAction extends Action");
        p.println("void execute("+dGen.disassemblerClassName+" d) throws InvalidInstruction { throw new InvalidInstruction(0); }");
        p.endblock();

        dGen.generateJavaDoc("The <code>SetBuilder</code> class is an action that is fired when the decoding tree " +
                "reaches a node where the instruction is known. This action fires and sets the <code>builder</code> " +
                "field to point the appropriate builder for the instruction.");
        p.startblock("static class SetBuilder extends Action");
        p.println(dGen.builderClassName+".Single builder;");
        p.println("SetBuilder("+dGen.builderClassName+".Single b) { builder = b; }");
        p.println("void execute("+dGen.disassemblerClassName+" d) throws InvalidInstruction { d.builder = builder; }");
        p.endblock();

        dGen.generateJavaDoc("The <code>SetReader</code> class is an action that is fired when the decoding tree " +
                "reaches a node where the addressing mode is known. This action fires and sets the " +
                "<code>operands</code> field to point the operands read from the instruction stream.");
        p.startblock("static class SetReader extends Action");
        p.println("OperandReader reader;");
        p.println("SetReader(OperandReader r) { reader = r; }");
        p.println("void execute("+dGen.disassemblerClassName+" d) throws InvalidInstruction { d.operands = reader.read(d); }");
        p.endblock();
    }

    void generateFields() {
        dGen.generateJavaDoc("The <code>builder</code> field stores a reference to the builder that was " +
                "discovered as a result of traversing the decoder tree. The builder corresponds to one " +
                "and only one instruction and has a method that can build a new instance of the instruction " +
                "from the operands.");
        p.println("private "+DisassemblerGenerator.builderClassName+".Single builder;");
        dGen.generateJavaDoc("The <code>operands</code> field stores a reference to the operands that were " +
                "extracted from the bit pattern as a result of traversing the decoding tree. When a node is " +
                "reached where the addressing mode is known, then the action on that node executes and " +
                "reads the operands from the bit pattern, storing them in this field.");
        p.println("private "+DisassemblerGenerator.operandClassName+"[] operands;");

        for ( int cntr = 0; cntr < dGen.maxInstrLength - 1; cntr += DisassemblerGenerator.WORD_SIZE ) {
            int word = cntr / DisassemblerGenerator.WORD_SIZE;
            dGen.generateJavaDoc("The <code>word"+word+"</code> field stores a word-sized chunk of the instruction " +
                    "stream. It is used by the decoders instead of repeatedly accessing the array. This implementation " +
                    "has been configured with "+DisassemblerGenerator.WORD_SIZE+"-bit words.");
            p.println("private int word"+word+";");
        }
    }

    void generateParallelRoot() {
        dGen.generateJavaDoc("The <code>decode_root()</code> method begins decoding the bit pattern " +
                "into an instruction. " +
                "This implementation is <i>parallel</i>, meaning there are two trees: one for " +
                "the instruction resolution and one of the addressing mode resolution. By beginning " +
                "at the root node of the addressing mode and " +
                "instruction resolution trees, the loop compares bits in the bit patterns and moves down " +
                "the two trees in parallel. When both trees reach an endpoint, the comparison stops and " +
                "an instruction will be built. This method accepts the value of the first word of the " +
                "bits and begins decoding from there.\n");
        p.startblock(DisassemblerGenerator.instrClassName+" decode_root() throws InvalidInstruction");
        p.println("DTNode addr = addr0;");
        p.println("DTNode instr = instr0;");
        p.startblock("while (true)");
        p.println("int bits = (word0 >> addr.left_bit) & addr.mask;");
        p.println("addr = addr.move(bits);");
        p.println("if ( instr != null ) instr = instr.move(bits);");
        p.println("if ( addr == null ) break;");
        p.println("if ( addr.action != null ) addr.action.execute(this);");
        p.println("if ( instr != null && instr.action != null ) instr.action.execute(this);");
        p.endblock();
        p.println("if ( builder != null && operands != null ) return builder.build(operands);");
        p.println("return null;");
        p.endblock();
    }

    private void generateRoot() {
        dGen.generateJavaDoc("The <code>decode_root()</code> method begins decoding the bit pattern " +
                "into an instruction. This implementation resolves both instruction and addressing " +
                "mode with one tree. It begins at the root node and continues comparing bits and " +
                "following the appropriate paths until a terminal node is reached. This method " +
                "accepts the value of the first word of the bits and begins " +
                "decoding from there.\n");
        p.startblock(DisassemblerGenerator.instrClassName+" decode_root() throws InvalidInstruction");
        p.println("DTNode node = root0;");
        p.startblock("while (true)");
        p.println("int bits = (word0 >> node.left_bit) & node.mask;");
        p.println("node = node.move(bits);");
        p.println("if ( node == null ) break;");
        p.println("if ( node.action != null ) node.action.execute(this);");
        p.endblock();
        p.println("if ( builder != null && operands != null ) return builder.build(operands);");
        p.println("return null;");
        p.endblock();
    }

    void generateEntryPoints() {
        if ( DisassemblerGenerator.WORD_SIZE != 16 ) throw Util.failure("Only 16 bit word size is supported");

        dGen.generateJavaDoc("The <code>decode()</code> method is the main entrypoint to the disassembler. " +
                "Given an array of bytes or shorts and an index, the disassembler will attempt to " +
                "decode one instruction at that location. If successful, the method will return a referece " +
                "to a new <code>"+DisassemblerGenerator.instrClassName+"</code> object. \n" +
                "@param base the base address of the array\n" +
                "@param index the index into the array where to begin decoding\n" +
                "@param code the actual code\n" +
                "@return an instance of the <code>"+DisassemblerGenerator.instrClassName+"</code> class corresponding to the " +
                "instruction at this address if a valid instruction exists here\n" +
                "@throws InvalidInstruction if the bit pattern at this address does not correspond to a valid " +
                "instruction");
        p.startblock("public "+DisassemblerGenerator.instrClassName+" decode(int base, int index, byte[] code) throws InvalidInstruction");
        for ( int cntr = 0; cntr < dGen.maxInstrLength - 1; cntr += DisassemblerGenerator.WORD_SIZE ) {
            int word = cntr / DisassemblerGenerator.WORD_SIZE;
            p.print("word"+word+" = ");
            p.print("code[index + "+(1+word/2)+"] << 8 | ");
            p.println("(code[index + "+(word/2)+"] & 0xF);");
        }
        p.println("builder = null;");
        p.println("operands = null;");
        p.println("return decode_root();");
        p.endblock();
    }

    private void generateDecodingTree(String methname, ActionGetter ag, DTNode dt) {
        dGen.generateJavaDoc("The <code>"+methname+"()</code> method creates a new instance of a " +
                "decoding tree by allocating the DTNode instances and connecting the references " +
                "together correctly. It is called only once in the static initialization of the " +
                "disassembler to build a single shared instance of the decoder tree implementation " +
                "and the reference to the root node is stored in a single private static field of " +
                "the same name.");
        p.startblock("static DTNode "+methname+"()");
        String last = "ERROR";
        for ( DTNode n : DGUtil.topologicalOrder(dt) )
            last = generateDecodingNode(n, ag);
        p.println("return "+last+";");
        p.endblock();
    }

    private String generateDecodingNode(DTNode n, ActionGetter ag) {
        String action = ag.getAction(n);
        if ( n.isLeaf() ) {
            String nname = dGen.nodeName(n);
            p.println("DTNode "+nname+" = new DTTerminal("+action+");");
            return nname;
        }
        DisassemblerGenerator.DTNodeImpl nodeImpl = dGen.newDTNode(n, action);
        for ( Map.Entry<Integer, DTNode> e : n.getSortedEdges() ) {
            int value = e.getKey();
            DTNode cdt = e.getValue();
            nodeImpl.add(value, dGen.nodeName(cdt));
        }
        nodeImpl.generate();
        return nodeImpl.nname;
    }

    abstract class ActionGetter {
        abstract String getAction(DTNode n);
    }

    class InstrActionGetter extends ActionGetter {
        String getAction(DTNode n) {
            String label = n.getLabel();
            if ( "*".equals(label) ) return "null";
            if ( "-".equals(label) ) return "null";
            else return "new SetBuilder("+DisassemblerGenerator.builderClassName+"."+label+")";
        }
    }

    class AddrModeActionGetter extends ActionGetter {
        String getAction(DTNode n) {
            String label = n.getLabel();
            if ( "*".equals(label) ) return "null";
            if ( "-".equals(label) ) return "null";
            else return "new SetReader("+label+")";
        }
    }
}
