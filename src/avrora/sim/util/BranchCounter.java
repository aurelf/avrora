package avrora.sim.util;

import avrora.core.Instr;
import avrora.sim.Simulator;
import avrora.sim.State;

/**
 * The <code>BranchCounter</code> class is a profiling probe that can be inserted
 * at a branch instruction to count the number of times the branch is taken and
 * not taken. It demonstrates the ability to inspect the state of the program
 * after the execution of a program. It determines whether the branch was taken
 * by inspecting the program counter of the new state. If the program counter
 * is not equal to the instruction following the branch, then the branch was
 * taken.
 *
 * @see avrora.sim.util.Counter
 * @author Ben L. Titzer
 */
public class BranchCounter implements Simulator.Probe {

    /**
     * This field tracks the number of times the branch is taken. It is
     * incremented in the <code>fireAfter</code> method if the branch
     * was taken.
     */
    public int takenCount;

    /**
     * This field tracks the number of times the branch is not taken. It is
     * incremented in the <code>fireAfter</code> method if the branch
     * was not taken.
     */
    public int nottakenCount;

    /**
     * The <code>fireBefore()</code> method is called before the probed instruction
     * executes. In the implementation of the branch counter, nothing needs to be
     * done before the branch is executed, so this method does nothing.
     *
     * @param i the instruction being probed
     * @param address the address at which this instruction resides
     * @param state the state of the simulation
     */
    public void fireBefore(Instr i, int address, State state) {
        // do nothing.
    }

    /**
     * The <code>fireAfter()</code> method is called after the probed instruction
     * executes. In the implementation of the branch counter, the counter
     * determines whether the branch was taken by inspecting the program counter
     * of the new state. If the program counter is not equal to the instruction
     * following the branch, then the branch was taken.
     *
     * @param i the instruction being probed
     * @param address the address at which this instruction resides
     * @param state the state of the simulation
     */
    public void fireAfter(Instr i, int address, State state) {
        int nextaddr = address + i.getSize();
        if ( state.getPC() == nextaddr ) nottakenCount++;
        else takenCount++;
    }
}
