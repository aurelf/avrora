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

import avrora.sim.Simulator;
import avrora.util.Options;
import avrora.util.Terminal;

/**
 * The <code>MonitorFactory</code> class represents a profiling utility that is able to produce a
 * <code>Monitor</code> for a simulation. The monitor can use probes, watches, and events to monitor the
 * execution of the program and issue a report after the simulation is completed.
 *
 * @author Ben L. Titzer
 */
public abstract class MonitorFactory {

    /**
     * The <code>help</code> field stores a reference to a string that represents the contextual help from
     * this monitor.
     */
    public final String help;

    /**
     * The <code>shortName</code> field stores a reference to the short name of the monitor.
     */
    public final String shortName;

    /**
     * The <code>options</code> field stores a reference to an instance of the <code>Options</code> class that
     * stores the command line options to the monitor.
     */
    public final Options options;

    /**
     * The constructor for the <code>MonitorFactory</code> class initializes the <code>options</code> field,
     * as well as the references to the help string and the short name of the monitor.
     *
     * @param sn the short name of the monitor as a string
     * @param h  the help item for the monitor as a string
     */
    protected MonitorFactory(String sn, String h) {
        shortName = sn;
        help = h;
        options = new Options();
    }


    /**
     * The <code>newMonitor()</code> method creates a new monitor for the specified instance of
     * <code>Simulator</code>. The resulting monitor may insert probes, watches, or events into the simulation
     * to collect information and later report that information after the simulation is complete.
     *
     * @param s the <code>Simulator</code> instance to create the monitor for
     * @return an instance of the <code>Monitor</code> interface that represents the monitor for the
     *         simulator
     */
    public abstract Monitor newMonitor(Simulator s);

    /**
     * The <code>processOptions()</code> method is called after the <code> MonitorFactory</code> instance is
     * created. These options are the options left over from processing the command line, extracting the
     * parameters to the main program, extracting the parameters from the action.
     *
     * @param o the options representing the known and unknown options from the command line
     */
    public void processOptions(Options o) {
        options.process(o);
    }

    /**
     * The <code>getShortName()</code> method returns a shortened version of the name of this monitor for more
     * quick access at the command line.
     *
     * @return a string representation of an abbreviated name for this monitor
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * The <code>reportQuantity()</code> method can be used by subclasses of <code>MonitorFactory</code> to
     * report various quantities such as execution time, cycles spent sleeping, etc, to the terminal.
     *
     * @param name  the name of the quantity being reported
     * @param val   the value to be reported as a long integer
     * @param units the units for the quantity as a string
     */
    protected void reportQuantity(String name, long val, String units) {
        reportQuantity(name, Long.toString(val), units);
    }

    /**
     * The <code>reportQuantity()</code> method can be used by subclasses of <code>MonitorFactory</code> to
     * report various quantities such as execution time, cycles spent sleeping, etc, to the terminal.
     *
     * @param name  the name of the quantity being reported
     * @param val   the value to be reported as a floating point number
     * @param units the units for the quantity as a string
     */
    protected void reportQuantity(String name, float val, String units) {
        reportQuantity(name, Float.toString(val), units);
    }

    /**
     * The <code>reportQuantity()</code> method can be used by subclasses of <code>MonitorFactory</code> to
     * report various quantities such as execution time, cycles spent sleeping, etc, to the terminal.
     *
     * @param name  the name of the quantity being reported
     * @param val   the value to be reported as a string
     * @param units the units for the quantity as a string
     */
    protected void reportQuantity(String name, String val, String units) {
        Terminal.printGreen(name);
        Terminal.print(": ");
        Terminal.printBrightCyan(val);
        Terminal.println(" " + units);
    }
}
