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
import avrora.Avrora;

/**
 * The <code>Radio</code> interface should be implemented by classes which would like to act as radios and access an
 * instance of the <code>RadioAir</code> interface.
 * @author Daniel Lee
 */
public interface Radio {

    /**
     * Time in ATMega128L cycles it takes for one byte to be sent over the air.
     * Much of the implementation is derived from this constant, so generalizing in the
     * future may require some careful consideration.
     */
    public final static long TRANSFER_TIME = 6106;

    /* How 6106 was calculated:
        19.2 Manchester kBaud / (2 Baud/Bit)= 9.6 kBits/sec
        (9600 bits/sec) / (8 bits/byte) = 1200 bytes/sec
        (7327800 cycles/sec) / (1200 bytes/sec) = 6106 cycles/byte (rounded)
     */


    /**
     * A <code>RadioPacket</code> is an object describing the data transmitted over <code>RadioAir</code> over some
     * period of time.
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
            if (o instanceof RadioPacket) {
                RadioPacket p = (RadioPacket) o;
                if ( p.originTime > originTime ) return -1;
                if ( p.originTime < originTime ) return 1;
                return 0;
            } else {
                throw Avrora.failure("cannot compare RadioPacket to "+o.getClass());
            }
        }

    }

    /**
     * A <code>RadioController</code> is an object installed into a Microcontroller. The recommended implementation
     * is to implement specialized IO registers as inner classes and install them into the Microcontroller. Changes to
     * these specialized registers should initiate appropriate behavior with the radio.
     */
    public interface RadioController {

        /**
         * Installs this Controller into a microcontroller. This should setup the pins, IO registers in such a way
         * that changes to CPU state will make corresponding changes to the RadioController state that will initiate
         * sends and receives if necessary.
         */
        public void install(Microcontroller mcu);

        /** Enable transfers. */
        public void enable();

        /** Disable transfers. */
        public void disable();
    }

    /**
     * Receive a frame from the air. Should be called by the <code>RadioAir</code>
     * and pass data into the <code>RadioController</code>.
     */
    public void receive(RadioPacket f);


    /**
     * Transmit a frame from the controller. Should be called by the
     * <code>RadioController</code> and transmitted into the <code>RadioAir</code>.
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

}
