package avrora.sim.util;

import avrora.core.Instr;
import avrora.sim.Simulator;
import avrora.sim.State;

/**
 * The <code>MulticastProbe</code> is a wrapper around multiple probes that
 * allows them to act as a single probe. It is useful for composing multiple
 * probes into one and is used internally in the simulator.
 */
public class MulticastProbe implements Simulator.Probe {
    static class Link {
        Simulator.Probe probe;
        Link next;

        Link(Simulator.Probe p) {
            probe = p;
        }
    }

    Link head;
    Link tail;

    public void add(Simulator.Probe b) {
        if ( b == null ) return;

        if ( head == null ) {
            head = tail = new Link(b);
        } else {
            tail.next = new Link(b);
            tail = tail.next;
        }
    }

    public void remove(Simulator.Probe b) {
        if ( b == null ) return;

        Link prev = null;
        Link pos = head;
        while ( pos != null ) {
            Link next = pos.next;

            if ( pos.probe == b ) {
                // remove the whole thing.
                if ( prev == null ) head = pos.next;
                else prev.next = pos.next;
            } else {
                prev = pos;
            }
            pos = next;
        }
    }

    public boolean isEmpty() {
        return head == null;
    }

    public void fireBefore(Instr i, int address, State state) {
        for ( Link pos = head; pos != null; pos = pos.next )
            pos.probe.fireBefore(i, address, state);
    }

    public void fireAfter(Instr i, int address, State state) {
        for ( Link pos = head; pos != null; pos = pos.next )
            pos.probe.fireAfter(i, address, state);
    }
}
