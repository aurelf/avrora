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
 * Created Oct 11, 2007
 */
package avrora.sim.radio;

import avrora.sim.clock.Clock;
import avrora.sim.clock.Synchronizer;
import avrora.sim.Simulator;
import avrora.sim.util.TransactionalList;

import java.util.*;

import cck.util.Arithmetic;

/**
 * The <code>Medium</code> definition.
 *
 * @author Ben L. Titzer
 */
public class Medium {

    private static final int BYTE_SIZE = 8;

    public interface Arbitrator {
        public boolean lockTransmission(Receiver receiver, Transmission tran);
        public char mergeTransmissions(Receiver receiver, List trans, long bit);
    }

    public interface Probe {
        public void fireBeforeTransmit(Transmitter t, byte val);
        public void fireBeforeTransmitEnd(Transmitter t);

        public void fireAfterReceive(Receiver r, char val);
        public void fireAfterReceiveEnd(Receiver r);

        public class Empty implements Probe {
            public void fireBeforeTransmit(Transmitter t, byte val) {}
            public void fireBeforeTransmitEnd(Transmitter t) {}

            public void fireAfterReceive(Receiver r, char val) {}
            public void fireAfterReceiveEnd(Receiver r) {}
        }

        public class List extends TransactionalList implements Probe {
            public void fireBeforeTransmit(Transmitter t, byte val) {
                beginTransaction();
                for (Link pos = head; pos != null; pos = pos.next)
                    ((Probe)pos.object).fireBeforeTransmit(t, val);
                endTransaction();
            }
            public void fireBeforeTransmitEnd(Transmitter t) {
                beginTransaction();
                for (Link pos = head; pos != null; pos = pos.next)
                    ((Probe)pos.object).fireBeforeTransmitEnd(t);
                endTransaction();
            }
            public void fireAfterReceive(Receiver r, char val) {
                beginTransaction();
                for (Link pos = head; pos != null; pos = pos.next)
                    ((Probe)pos.object).fireAfterReceive(r, val);
                endTransaction();
            }
            public void fireAfterReceiveEnd(Receiver r) {
                beginTransaction();
                for (Link pos = head; pos != null; pos = pos.next)
                    ((Probe)pos.object).fireAfterReceiveEnd(r);
                endTransaction();
            }
        }
     }

    protected static class TXRX {
        public final Medium medium;
        public final Clock clock;
        public final long cyclesPerByte;
        public final long leadCycles;
        public final long cyclesPerBit;
        protected Probe.List probeList;

        public boolean activated;

        protected TXRX(Medium m, Clock c) {
            medium = m;
            clock = c;
            long hz = c.getHZ();
            int bps = medium.bitsPerSecond;
            assert hz > bps;
            cyclesPerBit = (hz / bps);
            cyclesPerByte = BYTE_SIZE * cyclesPerBit;
            leadCycles = (medium.leadBits * hz / bps);
        }

        protected long getBitNum(long time) {
            return time  / cyclesPerBit;
        }

        protected long getCycleTime(long bit) {
            return bit * cyclesPerBit;
        }

        public void insertProbe(Medium.Probe probe) {
            if (this.probeList == null) this.probeList = new Probe.List();
            this.probeList.add(probe);
        }

        public void removeProbe(Medium.Probe probe) {
            if (this.probeList != null) this.probeList.remove(probe);
        }
    }

    /**
     * The <code>Medium.Transmitter</code> class represents an object that is capable of
     * making transmissions into the medium. When activated, it begins transmitting bytes
     * into the medium after the lead time. Internally, this class implements its own
     * clock-level synchronization so that clients only have to implement the
     * <code>nextByte()</code> routine.
     */
    public static abstract class Transmitter extends TXRX {

        protected Transmission transmission;
        protected final Transmitter.Ticker ticker;
        protected boolean shutdown;

        protected Transmitter(Medium m, Clock c) {
            super(m, c);
            ticker = new Ticker();
        }

        public final void endTransmit() {
            if (activated) {
                shutdown = true;
                transmission.end();
            }
        }

        public final void beginTransmit(double pow) {
            if ( !activated) {
                transmission = medium.newTransmission(this, pow);
                activated = true;
                clock.insertEvent(ticker, leadCycles);
            }
        }

        public abstract byte nextByte();

        protected class Ticker implements Simulator.Event {
            public void fire() {
                if (shutdown) {
                    // shut down the transmitter
                    if (probeList != null) probeList.fireBeforeTransmitEnd(Transmitter.this);
                    transmission = null;
                    shutdown = false;
                    activated = false;
                } else if (activated) {
                    // otherwise, transmit a single byte and add it to the buffer
                    int indx = transmission.counter++;
                    byte val = nextByte();
                    transmission.data[indx] = val;
                    if (probeList != null) probeList.fireBeforeTransmit(Transmitter.this, val);
                    clock.insertEvent(this, cyclesPerByte);
                }
            }
        }
    }

    /**
     * The <code>Receiver</code> class represents an object that can receive transmissions
     * from the medium. When activated, it listens for transmissions synchronously using
     * its own clock-level synchronization. It receives transmissions that may be the
     * result of multiple interfering transmissions.
     */
    public static abstract class Receiver extends TXRX {
        private static final int BIT_DELAY = 1;
        protected boolean locked;
        public Receiver.Ticker ticker;

        protected Receiver(Medium m, Clock c) {
            super(m, c);
            ticker = new Ticker();
        }

        public final void beginReceive() {
            activated = true;
            clock.insertEvent(ticker, leadCycles + cyclesPerByte);
        }

        public final void endReceive() {
            activated = false;
            locked = false;
            clock.removeEvent(ticker);
        }

        public abstract void nextByte(boolean lock, byte b);

        protected class Ticker implements Simulator.Event {

            public void fire() {
                if (activated) {
                    if ( locked ) {
                        // if receiver is locked onto some transmission, wait for neighbors' byte(s)
                        fireLocked(clock.getCount());
                    } else {
                        // if receiver is not locked, determine whether a lock will occur this interval
                        fireUnlocked(clock.getCount());
                    }
                }
            }

            private void fireUnlocked(long time) {
                long oneBitBeforeNow = getBitNum(time) - BIT_DELAY;
                waitForNeighbors(time - leadCycles - cyclesPerByte);
                Transmission tx = earliestNewTransmission(oneBitBeforeNow - BYTE_SIZE);
                if ( tx != null ) {
                    // there is a new transmission; calculate delivery of first byte.
                    long dcycle = getCycleTime(tx.firstBit + BYTE_SIZE + BIT_DELAY);
                    long delta = dcycle - time;
                    //assert dcycle >= time;
                    if (delta <= 0) {
                        // lock on and deliver the first byte right now.
                        locked = true;
                        deliverByte(oneBitBeforeNow);
                        return;
                    } else if (delta < leadCycles) {
                        // lock on and insert even at delivery time of first bit.
                        locked = true;
                        clock.insertEvent(this, delta);
                        return;
                    } else if (delta < leadCycles + cyclesPerByte) {
                        // don't lock on yet, but wait for delivery time
                        clock.insertEvent(this, delta);
                        return;
                    }
                }
                // remain unlocked.
                clock.insertEvent(this, leadCycles + cyclesPerByte);
            }

            private void fireLocked(long time) {
                long oneBitBeforeNow = getBitNum(time) - BIT_DELAY; // there is a one bit delay
                waitForNeighbors(time - cyclesPerByte);
                deliverByte(oneBitBeforeNow);
            }

            private void deliverByte(long oneBitBeforeNow) {
                List it = getIntersection(oneBitBeforeNow - BYTE_SIZE);
                if ( it != null ) {
                    // merge transmissions into a single byte and send it to receiver
                    char val = medium.arbitrator.mergeTransmissions(Receiver.this, it, oneBitBeforeNow - BYTE_SIZE);
                    nextByte(true, (byte)val);
                    if (probeList != null) probeList.fireAfterReceive(Receiver.this, val);
                    clock.insertEvent(this, cyclesPerByte);
                } else {
                    // all transmissions are over.
                    locked = false;
                    nextByte(false, (byte)0);
                    if (probeList != null) probeList.fireAfterReceiveEnd(Receiver.this);
                    clock.insertEvent(this, leadCycles + cyclesPerByte);
                }
            }

        }
        
        public final boolean isChannelClear() {
            if (!activated) {
                long time = clock.getCount();
                long bit = getBitNum(time) - BIT_DELAY; // there is a one bit delay
                waitForNeighbors(time - cyclesPerByte);
                List it = getIntersection(bit - BYTE_SIZE);
                return it != null;
            } else {
                return !locked;
            }
        }

        private Transmission earliestNewTransmission(long bit) {
            Transmission tx = null;
            synchronized(medium) {
                Iterator i = medium.transmissions.iterator();
                while ( i.hasNext() ) {
                    Transmission t = (Transmission)i.next();
                    if (bit <= t.firstBit && medium.arbitrator.lockTransmission(Receiver.this, t)) {
                        if ( tx == null ) tx = t;
                        else if ( t.firstBit < tx.firstBit ) tx = t;
                    } else if (bit - 8 - 2 * medium.leadBits > t.lastBit) {
                        // remove older transmissions
                        i.remove();
                    }
                }
            }
            return tx;
        }

        private List getIntersection(long bit) {
            List it = null;
            synchronized(medium) {
                Iterator i = medium.transmissions.iterator();
                while ( i.hasNext() ) {
                    Transmission t = (Transmission)i.next();
                    if (intersect(bit, t)) {
                        if ( it == null ) it = new LinkedList();
                        it.add(t);
                    }
                }
            }
            return it;
        }

        private boolean intersect(long bit, Transmission t) {
            return bit >= t.firstBit && bit < t.lastBit;
        }

        private void waitForNeighbors(long gtime) {
            if ( medium.synch != null ) medium.synch.waitForNeighbors(gtime);
        }
    }

    public static class BasicArbitrator implements Arbitrator {
        public boolean lockTransmission(Receiver receiver, Transmission trans) {
            return true;
        }
        public char mergeTransmissions(Receiver receiver, List it, long bit) {
            assert it.size() > 0;
            Iterator i = it.iterator();
            Transmission first = (Transmission)i.next();
            int value = 0xff & first.getByteAtTime(bit);
            while ( i.hasNext() ) {
                Transmission next = (Transmission)i.next();
                int nval = 0xff & next.getByteAtTime(bit);
                value |= (nval << 8) ^ (value << 8); // compute corrupted bits
                value |= nval;
            }
            return (char)value;
        }
    }

    /**
     * The {@code Transmission} class represents a transmission originating from
     * a particular {@code Transmitter} to this medium. A transmission consists
     * of a sequences of bytes sent one after another into the medium. Each transmission
     * has a start time and a power level.
     */
    public class Transmission {
        public final Transmitter origin;
        public final long start;
        public final long firstBit;
        public final double power;
        public long lastBit;
        public long end;

        protected int counter;
        protected byte[] data;

        protected Transmission(Transmitter o, double pow) {
            origin = o;
            power = pow;
            start = o.clock.getCount();
            end = Long.MAX_VALUE;
            long l = start + o.leadCycles;
            firstBit = origin.getBitNum(l);
            lastBit = Long.MAX_VALUE;
            data = new byte[Arithmetic.roundup(o.medium.maxLength, BYTE_SIZE)];
        }

        public void end() {
            end = origin.clock.getCount();
            lastBit = firstBit + counter * BYTE_SIZE;
        }

        public byte getByteAtTime(long bit) {
            assert bit >= firstBit;
            int offset = (int) (bit - firstBit);
            int shift = offset & 0x7;
            int indx = offset / BYTE_SIZE;
            int hi = 0xff & data[indx] << shift;
            if ( shift > 0 ) {
                int low = 0xff & data[1 + indx];
                return (byte)(hi | low >> (BYTE_SIZE - shift));
            }
            return (byte)hi;
        }
    }

    public final Synchronizer synch;
    public final Arbitrator arbitrator;

    public final int bitsPerSecond;
    public final int leadBits;
    public final int minLength;
    public final int maxLength;

    protected List transmissions = new LinkedList();

    /**
     * The constructor for the <code>Medium</code> class creates a new shared transmission
     * medium with the specified properties, including the bits per second, the lead time
     * before beginning transmission, and the minimum transmission size in bits. These
     * parameters are used to configure the medium and to ensure maximum possible simulation
     * performance.
     *
     * @param synch the synchronizer used to synchronize concurrent senders and receivers
     * @param arb the arbitrator that determines how to merge received transmissions
     * @param bps the bits per second throughput of this medium
     * @param ltb the lead time in bits before beginning a transmission and the first bit
     * @param mintl the minimum transmission length
     * @param maxtl the maximum transmission length
     */
    public Medium(Synchronizer synch, Arbitrator arb, int bps, int ltb, int mintl, int maxtl) {
        this.synch = synch;
        bitsPerSecond = bps;
        leadBits = ltb;
        minLength = mintl;
        maxLength = maxtl;
        if (arb == null)
            arbitrator = new BasicArbitrator();
        else
            arbitrator = arb;
    }

    protected synchronized Transmission newTransmission(Transmitter o, double p) {
        Transmission tx = new Transmission(o, p);
        transmissions.add(tx);
        return tx;
    }

    public static boolean isCorruptedByte(char c) {
        return (c & 0xff00) != 0;
    }

    public static byte getCorruptedBits(char c) {
        return (byte)(c >> 8);
    }

    public static byte getTransmittedBits(char c) {
        return (byte)c;
    }
}
