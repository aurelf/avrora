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
import avrora.sim.mcu.Microcontroller;
import avrora.sim.radio.freespace.LocalAir;
import avrora.sim.radio.freespace.Position;

/**
 * The <code>Radio</code> interface should be implemented by classes which would like to act as radios and
 * access an instance of the <code>RadioAir</code> interface.
 *
 * @author Daniel Lee
 */
public interface Radio extends Monitor {

    /**
     * Time in ATMega128L cycles it takes for one byte to be sent over the air. Much of the implementation is
     * derived from this constant, so generalizing in the future may require some careful consideration.
     */
    int TRANSFER_TIME = 3072;

    /* changed on 10/28/2004 OL
     * 
     * How the transfer time was calculated:
     * mica2 radio 38.4 kBaud, with Manchester Coding this is 19.2 Kbps
     * The chip has 7372800 cycles per sec
     * 
     * resulting in:
     * 38.4 kBaud / (2 Baud/Bit) = 19.2 kBit/sec
     * (19.2 kBit/sec) / (8 bits/byte) = 2400 bytes/sec
     * (7372800 cycles/sec) / (2400 bytes/sec) = 3072 cycles/byte
     * 
     * TODO
     * However, this calculation bases on 38.4 Baud and Manchester coding. As other 
     * baud rates and codings are supported by the CC1000 radio, this value should 
     * be part of the radio specific implementaion and be derived from the register
     * settings. 
     */
    
        
    /**
     * A <code>RadioPacket</code> is an object describing the data transmitted over <code>RadioAir</code> over
     * some period of time.
     */
    public class RadioPacket implements Comparable {

        public final byte data;
        public final long frequency;

        public final long originTime;
        public final long deliveryTime;


        public int strength = 0x3ff;

        public RadioPacket(byte data, long frequency, long originTime) {
            this.data = data;
            this.frequency = frequency;
            this.originTime = originTime;
            this.deliveryTime = originTime + TRANSFER_TIME;
        }

        public int compareTo(Object o) {
            RadioPacket p = (RadioPacket)o;
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
     * Receive a frame from the air. Should be called by the <code>RadioAir</code> and pass data into the
     * <code>RadioController</code>.
     */
    public void receive(RadioPacket f);


    /**
     * Transmit a frame from the controller. Should be called by the <code>RadioController</code> and
     * transmitted into the <code>RadioAir</code>.
     */
    public void transmit(RadioPacket f);

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

    /**
     * get local air implementation
     *
     * @return local air
     */
    public LocalAir getLocalAir();

    /**
     * activate local air, by setting params
     *
     * @param pos node position
     */
    public void activateLocalAir(Position pos);

    public RadioAir getAir();
}
