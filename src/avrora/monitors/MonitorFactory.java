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

import avrora.util.Options;
import avrora.util.Terminal;
import avrora.sim.Simulator;

/**
 * The <code>MonitorFactory</code> class represents a profiling utility
 * that is able to produce a <code>Monitor</code> for a simulation. The
 * monitor can use probes, watches, and events to monitor the execution
 * of the program and issue a report after the simulation is completed.
 *
 * @author Ben L. Titzer
 */
public abstract class MonitorFactory {
    public final String help;
    public final String shortName;

    public final Options options;

    protected MonitorFactory(String sn, String h) {
        shortName = sn;
        help = h;
        options = new Options();
    }


    public abstract Monitor newMonitor(Simulator s);

    public void processOptions(Options o) {
        options.process(o);
    }

    public String getShortName() {
        return shortName;
    }

    protected void reportQuantity(String name, long val, String units) {
        reportQuantity(name, Long.toString(val), units);
    }

    protected void reportQuantity(String name, float val, String units) {
        reportQuantity(name, Float.toString(val), units);
    }

    protected void reportQuantity(String name, String val, String units) {
        Terminal.printGreen(name);
        Terminal.print(": ");
        Terminal.printBrightCyan(val);
        Terminal.println(" " + units);
    }
}
