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

package avrora.sim.mcu;

import avrora.sim.Simulator;

/**
 * The <code>Microcontroller</code> interface corresponds to a hardware device
 * that implements the AVR instruction set. This interface contains methods that
 * get commonly needed information about the particular hardware device and
 * and can load programs onto this virtual device.
 *
 * @author Ben L. Titzer
 */
public interface Microcontroller extends MicrocontrollerProperties {

    /**
     * The <code>Pin</code> interface encapsulates the notion of a physical
     * pin on the microcontroller chip. It is generally used in wiring up
     * external devices to the microcontroller.
     *
     * @author Ben L. Titzer
     */
    public interface Pin {
        /**
         * The <code>Input</code> interface represents an input pin. When the
         * pin is configured to be an input and the microcontroller attempts
         * to read from this pin, the installed instance of this interface
         * will be called.
         */
        public interface Input {
            /**
             * The <code>enableInput()</code> method is called by the simulator
             * when the program changes the direction of the pin. The device
             * connected to this pin can then take action accordingly.
             */
            public void enableInput();

            /**
             * The <code>disableInput()</code> method is called by the simulator
             * when the program changes the direction of the pin. The device
             * connected to this pin can then take action accordingly.
             */
            public void disableInput();

            /**
             * The <code>read()</code> method is called by the simulator when
             * the program attempts to read the level of the pin. The device
             * can then compute and return the current level of the pin.
             * @return true if the level of the pin is high; false otherwise
             */
            public boolean read();
        }

        /**
         * The <code>Output</code> interface represents an output pin. When the
         * pin is configured to be an output and the microcontroller attempts
         * to wrote to this pin, the installed instance of this interface
         * will be called.
         */
        public interface Output {
            /**
             * The <code>enableOutput()</code> method is called by the simulator
             * when the program changes the direction of the pin. The device
             * connected to this pin can then take action accordingly.
             */
            public void enableOutput();

            /**
             * The <code>disableOutput()</code> method is called by the simulator
             * when the program changes the direction of the pin. The device
             * connected to this pin can then take action accordingly.
             */
            public void disableOutput();

            /**
             * The <code>write()</code> method is called by the simulator when
             * the program writes a logical level to the pin. The device can then
             * take the appropriate action.
             * @param level a boolean representing the logical level of the write
             */
            public void write(boolean level);
        }

        /**
         * The <code>connect()</code> method will connect this pin to the
         * specified input. Attempts by the microcontroller to read from this
         * pin when it is configured as an input will then call this instance's
         * <code>read()</code> method.
         *
         * @param i the <code>Input</code> instance to connect to
         */
        public void connect(Input i);

        /**
         * The <code>connect()</code> method will connect this pin to the
         * specified output. Attempts by the microcontroller to write to this
         * pin when it is configured as an output will then call this instance's
         * <code>write()</code> method.
         *
         * @param o the <code>Output</code> instance to connect to
         */
        public void connect(Output o);
    }

    /**
     * The <code>getSimulator()</code> method gets a simulator instance that is
     * capable of emulating this hardware device.
     *
     * @return a <code>Simulator</code> instance corresponding to this
     *         device
     */
    public Simulator getSimulator();

    /**
     * The <code>getPin()</code> method looks up the named pin and returns a
     * reference to that pin. Names of pins should be UPPERCASE. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     *
     * @param name the name of the pin; for example "PA0" or "OC1A"
     * @return a reference to the <code>Pin</code> object corresponding to
     *         the named pin if it exists; null otherwise
     */
    public Pin getPin(String name);

    /**
     * The <code>getPin()</code> method looks up the specified pin by its number
     * and returns a reference to that pin. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     *
     * @param num the pin number to look up
     * @return a reference to the <code>Pin</code> object corresponding to
     *         the named pin if it exists; null otherwise
     */
    public Pin getPin(int num);
}
