package avrora.sim.util;

import avrora.core.Instr;
import avrora.sim.Simulator;
import avrora.sim.State;

/**
 * The <code>Counter</code> class is a utility for profiling programs. It
 * simply increments an internal counter every time the probe fires.
 * This very simple type of probe can be used for profiling counts of basic
 * blocks, interrupt routines, particular methods, inner loops, or even
 * to count the total number of instructions executed in the program.
 *
 * @author Ben L. Titzer
 */
public class Counter implements Simulator.Probe {
    /**
     * The <code>count</code> field stores the accumulation of all
     * events received by this counter. This field is incremented
     * once each time the <code>fireBefore()</code> method is
     * invoked.
     */
    public long count;

    /**
     * The <code>fireBefore()</code> method is called before the probed instruction
     * executes. In the implementation of the counter, it simply updates the
     * internal counter.
     *
     * @param i       the instruction being probed
     * @param address the address at which this instruction resides
     * @param state   the state of the simulation
     */
    public void fireBefore(Instr i, int address, State state) {
        count++;
    }

    /**
     * The <code>fireAfter()</code> method is called after the probed instruction
     * executes. In the implementation of the counter, it does nothing.
     *
     * @param i       the instruction being probed
     * @param address the address at which this instruction resides
     * @param state   the state of the simulation
     */
    public void fireAfter(Instr i, int address, State state) {
    }
}
