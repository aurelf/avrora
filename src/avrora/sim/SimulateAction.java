package avrora.sim;

import avrora.Main;
import avrora.core.Program;
import avrora.sim.util.ProgramProfiler;
import avrora.util.StringUtil;
import avrora.util.Terminal;

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
 * @author Ben L. Titzer
 */
public class SimulateAction extends Main.Action {
    Program program;
    Simulator simulator;
    avrora.sim.util.Counter total;
    long startms, endms;

    List counters;
    List branchcounters;

    ProgramProfiler profile;

    private class Counter extends avrora.sim.util.Counter {
        private final Main.Location location;

        Counter(Main.Location loc) {
            location = loc;
        }

        public void report() {
            String cnt = StringUtil.rightJustify(count, 8);
            String addr = addrToString(location.address);
            String name;
            if ( location.name != null )
                name = "    "+location.name+" @ "+addr;
            else
                name = "    "+addr;
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
            if ( location.name != null )
                name = "    "+location.name+" @ "+addr;
            else
                name = "    "+addr;
            reportQuantity(name, tcnt+" "+ntcnt, "taken/not taken");
        }
    }

    /**
     * The <code>run()</code> method is called by the main class.
     * @param args the command line arguments after the options have been stripped out
     * @throws Exception if there is a problem loading the program, or an exception
     * occurs during simulation
     */
    public void run(String[] args) throws Exception {
        Main.ProgramReader r = Main.getProgramReader();
        program = r.read(args);
        simulator = Main.getMicrocontroller().newMicrocontroller(program).getSimulator();
        counters = new LinkedList();
        branchcounters = new LinkedList();

        processBreakPoints();
        processCounters();
        processBranchCounters();
        processTotal();
        processTimeout();
        processProfile();

        if ( Main.TRACE.get() ) {
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
        }
    }

    public String getHelp() {
        return "The \"simulate\" action launches a simulator with the specified program " +
                "for the specified microcontroller and begins executing the program. There " +
                "are several options provided to the simulator for profiling and analysis, " +
                "so for more information, see the Options section.";

    }

    void processBreakPoints() {
        Iterator i = Main.getLocationList(program, Main.BREAKS.get()).iterator();
        while ( i.hasNext() ) {
            Main.Location l = (Main.Location)i.next();
            simulator.insertBreakPoint(l.address);
        }
    }

    void processCounters() {
        List locs = Main.getLocationList(program, Main.COUNTS.get());
        Iterator i = locs.iterator();
        while ( i.hasNext() ) {
            Main.Location l = (Main.Location)i.next();
            Counter c = new Counter(l);
            counters.add(c);
            simulator.insertProbe(c, l.address);
        }
    }

    void reportCounters() {
        if ( counters.isEmpty() ) return;
        Terminal.printGreen(" Counter results\n");
        printSeparator();
        Iterator i = counters.iterator();
        while ( i.hasNext() ) {
            Counter c = (Counter)i.next();
            c.report();
        }
    }

    void processBranchCounters() {
        List locs = Main.getLocationList(program, Main.COUNTS.get());
        Iterator i = locs.iterator();
        while ( i.hasNext() ) {
            Main.Location l = (Main.Location)i.next();
            BranchCounter c = new BranchCounter(l);
            branchcounters.add(c);
            simulator.insertProbe(c, l.address);
        }
    }

    void reportBranchCounters() {
        if ( branchcounters.isEmpty() ) return;
        Terminal.printGreen(" Branch counter results\n");
        printSeparator();
        Iterator i = branchcounters.iterator();
        while ( i.hasNext() ) {
            BranchCounter c = (BranchCounter)i.next();
            c.report();
        }
    }

    void processProfile() {
        if ( Main.PROFILE.get() )
            simulator.insertProbe(profile = new ProgramProfiler(program));
    }

    void reportProfile() {
        if ( profile != null ) {
            Terminal.printGreen(" Profiling results\n");
            printSeparator();
            double total = 0;
            long[] icount = profile.icount;
            int imax = icount.length;

            // compute the total for percentage calculations
            for ( int cntr = 0; cntr < imax; cntr++ ) {
                total += icount[cntr];
            }

            for ( int cntr = 0; cntr < imax; cntr += 2) {
                int start = cntr;
                int runlength = 1;
                long c = icount[cntr];

                while ( cntr < imax - 2) {
                    if ( icount[cntr+2] != c ) break;
                    cntr += 2;
                    runlength++;
                }

                String cnt = StringUtil.rightJustify(c, 8);
                float pcnt = (float)(100*c / total);
                String percent = toFixedFloat(pcnt, 4)+" %";
                String addr;
                if ( runlength > 1 ) {
                    addr = addrToString(start)+"-"+addrToString(cntr);
                    if ( c != 0 ) {
                        percent += "  x"+runlength;
                        percent += "  = "+toFixedFloat(pcnt*runlength, 4)+" %";
                    }
                } else {
                    addr = "     "+addrToString(start);
                }

                reportQuantity("    "+addr, cnt, "  "+percent);
            }
        }
    }

    private void printSeparator() {
        Terminal.printSeparator(60);
    }

    void processTotal() {
        if ( Main.TOTAL.get() ) {
            simulator.insertProbe(total = new avrora.sim.util.Counter());
        }
    }

    void reportTotal() {
        if ( total != null )
            reportQuantity("Total instructions executed", total.count, "");
    }

    void processTimeout() {
        long timeout = Main.TIMEOUT.get();
        if ( timeout > 0 )
            simulator.insertProbe(new Simulator.InstructionCountTimeout(timeout));
    }

    void reportCycles() {
        if ( Main.CYCLES.get() ) {
            reportQuantity("Total clock cycles", simulator.getState().getCycles(), "cycles");
        }
    }

    void reportTime() {
        long diff = endms - startms;
        if ( Main.TIME.get() ) {
            reportQuantity("Time for simulation", StringUtil.milliAsString(diff), "");
            if ( total != null ) {
                float thru = ((float)total.count) / (diff*1000);
                reportQuantity("Average throughput", thru, "mips");
            }
            if ( Main.CYCLES.get() ) {
                float thru = ((float)simulator.getState().getCycles()) / (diff*1000);
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
        Terminal.println(" "+units);
    }

    // warning! only works on numbers < 100!!!!
    public static String toFixedFloat(float f, int places) {
        // TODO: fix this routine or find an alternative
        StringBuffer buf = new StringBuffer();
        float radix = 100;
        boolean nonzero = false;
        for ( int cntr = 0; cntr < places+3; cntr++ ) {
            int digit = ((int)(f/radix))%10;
            char dchar = (char)(digit+'0');

            if ( digit != 0 ) nonzero = true;

            if ( digit == 0 && !nonzero && cntr < 2) dchar = ' ';

            buf.append(dchar);
            if ( cntr == 2 ) buf.append('.');
            radix = radix / 10;
        }

        return buf.toString();
    }


}
