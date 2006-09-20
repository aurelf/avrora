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

import avrora.sim.clock.Clock;
import avrora.sim.Simulator;
import avrora.sim.InterruptTable;
import cck.util.Util;

/**
 * The <code>Forwarder</code> class implements a number of utilities for
 * connecting registers together; for example, a write forwarder, a read
 * forwarder, a set of register subfield forwarders, etc.
 *
 * @author Ben L. Titzer
 */
public class Forwarder {

    public static class Write implements Register.Notification {
        protected final Register register;

        public Write(Register r2) {
            register = r2;
        }

        public void written(Register r, int oldv, int newv) {
            register.write(newv);
        }

        public void read(Register r, int oldv) {
            // do nothing.
        }
    }

    public static class Buffer {
        protected final Register r1;
        protected final Register r2;

        public Buffer(Register a, Register b) {
            r1 = a;
            r2 = b;
        }

        public void flush() {
            r2.write(r1.get());
        }
    }

    /**
     * The <code>TimedBuffer</code> class implements a buffer for writes from one register
     * to another. Each write to the first register will be intercepted and forwarded to
     * the second register after a specified number of ticks of the specified clock.
     * This class implements the notifications and events necessary to accomplish the
     * functionality.
     */
    public static class TimedBuffer implements Register.Notification, Simulator.Event {
        protected final Clock clock;
        protected final Register r1;
        protected final Register r2;
        protected int value;
        protected long delay;

        public TimedBuffer(Clock c, Register a, Register b, long d) {
            clock = c;
            r1 = a;
            r2 = b;
            delay = d;
            r1.notifyLast(this);
        }

        public void written(Register r, int oldv, int newv) {
            value = newv;
            clock.insertEvent(this, delay);
        }

        public void read(Register r, int oldv) {
            // do nothing.
        }

        public void setDelay(long cycles) {
            delay = cycles;
        }

        public void fire() {
            r2.write(value);
        }
    }

    public Register highLowByteRegister(Register r8l, Register r8h) {
        Register r16 = new Register(16);
        new SubRegisterForwarder(r8l, r16, 0xff, 0);
        new SubRegisterForwarder(r8h, r16, 0xff, 8);
        return r16;
    }

    public Register highLowWordRegister(Register r16l, Register r16h) {
        Register r32 = new Register(32);
        new SubRegisterForwarder(r16l, r32, 0xffff, 0);
        new SubRegisterForwarder(r16h, r32, 0xffff, 16);
        return r32;
    }

    /**
     * The <code>SubRegisterForwarder</code> class implements a forwarder that gathers
     * a write from a (smaller) sub-register and forwards it back to the super
     * register. This class only implements one direction, however.
     */
    public static class SubRegisterForwarder implements Register.Notification {
        final Register supreg;
        final int mask;
        final int invmask;
        final int shift;

        public SubRegisterForwarder(Register sub, Register sup, int m, int s) {
            supreg = sup;
            mask = m;
            invmask = ~(mask << s);
            shift = s;
            sub.notifyFirst(this);
        }

        public void written(Register subreg, int oldv, int newv) {
            int cv = supreg.get();
            cv = (cv & invmask) | ((newv & mask) << shift);
            supreg.write(cv);
        }

        public void read(Register r, int oldv) {
            // do nothing.
        }
    }

    public static class SuperRegisterForwarder implements Register.Notification {
        final Register subreg;
        final int mask;
        final int shift;

        public SuperRegisterForwarder(Register sup, Register sub, int m, int s) {
            subreg = sub;
            mask = m;
            shift = s;
            sup.notifyFirst(this);
        }

        public void written(Register subreg, int oldv, int newv) {
            int cv = (newv >> shift) & mask;
            subreg.write(cv);
        }

        public void read(Register r, int oldv) {
            // do nothing.
        }
    }
}
