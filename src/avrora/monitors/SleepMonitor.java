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
 * The <code>SleepMonitor</code> class is a monitor that tracks statistics
 * about the sleeping patterns of programs, including the total number of
 * cycles awake and the total number of cycles asleep during the simulation.
 *
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
        super("sleep", "The \"sleep\" is a monitor that tracks statistics "+
               "about the sleeping patterns of programs, including the total number of "+
               "cycles awake and the total number of cycles asleep during the simulation.");
    }

    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
