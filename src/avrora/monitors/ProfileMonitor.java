
package avrora.monitors;

import avrora.sim.util.ProgramProfiler;
import avrora.sim.Simulator;
import avrora.core.Program;
import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.util.Options;

/**
 * @author Ben L. Titzer
 */
public class ProfileMonitor extends MonitorFactory {

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
                float pcnt = (float) (100 * c / total);
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

    public ProfileMonitor() {
        // TODO: write a decent help string
        super("profile", "");
    }

    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
