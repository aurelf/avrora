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

package avrora.actions;

import avrora.Avrora;
import avrora.sim.mcu.MicrocontrollerFactory;
import avrora.sim.mcu.Microcontrollers;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.platform.Platforms;
import avrora.util.Option;
import avrora.util.StringUtil;
import avrora.util.Terminal;

/**
 * @author Ben L. Titzer
 */
public abstract class SimAction extends Action {
    public final Option.Long ICOUNT = newOption("icount", 0,
            "This option is used to terminate the " +
            "simulation after the specified number of instructions have been executed. " +
            "It is useful for non-terminating programs.");
    public final Option.Long TIMEOUT = newOption("timeout", 0,
            "This option is used to terminate the " +
            "simulation after the specified number of clock cycles have passed. " +
            "It is useful for non-terminating programs and benchmarks.");
    public final Option.Str CHIP = newOption("chip", "atmega128l",
            "This option selects the microcontroller from a library of supported " +
            "microcontroller models.");
    public final Option.Bool SLEEP_STATS = newOption("sleep-statistics", false,
            "This option collects statistics on the sleeping behavior of the program, " +
            "recording the length of sleeping intervals and the total amount of time " +
            "spent in sleep modes.");
    public final Option.Str PLATFORM = newOption("platform", "",
            "This option selects the platform on which the microcontroller is built, " +
            "including the external devices such as LEDs and radio. If the platform " +
            "option is not set, the default platform is the microcontroller specified " +
            "in the \"chip\" option, with no external devices.");

    protected SimAction(String sn, String h) {
        super(sn, h);
    }

    /**
     * The <code>getMicrocontroller()</code> method is used to get the current
     * microcontroller from the library of implemented ones, based on the
     * command line option that was specified (-chip=xyz).
     * @return an instance of <code>MicrocontrollerFactory</code> for the
     * microcontroller specified on the command line.
     */
    protected MicrocontrollerFactory getMicrocontroller() {
        MicrocontrollerFactory mcu = Microcontrollers.getMicrocontroller(CHIP.get());
        if (mcu == null)
            Avrora.userError("Unknown microcontroller", StringUtil.quote(CHIP.get()));
        return mcu;
    }

    /**
     * The <code>getPlatform()</code> method is used to get the current
     * platform from the library of implemented ones, based on the command
     * line option that was specified (-platform=xyz).
     * @return an instance of <code>PlatformFactory</code> for the
     * platform specified on the command line
     */
    protected PlatformFactory getPlatform() {
        String pf = PLATFORM.get();
        if (pf.equals("")) return null;
        PlatformFactory pff = Platforms.getPlatform(pf);
        if (pff == null)
            Avrora.userError("Unknown platform", StringUtil.quote(pf));
        return pff;
    }

    protected void reportQuantity(String name, long val, String units) {
        reportQuantity(name, Long.toString(val), units);
    }

    protected void reportQuantity(String name, float val, String units) {
        reportQuantity(name, Float.toString(val), units);
    }

    protected void reportQuantity(String name, String val, String units) {
        Terminal.printGreen(name);
        Terminal.print(": ");
        Terminal.printBrightCyan(val);
        Terminal.println(" " + units);
    }
}
