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
            public final ImmutableState state;
            public final Link next;

            Link(ImmutableState t, Link n) {
                state = t;
                next = n;
            }
        }

        public final ImmutableState state;
        public Link outgoing;

        ConnectedState(ImmutableState s) {
            state = s;
        }

        void addEdge(ImmutableState t) {
            outgoing = new Link(state, outgoing);
        }
    }

    private HashMap states;

    public StateSpace() {
        states = new HashMap();
    }

    public boolean contains(ImmutableState s) {
        return states.get(s) != null;
    }

    public boolean addState(ImmutableState s) {
        if ( states.get(s) != null ) return false;
        states.put(s, new ConnectedState(s));
        return true;
    }

    public ConnectedState getConnections(ImmutableState s) {
        return (ConnectedState)states.get(s);
    }

    public ImmutableState getImmutableStateFor(MutableState s) {
        ImmutableState is = new ImmutableState(s);
        ConnectedState cs = getConnections(is);
        if ( cs == null ) {
            cs = new ConnectedState(is);
            states.put(is, cs);
            return is;
        } else {
            return cs.state;
        }
    }

    public void addEdge(ImmutableState s, ImmutableState t) {
        ConnectedState cs = (ConnectedState)states.get(s);
        if ( cs == null )
            throw VPCBase.failure("tried to add outgoing from state not in state space");
        cs.addEdge(t);
    }
}
