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

import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.core.Instr;
import avrora.core.Program;
import avrora.core.SourceMapping;
import avrora.util.StringUtil;
import avrora.util.Option;
import avrora.util.Terminal;

/**
 * The <code>CallMonitor</code> class implements a monitor that is capable of tracing the call/return behavior
 * of a program while it executes.
 *
 * @author Ben L. Titzer
 */
public class CallMonitor extends MonitorFactory {

    class Mon implements Monitor {

        Simulator s;
        InterruptProbe iprobe;
        SourceMapping sm;
        int depth = 0;
        String[] stack;

        Mon(Simulator s) {
            this.s = s;

            iprobe = new InterruptProbe();

            for ( int pc = 0; pc < Simulator.MAX_INTERRUPTS; pc++ ) {
                s.insertProbe(iprobe, pc*4);
            }

            stack = new String[256];
            stack[0] = "";

            Program p = s.getProgram();
            sm = p.getSourceMapping();
            for ( int pc = 0; pc < p.program_end; pc = p.getNextPC(pc)) {
                Instr i = p.readInstr(pc);
                if ( i != null ) {
                    if ( i instanceof Instr.CALL ) {
                        s.insertProbe(new CallProbe("CALL"), pc);
                    }
                    else if ( i instanceof Instr.ICALL ) {
                        s.insertProbe(new CallProbe("ICALL"), pc);
                    }
                    else if ( i instanceof Instr.RCALL) {
                        s.insertProbe(new CallProbe("RCALL"), pc);
                    }
                    else if ( i instanceof Instr.RET ) {
                        s.insertProbe(new ReturnProbe("RET"), pc);
                    }
                    else if ( i instanceof Instr.RETI ) {
                        s.insertProbe(new ReturnProbe("RETI"), pc);
                    }
                }
            }
        }

        public void report() {
            // do nothing
        }

        class CallProbe extends Simulator.Probe.Empty {
            String itype;

            CallProbe(String itype) {
                this.itype = itype;
            }

            public void fireAfter(Instr i, int addr, State s) {
                int npc = s.getPC();
                String caddr = StringUtil.addrToString(addr);
                String daddr = sm.getName(npc);
                push(caddr, daddr, itype);
            }

        }

        private void push(String caller, String dest, String edge) {
            String idstr = s.getIDTimeString();
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

        }

        private void printStack(int depth) {
            for ( int cntr = 0; cntr <= depth; cntr++ ) {
                Terminal.printGreen(stack[cntr]);
                if ( cntr != depth ) Terminal.print(":");
            }
        }

        private void pop(String caller, String edge) {
            String idstr = s.getIDTimeString();
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
        }

        class InterruptProbe extends Simulator.Probe.Empty {
            public void fireBefore(Instr i, int addr, State s) {
                int inum = (addr / 4) + 1;
                String istr;
                if ( inum == 1) {
                    istr = "RESET";
                    depth = 0;
                }
                else istr = "INT #"+inum;

                String caddr = StringUtil.addrToString(addr);
                push(caddr, caddr, istr);
            }

        }

        class ReturnProbe extends Simulator.Probe.Empty {
            String itype;

            ReturnProbe(String itype) {
                this.itype = itype;
            }

            public void fireAfter(Instr i, int addr, State s) {
                int npc = s.getPC();
                String daddr = StringUtil.addrToString(npc);
                pop(daddr, itype);
            }
        }
    }

    public CallMonitor() {
        super("The \"call\" monitor tracks the call/return behavior of the program as it executes, " +
                "displaying the stacking up of function calls and interrupt handlers.");
    }

    public Monitor newMonitor(Simulator s) {
        return new Mon(s);
    }
}
