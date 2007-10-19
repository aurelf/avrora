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

import java.util.*;

import cck.util.Arithmetic;

/**
 * The <code>Medium</code> definition.
 *
 * @author Ben L. Titzer
 */
public class Medium {

    private static final int BYTE_SIZE = 8;

    protected static class TXRX {
        public final Medium medium;
        public final Clock clock;
        public final long cyclesPerByte;
        public final long leadCycles;
        public final long cyclesPerBit;

        public boolean activated;

        protected TXRX(Medium m, Clock c) {
            medium = m;
            clock = c;
            long hz = c.getHZ();
            int bps = medium.bitsPerSecond;
            assert hz > bps;
            // NOTE: the roundoff behavior here is important for correct synchronization
            // because we must compute everything on the bit level, the actual throughput
            // of the connection can be slightly off if there is a large roundoff error.
            // TODO: should we round up for XX.5 by adding in bps / 2 ?
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

        protected Transmitter(Medium m, Clock c) {
            super(m, c);
        }

        public void endTransmit() {
            activated = false;
            transmission.end();
        }

        public void beginTransmit(double pow) {
            if ( !activated) {
                transmission = medium.newTransmission(this, pow);
                activated = true;
                clock.insertEvent(new Ticker(), leadCycles);
            }
        }

        public abstract byte nextByte();

        protected class Ticker implements Simulator.Event {
            public void fire() {
                if (activated) {
                    // otherwise, transmit a single byte and add it to the buffer
                    int indx = transmission.counter++;
                    transmission.data[indx] = nextByte();
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
        protected boolean locked;

        protected Receiver(Medium m, Clock c) {
            super(m, c);
        }

        public void beginReceive() {
            activated = true;
            clock.insertEvent(new Ticker(), leadCycles + cyclesPerByte);
        }

        public void endReceive() {
            activated = false;
        }

        public int sample() {
            return 0;
        }

        public abstract void nextByte(byte b);

        protected class Ticker implements Simulator.Event {
            private static final int BIT_DELAY = 1;

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
                long bit = getBitNum(time) - BIT_DELAY;
                waitForNeighbors(time - leadCycles - cyclesPerByte);
                Transmission tx = earliestTransmission(bit);
                if ( tx != null ) {
                    // lock on to the signal.
                    locked = true;
                    long offset = (bit - tx.firstBit) & 7; // offset from delivery of first byte
                    long dbit = bit + BYTE_SIZE + BIT_DELAY - offset; // the delivery time (bit number)
                    // insert even at delivery time of first bit.
                    long ct = getCycleTime(dbit);
                    clock.insertEvent(this, ct - time);
                } else {
                    // remain unlocked.
                    clock.insertEvent(this, leadCycles + cyclesPerByte);
                }
            }

            private void fireLocked(long time) {
                long bit = getBitNum(time) - BIT_DELAY; // there is a one bit delay
                waitForNeighbors(time - cyclesPerByte);
                List it = getIntersection(bit - BYTE_SIZE);
                if ( it != null ) {
                    // merge transmissions into a single byte and send it to receiver
                    nextByte(mergeTransmissions(it, bit - BYTE_SIZE));
                    clock.insertEvent(this, cyclesPerByte);
                } else {
                    // all transmissions are over.
                    locked = false;
                    clock.insertEvent(this, leadCycles + cyclesPerByte);
                }
            }

            private byte mergeTransmissions(List it, long bit) {
                byte value = 0;
                Iterator i = it.iterator();
                while ( i.hasNext() ) {
                    Transmission t = (Transmission)i.next();
                    int offset = (int)(bit - t.firstBit);
                    value |= t.getByteAtOffset(offset);
                }
                return value;
            }

            private Transmission earliestTransmission(long bit) {
                Transmission tx = null;
                synchronized(medium) {
                    Iterator i = medium.transmissions.iterator();
                    while ( i.hasNext() ) {
                        Transmission t = (Transmission)i.next();
                        if (intersect(bit, t)) {
                            if ( tx == null ) tx = t;
                            else if ( t.start < tx.start ) tx = t;
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
    }

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

        public byte getByteAtOffset(int offset) {
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
     * @param bps the bits per second throughput of this medium
     * @param ltb the lead time in bits before beginning a transmission and the first bit
     * @param mintl the minimum transmission length
     * @param maxtl the maximum transmission length
     */
    public Medium(Synchronizer synch, int bps, int ltb, int mintl, int maxtl) {
        this.synch = synch;
        bitsPerSecond = bps;
        leadBits = ltb;
        minLength = mintl;
        maxLength = maxtl;
    }

    public synchronized Transmission newTransmission(Transmitter o, double p) {
        Transmission tx = new Transmission(o, p);
        transmissions.add(tx);
        return tx;
    }
}
