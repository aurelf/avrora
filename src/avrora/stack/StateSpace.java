package avrora.stack;

import avrora.util.StringUtil;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import avrora.sim.IORegisterConstants;
import avrora.core.Program;

/**
 * The <code>StateSpace</code> class represents the reachable state space
 * as it is explored by the <code>Analyzer</code> class. It stores reachable
 * states and the outgoing edges that connect them.
 *
 * @author Ben L. Titzer
 */
public class StateSpace {

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
            for ( int cntr = 0; cntr < NUM_REGS; cntr++ ) {
                av_REGISTERS[cntr] = s.av_REGISTERS[cntr];
            }
            hashCode = computeHashCode();
            UID = uidCount++;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object o) {
            if ( this == o ) return true;
            if ( !(o instanceof State) ) return false;
            State i = (State)o;
            if ( this.pc != i.pc ) return false;
            if ( this.av_SREG != i.av_SREG ) return false;
            for ( int cntr = 0; cntr < NUM_REGS; cntr++ )
                if ( this.av_REGISTERS[cntr] != i.av_REGISTERS[cntr] ) return false;
            return true;
        }

        public Object mark;
        public Link outgoing;

        void addEdge(State t, int weight) {
            outgoing = new Link(t, outgoing, weight);
        }

        public String getUniqueName() {
            return StringUtil.toHex(UID, 10);
        }

    }

    class SpecialState extends State {
        public final String stateName;

        SpecialState(String name) {
            stateName = name;
        }

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

    public StateSpace(Program p) {
        stateMap = new HashMap();
        edenState = getStateFor(new MutableState());
        program = p;
    }

    public State getEdenState() {
        return edenState;
    }

    public State getFrontierState() {
        Link head = frontier;
        if ( head == null ) return null;

        frontier = frontier.next;
        head.state.onFrontier = false;
        return head.state;
    }

    public boolean isExplored(State s) {
        return s.inSpace;
    }

    public boolean isFrontier(State s) {
        return s.onFrontier;
    }

    public boolean addState(State s) {
        boolean wasBefore = s.inSpace;
        if ( !wasBefore ) statesInSpace++;
        s.inSpace = true;
        s.onFrontier = false;
        return wasBefore;
    }

    public boolean addFrontier(State s) {
        if ( s.onFrontier ) return true;
        frontier = new Link(s, frontier, 0);
        s.onFrontier = true;
        return false;
    }

    public SpecialState makeSpecialState(String name) {
        specialStates++;
        return new SpecialState(name);
    }

    public State getStateFor(MutableState s) {
        State is = new State(s);
        State cs = (State)stateMap.get(is);

        if ( cs != null ) {
            // if the state is already in the state map, return original
            return cs;
        } else {
            totalStateCount++;
            // the state is new, put it in the map and return it.
            stateMap.put(is, is);
            return is;
        }
    }

    public void addEdge(State s, State t, int weight) {
        s.addEdge(t, weight);
    }

    public void addEdge(State s, State t) {
        s.addEdge(t, 0);
    }

    public long getTotalStateCount() {
        return totalStateCount;
    }

    public long getStatesInSpaceCount() {
        return statesInSpace;
    }

    public long getSpecialStateCount() {
        return specialStates;
    }
}
