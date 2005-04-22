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

import avrora.core.Instr;
import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.util.ProgramProfiler;
import avrora.util.StringUtil;
import avrora.util.TermUtil;

/**
 * The <code>StackMonitor</code> class is a monitor that tracks the height of the program's stack over the
 * execution of the simulation and reports the maximum stack of the program.
 *
 * @author Ben L. Titzer
 */
public class StackMonitor extends MonitorFactory {

    /**
     * The <code>Monitor</code> class implements a monitor for the stack height that inserts a probe after
     * every instruction in the program and checks the stack height after each instruction is executed.
     */
    public class Monitor extends Simulator.Probe.Empty implements avrora.monitors.Monitor {
        public final Simulator simulator;
        public final Program program;

        int minStack1 = Integer.MAX_VALUE;
        int minStack2 = Integer.MAX_VALUE;
        int minStack3 = Integer.MAX_VALUE;
        int maxStack = Integer.MIN_VALUE;

        final Simulator.Printer printer;

        public void fireAfter(State s, int pc) {
            int newStack = s.getSP();
            if (newStack == minStack1) return;

            if (printer.enabled)
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
            printer = simulator.getPrinter("monitor.stack");
            // insert the global probe
            s.insertProbe(this);
        }

        /**
         * The <code>report()</code> method generates a textual report after the simulation is complete. The
         * text report contains the 3 smallest stack pointers encountered (tracking all three is necessary
         * because the stack pointer begins at 0 and then is initialized one byte at a time).
         */
        public void report() {
            TermUtil.reportQuantity("Minimum stack pointer #1", StringUtil.addrToString(minStack1), "");
            TermUtil.reportQuantity("Minimum stack pointer #2", StringUtil.addrToString(minStack2), "");
            TermUtil.reportQuantity("Minimum stack pointer #3", StringUtil.addrToString(minStack3), "");
            TermUtil.reportQuantity("Maximum stack pointer", StringUtil.addrToString(maxStack), "");
            TermUtil.reportQuantity("Maximum stack size #1", (maxStack - minStack1), "bytes");
            TermUtil.reportQuantity("Maximum stack size #2", (maxStack - minStack2), "bytes");
            TermUtil.reportQuantity("Maximum stack size #3", (maxStack - minStack3), "bytes");
        }

    }

    /**
     * The constructor for the <code>StackMonitor</code> class builds a new <code>MonitorFactory</code>
     * capable of creating monitors for each <code>Simulator</code> instance passed to the
     * <code>newMonitor()</code> method.
     */
    public StackMonitor() {
        super("The \"stack\" monitor tracks the height of the stack while " +
                "the program executes, reporting the maximum stack height seen.");
    }

    /**
     * The <code>newMonitor()</code> method creates a new monitor that is capable of monitoring the stack
     * height of the program over its execution.
     *
     * @param s the simulator to create a monitor for
     * @return an instance of the <code>Monitor</code> interface for the specified simulator
     */
    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
