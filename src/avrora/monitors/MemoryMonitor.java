
package avrora.monitors;

import avrora.sim.util.ProgramProfiler;
import avrora.sim.Simulator;
import avrora.sim.BaseInterpreter;
import avrora.sim.mcu.Microcontroller;
import avrora.core.Program;
import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.util.Options;

/**
 * @author Ben L. Titzer
 */
public class MemoryMonitor extends MonitorFactory {

    public class Monitor implements avrora.monitors.Monitor {
        public final Simulator simulator;
        public final Microcontroller microcontroller;
        public final Program program;
        public final int ramsize;
        public final int memstart;
        public final avrora.sim.util.MemoryProfiler memprofile;

        Monitor(Simulator s) {
            simulator = s;
            microcontroller = simulator.getMicrocontroller();
            program = simulator.getProgram();
            ramsize = microcontroller.getRamSize();
            memstart = BaseInterpreter.NUM_REGS + microcontroller.getIORegSize();
            memprofile = new avrora.sim.util.MemoryProfiler(ramsize);
            for (int cntr = memstart; cntr < ramsize; cntr++) {
                simulator.insertWatch(memprofile, cntr);
            }
        }

        public void report() {
            Terminal.printSeparator(78);
            Terminal.printGreen("   Address     Reads               Writes");
            Terminal.nextln();
            Terminal.printSeparator(78);
            double rtotal = 0;
            long[] rcount = memprofile.rcount;
            double wtotal = 0;
            long[] wcount = memprofile.wcount;
            int imax = rcount.length;

            // compute the total for percentage calculations
            for (int cntr = 0; cntr < imax; cntr++) {
                rtotal += rcount[cntr];
                wtotal += wcount[cntr];
            }

            int zeroes = 0;

            for (int cntr = memstart; cntr < imax; cntr++) {
                int start = cntr;
                long r = rcount[cntr];
                long w = wcount[cntr];

                if (r == 0 && w == 0)
                    zeroes++;
                else
                    zeroes = 0;

                // skip long runs of zeroes
                if (zeroes == 2) {
                    Terminal.println("                   .                    .");
                    continue;
                } else if (zeroes > 2) continue;

                String rcnt = StringUtil.rightJustify(r, 8);
                float rpcnt = (float) (100 * r / rtotal);
                String rpercent = StringUtil.toFixedFloat(rpcnt, 4) + " %";

                String wcnt = StringUtil.rightJustify(w, 8);
                float wpcnt = (float) (100 * w / wtotal);
                String wpercent = StringUtil.toFixedFloat(wpcnt, 4) + " %";

                String addr = StringUtil.addrToString(start);

                Terminal.printGreen("    " + addr);
                Terminal.print(": ");
                Terminal.printBrightCyan(rcnt);
                Terminal.print(" " + ("  " + rpercent));
                Terminal.printBrightCyan(wcnt);
                Terminal.println(" " + ("  " + wpercent));
            }
        }

    }

    public MemoryMonitor() {
        // TODO: write a decent help string
        super("memory", "");
    }

    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
