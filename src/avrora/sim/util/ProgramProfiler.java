package avrora.sim.util;

import avrora.core.Program;
import avrora.core.Instr;
import avrora.sim.State;

/**
 * The <code>ProgramProfiler</code> class implements a probe that can be
 * used to profile pieces of the program or the whole program.
 *
 * @author Ben L. Titzer
 */
public class ProgramProfiler {

    public final long icount[];

    ProgramProfiler(Program p) {
        int size = p.program_end;
        icount = new long[size];
    }

    public void fireBefore(Instr i, int address, State s) {
        icount[address]++;
    }

    public void fireAfter(Instr i, int address, State s) {
        // do nothing.
    }
}
