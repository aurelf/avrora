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
 * THIS SOFTWARE IS PROVI�DED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
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

import avrora.sim.mcu.Microcontroller;
import avrora.sim.mcu.ATMega128L;
import avrora.sim.radio.Radio;
import avrora.sim.radio.CC1000Radio;
import avrora.sim.radio.SimpleAir;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.core.Program;
import avrora.core.Instr;
import avrora.core.Register;
import avrora.util.Terminal;

/**
 *
 * The <code>Mica2</code> class is an implementation of the <code>Platform</code>
 * interface that represents both a specific microcontroller and the
 * devices connected to it. This implementation therefore uses the ATMega128L
 * microcontroller and uses LED and Radio devices, etc. The
 * Mica2 class differs from Mica in that it runs the ATMega128L not in compatibility
 * mode. In addition, the CC1000 radio implementation is installed on the Mica2.
 *
 * @author Ben L. Titzer, Daniel Lee
 */
public class Mica2 implements Platform, PlatformFactory {

    protected final Microcontroller mcu;

    protected Radio radio;

    public Mica2() {
        mcu = null;
    }

    private Mica2(Microcontroller m) {
        mcu = m;
        addDevices();
    }

    public Microcontroller getMicrocontroller() {
        return mcu;
    }

    public Platform newPlatform(Program p) {
        return new Mica2(new ATMega128L(false).newMicrocontroller(p));
    }

    // TODO: Merge the LED implementations between Mica and Mica2
    protected class LED implements Microcontroller.Pin.Output {
        protected boolean initialized;
        protected boolean on;

        protected final int colornum;
        protected final String color;

        protected final int nodeId = nodeCount;

        protected LED(int n, String c) {
            colornum = n;
            color = c;
        }

        public void write(boolean level) {
            if (!initialized) {
                initialized = true;
                on = level;
                print();
            } else {
                if (level != on) {
                    on = level;
                    print();
                }
            }
        }

        public void print() {
            Terminal.print(colornum, color);
            Terminal.println("(" + nodeId + "): " + (on ? "on" : "off"));
        }

        public void enableOutput() {
            // do nothing
        }

        public void disableOutput() {
            // do nothing
        }
    }

    // TODO: Consider whether a more elegant solution to determining the node number
    // is available.
    private static int nodeCount = 0;

    protected void addDevices() {
        mcu.getPin("PA0").connect(new LED(Terminal.COLOR_YELLOW, "Yellow"));
        mcu.getPin("PA1").connect(new LED(Terminal.COLOR_GREEN, "Green"));
        mcu.getPin("PA2").connect(new LED(Terminal.COLOR_RED, "Red"));

        // radio
        radio = new CC1000Radio(mcu);

        nodeCount++;
    }

}