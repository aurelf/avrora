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
 * The <code>BranchCounter</code> class is a profiling probe that can be inserted at a branch instruction to
 * count the number of times the branch is taken and not taken. It demonstrates the ability to inspect the
 * state of the program after the execution of a program. It determines whether the branch was taken by
 * inspecting the program counter of the new state. If the program counter is not equal to the instruction
 * following the branch, then the branch was taken.
 *
 * @author Ben L. Titzer
 * @see avrora.sim.util.Counter
 */
public class BranchCounter implements Simulator.Probe {

    /**
     * This field tracks the number of times the branch is taken. It is incremented in the
     * <code>fireAfter</code> method if the branch was taken.
     */
    public int takenCount;

    /**
     * This field tracks the number of times the branch is not taken. It is incremented in the
     * <code>fireAfter</code> method if the branch was not taken.
     */
    public int nottakenCount;

    /**
     * The <code>fireBefore()</code> method is called before the probed instruction executes. In the
     * implementation of the branch counter, nothing needs to be done before the branch is executed, so this
     * method does nothing.
     *
     * @param i       the instruction being probed
     * @param address the address at which this instruction resides
     * @param state   the state of the simulation
     */
    public void fireBefore(Instr i, int address, State state) {
        // do nothing.
    }

    /**
     * The <code>fireAfter()</code> method is called after the probed instruction executes. In the
     * implementation of the branch counter, the counter determines whether the branch was taken by inspecting
     * the program counter of the new state. If the program counter is not equal to the instruction following
     * the branch, then the branch was taken.
     *
     * @param i       the instruction being probed
     * @param address the address at which this instruction resides
     * @param state   the state of the simulation
     */
    public void fireAfter(Instr i, int address, State state) {
        int nextaddr = address + i.getSize();
        if (state.getPC() == nextaddr)
            nottakenCount++;
        else
            takenCount++;
    }
}
