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

package avrora.sim;

import avrora.Main;
import avrora.core.Program;
import avrora.core.Instr;
import avrora.sim.util.ProgramProfiler;
import avrora.sim.platform.PlatformFactory;
import avrora.util.StringUtil;
import avrora.util.Terminal;
import avrora.util.Verbose;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>SimulateAction</code> implements the bridge between the functionality
 * in the <code>avrora.sim</code> package and the entrypoint to Avrora in
 * <code>avrora.Main</code>. This class has a <code>run()</code> method that is
 * called by the main class after the options have been processed. The <code>run()</code>
 * reads in the program, processes breakpoints, profiling counters, and other
 * options, and begins the simulation.
 *
 * @author Ben L. Titzer
 */
public class BenchmarkAction extends Main.Action {
    Program program;
    Simulator simulator;
    avrora.sim.util.Counter total;
    long startms, endms;

    long programCycles;
    long totalCyclesA, totalCyclesB;
    long totalMillisA, totalMillisB;

    /**
     * The <code>run()</code> method is called by the main class.
     *
     * @param args the command line arguments after the options have been stripped out
     * @throws Exception if there is a problem loading the program, or an exception
     *                   occurs during simulation
     */
    public void run(String[] args) throws Exception {
        Main.ProgramReader r = Main.getProgramReader();
        program = r.read(args);

        long repeat = Main.REPEAT.get();

        Terminal.printGreen("Legacy Interpreter                           Generated Interpreter");
        Terminal.nextln();
        Terminal.printSeparator(88);

        for ( long cntr = 0; cntr < repeat; cntr++ ) {

            long millisA = runOne(true);
            totalMillisA += millisA;
            long cyclesA = simulator.getState().getCycles();
            totalCyclesA += cyclesA;

            long millisB = runOne(false);
            totalMillisB += millisB;
            long cyclesB = simulator.getState().getCycles();
            totalCyclesB += cyclesB;

            report(cyclesA, millisA, cyclesB, millisB);
        }

        Terminal.printSeparator(88);

        long avgCyclesA = totalCyclesA / repeat;
        long avgMillisA = totalMillisA / repeat;
        float mhzA = reportResult(avgCyclesA, avgMillisA);
        long avgCyclesB = totalCyclesB / repeat;
        long avgMillisB = totalMillisB / repeat;
        float mhzB = reportResult(avgCyclesB, avgMillisB);
        Terminal.nextln();
        Terminal.nextln();
        if ( mhzA > mhzB ) {
            Terminal.printGreen("Average slowdown: ");
        }
        else {
            Terminal.printGreen("Average speedup: ");
        }
        float speedup = 100*((mhzB / mhzA) - 1);
        Terminal.printBrightCyan(speedup+" ");
        Terminal.println("%");
        Terminal.println("");
    }

    private void report(long cyclesA, long millisA, long cyclesB, long millisB) {
        reportResult(cyclesA, millisA);
        reportResult(cyclesB, millisB);

        Terminal.nextln();
    }

    private float reportResult(long cyclesA, long millisA) {
        String cstrA = StringUtil.rightJustify(cyclesA, 9);
        String timA = StringUtil.rightJustify(StringUtil.milliAsString(millisA),8);
        float mhzA = ((float) cyclesA) / (millisA * 1000);
        String mstrA = StringUtil.rightJustify(mhzA, 9);

        Terminal.printBrightCyan(cstrA+" ");
        Terminal.print("cycles  ");
        Terminal.printBrightCyan(timA+" ");
        Terminal.print("  ");
        Terminal.printBrightCyan(mstrA+" ");
        Terminal.print("mhz  ");
        return mhzA;
    }

    private long runOne(boolean legacy) {
        long startms = System.currentTimeMillis();
        try {
            Simulator.LEGACY_INTERPRETER = legacy;
            PlatformFactory pf = Main.getPlatform();
            if (pf != null)
                simulator = pf.newPlatform(program).getMicrocontroller().getSimulator();
            else
                simulator = Main.getMicrocontroller().newMicrocontroller(program).getSimulator();
            processIcount();
            processTimeout();
            simulator.start();
        } catch (Throwable t) {
        }

        endms = System.currentTimeMillis();
        return endms - startms;
    }

    public String getHelp() {
        return "The \"benchmark\" action benchmarks the simulator's performace on " +
                "an input program and gives tables of performance information. ";

    }

    void processBreakPoints() {
        Iterator i = Main.getLocationList(program, Main.BREAKS.get()).iterator();
        while (i.hasNext()) {
            Main.Location l = (Main.Location) i.next();
            simulator.insertBreakPoint(l.address);
        }
    }

    private void printSeparator() {
        Terminal.printSeparator(60);
    }

    void processIcount() {
        long icount = Main.ICOUNT.get();
        if (icount > 0)
            simulator.insertProbe(new Simulator.InstructionCountTimeout(icount));
    }

    void processTimeout() {
        long timeout = Main.TIMEOUT.get();
        if (timeout > 0)
            simulator.insertTimeout(timeout);
    }

    void reportCycles() {
        if (Main.CYCLES.get()) {
            reportQuantity("Total clock cycles", simulator.getState().getCycles(), "cycles");
        }
    }

    void reportTime() {
        long diff = endms - startms;
        if (Main.TIME.get()) {
            reportQuantity("Time for simulation", StringUtil.milliAsString(diff), "");
            if (total != null) {
                float thru = ((float) total.count) / (diff * 1000);
                reportQuantity("Average throughput", thru, "mips");
            }
            if (Main.CYCLES.get()) {
                float thru = ((float) simulator.getState().getCycles()) / (diff * 1000);
                reportQuantity("Average throughput", thru, "mhz");
            }
        }
    }

    String addrToString(int address) {
        return StringUtil.toHex(address, 4);
    }

    void reportQuantity(String name, long val, String units) {
        reportQuantity(name, Long.toString(val), units);
    }

    void reportQuantity(String name, float val, String units) {
        reportQuantity(name, Float.toString(val), units);
    }

    void reportQuantity(String name, String val, String units) {
        Terminal.printGreen(name);
        Terminal.print(": ");
        Terminal.printBrightCyan(val);
        Terminal.println(" " + units);
    }

}
