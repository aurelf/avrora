package avrora.stack;

import avrora.Avrora;
import avrora.core.Program;

import java.util.HashSet;
import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class StateTransitionGraph {

    private static class Link {
        public final StateCache.State state;
        public final Link next;

        Link(StateCache.State tar, Link n) {
            state = tar;
            next = n;
        }
    }

    /**
     * The <code>Edge</code> inner class represents a bidirectional edge between two
     * states. It is contained on two linked lists: the forward edge list of the source
     * node and the backward edge list of the target node.
     */
    public static class Edge {
        public final StateCache.State source;
        public final StateCache.State target;
        public final Edge forwardLink;
        public final Edge backwardLink;
        public final int type;
        public final int weight;

        Edge(StateCache.State source, StateCache.State target, Edge fl, Edge bl, int type, int weight) {
            this.source = source;
            this.target = target;
            this.forwardLink = fl;
            this.backwardLink = bl;
            this.type = type;
            this.weight = weight;
        }
    }

    public static class StateInfo {
        public final StateCache.State state;
        public HashSet stateSet;
        public Edge forwardEdges;
        public Edge backwardEdges;

        StateInfo(StateCache.State s) {
            state = s;
        }

        public void addEdge(int type, int weight, StateCache.State target) {
            Edge nedge = new Edge(state, target, forwardEdges, target.info.backwardEdges, type, weight);

            this.forwardEdges = nedge;
            target.info.backwardEdges = nedge;
        }
    }

    /**
     * The <code>frontierList</code> field stores a simple linked list of the current
     * states on the frontier.
     */
    private Link frontierList;

    private StateCache cache;


    public StateTransitionGraph(Program p) {
        cache = new StateCache(p);
        addFrontierState(cache.getEdenState());
    }


    /**
     * The <code>getCachedState()</code> method looks for the a cached, immutable
     * state that corresponds to the given mutable state. If there is no cached state
     * yet, it will create and return a new one.
     * @param s the mutable state to look for
     * @return an instance of the <code>StateCache.State</code> class
     */
    public StateCache.State getCachedState(MutableState s) {
        StateCache.State ns = cache.getStateFor(s);
        if ( ns.info == null ) ns.info = new StateInfo(ns);
        return ns;
    }


    public void addEdge(StateCache.State s, int type, int weight, StateCache.State t) {
        if ( s.info == null )
            throw Avrora.failure("No edge info for: "+s.getUniqueName());
        if ( t.info == null )
            throw Avrora.failure("No edge info for: "+t.getUniqueName());
        s.info.addEdge(type, weight, t);
    }

    /**
     * The <code>getNextFrontierState()</code> chooses a state off of the state frontier,
     * removes it from the state frontier, and returns it. If there are no states
     * left on the state frontier, this method returns null. Note that initially only
     * the eden state is on the frontier.
     *
     * @return one of the states on the current state frontier; null if there are none.
     */
    public StateCache.State getNextFrontierState() {
        if ( frontierList == null ) return null;
        Link l = frontierList;
        frontierList = frontierList.next;

        StateCache.State state = l.state;
        if ( state.info == null )
            throw Avrora.failure("State on frontier has no edge info: "+state.getUniqueName());

        return state;
    }

    /**
     * The <code>addFrontierState</code> method adds a state to the frontier.
     * @param s the state to add
     */
    public void addFrontierState(StateCache.State s) {
        if ( isExplored(s) )
            throw Avrora.failure("Attempt to re-add state to frontier: "+s.getUniqueName());

        if ( !isFrontier(s) ) {
            frontierList = new Link(s, frontierList);
            s.onFrontier = true;
        }
    }

    public boolean isExplored(StateCache.State s) {
        return s.isExplored;
    }

    public boolean isFrontier(StateCache.State s) {
        return s.onFrontier;
    }

}
