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

package avrora.monitors;

import avrora.core.Program;
import avrora.core.Instr;
import avrora.sim.BaseInterpreter;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.mcu.Microcontroller;
import avrora.sim.mcu.MicrocontrollerProperties;
import avrora.util.StringUtil;
import avrora.util.Terminal;
import avrora.util.TermUtil;
import avrora.util.Option;
import avrora.actions.SimAction;

import java.util.List;
import java.util.Iterator;

/**
 * The <code>MemoryMonitor</code> class implements a monitor that collects information about how the program
 * accesses the data memory over its execution. For each RAM address it keeps an account of the number of
 * reads and the number of writes and reports that information after the program is completed.
 *
 * @author Ben L. Titzer
 */
public class MemoryMonitor extends MonitorFactory {

    public final Option.Bool EMPTY = options.newOption("empty-watch", false,
            "This option is used to test the overhead of adding an empty watch to every" +
            "memory location. ");
    public final Option.List LOCATIONS = options.newOptionList("locations", "",
            "This option is used to test the overhead of adding an empty watch to every" +
            "memory location. ");

    public final Option.Bool LOWER_ADDRESS = options.newOption("low-addresses", false,
            "When this option is enabled, the memory monitor will be inserted for lower addresses, " +
            "recording reads and writes to IO registers and indirect reads and writes to the IO " +
            "registers.");

    public class Monitor implements avrora.monitors.Monitor {
        public final Simulator simulator;
        public final Microcontroller microcontroller;
        public final Program program;
        public final int ramsize;
        public final int memstart;
        public final avrora.sim.util.MemoryProfiler memprofile;

        Monitor(Simulator s) {
            boolean empty = EMPTY.get();
            simulator = s;
            microcontroller = simulator.getMicrocontroller();
            program = simulator.getProgram();
            MicrocontrollerProperties p = microcontroller.getProperties();
            if ( LOWER_ADDRESS.get() ) {
                ramsize = p.sram_size + p.ioreg_size + BaseInterpreter.NUM_REGS;
                memstart = 0;
            } else {
                ramsize = p.sram_size;
                memstart = BaseInterpreter.NUM_REGS + p.ioreg_size;
            }
            memprofile = new avrora.sim.util.MemoryProfiler(ramsize);

            insertWatches(empty);

        }

        private void insertWatches(boolean empty) {

            Simulator.Watch w;
            if (  empty ) w = new EmptyWatch();
            else w = memprofile;

            if ( LOCATIONS.get().size() > 0 ) {
                // instrument only the locations specified
                List l = LOCATIONS.get();
                List loc = SimAction.getLocationList(program, l);
                Iterator i = loc.iterator();
                while ( i.hasNext() ) {
                    // TODO: this should not be program locations, but memory locations!!!
                    Program.Location location = (Program.Location)i.next();
                    simulator.insertWatch(w, location.address);
                }
            } else {
                // instrument the entire memory
                for (int cntr = memstart; cntr < ramsize; cntr++) {
                    simulator.insertWatch(w, cntr);
                }
            }
        }

        public void report() {
            TermUtil.printSeparator(Terminal.MAXLINE, "Memory profiling results");
            Terminal.printGreen("   Address     Reads               Writes");
            Terminal.nextln();
            TermUtil.printThinSeparator(Terminal.MAXLINE);
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

            for (int cntr = memstart; cntr < imax; cntr++) {
                int start = cntr;
                long r = rcount[cntr];
                long w = wcount[cntr];

                if (r == 0 && w == 0)
                    zeroes++;
                else
                    zeroes = 0;

                // skip long runs of zeroes
                if (zeroes == 2) {
                    Terminal.println("                   .                    .");
                    continue;
                } else if (zeroes > 2) continue;

                String addr = StringUtil.addrToString(start);
                printLine(addr, r, rtotal, w, wtotal);

            }
            printLine("total ", (long)rtotal, rtotal, (long)wtotal, wtotal);
        }

        private void printLine(String addr, long r, double rtotal, long w, double wtotal) {
            String rcnt = StringUtil.rightJustify(r, 8);
            float rpcnt = (float)(100 * r / rtotal);
            String rpercent = StringUtil.rightJustify(StringUtil.toFixedFloat(rpcnt, 4),8) + " %";

            String wcnt = StringUtil.rightJustify(w, 8);
            float wpcnt = (float)(100 * w / wtotal);
            String wpercent = StringUtil.rightJustify(StringUtil.toFixedFloat(wpcnt, 4),8) + " %";

            Terminal.printGreen("    " + addr);
            Terminal.print(": ");
            Terminal.printBrightCyan(rcnt);
            Terminal.print(' ' + ("  " + rpercent));
            Terminal.printBrightCyan(wcnt);
            Terminal.println(' ' + ("  " + wpercent));
        }

    }

    public class EmptyWatch implements Simulator.Watch {
        public void fireBeforeRead(Instr i, int address, State state, int data_addr) {}

        public void fireBeforeWrite(Instr i, int address, State state, int data_addr, byte value) {}

        public void fireAfterRead(Instr i, int address, State state, int data_addr, byte value) {}

        public void fireAfterWrite(Instr i, int address, State state, int data_addr, byte value) {}
    }

    public MemoryMonitor() {
        super("memory", "The \"memory\" monitor collects information about the " +
                "memory usage statistics of the program, which includes the number " +
                "of reads and writes to every byte of data memory.");
    }

    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}
