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

package avrora.sim;

import avrora.Avrora;

/**
 * The <code>FiniteStateMachine</code> class represents a model of a finite state machine that
 * allows probing and monitoring the state of a device.
 *
 * @author Ben L. Titzer
 */
public class FiniteStateMachine {

    /**
     * The <code>Probe</code> interface allows observation of the state changes of a finite
     * state machine. Probes can be inserted for a particular state so that the probe will
     * fire before and after transitions into and out of that state, as well as for every
     * state transition.
     */
    public interface Probe {
        /**
         * The <code>fireBeforeTransition()</code> method allows the probe to gain control
         * before the state machine transitions between two states. The before state and the
         * after state are passed as parameters.
         * @param beforeState the before state represented as an integer
         * @param afterState the after state represented as an integer
         */
        public void fireBeforeTransition(int beforeState, int afterState);

        /**
         * The <code>fireAfterTransition()</code> method allows the probe to gain control
         * after the state machine transitions between two states. The before state and the
         * after state are passed as parameters.
         * @param beforeState the before state represented as an integer
         * @param afterState the after state represented as an integer
         */
        public void fireAfterTransition(int beforeState, int afterState);
    }


    public static final int IN_TRANSITION = -1;

    public final int numStates;
    public final int startState;
    public final Clock clock;
    protected int curState;

    public FiniteStateMachine(Clock c, int ss, int ns) {
        clock = c;
        startState = ss;
        curState = ss;
        numStates = ns;
    }


    public void insertProbe(Probe p) {
        throw Avrora.unimplemented();
    }

    public void removeProbe(Probe p) {
        throw Avrora.unimplemented();
    }

    public void insertProbe(Probe p, int state) {
        throw Avrora.unimplemented();
    }

    public void removeProbe(Probe p, int state) {
        throw Avrora.unimplemented();
    }

    public int getNumberOfStates() {
        return numStates;
    }

    public int getStartState() {
        return startState;
    }

    public int getCurrentState() {
        return curState;
    }

    public void transition(int newstate) {
        throw Avrora.unimplemented();
    }

    public int getTransitionTime(int beforeState, int afterState) {
        throw Avrora.unimplemented();
    }

    public String getStateName(int state) {
        throw Avrora.unimplemented();
    }

    public Clock getClock() {
        return clock;
    }
}
