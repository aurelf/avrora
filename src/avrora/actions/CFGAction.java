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

package avrora.actions;

import avrora.core.Program;
import avrora.core.ControlFlowGraph;
import avrora.core.CFGBuilder;
import avrora.core.Instr;
import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.util.Printer;
import avrora.Main;

import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class CFGAction extends Action {

    public static final String HELP = "The \"cfg\" action builds and displays a control flow graph of the " +
            "given input program. This is useful for better program understanding " +
            "and for optimizations. The graph can be outputted in a textual format, or the " +
            "format supported by the \"dot\" graph tool.";

    /**
     * The default constructor of the <code>CFGAction</code> class simply
     * creates an empty instance with the appropriate name and help string.
     */
    public CFGAction() {
        super("cfg", HELP);
    }

    protected ControlFlowGraph cfg;
    protected Program program;

    public void run(String[] args) throws Exception {
        program = Main.readProgram(args);
        cfg = new CFGBuilder(program).buildCFG();

        if (Main.OUTPUT.get().equals("dot"))
            dumpDotCFG(cfg);
        else
            dumpCFG(cfg);

    }

    private void dumpCFG(ControlFlowGraph cfg) {
        Iterator biter = cfg.getSortedBlockIterator();

        while (biter.hasNext()) {
            ControlFlowGraph.Block block = (ControlFlowGraph.Block) biter.next();
            Terminal.print("[");
            Terminal.printBrightCyan(StringUtil.addrToString(block.getAddress()));
            Terminal.println(":" + block.getSize() + "]");
            Iterator iiter = block.getInstrIterator();
            while (iiter.hasNext()) {
                Instr instr = (Instr) iiter.next();
                Terminal.printBrightBlue("    " + instr.getName());
                Terminal.println(" " + instr.getOperands());
            }
            Terminal.print("    [");
            dumpEdges(block.getEdgeIterator());
            Terminal.println("]");
        }
    }

    private void dumpDotCFG(ControlFlowGraph cfg) {
        Printer p = Printer.STDOUT;
        p.startblock("digraph G");

        if (Main.COLOR_PROCEDURES.get() ||
                Main.GROUP_PROCEDURES.get() ||
                Main.COLLAPSE_PROCEDURES.get())
            discoverProcedures(cfg);

        dumpDotNodes(cfg, p);
        dumpDotEdges(cfg, p);
        p.endblock();
    }

    private static final Object BLACK = new Object();

    private HashSet ENTRYPOINTS;
    private HashMap COLORMAP;

    private void discoverProcedures(ControlFlowGraph cfg) {

        HashSet colors = discoverEntrypoints(cfg);

        COLORMAP = new HashMap();

        Iterator iter = colors.iterator();
        while (iter.hasNext()) {
            Object block = iter.next();
            COLORMAP.put(block, block);
        }

        iter = colors.iterator();
        while (iter.hasNext()) {
            ControlFlowGraph.Block block = (ControlFlowGraph.Block) iter.next();
            propagate(block, block, COLORMAP, new HashSet());
        }

    }

    private HashSet discoverEntrypoints(ControlFlowGraph cfg) {
        ENTRYPOINTS = new HashSet();

        // discover nodes that have incoming call edges
        Iterator nodes = cfg.getBlockIterator();
        while (nodes.hasNext()) {
            ControlFlowGraph.Block block = (ControlFlowGraph.Block) nodes.next();
            Iterator edges = block.getEdgeIterator();
            while (edges.hasNext()) {
                ControlFlowGraph.Edge edge = (ControlFlowGraph.Edge) edges.next();
                if (edge.getType().equals("CALL")) {
                    if (edge.getTarget() == null) {
                        addIndirectEntrypoints(edge, cfg);
                    } else ENTRYPOINTS.add(edge.getTarget());
                }
            }
        }

        return ENTRYPOINTS;
    }

    private void addIndirectEntrypoints(ControlFlowGraph.Edge edge, ControlFlowGraph cfg) {
        List l = program.getIndirectEdges(edge.getSource().getLastAddress());
        if ( l == null ) return;
        Iterator i = l.iterator();
        while ( i.hasNext() ) {
            int target_addr = ((Integer)i.next()).intValue();
            ControlFlowGraph.Block target = cfg.getBlockStartingAt(target_addr);
            if ( target != null ) {
                ENTRYPOINTS.add(target);
            }
        }
    }

    private void propagate(ControlFlowGraph.Block color, ControlFlowGraph.Block block, HashMap colorMap, HashSet seen) {
        if (colorMap.get(block) == BLACK) return;

        Iterator edges = block.getEdgeIterator();
        while (edges.hasNext()) {
            ControlFlowGraph.Edge edge = (ControlFlowGraph.Edge) edges.next();
            if (edge.getType().equals("CALL")) continue;
            ControlFlowGraph.Block target = edge.getTarget();
            if (target == null) continue;
            color(color, target, colorMap);

            if (!seen.contains(target)) {
                seen.add(target);
                propagate(color, target, colorMap, seen);
            } else {
                seen.add(target);
            }
        }
    }

    private void color(ControlFlowGraph.Block color, ControlFlowGraph.Block block, HashMap colorMap) {
        Object c = colorMap.get(block);
        if (c == BLACK) return;
        if (c == null) c = color;
        if (c != color)
            blacken(block, colorMap);
        else
            colorMap.put(block, color);
    }

    private void blacken(ControlFlowGraph.Block block, HashMap colorMap) {
        if (colorMap.get(block) == BLACK) return;
        colorMap.put(block, BLACK);

        Iterator edges = block.getEdgeIterator();
        while (edges.hasNext()) {
            ControlFlowGraph.Edge edge = (ControlFlowGraph.Edge) edges.next();
            if (edge.getType().equals("CALL")) continue;
            ControlFlowGraph.Block target = edge.getTarget();
            if (target == null) continue;
            blacken(target, colorMap);
        }
    }

    private void dumpDotNodes(ControlFlowGraph cfg, Printer p) {

        if (!Main.GROUP_PROCEDURES.get()) {
            Iterator blocks = cfg.getSortedBlockIterator();
            while (blocks.hasNext()) {
                ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
                printBlock(block, p);
            }
        } else if (Main.COLLAPSE_PROCEDURES.get()) {

            // add each block to its respective color set
            Iterator blocks = cfg.getSortedBlockIterator();
            while (blocks.hasNext()) {
                ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
                Object color = COLORMAP.get(block);

                // only print out nodes with no color, or the same as their own color
                if (color == BLACK || color == null || color == block) {
                    printBlock(block, p);
                }
            }
        } else {
            HashMap colorSets = new HashMap();

            Iterator citer = ENTRYPOINTS.iterator();
            while (citer.hasNext()) {
                Object color = citer.next();
                colorSets.put(color, new HashSet());
            }

            // add each block to its respective color set
            Iterator blocks = cfg.getSortedBlockIterator();
            while (blocks.hasNext()) {
                ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
                Object color = COLORMAP.get(block);

                if (color == BLACK || color == null) {
                    printBlock(block, p);
                } else {
                    HashSet set = (HashSet) colorSets.get(color);
                    set.add(block);
                }
            }

            citer = ENTRYPOINTS.iterator();
            int num = 0;
            while (citer.hasNext()) {
                ControlFlowGraph.Block entry = (ControlFlowGraph.Block) citer.next();
                p.startblock("subgraph cluster" + (num++));
                blocks = ((HashSet) colorSets.get(entry)).iterator();
                while (blocks.hasNext()) {
                    ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
                    printBlock(block, p);
                }
                p.endblock();
            }

        }
    }

    private void printBlock(ControlFlowGraph.Block block, Printer p) {
        String bName = blockName(block);
        String shape = getShape(block);
        String color = getColor(block);
        p.print(bName + " [shape=" + shape);
        if (!color.equals("")) p.print(",style=filled,fillcolor=" + color);
        p.println("];");
    }

    private void dumpDotEdges(ControlFlowGraph cfg, Printer p) {
        Iterator blocks = cfg.getSortedBlockIterator();
        while (blocks.hasNext()) {
            ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
            dumpDotEdges(block.getEdgeIterator(), p);
        }
    }

    private String getShape(ControlFlowGraph.Block block) {
        if (getEntryOf(block) == block) return "doubleoctagon";

        int addr = block.getAddress();
        Iterator edges = block.getEdgeIterator();
        if (addr % 4 == 0 && addr < 35 * 4) // interrupt handler
            return "box";

        while (edges.hasNext()) {
            ControlFlowGraph.Edge e = (ControlFlowGraph.Edge) edges.next();
            String type = e.getType();
            if (isReturnEdge(type)) return "hexagon";
        }
        return "ellipse";
    }

    int colorCounter;
    HashMap COLORS = new HashMap();
    String palette[] = {"aquamarine", "blue2", "brown1", "cadetblue1",
                        "chartreuse1", "cyan4", "darkgoldenrod1", "darkorchid3", "darkslateblue",
                        "deeppink2", "yellow", "seagreen3", "orangered1"};

    private String getColor(ControlFlowGraph.Block block) {
        if (!Main.COLOR_PROCEDURES.get()) return "";
        ControlFlowGraph.Block entry = getEntryOf(block);
        if (entry == null) return "";
        String c = (String) COLORS.get(entry);
        if (c == null) {
            int newColor = colorCounter;
            colorCounter = (colorCounter + 1) % palette.length;
            COLORS.put(entry, palette[newColor]);
            c = palette[newColor];
        }
        return c;
    }

    private boolean isReturnEdge(String type) {
        return type != null && (type.equals("RET") || type.equals("RETI"));
    }

    private void dumpEdges(Iterator edges) {
        while (edges.hasNext()) {
            ControlFlowGraph.Edge e = (ControlFlowGraph.Edge) edges.next();
            ControlFlowGraph.Block t = e.getTarget();

            if (e.getType().equals(""))
                Terminal.print("--> ");
            else
                Terminal.print("--(" + e.getType() + ")--> ");

            if (t != null)
                Terminal.printBrightGreen(StringUtil.addrToString(e.getTarget().getAddress()));
            else
                Terminal.printRed("UNKNOWN");

            if (edges.hasNext()) Terminal.print(", ");
        }
    }

    private boolean unknownExists;

    private void dumpDotEdges(Iterator edges, Printer p) {
        while (edges.hasNext()) {
            ControlFlowGraph.Edge e = (ControlFlowGraph.Edge) edges.next();
            ControlFlowGraph.Block source = e.getSource();
            ControlFlowGraph.Block target = e.getTarget();
            ControlFlowGraph.Block es, et;
            String type = e.getType();

            // don'type print out the return edges which point to null
            if (isReturnEdge(type)) continue;

            // remember only inter-procedural edges
            if (Main.COLLAPSE_PROCEDURES.get()) {
                source = ((es = getEntryOf(source)) == null) ? source : es;
                target = ((et = getEntryOf(target)) == null) ? target : et;
                // don'type print out intra-block edges if we are collapsing procedures
                if (es == et && et != null) continue;
            }

            // get the names of the blocks
            String sName = blockName(source);

            if ( target == null ) { // emit indirect edges
                emitIndirectEdge(source, sName, p, type);
            } else {
                emitEdge(target, p, sName, type, true);
            }

        }
    }

    private void emitIndirectEdge(ControlFlowGraph.Block source, String sName, Printer p, String type) {
        List l = program.getIndirectEdges(source.getLastAddress());

        if ( l == null ) {
            // emit the description for the unknown node if we haven't already
            if (!unknownExists) {
                p.println("UNKNOWN [shape=Msquare];");
                unknownExists = true;
            }

            p.println(sName +" -> UNKNOWN [style=dotted];");
        } else {
            // emit indirect edges
            Iterator i = l.iterator();
            while ( i.hasNext() ) {
                int taddr = ((Integer)i.next()).intValue();
                ControlFlowGraph.Block target = cfg.getBlockStartingAt(taddr);
                emitEdge(target, p, sName, type, false);
            }

        }
    }

    private void emitEdge(ControlFlowGraph.Block target, Printer p, String sName, String t, boolean direct) {
        String tName = blockName(target);
        // print the edge
        p.print(sName + " -> " + tName);
        p.print(" [headport=s,tailport=n");

        if ( !direct )
            p.print(",style=dotted");

        // change the style of the edge based on its type
        if (t.equals("CALL"))
            p.print(",color=red");

        p.println("];");
    }

    private ControlFlowGraph.Block getEntryOf(ControlFlowGraph.Block b) {
        if (COLORMAP == null) return null;
        if (b == null) return null;
        Object c = COLORMAP.get(b);
        if (c == null) return null;
        if (c == BLACK) return null;
        return (ControlFlowGraph.Block) c;
    }

    private String blockName(ControlFlowGraph.Block block) {
        String start = StringUtil.addrToString(block.getAddress());
        String end = StringUtil.addrToString(block.getAddress() + block.getSize());
        return StringUtil.quote(start + " - \\n" + end);
    }

    private void printPair(String t, ControlFlowGraph.Block b) {
        String v = b == null ? "null" : StringUtil.addrToString(b.getAddress());
        Terminal.printPair(Terminal.COLOR_BRIGHT_GREEN, Terminal.COLOR_BRIGHT_CYAN, t, ": ", v);
    }
}
