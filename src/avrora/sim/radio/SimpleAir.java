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

package avrora.sim.radio;

import avrora.sim.mcu.Microcontroller;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.util.GlobalClock;
import avrora.Avrora;
import avrora.util.Verbose;

import java.util.*;

/**
 * Very simple implementation of radio air. It assumes a lossless environment where all
 * radios are able to communicate with each other. This simple air is blind to the frequencies
 * used in transmission (i.e. it assumes that all frequencies are really the same).
 *
 * This class should provide the proper scheduling policy with respect to threads that
 * more complicated radio implementations can use the time scheduling policy
 * and only overload the delivery policy.
 *
 * @author Daniel Lee
 */
public class SimpleAir implements RadioAir {

    public final static SimpleAir simpleAir;

    private final Verbose.Printer rssiPrinter = Verbose.getVerbosePrinter("sim.rssi");

    static {
        simpleAir = new SimpleAir();
    }

    /** GlobalClock used by the air environment. Essential for synchronization. */
    protected final RadioClock globalQueue;

    protected Radio.RadioPacket firstPacket;
    protected LinkedList packetsThisPeriod;
    protected LinkedList packetsLeftOver;

    protected final HashSet radios;

    private long lastDeliveryTime;
    private long nextDeliveryTime;

    private final DeliveryMeet deliveryMeet;

    /** State for maintaining RSSI wait for neighbors */
    int rssi_count;
    int meet_count;
    TreeSet rssi_waiters;

    /** The amount of cycles it takes for one byte to be sent.*/
    public final int bytePeriod = Radio.TRANSFER_TIME;
    public final int bitPeriod = Radio.TRANSFER_TIME / 8;
    public final int bitPeriod2 = Radio.TRANSFER_TIME / 16;

    public SimpleAir() {
        radios = new HashSet();
        packetsThisPeriod = new LinkedList();
        packetsLeftOver = new LinkedList();
        globalQueue = new RadioClock(bytePeriod);
        deliveryMeet = new DeliveryMeet();
        rssi_waiters = new TreeSet();
    }

    public synchronized void addRadio(Radio r) {
        radios.add(r);
        globalQueue.add(r.getSimulatorThread());
        deliveryMeet.setGoal(globalQueue.getNumberOfThreads());
    }

    public synchronized void removeRadio(Radio r) {
        radios.remove(r);
    }

    public synchronized void transmit(Radio r, Radio.RadioPacket f) {
        packetsThisPeriod.addLast(f);

        if ( firstPacket == null ) {
            firstPacket = f;
        } else if ( f.originTime < firstPacket.originTime ) {
            firstPacket = f;
        }
    }

    private class RSSIWait implements Comparable {
        boolean shouldWait;
        final long sampleTime;
        final Simulator simulator;

        RSSIWait(Simulator s, long st) {
            simulator = s;
            sampleTime = st;
            shouldWait = true;
        }

        public int compareTo(Object o) {
            if ( o == this ) return 0;
            RSSIWait w = (RSSIWait)o;
            int diff = (int)(w.sampleTime - this.sampleTime);
            if ( diff == 0 ) return w.hashCode() - this.hashCode();
            return diff;
        }
    }

    public int sampleRSSI(Radio r) {
        Simulator s = r.getSimulator();
        long t = s.getState().getCycles();
        RSSIWait w = new RSSIWait(s, t);
        RSSIWait f = null;

        synchronized ( this ) {
            if ( rssiPrinter.enabled ) {
                rssiPrinter.println("RSSI: sampleRSSI @ "+t);
            }
            // add use to the waiters
            rssi_waiters.add(w);
            rssi_count++;
            // check to see if everyone is ready to go
            f = checkRSSIWaiters(w);
        }

        // are we at the head of the RSSI waiter's list, and everyone is ready to go?
        if ( f != w ) {
            // we are not at the head of the RSSI waiters list, wait until we are notified
            try {
                synchronized ( w ) {
                    if ( rssiPrinter.enabled ) {
                        rssiPrinter.println("RSSI: wait @ "+t+" "+w);
                    }
                    // check that no thread got to us in between giving up the global lock
                    if ( w.shouldWait ) w.wait();
                }
            } catch ( InterruptedException e ) {
                throw new GlobalClock.InterruptedException(e);
            }
        }

        int strength = 0x3ff;

        // we just got woken up (or returned immediately from checkRSSIWaiters
        // this means we are now ready to proceed and compute our RSSI value.
        synchronized ( this ) {
            Iterator i = packetsLeftOver.iterator();
            while ( i.hasNext() ) {
                Radio.RadioPacket p = (Radio.RadioPacket)i.next();
                if ( p.deliveryTime > t && p.originTime < t ) strength = 0;
            }
            i = packetsThisPeriod.iterator();
            while ( i.hasNext() ) {
                Radio.RadioPacket p = (Radio.RadioPacket)i.next();
                if ( p.deliveryTime > t && p.originTime < t ) strength = 0;
            }
        }

        return strength;
    }

    private synchronized void incrementMeets() {
        meet_count++;
        // wake up the first RSSI waiter (if there are any)
        checkRSSIWaiters(null);
    }

    /**
     * Tricky! This method checks whether every thread has reached either a local meet
     * or has tried to read the RSSI value. If every thread has joined in one of these
     * two ways this method will select the RSSI waiter that made the earliest request.
     * If that waiter is NOT the waiter passed as a parameter, it will wake that waiter.
     * If that waiter IS the waiter passed, it will NOT wake it.
     * @param curWait the current waiter, null if this method is being called as a result
     * of a thread entering a local meet
     * @return the waiter at the head of the line if all threads have joined, null otherwise
     */
    private RSSIWait checkRSSIWaiters(RSSIWait curWait) {
        if ( rssiPrinter.enabled )  rssiPrinter.print("RSSI check("+curWait+"): met "+meet_count+", sampling "+rssi_count+": ");

        if ( meet_count + rssi_count == globalQueue.getNumberOfThreads() ) {

            // are there any waiters?
            if ( rssi_waiters.isEmpty() ) {
                if ( rssiPrinter.enabled ) rssiPrinter.println("no waiters");
                return null;
            }

            // signal first thread to continue
            RSSIWait w = (RSSIWait)rssi_waiters.first();
            rssi_count--;
            rssi_waiters.remove(w);

            if ( w != curWait ) {
                synchronized(w) {
                    if ( rssiPrinter.enabled ) rssiPrinter.println("notify "+w);
                    w.shouldWait = false;
                    w.notifyAll();
                }
            }
            return w;

        } else {
            // not everyone has joined
            if ( rssiPrinter.enabled ) rssiPrinter.println("not ready");
            return null;
        }
    }

    /**
     * The <code>RadioTicker</code> class is the global timer for the radio. It is
     * specialized in that it will schedule delivery meets when, at the end of
     * the interval, there was at least one packet sent.
     */
    protected class RadioTicker extends GlobalClock.Ticker {
        long deliveryDelta;

        RadioTicker(long p) {
            super(p);
        }

        public void preSynchAction() {
            incrementMeets();
        }

        public void serialAction() {
            eventQueue.advance(1);
            if ( firstPacket != null ) {
                deliveryDelta = computeNextDelivery();
                firstPacket = null;
            } else {
                deliveryDelta = 0;
            }
            meet_count = 0;
        }

        protected long computeNextDelivery() {
            long globalTime = globalQueue.globalTime();
            long deliveryGoal;

            deliveryGoal = firstPacket.deliveryTime;

            if ( lastDeliveryTime > globalTime - bytePeriod ) {
                // there was a byte delivered in the period just finished
                if ( firstPacket.deliveryTime <= lastDeliveryTime + bytePeriod ) {
                    // the firstPacket packet in this interval overlapped a bit with the last delivery
                    deliveryGoal = lastDeliveryTime + bytePeriod;
                }
            }

            // add any packets from the previous period that have a delivery
            // point after the last delivery
            Iterator i = packetsLeftOver.iterator();
            while ( i.hasNext() ) {
                Radio.RadioPacket p = (Radio.RadioPacket)i.next();
                if ( p.deliveryTime > lastDeliveryTime )
                    packetsThisPeriod.addLast(p);
            }

            // just finished this period, so create a new set for the next period
            packetsLeftOver = packetsThisPeriod;
            packetsThisPeriod = new LinkedList();
            nextDeliveryTime = deliveryGoal;
            return deliveryGoal - globalTime;
        }

        public void parallelAction(SimulatorThread t) {
            Simulator simulator = t.getSimulator();
            simulator.insertEvent(this, period);
            if ( deliveryDelta > 0 )
                simulator.insertEvent(deliveryMeet, deliveryDelta);

        }
    }

    protected class DeliveryMeet extends GlobalClock.LocalMeet {

        protected Radio.RadioPacket computedPacket;

        DeliveryMeet() {
            super("DELIVERY");
        }

        public void preSynchAction() {
            incrementMeets();
        }

        public void serialAction() {
            computeWaveForm();
            meet_count = 0;
        }

        protected void computeWaveForm() {

            long currentTime = nextDeliveryTime;

            int data = 0;
            int bitsFront = 0, bitsEnd = 0;

            // iterate over all of the packets in the past two intervals
            Iterator pastIterator = packetsLeftOver.iterator();
            Iterator curIterator = packetsThisPeriod.iterator();
            while ( true ) {
                Radio.RadioPacket packet;
                if ( pastIterator.hasNext() )
                    packet = (Radio.RadioPacket)pastIterator.next();
                else if ( curIterator.hasNext() )
                    packet = (Radio.RadioPacket)curIterator.next();
                else break;

                // NOTE: this calculation assumes MSB first
                if ( currentTime == packet.deliveryTime ) {
                    // exact match!
                    data |= packet.data;
                    bitsFront = 8;
                    bitsEnd = 8;
                } else if ( currentTime < packet.deliveryTime ) {
                    // end of the packet is sliced off
                    int bitdiffx2 = ((int)(packet.deliveryTime - currentTime)) / bitPeriod2;
                    int bitdiff = (bitdiffx2 + 1) / 2;
                    int bitsE = 8 - bitdiff;
                    if ( bitsE > bitsEnd ) bitsEnd = bitsE;
                    data |= packet.data >>> bitdiff;
                } else {
                    // front of packet is sliced off
                    int bitdiffx2 = ((int)(currentTime - packet.deliveryTime)) / bitPeriod2;
                    int bitdiff = (bitdiffx2 + 1) / 2;
                    int bitsF = 8 - bitdiff;
                    if ( bitsF > bitsFront ) bitsFront = bitsF;
                    data |= packet.data << bitdiff;
                }
            }


            // finish building the radio packet
            if ( bitsFront + bitsEnd >= 8) {
                // enough bits were collected to deliver a packet
                computedPacket = new Radio.RadioPacket((byte)data, 0, nextDeliveryTime - bytePeriod);
                lastDeliveryTime = nextDeliveryTime;
            } else {
                // not enough bits were collected to deliver a packet
                computedPacket = null;
            }


        }

        protected void setGoal(int goal) {
            this.goal = goal;
        }

        public void parallelAction(SimulatorThread s) {
            if ( computedPacket != null) {
                // if there was a complete radio packet assembled
                Radio radio = s.getSimulator().getMicrocontroller().getRadio();
                radio.receive(computedPacket);
            }
        }
    }
    /**
     * An extended version of <code>GlobalClock</code> that implements a version of
     * <code>LocalMeet</code> that is appropriate for delivering radio packets.
     */
    protected class RadioClock extends GlobalClock {
        protected RadioClock(long p) {
            super(p, new RadioTicker(p));
        }

    }


}
