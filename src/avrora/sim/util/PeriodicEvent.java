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

package avrora.sim.util;

import avrora.sim.Simulator;

/**
 * The <code>PeriodicEvent</code> class is a utility that allows a <code>Simulator.Event</code> to be inserted
 * into the simulator that will fire with a specified period of clock cycles. It works by automatically adding
 * itself back into the timer queue at each firing.
 *
 * @author Ben L. Titzer
 * @see avrora.sim.Simulator.Event
 */
public class PeriodicEvent implements Simulator.Event {
    /**
     * This field stores the simulator in which the event will continually be reinserted.
     */
    public final Simulator simulator;

    /**
     * This field stores the event that will be fired after each period.
     */
    public final Simulator.Event event;

    /**
     * This field stores the period (in clock cycles) that the event will be fired.
     */
    public final long period;

    /**
     * The constructor for the <code>PeriodicEvent</code> class creates a new periodic event with the
     * specified period. Each time the event fires, it will be added again back into the simulator's timer
     * event queue with the same delta. The result is the specified event fires with the precise period
     * specified.
     * <p/>
     * Creating the <code>PeriodicEvent</code> does not insert it into the simulator. It is important that
     * these instances of <code>Simulator</code> match--this probe will always reinsert itself into the
     * instance passed in the constructor.
     *
     * @param s the simulator in which to reinsert the event each time
     * @param t the event to fire after each period
     * @param p the period in clock cycles
     */
    public PeriodicEvent(Simulator s, Simulator.Event t, long p) {
        event = t;
        period = p;
        simulator = s;
    }

    /**
     * The <code>fire()</code> method is called by the simulator when the timer event occurs. In this
     * implementation, the periodic event is reinserted into the timer queue, and then the <code>fire()</code>
     * method of the event is called.
     */
    public void fire() {
        simulator.insertEvent(this, period);
        event.fire();
    }
}
