/**
 * Copyright (c) 2004, Regents of the University of California
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

package avrora.sim;

/**
 * The <code>ClockPrescaler</code> class represents a clock that is
 * another clock scaled appropriately; e.g. 8x slower.
 *
 * @author Ben L. Titzer
 */
public class ClockPrescaler extends Clock {

    protected final Clock driveClock;
    protected final int divider;
    protected long base;
    protected long ticksBeforeBase;

    public ClockPrescaler(String n, Clock drive, int multiplier) {
        super(n, drive.getHZ() / multiplier);
        driveClock = drive;
        this.divider = multiplier;
    }

    public long getCount() {
        return driveClock.getCount() / divider;
    }

    public void insertEvent(Simulator.Event e, long delta) {
        long driverCount = driveClock.getCount() - base;
        long nextTick = ((driverCount / divider)+1)*divider;
        driveClock.insertEvent(e, nextTick - driverCount);
    }

    public void removeEvent(Simulator.Event e) {
        driveClock.removeEvent(e);
    }

    /**
     * The <code>reset()</code> method resets the internal clock prescaler
     * to zero. Thus, the prescaler's previous phase is broken, and the
     * clock signal continues with the same frequency, only that the first
     * tick will happen <code>divider</code> cycles from now.
     */
    public void reset() {
        long newbase = driveClock.getCount();
        long diff = newbase - base;
        ticksBeforeBase += diff / divider;
        base = newbase;
    }

}
