
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
public class SleepMonitor extends MonitorFactory {

    public class Monitor implements avrora.monitors.Monitor, Simulator.Event {
        public final Simulator simulator;
        public final Program program;

        protected long sleepCycles;
        protected long awakeCycles;

        Monitor(Simulator s) {
            simulator = s;
            program = s.getProgram();
            s.insertEvent(this, 1);
        }

        public void report() {
            reportQuantity("Time slept", sleepCycles, "cycles");
            reportQuantity("Time awake", awakeCycles, "cycles");
            float percent = 100*((float)sleepCycles) / (sleepCycles + awakeCycles);
            reportQuantity("Total", percent, "%");
        }

        public void fire() {
            if ( simulator.getState().isSleeping() )
                sleepCycles++;
            else
                awakeCycles++;

            simulator.insertEvent(this, 1);
        }

    }

    public SleepMonitor() {
        // TODO: write a decent help string
        super("sleep", "");
    }

    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
