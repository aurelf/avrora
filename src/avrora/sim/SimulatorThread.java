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

package avrora.sim;

import avrora.Avrora;


/**
 * The <code>SimulatorThread</code> class is a thread intended to run a
 * <code>Simulator</code> in a multiple-node simulation. The mapping is
 * one-to-one: each simulator is expected to be run in its own thread.
 * Multiple simulators are then synchronized by being inserted into a group
 * using the <code>GlobalClock</code> class.
 *
 * @author Ben L. Titzer
 */
public class SimulatorThread extends Thread {

    /**
     * The <code>simulator</code> field stores a reference to the simulator that
     * this thread encapsulates.
     */
    protected final Simulator simulator;

    /**
     * The constructor for the simulator thread accepts an instance of <code>Simulator</code>
     * as a parameter and stores it internally.
     * @param s the simulator this thread is intended to run.
     */
    public SimulatorThread(Simulator s) {
        simulator = s;
    }

    /**
     * The <code>getSimulator()</code> method gets the <code>Simulator</code> instance that
     * this thread is bound to.
     * @return the instance of <code>Simulator</code> this thread is intended to run.
     */
    public Simulator getSimulator() {
        return simulator;
    }

    /**
     * The <code>run()</code> method begins the simulation, calling the <code>start()</code>
     * method of the <code>Simulator</code> instance associated with this thread.
     */
    public void run() {
        try {
            simulator.start();
        } catch ( Simulator.TimeoutException te ) {
            // suppress timeout exceptions.
        } catch ( Avrora.Error e ) {
            e.report();
        }
    }
}
