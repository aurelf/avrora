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

import avrora.sim.util.DeltaQueue;

/**
 * @author Ben L. Titzer
 */
public class MainClock extends Clock {

    protected final DeltaQueue eventQueue;

    public MainClock(String n, long hz) {
        super(n, hz);
        eventQueue = new DeltaQueue();
    }

    /**
     * The <code>getCount()</code> method returns the number of clock cycles (ticks)
     * that have elapsed for this clock.
     * @return the number of elapsed time ticks in clock cycles
     */
    public long getCount() {
        return eventQueue.getCount();
    }

    /**
     * The <code>insertEvent()</code> method inserts an event into the
     * event queue of the clock with the specified delay in clock cycles.
     * The event will then be executed at the future time specified.
     *
     * @param e      the event to be inserted
     * @param cycles the number of cycles in the future at which to fire
     */
    public void insertEvent(Simulator.Event e, long cycles) {
        eventQueue.add(e, cycles);
    }

    /**
     * The <code>removeEvent()</code> method removes an event from
     * the event queue of the clock. The comparison used is reference
     * equality, not <code>.equals()</code>.
     *
     * @param e the event to remove
     */
    public void removeEvent(Simulator.Event e) {
        eventQueue.remove(e);
    }

    /**
     * The <code>advance()</code> method advances the time of the clock
     * by the number of cycles. This may happen as the result of executing
     * an instruction, sleeping for a time, delaying, etc. This method is
     * only intended for use by the agent driving the clock; e.g. the
     * simulator, and not a monitor or probe.
     *
     * @param cycles the number of cycles to advance the clock
     */
    public void advance(long cycles) {
        eventQueue.advance(cycles);
    }

    public long getFirstEventDelta() {
        return eventQueue.getHeadDelta();
    }
}
