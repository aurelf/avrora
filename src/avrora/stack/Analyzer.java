package avrora.stack;

import avrora.core.Program;
import avrora.core.InstrVisitor;
import avrora.core.Instr;
import avrora.core.Register;
import avrora.sim.IORegisterConstants;

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
    AbstractInterpreter interpreter;
    Frontier frontier;

    public static class CallSiteList {
        public final ImmutableState state;
        public final CallSiteList next;

        CallSiteList(ImmutableState s, CallSiteList n) {
            state = s;
            next = n;
        }
    }

    public static class FrontierState {
        public ImmutableState state;
        public CallSiteList callsites;
        protected FrontierState next;
    }

    /**
     * The <code>Frontier</code> class is a collection of states that
     * are on the edge of the explored state space. They are immutable and
     * have had their incoming edges computed thus far, but their outgoing
     * edges have not yet been computed. This is a worklist which the
     * algorithm continually pulls states off of.
     */
    protected static class Frontier {

        FrontierState head;

        protected void add(ImmutableState s) {
            FrontierState link = new FrontierState();

            link.state = s;
            link.callsites = null;
            link.next = head;
            head = link;
        }

        protected void add(FrontierState s) {
            s.next = head;
            head = s;
        }

        protected boolean isEmpty() {
            return head != null;
        }

        protected FrontierState choose() {
            if (head == null) return null;
            FrontierState res = head;
            head = head.next;
            res.next = null;
            return res;
        }

        protected FrontierState getFrontierState(MutableState s) {
            for ( FrontierState pos = head; pos != null; pos = pos.next ) {
                if ( pos.state.equals(s) ) return pos;
            }
            return null;
        }

        protected boolean contains(MutableState s) {
            return getFrontierState(s) != null;
        }
    }

    public Analyzer(Program p) {
        program = p;
        space = new StateSpace();
        interpreter = new AbstractInterpreter(program, new ContextSensitivePolicy());
        frontier = new Frontier();
    }

    /**
     * The <code>run()</code> method begins the analysis. The entrypoint of the program
     * with an initial default state serves as the first state to start exploring.
     */
    public void run() {
        MutableState s = new MutableState();
        frontier.add(space.getImmutableStateFor(s));

        while (!frontier.isEmpty()) {
            FrontierState state = frontier.choose();
            interpreter.nextState(state);
        }
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
         */
        public void call(MutableState s, int target_address) {
        }

        /**
         * The <code>ret()</code> method is called by the abstract interpreter when it
         * encounters a return within the program. In the context-sensitive analysis,
         * the return state must be connected with the call site. This is done by
         * accessing the call site list which is stored in this frontier state and
         * inserting edges for each call site.
         *
         * @param s the current abstract state
         */
        public void ret(MutableState s) {
        }

        /**
         * The <code>reti()</code> method is called by the abstract interpreter when it
         * encounters a return from an interrupt within the program.
         * @param s the current abstract state
         */
        public void reti(MutableState s) {
        }

        /**
         * The <code>indirectCall()</code> method is called by the abstract interpreter
         * when it encounters an indirect call within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         * @param s the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi the (abstract) high byte of the address
         */
        public void indirectCall(MutableState s, char addr_low, char addr_hi) {
        }

        /**
         * The <code>indirectJump()</code> method is called by the abstract interpreter
         * when it encounters an indirect jump within the program. The abstract values
         * of the address are given as parameters, so that a policy can choose to compute
         * possible targets or be conservative or whatever it so chooses.
         * @param s the current abstract state
         * @param addr_low the (abstract) low byte of the address
         * @param addr_hi the (abstract) high byte of the address
         */
        public void indirectJump(MutableState s, char addr_low, char addr_hi) {
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
         */
        public void indirectCall(MutableState s, char addr_low, char addr_hi, char ext) {
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
         */
        public void indirectJump(MutableState s, char addr_low, char addr_hi, char ext) {
        }

        /**
         * The <code>pushState</code> method is called by the abstract interpreter when
         * a state is forked by the abstract interpreter (for example when a branch
         * condition is not known and both branches must be taken.
         * @param oldState the old state
         * @param newState the new state created
         */
        public void pushState(FrontierState oldState, MutableState newState) {
        }

        /**
         * The <code>push()</code> method is called by the abstract interpreter when
         * a push to the stack is encountered in the program. The policy can then choose
         * what outgoing and/or modelling of the stack needs to be done.
         * @param s the current abstract state
         * @param val the abstract value to push onto the stack
         */
        public void push(MutableState s, char val) {
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
            return AbstractArithmetic.UNKNOWN;
        }


    }
}
