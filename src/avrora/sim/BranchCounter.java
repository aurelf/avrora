package avrora.sim;

import avrora.core.Instr;

/**
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
