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
import avrora.sim.Simulator;

/**
 * The <code>Medium</code> definition.
 *
 * @author Ben L. Titzer
 */
public class Medium {

    protected static class TXRX {
        public final Medium medium;
        public final Clock clock;
        public final long cyclesPerByte;
        public final long leadCycles;
        public boolean activated;

        protected TXRX(Medium m, Clock c) {
            medium = m;
            clock = c;
            long hz = c.getHZ();
            assert hz > medium.bitsPerSecond;
            cyclesPerByte = (8 * hz / medium.bitsPerSecond);
            leadCycles = (medium.leadBits * hz / medium.bitsPerSecond);
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
        }

        public void beginTransmit(double pow) {
            if ( !activated) {
                transmission = new Transmission(this, pow);
                activated = true;
                clock.insertEvent(new Ticker(), leadCycles);
            }
        }

        public abstract byte nextByte();

        protected class Ticker implements Simulator.Event {
            public void fire() {
                if (activated) {
                    // otherwise, transmit a single byte and add it to the buffer
                    transmission.addByte(nextByte());
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
        protected Receiver(Medium m, Clock c) {
            super(m, c);
        }

        public void beginReceive() {
            activated = true;
        }

        public void endReceive() {
            activated = false;
        }

        public int sample() {
            return 0;
        }

        public abstract void nextByte(byte b);

        protected class Ticker implements Simulator.Event {
            public void fire() {
                // TODO: receiver ticker.
            }
        }
    }

    public static class Transmission {
        public final Transmitter origin;
        public final long start;
        public final long firstBit;
        public final double power;

        protected int counter;
        protected byte[] data;

        protected Transmission(Transmitter o, double pow) {
            origin = o;
            power = pow;
            start = o.clock.getCount();
            long hz = o.clock.getHZ();
            long cyclesPerBit = (hz / o.medium.bitsPerSecond);
            firstBit = (start + o.leadCycles + cyclesPerBit - 1) / cyclesPerBit;
            // TODO: use a less wasteful buffering strategy
            data = new byte[(o.medium.maxLength + 7) / 8];
        }

        public void addByte(byte b) {
            data[counter++] = b;
        }

    }

    public final int bitsPerSecond;
    public final int leadBits;
    public final int minLength;
    public final int maxLength;

    /**
     * The constructor for the <code>Medium</code> class creates a new shared transmission
     * medium with the specified properties, including the bits per second, the lead time
     * before beginning transmission, and the minimum transmission size in bits. These
     * parameters are used to configure the medium and to ensure maximum possible simulation
     * performance.
     *
     * @param bps the bits per second throughput of this medium
     * @param ltb the lead time in bits before beginning a transmission and the first bit
     * @param mintl the minimum transmission length
     * @param maxtl the maximum transmission length
     */
    public Medium(int bps, int ltb, int mintl, int maxtl) {
        bitsPerSecond = bps;
        leadBits = ltb;
        minLength = mintl;
        maxLength = mintl;
    }
}
