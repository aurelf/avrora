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

import avrora.sim.*;
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
    protected Simulator simulator;
    protected BaseInterpreter interpreter;
    protected Simulator.Printer pinPrinter;

    public final MicrocontrollerProperties properties;

    protected final ClockDomain clockDomain;
    protected final HashMap devices;
    protected final HashMap ioregs;

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

    protected AtmelMicrocontroller(ClockDomain cd, MicrocontrollerProperties p) {
        HZ = cd.getMainClock().getHZ();
        clockDomain = cd;
        mainClock = cd.getMainClock();
        properties = p;
        pins = new Pin[properties.num_pins];
        devices = new HashMap();
        ioregs = new HashMap();
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

    protected void installIOReg(String name, State.IOReg reg) {
        interpreter.setIOReg(properties.getIOReg(name), reg);
    }

    protected void installInterrupt(String name, int num, Simulator.Interrupt interrupt) {
        // TODO: put the interrupt in the hashmap by its name
        simulator.installInterrupt(num, interrupt);
    }

    protected State.IOReg getIOReg(String name) {
        return interpreter.getIOReg(properties.getIOReg(name));
    }

    protected void addDevice(AtmelInternalDevice d) {
        devices.put(d.name, d);
    }

    public AtmelInternalDevice getDevice(String name) {
        AtmelInternalDevice device = (AtmelInternalDevice)devices.get(name);
        if ( device == null )
            throw new NoSuchElementException(StringUtil.quote(name)+" device not found");
        return device;
    }

    public Clock getClock(String name) {
        return clockDomain.getClock(name);
    }

    public Simulator getSimulator() {
        return simulator;
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

    public static void addIOReg(HashMap ioregMap, String n, int i) {
        ioregMap.put(n, new Integer(i));
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

    public int getPinNumber(String n) {
        return properties.getPin(n);
    }

    public MicrocontrollerProperties getProperties() {
        return properties;
    }

    public ClockDomain getClockDomain() {
        return clockDomain;
    }
}
