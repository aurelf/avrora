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
import avrora.sim.util.MulticastFSMProbe;
import avrora.sim.util.MulticastProbe;

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

    protected class State {
        final String name;
        final int[] transition_time;
        final MulticastFSMProbe probes;

        State(String n, int[] tt) {
            name = n;
            transition_time = tt;
            probes = new MulticastFSMProbe();
        }
    }

    protected class TransitionEvent implements Simulator.Event {

        protected int oldState;
        protected int newState;

        public void fire() {
            // set the current state to the new state
            curState = newState;
            // fire any probes as necessary
            fireAfter(states[oldState].probes, oldState, newState);
            fireAfter(states[newState].probes, oldState, newState);
            fireAfter(globalProbe, oldState, newState);
        }

    }

    private void fireBefore(MulticastFSMProbe p, int oldState, int newState) {
        if ( !p.isEmpty() )
            p.fireBeforeTransition(oldState, newState);
    }

    private void fireAfter(MulticastFSMProbe p, int oldState, int newState) {
        if ( !p.isEmpty() )
            p.fireAfterTransition(oldState, newState);
    }

    public static final int IN_TRANSITION = -1;

    protected final int numStates;
    protected final int startState;
    protected final Clock clock;
    protected final TransitionEvent transEvent = new TransitionEvent();
    protected final MulticastFSMProbe globalProbe = new MulticastFSMProbe();
    protected State[] states;

    protected int curState;

    public FiniteStateMachine(Clock c, int ss, String[] nm, int tt) {
        clock = c;
        startState = ss;
        curState = ss;
        numStates = nm.length;
        states = new State[numStates];
        int[][] ttm = buildUniformTTM(numStates, tt);
        buildStates(nm, ttm);
    }

    public FiniteStateMachine(Clock c, int ss, String[] nm, int[][] ttm) {
        clock = c;
        startState = ss;
        curState = ss;
        numStates = nm.length;
        states = new State[numStates];
        buildStates(nm, ttm);
    }

    private void buildStates(String[] nm, int[][] ttm) {
        for ( int cntr = 0; cntr < numStates; cntr++ ) {
            states[cntr] = new State(nm[cntr], ttm[cntr]);
        }
    }

    public void insertProbe(Probe p) {
        globalProbe.add(p);
    }

    public void removeProbe(Probe p) {
        globalProbe.remove(p);
    }

    public void insertProbe(Probe p, int state) {
        states[state].probes.add(p);
    }

    public void removeProbe(Probe p, int state) {
        states[state].probes.remove(p);
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

    public void transition(int newState) {
        // are we currently in a transition already?
        if ( curState == IN_TRANSITION ) {
            throw Avrora.failure("cannot transition to state "
                    +newState+" while in transition: "
                    +transEvent.oldState+" -> "+transEvent.newState);
        }

        // get transition time
        int ttime = states[curState].transition_time[newState];
        if ( ttime < 0 ) // valid transition ?
            throw Avrora.failure("cannot transition from state "
                    +curState+" -> "+newState);

        // fire probes before transition
        fireBefore(globalProbe, curState, newState);
        fireBefore(states[curState].probes, curState, newState);
        fireBefore(states[newState].probes, curState, newState);

        if ( ttime == 0 ) {
            // transition is instantaneous
            int oldState = curState;
            curState = newState;
            fireAfter(states[oldState].probes, oldState, newState);
            fireAfter(states[newState].probes, oldState, newState);
            fireAfter(globalProbe, oldState, newState);
        }
        else {
            // transition will complete in the future
            transEvent.oldState = curState;
            transEvent.newState = newState;
            clock.insertEvent(transEvent, ttime);
        }
    }

    public int getTransitionTime(int beforeState, int afterState) {
        return states[beforeState].transition_time[afterState];
    }

    public String getStateName(int state) {
        return states[state].name;
    }

    public String getCurrentStateName() {
        return states[curState].name;
    }

    public Clock getClock() {
        return clock;
    }

    /**
     * The <code>buildUniformTTM()</code> method builds a transition time
     * matrix that is uniform; the machine can transition from any state to any other
     * state with the given transition time.
     * @param size the size of the transition time matrix
     * @param tt the transition time for each edge
     * @return a new transition time matrix where each entry is the given transition time
     */
    public static int[][] buildUniformTTM(int size, int tt) {
        int[][] ttm = new int[size][size];
        for ( int cntr = 0; cntr < size; cntr++ ) {
            for ( int loop = 0; loop < size; loop++ ) {
                ttm[cntr][loop] = tt;
            }
        }
        return ttm;
    }

    /**
     * The <code>buildBimodalTTM()</code> method builds a transition time matrix
     * that corresponds to a finite state machine with two modes. One special state
     * is the "default" state. The machine can transition from the default state to
     * any other state, and from any other state back to the default state, but not
     * between any other two states.
     * @param size the size of the transition time matrix
     * @param ds the default state
     * @param tf the transition times from each state back to the default state
     * @param tt the transition times from the default state to each other state
     * @return a square transition time matrix that represents a bimodal state machine
     */
    public static int[][] buildBimodalTTM(int size, int ds, int[] tf, int[] tt) {
        int[][] ttm = newTTM(size);

        for ( int cntr = 0; cntr < size; cntr++ ) {
            for ( int loop = 0; loop < size; loop++ ) {
                ttm[cntr][ds] = tf[cntr];
                ttm[ds][cntr] = tt[cntr];
            }
        }
        return ttm;
    }

    /**
     * The <code>setCircularTTM()</code> method builds a transition time matrix
     * that represents a finite state machine arranged in a ring; each state can transition
     * to one other state, wrapping around. For example, a finite state machine of consisting
     * of states S1, S2, and S3 could have a cycle S1 -> S2 -> S3 -> S1.
     * @param ttm the original transition time matrix
     * @param perm an array of integers representing the order of the state transitions in
     * the ring
     * @param tt the transition time between the corresponding states in the ring
     * @return a square
     */
    public static int[][] setCircularTTM(int[][] ttm, int[] perm, int[] tt) {
        int size = ttm.length;

        for ( int cntr = 0; cntr < size-1; cntr++ ) {
            ttm[perm[cntr]][perm[cntr+1]] = tt[perm[cntr+1]];
        }
        ttm[perm[size-1]][perm[0]] = tt[perm[0]];
        return ttm;
    }

    /**
     * The <code>newTTM()</code> method is a utility function for building a new
     * transition time matrix. It will create a new transition time matrix (TTM) where
     * each entry is <code>-1</code>, indicating that there are no legal state transitions.
     * @param size the size of matrix, i.e. the number of rows, which is equal to the
     * number of columns
     * @return a square matrix of the given size where each entry is set to -1
     */
    public static int[][] newTTM(int size) {
        int[][] ttm = new int[size][size];

        for ( int cntr = 0; cntr < size; cntr++ ) {
            for ( int loop = 0; loop < size; loop++ ) {
                ttm[cntr][loop] = -1;
            }
        }
        return ttm;
    }

    /**
     * The <code>setDiagonal()</code> method sets the diagonal of the given transition
     * time matrix to the specified value. This is useful for finite state machines where
     * transitions from one state to the same state is either impossible or a no-op.
     * @param ttm the original transition time matrix
     * @param diag the value to set the diagonal entries to
     * @return the original transition time matrix with the diagonal entries appropriately
     * set
     */
    public static int[][] setDiagonal(int[][] ttm, int diag) {
        for ( int cntr = 0; cntr < ttm.length; cntr++ )
            ttm[cntr][cntr] = diag;
        return ttm;
    }
}
