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
import avrora.core.Instr;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.Clock;
import avrora.sim.util.ProgramProfiler;
import avrora.sim.util.ProgramTimeProfiler;
import avrora.util.Option;
import avrora.util.StringUtil;
import avrora.util.Terminal;
import avrora.util.TermUtil;

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

    public final Option.Bool CYCLES = options.newOption("record-cycles", true,
            "This option is used by the profiling and controls whether it records " +
            "the cycles consumed by each instruction or basic block. ");
    public final Option.Bool EMPTY = options.newOption("empty-probe", false,
            "This option is used to test the overhead of adding an empty probe to every" +
            "instruction. ");
    public final Option.Long PERIOD = options.newOption("period", 0,
            "This option specifies whether the profiling will be exact or periodic. When " +
            "this option is set to non-zero, then a sample of the program counter is taken at " +
            "the specified period in clock cycles, rather than through probes at each instruction.");

    public class Monitor implements avrora.monitors.Monitor {
        public final Simulator simulator;
        public final Program program;
        public final CCProbe ccprobe;
        public final CProbe cprobe;

        public final long icount[];
        public final long itime[];

        Monitor(Simulator s) {
            simulator = s;
            program = s.getProgram();
            icount = new long[program.program_end];
            itime = new long[program.program_end];

            ccprobe = new CCProbe(icount, itime);
            cprobe = new CProbe(icount);

            // insert the global probe
            insertInstrumentation(s);
        }

        private void insertInstrumentation(Simulator s) {
            if ( EMPTY.get() ) {
                s.insertProbe(new EmptyProbe());
                return;
            }
            long period = PERIOD.get();
            if ( period > 0 ) {
                s.insertEvent(new PeriodicProfile(period), period);
                return;
            }

            if ( CYCLES.get() )
                s.insertProbe(ccprobe);
            else
                s.insertProbe(cprobe);

        }

        public class PeriodicProfile implements Simulator.Event {
            private final long period;
            State s;

            PeriodicProfile(long p) {
                period = p;
                // TODO: this getState() is actually unsafe;
                // optimizations in the future may break this!!!!
                s = simulator.getState();
            }

            public void fire() {
                icount[s.getPC()]++;
                simulator.insertEvent(this, period);
            }
        }

        public class EmptyProbe implements Simulator.Probe {
            public void fireBefore(Instr i, int address, State state) {
            }

            public void fireAfter(Instr i, int address, State state) {
            }
        }

        public class CCProbe implements Simulator.Probe {
            public final long count[];
            public final long time[];

            protected long timeBegan;

            public CCProbe(long[] ic, long[] it) {
                count = ic;
                time = it;
            }

            public void fireBefore(Instr i, int address, State state) {
                count[address]++;
                timeBegan = state.getCycles();
            }

            public void fireAfter(Instr i, int address, State state) {
                time[address] += state.getCycles() - timeBegan;
            }
        }

        public class CProbe implements Simulator.Probe {

            public final long count[];

            public CProbe(long[] ic) {
                count = ic;
            }

            public void fireBefore(Instr i, int address, State state) {
                count[address]++;
            }

            public void fireAfter(Instr i, int address, State state) {
                // do nothing
            }
        }

        public void report() {
            TermUtil.printSeparator(Terminal.MAXLINE, "Profiling results");
            Terminal.printGreen("       Address     Count  Run     Cycles     Cumulative");
            Terminal.nextln();
            TermUtil.printThinSeparator(Terminal.MAXLINE);

            reportProfile(icount, itime);

        }

        private void reportProfile(long[] icount, long[] itime) {
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
                    percent += " = " + StringUtil.rightJustify(StringUtil.toFixedFloat(pcnt, 4),8) + " %";
                }

                TermUtil.reportQuantity(' ' + addr, cnt, percent);
            }
        }

    }

    public ProfileMonitor() {
        super("profile", "The \"profile\" monitor profiles the execution history " +
                "of every instruction in the program and generates a textual report " +
                "of the execution frequency for all instructions.");
    }

    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
