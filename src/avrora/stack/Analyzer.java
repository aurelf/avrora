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
import avrora.Avrora;

import java.util.*;

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
    boolean unbounded;
    Path maximalPath;
    int maxDepth;

    public final int NORMAL_EDGE = 0;
    public final int PUSH_EDGE = 1;
    public final int POP_EDGE = 2;
    public final int CALL_EDGE = 3;
    public final int INT_EDGE = 4;
    public final int RET_EDGE = 5;
    public final int RETI_EDGE = 6;
    public final int SPECIAL_EDGE = 7;

    public final String[] EDGE_NAMES = {"", "PUSH", "POP", "CALL", "INT", "RET", "RETI", "SPECIAL"};

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
            policy.edgeType = NORMAL_EDGE;

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
        // the stack hashmap contains a mapping between states on the stack
        // and the current stack depth at which they are being explored
        HashMap stack = new HashMap();
        StateSpace.State state = space.getEdenState();

        try {
            maximalPath = findMaximalPath(state, stack, 0);
//            maxDepth = traverse(state, stack, 0);
        } catch (UnboundedStackException e) {
            unbounded = true;
            maximalPath = e.path;
//            maxDepth = Integer.MAX_VALUE;
        }
    }

    private int traverse(StateSpace.State s, HashMap stack, int depth) {
        // record this node and the stack depth at which we first encounter it
        stack.put(s, new Integer(depth));

        int maxdepth = 0;
        for (StateSpace.Link link = s.outgoing; link != null; link = link.next) {

            StateSpace.State t = link.target;
            if (stack.containsKey(t)) {
                // cycle detected. check that the depth when reentering is the same
                int prevdepth = ((Integer) stack.get(t)).intValue();
                if (depth + link.weight != prevdepth)
                    throw new UnboundedStackException(null);
            } else {
                int extra = link.weight; // maximum added stack depth by following this link

                if (link.target.mark instanceof Integer) {
                    // node has already been visited and marked with the
                    // maximum amount of stack depth that it can add.
                    extra += ((Integer) link.target.mark).intValue();
                } else {
                    // node has not been seen before, traverse it
                    extra += traverse(link.target, stack, depth + link.weight);
                }

                // remember the most added stack depth from following any of the links
                if (extra > maxdepth) {
                    maxdepth = extra;
                }
            }
        }
        // we are finished with this node, remember how much deeper it can take us
        stack.remove(s);
        s.mark = new Integer(maxdepth);
        return maxdepth;
    }

    private class Path {
        final int depth;
        final StateSpace.State state;
        final StateSpace.Link link;
        final Path tail;

        Path(int d, StateSpace.State s, StateSpace.Link l, Path p) {
            state = s;
            depth = d;
            link = l;
            tail = p;
        }
    }

    private Path findMaximalPath(StateSpace.State s, HashMap stack, int depth) {
        // record this node and the stack depth at which we first encounter it
        stack.put(s, new Integer(depth));

        int maxdepth = 0;
        Path maxtail = null;
        StateSpace.Link maxlink = null;
        for (StateSpace.Link link = s.outgoing; link != null; link = link.next) {

            StateSpace.State t = link.target;
            if (stack.containsKey(t)) {
                // cycle detected. check that the depth when reentering is the same
                int prevdepth = ((Integer) stack.get(t)).intValue();
                if (depth + link.weight != prevdepth) {
                    // we are finished with this node, remember how much deeper it can take us
                    stack.remove(s);
                    throw new UnboundedStackException(new Path(depth + link.weight, s, link, null));
                }
            } else {
                Path tail;

                if (link.target.mark instanceof Path) {
                    // node has already been visited and marked with the
                    // maximum amount of stack depth that it can add.
                    tail = ((Path) link.target.mark);
                } else {
                    // node has not been seen before, traverse it
                    try {
                        tail = findMaximalPath(link.target, stack, depth + link.weight);
                    } catch ( UnboundedStackException e) {
                        e.path = new Path(depth + link.weight, s, link, e.path);
                        stack.remove(s);
                        throw e;
                    }
                }

                int extra = link.weight + tail.depth; // maximum added stack depth by following this link

                // remember the most added stack depth from following any of the links
                if (extra > maxdepth) {
                    maxdepth = extra;
                    maxtail = tail;
                    maxlink = link;
                }
            }
        }
        // we are finished with this node, remember how much deeper it can take us
        stack.remove(s);
        Path maxpath = new Path(maxdepth, s, maxlink, maxtail);
        s.mark = maxpath;
        return maxpath;
    }


    private class UnboundedStackException extends RuntimeException {
        Path path;

        UnboundedStackException(Path p) {
            path = p;
        }
    }

    /**
     * The <code>report()</code> method generates a textual report after the
     * analysis has been completed. It reports information about states, reachable
     * states, time for analysis, and of course, the maximum stack size for the
     * program.
     */
    public void report() {
        printQuantity("Total cached states   ", "" + space.getTotalStateCount());
        printQuantity("Total reachable states", "" + space.getStatesInSpaceCount());
        printQuantity("Time to build graph   ", StringUtil.milliAsString(buildTime));
        printQuantity("Time to traverse graph", StringUtil.milliAsString(traverseTime));
        if (maxDepth == Integer.MAX_VALUE)
            printQuantity("Maximum stack depth", "unbounded");
        else
            printQuantity("Maximum stack depth", "" + maxDepth);
        printPath(maximalPath);
    }

    private void printPath(Path p) {
        for ( Path path = p; path != null; path = path.tail ) {
            printFullState("Depth: "+path.depth, path.state);
            printEdge(path.state, path.link.type, path.link.weight, path.link.target);
        }
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
        int edgeType;

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
            addEdge(CALL_EDGE, frontierState.state, target, 2);
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
            s.setPC((num - 1) * 4);
            StateSpace.State target = space.getStateFor(s);

            traceProducedState(target);
            addEdge(INT_EDGE, frontierState.state, target, 2);
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
            addReturnEdges(s, frontierState.callsites);
            addEdge(SPECIAL_EDGE, frontierState.state, returnState, 0);
            return null;
        }

        private void addReturnEdges(MutableState s, FrontierInfo.CallSiteList list) {
            while (list != null) {
                MutableState retState = s.copy();
                FrontierInfo caller = list.caller;
                addReturnEdge(caller, retState);
                list = list.next;
            }
        }

        private void addReturnEdge(FrontierInfo caller, MutableState retState) {
            int pc = caller.state.getPC();
            int npc = pc + program.readInstr(pc).getSize();
            retState.setPC(npc);
            StateSpace.State immRetState = space.getStateFor(retState);
            traceProducedState(immRetState);
            addEdge(RET_EDGE, caller.state, immRetState, 0);
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
            addInterruptReturnEdges(s, frontierState.callsites);
            addEdge(SPECIAL_EDGE, frontierState.state, returnIState, 0);
            return null;
        }

        private void addInterruptReturnEdges(MutableState s, FrontierInfo.CallSiteList list) {
            while (list != null) {
                MutableState retState = s.copy();
                retState.setFlag_I(AbstractArithmetic.TRUE);
                FrontierInfo caller = list.caller;
                retState.setPC(caller.state.getPC());
                StateSpace.State immRetState = space.getStateFor(retState);
                traceProducedState(immRetState);
                addEdge(RETI_EDGE, caller.state, immRetState, 0);
                pushFrontier(immRetState, caller.callsites);
                list = list.next;
            }
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
            int callsite = s.pc;
            List iedges = program.getIndirectEdges(callsite);
            if (iedges == null)
                throw Avrora.failure("No control flow information for indirect jump at: " +
                        StringUtil.addrToString(callsite));
            Iterator i = iedges.iterator();
            while (i.hasNext()) {
                int target_address = ((Integer) i.next()).intValue();
                call(s, target_address);
            }

            return null;
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
            int callsite = s.pc;
            List iedges = program.getIndirectEdges(callsite);
            if (iedges == null)
                throw Avrora.failure("No control flow information for indirect call at: " +
                        StringUtil.addrToString(callsite));
            Iterator i = iedges.iterator();
            while (i.hasNext()) {
                int target_address = ((Integer) i.next()).intValue();
                s.setPC(target_address);
                pushState(s);
            }

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
            edgeType = PUSH_EDGE;
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
            edgeType = POP_EDGE;
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
            addEdge(edgeType, frontierState.state, t, stackDelta);
            pushFrontier(t, frontierState.callsites);
        }

        private void pushFrontier(StateSpace.State t, FrontierInfo.CallSiteList l) {
            // CASE 4: self loop
            if (t == frontierState.state) return;

            if (space.isExplored(t)) {
                // CASE 3: state is already explored
                retraverseExploredSpace(t, l);
            } else if (space.isFrontier(t)) {
                // CASE 2: state is already on frontier
                mergeFrontierStateLists(t, l);
            } else {
                // CASE 1: new state, add to frontier
                space.addFrontier(t);
                FrontierInfo fs = getFrontierInfo(t);
                fs.callsites = l;
            }
        }

        private void addEdge(int type, StateSpace.State s, StateSpace.State t, int weight) {
            traceEdge(type, s, t, weight);
            space.addEdge(s, t, type, weight);
        }

        private void retraverseExploredSpace(StateSpace.State t, FrontierInfo.CallSiteList newCalls) {
            Object mark = new Object();

            propagate(t, newCalls, mark);
        }

        // propagate call sites to all reachable return states
        private void propagate(StateSpace.State t, FrontierInfo.CallSiteList newCalls, Object mark) {
            if (t.mark == mark) return;
            t.mark = mark;

            for (StateSpace.Link link = t.outgoing; link != null; link = link.next) {

                // do not follow call edges
                if (link.type == CALL_EDGE) continue;

                StateSpace.State r = link.target;
                if (r == returnState) {
                    // add return edge
                    addReturnEdges(t.copy(), newCalls);
                } else if (r == returnIState) {
                    // add return from interrupt edge
                    addInterruptReturnEdges(t.copy(), newCalls);
                } else if (space.isFrontier(r)) {
                    // encountered a frontier state, merge callsites
                    mergeFrontierStateLists(r, newCalls);
                } else {
                    propagate(r, newCalls, mark);
                }
            }
        }

        private void mergeFrontierStateLists(StateSpace.State t, FrontierInfo.CallSiteList newCalls) {
            FrontierInfo fi = getFrontierInfo(t);

            if ( fi.callsites == newCalls ) return;
            
            HashSet callers = new HashSet();
            for ( FrontierInfo.CallSiteList list = fi.callsites; list != null; list = list.next ) {
                callers.add(list.caller);
            }

            for ( FrontierInfo.CallSiteList list = newCalls; list != null; list = list.next ) {
                if ( !callers.contains(list.caller) )
                    fi.callsites = new FrontierInfo.CallSiteList(list.caller, fi.callsites);
                callers.add(list.caller);
            }
        }

    }

    //-----------------------------------------------------------------------
    //             D E B U G G I N G   A N D   T R A C I N G
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------


    private void traceState(StateSpace.State s) {
        if (TRACE) {
            printFullState("Exploring", s);
        }
    }

    private void printFullState(String head, StateSpace.State s) {
        Terminal.print(head+" ");
        printStateName(s);
        Terminal.nextln();
        Instr instr = program.readInstr(s.getPC());
        String str = StringUtil.leftJustify(instr.toString(), 14);
        printState(str, s);
    }

    private void traceProducedState(StateSpace.State s) {
        if (TRACE) {
            String str;
            if (space.isExplored(s)) {
                str = "        E ==> ";
            } else if (space.isFrontier(s)) {
                str = "        F ==> ";

            } else {
                str = "        N ==> ";
            }
            printState(str, s);
        }
    }

    private void traceEdge(int type, StateSpace.State s, StateSpace.State t, int weight) {
        if (!TRACE) return;
        Terminal.print("adding edge ");
        printEdge(s, type, weight, t);
    }

    private void printEdge(StateSpace.State s, int type, int weight, StateSpace.State t) {
        printStateName(s);
        Terminal.print(" --(");
        Terminal.print(EDGE_NAMES[type]);
        if (weight > 0) Terminal.print("+");
        Terminal.print(weight + ")--> ");
        printStateName(t);
        Terminal.nextln();
    }

    private void printStateName(StateSpace.State t) {
        Terminal.print("[");
        Terminal.printBrightGreen(StringUtil.toHex(t.getPC(), 4));
        Terminal.print("|");
        Terminal.printBrightCyan(t.getUniqueName());
        Terminal.print("] ");
    }

    private void printState(String beg, StateSpace.State s) {
        Terminal.printBrightRed(beg);

        printStateName(s);

        for (int cntr = 0; cntr < 8; cntr++) {
            Terminal.print(toString(s.getRegisterAV(cntr)));
            Terminal.print(" ");
        }
        Terminal.nextln();

        Terminal.print("                [");
        Terminal.printBrightGreen("SREG");
        Terminal.print(":");
        Terminal.print(AbstractArithmetic.toString(s.getSREG()));
        Terminal.print("] ");
        for (int cntr = 8; cntr < 16; cntr++) {
            Terminal.print(toString(s.getRegisterAV(cntr)));
            Terminal.print(" ");
        }

        Terminal.nextln();

        Terminal.print("               [");
        Terminal.printBrightGreen("EIMSK");
        Terminal.print(":");
        Terminal.print(AbstractArithmetic.toString(s.getIORegisterAV(IORegisterConstants.EIMSK)));
        Terminal.print("] ");
        for (int cntr = 16; cntr < 24; cntr++) {
            Terminal.print(toString(s.getRegisterAV(cntr)));
            Terminal.print(" ");
        }

        Terminal.nextln();

        Terminal.print("               [");
        Terminal.printBrightGreen("TIMSK");
        Terminal.print(":");
        Terminal.print(AbstractArithmetic.toString(s.getIORegisterAV(IORegisterConstants.TIMSK)));
        Terminal.print("] ");
        for (int cntr = 24; cntr < 32; cntr++) {
            Terminal.print(toString(s.getRegisterAV(cntr)));
            Terminal.print(" ");
        }

        Terminal.nextln();
    }

    private String toString(char av) {
        if (av == AbstractArithmetic.ZERO)
            return "       0";
        else if (av == AbstractArithmetic.UNKNOWN) return "       .";
        return AbstractArithmetic.toString(av);
    }

}
