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

package avrora.sim.radio;

import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.clock.GlobalClock;
import avrora.sim.clock.IntervalSynchronizer;
import avrora.util.Verbose;
import avrora.Avrora;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Very simple implementation of radio air. It assumes a lossless environment where all radios are able to
 * communicate with each other. This simple air is blind to the frequencies used in transmission (i.e. it
 * assumes that all frequencies are really the same).
 * <p/>
 * This class should provide the proper scheduling policy with respect to threads that more complicated radio
 * implementations can use the time scheduling policy and only overload the delivery policy.
 *
 * @author Daniel Lee
 * @author Ben L. Titzer
 */
public class BroadcastAir implements RadioAir {

    protected Radio.Transmission firstTransmission;
    protected Radio.Transmission computedTransmission;

    protected LinkedList packetsThisPeriod;
    protected LinkedList packetsLeftOver;

    protected final HashSet radios;

    // state for maintaining meeting of threads for delivery
    private long lastDeliveryTime;
    private long nextDeliveryTime;

    protected final IntervalSynchronizer synchronizer;
    DeliveryEvent deliveryEvent;

    /**
     * The amount of cycles it takes for one byte to be sent.
     */
    public static final int bytePeriod = Radio.TRANSFER_TIME;
    public static final int bitPeriod = Radio.TRANSFER_TIME / 8;
    public static final int bitPeriod2 = Radio.TRANSFER_TIME / 16;
    public static final int sampleTime = 13 * 64;
    private static final int TRANSFER_TIME = Radio.TRANSFER_TIME;

    public BroadcastAir() {
        radios = new HashSet();
        packetsThisPeriod = new LinkedList();
        packetsLeftOver = new LinkedList();
        synchronizer = new IntervalSynchronizer(TRANSFER_TIME, new MeetEvent());
        deliveryEvent = new DeliveryEvent();
    }

    /**
     * The <code>addRadio()</code> method adds a new radio to this radio model.
     * @param r the radio to add to this air implementation
     */
    public synchronized void addRadio(Radio r) {
        radios.add(r);
        r.setAir(this);
        synchronizer.addNode(r.getSimulatorThread());
    }

    /**
     * The <code>removeRadio()</code> method removes a radio from this radio model.
     * @param r the radio to remove from this air implementation
     */
    public synchronized void removeRadio(Radio r) {
        radios.remove(r);
        synchronizer.removeNode(r.getSimulatorThread());
    }

    /**
     * The <code>transmit()</code> method is called by a radio when it begins to transmit
     * a packet over the air. The radio packet should be delivered to those radios in
     * range which are listening, according to the radio model.
     * @param r the radio transmitting this packet
     * @param f the radio packet transmitted into the air
     */
    public synchronized void transmit(Radio r, Radio.Transmission f) {
        packetsThisPeriod.addLast(f);

        if (firstTransmission == null) {
            firstTransmission = f;
        } else if (f.originTime < firstTransmission.originTime) {
            firstTransmission = f;
        }
    }

    protected class MeetEvent implements Simulator.Event {
        long meets;

        public void fire() {
            meets++;
            if (firstTransmission != null) {
                long globalTime = meets * TRANSFER_TIME;
                long deliveryGoal;

                deliveryGoal = firstTransmission.deliveryTime;

                if (lastDeliveryTime > globalTime - bytePeriod) {
                    // there was a byte delivered in the period just finished
                    if (firstTransmission.deliveryTime <= lastDeliveryTime + bytePeriod) {
                        // the firstPacket packet in this interval overlapped a bit with the last delivery
                        deliveryGoal = lastDeliveryTime + bytePeriod;
                    }
                }

                // add any packets from the previous period that have a delivery
                // point after the last delivery (i.e. they have bits left over at the end)
                Iterator i = packetsLeftOver.iterator();
                while (i.hasNext()) {
                    Radio.Transmission p = (Radio.Transmission)i.next();
                    if (p.deliveryTime > lastDeliveryTime)
                        packetsThisPeriod.addLast(p);
                }

                // just finished this period, so create a new set for the next period
                packetsLeftOver = packetsThisPeriod;
                packetsThisPeriod = new LinkedList();
                nextDeliveryTime = deliveryGoal;
                long deliveryDelta = deliveryGoal - globalTime;

                scheduleDeliveries(deliveryDelta);
            }
            firstTransmission = null;
            computedTransmission = null;
        }

        private void scheduleDeliveries(long deliveryDelta) {
            Iterator ri = radios.iterator();
            while ( ri.hasNext() ) {
                Radio r = (Radio)ri.next();
                if ( r.isListening() )
                    r.getSimulator().insertEvent(deliveryEvent, deliveryDelta);
            }
        }
    }

    /**
     * The <code>sampleRSSI()</code> method is called by a radio when it wants to
     * sample the RSSI value of the air around it at the current time. The air may
     * need to block (i.e. wait for neighbors) because this thread may be ahead
     * of other threads in global time. The underlying air implementation should use
     * a <code>Synchronizer</code> for this purpose.
     * @param r the radio sampling the RSSI value
     * @return an integer value representing the received signal strength indicator
     */
    public int sampleRSSI(Radio r) {
        long t = r.getSimulator().getClock().getCount();
        synchronizer.waitForNeighbors(t);

        int strength = 0x3ff;
        synchronized (this) {
            Iterator i = packetsLeftOver.iterator();
            while (i.hasNext()) {
                Radio.Transmission p = (Radio.Transmission)i.next();
                if (intersect(p, t - sampleTime, t)) strength = 0;
            }
            i = packetsThisPeriod.iterator();
            while (i.hasNext()) {
                Radio.Transmission p = (Radio.Transmission)i.next();
                if (intersect(p, t - sampleTime, t)) strength = 0;
            }
        }

        return strength;
    }

    private boolean intersect(Radio.Transmission p, long begin, long end) {
        return p.deliveryTime > begin && p.originTime < end;
    }

    protected synchronized void computeWaveForm() {

        // only need to compute the transmission once (per interval)
        if ( computedTransmission != null ) return;

        long currentTime = nextDeliveryTime;

        int data = 0;
        int bitsFront = 0, bitsEnd = 0;

        // iterate over all of the packets in the past two intervals
        Iterator pastIterator = packetsLeftOver.iterator();
        Iterator curIterator = packetsThisPeriod.iterator();
        while (true) {
            Radio.Transmission packet;
            if (pastIterator.hasNext())
                packet = (Radio.Transmission)pastIterator.next();
            else if (curIterator.hasNext())
                packet = (Radio.Transmission)curIterator.next();
            else
                break;

            // NOTE: this calculation assumes MSB first
            if (currentTime == packet.deliveryTime) {
                // exact match!
                data |= packet.data;
                bitsFront = 8;
                bitsEnd = 8;
            } else if (currentTime < packet.deliveryTime) {
                // end of the packet is sliced off
                int bitdiffx2 = ((int)(packet.deliveryTime - currentTime)) / bitPeriod2;
                int bitdiff = (bitdiffx2 + 1) / 2;
                int bitsE = 8 - bitdiff;
                if (bitsE > bitsEnd) bitsEnd = bitsE;
                data |= packet.data >>> bitdiff;
            } else {
                // front of packet is sliced off
                int bitdiffx2 = ((int)(currentTime - packet.deliveryTime)) / bitPeriod2;
                int bitdiff = (bitdiffx2 + 1) / 2;
                int bitsF = 8 - bitdiff;
                if (bitsF > bitsFront) bitsFront = bitsF;
                data |= packet.data << bitdiff;
            }
        }


        // finish building the radio packet
        if (bitsFront + bitsEnd >= 8) {
            // enough bits were collected to deliver a packet
            computedTransmission = new Radio.Transmission((byte)data, 0, nextDeliveryTime - bytePeriod);
            lastDeliveryTime = nextDeliveryTime;
        } else {
            // not enough bits were collected to deliver a packet
            computedTransmission = null;
        }


    }

    protected class DeliveryEvent implements Simulator.Event {
        public void fire() {
            SimulatorThread thread = (SimulatorThread)Thread.currentThread();
            Simulator sim = thread.getSimulator();
            synchronizer.waitForNeighbors(sim.getClock().getCount());
            computeWaveForm();
            if ( computedTransmission != null ) {
                Radio radio = sim.getMicrocontroller().getRadio();
                radio.receive(computedTransmission);
                // deliver the transmission to the radio
            }
        }
    }

}
