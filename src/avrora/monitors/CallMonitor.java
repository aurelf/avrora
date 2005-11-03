/**
 * Created on 18. September 2004, 22:02
 *
 * Copyright (c) 2004, Olaf Landsiedel, Protocol Engineering and
 * Distributed Systems, University of Tuebingen
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
 * Neither the name of the Protocol Engineering and Distributed Systems
 * Group, the name of the University of Tuebingen nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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

import avrora.core.*;
import avrora.sim.*;
import avrora.sim.mcu.MicrocontrollerProperties;
import avrora.sim.util.SimUtil;
import cck.text.*;
import cck.util.Util;

/**
 * The <code>CallMonitor</code> class implements a monitor that is capable of tracing the call/return behavior
 * of a program while it executes.
 *
 * @author Ben L. Titzer
 */
public class CallMonitor extends MonitorFactory {

    public static final int MAX_STACK_DEPTH = 256;

    class Mon implements Monitor {

        private final Simulator simulator;
        private final MicrocontrollerProperties props;
        private final SourceMapping sourceMap;

        private String[] stack;

        private int depth = 0;
        private int maxdepth = 0;

        Mon(Simulator s) {
            this.simulator = s;
            props = simulator.getMicrocontroller().getProperties();
            BaseInterpreter interpreter = s.getInterpreter();
            InterruptTable itable = interpreter.getInterruptTable();
            itable.insertProbe(new InterruptProbe());

            stack = new String[MAX_STACK_DEPTH];
            stack[0] = "";

            Program p = s.getProgram();
            sourceMap = p.getSourceMapping();
            for ( int pc = 0; pc < p.program_end; pc = p.getNextPC(pc)) {
                Instr i = p.readInstr(pc);
                if ( i != null ) {
                    if ( i instanceof Instr.CALL ) s.insertProbe(new CallProbe("CALL"), pc);
                    else if ( i instanceof Instr.ICALL ) s.insertProbe(new CallProbe("ICALL"), pc);
                    else if ( i instanceof Instr.RCALL) s.insertProbe(new CallProbe("RCALL"), pc);
                    else if ( i instanceof Instr.RET ) s.insertProbe(new ReturnProbe("RET"), pc);
                    else if ( i instanceof Instr.RETI ) s.insertProbe(new ReturnProbe("RETI"), pc);
                }
            }
        }

        public void report() {
            TermUtil.reportQuantity("Maximum stack depth", maxdepth, "frames");
            // do nothing
        }

        private void push(String caller, String dest, String edge) {
            String idstr = SimUtil.getIDTimeString(simulator);
            if ( depth >= stack.length )
                throw Util.failure("Stack overflow: more than "+MAX_STACK_DEPTH+" calls nested");
            stack[depth+1] = dest;
            synchronized (Terminal.class ) {
                Terminal.print(idstr);
                printStack(depth);
                Terminal.print(" @ ");
                Terminal.printBrightCyan(caller);
                Terminal.print(" --(");
                Terminal.printRed(edge);
                Terminal.print(")-> ");
                Terminal.printGreen(dest);
                Terminal.nextln();
            }
            depth++;
            if ( depth > maxdepth ) maxdepth = depth;
        }

        private void printStack(int depth) {
            for ( int cntr = 0; cntr <= depth; cntr++ ) {
                Terminal.printGreen(stack[cntr]);
                if ( cntr != depth ) Terminal.print(":");
            }
        }

        private void pop(String caller, String edge) {
            String idstr = SimUtil.getIDTimeString(simulator);
            synchronized (Terminal.class ) {
                Terminal.print(idstr);
                printStack(depth-1);
                Terminal.print(" @ ");
                Terminal.printBrightCyan(caller);
                Terminal.print(" <-(");
                Terminal.printRed(edge);
                Terminal.println(")-- ");
            }
            stack[depth] = null;
            depth--;
            if ( depth < 0 ) depth = 0;
        }

        class CallProbe extends Simulator.Probe.Empty {
            String itype;

            CallProbe(String itype) {
                this.itype = itype;
            }

            public void fireAfter(State s, int addr) {
                int npc = s.getPC();
                String caddr = StringUtil.addrToString(addr);
                String daddr = sourceMap.getName(npc);
                push(caddr, daddr, itype);
            }

        }

        class InterruptProbe extends Simulator.InterruptProbe.Empty {
            public void fireBeforeInvoke(State s, int inum) {
                String istr;
                if ( inum == 1) {
                    istr = "RESET";
                    depth = 0;
                }
                else istr = "INT #"+inum+"("+props.getInterruptName(inum)+")";
                String caddr = StringUtil.addrToString(s.getPC());
                push(caddr, istr, istr);
            }

        }

        class ReturnProbe extends Simulator.Probe.Empty {
            String itype;

            ReturnProbe(String itype) {
                this.itype = itype;
            }

            public void fireAfter(State s, int addr) {
                int npc = s.getPC();
                String daddr = StringUtil.addrToString(npc);
                pop(daddr, itype);
            }
        }
    }

    /**
     * The constructor for the <code>CallMonitor</code> class simply initializes the help for this
     * class. Monitors are also help categories, so they will have an options section in their help
     * that explains each option and its use.
     */
    public CallMonitor() {
        super("The \"calls\" monitor tracks the call/return behavior of the program as it executes, " +
                "displaying the stacking up of function calls and interrupt handlers.");
    }

    /**
     * The <code>newMonitor()</code> method simply creates a new call monitor for each simulator. The
     * call monitor will print out each call, interrupt, and return during the execution of the program.
     * @param s the simulator to create a new monitor for
     * @return a new monitor that tracks the call and return behavior of the simulator as it executes
     */
    public Monitor newMonitor(Simulator s) {
        return new Mon(s);
    }
}
