
package avrora.monitors;

import avrora.sim.util.ProgramProfiler;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.core.Program;
import avrora.core.Instr;
import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.util.Options;
import avrora.util.Verbose;

/**
 * @author Ben L. Titzer
 */
public class StackMonitor extends MonitorFactory {

    public class Monitor implements avrora.monitors.Monitor, Simulator.Probe {
        public final Simulator simulator;
        public final Program program;
        public final ProgramProfiler profile;

            int minStack1 = Integer.MAX_VALUE;
            int minStack2 = Integer.MAX_VALUE;
            int minStack3 = Integer.MAX_VALUE;
            int maxStack = Integer.MIN_VALUE;

            final Verbose.Printer printer = Verbose.getVerbosePrinter("sim.stack");

            public void fireBefore(Instr i, int address, State s) {
                // do nothing
            }

            public void fireAfter(Instr i, int address, State s) {
                int newStack = s.getSP();
                if (newStack == minStack1) return;

                if ( printer.enabled )
                    printer.println("new stack: " + newStack);

                if (newStack < minStack1) {
                    minStack3 = minStack2;
                    minStack2 = minStack1;
                    minStack1 = newStack;
                } else if (newStack < minStack2) {
                    minStack3 = minStack2;
                    minStack2 = newStack;
                } else if (newStack == minStack2)
                    return;
                else if (newStack < minStack3) {
                    minStack3 = newStack;
                }

                if (newStack > maxStack) maxStack = newStack;
            }

        Monitor(Simulator s) {
            simulator = s;
            program = s.getProgram();
            profile = new ProgramProfiler(program);
            // insert the global probe
            s.insertProbe(profile);
        }

        public void report() {
            reportQuantity("Minimum stack pointer #1", "0x" + StringUtil.toHex(minStack1, 4), "");
            reportQuantity("Minimum stack pointer #2", "0x" + StringUtil.toHex(minStack2, 4), "");
            reportQuantity("Minimum stack pointer #3", "0x" + StringUtil.toHex(minStack3, 4), "");
            reportQuantity("Maximum stack pointer", "0x" + StringUtil.toHex(maxStack, 4), "");
            reportQuantity("Maximum stack size #1", (maxStack - minStack1), "bytes");
            reportQuantity("Maximum stack size #2", (maxStack - minStack2), "bytes");
            reportQuantity("Maximum stack size #3", (maxStack - minStack3), "bytes");
        }

    }

    public StackMonitor() {
        // TODO: write a decent help string
        super("stack", "");
    }

    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
