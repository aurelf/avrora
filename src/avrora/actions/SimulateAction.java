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

import avrora.Defaults;
import avrora.monitors.Monitor;
import avrora.sim.Simulation;
import avrora.sim.Simulator;
import avrora.sim.util.SimUtil;
import cck.text.*;
import cck.util.Util;
import cck.util.TimeUtil;
import java.util.Iterator;

/**
 * The <code>SimulateAction</code> implements the bridge between the functionality in the
 * <code>avrora.sim</code> package and the entrypoint to Avrora in <code>avrora.Main</code>. This class has a
 * <code>run()</code> method that is called by the main class after the options have been processed. The
 * <code>run()</code> reads in the program, processes breakpoints, profiling counters, and other options, and
 * begins the simulation.
 *
 * @author Ben L. Titzer
 */
public class SimulateAction extends SimAction {

    public static final String HELP = "The \"simulate\" action creates a simulation with the specified program(s) " +
            "for the specified node(s). The simulation type might be as simple as a single node with a single " +
            "program, or a multiple-node sensor network simulation or robotics simulation.";

    public SimulateAction() {
        super(HELP);
    }

    /**
     * The <code>run()</code> method is called by the main class.
     *
     * @param args the command line arguments after the options have been stripped out
     * @throws Exception if there is a problem loading the program, or an exception occurs during
     *                             simulation
     */
    public void run(String[] args) throws Exception {
        SimUtil.REPORT_SECONDS = REPORT_SECONDS.get();
        SimUtil.SECONDS_PRECISION = (int)SECONDS_PRECISION.get();

        Simulation sim = Defaults.getSimulation(SIMULATION.get());
        sim.process(options, args);

        printSimHeader();
        long startms = System.currentTimeMillis();
        try {
            sim.start();
            sim.join();
        } catch (BreakPointException e) {
            Terminal.printYellow("Simulation terminated");
            Terminal.println(": breakpoint at " + StringUtil.addrToString(e.address) + " reached.");
        } catch (TimeoutException e) {
            Terminal.printYellow("Simulation terminated");
            Terminal.println(": timeout reached at pc = " + StringUtil.addrToString(e.address) + ", time = " + e.state.getCycles());
        } catch (Util.Error e) {
            Terminal.printRed("Simulation terminated");
            Terminal.print(": ");
            e.report();
        } catch (Throwable t) {
            Terminal.printRed("Simulation terminated with unexpected exception");
            Terminal.print(": ");
            t.printStackTrace();
        } finally {
            TermUtil.printSeparator();
            long endms = System.currentTimeMillis();

            reportTime(sim, endms - startms);
            reportMonitors(sim);
        }
    }

    protected static void reportMonitors(Simulation sim) {
        Iterator i = sim.getNodeIterator();
        while (i.hasNext()) {
            Simulation.Node n = (Simulation.Node)i.next();
            Iterator im = n.getMonitors().iterator();
            if ( im.hasNext() )
                TermUtil.printSeparator(Terminal.MAXLINE, "Monitors for node "+n.id);
            while ( im.hasNext() ) {
                Monitor m = (Monitor)im.next();
                m.report();
            }
        }
    }

    protected static void reportTime(Simulation sim, long diff) {
        // calculate total throughput over all threads
        Iterator i = sim.getNodeIterator();
        long aggCycles = 0;
        long maxCycles = 0;
        while ( i.hasNext() ) {
            Simulation.Node n = (Simulation.Node)i.next();
            Simulator simulator = n.getSimulator();
            if ( simulator == null ) continue;
            long count = simulator.getClock().getCount();
            aggCycles += count;
            if ( count > maxCycles ) maxCycles = count;
        }
        TermUtil.reportQuantity("Simulated time", maxCycles, "cycles");
        TermUtil.reportQuantity("Time for simulation", TimeUtil.milliToSecs(diff), "seconds");
        int nn = sim.getNumberOfNodes();
        double thru = ((double)aggCycles) / (diff * 1000);
        TermUtil.reportQuantity("Total throughput", (float)thru, "mhz");
        if ( nn > 1 )
            TermUtil.reportQuantity("Throughput per node", (float)(thru / nn), "mhz");
    }
}
