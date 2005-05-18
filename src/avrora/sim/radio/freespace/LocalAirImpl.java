/**
 * Created on 29.10.2004
 *
 * Copyright (c) 2004-2005, Olaf Landsiedel, Protocol Engineering and
 * Distributed Systems, University of Tuebingen
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
 * Neither the name of the Protocol Engineering and Distributed Systems
 * Group, the name of the University of Tuebingen nor the names of its 
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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

package avrora.sim.radio.freespace;

import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.clock.Synchronizer;
import avrora.sim.radio.Radio;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Implementation of Local Air
 *
 * @author Olaf Landsiedel
 */
public class LocalAirImpl {

    //neighbor list, sorted by distance
    private final LinkedList neighbors;
    //position of this node
    private final Position position;
    //delivery of the packets
    private final Deliver deliver;
    //received packet list
    private LinkedList packets;
    //first received packet -> earliest packet
    private Radio.Transmission firstPacket;
    //the strongest packet -> highest signal strength
    private PowerRadioPacket finalPacket;
    //the radio sending and receiving from this air
    private final Radio radio;

    // state for maintaining meeting of threads for delivery
    private long lastDeliveryTime;
    private long nextDeliveryTime;
    //some radio const
    public static final int sampleTime = 13 * 64;
    public static final int bytePeriod = Radio.TRANSFER_TIME;

    final Synchronizer synchronizer;

    /**
     * new local air
     *
     * @param r   radio
     * @param pos position
     */
    public LocalAirImpl(Radio r, Position pos, Synchronizer synch) {
        position = pos;
        neighbors = new LinkedList();
        packets = new LinkedList();
        deliver = new Deliver();
        radio = r;
        synchronizer = synch;
    }

    /**
     * get node position
     *
     */
    public Position getPosition() {
        return position;
    }

    public Radio getRadio() {
        return radio;
    }

    /**
     * add neighbor
     *
     */
    public synchronized void addNeighbor(LocalAirImpl r) {
        Position position = r.getPosition();
        double x = position.x - this.position.x;
        double y = position.y - this.position.y;
        double z = position.z - this.position.z;
        double distance = Math.sqrt(x * x + y * y + z * z);
        if (distance == 0)
            distance = 0.000001;
        addNeighbor(r, distance);
    }

    /**
     * local helper for adding a neighbor
     *
     * @param r        radio
     * @param distance distance
     */
    private void addNeighbor(LocalAirImpl r, double distance) {
        ListIterator it = neighbors.listIterator();
        boolean done = false;
        while (!done && it.hasNext()) {
            Distance dis = (Distance)it.next();
            if (distance < dis.distance) {
                it.previous();
                it.add(new Distance(r, distance));
                done = true;
            }
        }
        //either the list is empty
        //or this new radio shall go to the end of the list
        //as it has the greatest distance
        if (!done) {
            it.add(new Distance(r, distance));
        }
    }

    /**
     * remove a node
     *
     */
    public synchronized void removeNeighbor(LocalAirImpl r) {
        ListIterator it = neighbors.listIterator();
        while (it.hasNext()) {
            Distance dis = (Distance)it.next();
            if (dis.radio == r) {
                it.remove();
            }
        }
    }

    /**
     * tell me, who is around
     *
     */
    public synchronized Iterator getNeighbors() {
        return neighbors.iterator();
    }

    /**
     * receive a packet
     *
     */
    public synchronized void addPacket(Radio.Transmission p, double pow, Radio sender) {
        packets.addLast(new PowerRadioPacket(p, pow, sender));
        //find the earliest packet
        if (firstPacket == null) {
            firstPacket = p;
        } else if (p.originTime < firstPacket.originTime) {
            firstPacket = p;
        }

    }


    /**
     * compute signal strength bases on @see avrora.sim.radio.SimpleAir#sampleRSSI(long) by Daniel Lee
     *
     */
    public int sampleRSSI(long cycles) {
        int strength = 0x3ff;
        Iterator i = packets.iterator();
        //see whether there are packets around
        //if so, set strength to 0 (it works inverted)
        while (i.hasNext()) {
            Radio.Transmission p = ((PowerRadioPacket)i.next()).packet;
            if (p.deliveryTime > (cycles - sampleTime) && p.originTime < cycles) strength = 0;
        }
        //TODO: hmm... do I really need the lines
        //this packet shall be included in the while loop
        if (finalPacket != null && finalPacket.packet != null) {
            Radio.Transmission p = finalPacket.packet;
            if (p.deliveryTime > (cycles - sampleTime) && p.originTime < cycles) strength = 0;
        }
        return strength;
    }

    /**
     * schedule a packet for delivery
     *
     */
    public void scheduleDelivery(long globalTime) {
        long deliveryDelta;
        //when there is at least one packet to deliver, find the right one 
        //and the delivery time
        if (firstPacket != null) {
            deliveryDelta = computeNextDelivery(globalTime);
            firstPacket = null;
        } else {
            deliveryDelta = 0;
        }
        //when there is a packet to deliver, do so...
        if (deliveryDelta > 0) {
            radio.getSimulator().insertEvent(deliver, deliveryDelta);
        }
    }

    /**
     * find the right packet to deliver
     *
     * @param globalTime current time
     * @return time to deliver
     */
    protected long computeNextDelivery(long globalTime) {

        long limit = lastDeliveryTime;

        if (lastDeliveryTime > globalTime - bytePeriod) {
            // there was a byte delivered in the period just finished
            limit += bytePeriod;
        }

        finalPacket = null;

        //iterate over all of the packets
        Iterator it = packets.iterator();
        packets = new LinkedList();
        while (it.hasNext()) {
            PowerRadioPacket packet = (PowerRadioPacket)it.next();            
            //check, whether packet maybe delivered
            if (packet.packet.deliveryTime >= limit) {
                if (finalPacket == null)
                    finalPacket = packet;
                // find the strongest packet
                else if (packet.power > finalPacket.power)
                    finalPacket = packet;
            }
        }
        if (finalPacket == null)
            return 0;
        nextDeliveryTime = finalPacket.packet.deliveryTime;
        return nextDeliveryTime - globalTime;
    }

    /**
     * handles the final delivery to the radio
     *
     * @author Olaf Landsiedel
     */
    protected class Deliver implements Simulator.Event {

        /**
         * it is time for a delivery
         *
         * @see avrora.sim.Simulator.Event#fire()
         */
        public void fire() {
            SimulatorThread thread = (SimulatorThread)Thread.currentThread();
            Simulator sim = thread.getSimulator();
            synchronizer.waitForNeighbors(sim.getClock().getCount());
            if (finalPacket != null) {
                lastDeliveryTime = finalPacket.packet.deliveryTime;
                radio.receive(finalPacket.packet);
            }

        }
    }

    /**
     * class to model a packet and transmission power
     *
     * @author Olaf Landsiedel
     */
    public static class PowerRadioPacket {

        //radio packet
        protected final Radio.Transmission packet;
        //transmission power
        protected final double power;

        protected final Radio sender;

        /**
         * new packet
         *
         * @param p   packet
         * @param pow transmission power
         */
        public PowerRadioPacket(Radio.Transmission p, double pow, Radio s) {
            this.packet = p;
            this.power = pow;
            this.sender = s;
        }


        /**
         * get the radio packet
         *
         * @return radio packet
         */
        public Radio.Transmission getRadioPacket() {
            return packet;
        }

        /**
         * get the transmission power
         *
         * @return power
         */
        public double getPower() {
            return power;
        }
    }
}
