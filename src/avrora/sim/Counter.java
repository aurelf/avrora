package avrora.sim;

import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.core.Instr;

/**
 * The <code>Counter</code> class is a utility for profiling programs. For
 * example, a counter can be inserted at particular addresses in the program
 * to do runtime profiling of the program as it runs.
 *
 * @author Ben L. Titzer
 */
public class Counter implements Simulator.Probe {
    int count;
    int address;

    public Counter(int a) {
        address = a;
    }

    public void fireBefore(Instr i, int address, State state) {
        count++;
    }

    public void fireAfter(Instr i, int address, State state) {
    }
}
