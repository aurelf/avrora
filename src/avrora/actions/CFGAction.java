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

/**
 * @author Ben L. Titzer
 */
public class CFGAction extends Action {

    public static final String HELP = "The \"cfg\" action builds and displays a control flow graph of the " +
                    "given input program. This is useful for better program understanding " +
                    "and for optimizations.";

    /**
     * The default constructor of the <code>CFGAction</code> class simply
     * creates an empty instance with the appropriate name and help string.
     */
    public CFGAction() {
        super("cfg", HELP);
    }

    public void run(String[] args) throws Exception {
        Main.ProgramReader r = Main.getProgramReader();
        Program p = r.read(args);
        ControlFlowGraph cfg = new CFGBuilder(p).buildCFG();

        if ( Main.OUTPUT.get().equals("dot") ) dumpDotCFG(cfg);
        else dumpCFG(cfg);

    }

    private void dumpCFG(ControlFlowGraph cfg) {
        Iterator biter = cfg.getSortedBlockIterator();

        while ( biter.hasNext() ) {
            ControlFlowGraph.Block block = (ControlFlowGraph.Block)biter.next();
            Terminal.print("[");
            Terminal.printBrightCyan(StringUtil.addrToString(block.getAddress()));
            Terminal.println(":"+block.getSize()+"]");
            Iterator iiter = block.getInstrIterator();
            while ( iiter.hasNext() ) {
                Instr instr = (Instr)iiter.next();
                Terminal.printBrightBlue("    "+instr.getName());
                Terminal.println(" "+instr.getOperands());
            }
            Terminal.print("    [");
            dumpEdges(block.getEdgeIterator());
            Terminal.println("]");
        }
    }

    private void dumpDotCFG(ControlFlowGraph cfg) {
        Iterator biter = cfg.getSortedBlockIterator();

        Printer p = Printer.STDOUT;
        p.startblock("digraph G");
        while ( biter.hasNext() ) {
            ControlFlowGraph.Block block = (ControlFlowGraph.Block)biter.next();
            String bName = blockName(block);
            int addr = block.getAddress();
            String shape = getShape(addr, block.getEdgeIterator());
            p.println(bName+" [shape="+shape+"];");

            dumpDotEdges(block.getEdgeIterator(), p);
        }
        p.endblock();
    }

    private String getShape(int addr, Iterator edges) {
        if ( addr % 4 == 0 && addr < 35 * 4 ) // interrupt handler
            return "box";

        while (edges.hasNext()) {
            ControlFlowGraph.Edge e = (ControlFlowGraph.Edge)edges.next();
            String type = e.getType();
            if ( isReturnEdge(type) ) return "hexagon";
        }
        return "ellipse";
    }

    private boolean isReturnEdge(String type) {
        return type != null && (type.equals("RET") || type.equals("RETI"));
    }

    private void dumpEdges(Iterator edges) {
        while (edges.hasNext()) {
            ControlFlowGraph.Edge e = (ControlFlowGraph.Edge)edges.next();
            Terminal.print(StringUtil.addrToString(e.getTarget().getAddress()));
        }
    }

    private void dumpDotEdges(Iterator edges, Printer p) {
        while (edges.hasNext()) {
            ControlFlowGraph.Edge e = (ControlFlowGraph.Edge)edges.next();
            String sName = blockName(e.getSource());
            ControlFlowGraph.Block target = e.getTarget();
            String t = e.getType();

            if ( isReturnEdge(t) ) continue;

            if ( target != null ) {
                String tName = blockName(target);
                p.print(sName+" -> "+tName);
            } else {
                p.print(sName +" -> UNKNOWN ");
            }

            if ( t != null ) {
                if ( t != null ) p.print("[label="+t+"]");
                p.println(";");
            }
        }
    }

    private String blockName(ControlFlowGraph.Block block) {
        return StringUtil.quote(StringUtil.addrToString(block.getAddress()));
    }

    private void printPair(String t, ControlFlowGraph.Block b) {
        String v = b == null ? "null" : StringUtil.addrToString(b.getAddress());
        Terminal.printPair(Terminal.COLOR_BRIGHT_GREEN, Terminal.COLOR_BRIGHT_CYAN, t, ": ", v);
    }
}
