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

import avrora.Main;
import avrora.Avrora;
import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.mcu.Microcontroller;
import avrora.util.Option;
import avrora.util.StringUtil;
import avrora.util.Terminal;
import avrora.util.Visual;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
    Program program;
    Simulator simulator;
    Microcontroller microcontroller;
    avrora.sim.util.Counter total;
    long startms, endms;

    List counters;
    List branchcounters;

    public static final String HELP = "The \"simulate\" action launches a simulator with the specified program " +
            "for the specified microcontroller and begins executing the program. There " +
            "are several options provided by the simulator for profiling and analysis.";
    public final Option.List BREAKS = newOptionList("breakpoint", "",
            "This option is used in the simulate action. It allows the user to " +
            "insert a series of breakpoints in the program from the command line. " +
            "The address of the breakpoint can be given in hexadecimal or as a label " +
            "within the program. Hexadecimal constants are denoted by a leading '$'.");
    public final Option.Bool TIME = newOption("time", true,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the time used in executing the simulation. When combined with " +
            "the \"cycles\" and \"total\" options, it will report performance " +
            "information about the simulation.");
    public final Option.Bool TOTAL = newOption("total", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the total instructions executed in the simulation. When combined " +
            "with the \"time\" option, it will report performance information.");
    public final Option.Bool CYCLES = newOption("cycles", true,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the total cycles executed in the simulation. When combined " +
            "with the \"time\" option, it will report performance information.");
    public final Option.Bool REALTIME = newOption("real-time", false,
            "This option is used in the simulate action to slow the simulation if it is too fast. " +
            "By default, the simulator will attempt to execute the program as fast as possible. " +
            "This option will cause the simulation to pause periodically for a few milliseconds in " +
            "order that it does not run faster than real-time.");

    public SimulateAction() {
        super("simulate", HELP);
    }

    private class Counter extends avrora.sim.util.Counter {
        private final Program.Location location;

        Counter(Program.Location loc) {
            location = loc;
        }

        public void report() {
            String cnt = StringUtil.rightJustify(count, 8);
            String addr = StringUtil.addrToString(location.address);
            String name;
            if (location.name != null)
                name = "    " + location.name + " @ " + addr;
            else
                name = "    " + addr;
            reportQuantity(name, cnt, "");
        }
    }

    private class BranchCounter extends avrora.sim.util.BranchCounter {
        private final Program.Location location;

        BranchCounter(Program.Location loc) {
            location = loc;
        }

        public void report() {
            String tcnt = StringUtil.rightJustify(takenCount, 8);
            String ntcnt = StringUtil.rightJustify(nottakenCount, 8);
            String addr = StringUtil.addrToString(location.address);
            String name;
            if (location.name != null)
                name = "    " + location.name + " @ " + addr;
            else
                name = "    " + addr;
            reportQuantity(name, tcnt + ' ' + ntcnt, "taken/not taken");
        }
    }

    private class ThrottleEvent implements Simulator.Event {
        long lastMs;

        public void fire() {
            long newMs = System.currentTimeMillis();
            long diff = newMs - lastMs;
            try {
                if (diff < 10) {
                    Thread.sleep(10 - diff);
                    newMs = System.currentTimeMillis();
                }
            } catch (InterruptedException e) {
                // interrupt exceptions are dumb.
            }
            lastMs = newMs;
            simulator.insertEvent(this, microcontroller.getHz() / 100);
        }
    }

    /**
     * The <code>run()</code> method is called by the main class.
     *
     * @param args the command line arguments after the options have been stripped out
     * @throws java.lang.Exception if there is a problem loading the program, or an exception occurs during
     *                             simulation
     */
    public void run(String[] args) throws Exception {
        initializeSimulatorStatics();
        runSimulation(args);
    }

    private void runSimulation(String[] args) throws Exception {
        program = Main.readProgram(args);

        simulator = newSimulator(program);
        microcontroller = simulator.getMicrocontroller();
        counters = new LinkedList();
        branchcounters = new LinkedList();

        processBreakPoints();
        processTotal();

        if (REALTIME.get())
            simulator.insertEvent(new ThrottleEvent(), microcontroller.getHz() / 100);

        String visual = VISUAL.get();
        if (!"".equals(visual)) {
            //visualisation is turned on
            Visual.connect(visual);
        }

        startms = System.currentTimeMillis();
        try {
            printSimHeader();
            simulator.start();
        } catch (Simulator.BreakPointException e) {
            Terminal.printYellow("Simulation terminated");
            Terminal.println(": breakpoint at " + StringUtil.addrToString(e.address) + " reached.");
        } catch (Simulator.TimeoutException e) {
            Terminal.printYellow("Simulation terminated");
            Terminal.println(": timeout reached at pc = " + StringUtil.addrToString(e.address) + ", time = " + simulator.getClock().getCount());
        } catch (Avrora.Error e) {
            Terminal.printRed("Simulation terminated");
            Terminal.print(": ");
            e.report();
        } catch (Throwable t) {
            Terminal.printRed("Simulation terminated with unexpected exception");
            Terminal.print(": ");
            t.printStackTrace();
        } finally {
            printSeparator();
            endms = System.currentTimeMillis();

            reportTotal();
            reportCycles();
            reportTime();
            reportMonitors(simulator);
        }
    }

    void processBreakPoints() {
        Iterator i = getLocationList(program, BREAKS.get()).iterator();
        while (i.hasNext()) {
            Program.Location l = (Program.Location)i.next();
            simulator.insertBreakPoint(l.address);
        }
    }

    void processTotal() {
        if (TOTAL.get()) {
            simulator.insertProbe(total = new avrora.sim.util.Counter());
        }
    }

    void reportTotal() {
        if (total != null)
            reportQuantity("Instructions executed", total.count, "");
    }

    void reportCycles() {
        if (CYCLES.get()) {
            reportQuantity("Simulated time", simulator.getState().getCycles(), "cycles");
        }
    }

    void reportTime() {
        long diff = endms - startms;
        if (TIME.get()) {
            reportQuantity("Time for simulation", StringUtil.milliToSecs(diff), "seconds");
            if (total != null) {
                float thru = ((float)total.count) / (diff * 1000);
                reportQuantity("Simulator throughput", thru, "mips");
            }
            if (CYCLES.get()) {
                float thru = ((float)simulator.getState().getCycles()) / (diff * 1000);
                reportQuantity("Simulator throughput", thru, "mhz");
            }
        }
    }
}
