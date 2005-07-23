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

import avrora.sim.ActiveRegister;
import avrora.sim.BaseInterpreter;
import avrora.sim.FiniteStateMachine;
import avrora.sim.Simulator;
import avrora.sim.clock.Clock;
import avrora.sim.clock.ClockDomain;
import avrora.sim.clock.MainClock;
import avrora.sim.platform.Platform;
import avrora.util.StringUtil;

import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * The <code>AtmelMicrocontroller</code> class represents the common functionality among microcontrollers
 * for the Atmel series. These all contain a clock domain (collection of internal, external clocks), a
 * simulator, an interpreter, microcontroller properties, and a mapping between string names and IO reg
 * addresses, etc.
 *
 * @author Ben L. Titzer
 */
public abstract class AtmelMicrocontroller implements Microcontroller {
    public final long HZ;

    protected final Microcontroller.Pin[] pins;
    protected final MainClock mainClock;
    protected final RegisterSet registers;
    protected Platform platform;
    protected Simulator simulator;
    protected BaseInterpreter interpreter;
    protected Simulator.Printer pinPrinter;


    public final MicrocontrollerProperties properties;

    protected final ClockDomain clockDomain;
    protected final HashMap devices;
    public static final int MODE_ACTIVE = 0;
    protected final FiniteStateMachine sleepState;

    /**
     * The <code>sleep()</code> method is called by the interpreter when the program executes a SLEEP
     * instruction. This method transitions the microcontroller into a sleep mode, including turning
     * off any devices, shutting down clocks, and transitioning the sleep FSM into a sleep mode.
     *
     * @see Microcontroller#sleep()
     */
    public void sleep() {
        // transition to the sleep state in the MCUCR register
        sleepState.transition(getSleepMode());
    }

    protected abstract int getSleepMode();

    /**
     * The <code>wakeup()</code> method is called by the interpreter when the microcontroller is
     * woken from a sleep mode by an interrupt or other event. This method transitions the
     * microcontroller back into active mode, turning back on devices. This method returns
     * the number of clock cycles necessary to wake the MCU from sleep.
     *
     * @return cycles it takes to wake up
     * @see Microcontroller#wakeup()
     */
    public int wakeup() {
        // transition to the active state (may insert transition event into event queue)
        sleepState.transition(MODE_ACTIVE);
        // return the number of cycles consumed by waking up
        return sleepState.getTransitionTime(sleepState.getCurrentState(), MODE_ACTIVE);
    }

    /**
     * The <code>getMode()</code> method returns the current sleep mode of the MCU.
     *
     * @return current mode
     * @see Microcontroller#getMode()
     */
    public byte getMode() {
        return (byte)sleepState.getCurrentState();
    }

    /**
     * get the name of the current mode
     *
     * @return name of the current mode
     */
    public String getModeName() {
        return sleepState.getCurrentStateName();
    }

    /**
     * The <code>getFSM()</code> method gets a reference to the finite state machine that represents
     * the sleep modes of the MCU. The finite state machine allows probing of the sleep mode transitions.
     * @return a reference to the finite state machine representing the sleep mode of the MCU
     */
    public FiniteStateMachine getFSM() {
        return sleepState;
    }

    /**
     * The <code>Pin</code> class implements a model of a pin on the ATMegaFamily for the general purpose IO
     * ports.
     */
    protected class Pin implements Microcontroller.Pin {
        protected final int number;

        boolean level;
        boolean outputDir;
        boolean pullup;

        Microcontroller.Pin.Input input;
        Microcontroller.Pin.Output output;

        protected Pin(int num) {
            number = num;
        }

        public void connect(Output o) {
            output = o;
        }

        public void connect(Input i) {
            input = i;
        }

        protected void setOutputDir(boolean out) {
            outputDir = out;
            if (out) write(level);
        }

        protected void setPullup(boolean pull) {
            pullup = pull;
        }

        protected boolean read() {
            boolean result;
            if (!outputDir) {
                if (input != null)
                    result = input.read();
                else
                    result = pullup;

            } else {
                result = level;
            }
            // print the result of the read
            printRead(result);
            return result;
        }

        private void printRead(boolean result) {
            if (pinPrinter == null) pinPrinter = simulator.getPrinter("mcu.pin");
            if (pinPrinter.enabled) {
                String dir = getDirection();
                pinPrinter.println("READ PIN: " + number + ' ' + dir + "<- " + result);
            }
        }

        private String getDirection() {
            if (!outputDir) {
                if (input != null)
                    return "[input] ";
                else
                    return "[pullup:" + pullup + "] ";

            } else {
                return "[output] ";
            }
        }

        protected void write(boolean value) {
            level = value;
            // print the write
            printWrite(value);
            if (outputDir && output != null) output.write(value);
        }

        private void printWrite(boolean value) {
            if (pinPrinter == null) pinPrinter = simulator.getPrinter("mcu.pin");
            if (pinPrinter.enabled) {
                String dir = getDirection();
                pinPrinter.println("WRITE PIN: " + number + ' ' + dir + "-> " + value);
            }
        }
    }

    protected AtmelMicrocontroller(ClockDomain cd, MicrocontrollerProperties p, FiniteStateMachine fsm) {
        HZ = cd.getMainClock().getHZ();
        sleepState = fsm;
        clockDomain = cd;
        mainClock = cd.getMainClock();
        properties = p;
        pins = new Pin[properties.num_pins];
        registers = p.getRegisterLayout().instantiate();
        devices = new HashMap();
    }

    /**
     * The <code>getRegisterSet()</code> method gets a reference to the register set of the microcontroller.
     * The register set contains all of the IO registers for this microcontroller.
     *
     * @return a reference to the register set of this microcontroller instance
     */
    public RegisterSet getRegisterSet() {
        return registers;
    }

    /**
     * The <code>millisToCycles()</code> method converts the specified number of milliseconds to a cycle
     * count. The conversion factor used is the number of cycles per second of this device. This method serves
     * as a utility so that clients need not do repeated work in converting milliseconds to cycles and back.
     *
     * @param ms a time quantity in milliseconds as a double
     * @return the same time quantity in clock cycles, rounded up to the nearest integer
     */
    public long millisToCycles(double ms) {
        return (long)(ms * HZ / 1000);
    }

    /**
     * The <code>cyclesToMillis()</code> method converts the specified number of cycles to a time quantity in
     * milliseconds. The conversion factor used is the number of cycles per second of this device. This method
     * serves as a utility so that clients need not do repeated work in converting milliseconds to cycles and
     * back.
     *
     * @param cycles the number of cycles
     * @return the same time quantity in milliseconds
     */
    public double cyclesToMillis(long cycles) {
        return 1000 * ((double)cycles) / HZ;
    }

    /**
     * The <code>getPin()</code> method looks up the specified pin by its number and returns a reference to
     * that pin. The intended users of this method are external device implementors which connect their
     * devices to the microcontroller through the pins.
     *
     * @param num the pin number to look up
     * @return a reference to the <code>Pin</code> object corresponding to the named pin if it exists; null
     *         otherwise
     */
    public Microcontroller.Pin getPin(int num) {
        if (num < 0 || num > pins.length) return null;
        return pins[num];
    }

    /**
     * The <code>installIOReg()</code> method installs an IO register with the specified name. The register
     * layout for this microcontroller is used to get the address of the register (if it exists) and
     * install the <code>ActiveRegister</code> object into the correct place.
     * @param name the name of the IO register as a string
     * @param reg the register to install
     */
    protected void installIOReg(String name, ActiveRegister reg) {
        interpreter.installIOReg(properties.getIOReg(name), reg);
    }

    /**
     * The <code>getIOReg()</code> method gets a reference to the active register currently installed for
     * the specified name. The register layout for this microcontroller is used to get the correct address.
     * @param name the name of the IO register as a string
     * @return a reference to the active register object if it exists
     */
    protected ActiveRegister getIOReg(String name) {
        return interpreter.getIOReg(properties.getIOReg(name));
    }

    /**
     * The <code>addDevice()</code> method adds a new internal device to this microcontroller so that it can
     * be retrieved later with <code>getDevice()</code>
     * @param d the device to add to this microcontroller
     */
    protected void addDevice(AtmelInternalDevice d) {
        devices.put(d.name, d);
    }

    /**
     * The <code>getDevice()</code> method is used to get a reference to an internal device with the given name.
     * For example, the ADC device will be under the name "adc" and Timer0 will be under the name "timer0". This
     * is useful for external devices that need to connect to the input of internal devices.
     *
     * @param name the name of the internal device as a string
     * @return a reference to the internal device if it exists
     * @throws NoSuchElementException if no device with that name exists
     */
    public AtmelInternalDevice getDevice(String name) {
        AtmelInternalDevice device = (AtmelInternalDevice)devices.get(name);
        if ( device == null )
            throw new NoSuchElementException(StringUtil.quote(name)+" device not found");
        return device;
    }

    /**
     * The <code>getClock()</code> method gets a reference to a specific clock on this device. For example,
     * the external clock, or a specific device's clock can be accessed by specifying its name.
     * @param name the name of the clock to get
     * @return a reference to the <code>Clock</code> instance for the specified clock if it exists
     */
    public Clock getClock(String name) {
        return clockDomain.getClock(name);
    }

    /**
     * The <code>getSimulator()</code> method gets a reference to the simulator for this microcontroller instance.
     * @return a reference to the simulator instance for this microcontroller
     */
    public Simulator getSimulator() {
        return simulator;
    }

    /**
     * The <code>getPlatform()</code> method returns the platform for this microcontroller.
     * @return the platform instance containing this microcontroller
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * The <code>setPlatform()</code> method sets the platform instance for this microcontroller
     * @param p the platform instance associated with this microcontroller
     */
    public void setPlatform(Platform p) {
        platform = p;
    }

    public static void addPin(HashMap pinMap, int p, String n) {
        pinMap.put(n, new Integer(p));
    }

    public static void addPin(HashMap pinMap, int p, String n1, String n2) {
        Integer i = new Integer(p);
        pinMap.put(n1, i);
        pinMap.put(n2, i);
    }

    public static void addPin(HashMap pinMap, int p, String n1, String n2, String n3) {
        Integer i = new Integer(p);
        pinMap.put(n1, i);
        pinMap.put(n2, i);
        pinMap.put(n3, i);
    }

    public static void addInterrupt(HashMap iMap, String n, int i) {
        iMap.put(n, new Integer(i));
    }

    /**
     * The <code>getPin()</code> method looks up the named pin and returns a reference to that pin. Names of
     * pins should be UPPERCASE. The intended users of this method are external device implementors which
     * connect their devices to the microcontroller through the pins.
     *
     * @param n the name of the pin; for example "PA0" or "OC1A"
     * @return a reference to the <code>Pin</code> object corresponding to the named pin if it exists; null
     *         otherwise
     */
    public Microcontroller.Pin getPin(String n) {
        return pins[properties.getPin(n)];
    }

    /**
     * The <code>getPinNumber()</code> method gets the pin number (according to the pin assignments) for the
     * pin with the specified name.
     * @param n the name of the pin as a string
     * @return the number of the pin if it exists
     */
    public int getPinNumber(String n) {
        return properties.getPin(n);
    }

    /**
     * The <code>getProperties()</code> method gets a reference to the microcontroller properties for this
     * microcontroller instance.
     * @return a reference to the microcontroller properties for this instance
     */
    public MicrocontrollerProperties getProperties() {
        return properties;
    }

    /**
     * The <code>getClockDomain()</code> method gets a reference to the <code>ClockDomain</code> instance for
     * this node that contains the main clock and any derived clocks for this microcontroller.
     * @return a reference to the clock domain for this microcontroller
     */
    public ClockDomain getClockDomain() {
        return clockDomain;
    }
}
