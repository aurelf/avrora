/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package avrora.sim.util;

import avrora.sim.FiniteStateMachine;

/**
 * The <code>MulticastProbe</code> is a wrapper around multiple probes that allows them to act as a single
 * probe. It is useful for composing multiple probes into one and is used internally in the simulator.
 *
 * @author Ben L. Titzer
 * @see avrora.sim.FiniteStateMachine
 */
public class MulticastFSMProbe implements FiniteStateMachine.Probe {

    /**
     * The <code>Link</code> class is used internally to implement the linked list of the probes. It exists
     * because a simple, custom list structure allows for the most efficient dispatching code possible.
     * Performance is critical since the multicast probe may be for every instruction executed in the
     * simulator.
     */
    private static class Link {
        final FiniteStateMachine.Probe probe;
        Link next;

        Link(FiniteStateMachine.Probe p) {
            probe = p;
        }
    }

    private Link head;
    private Link tail;

    /**
     * The <code>add()</code> method allows another probe to be inserted into the multicast set. It will be
     * inserted at the end of the list of current probes and will therefore fire after any probes already in
     * the multicast set.
     *
     * @param b the probe to insert
     */
    public void add(FiniteStateMachine.Probe b) {
        if (b == null) return;

        if (head == null) {
            head = tail = new Link(b);
        } else {
            tail.next = new Link(b);
            tail = tail.next;
        }
    }

    /**
     * The <code>remove</code> method removes a probe from the multicast set. The order of the remaining
     * probes is not changed. The comparison used is reference equality, not the <code>.equals()</code>
     * method.
     *
     * @param b the probe to remove
     */
    public void remove(FiniteStateMachine.Probe b) {
        if (b == null) return;

        Link prev = null;
        Link pos = head;
        while (pos != null) {
            Link next = pos.next;

            // matched?
            if (pos.probe == b) {
                // remove the head ?
                if (prev == null) head = pos.next;
                // somewhere in the middle (or at end)
                else prev.next = pos.next;
                // remove the tail item ?
                if (pos == tail) tail = prev;

            } else {
                // no match; continue
                prev = pos;
            }
            pos = next;
        }
    }

    /**
     * The <code>isEmpty()</code> method tests whether the multicast set of this probe is empty.
     *
     * @return false otherwise
     */
    public boolean isEmpty() {
        return head == null;
    }

    public void fireBeforeTransition(int beforeState, int afterState) {
        for (Link pos = head; pos != null; pos = pos.next)
            pos.probe.fireBeforeTransition(beforeState, afterState);
    }

    public void fireAfterTransition(int beforeState, int afterState) {
        for (Link pos = head; pos != null; pos = pos.next)
            pos.probe.fireAfterTransition(beforeState, afterState);
    }
}
