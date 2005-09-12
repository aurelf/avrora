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

package avrora.sim.platform;

import avrora.core.Program;
import avrora.sim.InterpreterFactory;
import avrora.sim.Simulator;
import avrora.sim.clock.ClockDomain;
import avrora.sim.mcu.*;
import avrora.sim.platform.sensors.LightSensor;
import avrora.sim.platform.sensors.SensorBoard;
import avrora.sim.radio.CC2420Radio;
import avrora.sim.radio.Radio;
import avrora.util.Terminal;

/**
 * The <code>Mica2</code> class is an implementation of the <code>Platform</code> interface that represents
 * both a specific microcontroller and the devices connected to it. This implementation therefore uses the
 * ATMega128L microcontroller and uses LED and Radio devices, etc. The Mica2 class differs from Mica in that
 * it runs the ATMega128L not in compatibility mode. In addition, the CC1000 radio implementation is installed
 * on the Mica2.
 *
 * @author Ben L. Titzer, Daniel Lee
 */
public class MicaZ extends Platform {

    protected static final int MAIN_HZ = 7372800;

    public static class Factory implements PlatformFactory {

        /**
         * The <code>newPlatform()</code> method is a factory method used to create new instances of the
         * <code>Mica2</code> class.
         * @param id the integer ID of the node
         * @param f the interpreter factory for the node
         * @param p the program to load onto the node
         * @return a new instance of the <code>Mica2</code> platform
         */
        public Platform newPlatform(int id, InterpreterFactory f, Program p) {
            ClockDomain cd = new ClockDomain(MAIN_HZ);
            cd.newClock("external", 32768);

            return new MicaZ(new ATMega128(id, cd, f, p));
        }
    }

    protected final Simulator sim;

    protected Radio radio;
    protected SensorBoard sensorboard;
    protected ExternalFlash externalFlash;
    protected LightSensor lightSensor;

    private MicaZ(Microcontroller m) {
        super(m);
        sim = m.getSimulator();
        addDevices();
    }

    /**
     * The <code>addDevices()</code> method is used to add the external (off-chip) devices to the
     * platform. For the mica2, these include the LEDs, the radio, and the sensor board.
     */
    protected void addDevices() {
        LED yellow = new LED(sim, Terminal.COLOR_YELLOW, "Yellow");
        LED green = new LED(sim, Terminal.COLOR_GREEN, "Green");
        LED red = new LED(sim, Terminal.COLOR_RED, "Red");

        yellow.enablePrinting();
        green.enablePrinting();
        red.enablePrinting();

        mcu.getPin("PA0").connect(yellow);
        mcu.getPin("PA1").connect(green);
        mcu.getPin("PA2").connect(red);

        // radio
        radio = new CC2420Radio(mcu, MAIN_HZ * 2);
        addDevice("radio", radio);
        // sensor board
        sensorboard = new SensorBoard(sim);
        // external flash
        externalFlash = new ExternalFlash(mcu);
        // light sensor
        AtmelMicrocontroller amcu = (AtmelMicrocontroller)mcu;
        lightSensor = new LightSensor(amcu, 1, "PC2", "PE5");
        addDevice("light-sensor", lightSensor);
    }

}