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

package avrora.monitors;

import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.util.ProgramProfiler;
import avrora.sim.util.ProgramTimeProfiler;
import avrora.util.Option;
import avrora.util.StringUtil;
import avrora.util.Terminal;

/**
 * The <code>ProfileMonitor</code> class represents a monitor that can collect profiling information such as
 * counts and branchcounts about the program as it executes.
 *
 * @author Ben L. Titzer
 */
public class ProfileMonitor extends MonitorFactory {

    public final Option.Bool BASIC_BLOCKS = options.newOption("basic-blocks", false,
            "This option is used in by the profiling monitor to determine how to " +
            "collate the profiling information. When this option is set to true, " +
            "the profiling monitor will report the execution count and total " +
            "cycles consumed by each basic block, rather than each instruction " +
            "or instruction range.");

    /**
     * The <code>Monitor</code> class implements the monitor for the profiler. It contains a
     * <code>ProgramProfiler</code> instance which is a probe that is executed after every instruction that
     * collects execution counts for every instruction in the program.
     */
    public class Monitor implements avrora.monitors.Monitor {
        public final Simulator simulator;
        public final Program program;
        public final ProgramProfiler profile;
        public final ProgramTimeProfiler timeprofile;

        Monitor(Simulator s) {
            simulator = s;
            program = s.getProgram();
            profile = new ProgramProfiler(program);
            timeprofile = new ProgramTimeProfiler(program);
            // insert the global probe
            s.insertProbe(profile);
            s.insertProbe(timeprofile);
        }

        /**
         * The <code>report()</code> method generates a textual report for the profiling information gathered
         * from the execution of the program. The result is a table of performance information giving the
         * number of executions of each instruction, compressed for basic blocks.
         */
        public void report() {
            Terminal.printSeparator(Terminal.MAXLINE, "Profiling results");
            Terminal.printGreen("       Address     Count  Run     Cycles     Cumulative");
            Terminal.nextln();
            Terminal.printThinSeparator(Terminal.MAXLINE);
            long[] icount = profile.icount;
            long[] itime = timeprofile.itime;
            int imax = icount.length;
            float totalcycles = 0;

            // compute the total cycle count
            for (int cntr = 0; cntr < itime.length; cntr++) {
                totalcycles += itime[cntr];
            }

            // report the profile for each instruction in the program
            for (int cntr = 0; cntr < imax; cntr = program.getNextPC(cntr)) {
                int start = cntr;
                int runlength = 1;
                long curcount = icount[cntr];
                long cumulcycles = itime[cntr];


                // collapse long runs of equivalent counts (e.g. basic blocks)
                int nextpc;
                for (; cntr < imax - 2; cntr = nextpc) {
                    nextpc = program.getNextPC(cntr);
                    if (icount[nextpc] != curcount) break;
                    runlength++;
                    cumulcycles += itime[nextpc];
                }

                // format the results appropriately (columnar)
                String cnt = StringUtil.rightJustify(curcount, 8);
                float pcnt = (100 * cumulcycles / totalcycles);
                String percent = "";
                String addr;
                if (runlength > 1) {
                    // if there is a run, adjust the count and address strings appropriately
                    addr = StringUtil.addrToString(start) + '-' + StringUtil.addrToString(cntr);
                    percent = " x" + runlength;
                } else {
                    addr = "       " + StringUtil.addrToString(start);
                }

                percent = StringUtil.leftJustify(percent, 7);

                // compute the percentage of total execution time
                if (curcount != 0) {
                    percent += StringUtil.rightJustify(cumulcycles, 8);
                    percent += " = " + StringUtil.toFixedFloat(pcnt, 4) + " %";
                }

                reportQuantity(' ' + addr, cnt, percent);
            }
        }

    }

    /**
     * The constructor for the <code>ProfileMonitor</code> class creates a factory that is capable of
     * producing profile monitors for each simulator passed.
     */
    public ProfileMonitor() {
        super("profile", "The \"profile\" monitor profiles the execution history " +
                "of every instruction in the program and generates a textual report " +
                "of the execution frequency for all instructions.");
    }

    /**
     * The <code>newMonitor()</code> method creates a new monitor for the given simulator that is capable of
     * collecting performance information as the program executes.
     *
     * @param s the simulator to create the monitor for
     * @return an instance of the <code>Monitor</code> interface that tracks performance information from the
     *         program
     */
    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
