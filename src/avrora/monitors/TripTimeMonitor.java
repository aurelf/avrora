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

import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.util.Option;
import avrora.util.StringUtil;
import avrora.util.Terminal;
import avrora.util.profiling.Distribution;
import avrora.util.profiling.MinMaxMean;
import avrora.core.Instr;
import avrora.core.Program;
import avrora.Avrora;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Ben L. Titzer
 */
public class TripTimeMonitor extends MonitorFactory {

    final Option.List PAIRS = options.newOptionList("pairs", "",
            "The \"pairs\" option specifies the list of program point pairs for which " +
            "to measure the point-to-point trip time. ");
    final Option.List FROM = options.newOptionList("from", "",
            "The \"from\" option specifies the list of program points for which " +
            "to measure to every other instruction in the program. ");
    final Option.List TO = options.newOptionList("to", "",
            "The \"from\" option specifies the list of program points for which " +
            "to measure from every other instruction in the program. ");
    final Option.Bool DISTRIBUTION = options.newOption("distribution", false, "");

    public TripTimeMonitor() {
        super("trip-time", "The \"trip-time\" monitor records profiling " +
                "information about the program that consists of the time it takes " +
                "(on average) to reach one point from another point in the program.");
    }

    protected class PointToPointMon implements Monitor {

        class Pair {
            final int start;
            final int end;
            long cumul;
            int count;

            Pair startLink;
            Pair endLink;

            Distribution distrib;


            Pair(int start, int end) {
                this.start = start;
                this.end = end;

                if ( DISTRIBUTION.get() )
                    distrib = new Distribution("trip time "
                            +StringUtil.addrToString(start)+" -to- "
                            +StringUtil.addrToString(end), "Trips", "Total Time", "Distribution");
            }

            void record(long time) {
                if ( distrib != null ) {
                    distrib.record((int)time);
                } else {
                    cumul += time;
                }
                count++;
            }

            void report() {
                if ( distrib == null ) {
                float avg = (float)cumul / count;
                Terminal.println("  "+StringUtil.addrToString(start)+"  "
                        +StringUtil.addrToString(end)+"  "
                        +StringUtil.rightJustify(count, 6)+"  "
                        +StringUtil.rightJustify(avg, 6));
                } else {
                    distrib.processData();
                    distrib.textReport();
                }
            }
        }

        final Pair[] startArray;
        final Pair[] endArray;
        final long[] lastEnter;

        final Simulator simulator;
        final Program program;
        final PTPProbe PROBE;

        PointToPointMon(Simulator s) {
            simulator = s;
            program = s.getProgram();
            int psize = program.program_end;
            startArray = new Pair[psize];
            endArray = new Pair[psize];
            lastEnter = new long[psize];
            PROBE = new PTPProbe();

            addPairs();
            addFrom();
            addTo();
        }

        private void addPairs() {
            Iterator i = PAIRS.get().iterator();
            while (i.hasNext()) {
                String str = (String)i.next();
                int ind = str.indexOf(":");
                if (ind <= 0)
                    throw Avrora.failure("invalid address format: " + StringUtil.quote(str));
                String src = str.substring(0, ind);
                String dst = str.substring(ind + 1);

                Program.Location loc = getLocation(src);
                Program.Location tar = getLocation(dst);

                addPair(loc.address, tar.address);
            }
        }

        private Program.Location getLocation(String src) {
            Program.Location loc = program.getProgramLocation(src);
            if ( loc == null )
                Avrora.userError("Invalid program address: ", src);
            if ( program.readInstr(loc.address) == null )
                Avrora.userError("Invalid program address: ", src);
            return loc;
        }

        private void addFrom() {
            Iterator i = FROM.get().iterator();
            while (i.hasNext()) {
                String str = (String)i.next();
                Program.Location loc = program.getProgramLocation(str);
                for ( int cntr = 0; cntr < program.program_end; cntr = program.getNextPC(cntr) )
                    addPair(loc.address, cntr);
            }
        }

        private void addTo() {
            Iterator i = TO.get().iterator();
            while (i.hasNext()) {
                String str = (String)i.next();
                Program.Location loc = program.getProgramLocation(str);
                for ( int cntr = 0; cntr < program.program_end; cntr = program.getNextPC(cntr) )
                    addPair(cntr, loc.address);
            }
        }

        void addPair(int start, int end) {

            if ( program.readInstr(start) == null ) return;
            if ( program.readInstr(end) == null ) return;

            Pair p = new Pair(start, end);

            if (startArray[p.start] == null && endArray[p.start] == null)
                simulator.insertProbe(PROBE, p.start);

            p.startLink = startArray[p.start];
            startArray[p.start] = p;

            if (startArray[p.end] == null && endArray[p.end] == null)
                simulator.insertProbe(PROBE, p.end);


            p.endLink = endArray[p.end];
            endArray[p.end] = p;

        }

        protected class PTPProbe implements Simulator.Probe {
            public void fireBefore(Instr i, int address, State state) {
                long time = state.getCycles();

                for ( Pair p = endArray[address]; p != null; p = p.startLink ) {
                    if ( lastEnter[p.start] < 0 ) continue;
                    p.record(time - lastEnter[p.start]);
                }

                lastEnter[address] = time;
            }

            public void fireAfter(Instr i, int address, State state) {

            }
        }

        public void report() {
            for ( int cntr = 0; cntr < lastEnter.length; cntr++ )
                for ( Pair p = startArray[cntr]; p != null; p = p.startLink ) {
                    if ( p.count > 0 ) p.report();
                }
        }
    }

    public Monitor newMonitor(Simulator s) {
        return new PointToPointMon(s);
    }
}
