/**
 * Copyright (c) 2005, Regents of the University of California
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
 *
 * Creation date: Sep 12, 2005
 */

package avrora.sim.util;

import avrora.sim.Simulator;
import avrora.sim.clock.Clock;
import cck.text.*;
import cck.util.Util;

/**
 * The <code>SimUtil</code> class encapsulates a set of utility methods that are used in
 * the simulation, including textual utilities, etc.
 *
 * @author Ben L. Titzer
 */
public class SimUtil {

    public static void readError(Simulator sim, String segment, int address) {
        String msg = "illegal read from " + segment + " at address " + StringUtil.addrToString(address);
        int npc = sim.getState().getPC();
        warning(sim, StringUtil.to0xHex(npc, 4), msg);
    }

    public static void writeError(Simulator sim, String segment, int address, byte value) {
        String msg = "illegal write to " + segment + " at address " + StringUtil.addrToString(address);
        int npc = sim.getState().getPC();
        warning(sim, StringUtil.to0xHex(npc, 4), msg);
    }

    /**
     * The <code>Simulator.Printer</code> class is a printer that is tied to a specific <code>Simulator</code>
     * instance. Being tied to this instance, it will always report the node ID and time before printing
     * anything. This simple mechanism allows the output to be much cleaner to track the output
     * of multiple nodes at once.
     */
    public static class SimPrinter {

        /**
         * The <code>enabled</code> field is true when this printer is enabled. When this printer
         * is not enabled, the <code>println()</code> method SHOULD NOT BE CALLED.
         */
        public boolean enabled;
        private Simulator simulator;

        protected SimPrinter(Simulator simulator, String category) {
            this.simulator = simulator;
            Verbose.Printer p = Verbose.getVerbosePrinter(category);
            enabled = p.enabled;
        }

        /**
         * The <code>println()</code> method prints the node ID, the time, and a message to the
         * console, synchronizing with other threads so that output is not interleaved. This method
         * SHOULD ONLY BE CALLED WHEN <code>enabled</code> IS TRUE! This is done to prevent
         * performance bugs created by string construction inside printing (and debugging code).
         * @param s the string to print
         */
        public void println(String s) {
            if (enabled) {
                synchronized ( Terminal.class ) {
                    // synchronize on the terminal to prevent interleaved output
                    StringBuffer buf = new StringBuffer(s.length()+30);
                    SimUtil.getIDTimeString(buf, simulator);
                    buf.append(s);
                    Terminal.println(buf.toString());
                }
            } else {
                throw Util.failure("Disabled printer: performance bug!");
            }
        }
    }

    public static SimPrinter getPrinter(Simulator s, String str) {
        return new SimPrinter(s, str);
    }

    public static void toIDTimeString(StringBuffer buf, int id, Clock clk) {
        buf.append(StringUtil.rightJustify(id, StringUtil.ID_LENGTH));
        buf.append("  ");

        if ( StringUtil.REPORT_SECONDS ) {
            StringBuffer buf2 = new StringBuffer(StringUtil.TIME_LENGTH +1);
            long hz = clk.getHZ();
            long count = clk.getCount();
            long seconds = count / hz;
            long fract = count % hz;
            double f = (double)fract / hz;
            StringUtil.appendSecs(buf2, seconds);
            StringUtil.appendFract(buf2, f, StringUtil.SECONDS_PRECISION);
            buf.append(StringUtil.rightJustify(buf2.toString(), StringUtil.TIME_LENGTH));
        } else {
            buf.append(StringUtil.rightJustify(clk.getCount(), StringUtil.TIME_LENGTH));
        }
        buf.append("  ");
    }

    public static String toIDTimeString(int id, Clock clk) {
        StringBuffer buf = new StringBuffer(40);
        toIDTimeString(buf, id, clk);
        return buf.toString();
    }

    public static String getIDTimeString(Simulator s) {
        return toIDTimeString(s.getID(), s.getClock());
    }

    public static void getIDTimeString(StringBuffer buf, Simulator s) {
        toIDTimeString(buf, s.getID(), s.getClock());
    }

    public static void warning(Simulator s, String w, String m) {
        StringBuffer buf = new StringBuffer(40 + w.length() + m.length());
        SimUtil.getIDTimeString(buf, s);
        Terminal.append(Terminal.WARN_COLOR, buf, w);
        buf.append(": ");
        buf.append(m);
        Terminal.println(buf.toString());
    }



}
