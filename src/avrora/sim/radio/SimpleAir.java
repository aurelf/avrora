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

    static {
        simpleAir = new SimpleAir();
    }

    /** GlobalClock used by the air environment. Essential for synchronization. */
    protected final EnvironmentQueue globalQueue;

    protected Radio.RadioPacket firstPacket;
    protected LinkedList packetsThisPeriod;
    protected LinkedList packetsLeftOver;

    protected final HashSet radios;

    private long lastDeliveryTime;
    private long nextDeliveryTime;

    protected final Simulator.Event scheduleDelivery;

    /** The amount of cycles it takes for one byte to be sent.*/
    public final int bytePeriod = Radio.TRANSFER_TIME;
    public final int bitPeriod = Radio.TRANSFER_TIME / 8;
    public final int bitPeriod2 = Radio.TRANSFER_TIME / 16;

    public SimpleAir() {
        radios = new HashSet();
        packetsThisPeriod = new LinkedList();
        packetsLeftOver = new LinkedList();
        globalQueue = new EnvironmentQueue(bytePeriod);
        scheduleDelivery = new ScheduleDelivery();
    }

    public synchronized void addRadio(Radio r) {
        radios.add(r);
        globalQueue.add(r.getSimulatorThread());

    }

    public synchronized void removeRadio(Radio r) {
        radios.remove(r);
    }

    public synchronized void transmit(Radio r, Radio.RadioPacket f) {
        packetsThisPeriod.addLast(f);

        if ( firstPacket == null ) {
            globalQueue.insertEvent(scheduleDelivery, 1);
            firstPacket = f;
        }
        else {
            if ( f.originTime < firstPacket.originTime ) firstPacket = f;
        }
    }

    /**
     * The event entered into the GLOBAL event queue, which will schedule
     * synchronization events for individual nodes.
     */
    protected class ScheduleDelivery implements Simulator.Event {

        public void fire() {
            long globalTime = globalQueue.getCount() * globalQueue.period;
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

            // add a local meet for the delivery of the packet
            globalQueue.addLocalMeet(globalQueue.meet, deliveryGoal - globalTime);

            // just finished this period, so create a new set for the next period
            packetsLeftOver = packetsThisPeriod;
            packetsThisPeriod = new LinkedList();
            nextDeliveryTime = deliveryGoal;
            firstPacket = null;
        }

    }


    /**
     * An extended version of <code>GlobalClock</code> that implements a version of
     * <code>LocalMeet</code> that is appropriate for delivering radio packets.
     */
    protected class EnvironmentQueue extends GlobalClock {
        DeliveryMeet meet = new DeliveryMeet();

        protected EnvironmentQueue(long p) {
            super(p);
        }

        protected class DeliveryMeet extends LocalMeet {

            protected Radio.RadioPacket computedPacket;

            DeliveryMeet() {
                super("DELIVERY");
            }

            public void serialAction() {
                computeWaveForm();
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

            protected byte addToWaveform(byte data, long currentTime, Radio.RadioPacket p) {
                return data;
            }

            public void parallelAction(SimulatorThread s) {
                if ( computedPacket != null) {
                    // if there was a complete radio packet assembled
                    Radio radio = s.getSimulator().getMicrocontroller().getRadio();
                    radio.receive(computedPacket);
                }
            }
        }

    }


}
