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

package avrora.sim.mcu;

import avrora.sim.Simulator;
import avrora.sim.radio.Radio;
import avrora.core.InstrPrototype;

/**
 * The <code>Microcontroller</code> interface corresponds to a hardware device that implements the AVR
 * instruction set. This interface contains methods that get commonly needed information about the particular
 * hardware device and and can load programs onto this virtual device.
 *
 * @author Ben L. Titzer
 */
public interface Microcontroller {

    /**
     * The <code>Pin</code> interface encapsulates the notion of a physical pin on the microcontroller chip.
     * It is generally used in wiring up external devices to the microcontroller.
     *
     * @author Ben L. Titzer
     */
    public interface Pin {
        /**
         * The <code>Input</code> interface represents an input pin. When the pin is configured to be an input
         * and the microcontroller attempts to read from this pin, the installed instance of this interface
         * will be called.
         */
        public interface Input {
            /**
             * The <code>enableInput()</code> method is called by the simulator when the program changes the
             * direction of the pin. The device connected to this pin can then take action accordingly.
             */
            public void enableInput();

            /**
             * The <code>disableInput()</code> method is called by the simulator when the program changes the
             * direction of the pin. The device connected to this pin can then take action accordingly.
             */
            public void disableInput();

            /**
             * The <code>read()</code> method is called by the simulator when the program attempts to read the
             * level of the pin. The device can then compute and return the current level of the pin.
             *
             * @return true if the level of the pin is high; false otherwise
             */
            public boolean read();
        }

        /**
         * The <code>Output</code> interface represents an output pin. When the pin is configured to be an
         * output and the microcontroller attempts to wrote to this pin, the installed instance of this
         * interface will be called.
         */
        public interface Output {
            /**
             * The <code>enableOutput()</code> method is called by the simulator when the program changes the
             * direction of the pin. The device connected to this pin can then take action accordingly.
             */
            public void enableOutput();

            /**
             * The <code>disableOutput()</code> method is called by the simulator when the program changes the
             * direction of the pin. The device connected to this pin can then take action accordingly.
             */
            public void disableOutput();

            /**
             * The <code>write()</code> method is called by the simulator when the program writes a logical
             * level to the pin. The device can then take the appropriate action.
             *
             * @param level a boolean representing the logical level of the write
             */
            public void write(boolean level);
        }

        /**
         * The <code>connect()</code> method will connect this pin to the specified input. Attempts by the
         * microcontroller to read from this pin when it is configured as an input will then call this
         * instance's <code>read()</code> method.
         *
         * @param i the <code>Input</code> instance to connect to
         */
        public void connect(Input i);

        /**
         * The <code>connect()</code> method will connect this pin to the specified output. Attempts by the
         * microcontroller to write to this pin when it is configured as an output will then call this
         * instance's <code>write()</code> method.
         *
         * @param o the <code>Output</code> instance to connect to
         */
        public void connect(Output o);
    }

    /**
     * The <code>getSimulator()</code> method gets a simulator instance that is capable of emulating this
     * hardware device.
     *
     * @return a <code>Simulator</code> instance corresponding to this device
     */
    public Simulator getSimulator();

    /**
     * The <code>getPin()</code> method looks up the named pin and returns a reference to that pin. Names of
     * pins should be UPPERCASE. The intended users of this method are external device implementors which
     * connect their devices to the microcontroller through the pins.
     *
     * @param name the name of the pin; for example "PA0" or "OC1A"
     * @return a reference to the <code>Pin</code> object corresponding to the named pin if it exists; null
     *         otherwise
     */
    public Pin getPin(String name);

    /**
     * The <code>getPin()</code> method looks up the specified pin by its number and returns a reference to
     * that pin. The intended users of this method are external device implementors which connect their
     * devices to the microcontroller through the pins.
     *
     * @param num the pin number to look up
     * @return a reference to the <code>Pin</code> object corresponding to the named pin if it exists; null
     *         otherwise
     */
    public Pin getPin(int num);

    // TODO: this method should be deprecated!
    public void setRadio(Radio r);

    // TODO: this method should be deprecated!
    public Radio getRadio();

    /**
     * send to mcu to sleep
     */
    public void sleep();

    /**
     * wake the mcu up
     *
     * @return cycles it takes to wake up
     */
    public int wakeup();

    /**
     * get the current mode of the mcu
     *
     * @return current mode
     */
    public byte getMode();

    /**
     * get the name of the current mode
     *
     * @return name of the current mode
     */
    public String getModeName();

    /**
     * The <code>getHZ()</code> method returns the number of cycles per second at which this hardware device
     * is designed to run.
     *
     * @return the number of cycles per second on this device
     */
    int getHz();

    /**
     * The <code>millisToCycles()</code> method converts the specified number of milliseconds to a cycle
     * count. The conversion factor used is the number of cycles per second of this device. This method serves
     * as a utility so that clients need not do repeated work in converting milliseconds to cycles and back.
     *
     * @param ms a time quantity in milliseconds as a double
     * @return the same time quantity in clock cycles, rounded up to the nearest integer
     */
    long millisToCycles(double ms);

    /**
     * The <code>cyclesToMillis()</code> method converts the specified number of cycles to a time quantity in
     * milliseconds. The conversion factor used is the number of cycles per second of this device. This method
     * serves as a utility so that clients need not do repeated work in converting milliseconds to cycles and
     * back.
     *
     * @param cycles the number of cycles
     * @return the same time quantity in milliseconds
     */
    double cyclesToMillis(long cycles);

    /**
     * The <code>isSupported()</code> method allows a client to query whether a particular instruction is
     * implemented on this hardware device. Older implementations of the AVR instruction set preceded the
     * introduction of certain instructions, and therefore did not support the new instructions.
     *
     * @param i the instruction prototype of the instruction
     * @return true if the specified instruction is supported on this device; false otherwise
     */
    boolean isSupported(InstrPrototype i);

    /**
     * The <code>getPinNumber()</code> method looks up the named pin and returns its number. Names of pins
     * should be UPPERCASE. The intended users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     *
     * @param name the name of the pin; for example "PA0" or "OC1A"
     * @return the number of the pin if it exists; -1 otherwise
     */
    int getPinNumber(String name);

    /**
     * The <code>getProperties()</code> method gets an object that describes the microcontroller
     * including the size of the RAM, EEPROM, flash, etc.
     * @return an instance of the <code>MicrocontrollerProperties</code> class that contains all
     * the relevant information about this microcontroller
     */
    public MicrocontrollerProperties getProperties();

}
