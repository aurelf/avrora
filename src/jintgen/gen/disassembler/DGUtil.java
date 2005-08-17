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

import avrora.util.StringUtil;
import avrora.util.Printer;
import avrora.util.Terminal;
import avrora.util.Util;

import java.util.*;

/**
 * The <code>DGUtil</code> class contains a set of utility methods that are useful in
 * implementing, debugging, and understanding the disassembler generator.
 *
 * @author Ben L. Titzer
 */
public class DGUtil {

    /**
     * The <code>toString()</code> method converts an instance of the <code>EncodingInfo</code>
     * class into a string.
     * @param ei the encoding info instance to convert to a string
     * @return a string representation of the encoding info
     */
    public static String toString(EncodingInfo ei) {
        StringBuffer buf = new StringBuffer(25+ei.bitStates.length);
        buf.append(ei.instr.name.toString());
        buf.append(" x ");
        buf.append(ei.addrMode.name.toString());
        int space = 20 - buf.length();
        while ( space > 0 ) {
            buf.append(' ');
            space--;
        }
        buf.append(": ");
        for ( int cntr = 0; cntr < ei.bitStates.length; cntr++ ) {
            switch ( ei.bitStates[cntr] ) {
                case EncodingInfo.ENC_ZERO:
                    buf.append("0");
                    break;
                case EncodingInfo.ENC_ONE:
                    buf.append("1");
                    break;
                case EncodingInfo.ENC_MATCHED_ONE:
                    buf.append("U");
                    break;
                case EncodingInfo.ENC_MATCHED_ZERO:
                    buf.append("u");
                    break;
                case EncodingInfo.ENC_VAR:
                    buf.append(".");
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * The <code>printTree()</code> method is a utility method to dump out a tree
     * to a specified printer in a textual format.
     * @param p the printer to dump the tree to
     * @param dt the decoding tree to print
     */
    public static void printTree(Printer p, DecodingTree dt) {
        HashSet<DecodingTree> nodes = new HashSet<DecodingTree>();
        printTree(nodes, p, dt, 0);
    }

    /**
     * The <code>printTree()</code> method is a utility method to dump out a tree
     * to a specified printer in a textual format.
     * @param p the printer to dump the tree to
     * @param dt the decoding tree to print
     * @param depth the indenting depth
     */
    public static void printTree(HashSet<DecodingTree> nodes, Printer p, DecodingTree dt, int depth) {
        p.print("#"+dt.node_num+" ");
        if ( nodes.contains(dt) ) {
            p.nextln(); 
            return;
        }
        nodes.add(dt);
        String label = dt.getLabel();
        if ( label != null ) p.print(label+" ");
        if ( dt.children.size() == 0 ) {
            printLeaf(dt, p);
            return;
        }
        int length = dt.right_bit - dt.left_bit + 1;
        p.println("decode["+dt.left_bit+":"+dt.right_bit+"]: ");
        DecodingTree def = null;
        for ( Map.Entry<Integer, DecodingTree> e : dt.children.entrySet() ) {
            DecodingTree cdt = e.getValue();
            int val = e.getKey();
            if ( val < 0 ) {
                def = cdt;
                continue;
            }
            printNode(nodes, p, depth, val, length, cdt);
        }
        if ( def != null )
            printNode(nodes, p, depth, -1, length, def);
    }

    private static void printLeaf(DecodingTree dt, Printer p) {
        String label = dt.getLabel();
        if ( label == null )
            for ( EncodingInfo ei : dt.highPrio ) ei.print(0, p);
    }

    private static void printNode(HashSet<DecodingTree> nodes, Printer p, int depth, int val, int length, DecodingTree cdt) {
        indent(p, depth+1);
        p.print(getEdgeLabel(val, length)+" -> ");
        printTree(nodes, p, cdt, depth+1);
        p.nextln();
    }

    /**
     * The <code>countNodes()</code> method counts the number of nodes in the decoding tree.
     * @param dt the decoding tree for which to count the nodes
     * @return the number of nodes in this decoding tree and its subtrees
     */
    public static int numberNodes(DecodingTree dt) {
        HashSet<DecodingTree> nodes = new HashSet<DecodingTree>();
        numberNodes(nodes, dt);
        return nodes.size();
    }

    private static void numberNodes(HashSet<DecodingTree> nodes, DecodingTree dt) {
        if ( nodes.contains(dt) ) return;
        dt.node_num = nodes.size();
        nodes.add(dt);
        for ( DecodingTree cdt : dt.children.values() ) {
            numberNodes(nodes, cdt);
        }
    }

    /**
     * The <code>indent()</code> method simply prints a number of leading spaces that
     * help indentation for printing out trees.
     * @param p the printer to indent
     * @param depth the depth to indent
     */
    public static void indent(Printer p, int depth) {
        for ( int cntr = 0; cntr < depth; cntr++ ) p.print("    ");
    }

    public static void ambiguous(Set<EncodingInfo> set) {
        Terminal.nextln();
        Terminal.printRed("ERROR");
        Terminal.println(": the following encodings are ambiguous:");
        for ( EncodingInfo el : set )
            el.print(0, Printer.STDOUT);
        throw Util.failure("Disassembler generator cannot continue");
    }

    public static void printDotTree(String title, DecodingTree dt, Printer p) {
        p.startblock("digraph "+title);
        p.println("rankdir=LR;");
        p.println("randsep=2;");
        HashSet<DecodingTree> nodes = new HashSet<DecodingTree>();
        printNode(p, dt, nodes);
        p.endblock();
    }

    private static void printNode(Printer p, DecodingTree dt, HashSet<DecodingTree> nodes) {
        int length = dt.right_bit - dt.left_bit + 1;
        String name = getName(dt);
        p.println(name+";");
        for ( Map.Entry<Integer, DecodingTree> e : dt.children.entrySet() ) {
            int value = e.getKey();
            DecodingTree cdt = e.getValue();
            if ( !nodes.contains(cdt) ) {
                printNode(p, cdt, nodes);
                nodes.add(cdt);
            }
            p.println(name+" -> "+getName(cdt)+" [label="+getEdgeLabel(value, length)+"];");
        }
    }

    private static String getEdgeLabel(int value, int length) {
        return value == -1 ? "*" : StringUtil.toBin(value, length);
    }

    private static String getName(DecodingTree dt) {
        return StringUtil.quote(dt.node_num+":"+dt.getLabel()+"\\n["+dt.left_bit+":"+dt.right_bit+"]");
    }

    public static Collection<DTNode> topologicalOrder(DTNode n) {
        HashSet<DTNode> set = new HashSet<DTNode>();
        List<DTNode> list = new LinkedList<DTNode>();
        n.addTopologicalOrder(list, set);
        return list;
    }

    public static Collection<DTNode> preOrder(DTNode n) {
        HashSet<DTNode> set = new HashSet<DTNode>();
        List<DTNode> list = new LinkedList<DTNode>();
        n.addPreOrder(list, set);
        return list;
    }

    public static int numberNodes(DTNode n) {
        int number = 0;
        for ( DTNode c : preOrder(n) ) c.number = number++;
        return number;
    }
}
