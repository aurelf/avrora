package avrora.stack;

import vpc.VPCBase;

import java.util.HashSet;
import java.util.HashMap;

/**
 * The <code>StateSpace</code> class represents the reachable state space
 * as it is explored by the <code>Analyzer</code> class. It stores reachable
 * states and the outgoing edges that connect them.
 *
 * @author Ben L. Titzer
 */
public class StateSpace {

    public static class ConnectedState {
        public static class Link {
            public final AbstractState state;
            public final Link next;

            Link(AbstractState t, Link n) {
                state = t;
                next = n;
            }
        }

        public final AbstractState state;
        public Link outgoing;

        ConnectedState(AbstractState s) {
            state = s;
        }

        void addEdge(AbstractState t) {
            outgoing = new Link(state, outgoing);
        }
    }

    private HashMap states;

    public StateSpace() {
        states = new HashMap();
    }

    public boolean contains(AbstractState s) {
        return states.get(s) != null;
    }

    public boolean addState(AbstractState s) {
        if ( states.get(s) != null ) return false;
        states.put(s.copy(), new ConnectedState(s));
        return true;
    }

    public ConnectedState getConnections(AbstractState s) {
        return (ConnectedState)states.get(s);
    }

    public void addEdge(AbstractState s, AbstractState t) {
        ConnectedState cs = (ConnectedState)states.get(s);
        if ( cs == null )
            throw VPCBase.failure("tried to add outgoing from state not in state space");
        cs.addEdge(t);
    }
}
