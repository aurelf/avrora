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

import avrora.sim.State;
import avrora.sim.BaseInterpreter;
import avrora.sim.Simulator;
import avrora.sim.Clock;
import avrora.Avrora;

import java.util.Iterator;

/**
 * The <code>InteralDevice</code> class represents an internal device
 * on a microcontroller.
 *
 * @author Ben L. Titzer
 */
public abstract class AtmelInternalDevice {

    public final String name;
    protected final AtmelMicrocontroller microcontroller;
    protected final Simulator simulator;
    protected final BaseInterpreter interpreter;
    protected final Simulator.Printer devicePrinter;
    protected final Clock mainClock;

    public AtmelInternalDevice(String n, AtmelMicrocontroller m) {
        name = n;
        microcontroller = m;
        simulator = m.getSimulator();
        mainClock = simulator.getClock();
        interpreter = simulator.getInterpreter();
        devicePrinter = m.getSimulator().getPrinter("atmel."+n);
    }

    public Iterator getIORegs() {
        throw Avrora.unimplemented();
    }

    protected void installIOReg(String name, State.IOReg reg) {
        microcontroller.installIOReg(name, reg);
    }

    protected void installInterrupt(String name, int addr, Simulator.Interrupt intr) {
        microcontroller.installInterrupt(name, addr, intr);
    }

    /**
     * Helper function to get a 16 bit value from a pair of registers.
     */
    protected static int read16(State.RWIOReg high, State.RWIOReg low) {
        int result = low.read() & 0xff;
        result |= (high.read() & 0xff) << 8;
        return result;
    }

    /**
     * Helper function to write a 16-bit value to a pair of registers.
     */
    protected static void write16(int val, State.RWIOReg high, State.RWIOReg low) {
        high.write((byte)((val & 0xff00) >> 8));
        low.write((byte)(val & 0x00ff));
    }

    /**
     * Helper function to get a hex string representation of a byte.
     */
    protected static String hex(byte val) {
        return Integer.toHexString(0xff & val);
    }

    public Clock getClock() {
        return mainClock;
    }

}
