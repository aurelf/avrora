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

package avrora.gui;

import avrora.sim.Simulator;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.platform.Platform;
import avrora.core.Program;
import avrora.Avrora;
import avrora.Defaults;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

/**
 * The <code>VisualSimulation</code> class represents a simulation in the GUI. It contains nodes and
 * is able to be started, paused, and stopped. Each node can have monitors attached to it.
 * The actual <code>Simulator</code> objects are not allocated
 * until the simulation is started by calling the <code>start()</code> method.
 *
 * @author Ben L. Titzer
 * @author Adam Harmetz
 */
public class VisualSimulation {

    /**
     * The <code>MonitorFactory</code> interface represents a factory capable of attaching
     * monitors to sets of nodes. At the point the simulation is started, the monitor factory is
     * called for each node on which this monitor factory is installed, so that the monitor factory
     * has the opportunity to create per-node data structures and insert instrumentation on
     * the <code>Simulator</code> object.
     */
    public interface MonitorFactory {
        public void attach(List nodes);
        public void instantiate(Node n, Simulator s);
        public void remove(Node n);
    }

    /**
     * The <code>Node</code> class represents a node in a simulation, which has an ID and a program
     * to be loaded onto it. It also has a <code>PlatformFactory</code> instance that is used to create
     * the actual <code>Simulator</code> object when the simulation is begun.
     */
    public class Node {
        public final int id;
        protected final LoadableProgram path;

        protected final PlatformFactory platform;
        protected final LinkedList monitors;

        protected Simulator simulator;

        Node(int id, PlatformFactory pf, LoadableProgram p) {
            this.id = id;
            this.platform = pf;
            this.path = p;
            this.monitors = new LinkedList();
        }

        Simulator instantiate() {
            // create the simulator object
            Platform p = platform.newPlatform(id, Defaults.getInterpreterFactory(), path.getProgram());
            simulator = p.getMicrocontroller().getSimulator();
            // for each of the attached monitors, allow them to create their data structures
            Iterator i = monitors.iterator();
            while ( i.hasNext() ) {
                MonitorFactory f = (MonitorFactory)i.next();
                f.instantiate(this, simulator);
            }
            return simulator;
        }

        public Simulator getSimulator() {
            return simulator;
        }

        public void addMonitor(MonitorFactory f) {
            monitors.add(f);
        }

        public void removeMonitor(MonitorFactory f) {
            monitors.remove(f);
            f.remove(this);
        }

        public List getMonitors() {
            return monitors;
        }

        public LoadableProgram getProgram() {
            return path;
        }

    }

    protected int num_nodes;
    protected Node[] nodes;

    public VisualSimulation() {
        nodes = new Node[16];
    }

    /**
     * The <code>createNode()</code> method creates a new node in the simulation with the specified
     * platform, with the specified program loaded onto it.
     * @param pf the platform factory used to create the platform for the node
     * @param pp the program for the node
     * @return a new instance of the <code>Node</code> class representing the node
     */
    public Node createNode(PlatformFactory pf, LoadableProgram pp) {
        int id = num_nodes++;
        Node n = new Node(id, pf, pp);
        if ( id > nodes.length ) grow();
        nodes[id] = n;
        return n;
    }

    private void grow() {
        Node[] nnodes = new Node[nodes.length*2];
        System.arraycopy(nodes, 0, nnodes, 0, nodes.length);
        nodes = nnodes;
    }

    public Node getNode(int node_id) {
        return nodes[node_id];
    }

    public void removeNode(int node_id) {
        throw Avrora.unimplemented();
    }

    public void start() {
        throw Avrora.unimplemented();
    }

    public void pause() {
        throw Avrora.unimplemented();
    }

    public void resume() {
        throw Avrora.unimplemented();
    }

    public void stop() {
        throw Avrora.unimplemented();
    }

    public boolean isRunning() {
        throw Avrora.unimplemented();
    }
}
