package avrora.sim.util;

import avrora.core.Instr;
import avrora.sim.Simulator;
import avrora.sim.State;

/**
 * The <code>MulticastProbe</code> is a wrapper around multiple probes that
 * allows them to act as a single probe. It is useful for composing multiple
 * probes into one and is used internally in the simulator.
 *
 * @author Ben L. Titzer
 * @see avrora.sim.Simulator
 */
public class MulticastProbe implements Simulator.Probe {

    /**
     * The <code>FrontierInfo</code> class is used internally to implement the
     * linked list of the probes. It exists because a simple, custom
     * list structure allows for the most efficient dispatching code
     * possible. Performance is critical since the multicast probe
     * may be for every instruction executed in the simulator.
     */
    private static class Link {
        Simulator.Probe probe;
        Link next;

        Link(Simulator.Probe p) {
            probe = p;
        }
    }

    Link head;
    Link tail;

    /**
     * The <code>add()</code> method allows another probe to be inserted into
     * the multicast set. It will be inserted at the end of the list of current
     * probes and will therefore fire after any probes already in the multicast
     * set.
     *
     * @param b the probe to insert
     */
    public void add(Simulator.Probe b) {
        if (b == null) return;

        if (head == null) {
            head = tail = new Link(b);
        } else {
            tail.next = new Link(b);
            tail = tail.next;
        }
    }

    /**
     * The <code>remove</code> method removes a probe from the multicast set.
     * The order of the remaining probes is not changed. The comparison used
     * is reference equality, not the <code>.equals()</code> method.
     *
     * @param b the probe to remove
     */
    public void remove(Simulator.Probe b) {
        if (b == null) return;

        Link prev = null;
        Link pos = head;
        while (pos != null) {
            Link next = pos.next;

            if (pos.probe == b) {
                // remove the whole thing.
                if (prev == null)
                    head = pos.next;
                else
                    prev.next = pos.next;
            } else {
                prev = pos;
            }
            pos = next;
        }
    }

    /**
     * The <code>isEmpty()</code> method tests whether the multicast set
     * of this probe is empty.
     *
     * @return false otherwise
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * The <code>fireBefore()</code> method is called before the probed instruction
     * executes. In the implementation of the multicast probe, it simply calls the
     * <code>fireBefore()</code> method on each of the probes in the multicast set
     * in the order in which they were inserted.
     *
     * @param i       the instruction being probed
     * @param address the address at which this instruction resides
     * @param state   the state of the simulation
     */
    public void fireBefore(Instr i, int address, State state) {
        for (Link pos = head; pos != null; pos = pos.next)
            pos.probe.fireBefore(i, address, state);
    }

    /**
     * The <code>fireAfter()</code> method is called after the probed instruction
     * executes. In the implementation of the multicast probe, it simply calls the
     * <code>fireAfter()</code> method on each of the probes in the multicast set
     * in the order in which they were inserted.
     *
     * @param i       the instruction being probed
     * @param address the address at which this instruction resides
     * @param state   the state of the simulation
     */
    public void fireAfter(Instr i, int address, State state) {
        for (Link pos = head; pos != null; pos = pos.next)
            pos.probe.fireAfter(i, address, state);
    }
}
