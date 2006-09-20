/**
 * Copyright (c) 2006, Regents of the University of California
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
 *
 * Creation date: Sep 20, 2006
 */

package avrora.sim.state;

import cck.util.Util;

/**
 * The <code>Register</code> class represents a register of a certain
 * bit width within a simulated CPU or device. A <code>Register</code>
 * object allows instrumentation to be both state access (reads)
 * and state updates (writes). This allows registers to be instrumented
 * by the user for profiling and debugging purposes, and also by
 * device implementations that may sub ranges of registers.
 *
 * </p>
 * This implementation models registers that can be as wide as 32 bits.
 *
 * @author Ben L. Titzer
 */
public class Register {

    protected int value;
    protected final int mask;

    protected NotifyItem notifyHead;
    protected NotifyItem notifyTail;

    /**
     * The constructor for the <code>Register</code> class creates a new register
     * with the specified width in bits.
     * @param w the width of the register in bits
     */
    public Register(int w) {
        mask = -1 << w;
    }

    /**
     * The <code>Notification</code> interface allows clients to add instrumentation
     * to a register. The object implementing the notification is then consulted
     * when reads and writes to the register occur.
     *
     * </p>
     * Notifications are used to implement registers that have subfields (i.e.
     * they contain one or more subregisters with separate roles. A special
     * notification that extracts the appropriate bits and writes them to
     * the subfield accomplishes this.
     */
    public interface Notification {
        public void written(Register r, int oldv, int newv);
        public void read(Register r, int oldv);
    }

    protected static class NotifyItem {
        protected final Notification notify;
        protected NotifyItem next;
        protected NotifyItem(Notification n, NotifyItem nx) {
            notify = n;
            next = nx;
        }
    }

    /**
     * The <code>write()</code> method writes a value to the register. This method
     * will notify any objects that have been added to the notification list.
     * The write is considered to be complete before any notifications occur.
     *
     * @param val the value to write to this register
     */
    public void write(int val) {
        int oldv = value; // cache the old value
        value = val = val & mask; // mask off out-of-range bits
        for ( NotifyItem n = notifyHead; n != null; n = n.next )
            n.notify.written(this, oldv, val);
    }

    /**
     * The <code>read()</code> method reads a value from this register. This method
     * will trigger calls to an objects in the notification list.
     * @return the value in this register
     */
    public int read() {
        int val = value;
        for ( NotifyItem n = notifyHead; n != null; n = n.next )
            n.notify.read(this, val);
        return val;
    }

    /**
     * The <code>set()</code> method sets the value of this register, without triggering
     * the notification of any objects in the notification list. This interface should not
     * be used by client (user) code, but is intended for subfields and devices using
     * subfields.
     * @param val the value to which to set the register
     */
    public void set(int val) {
        value = val & mask;
    }

    /**
     * The <code>get()</code> method retrieves the value from this register, without triggering
     * the notification of any objects in the notification list. This interface should be used
     * by client code (if necessary at all) to avoid recursive triggering of notifications.
     * @return the value of this register
     */
    public int get() {
        return value;
    }

    /**
     * The <code>notifyFirst()</code> method adds a new object implementing the <code>Notification</code>
     * interface to the notification list. This method will always add the object to the beginning
     * of the list, ensuring the notification will occur before any notifications already present
     * in the list. This method does not check for duplicates; a duplicate entry in the list will
     * result in multiple repeated calls to the object notification.
     *
     * @param n the notification to add to this list
     */
    public void notifyFirst(Notification n) {
        if ( notifyHead == null ) {
            notifyHead = notifyTail = new NotifyItem(n, null);
        } else {
            notifyHead = new NotifyItem(n, notifyHead);
        }
    }

    /**
     * The <code>notifyLast()</code> method adds a new object implementing the <code>Notification</code>
     * interface to the notification list. This method will always add the object to the end
     * of the list, ensuring the notification will occur after any notifications already present
     * in the list. This method does not check for duplicates; a duplicate entry in the list will
     * result in multiple repeated calls to the object notification.
     *
     * @param n the notification to add to this list
     */
    public void notifyLast(Notification n) {
        if ( notifyHead == null ) {
            notifyHead = notifyTail = new NotifyItem(n, null);
        } else {
            notifyTail.next = new NotifyItem(n, null);
            notifyTail = notifyTail.next;
        }
    }

    /**
     * The <code>removeNotify()</code> method removes a notification from this list.
     * @param n the notification to remove from this list
     */
    public void removeNotify(Notification n) {
        throw Util.unimplemented();
    }
}
