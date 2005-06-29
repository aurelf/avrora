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

package avrora.sim.platform.sensors;

import avrora.sim.mcu.Microcontroller;
import avrora.sim.mcu.ADC;
import avrora.sim.mcu.AtmelMicrocontroller;
import avrora.sim.FiniteStateMachine;
import avrora.sim.platform.sensors.SensorData;

/**
 * @author Ben L. Titzer
 */
public class LightSensor {

    protected final AtmelMicrocontroller mcu;
    protected final int channel;
    protected final SensorData data;

    protected final FiniteStateMachine fsm;

    protected static final String[] names = { "power down", "off", "on" };
    protected boolean power;
    protected boolean on;

    public LightSensor(AtmelMicrocontroller m, int adcChannel, String onPin, String powPin, SensorData sd) {
        mcu = m;
        channel = adcChannel;
        data = sd;
        mcu.getPin(onPin).connect(new OnPin());
        mcu.getPin(powPin).connect(new PowerPin());
        fsm = new FiniteStateMachine(mcu.getClockDomain().getMainClock(), 0, names, 0);
        ADC adc = (ADC)mcu.getDevice("adc");
        adc.connectADCInput(new ADCInput(), channel);
    }

    class OnPin extends Microcontroller.OutputPin {
        public void write(boolean val) {
            // TODO: is there an inverter?
            on = !val;
            fsm.transition(state());
        }
    }

    class PowerPin extends Microcontroller.OutputPin {
        public void write(boolean val) {
            power = val;
            fsm.transition(state());
        }
    }

    private int state() {
        if ( !power ) return 0;
        if ( !on ) return 1;
        else return 2;
    }

    class ADCInput implements ADC.ADCInput {
        public int getLevel() {
            if ( power && on ) {
                return data.reading();
            } else {
                return ADC.GND_LEVEL;
            }
        }
    }
}
