/**
 * Copyright (c) 2004-2005, Regents of the University of California
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

package avrora.sim;

import avrora.util.Options;
import avrora.sim.clock.Clock;

import java.util.Iterator;

/**
 * The <code>Simulation</code> class represents a complete simulation, including
 * the nodes, the programs, the radio model (if any), the environment model, for
 * simulations of one or many nodes.
 *
 * @author Ben L. Titzer
 */
public abstract class Simulation {

    /**
     * The <code>Factory</code> interface represents a factory capable of creating
     * new simulations based on the command line options and arguments. 
     */
    public interface Factory {
        public Simulation newSimulation(Options options, String[] args);
    }

    /**
     * The <code>Node</code> class represents a node in the simulation.
     */
    public abstract static class Node {
        public final Simulator simulator;
        public final Clock clock;

        protected Node(Simulator s) {
            simulator = s;
            clock = s.getClock();
        }
    }

    /**
     * The <code>start()</code> method begins the simulation by starting all nodes.
     */
    public abstract void start();

    /**
     * The <code>getNodeCount()</code> method returns the number of nodes that are
     * in this simulation.
     * @return the number of nodes that are in the simulation
     */
    public abstract int getNodeCount();

    /**
     * The <code>getNodeIterator()</code> method returns an iterator over all the nodes
     * in the simulation. This can be used by monitors to instrument nodes, etc.
     * @return an instance of the <code>Iterator</code> interface that can iterate over
     * the nodes in this simulation
     */
    public abstract Iterator getNodeIterator();
}
