package avrora.stack;

import avrora.Avrora;
import avrora.core.Program;

import java.util.HashSet;

/**
 * @author Ben L. Titzer
 */
public class StateTransitionGraph {

    public static class StateList {
        public final StateCache.State state;
        public final StateList next;

        StateList(StateCache.State tar, StateList n) {
            state = tar;
            next = n;
        }
    }

    public static class EdgeList {
        public final Edge edge;
        public final EdgeList next;

        EdgeList(Edge tar, EdgeList n) {
            edge = tar;
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

    /**
     * The <code>StateInfo</code> class is a representation of both the forward and backward
     * edge list corresponding to a node in the state transition graph. It also
     * stores a cache of reachable return states (based on backwards reachability
     * search).
     */
    public static class StateInfo {
        public final StateCache.State state;
        public HashSet stateSet;
        public Edge forwardEdges;
        public Edge backwardEdges;

        StateInfo(StateCache.State s) {
            state = s;
        }

        public Edge addEdge(int type, int weight, StateCache.State target) {
            Edge nedge = new Edge(state, target, forwardEdges, target.info.backwardEdges, type, weight);

            this.forwardEdges = nedge;
            target.info.backwardEdges = nedge;
            return nedge;
        }
    }

    /**
     * The <code>frontierList</code> field stores a simple linked list of the current
     * states on the frontier.
     */
    private StateList frontierList;

    /**
     * The <code>cache</code> field stores a cache of all states; it guarantees that
     * object equality for states implies reference equality and vice versa.
     */
    private StateCache cache;

    private long edgeCount;
    private long frontierCount;
    private long exploredCount;

    private final StateCache.State edenState;

    /**
     * The constructor for the <code>StateTransitionGraph</code> class constructs
     * a new state transition graph, with a state cache. The program passed is used
     * as an approximation of the possible size of the state space.
     * @param p the program to create a state transition graph for.
     */
    public StateTransitionGraph(Program p) {
        cache = new StateCache(p);
        edenState = cache.getEdenState();
        edenState.info = new StateInfo(edenState);
        addFrontierState(edenState);
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

    /**
     * The <code>addEdge()</code> method adds an edge between two states in the
     * state transition graph. The edge has a type and a weight.
     * @param s the source node of the edge
     * @param type the type of the edge as an integer
     * @param weight the weight of the edge as an integer
     * @param t the target node of the edge
     */
    public Edge addEdge(StateCache.State s, int type, int weight, StateCache.State t) {
        if ( s.info == null )
            throw Avrora.failure("No edge info for: "+s.getUniqueName());
        if ( t.info == null )
            throw Avrora.failure("No edge info for: "+t.getUniqueName());
        edgeCount++;
        return s.info.addEdge(type, weight, t);
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
        StateList l = frontierList;
        frontierList = frontierList.next;

        StateCache.State state = l.state;
        if ( state.info == null )
            throw Avrora.failure("State on frontier has no edge info: "+state.getUniqueName());

        state.onFrontier = false;
        frontierCount--;
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
            frontierList = new StateList(s, frontierList);
            s.onFrontier = true;
            frontierCount++;
        }
    }

    /**
     * The <code>isExplored()</code> method tests whether a given state has been
     * explored before.
     * @param s the cached state to test whether it is explored
     * @return true if this state has been explored and had its outgoing edges
     * computed; false otherwise
     */
    public boolean isExplored(StateCache.State s) {
        return s.isExplored;
    }

    /**
     * The <code>setExplored()</code> method marks the given state as having been
     * explored. A state cannot both be explored and be on the frontier; thus this
     * method will throw a fatal error if the given state is marked as on
     * the frontier.
     * @param s the state to mark as explored
     */
    public void setExplored(StateCache.State s) {
        if ( isFrontier(s) )
            throw Avrora.failure("state cannot be on frontier and explored: "+s.getUniqueName());

        if ( !isExplored(s) ) {
            s.isExplored = true;
            exploredCount++;
        }
    }

    /**
     * The <code>isFrontier()</code> method tests whether a given state is currently
     * in the frontier list of the state transition graph.
     * @param s the state to test whether it is on the frontier
     * @return true if the state is currently on the frontier of the state transition
     * graph; false otherwise
     */
    public boolean isFrontier(StateCache.State s) {
        return s.onFrontier;
    }

    /**
     * The <code>getStateCache()</code> method gets the cache of all the states in
     * the state space.
     * @return a reference to the cache of all the states in the state transition
     * graph
     */
    public StateCache getStateCache() {
        return cache;
    }

    public StateCache.State getEdenState() {
        return edenState;
    }

    public long getFrontierCount() {
        return frontierCount;
    }

    public long getEdgeCount() {
        return edgeCount;
    }

    public long getExploredCount() {
        return exploredCount;
    }
}
