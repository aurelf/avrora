package avrora.stack;

import avrora.core.Program;
import avrora.core.InstrVisitor;
import avrora.core.Instr;
import avrora.core.Register;
import avrora.sim.IORegisterConstants;

import java.util.HashSet;
import java.util.HashMap;

import vpc.VPCBase;
import vpc.util.StringUtil;

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

    protected final StateSpace.SpecialState returnState;
    protected final StateSpace.SpecialState returnIState;

    HashMap frontierInfoMap;


    public static class FrontierInfo {
        public static class CallSiteList {
            public final StateSpace.State state;
            public final CallSiteList next;

            CallSiteList(StateSpace.State s, CallSiteList n) {
                state = s;
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

        void addCallSite(StateSpace.State s) {
            callsites = new CallSiteList(s, callsites);
        }
    }

    public Analyzer(Program p) {
        program = p;
        space = new StateSpace();
        policy = new ContextSensitivePolicy();
        interpreter = new AbstractInterpreter(program, policy);
        frontierInfoMap = new HashMap();
        returnState = space.makeSpecialState("RET");
        returnIState = space.makeSpecialState("RETI");
    }

    /**
     * The <code>run()</code> method begins the analysis. The entrypoint of the program
     * with an initial default state serves as the first state to start exploring.
     */
    public void run() {
        String headerStr = AbstractState.getHeaderString();
        int states = 0;
        StateSpace.State s = space.getEdenState();

        while ( s != null ) {
            if ( VPCBase.VERBOSE ) {
                String i = program.readInstr(s.getPC()).getVariant();
                i = StringUtil.leftJustify(i, 6);
                if ( states++ % 32 == 0 ) {
                    VPCBase.verboseln("INSTR "+headerStr);
                    VPCBase.verbosesep(headerStr.length()+5);
                }
                VPCBase.verboseln(i+s.toShortString());
            }

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

    private FrontierInfo getFrontierInfo(StateSpace.State s) {
        FrontierInfo fs = (FrontierInfo)frontierInfoMap.get(s);
        if ( fs == null ) fs = new FrontierInfo(s);
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
         * @param s the current abstract state
         * @param target_address the concrete target address of the call
         * @return null because the correct state transitions are inserted by
         * the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState call(MutableState s, int target_address) {
            s.setPC(target_address);
            StateSpace.State target = space.getStateFor(s);

            if ( space.isExplored(target) ) {
                // TODO: find reachable return states
                return null;
            }

            FrontierInfo info = getFrontierInfo(target);
            info.addCallSite(frontierState.state);
            space.addFrontier(target);
            space.addEdge(frontierState.state, target, 2);

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
         * the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState ret(MutableState s) {
            FrontierInfo.CallSiteList list = frontierState.callsites;
            while ( list != null ) {
                MutableState retState = s.copy();
                int pc = list.state.getPC();
                int npc = pc + program.readInstr(pc).getSize();
                retState.setPC(npc);
                space.addEdge(list.state, space.getStateFor(retState));
            }
            space.addEdge(frontierState.state, returnState);
            return null;
        }

        /**
         * The <code>reti()</code> method is called by the abstract interpreter when it
         * encounters a return from an interrupt within the program.
         * @param s the current abstract state
         * @return null because the correct state transitions are inserted by
         * the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState reti(MutableState s) {
            FrontierInfo.CallSiteList list = frontierState.callsites;
            while ( list != null ) {
                MutableState retState = s.copy();
                retState.setFlag_I(AbstractArithmetic.TRUE);
                int pc = list.state.getPC();
                int npc = pc + program.readInstr(pc).getSize();
                retState.setPC(npc);
                space.addEdge(list.state, space.getStateFor(retState));
            }
            space.addEdge(frontierState.state, returnIState);
            return null;
        }

        /**
         * The <code>indirectCall()</code> method is called by the abstract interpreter
         * when it encounters an indirect call within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         * @param s the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi the (abstract) high byte of the address
         * @return null because the correct state transitions are inserted by
         * the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState indirectCall(MutableState s, char addr_low, char addr_hi) {
            throw new Error("indirect calls not supported");
        }

        /**
         * The <code>indirectJump()</code> method is called by the abstract interpreter
         * when it encounters an indirect jump within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         * @param s the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi the (abstract) high byte of the address
         * @return null because the correct state transitions are inserted by
         * the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState indirectJump(MutableState s, char addr_low, char addr_hi) {
            throw new Error("indirect jumps not supported");
        }

        /**
         * The <code>indirectCall()</code> method is called by the abstract interpreter
         * when it encounters an indirect call within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         * @param s the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi the (abstract) high byte of the address
         * @param ext the (abstract) extended part of the address
         * @return null because the correct state transitions are inserted by
         * the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState indirectCall(MutableState s, char addr_low, char addr_hi, char ext) {
            throw new Error("indirect calls not supported");
        }

        /**
         * The <code>indirectJump()</code> method is called by the abstract interpreter
         * when it encounters an indirect jump within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         * @param s the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi the (abstract) high byte of the address
         * @param ext the (abstract) extended part of the address
         * @return null because the correct state transitions are inserted by
         * the policy, and the abstract interpreter should not be concerned.
         */
        public MutableState indirectJump(MutableState s, char addr_low, char addr_hi, char ext) {
            throw new Error("indirect jumps not supported");
        }

        /**
         * The <code>push()</code> method is called by the abstract interpreter when
         * a push to the stack is encountered in the program. The policy can then choose
         * what outgoing and/or modelling of the stack needs to be done.
         * @param s the current abstract state
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
         * @param newState the new state created
         */
        public void pushState(MutableState newState) {
            StateSpace.State t = space.getStateFor(newState);
            space.addEdge(frontierState.state, t, stackDelta);
            if ( !space.isExplored(t) )
                space.addFrontier(t);
        }


    }
}
