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
import avrora.sim.util.GlobalQueue;

import java.util.*;

/**
 * Very simple implementation of radio air. It assumes a lossless environment where all radios are able to communicate
 * with each other. This simple air is blind to the frequencies used in transmission (i.e. it assumes that all
 * frequencies are really the same).
 *
 * This class should provide the proper scheduling policy with respect to threads that
 * more complicated radio implementations can use the time scheduling policy
 * and only overload the delivery policy.
 *
 * @author Daniel Lee
 */
public class SimpleAir implements RadioAir {

    public final static SimpleAir simpleAir;

    //TODO: This class needs a verbose printer.

    // TODO: Determine whether it is worthwhile to create this initial simple air more cleanly.
    static {
        simpleAir = new SimpleAir();
    }

    public boolean messageInAir() {
        return !messages.isEmpty();
    }

    /** GlobalQueue used by the air environment. Essential for synchronization. */
    protected final EnvironmentQueue globalQueue;

    protected final LinkedList messages;

    protected final HashSet radios;

    private boolean scheduledGlobalMeet;

    /** The amount of cycles it takes for one byte to be sent.*/
    public final long bytePeriod = Radio.TRANSFER_TIME;
    public final long bitPeriod = 763;

    public final double transferTime = .8332651000300227;

    public SimpleAir() {
        radios = new HashSet();
        messages = new LinkedList();
        globalQueue = new EnvironmentQueue(bytePeriod);
    }

    public synchronized void addRadio(Radio r) {
        radios.add(r);
        globalQueue.add(r.getSimulatorThread());

    }

    public synchronized void removeRadio(Radio r) {
        radios.remove(r);
    }

    public synchronized void transmit(Radio r, Radio.RadioPacket f) {
        messages.addLast(f);

        if (!scheduledGlobalMeet) {
            scheduledGlobalMeet = true;
            globalQueue.addTimerEvent(new ScheduleDelivery(f), 1);
        }
    }

    /** The event entered into the GLOBAL event queue, which will schedule synchronization
     * events for individual nodes.*/
    protected class ScheduleDelivery implements Simulator.Event {

        Radio.RadioPacket packet;

        ScheduleDelivery(Radio.RadioPacket f) {
            this.packet = f;
        }

        public void fire() {
            // Schedule local events.
            //double delay = 1.0; // TODO: calculate delay properly
            long globalTime = (globalQueue.getCount()) * Radio.TRANSFER_TIME;
            long delay = Radio.TRANSFER_TIME - (globalTime - packet.origination.longValue());

            //System.err.println("Delay " + delay + ", Global Time: " + globalTime +
            //        " , or: " + packet.origination.longValue() + ", dif: " + (globalTime - packet.origination.longValue()));
            /*
                delay = TXTime - (GT - LT); if GT is ahead of LT when this is fired

            */
            globalQueue.addDeliveryMeet(delay);
            scheduledGlobalMeet = false;
        }

    }


    /** An extended version of <code>GlobalQueue</code> that implements a version of
     * <code>LocalMeet</code> that is appropriate for delivering radio packets. */
    protected class EnvironmentQueue extends GlobalQueue {
        protected EnvironmentQueue(long p) {
            super(p);
        }

        protected class DeliveryMeet extends LocalMeet {

            DeliveryMeet(Simulator sim, long scale, long delay) {
                super(sim, bytePeriod, delay);      // TODO cleanup. either use scale or leave it out
                id = "DEL";
            }

            public void action() {
                // TODO: Implement the action of this...
                /*
                    What am I doing here?
                    I am calculating the waveform.
                    Then, I am deliverying the waveform.
                    Need a sense of time...
                 */

                long currentTime = simulator.getState().getCycles();
                deliverWaveForm(currentTime);

            }
        }

        protected class DeliveryMeetFactory implements LocalMeetFactory {
            public LocalMeet produce(Simulator s, long scale, long delay) { // TODO: cleanup
                return new DeliveryMeet(s, scale, delay);     // TODO: cleanup
            }
        }

        protected void addDeliveryMeet(long delay) {
            addLocalMeet(new DeliveryMeetFactory(), bytePeriod, delay);
        }
    }

    /* Conjecture: I can make this the differentiating point of an Air implementation.
     * Specifically, I can allow more complicated delivery mechanisms to simply be
     * derived from overriding this method.
     * In order to make this the "differentiating point", I need to abstract
     * how messages are stored. OR, the transmit() method can be overloaded...
     * Question: Is it sufficient to only overload transmit and deliver, then?
     */
    protected synchronized void deliverWaveForm(long time) {
        // in order to calculate the radiopacket
        // grab all frames currently in the air
        // merge any that conflict, lower RSSI
        // deliver calculated form to member radios..

        Iterator packetIterator = messages.listIterator();
        Radio.RadioPacket packet = (Radio.RadioPacket) packetIterator.next();

        long del = packet.delivery.longValue();
        long diff = packet.delivery.longValue() - time;
        int tolerance = 3;

        packetIterator.remove();  // TODO: figure out right palce for this

        if (diff <= tolerance && -tolerance <= diff) {

            // This packet is meant to be delivered now. We must determine whether any
            // other packets conflict.

            // this set is nonempty if there are any overlapping packets.

            if(!messages.isEmpty()) {
                // conflicts exist..
                // we must perform a waveform calculation...

                TreeSet mappedSet = new TreeSet();

                long base = packet.origination.longValue() / bitPeriod;

                while (packetIterator.hasNext()) {
                    Radio.RadioPacket current = (Radio.RadioPacket) packetIterator.next();
                    mappedSet.add(new Radio.RadioPacket(current.data,
                            current.origination.longValue() / bitPeriod - base,
                            current.delivery.longValue() / bitPeriod - base));

                    packetIterator.remove();
                }

                byte acc = packet.data;

                packetIterator = mappedSet.iterator();

                while (packetIterator.hasNext()) {
                    Radio.RadioPacket current = (Radio.RadioPacket) packetIterator.next();

                    // TODO: fix shift direction based on whether it is MSB or LSB first
                    acc |= (byte) (current.data >> current.delivery.intValue());
                }

                packet = new Radio.RadioPacket(acc, packet.origination.longValue(),
                        packet.delivery.longValue());
                packet.strength = 0;
            }

            Iterator radioIterator = radios.iterator();

            while (radioIterator.hasNext()) {
                Radio radio = (Radio) radioIterator.next();
                radio.receive(packet);
            }


        } else if (time < del) {
            // TODO: handle these bad cases. early/late delivery.
            // early
            return;
        } else {
            // late
            return;

        }
    }

}
