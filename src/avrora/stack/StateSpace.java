package avrora.stack;

import avrora.core.Program;
import avrora.sim.IORegisterConstants;
import avrora.util.StringUtil;

import java.util.HashMap;

/**
 * The <code>StateSpace</code> class represents the reachable state space
 * as it is explored by the <code>Analyzer</code> class. It stores reachable
 * states and the outgoing edges that connect them.
 *
 * @author Ben L. Titzer
 */
public class StateSpace {

    /**
     * The <code>Link</code> inner class represents an edge between two states
     * within the state space. The edge's destination and weight is specified.
     * The source is known by context.
     */
    public class Link {
        public final State state;
        public final Link next;
        public final int weight;

        Link(State t, Link n, int w) {
            state = t;
            next = n;
            weight = w;
        }
    }

    private long uidCount;

    /**
     * The <code>State</code> class represents an immutable state within the state
     * space of the program. Such a state is cached and cannot be modified. It
     * contains a unique identifier, a mark for graph traversals, and a list of
     * outgoing edges.
     */
    public class State extends AbstractState implements IORegisterConstants {

        private final int hashCode;
        public final long UID;

        boolean inSpace;
        boolean onFrontier;

        State() {
            hashCode = computeHashCode();
            UID = uidCount++;
        }

        State(MutableState s) {
            pc = s.pc;
            av_SREG = s.av_SREG;
            av_EIMSK = s.av_EIMSK;
            av_REGISTERS = new char[NUM_REGS];
            for (int cntr = 0; cntr < NUM_REGS; cntr++) {
                av_REGISTERS[cntr] = s.av_REGISTERS[cntr];
            }
            hashCode = computeHashCode();
            UID = uidCount++;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof State)) return false;
            State i = (State) o;
            if (this.pc != i.pc) return false;
            if (this.av_SREG != i.av_SREG) return false;
            for (int cntr = 0; cntr < NUM_REGS; cntr++)
                if (this.av_REGISTERS[cntr] != i.av_REGISTERS[cntr]) return false;
            return true;
        }

        /**
         * The <code>mark</code> field is used by graph traversal algorithms to
         * detect cycles and terminate traversals. Concurrent traversal is not
         * supported.
         */
        public Object mark;

        /**
         * The <code>outgoing</code> field is a reference to the head of the
         * list of outgoing edges from this state.
         */
        public Link outgoing;

        void addEdge(State t, int weight) {
            outgoing = new Link(t, outgoing, weight);
        }

        /**
         * The <code>getUniqueName()</code> gets a string that uniquely identifies this
         * state. For immutable states, this is simply the UID. For special states, this
         * is the name of the special state.
         * @return a unique identifying string for this state
         */
        public String getUniqueName() {
            return StringUtil.toHex(UID, 10);
        }

    }

    /**
     * The <code>SpecialState</code> class represents a special state within the
     * state space such as a RETURN state or a IRETURN state that can be used as
     * markers for smart traversal algorithms. These states are NOT stored in
     * the internal state cache.
     */
    class SpecialState extends State {
        /**
         * The <code>stateName</code> field records the name of this special state.
         */
        public final String stateName;

        SpecialState(String name) {
            stateName = name;
        }

        /**
         * The <code>getUniqueName()</code> gets a string that uniquely identifies this
         * state. For special states, this is the name of the special state.
         * @return a unique identifying string for this state
         */
        public String getUniqueName() {
            return stateName;
        }
    }


    private HashMap stateMap;
    private Link frontier;
    private final State edenState;
    private final Program program;
    private long statesInSpace;
    private long totalStateCount;
    private long specialStates;

    /**
     * The constructor for the <code>StateSpace</code> accepts a program as a parameter.
     * This is currently unused, but is reserved for use later.
     * @param p the program to create the state space for
     */
    public StateSpace(Program p) {
        stateMap = new HashMap();
        edenState = getStateFor(new MutableState());
        program = p;
    }

    /**
     * The <code>getEdenState()</code> method gets the starting state of the abstract
     * interpretation.
     * @return the initial state to begin abstract interpretation
     */
    public State getEdenState() {
        return edenState;
    }

    /**
     * The <code>getFrontierState()</code> chooses a state off of the state frontier,
     * removes it from the state frontier, and returns it. If there are no states
     * left on the state frontier, this method returns null. Note that initially there
     * are no states on the frontier, not even the eden state.
     *
     * @return one of the states on the current state frontier; null if there are none.
     */
    public State getFrontierState() {
        Link head = frontier;
        if (head == null) return null;

        frontier = frontier.next;
        head.state.onFrontier = false;
        return head.state;
    }

    /**
     * The <code>isExplored()</code> method checks whether the specified state has been
     * added to this state space (i.e. it has been explored). A state can be added at most
     * once and cannot be removed from the state space.
     * @param s the state to check
     * @return true if the specified state has been explored; false otherwise
     */
    public boolean isExplored(State s) {
        return s.inSpace;
    }

    /**
     * The <code>isFrontier()</code> method checks whether the specified state is currently
     * on the state frontier.
     * @param s the state to check
     * @return true if the specified state is currently on the frontier; false otherwise
     */
    public boolean isFrontier(State s) {
        return s.onFrontier;
    }

    /**
     * The <code>addState()</code> method adds a state to the state space (marks it as explored).
     * A state can be added at most once and cannot be removed once it has been added. Note that
     * being cached does not imply that the state is in the state space.
     * @param s the state to add to the state space.
     * @return true if this state is already in the state space; false otherwise
     */
    public boolean addState(State s) {
        boolean wasBefore = s.inSpace;
        if (!wasBefore) statesInSpace++;
        s.inSpace = true;
        s.onFrontier = false;
        return wasBefore;
    }

    /**
     * The <code>addFrontier</code> method adds a state to the frontier.
     * @param s the state to add
     * @return true if the state was already on the frontier; false otherwise
     */
    public boolean addFrontier(State s) {
        if (s.onFrontier) return true;
        frontier = new Link(s, frontier, 0);
        s.onFrontier = true;
        return false;
    }

    /**
     * The <code>makeSpecialState()</code> method creates a special state that
     * represents something important to an analysis tool, such as a RETURN state.
     * These special states are not stored in the state cache.
     * @param name the name of the special state
     * @return an instance of the <code>SpecialState</code> class that is the
     * newly created special states.
     */
    public SpecialState makeSpecialState(String name) {
        specialStates++;
        return new SpecialState(name);
    }

    /**
     * The <code>getStateFor()</code> method searches the state cache for an
     * immutable state that corresponds to the given mutable state. If no
     * immutable state exists in the cache, one will be created and inserted.
     * @param s the state to search for
     * @return an instance of the <code>StateSpace.State</code> immutable state
     * that corresponds to the given mutable state
     */
    public State getStateFor(MutableState s) {
        State is = new State(s);
        State cs = (State) stateMap.get(is);

        if (cs != null) {
            // if the state is already in the state map, return original
            return cs;
        } else {
            totalStateCount++;
            // the state is new, put it in the map and return it.
            stateMap.put(is, is);
            return is;
        }
    }

    /**
     * The <code>addEdge()</code> method creates an edge between the specified states
     * with the given weight. No checking for duplicate edges is done.
     * @param s the source state
     * @param t the destination state
     * @param weight the weight to assign to the edge
     */
    public void addEdge(State s, State t, int weight) {
        s.addEdge(t, weight);
    }

    /**
     * The <code>addEdge()</code> method creates an edge between the specified states
     * with weight 0. No checking for duplicate edges is done.
     * @param s the source state
     * @param t the destination state
     */
    public void addEdge(State s, State t) {
        s.addEdge(t, 0);
    }

    /**
     * The <code>getTotalStateCount()</code> method returns the internally recorded
     * number of states created in this state space. This is mainly used for reporting
     * purposes.
     * @return the total number of states in the state cache
     */
    public long getTotalStateCount() {
        return totalStateCount;
    }

    /**
     * The <code>getStatesInSpaceCount()</code> method returns the number of unique states
     * that have been added to this state space. This is mainly used for reporting purposes.
     * @return the number of states added to the state space
     */
    public long getStatesInSpaceCount() {
        return statesInSpace;
    }

    /**
     * The <code>getSpecialStateCount()</code> method returns the number of special states
     * that were created. This is mainly used for reporting purposes.
     * @return the number of special states created
     */
    public long getSpecialStateCount() {
        return specialStates;
    }
}
