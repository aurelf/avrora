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

import avrora.Main;
import avrora.core.ControlFlowGraph;
import avrora.core.Instr;
import avrora.core.ProcedureMap;
import avrora.core.Program;
import avrora.util.Option;
import avrora.util.Printer;
import avrora.util.StringUtil;
import avrora.util.Terminal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class CFGAction extends Action {

    public static final String HELP = "The \"cfg\" action builds and displays a control flow graph of the " +
            "given input program. This is useful for better program understanding " +
            "and for optimizations. The graph can be outputted in a textual format, or the " +
            "format supported by the \"dot\" graph tool.";

    public final Option.Bool COLOR_PROCEDURES = newOption("color-procedures", true,
            "This option is used when outputting in the " +
            "\"dot\" output format. When this option is true, the control flow graph " +
            "utility will attempt to discover procedures and color them in the output.");
    public final Option.Bool GROUP_PROCEDURES = newOption("group-procedures", true,
            "This option is used when outputting in the " +
            "\"dot\" output format. When this option is true, the control flow graph " +
            "utility will attempt to discover procedures and group them as subgraphs " +
            "in the output.");
    public final Option.Bool COLLAPSE_PROCEDURES = newOption("collapse-procedures", false,
            "This option is used when outputting in the " +
            "\"dot\" output format. When this option is true, the control flow graph " +
            "utility will attempt to discover procedures within the control flow graph " +
            "and collapse whole procedures to a single node in the output.");

    /**
     * The default constructor of the <code>CFGAction</code> class simply
     * creates an empty instance with the appropriate name and help string.
     */
    public CFGAction() {
        super("cfg", HELP);
    }

    protected ProcedureMap pmap;
    protected ControlFlowGraph cfg;
    protected Program program;

    public void run(String[] args) throws Exception {
        program = Main.readProgram(args);
        cfg = program.getCFG();

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

        if (COLOR_PROCEDURES.get() ||
                GROUP_PROCEDURES.get() ||
                COLLAPSE_PROCEDURES.get())
            pmap = cfg.getProcedureMap();

        dumpDotNodes(p);
        dumpDotEdges(p);
        p.endblock();
    }

    private void dumpDotNodes(Printer p) {
        if ( COLOR_PROCEDURES.get() )
            assignProcedureColors();


        if ( COLLAPSE_PROCEDURES.get() ) {

            Iterator blocks = cfg.getSortedBlockIterator();
            while (blocks.hasNext()) {
                ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
                ControlFlowGraph.Block entry = pmap.getProcedureContaining(block);

                // only print out nodes that have no color, are shared, or are entrypoints
                if ( entry == null || entry == block )
                    printBlock(block, p);
            }
        } else if ( GROUP_PROCEDURES.get() ) {
            // print out blocks that have no color
            Iterator block_iter = cfg.getSortedBlockIterator();
            while (block_iter.hasNext()) {
                ControlFlowGraph.Block block = (ControlFlowGraph.Block) block_iter.next();
                ControlFlowGraph.Block entry = pmap.getProcedureContaining(block);

                // only print out nodes that have no color first
                if ( entry == null )
                    printBlock(block, p);
            }

            // print out each subgraph as a cluster
            int num = 0;
            Iterator entry_iter = pmap.getProcedureEntrypoints().iterator();
            while (entry_iter.hasNext()) {
                ControlFlowGraph.Block entry = (ControlFlowGraph.Block) entry_iter.next();
                p.startblock("subgraph cluster" + (num++));
                Iterator blocks = pmap.getProcedureBlocks(entry).iterator();
                while (blocks.hasNext()) {
                    ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
                    printBlock(block, p);
                }
                p.endblock();
            }

        } else {
            // no grouping or collapsing of nodes
            Iterator blocks = cfg.getSortedBlockIterator();
            while (blocks.hasNext()) {
                ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
                printBlock(block, p);
            }
        }
    }

    private void assignProcedureColors() {
        // add each block to its respective color set
        Iterator blocks = cfg.getBlockIterator();
        while (blocks.hasNext()) {
            ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
            ControlFlowGraph.Block entry = pmap.getProcedureContaining(block);

            if ( entry != null) {
                String c = colorize(entry);
                BLOCK_COLORS.put(block, c);
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

    private void dumpDotEdges(Printer p) {
        Iterator blocks = cfg.getBlockIterator();
        while (blocks.hasNext()) {
            ControlFlowGraph.Block block = (ControlFlowGraph.Block) blocks.next();
            dumpDotEdges(block.getEdgeIterator(), p);
        }
    }

    private String getShape(ControlFlowGraph.Block block) {
        ControlFlowGraph.Block entry = getEntryOf(block);
        if (entry == block) return "doubleoctagon";

        int addr = block.getAddress();
        if (addr % 4 == 0 && addr < 35 * 4) // interrupt handler
            return "box";

        Iterator edges = block.getEdgeIterator();
        while (edges.hasNext()) {
            ControlFlowGraph.Edge e = (ControlFlowGraph.Edge) edges.next();
            String type = e.getType();
            if (isReturnEdge(type)) return "hexagon";
        }
        return "ellipse";
    }

    private int colorCounter;
    private final HashMap BLOCK_COLORS = new HashMap();
    private static final String palette[] = {"aquamarine", "blue2", "brown1", "cadetblue1",
                        "chartreuse1", "cyan4", "darkgoldenrod1", "darkorchid3", "darkslateblue",
                        "deeppink2", "yellow", "seagreen3", "orangered1"};

    private String colorize(ControlFlowGraph.Block b) {
        String color = (String)BLOCK_COLORS.get(b);
        if ( color != null ) return color;
        color = palette[colorCounter];
        colorCounter = (colorCounter + 1) % palette.length;
        BLOCK_COLORS.put(b, color);
        return color;
    }

    private String getColor(ControlFlowGraph.Block block) {
        String color = (String)BLOCK_COLORS.get(block);
        if ( color == null ) return "";
        return color;
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
            if (COLLAPSE_PROCEDURES.get()) {
                source = ((es = getEntryOf(source)) == null) ? source : es;
                target = ((et = getEntryOf(target)) == null) ? target : et;
                // don't print out intra-block edges if we are collapsing procedures
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
        if ( pmap == null ) return null;
        return pmap.getProcedureContaining(b);
    }

    private String blockName(ControlFlowGraph.Block block) {
        String start = StringUtil.addrToString(block.getAddress());
        String end = StringUtil.addrToString(block.getAddress() + block.getSize());
        return StringUtil.quote(start + " - \\n" + end);
    }
}
