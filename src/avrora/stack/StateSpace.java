package avrora.stack;

import vpc.VPCBase;

import java.util.HashSet;

/**
 * The <code>StateSpace</code> class represents the reachable state space
 * as it is explored by the <code>Analyzer</code> class. It stores reachable
 * states and the edges that connect them.
 *
 * @author Ben L. Titzer
 */
public class StateSpace {

    private HashSet states;

    StateSpace() {
        states = new HashSet();
    }

    public boolean contains(AbstractState s) {
        return states.contains(s);
    }

    public void addEdge(AbstractState s, AbstractState t) {
        throw VPCBase.unimplemented();
    }
}
