/**
 * Created on 29.10.2004
 *
 * Copyright (c) 2004, Olaf Landsiedel, Protocol Engineering and 
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

import java.util.Iterator;
import java.util.LinkedList;

import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.radio.*;
import avrora.sim.radio.Radio.RadioPacket;
import avrora.sim.mcu.*;
import avrora.sim.radio.freespace.*;

import java.util.*;

/** Implementation of Local Air
 * @author Olaf Landsiedel
 *
 */
public class LocalAirImpl implements LocalAir{

    //neighbor list, sorted by distance
    private LinkedList neighbors;
    //position of this node
    private Position position;
    //delivery of the packets
    private Deliver deliver;
    //received packet lisr
    private LinkedList packets;
    //first received packet -> earliest packet
    private Radio.RadioPacket firstPacket;
    //the strongest packet -> highest signal strength
    private PowerRadioPacket finalPacket;
    //the radio sending and receiving from this air
    private Radio radio;
    
    // state for maintaining meeting of threads for delivery
    private long lastDeliveryTime;
    private long nextDeliveryTime;
    
    /** new local air
     * @param r radio
     * @param pos position
     */
    public LocalAirImpl(Radio r, Position pos){
        position = pos;
        neighbors = new LinkedList();
        packets = new LinkedList();
        deliver = new Deliver();
        radio = r;
    }
    
    /** get node position
     * @see avrora.sim.radio.freespace.LocalAir#getPosition()
     */
    public Position getPosition(){
        return position;
    }
    
    /** add neighbor
     * @see avrora.sim.radio.freespace.LocalAir#addNeighbor(avrora.sim.radio.freespace.LocalAir)
     */
    public synchronized void addNeighbor(LocalAir r){
        double x = r.getPosition().getX() - position.getX();
        double y = r.getPosition().getY() - position.getY();
        double z = r.getPosition().getZ() - position.getZ();
        double distance = Math.sqrt(x*x + y*y + z*z);
        if (distance == 0)
            distance = 0.000001;
        addNeighbor(r, distance);
    }
    
    /** local helper for adding a neighbo
     * @param r radio
     * @param distance distance
     */
    private void addNeighbor(LocalAir r, double distance){
        ListIterator it = neighbors.listIterator();
        boolean done = false;
        while( !done && it.hasNext()) {
            Distance dis = (Distance) it.next();
            if( distance < dis.distance ){
                it.previous();
                it.add( new Distance(r, distance));
                done = true;
            }
        }
        //either the list is empty
        //or this new radio shall go to the end of the list
        //as it has the greatest distance
        if( !done ){
            it.add( new Distance(r, distance));
        }
    }
    
    /** remove a node
     * @see avrora.sim.radio.freespace.LocalAir#removeNeighbor(avrora.sim.radio.freespace.LocalAir)
     */
    public synchronized void removeNeighbor(LocalAir r){
        ListIterator it = neighbors.listIterator();
        while( it.hasNext()) {
            Distance dis = (Distance) it.next();            
            if( dis.radio == r ){
                it.remove();
            }
        }
    }
        
    /** tell me, who is around
     * @see avrora.sim.radio.freespace.LocalAir#getNeighbors()
     */
    public synchronized Iterator getNeighbors(){
        return neighbors.iterator();
    }
    
    /** receive a packet
     * @see avrora.sim.radio.freespace.LocalAir#addPacket(avrora.sim.radio.Radio.RadioPacket, double)
     */
    public synchronized void addPacket(RadioPacket p, double pow){
        packets.addLast(new PowerRadioPacket(p, pow));
        //find the earliest packet
        if ( firstPacket == null ) {
            firstPacket = p;
        } else if ( p.originTime < firstPacket.originTime ) {
            firstPacket = p;
        }
        
    }


    /** compute signal strength
     *  bases on @see avrora.sim.radio.SimpleAir#sampleRSSI(long) by Daniel Lee
     * @see avrora.sim.radio.freespace.LocalAir#sampleRSSI(long)
     */
    public int sampleRSSI(long cycles){
        int strength = 0x3ff;
        Iterator i = packets.iterator();
        //see whether there are packets around
        //if so, set strength to 0 (it works inverted)
        while ( i.hasNext() ) {
            Radio.RadioPacket p = ((PowerRadioPacket)i.next()).packet;
            if ( p.deliveryTime > (cycles - sampleTime) && p.originTime < cycles ) strength = 0;
        }
        //TODO: hmm... do I really need the lines
        //this packet shall be included in the while loop
        if( finalPacket != null && finalPacket.packet != null){
        	Radio.RadioPacket p = finalPacket.packet;
            if ( p.deliveryTime > (cycles - sampleTime) && p.originTime < cycles ) strength = 0;
        }
        return strength;
    }
    
    /** schedule a packet for delivery
     * @see avrora.sim.radio.freespace.LocalAir#scheduleDelivery(long, avrora.sim.Simulator)
     */
    public void scheduleDelivery(long globalTime, Simulator sim){
        long deliveryDelta = 0;
        //when there is at least one packet to deliver, find the right one 
        //and the delivery time
        if ( firstPacket != null ) {
            deliveryDelta = computeNextDelivery(globalTime);
            firstPacket = null;
        } else {
            deliveryDelta = 0;
        }
        //when there is a packet to deliver, do so...
        if ( deliveryDelta > 0 )
            sim.insertEvent(deliver, deliveryDelta);
        return;
    }
    
    /** find the right packet to deliver
     * @param globalTime current time
     * @return time to deliver
     */
    protected long computeNextDelivery(long globalTime) {

        long limit = lastDeliveryTime;

        if ( lastDeliveryTime > globalTime - bytePeriod ) {
            // there was a byte delivered in the period just finished
            limit +=  bytePeriod;
        }

        finalPacket = null;

        //iterate over all of the packets
        Iterator it = packets.iterator();
        packets = new LinkedList();
        while ( it.hasNext() ) {
            PowerRadioPacket packet = (PowerRadioPacket)it.next();            
            //System.out.print(radio.getSimulator().getClock().getCount() + " " +
            //        radio.getSimulator().getID() + " choose " + ~packet.packet.data + " " + globalTime + " " + packet.packet.deliveryTime + "   |   ");            
            //check, whether packet maybe delivered
            if( packet.packet.deliveryTime >= limit ){
                if( finalPacket == null )
                    finalPacket = packet;
                // find the strongest packet
                else if (packet.power > finalPacket.power)
                    finalPacket = packet;
            }
        }
        if( finalPacket == null )
            return 0;
        nextDeliveryTime = finalPacket.packet.deliveryTime;
        //System.out.println();
        //System.out.println(radio.getSimulator().getClock().getCount() + " " +
        //        radio.getSimulator().getID() + " final packet: " + ~finalPacket.packet.data);        
        return nextDeliveryTime - globalTime;
    }

    /** handles the final delivery to the radio
     * @author Olaf Landsiedel
     *
     */
    protected class Deliver implements Simulator.Event {

        /** it is time for a delivery
         * @see avrora.sim.Simulator.Event#fire()
         */
        public void fire() {
            //long currentTime = nextDeliveryTime;
            if( finalPacket != null ){
                lastDeliveryTime = finalPacket.packet.deliveryTime;
                //Radio radio = s.getSimulator().getMicrocontroller().getRadio();
                radio.receive(finalPacket.packet);
            }

        }
    }
    

}
