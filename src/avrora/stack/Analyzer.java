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

package avrora.stack;

import avrora.core.Instr;
import avrora.core.Program;
import avrora.sim.IORegisterConstants;
import avrora.util.StringUtil;
import avrora.util.Terminal;

import java.util.HashMap;
import java.util.HashSet;

/**
 * The <code>Analyzer</code> class implements the analysis phase that determines
 * the transition relation between the states in the abstract state space. It is
 * modelled on the simulator, but only does abstract interpretation rather than
 * executing the entire program.
 *
 * @author Ben L. Titzer
 */
public class Analyzer {

    protected final Program program;
    protected final StateSpace space;
    ContextSensitivePolicy policy;
    AbstractInterpreter interpreter;
    HashMap frontierInfoMap;
    long buildTime;
    long traverseTime;
    int maxDepth;

    protected final StateSpace.SpecialState returnState;
    protected final StateSpace.SpecialState returnIState;

    public static boolean TRACE;

    public static class FrontierInfo {
        public static class CallSiteList {
            public final FrontierInfo caller;
            public final CallSiteList next;

            CallSiteList(FrontierInfo s, CallSiteList n) {
                caller = s;
                next = n;
            }
        }

        public StateSpace.State state;
        public CallSiteList callsites;

        FrontierInfo(StateSpace.State s) {
            state = s;
        }

        FrontierInfo(StateSpace.State s, CallSiteList cl) {
            state = s;
            callsites = cl;
        }
    }

    public Analyzer(Program p) {
        program = p;
        space = new StateSpace(p);
        policy = new ContextSensitivePolicy();
        interpreter = new AbstractInterpreter(program, policy);
        frontierInfoMap = new HashMap();
        returnState = space.makeSpecialState("---RET----");
        returnIState = space.makeSpecialState("---RETI---");
    }

    /**
     * The <code>run()</code> method begins the analysis. The entrypoint of the program
     * with an initial default state serves as the first state to start exploring.
     */
    public void run() {
        long start = System.currentTimeMillis();
        buildReachableStateSpace();
        long check = System.currentTimeMillis();
        buildTime = check - start;
        findMaximalPath();
        traverseTime = System.currentTimeMillis() - check;
    }

    private void buildReachableStateSpace() {
        StateSpace.State s = space.getEdenState();

        while (s != null) {
            traceState(s);

            // get the frontier information (call sites)
            FrontierInfo fs = getFrontierInfo(s);
            policy.frontierState = fs;
            policy.stackDelta = 0;

            // remove frontier information
            removeFrontierInfo(fs);
            // add this to explored states
            space.addState(fs.state);

            // compute the possible next states
            interpreter.computeNextStates(s);

            // get the next frontier state
            s = space.getFrontierState();
        }
    }

    private void findMaximalPath() {
        HashSet stack = new HashSet();
        StateSpace.State state = space.getEdenState();

        try {
            maxDepth = traverse(state, stack, 0);
        } catch (UnboundedStackException e) {
            maxDepth = Integer.MAX_VALUE;
        }
    }

    private int traverse(StateSpace.State s, HashSet stack, int cumul) {
        StateSpace.Link link = s.outgoing;
        if (s.mark != null) {
            return ((Integer) s.mark).intValue();
        }
        int max = cumul;
        stack.add(s);
        s.mark = new Integer(cumul);
        while (link != null) {
            StateSpace.State t = link.state;
            if (stack.contains(t)) { // cycle detected.
                Integer i = (Integer) t.mark;
                if (i.intValue() != cumul)
                    throw new UnboundedStackException();
            }
            int extra = traverse(t, stack, cumul + link.weight);
            if (extra > max) max = extra;
            link = link.next;
        }
        stack.remove(s);
        s.mark = new Integer(max);
        return max;
    }

    private class UnboundedStackException extends RuntimeException {

    }

    /**
     * The <code>report()</code> method generates a textual report after the
     * analysis has been completed. It reports information about states, reachable
     * states, time for analysis, and of course, the maximum stack size for the
     * program.
     */
    public void report() {
        printQuantity("Total cached states", "" + space.getTotalStateCount());
        printQuantity("Total reachable states", "" + space.getStatesInSpaceCount());
        printQuantity("Time to build graph", StringUtil.milliAsString(buildTime));
        printQuantity("Time to traverse graph", StringUtil.milliAsString(traverseTime));
        if (maxDepth == Integer.MAX_VALUE)
            printQuantity("Maximum stack depth", "unbounded");
        else
            printQuantity("Maximum stack depth", "" + maxDepth);
    }

    private void printQuantity(String q, String v) {
        Terminal.printBrightGreen(q);
        Terminal.println(": " + v);
    }

    private FrontierInfo getFrontierInfo(StateSpace.State s) {
        FrontierInfo fs = (FrontierInfo) frontierInfoMap.get(s);
        if (fs == null) {
            fs = new FrontierInfo(s);
            frontierInfoMap.put(s, fs);
        }
        return fs;
    }

    private void removeFrontierInfo(FrontierInfo fs) {
        frontierInfoMap.remove(fs.state);
    }


    /**
     * The <code>ContextSensitive</code> class implements the context-sensitive
     * analysis similar to 1-CFA. It is an implementation of the <code>Analyzer.Policy</code>
     * interface that determines what should be done in the case of a call, return, push,
     * pop, indirect call, etc. The context-sensitive analysis does not model the
     * contents of the stack, so pushes and pops essentially only modify the height of
     * the stack.
     *
     * @author Ben L. Titzer
     */
    public class ContextSensitivePolicy implements AnalyzerPolicy {

        public FrontierInfo frontierState;
        public int stackDelta;

        /**
         * The <code>call()</code> method is called by the abstract interpreter when it
         * encounters a call instruction within the program. Different policies may
         * handle calls differently. This context-sensitive analysis keeps track of
         * the call site every time a new method is entered. Thus the states coming
         * into a method call are merged according to the call site, instead of all
         * merged together.
         *
         * @param s              the current abstract state
         * @param target_address the concrete target address of the call
         * @return null because the correct state transitions are inserted by
         *         the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState call(MutableState s, int target_address) {
            s.setPC(target_address);
            StateSpace.State target = space.getStateFor(s);

            traceProducedState(target);
            addEdge("CALL", frontierState.state, target, 2);
            pushFrontier(target, new FrontierInfo.CallSiteList(frontierState, null));

            return null;
        }

        /**
         * The <code>interrupt()</code> is called by the abstract interrupt when it
         * encounters a place in the program when an interrupt might occur.
         *
         * @param s   the abstract state just before interrupt
         * @param num the interrupt number that might occur
         * @return the state of the program after the interrupt, null if there is
         *         no next state
         */
        public MutableState interrupt(MutableState s, int num) {
            s.setFlag_I(AbstractArithmetic.FALSE);
            s.setPC(0x0004 + num * 4);
            StateSpace.State target = space.getStateFor(s);

            traceProducedState(target);
            addEdge("INT", frontierState.state, target, 2);
            pushFrontier(target, new FrontierInfo.CallSiteList(frontierState, null));

            return null;
        }

        /**
         * The <code>ret()</code> method is called by the abstract interpreter when it
         * encounters a return within the program. In the context-sensitive analysis,
         * the return state must be connected with the call site. This is done by
         * accessing the call site list which is stored in this frontier state and
         * inserting edges for each call site.
         *
         * @param s the current abstract state
         * @return null because the correct state transitions are inserted by
         *         the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState ret(MutableState s) {
            FrontierInfo.CallSiteList list = frontierState.callsites;
            while (list != null) {
                MutableState retState = s.copy();
                FrontierInfo caller = list.caller;
                addReturnEdge(caller, retState);
                list = list.next;
            }
            addEdge("$RET", frontierState.state, returnState, 0);
            return null;
        }

        private void addReturnEdge(FrontierInfo caller, MutableState retState) {
            int pc = caller.state.getPC();
            int npc = pc + program.readInstr(pc).getSize();
            retState.setPC(npc);
            StateSpace.State immRetState = space.getStateFor(retState);
            traceProducedState(immRetState);
            addEdge("RET", caller.state, immRetState, 0);
            pushFrontier(immRetState, caller.callsites);
        }

        /**
         * The <code>reti()</code> method is called by the abstract interpreter when it
         * encounters a return from an interrupt within the program.
         *
         * @param s the current abstract state
         * @return null because the correct state transitions are inserted by
         *         the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState reti(MutableState s) {
            FrontierInfo.CallSiteList list = frontierState.callsites;
            while (list != null) {
                MutableState retState = s.copy();
                retState.setFlag_I(AbstractArithmetic.TRUE);
                FrontierInfo caller = list.caller;
                retState.setPC(caller.state.getPC());
                StateSpace.State immRetState = space.getStateFor(retState);
                traceProducedState(immRetState);
                addEdge("RETI", caller.state, immRetState, 0);
                pushFrontier(immRetState, caller.callsites);
                list = list.next;
            }
            addEdge("$RETI", frontierState.state, returnIState, 0);
            return null;
        }

        /**
         * The <code>indirectCall()</code> method is called by the abstract interpreter
         * when it encounters an indirect call within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         *
         * @param s        the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi  the (abstract) high byte of the address
         * @return null because the correct state transitions are inserted by
         *         the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState indirectCall(MutableState s, char addr_low, char addr_hi) {
            throw new Error("indirect calls not supported");
        }

        /**
         * The <code>indirectJump()</code> method is called by the abstract interpreter
         * when it encounters an indirect jump within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         *
         * @param s        the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi  the (abstract) high byte of the address
         * @return null because the correct state transitions are inserted by
         *         the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState indirectJump(MutableState s, char addr_low, char addr_hi) {
            throw new Error("indirect jumps not supported");
        }

        /**
         * The <code>indirectCall()</code> method is called by the abstract interpreter
         * when it encounters an indirect call within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         *
         * @param s        the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi  the (abstract) high byte of the address
         * @param ext      the (abstract) extended part of the address
         * @return null because the correct state transitions are inserted by
         *         the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState indirectCall(MutableState s, char addr_low, char addr_hi, char ext) {
            throw new Error("indirect calls not supported");
        }

        /**
         * The <code>indirectJump()</code> method is called by the abstract interpreter
         * when it encounters an indirect jump within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         *
         * @param s        the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi  the (abstract) high byte of the address
         * @param ext      the (abstract) extended part of the address
         * @return null because the correct state transitions are inserted by
         *         the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState indirectJump(MutableState s, char addr_low, char addr_hi, char ext) {
            throw new Error("indirect jumps not supported");
        }

        /**
         * The <code>push()</code> method is called by the abstract interpreter when
         * a push to the stack is encountered in the program. The policy can then choose
         * what outgoing and/or modelling of the stack needs to be done.
         *
         * @param s   the current abstract state
         * @param val the abstract value to push onto the stack
         */
        public void push(MutableState s, char val) {
            stackDelta = 1;
        }

        /**
         * The <code>pop()</code> method is called by the abstract interpreter when
         * a pop from the stack is ecountered in the program. The policy can then
         * choose to either return whatever information it has about the stack
         * contents, or return an UNKNOWN value.
         *
         * @param s the current abstract state
         * @return the abstract value popped from the stack
         */
        public char pop(MutableState s) {
            stackDelta = -1;
            return AbstractArithmetic.UNKNOWN;
        }

        /**
         * The <code>pushState</code> method is called by the abstract interpreter when
         * a state is forked by the abstract interpreter (for example when a branch
         * condition is not known and both branches must be taken.
         *
         * @param newState the new state created
         */
        public void pushState(MutableState newState) {
            StateSpace.State t = space.getStateFor(newState);
            traceProducedState(t);
            addEdge(null, frontierState.state, t, stackDelta);
            pushFrontier(t, frontierState.callsites);
        }

        private void pushFrontier(StateSpace.State t, FrontierInfo.CallSiteList l) {
            if (space.isExplored(t)) {
                // TODO: find reachable return states
            } else {
                space.addFrontier(t);
                FrontierInfo fs = getFrontierInfo(t);
                fs.callsites = l;
            }
        }

        private void addEdge(String type, StateSpace.State s, StateSpace.State t, int weight) {
            traceEdge(type, s, t, weight);
            space.addEdge(s, t, weight);
        }

    }

    //-----------------------------------------------------------------------
    //             D E B U G G I N G   A N D   T R A C I N G
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------


    private void traceState(StateSpace.State s) {
        if (TRACE) {
            Instr instr = program.readInstr(s.getPC());
            String str = StringUtil.leftJustify(instr.toString(), 14);
            printState(str, s);
        }
    }

    private void traceProducedState(StateSpace.State s) {
        if (TRACE) {
            String str;
            if (space.isExplored(s)) {
                str = "        E ==> ";
            }
            if (space.isFrontier(s)) {
                str = "        F ==> ";

            } else {
                str = "        N ==> ";
            }
            printState(str, s);
        }
    }

    private void traceEdge(String type, StateSpace.State s, StateSpace.State t, int weight) {
        if (!TRACE) return;
        Terminal.print("[");
        Terminal.printBrightCyan(s.getUniqueName());
        Terminal.print("] --(");
        if (type != null) Terminal.print(type + " ");
        if (weight > 0) Terminal.print("+");
        Terminal.print(weight + ")--> ");
        Terminal.print("[");
        Terminal.printBrightCyan(t.getUniqueName());
        Terminal.println("]");
    }

    private void printState(String beg, StateSpace.State s) {
        Terminal.printBrightRed(beg);

        Terminal.print("[");
        Terminal.printBrightGreen(StringUtil.toHex(s.getPC(), 4));
        Terminal.print("|");
        Terminal.printBrightCyan(s.getUniqueName());
        Terminal.print("] ");

        for (int cntr = 0; cntr < 8; cntr++) {
            Terminal.print(AbstractArithmetic.toString(s.getRegisterAV(cntr)));
            Terminal.print(" ");
        }
        Terminal.nextln();

        Terminal.print("                [");
        Terminal.printBrightGreen("SREG");
        Terminal.print(":");
        Terminal.print(AbstractArithmetic.toString(s.getSREG()));
        Terminal.print("] ");
        for (int cntr = 8; cntr < 16; cntr++) {
            Terminal.print(AbstractArithmetic.toString(s.getRegisterAV(cntr)));
            Terminal.print(" ");
        }

        Terminal.nextln();

        Terminal.print("               [");
        Terminal.printBrightGreen("EIMSK");
        Terminal.print(":");
        Terminal.print(AbstractArithmetic.toString(s.getIORegisterAV(IORegisterConstants.EIMSK)));
        Terminal.print("] ");
        for (int cntr = 16; cntr < 24; cntr++) {
            Terminal.print(AbstractArithmetic.toString(s.getRegisterAV(cntr)));
            Terminal.print(" ");
        }

        Terminal.nextln();

        Terminal.print("                                ");
        for (int cntr = 24; cntr < 32; cntr++) {
            Terminal.print(AbstractArithmetic.toString(s.getRegisterAV(cntr)));
            Terminal.print(" ");
        }

        Terminal.nextln();
    }

}
