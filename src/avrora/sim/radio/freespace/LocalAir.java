/**
 * Created on 30.10.2004
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
import avrora.sim.radio.Radio;

import java.util.Iterator;

/**
 * local air each device has a local view on the global air. It sees different packets and transmission
 * power.
 *
 * @author Olaf Landsiedel
 */
public interface LocalAir {
    //some radio const
    int sampleTime = 13 * 64;
    int bytePeriod = Radio.TRANSFER_TIME;

    /**
     * get the position of this node
     *
     * @return node position
     */
    public Position getPosition();

    /**
     * add a new node the air
     *
     * @param r node radio
     */
    public void addNeighbor(LocalAir r);

    /**
     * remove node from the air
     *
     * @param r node radio
     */
    public void removeNeighbor(LocalAir r);

    /**
     * get list of nodes around
     *
     * @return node list iterator
     */
    public Iterator getNeighbors();

    /**
     * measure the signal strength around here
     *
     * @param cycles time
     * @return signal strength
     */
    public int sampleRSSI(long cycles);

    /**
     * add packet to be received, called by transmitting nodes
     *
     * @param p   packet
     * @param pow transission power
     */
    public void addPacket(Radio.RadioPacket p, double pow, Radio sender);

    /**
     * schedule deivery of packets
     *
     * @param time delivery time
     * @param sim  the simulator
     */
    public void scheduleDelivery(long time, Simulator sim);

    public Radio getRadio();

    /**
     * class to model a packet and transmission power
     *
     * @author Olaf Landsiedel
     */
    public class PowerRadioPacket {

        //radio packet
        protected Radio.RadioPacket packet;
        //transmission power
        protected double power;

        protected Radio sender;

        /**
         * new packet
         *
         * @param p   packet
         * @param pow transmission power
         */
        public PowerRadioPacket(Radio.RadioPacket p, double pow, Radio s) {
            this.packet = p;
            this.power = pow;
            this.sender = s;
        }


        /**
         * get the radio packet
         *
         * @return radio packet
         */
        public Radio.RadioPacket getRadioPacket() {
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
