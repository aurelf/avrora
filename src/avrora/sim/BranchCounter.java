package avrora.sim;

import avrora.core.Instr;

/**
 * The <code>BranchCounter</code> class is a profiling probe that can be inserted
 * at a branch instruction to count the number of times the branch is taken and
 * not taken. It demonstrates the ability to inspect the state of the program
 * after the execution of a program. It determines whether the branch was taken
 * by inspecting the program counter of the new state. If the program counter
 * is not equal to the instruction following the branch, then the branch was
 * taken.
 * @author Ben L. Titzer
 */
public class BranchCounter implements Simulator.Probe {

    public int takenCount;
    public int nottakenCount;

    public void fireBefore(Instr i, int address, State s) {
        // do nothing.
    }

    public void fireAfter(Instr i, int address, State s) {
        int nextaddr = address + i.getSize();
        if ( s.getPC() == nextaddr ) nottakenCount++;
        else takenCount++;
    }
}
