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

package avrora.sim.platform;

import avrora.sim.Energy;
import avrora.sim.Simulator;
import avrora.sim.mcu.Microcontroller;
import avrora.util.StringUtil;
import avrora.util.Terminal;

/**
 * The <code>LED</code> class implements an LED (light emitting diode) that can be hooked up
 * to a pin on the microcontroller. The LED prints its state when it is initialized and each
 * time it is turned on or off.
 *
 * @author Ben L. Titzer
 */
class LED implements Microcontroller.Pin.Output {
    protected boolean initialized;
    protected boolean on;
    protected Simulator sim;

    protected final int colornum;
    protected final String color;

    //energy profile of this device
    private Energy energy;
    // names of the states of this device
    private final String modeName[] = {"off: ", "on:  "};
    // power consumption of the device states
    private final double modeAmphere[] = {0.0, 0.0022};
    // default mode of the device is off
    private static final int startMode = 0;

    protected LED(Simulator s, int n, String c) {
        sim = s;
        colornum = n;
        color = c;
        //setup energy recording
        energy = new Energy(color, modeAmphere, modeName, sim.getMicrocontroller().getHz(), startMode, sim.getEnergyControl(), sim.getState());
    }

    public void write(boolean level) {
        // NOTE: there is an inverter between the port and the LED!
        // reverse the level!
        if (!initialized) {
            initialized = true;
            on = !level;
            if (on)
                energy.setMode(1);
            else
                energy.setMode(0);
            print();
        } else {
            if (level == on) {
                on = !level;
                if (on)
                    energy.setMode(1);
                else
                    energy.setMode(0);
                print();
            }
        }
    }

    public void print() {
        synchronized ( Terminal.class ) {
            // synchronize on the terminal to prevent interleaved output
            Terminal.print(sim.getIDTimeString());
            Terminal.print(colornum, color);
            Terminal.println(": " + (on ? "on" : "off"));
        }
    }

    public void enableOutput() {
        // do nothing
    }

    public void disableOutput() {
        // do nothing
    }
}
