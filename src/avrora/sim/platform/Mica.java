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


import avrora.core.Program;
import avrora.sim.InterpreterFactory;
import avrora.sim.Simulator;
import avrora.sim.mcu.ATMega128L;
import avrora.sim.mcu.Microcontroller;
import avrora.util.Terminal;

/**
 * The <code>Mica</code> class is an implementation of the <code>Platform</code> interface that represents
 * both a specific microcontroller and the devices connected to it. This implementation therefore uses the
 * ATMega128L microcontroller in compatibility mode and uses LED and Radio devices, etc.
 *
 * @author Ben L. Titzer
 */
public class Mica implements Platform, PlatformFactory {

    protected final Microcontroller mcu;
    protected final Simulator sim;

    public Mica() {
        mcu = null;
        sim = null;
    }

    private Mica(Microcontroller m) {
        mcu = m;
        sim = mcu.getSimulator();
        addDevices();
    }

    public Microcontroller getMicrocontroller() {
        return mcu;
    }

    public Platform newPlatform(int id, InterpreterFactory f, Program p) {
        return new Mica(new ATMega128L(true).newMicrocontroller(id, f, p));
    }

    protected void addDevices() {
        mcu.getPin("PA0").connect(new LED(sim, Terminal.COLOR_YELLOW, "Yellow"));
        mcu.getPin("PA1").connect(new LED(sim, Terminal.COLOR_GREEN, "Green"));
        mcu.getPin("PA2").connect(new LED(sim, Terminal.COLOR_RED, "Red"));

    }

}
