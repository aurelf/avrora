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
import avrora.util.Option;
import avrora.stack.Analyzer;
import avrora.core.Program;

/**
 * The <code>AnalyzeStackAction</code> class is an extension of the <code>Main.Action</code>
 * class that allows the stack tool to be reached from the command line.
 *
 * @author Ben L. Titzer
 */
public class AnalyzeStackAction extends Action {

    public static final String HELP = "The \"analyze-stack\" option invokes the built-in " +
            "stack analysis tool on the specified program. It uses an abstract interpretation " +
            "of the program to determine the possible interrupt masks at each program point " +
            "and determines the worst-case stack depth in the presence of interrupts.";

    public final Option.Bool MONITOR_STATES = newOption("monitor-states", false,
            "This option is used to monitor the progress of a long-running stack analysis problem. " +
            "The analyzer will report the count of states, edges, and propagation information " +
            "produced every 5 seconds. ");
    public final Option.Bool TRACE_SUMMARY = newOption("trace-summary", true,
            "This option is used to reduce the amount of output by summarizing the error trace" +
            "that yields the maximal stack depth. When true, the analysis will shorten the error " +
            "trace by not reporting edges between states of adjacent instructions that do not " +
            "change the stack height.");
    public final Option.Bool TRACE = newOption("trace", true,
            "This option causes the stack analyzer to print a trace of each abstract state " +
            "produced, every edge between states that is inserted, and all propagations " +
            "performed during the analysis. ");

    /**
     * The default constructor of the <code>AnalyzeStackAction</code> class simply
     * creates an empty instance with the appropriate name and help string.
     */
    public AnalyzeStackAction() {
        super("analyze-stack", HELP);
    }

    /**
     * The <code>run()</code> method runs the stack analysis by loading the program from
     * the command line options specified, creating an instance of the <code>Analyzer</code>
     * class, and running the analysis.
     * @param args the string arguments that are the files containing the program
     * @throws java.lang.Exception if the program cannot be loaded correctly
     */
    public void run(String[] args) throws Exception {
        Program p = Main.readProgram(args);
        Analyzer a = new Analyzer(p);

        Analyzer.TRACE_SUMMARY = TRACE_SUMMARY.get();
        Analyzer.MONITOR_STATES = MONITOR_STATES.get();
        Analyzer.TRACE = TRACE.get();

        a.run();
        a.report();
    }

}
