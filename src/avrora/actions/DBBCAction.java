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
import avrora.core.Program;
import avrora.core.isdl.gen.PrettyPrinter;
import avrora.sim.dbbc.DBBC;
import avrora.util.Printer;
import avrora.util.StringUtil;

import java.util.Iterator;

/**
 * The <code>DBBCAction</code> class contains a simple test action where a program can be loaded
 * and compiled to Java source by the DBBC and that source is output on the console. This is
 * mostly used for testing the DBBC.
 * @author Ben L. Titzer
 */
public class DBBCAction extends Action {

    public static String HELP = "The \"dbbc\" action tests the operation of the Dynamic Basic Block Compiler " +
            "(DBBC) in Avrora, which dynamically compiles AVR code to Java source code.";

    public DBBCAction() {
        super("dbbc", HELP);
    }

    /**
     * The <code>run()</code> method starts the DBBC test with the given program. The program is
     * compiled to Java source and each compiled block is output to the console.
     * @param args the arguments from the command line to the DBBC test program
     * @throws Exception
     */
    public void run(String[] args) throws Exception {
        Printer printer = Printer.STDOUT;
        PrettyPrinter pp = new PrettyPrinter(printer);
        Program p = Main.readProgram(args);
        DBBC dbbc = new DBBC(p, options);

        ControlFlowGraph cfg = p.getCFG();
        Iterator i = cfg.getSortedBlockIterator();
        while (i.hasNext()) {
            ControlFlowGraph.Block b = (ControlFlowGraph.Block)i.next();
            printer.startblock("block starting at: " + StringUtil.addrToString(b.getAddress()));
            DBBC.CodeBlock code = dbbc.getCodeBlock(b.getAddress());
            if (code != null) {
                printer.println("// worst case execution time = " + code.wcet + " cycles");
                pp.visitStmtList(code.stmts);
            } else {
                printer.println("// no code generated for this block");
            }
            printer.endblock();
        }
    }
}
