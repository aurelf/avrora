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

package avrora.monitors;

import avrora.sim.util.ProgramProfiler;
import avrora.sim.Simulator;
import avrora.core.Program;
import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.util.Options;

/**
 * The <code>ProfileMonitor</code> class represents a monitor that can collect profiling information such as counts and
 * branchcounts about the program as it executes.
 *
 * @author Ben L. Titzer
 */
public class ProfileMonitor extends MonitorFactory {

    /**
     * The <code>Monitor</code> class implements the monitor for the profiler. It contains a
     * <code>ProgramProfiler</code> instance which is a probe that is executed after every instruction that collects
     * execution counts for every instruction in the program.
     */
    public class Monitor implements avrora.monitors.Monitor {
        public final Simulator simulator;
        public final Program program;
        public final ProgramProfiler profile;

        Monitor(Simulator s) {
            simulator = s;
            program = s.getProgram();
            profile = new ProgramProfiler(program);
            // insert the global probe
            s.insertProbe(profile);
        }

        /**
         * The <code>report()</code> method generates a textual report for the profiling information gathered from the
         * execution of the program. The result is a table of performance information giving the number of executions of
         * each instruction, compressed for basic blocks.
         */
        public void report() {
            Terminal.printSeparator(78);
            Terminal.printGreen("          Address     Count    Percent   Run       Cumulative");
            Terminal.nextln();
            Terminal.printSeparator(78);
            double total = 0;
            long[] icount = profile.icount;
            int imax = icount.length;

            // compute the total for percentage calculations
            for (int cntr = 0; cntr < imax; cntr++) {
                total += icount[cntr];
            }

            // report the profile for each instruction in the program
            for (int cntr = 0; cntr < imax; cntr = program.getNextPC(cntr)) {
                int start = cntr;
                int runlength = 1;
                long c = icount[cntr];

                // collapse long runs of equivalent counts (e.g. basic blocks)
                int nextpc;
                for (; cntr < imax - 2; cntr = nextpc) {
                    nextpc = program.getNextPC(cntr);
                    if (icount[nextpc] != c) break;
                    runlength++;
                }

                // format the results appropriately (columnar)
                String cnt = StringUtil.rightJustify(c, 8);
                float pcnt = (float)(100 * c / total);
                String percent = StringUtil.toFixedFloat(pcnt, 4) + " %";
                String addr;
                if (runlength > 1) {
                    addr = StringUtil.addrToString(start) + "-" + StringUtil.addrToString(cntr);
                    if (c != 0) {
                        percent += StringUtil.leftJustify(" x" + runlength, 7);
                        percent += " = " + StringUtil.toFixedFloat(pcnt * runlength, 4) + " %";
                    }
                } else {
                    addr = "       " + StringUtil.addrToString(start);
                }

                reportQuantity("    " + addr, cnt, "  " + percent);
            }
        }

    }

    /**
     * The constructor for the <code>ProfileMonitor</code> class creates a factory that is capable of producing profile
     * monitors for each simulator passed.
     */
    public ProfileMonitor() {
        super("profile", "The \"profile\" monitor profiles the execution history " +
                         "of every instruction in the program and generates a textual report " +
                         "of the execution frequency for all instructions.");
    }

    /**
     * The <code>newMonitor()</code> method creates a new monitor for the given simulator that is capable of collecting
     * performance information as the program executes.
     *
     * @param s the simulator to create the monitor for
     * @return an instance of the <code>Monitor</code> interface that tracks performance information from the program
     */
    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
