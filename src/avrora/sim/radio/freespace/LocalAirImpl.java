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

import avrora.sim.clock.Synchronizer;
import avrora.sim.mcu.ADC;
import avrora.sim.radio.Channel;
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
    //the radio sending and receiving from this air
    private final Radio radio;

    private final Channel radioChannel;

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
        radio = r;
        radioChannel = new Channel(8, bytePeriod, true);
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
        radioChannel.write(p.data, 8, p.originTime);
    }


    /**
     * compute signal strength bases on @see avrora.sim.radio.SimpleAir#sampleRSSI(long) by Daniel Lee
     *
     */
    public int sampleRSSI(long gtime) {
        synchronizer.waitForNeighbors(gtime);
        return radioChannel.occupied(gtime - sampleTime, gtime) ? 0x0 : ADC.VBG_LEVEL;
    }

    public void advanceChannel() {
        radioChannel.advance();
    }

    public byte readChannel() {
        long ltime = radio.getSimulator().getClock().getCount();
        synchronizer.waitForNeighbors(ltime);
        return (byte)radioChannel.read(ltime, 8);
    }
}
