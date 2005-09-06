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

package avrora.actions;

import avrora.core.*;
import avrora.sim.State;
import avrora.util.*;
import java.util.*;

/**
 * The <code>SimAction</code> is an abstract class that collects many of the options common to single node and
 * multiple-node simulations into one place.
 *
 * @author Ben L. Titzer
 */
public abstract class SimAction extends Action {

    public final Option.Bool REPORT_SECONDS = newOption("report-seconds", false,
            "This option causes all times printed out by the simulator to be reported " +
            "in seconds rather than clock cycles.");
    public final Option.Long SECONDS_PRECISION = newOption("seconds-precision", 6,
            "This option sets the precision (number of decimal places) reported for " +
            "event times in the simulation.");
    public final Option.Str SIMULATION = newOption("simulation", "single",
            "The \"simulation\" option selects from the available simulation types, including a single node " +
            "simulation, a sensor network simulation, or a robotics simulation.");

    protected HashMap monitorListMap;

    protected SimAction(String h) {
        super(h);
        monitorListMap = new HashMap();
    }

    /**
     * The <code>getLocationList()</code> method is to used to parse a list of program locations and turn them
     * into a list of <code>Main.Location</code> instances.
     *
     * @param program the program to look up labels in
     * @param v       the list of strings that are program locations
     * @return a list of program locations
     */
    public static List getLocationList(Program program, List v) {
        HashSet locset = new HashSet(v.size()*2);

        SourceMapping lm = program.getSourceMapping();
        Iterator i = v.iterator();

        while (i.hasNext()) {
            String val = (String)i.next();

            SourceMapping.Location l = lm.getLocation(val);
            if ( l == null )
                Util.userError("Label unknown", val);
            locset.add(l);
        }

        List loclist = Collections.list(Collections.enumeration(locset));
        Collections.sort(loclist, LabelMapping.LOCATION_COMPARATOR);

        return loclist;
    }

    /**
     * The <code>printSimHeader()</code> method simply prints the first line of output that names
     * the columns for the events outputted by the rest of the simulation.
     */
    protected static void printSimHeader() {
        TermUtil.printSeparator(Terminal.MAXLINE, "Simulation events");
        Terminal.printGreen("Node          Time   Event");
        Terminal.nextln();
        TermUtil.printThinSeparator(Terminal.MAXLINE);
    }

    /**
     * The <code>BreakPointException</code> is an exception that is thrown by the simulator before it executes
     * an instruction which has a breakpoint. When this exception is thrown within the simulator, the
     * simulator is left in a state where it is ready to be resumed where it left off by the
     * <code>start()</code> method. When resuming, the breakpointed instruction will not cause a second
     * <code>BreakPointException</code> until the the instruction is executed a second time.
     *
     * @author Ben L. Titzer
     */
    public static class BreakPointException extends RuntimeException {
        /**
         * The <code>address</code> field stores the address of the instruction that caused the breakpoint.
         */
        public final int address;

        /**
         * The <code>state</code> field stores a reference to the state of the simulator when the breakpoint
         * occurred, before executing the instruction.
         */
        public final State state;

        public BreakPointException(int a, State s) {
            super("breakpoint @ " + StringUtil.addrToString(a) + " reached");
            address = a;
            state = s;
        }
    }

    /**
     * The <code>TimeoutException</code> is thrown by the simulator when a timeout reaches zero. Timeouts can
     * be used to ensure termination of the simulator during testing, and implementing timestepping in
     * surrounding tools such as interactive debuggers or visualizers.
     * <p/>
     * When the exception is thrown, the simulator is left in a state that is safe to be resumed by a
     * <code>start()</code> call.
     *
     * @author Ben L. Titzer
     */
    public static class TimeoutException extends RuntimeException {

        /**
         * The <code>address</code> field stores the address of the next instruction to be executed after the
         * timeout.
         */
        public final int address;

        /**
         * The <code>state</code> field stores the state of the simulation at the point at which the timeout
         * occurred.
         */
        public final State state;

        /**
         * The <code>timeout</code> field stores the value (in clock cycles) of the timeout that occurred.
         */
        public final long timeout;

        public TimeoutException(int a, State s, long t, String l) {
            super("timeout @ " + StringUtil.addrToString(a) + " reached after " + t + ' ' + l);
            address = a;
            state = s;
            timeout = t;
        }
    }
}
