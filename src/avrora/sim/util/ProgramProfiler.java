package avrora.sim.util;

import avrora.core.Program;
import avrora.core.Instr;
import avrora.sim.State;
import avrora.sim.Simulator;

/**
 * The <code>ProgramProfiler</code> class implements a probe that can be
 * used to profile pieces of the program or the whole program. It maintains
 * a simple array of <code>long</code> that stores the count for every
 * instruction
 *
 * @ see avrora.sim.util.Counter
 * @author Ben L. Titzer
 */
public class ProgramProfiler implements Simulator.Probe {

    /**
     * The <code>program</code> field stores a reference to the program
     * being profiled.
     */
    public final Program program;

    /**
     * The <code>icount</code> field stores the invocation count
     * for every instruction in the program. It is indexed by byte addresses.
     * Thus <code>icount[addr]</code> corresponds to the invocation for the instruction
     * at <code>program.readInstr(addr)</code>.
     */
    public final long icount[];

    /**
     * The constructor for the program profiler constructs the required internal
     * state to store the invocation counts of each instruction.
     * @param p the program to profile
     */
    public ProgramProfiler(Program p) {
        int size = p.program_end;
        icount = new long[size];
        program = p;
    }

    /**
     * The <code>fireBefore()</code> method is called before the probed instruction
     * executes. In the implementation of the program profiler, it simply increments the
     * count of the instruction at the specified address.
     *
     * @param i the instruction being probed
     * @param address the address at which this instruction resides
     * @param state the state of the simulation
     */
    public void fireBefore(Instr i, int address, State state) {
        icount[address]++;
    }

    /**
     * The <code>fireAfter()</code> method is called after the probed instruction
     * executes. In the implementation of the profiler, it does nothing.
     *
     * @param i the instruction being probed
     * @param address the address at which this instruction resides
     * @param state the state of the simulation
     */
    public void fireAfter(Instr i, int address, State state) {
        // do nothing.
    }
}
