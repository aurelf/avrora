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

    protected TreeSet packetsThisPeriod;
    protected TreeSet packetsLeftOver;

    protected final HashSet radios;

    private boolean scheduledDelivery;
    private long lastDeliveryTime;
    private long nextDeliveryTime;

    protected final Simulator.Event scheduleDelivery;

    /** The amount of cycles it takes for one byte to be sent.*/
    public final long bytePeriod = Radio.TRANSFER_TIME;
    public final long bitPeriod = Radio.TRANSFER_TIME / 8;

    public SimpleAir() {
        radios = new HashSet();
        packetsThisPeriod = new TreeSet();
        packetsLeftOver = new TreeSet();
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
        packetsThisPeriod.add(f);

        if (!scheduledDelivery) {
            scheduledDelivery = true;
            globalQueue.insertEvent(scheduleDelivery, 1);
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

            Radio.RadioPacket first = (Radio.RadioPacket)packetsThisPeriod.first();
            deliveryGoal = first.deliveryTime;

            if ( lastDeliveryTime > globalTime - bytePeriod ) {
                // there was a byte delivered in the period just finished
                if ( first.deliveryTime <= lastDeliveryTime + bytePeriod ) {
                    // the first packet in this interval overlapped a bit with the last delivery
                    deliveryGoal = lastDeliveryTime + bytePeriod;
                }
            }

            // TODO: pick out individual packets one by one?
            Set pastPackets = packetsLeftOver.tailSet(new Radio.RadioPacket((byte)0, 0, lastDeliveryTime-bytePeriod));
            packetsThisPeriod.addAll(pastPackets);
            globalQueue.addLocalMeet(globalQueue.meet, deliveryGoal);
            scheduledDelivery = false;

            // just finished this period, so create a new set for the next period
            packetsLeftOver = packetsThisPeriod;
            packetsThisPeriod = new TreeSet();
            nextDeliveryTime = deliveryGoal;
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

                byte data = 0;

                // iterate over all of the packets in the past two intervals
                Iterator pastIterator = packetsLeftOver.iterator();
                while ( pastIterator.hasNext() ) {
                    Radio.RadioPacket p = (Radio.RadioPacket)pastIterator.next();
                    data = addToWaveform(data, currentTime, p);
                }

                // iterate over all of the packets in the current interval
                Iterator curIterator = packetsThisPeriod.iterator();
                while ( curIterator.hasNext() ) {
                    Radio.RadioPacket p = (Radio.RadioPacket)curIterator.next();
                    data = addToWaveform(data, currentTime, p);
                }

                // finish building the radio packet
                computedPacket = new Radio.RadioPacket(data, 0, nextDeliveryTime - bytePeriod);
                lastDeliveryTime = nextDeliveryTime;
            }

            protected byte addToWaveform(byte data, long currentTime, Radio.RadioPacket p) {
                return data;
            }

            public void parallelAction(SimulatorThread s) {
                Radio radio = s.getSimulator().getMicrocontroller().getRadio();
                radio.receive(computedPacket);
            }
        }

    }


}
