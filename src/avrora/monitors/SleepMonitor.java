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
import avrora.sim.FiniteStateMachine;
import avrora.sim.Simulator;
import avrora.sim.clock.Clock;
import avrora.sim.mcu.AtmelMicrocontroller;
import avrora.util.StringUtil;
import avrora.util.TermUtil;
import avrora.util.Terminal;

/**
 * The <code>SleepMonitor</code> class is a monitor that tracks statistics about the sleeping patterns of
 * programs, including the total number of cycles awake and the total number of cycles asleep during the
 * simulation.
 *
 * @author Ben L. Titzer
 */
public class SleepMonitor extends MonitorFactory {

    public static class Monitor implements avrora.monitors.Monitor, FiniteStateMachine.Probe {
        public final Simulator simulator;
        public final Program program;

        protected long sleepCycles;
        protected long awakeCycles;

        long[] times;
        long lastTransition;
        final FiniteStateMachine fsm;
        final Clock clock;

        Monitor(Simulator s) {
            simulator = s;
            program = s.getProgram();
            fsm = ((AtmelMicrocontroller)s.getMicrocontroller()).getFSM();
            clock = fsm.getClock();
            times = new long[fsm.getNumberOfStates()];
            fsm.insertProbe(this);
        }

        public void report() {
            recordCycles(fsm.getCurrentState());

            TermUtil.printSeparator(Terminal.MAXLINE, "Sleep Monitor Results");
            Terminal.printGreen("State                      Cycles  Percent");
            Terminal.nextln();
            TermUtil.printThinSeparator(Terminal.MAXLINE);
            long total = clock.getCount();
            for ( int cntr = 0; cntr < times.length; cntr++ ) {
                String state = fsm.getStateName(cntr);
                Terminal.printGreen(StringUtil.leftJustify(state, 23));
                Terminal.printBrightCyan(StringUtil.rightJustify(times[cntr],10));
                float pcnt = (100*times[cntr])/(float)total;
                Terminal.printBrightCyan(StringUtil.rightJustify(StringUtil.toFixedFloat(pcnt, 4),9));
                Terminal.print(" %");
                Terminal.nextln();
            }
        }


        public void fireBeforeTransition(int bs, int as) {
            // do nothing.
        }

        public void fireAfterTransition(int bs, int as) {
            recordCycles(bs);
        }

        private void recordCycles(int bs) {
            long now = clock.getCount();
            times[bs] += now - lastTransition;
            lastTransition = now;
        }

    }

    public SleepMonitor() {
        super("The \"sleep\" is a monitor that tracks statistics " +
                "about the sleeping patterns of programs, including the total number of " +
                "cycles awake and the total number of cycles asleep during the simulation.");
    }

    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
