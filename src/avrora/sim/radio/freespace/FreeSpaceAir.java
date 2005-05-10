/**
 * Created on 01.11.2004
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
import avrora.sim.clock.GlobalClock;
import avrora.sim.radio.Radio;
import avrora.sim.radio.RadioAir;
import avrora.sim.clock.GlobalClock;
import avrora.util.Verbose;
import avrora.util.Visual;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Implementation of the free space radio propagation model
 *
 * @author Olaf Landsiedel
 *         <p/>
 *         Yes, the free space radio model is not very realistic, but this is not the purpose of thie
 *         implementation. It shall model the characteristics of radio propagation and so enable multihop
 *         scenarios. This implementation bases heavily on the SimpleAir class by Daniel Lee. However, the
 *         changes needed, where to heavy to allow for standard class extension
 */
public class FreeSpaceAir implements RadioAir {

    //one global air for everybody
    public static final FreeSpaceAir freeSpaceAir;

    private final Verbose.Printer rssiPrinter = Verbose.getVerbosePrinter("radio.rssi");

    //one global air for everybody
    static {
        freeSpaceAir = new FreeSpaceAir();
    }

    /**
     * GlobalClock used by the air environment. Essential for synchronization.
     */
    protected final RadioClock radioClock;

    // all radios
    protected final HashSet radios;

    /**
     * State for maintaining RSSI wait for neighbors
     */
    int rssi_count;
    int meet_count;
    TreeSet rssi_waiters;

    /**
     * The amount of cycles it takes for one byte to be sent.
     */
    public static final int bytePeriod = Radio.TRANSFER_TIME;
    public static final int bitPeriod = Radio.TRANSFER_TIME / 8;
    public static final int bitPeriod2 = Radio.TRANSFER_TIME / 16;

    //const. for radio propagation
    private static final double lightTemp = 299792458 / (4 * Math.PI);
    private static final double lightConst = lightTemp * lightTemp;
    private static final double noiseCutOff = 0.000009;

    /**
     * new free space air
     */
    public FreeSpaceAir() {
        radios = new HashSet();
        radioClock = new RadioClock(bytePeriod);
        rssi_waiters = new TreeSet();
    }

    /**
     * add radio
     *
     * @see avrora.sim.radio.RadioAir#addRadio(avrora.sim.radio.Radio)
     */
    public synchronized void addRadio(Radio r) {
        //add this radio to the other radio's neighbor list
        Iterator it = radios.iterator();
        while (it.hasNext()) {
            LocalAir localAir = ((Radio)it.next()).getLocalAir();
            //add the new radio to the other radio's neighbor list
            localAir.addNeighbor(r.getLocalAir());
            //add the other radios to this radio's neighbor list
            r.getLocalAir().addNeighbor(localAir);
        }
        radios.add(r);
        radioClock.add(r.getSimulatorThread());
        //deliveryMeet.setGoal(radioClock.getNumberOfThreads());
    }

    /**
     * remove radio
     *
     * @see avrora.sim.radio.RadioAir#removeRadio(avrora.sim.radio.Radio)
     */
    public synchronized void removeRadio(Radio r) {
        radioClock.ticker.decGoal();
        radios.remove(r);
        Iterator it = radios.iterator();
        while (it.hasNext()) {
            LocalAir localAir = ((Radio)it.next()).getLocalAir();
            //remove the radio from the other radio's neighbor list
            localAir.removeNeighbor(r.getLocalAir());
        }
    }

    /**
     * transmit packet
     *
     * @see avrora.sim.radio.RadioAir#transmit(avrora.sim.radio.Radio, avrora.sim.radio.Radio.RadioPacket)
     */
    public synchronized void transmit(Radio r, Radio.RadioPacket f) {
        //compute transmission range, incl. noise
        //first compute tranmission power in Watt
        double powerSet = (double)r.getPower();
        double power;
        //convert to dB (by linearization) and than to Watts
        //for linearization, we distinguish values less than 16
        //and higher ones. Probably a lookup table and Spline
        //interpolation would be nice here
        if (powerSet < 16)
            power = Math.pow(10, (0.12 * powerSet - 1.8));
        else
            power = Math.pow(10, (0.00431 * powerSet - 0.06459));
        
        //second compute free space formula (without distance)
        //SignalRec = SignalSend * lightTerm * (1 / ( distance * freq))^2;
        // where lightTerm is ( c / ( 4Pi ))^2
        double freq = r.getFrequency();
        double temp = power * lightConst * (1 / (freq * freq));
        // send packet to devices in ranges
        Iterator it = r.getLocalAir().getNeighbors();
        Visual.send(r.getSimulator().getID(),
                "packetTx",
                f.data);
        while (it.hasNext()) {
            Distance dis = (Distance)it.next();
            double powerRec = temp / (dis.distance * dis.distance);
            //check if device is in range
            if (powerRec > noiseCutOff) {
                Visual.send(r.getSimulator().getID(),
                        "packetTxInRange",
                        dis.radio.getRadio().getSimulator().getID(),
                        f.data);
                dis.radio.addPacket(f, powerRec, r);
            }
        }

    }

    /**
     * see SimpleAir
     */
    protected class RSSIWait implements Comparable {
        boolean shouldWait;
        final long sampleTime;
        final Simulator simulator;

        public RSSIWait(Simulator s, long st) {
            simulator = s;
            sampleTime = st;
            shouldWait = true;
        }

        public int compareTo(Object o) {
            if (o == this) return 0;
            RSSIWait w = (RSSIWait)o;
            int diff = (int)(w.sampleTime - this.sampleTime);
            if (diff == 0) return w.hashCode() - this.hashCode();
            return diff;
        }
    }

    /**
     * see simple air for more
     *
     * @see avrora.sim.radio.RadioAir#sampleRSSI(avrora.sim.radio.Radio)
     */
    public int sampleRSSI(Radio r) {
        Simulator s = r.getSimulator();
        long t = s.getState().getCycles();
        RSSIWait w = new RSSIWait(s, t);
        RSSIWait f;

        // TODO: gross hack to handle the case of only one node
        if (!(Thread.currentThread() instanceof SimulatorThread))
            return 0x3ff;

        synchronized (this) {
            if (rssiPrinter.enabled) {
                rssiPrinter.println("RSSI: sampleRSSI @ " + t);
            }
            // add use to the waiters
            rssi_waiters.add(w);
            rssi_count++;
            // check to see if everyone is ready to go
            f = checkRSSIWaiters(w);
        }

        // are we at the head of the RSSI waiter's list, and everyone is ready to go?
        if (f != w) {
            // we are not at the head of the RSSI waiters list, wait until we are notified
            try {
                synchronized (w) {
                    if (rssiPrinter.enabled) {
                        rssiPrinter.println("RSSI: wait @ " + t + ' ' + w);
                    }
                    // check that no thread got to us in between giving up the global lock
                    if (w.shouldWait) w.wait();
                }
            } catch (InterruptedException e) {
                throw new GlobalClock.InterruptedException(e);
            }
        }

        int strength;

        // we just got woken up (or returned immediately from checkRSSIWaiters
        // this means we are now ready to proceed and compute our RSSI value.
        synchronized (this) {
            strength = r.getLocalAir().sampleRSSI(t);
        }

        return strength;
    }

    // see simple air
    private synchronized void incrementMeets() {
        meet_count++;
        // wake up the first RSSI waiter (if there are any)
        checkRSSIWaiters(null);
    }

    /**
     * see simple air Tricky! This method checks whether every thread has reached either a local meet or has
     * tried to read the RSSI value. If every thread has joined in one of these two ways this method will
     * select the RSSI waiter that made the earliest request. If that waiter is NOT the waiter passed as a
     * parameter, it will wake that waiter. If that waiter IS the waiter passed, it will NOT wake it.
     *
     * @param curWait the current waiter, null if this method is being called as a result of a thread entering
     *                a local meet
     * @return the waiter at the head of the line if all threads have joined, null otherwise
     */
    private RSSIWait checkRSSIWaiters(RSSIWait curWait) {
        if (rssiPrinter.enabled) rssiPrinter.print("RSSI check(" + curWait + "): met " + meet_count + ", sampling " + rssi_count + ": ");

        if (meet_count + rssi_count == radioClock.getNumberOfThreads()) {

            // are there any waiters?
            if (rssi_waiters.isEmpty()) {
                if (rssiPrinter.enabled) rssiPrinter.println("no waiters");
                return null;
            }

            // signal first thread to continue
            RSSIWait w = (RSSIWait)rssi_waiters.first();
            rssi_count--;
            rssi_waiters.remove(w);

            if (w != curWait) {
                synchronized (w) {
                    if (rssiPrinter.enabled) rssiPrinter.println("notify " + w);
                    w.shouldWait = false;
                    w.notifyAll();
                }
            }
            return w;

        } else {
            // not everyone has joined
            if (rssiPrinter.enabled) rssiPrinter.println("not ready");
            return null;
        }
    }

    /**
     * @author Olaf Landsiedel
     *         <p/>
     *         The <code>RadioTicker</code> class is the global timer for the radio. It is specialized in that
     *         it will schedule delivery meets when, at the end of the interval, there was at least one packet
     *         sent. The simple air version (by Daniel Lee) has been heavily changed to enable free space
     *         radio modelling
     */
    protected class RadioTicker extends GlobalClock.Ticker {
        //long deliveryDelta;

        /**
         * new radio ticket
         *
         * @param p until event
         */
        RadioTicker(long p) {
            super(p);
        }

        /**
         * do the work needed before the synched action
         *
         * @see avrora.sim.clock.GlobalClock.LocalMeet#preSynchAction()
         */
        public void preSynchAction() {
            incrementMeets();
        }

        /**
         * do the action, which needs to be done in serialized
         *
         * @see avrora.sim.clock.GlobalClock.LocalMeet#serialAction()
         */
        public void serialAction() {
            eventQueue.advance(1);
            scheduleDelivery();
            meet_count = 0;
        }

        /**
         * schedule the delivery time to the local air implementation
         */
        private void scheduleDelivery() {
            Iterator it = radios.iterator();
            while (it.hasNext()) {
                Radio r = (Radio)it.next();
                LocalAir pr = r.getLocalAir();
                pr.scheduleDelivery(radioClock.globalTime(), r.getSimulator());
            }
        }

        /**
         * the actions, that can be done in parallel
         *
         * @see avrora.sim.clock.GlobalClock.LocalMeet#parallelAction(avrora.sim.SimulatorThread)
         */
        public void parallelAction(SimulatorThread t) {
            Simulator simulator = t.getSimulator();
            simulator.insertEvent(this, tickerPeriod);
            //LocalAir ps = simulator.getMicrocontroller().getRadio().getLocalAir();
            //ps.scheduleDelivery(radioClock.globalTime(), simulator);
        }
    }

    /**
     * An extended version of <code>GlobalClock</code> that implements a version of <code>LocalMeet</code>
     * that is appropriate for delivering radio packets.
     *
     * @author Daniel Lee
     */
    protected class RadioClock extends GlobalClock {
        protected RadioClock(long p) {
            super(p, new RadioTicker(p));
        }

    }


}
