/**
 * Copyright (c) 2007, Regents of the University of California
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
 * Created Oct 20, 2007
 */
package avrora.sim.output;

import avrora.sim.Simulator;

import java.util.List;
import java.util.ArrayList;

/**
 * The <code>EventBuffer</code> definition.
 *
 * @author Ben L. Titzer
 */
public class EventBuffer {
    public static final DiscardPolicy DISCARD = new DiscardPolicy();
    public static final WrapAroundPolicy WRAPAROUND = new WrapAroundPolicy();
    public static final GrowPolicy GROW = new GrowPolicy();

    public class Event {
        protected long time;
        protected long param;
        protected Object object;

        protected Simulator getSimulator() {
            return sim;
        }
    }

    public interface OverflowPolicy {
        public void overflow(EventBuffer buf);
    }

    protected static class DiscardPolicy implements OverflowPolicy {
        public void overflow(EventBuffer buf) {
            buf.flush();
        }
    }

    protected static class WrapAroundPolicy implements OverflowPolicy {
        public void overflow(EventBuffer buf) {
            buf.index = 0;
        }
    }

    protected static class GrowPolicy implements OverflowPolicy {
        public void overflow(EventBuffer buf) {
            EventBuffer.Event[] old = buf.buffer;
            buf.buffer = new Event[old.length * 2];
            System.arraycopy(buf.buffer, 0, old, 0, old.length);
        }
    }

    public final Simulator sim;
    protected final OverflowPolicy policy;
    protected Event[] buffer;
    protected int index;

    public EventBuffer(Simulator s, int length, OverflowPolicy p) {
        buffer = new Event[length];
        sim = s;
        policy = p;
    }

    protected Event recordEvent(Object o, long param) {
        if (index >= buffer.length) {
            policy.overflow(this);
        }
        Event e = buffer[index];
        if (e == null) {
            buffer[index] = e = new Event();
        }
        index++;
        e.time = sim.getClock().getCount();
        e.object = o;
        e.param = param;
        return e;
    }

    public void flush() {
        for (int i = 0; i < index; i++) {
            EventBuffer.Event e = buffer[i];
            e.time = 0;
            e.object = null;
        }
        index = 0;
    }

    public List toList() {
        ArrayList list = new ArrayList(index);
        for (int i = 0; i < index; i++) list.add(buffer[i]);
        return list;
    }

    public List extractList() {
        List list = toList();
        flush();
        return list;
    }
}
