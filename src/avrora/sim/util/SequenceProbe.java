package avrora.sim.util;

import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.core.Instr;

/**
 * The <code>SequenceProbe</code> is a probe composer that allows a probe to
 * be fired for every instruction executed between a specified entrypoint
 * and a specified exit point. For example, if the entrypoint is a call
 * instruction and the exit point is the instruction following the call
 * instruction, then the probe will fire for every instruction executed
 * between the call and return, including both the call and the instruction
 * following the call. <br><br>
 *
 * This probe supports nested entries (e.g. recursive calls). It is best
 * used on pieces of the program that are single-entry/single-exit such as
 * calls, interrupts, basic blocks, and SSE regions of control flow
 * graphs. It does not work well for loops because of the nesting
 * behavior.
 *
 * @author Ben L. Titzer
 */
public class SequenceProbe implements Simulator.Probe {

    public final int entry_addr;
    public final int exit_addr;
    public final Simulator.Probe probe;

    public int nesting;

    /**
     * The constructor for the <code>SequenceProbe</code> class simply stores
     * its arguments into the corresponding public final fields in this object,
     * leaving the probe in a state where it is ready to be inserted into
     * a simulator.
     * @param p the probe to fire for each instruction when the sequence is entered
     * @param entry the byte address of the entrypoint to the sequence
     * @param exit the byte address of the exitpoint of the sequence
     */
    public SequenceProbe(Simulator.Probe p, int entry, int exit) {
        entry_addr = entry;
        exit_addr = exit;
        probe = p;
    }

    /**
     * The <code>fireBefore()</code> method is called before the probed instruction
     * executes. In the implementation of the sequence probe, if the address is
     * the entrypoint address, then the nesting level is incremented. When
     * the nesting level is greater than one, then the sequence probe will
     * delegate the <code>fireBefore()</code> call to the user probe.
     *
     * @param i the instruction being probed
     * @param address the address at which this instruction resides
     * @param state the state of the simulation
     */
    public void fireBefore(Instr i, int address, State state) {
        if ( address == entry_addr ) nesting++;
        if ( nesting > 0 ) probe.fireBefore(i, address, state);
    }

    /**
     * The <code>fireBefore()</code> method is called before the probed instruction
     * executes. When the nesting level is greater than one, then the sequence probe will
     * delegate the <code>fireAfter()</code> call to the user probe.  If the
     * address is the exit point, then the nesting level is decremented after the
     * call to <code>fireAfter()</code> of the user probe.
     *
     * @param i the instruction being probed
     * @param address the address at which this instruction resides
     * @param state the state of the simulation
     */
    public void fireAfter(Instr i, int address, State state) {
        if ( nesting > 0 ) probe.fireAfter(i, address, state);
        if ( address == exit_addr )
            nesting = (nesting - 1) <= 0 ? 0 : nesting - 1;
    }

    /**
     * The <code>reset()</code> method simply resets the nesting level of the
     * sequence probe, as if it had exited from all nested entries into the
     * region.
     */
    public void reset() {
        nesting = 0;
    }
}
