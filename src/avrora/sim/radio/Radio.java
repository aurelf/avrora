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

import avrora.monitors.Monitor;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.FiniteStateMachine;
import avrora.sim.mcu.Microcontroller;

/**
 * The <code>Radio</code> interface should be implemented by classes which would like to act as radios and
 * access an instance of the <code>RadioAir</code> interface.
 *
 * @author Daniel Lee
 */
public interface Radio {

    public int NODE_HZ = 7372800;
    public int MAX_BIT_RATE = 19200;

    /**
     * Time in main clock cycles it takes for one byte to be sent over the air. Much of the implementation is
     * derived from this constant, so generalizing in the future may require some careful consideration.
     */
    public int TRANSFER_TIME = 8 * NODE_HZ / MAX_BIT_RATE;

    /**
     * A <code>Transmission</code> is an object describing the data transmitted over <code>RadioAir</code> over
     * some period of time.
     */
    public class Transmission implements Comparable {

        public final byte data;
        public final long frequency;

        public final long originTime;
        public final long deliveryTime;

        public int strength = 0x3ff;

        public Transmission(byte data, long frequency, long originTime) {
            this.data = data;
            this.frequency = frequency;
            this.originTime = originTime;
            this.deliveryTime = originTime + TRANSFER_TIME;
        }

        public int compareTo(Object o) {
            Transmission p = (Transmission)o;
            if (p.originTime > originTime) return -1;
            if (p.originTime < originTime) return 1;
            return 0;
        }

    }

    /**
     * A <code>RadioController</code> is an object installed into a Microcontroller. The recommended
     * implementation is to implement specialized IO registers as inner classes and install them into the
     * Microcontroller. Changes to these specialized registers should initiate appropriate behavior with the
     * radio.
     */
    public interface RadioController {

        /**
         * Installs this Controller into a microcontroller. This should setup the pins, IO registers in such a
         * way that changes to CPU state will make corresponding changes to the RadioController state that
         * will initiate sends and receives if necessary.
         */
        public void install(Microcontroller mcu);

        /**
         * Enable transfers.
         */
        public void enable();

        /**
         * Disable transfers.
         */
        public void disable();
    }

    /**
     * Get the <code>Simulator</code> on which this radio is running.
     */
    public Simulator getSimulator();

    /**
     * Get the <code>SimulatorThread</code> thread on which this radio is running.
     */
    public SimulatorThread getSimulatorThread();

    /**
     * Set the <code>SimulatorThread</code> of this radio.
     */
    public void setSimulatorThread(SimulatorThread thread);

    /**
     * get the transmission power
     *
     * @return transmission power
     */
    public int getPower();

    /**
     * get the current frequency
     *
     * @return frequency
     */
    public double getFrequency();

    public boolean isListening();

    public RadioAir getAir();

    public void setAir(RadioAir air);

    /**
     * The <code>getFiniteStateMachine()</code> method gets a reference to the finite state
     * machine that represents this radio's state. For example, there are states corresponding
     * to "on", "off", "transmitting", and "receiving". The state names and numbers will vary
     * by radio implementation. The <code>FiniteStateMachine</code> instance allows the user
     * to instrument the state transitions in order to gather information during simulation.
     * @return a reference to the finite state machine for this radio
     */
    public FiniteStateMachine getFiniteStateMachine();

    /**
     * The <code>insertProbe()</code> method inserts a probe into a radio. The probe is then
     * notified when the radio changes power, frequency, baud rate, or transmits or receives
     * a byte.
     * @param p the probe to insert on this radio
     */
    public void insertProbe(RadioProbe p);

    /**
     * The <code>removeProbe()</code> method removes a probe on this radio.
     * @param p the probe to remove from this radio instance
     */
    public void removeProbe(RadioProbe p);


    /**
     * The <code>RadioProbe</code> interface encapsulates the idea of a probe inserted on a radio
     * that is notified when changes in the state of the radio occur and when packets are sent and received
     * from this radio.
     */
    public interface RadioProbe {
        public void fireAtPowerChange(Radio r, int newPower);
        public void fireAtFrequencyChange(Radio r, double freq);
        public void fireAtBitRateChange(Radio r, int newbitrate);
        public void fireAtTransmit(Radio r, Radio.Transmission p);
        public void fireAtReceive(Radio r, Radio.Transmission p);

        public class Empty implements RadioProbe {
            public void fireAtPowerChange(Radio r, int newPower) {}
            public void fireAtFrequencyChange(Radio r, double freq) {}
            public void fireAtBitRateChange(Radio r, int newbitrate) {}
            public void fireAtTransmit(Radio r, Radio.Transmission p) {}
            public void fireAtReceive(Radio r, Radio.Transmission p) {}
        }
    }
}
