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

package avrora.sim.util;

import avrora.core.Instr;
import avrora.sim.Simulator;
import avrora.sim.State;

/**
 * The <code>Counter</code> class is a utility for profiling programs. It simply increments an internal counter every
 * time the probe fires. This very simple type of probe can be used for profiling counts of basic blocks, interrupt
 * routines, particular methods, inner loops, or even to count the total number of instructions executed in the
 * program.
 *
 * @author Ben L. Titzer
 */
public class Counter implements Simulator.Probe {
    /**
     * The <code>count</code> field stores the accumulation of all events received by this counter. This field is
     * incremented once each time the <code>fireBefore()</code> method is invoked.
     */
    public long count;

    /**
     * The <code>fireBefore()</code> method is called before the probed instruction executes. In the implementation of
     * the counter, it simply updates the internal counter.
     *
     * @param i       the instruction being probed
     * @param address the address at which this instruction resides
     * @param state   the state of the simulation
     */
    public void fireBefore(Instr i, int address, State state) {
        count++;
    }

    /**
     * The <code>fireAfter()</code> method is called after the probed instruction executes. In the implementation of the
     * counter, it does nothing.
     *
     * @param i       the instruction being probed
     * @param address the address at which this instruction resides
     * @param state   the state of the simulation
     */
    public void fireAfter(Instr i, int address, State state) {
    }
}
