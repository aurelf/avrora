package avrora.sim.util;

import avrora.core.Instr;
import avrora.core.Program;
import avrora.sim.State;

/**
 * The <code>RangeProfiler</code> class implements a probe that can be
 * used to profile a range of addresses in the program. It maintains
 * a simple array of <code>long</code> that stores the count for each
 * instruction in the specified range. It is more space efficient than
 * the <code>ProgramProfiler</code> since it only stores the count for
 * the range specified instead of for the entire program.
 *
 * @see avrora.sim.util.Counter
 * @see avrora.sim.util.ProgramProfiler
 * @author Ben L. Titzer
 */
public class RangeProfiler {
    /**
     * The <code>program</code> field stores a reference to the program
     * being profiled.
     */
    public final Program program;

    /**
     * The <code>low_addr</code> stores the lowest address in the range.
     */
    public final int low_addr;

    /**
     * The <code>high_addr</code> stores the highest address in the range.
     */
    public final int high_addr;

    /**
     * The <code>icount</code> field stores the invocation count
     * for each instruction in the range. It is indexed by byte addresses, with
     * index 0 corresponding to the lowest address in the range (<code>low_addr</code>).
     * at <code>program.getInstr(addr)</code>.
     */
    public final long icount[];

    /**
     * The constructor for the program profiler constructs the required internal
     * state to store the invocation counts of each instruction.
     * @param p the program to profile
     * @param low the low address in the range
     * @param high the high address in the range
     */
    public RangeProfiler(Program p, int low, int high) {
        int size = p.program_end;
        icount = new long[size];
        program = p;
        low_addr = low;
        high_addr = high;
    }

    /**
     * The <code>fireBefore()</code> method is called before the probed instruction
     * executes. In the implementation of the range profiler, it simply increments the
     * count of the instruction at the specified address if that address is in
     * the given range.
     *
     * @param i the instruction being probed
     * @param address the address at which this instruction resides
     * @param state the state of the simulation
     */
    public void fireBefore(Instr i, int address, State state) {
        if ( address < low_addr ) return;
        if ( address >= high_addr ) return;
        icount[address - low_addr]++;
    }

    /**
     * The <code>fireAfter()</code> method is called after the probed instruction
     * executes. In the implementation of the range profiler, it does nothing.
     *
     * @param i the instruction being probed
     * @param address the address at which this instruction resides
     * @param state the state of the simulation
     */
    public void fireAfter(Instr i, int address, State state) {
        // do nothing.
    }
}
