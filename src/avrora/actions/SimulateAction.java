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

import avrora.Main;
import avrora.core.Instr;
import avrora.core.Program;
import avrora.sim.BaseInterpreter;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.mcu.Microcontroller;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.util.MemoryProfiler;
import avrora.sim.util.ProgramProfiler;
import avrora.util.Option;
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
public class SimulateAction extends SimAction {
    Program program;
    Simulator simulator;
    Microcontroller microcontroller;
    avrora.sim.util.Counter total;
    long startms, endms;

    List counters;
    List branchcounters;

    StackProbe sprobe;

    ProgramProfiler profile;

    MemoryProfiler memprofile;

    public static final String HELP = "The \"simulate\" action launches a simulator with the specified program " +
            "for the specified microcontroller and begins executing the program. There " +
            "are several options provided to the simulator for profiling and analysis, " +
            "so for more information, see the Options section.";
    public int memStart;

    public final Option.List BREAKS = newOptionList("breakpoint", "",
            "This option is used in the simulate action. It allows the user to " +
            "insert a series of breakpoints in the program from the command line. " +
            "The address of the breakpoint can be given in hexadecimal or as a label " +
            "within the program. Hexadecimal constants are denoted by a leading '$'.");
    public final Option.List COUNTS = newOptionList("count", "",
            "This option is used in the simulate action. It allows the user to " +
            "insert a list of profiling counters in the program that collect profiling " +
            "information during the execution of the program.");
    public final Option.List BRANCHCOUNTS = newOptionList("branchcount", "",
            "This option is used in the simulate action. It allows the user to " +
            "insert a list of branch counters in the program that collect information " +
            "about taken and not taken counts for branches.");
    public final Option.Bool PROFILE = newOption("profile", false,
            "This option is used in the simulate action. It compiles a histogram of " +
            "instruction counts for each instruction in the program and presents the " +
            "results in a tabular format.");
    public final Option.Bool TIME = newOption("time", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the time used in executing the simulation. When combined with " +
            "the \"cycles\" and \"total\" options, it will report performance " +
            "information about the simulation.");
    public final Option.Bool TOTAL = newOption("total", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the total instructions executed in the simulation. When combined " +
            "with the \"time\" option, it will report performance information.");
    public final Option.Bool CYCLES = newOption("cycles", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the total cycles executed in the simulation. When combined " +
            "with the \"time\" option, it will report performance information.");
    public final Option.Bool TRACE = newOption("trace", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to print each instruction as it is executed.");
    public final Option.Bool MONITOR_STACK = newOption("monitor-stack", false,
            "This option is used in the \"simulate\" action. It causes the simulator " +
            "to report changes to the stack height.");
    public final Option.Bool MEMORY_PROFILE = newOption("memory-profile", false,
            "This option is used in the \"simulate\" action. It causes the simulator " +
            "to analyze the memory traffic of the program and print out the results in " +
            "a tabular format. This can be useful to see what portions of memory are the " +
            "most used.");
    public final Option.Bool LEGACY_INTERPRETER = newOption("legacy-interpreter", true,
            "This option is used in the \"simulate\" action. It causes the simulator " +
            "to use the legacy (hand-written) interpreter rather than the interpreter " +
            "generated from the architecture description language. It is used for " +
            "benchmarking and regression purposes.");
    public final Option.Bool FAST_INTERPRETER = newOption("fast-interpreter", false,
            "This option is used in the \"simulate\" action. It causes the simulator " +
            "to use the fast (but space inefficient) interpreter rather than the " +
            "default interpreter. It is used for benchmarking and regression purposes.");

    public SimulateAction() {
        super("simulate", HELP);
    }

    private class Counter extends avrora.sim.util.Counter {
        private final Main.Location location;

        Counter(Main.Location loc) {
            location = loc;
        }

        public void report() {
            String cnt = StringUtil.rightJustify(count, 8);
            String addr = addrToString(location.address);
            String name;
            if (location.name != null)
                name = "    " + location.name + " @ " + addr;
            else
                name = "    " + addr;
            reportQuantity(name, cnt, "");
        }
    }

    private class BranchCounter extends avrora.sim.util.BranchCounter {
        private final Main.Location location;

        BranchCounter(Main.Location loc) {
            location = loc;
        }

        public void report() {
            String tcnt = StringUtil.rightJustify(takenCount, 8);
            String ntcnt = StringUtil.rightJustify(nottakenCount, 8);
            String addr = addrToString(location.address);
            String name;
            if (location.name != null)
                name = "    " + location.name + " @ " + addr;
            else
                name = "    " + addr;
            reportQuantity(name, tcnt + " " + ntcnt, "taken/not taken");
        }
    }

    /**
     * The <code>run()</code> method is called by the main class.
     *
     * @param args the command line arguments after the options have been stripped out
     * @throws java.lang.Exception if there is a problem loading the program, or an exception
     *                   occurs during simulation
     */
    public void run(String[] args) throws Exception {
//        long repeat = BenchmarkAction.REPEAT.get();

//        for (long cntr = 0; cntr < repeat; cntr++)
            runSimulation(args);

    }

    private void runSimulation(String[] args) throws Exception {
        program = Main.readProgram(args);

        Simulator.LEGACY_INTERPRETER = LEGACY_INTERPRETER.get();
        Simulator.FIF_INTERPRETER = FAST_INTERPRETER.get();

        PlatformFactory pf = getPlatform();
        if (pf != null) {
            microcontroller = pf.newPlatform(program).getMicrocontroller();
            simulator = microcontroller.getSimulator();
        } else {
            microcontroller = getMicrocontroller().newMicrocontroller(program);
            simulator = microcontroller.getSimulator();
        }
        counters = new LinkedList();
        branchcounters = new LinkedList();

        processBreakPoints();
        processCounters();
        processBranchCounters();
        processTotal();
        processIcount();
        processTimeout();
        processProfile();
        processMemoryProfile();
        processStackMonitor();

        if (TRACE.get()) {
            simulator.insertProbe(Simulator.TRACEPROBE);
        }

        startms = System.currentTimeMillis();
        try {
            simulator.start();
        } finally {
            endms = System.currentTimeMillis();

            reportCounters();
            reportBranchCounters();
            reportTotal();
            reportCycles();
            reportTime();
            reportProfile();
            reportMemoryProfile();
            reportStackMonitor();
        }
    }

    void processBreakPoints() {
        Iterator i = Main.getLocationList(program, BREAKS.get()).iterator();
        while (i.hasNext()) {
            Main.Location l = (Main.Location) i.next();
            simulator.insertBreakPoint(l.address);
        }
    }

    void processCounters() {
        List locs = Main.getLocationList(program, COUNTS.get());
        Iterator i = locs.iterator();
        while (i.hasNext()) {
            Main.Location l = (Main.Location) i.next();
            Counter c = new Counter(l);
            counters.add(c);
            simulator.insertProbe(c, l.address);
        }
    }

    void reportCounters() {
        if (counters.isEmpty()) return;
        Terminal.printGreen(" Counter results\n");
        printSeparator();
        Iterator i = counters.iterator();
        while (i.hasNext()) {
            Counter c = (Counter) i.next();
            c.report();
        }
    }

    void processBranchCounters() {
        List locs = Main.getLocationList(program, BRANCHCOUNTS.get());
        Iterator i = locs.iterator();
        while (i.hasNext()) {
            Main.Location l = (Main.Location) i.next();
            BranchCounter c = new BranchCounter(l);
            branchcounters.add(c);
            simulator.insertProbe(c, l.address);
        }
    }

    void reportBranchCounters() {
        if (branchcounters.isEmpty()) return;
        Terminal.printGreen(" Branch counter results\n");
        printSeparator();
        Iterator i = branchcounters.iterator();
        while (i.hasNext()) {
            BranchCounter c = (BranchCounter) i.next();
            c.report();
        }
    }

    void processProfile() {
        if (PROFILE.get())
            simulator.insertProbe(profile = new ProgramProfiler(program));
    }

    void reportProfile() {
        if (profile != null) {
            Terminal.printGreen(" Profiling results\n");
            printSeparator();
            double total = 0;
            long[] icount = profile.icount;
            int imax = icount.length;

            // compute the total for percentage calculations
            for (int cntr = 0; cntr < imax; cntr++) {
                total += icount[cntr];
            }

            for (int cntr = 0; cntr < imax; cntr += 2) {
                int start = cntr;
                int runlength = 1;
                long c = icount[cntr];

                if ( program.readInstr(cntr) == null ) continue;

                for (; cntr < imax - 2; cntr+= 2) {
                    if ( program.readInstr(cntr + 2) == null ) continue;
                    if (icount[cntr + 2] != c) break;
                    runlength++;
                }

                String cnt = StringUtil.rightJustify(c, 8);
                float pcnt = (float) (100 * c / total);
                String percent = toFixedFloat(pcnt, 4) + " %";
                String addr;
                if (runlength > 1) {
                    addr = addrToString(start) + "-" + addrToString(cntr);
                    if (c != 0) {
                        percent += "  x" + runlength;
                        percent += "  = " + toFixedFloat(pcnt * runlength, 4) + " %";
                    }
                } else {
                    addr = "     " + addrToString(start);
                }

                reportQuantity("    " + addr, cnt, "  " + percent);
            }
        }
    }

    void processMemoryProfile() {
        if (MEMORY_PROFILE.get()) {
            int ramSize = microcontroller.getRamSize();
            memStart = BaseInterpreter.NUM_REGS + microcontroller.getIORegSize();
            memprofile = new MemoryProfiler(ramSize);
            for (int cntr = memStart; cntr < ramSize; cntr++) {
                simulator.insertWatch(memprofile, cntr);
            }
        }
    }

    void reportMemoryProfile() {
        if (memprofile != null) {
            Terminal.printGreen(" Memory Profiling results\n");
            printSeparator();
            Terminal.println("             Reads               Writes");
            printSeparator();
            double rtotal = 0;
            long[] rcount = memprofile.rcount;
            double wtotal = 0;
            long[] wcount = memprofile.wcount;
            int imax = rcount.length;

            // compute the total for percentage calculations
            for (int cntr = 0; cntr < imax; cntr++) {
                rtotal += rcount[cntr];
                wtotal += wcount[cntr];
            }

            int zeroes = 0;

            for (int cntr = memStart; cntr < imax; cntr++) {
                int start = cntr;
                long r = rcount[cntr];
                long w = wcount[cntr];

                if (r == 0 && w == 0)
                    zeroes++;
                else
                    zeroes = 0;

                if (zeroes == 2) {
                    Terminal.println("                 .                    .");
                    continue;
                } else if (zeroes > 2) continue;

                String rcnt = StringUtil.rightJustify(r, 8);
                float rpcnt = (float) (100 * r / rtotal);
                String rpercent = toFixedFloat(rpcnt, 4) + " %";

                String wcnt = StringUtil.rightJustify(w, 8);
                float wpcnt = (float) (100 * w / wtotal);
                String wpercent = toFixedFloat(wpcnt, 4) + " %";

                String addr = addrToString(start);

                Terminal.printGreen("    " + addr);
                Terminal.print(": ");
                Terminal.printBrightCyan(rcnt);
                Terminal.print(" " + ("  " + rpercent));
                Terminal.printBrightCyan(wcnt);
                Terminal.println(" " + ("  " + wpercent));
            }
        }
    }

    void processStackMonitor() {
        if (MONITOR_STACK.get())
            simulator.insertProbe(sprobe = new StackProbe());
    }

    private void printSeparator() {
        Terminal.printSeparator(60);
    }

    void processTotal() {
        if (TOTAL.get()) {
            simulator.insertProbe(total = new avrora.sim.util.Counter());
        }
    }

    void reportTotal() {
        if (total != null)
            reportQuantity("Total instructions executed", total.count, "");
    }

    void processIcount() {
        long icount = ICOUNT.get();
        if (icount > 0)
            simulator.insertProbe(new Simulator.InstructionCountTimeout(icount));
    }

    void processTimeout() {
        long timeout = TIMEOUT.get();
        if (timeout > 0)
            simulator.insertTimeout(timeout);
    }

    void reportCycles() {
        if (CYCLES.get()) {
            reportQuantity("Total clock cycles", simulator.getState().getCycles(), "cycles");
        }
    }

    void reportTime() {
        long diff = endms - startms;
        if (TIME.get()) {
            reportQuantity("Time for simulation", StringUtil.milliAsString(diff), "");
            if (total != null) {
                float thru = ((float) total.count) / (diff * 1000);
                reportQuantity("Average throughput", thru, "mips");
            }
            if (CYCLES.get()) {
                float thru = ((float) simulator.getState().getCycles()) / (diff * 1000);
                reportQuantity("Average throughput", thru, "mhz");
            }
        }
    }

    void reportStackMonitor() {
        if (sprobe == null) return;
        reportQuantity("Minimum stack pointer #1", "0x" + StringUtil.toHex(sprobe.minStack1, 4), "");
        reportQuantity("Minimum stack pointer #2", "0x" + StringUtil.toHex(sprobe.minStack2, 4), "");
        reportQuantity("Minimum stack pointer #3", "0x" + StringUtil.toHex(sprobe.minStack3, 4), "");
        reportQuantity("Maximum stack pointer", "0x" + StringUtil.toHex(sprobe.maxStack, 4), "");
        reportQuantity("Maximum stack size #1", (sprobe.maxStack - sprobe.minStack1), "bytes");
        reportQuantity("Maximum stack size #2", (sprobe.maxStack - sprobe.minStack2), "bytes");
        reportQuantity("Maximum stack size #3", (sprobe.maxStack - sprobe.minStack3), "bytes");
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

    // warning! only works on numbers < 100!!!!
    public static String toFixedFloat(float f, int places) {
        // TODO: fix this routine or find an alternative
        StringBuffer buf = new StringBuffer();
        float radix = 100;
        boolean nonzero = false;
        for (int cntr = 0; cntr < places + 3; cntr++) {
            int digit = ((int) (f / radix)) % 10;
            char dchar = (char) (digit + '0');

            if (digit != 0) nonzero = true;

            if (digit == 0 && !nonzero && cntr < 2) dchar = ' ';

            buf.append(dchar);
            if (cntr == 2) buf.append('.');
            radix = radix / 10;
        }

        return buf.toString();
    }


    public static class StackProbe implements Simulator.Probe {
        int lastStack;

        int minStack1 = Integer.MAX_VALUE;
        int minStack2 = Integer.MAX_VALUE;
        int minStack3 = Integer.MAX_VALUE;
        int maxStack = Integer.MIN_VALUE;

        static final Verbose.Printer printer = Verbose.getVerbosePrinter("sim.stack");

        public void fireBefore(Instr i, int address, State s) {
            lastStack = s.getSP();
        }

        public void fireAfter(Instr i, int address, State s) {
            int newStack = s.getSP();
            if (lastStack != newStack) {
                printer.println("new stack: " + newStack);
                lastStack = newStack;
            }

            if (newStack < minStack1) {
                minStack3 = minStack2;
                minStack2 = minStack1;
                minStack1 = newStack;
            } else if (newStack == minStack1)
                return;
            else if (newStack < minStack2) {
                minStack3 = minStack2;
                minStack2 = newStack;
            } else if (newStack == minStack2)
                return;
            else if (newStack < minStack3) {
                minStack3 = newStack;
            }

            if (newStack > maxStack) maxStack = newStack;
        }
    }

}
