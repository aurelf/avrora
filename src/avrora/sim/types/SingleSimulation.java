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

package avrora.sim.types;

import avrora.sim.Simulation;
import avrora.sim.SimulatorThread;
import avrora.sim.util.InterruptScheduler;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.clock.Synchronizer;
import avrora.Avrora;
import avrora.Main;
import avrora.Defaults;
import avrora.core.Program;
import avrora.core.LoadableProgram;
import avrora.util.Options;
import avrora.util.Option;

import java.io.File;

/**
 * The <code>SingleSimulation</code> class implements a simulation for a single node. This class
 * has its own built-in synchronizer that is designed to accept only one node. It processes command
 * line options to configure monitors and load one program onto one microcontroller and simulate it.
 *
 * @author Ben L. Titzer
 */
public class SingleSimulation extends Simulation {

    protected static final String HELP = "The \"single\" simulation type corresponds to a standard simulation " +
            "of a single microcontroller with a single program.";

    protected static class SingleSynchronizer extends Synchronizer {

        protected Simulation.Node node;
        protected SimulatorThread thread;

        /**
         * The <code>addNode()</code> method adds a node to this synchronization group.
         * This method should only be called before the <code>start()</code> method is
         * called.
         * @param n the simulator representing the node to add to this group
         */
        public void addNode(Simulation.Node n) {
            if ( node != null )
                throw Avrora.failure("Only one node supported at a time");
            node = n;
        }

        /**
         * The <code>removeNode()</code> method removes a node from this synchronization
         * group, and wakes any nodes that might be waiting on it.
         * @param n the simulator thread to remove from this synchronization group
         */
        public void removeNode(Simulation.Node n) {
            if ( node == n ) node = null;
        }

        /**
         * The <code>waitForNeighbors()</code> method is called from within the execution
         * of a node when that node needs to wait for its neighbors to catch up to it
         * in execution time. The node will be blocked until the other nodes in other
         * threads catch up in global time.
         */
        public void waitForNeighbors(long time) {
            return;
        }

        /**
         * The <code>start()</code> method starts the threads executing, and the synchronizer
         * will add whatever synchronization to their execution that is necessary to preserve
         * the global timing properties of simulation.
         */
        public void start() {
            if ( node == null )
                throw Avrora.failure("No nodes in simulation");
            thread = new SimulatorThread(node);
            thread.start();
        }

        /**
         * The <code>join()</code> method will block the caller until all of the threads in
         * this synchronization interval have terminated, either through <code>stop()</code>
         * being called, or terminating normally such as through a timeout.
         */
        public void join() throws InterruptedException {
            thread.join();
        }

        /**
         * The <code>pause()</code> method temporarily pauses the simulation. The nodes are
         * not guaranteed to stop at the same global time. This method will return when all
         * threads in the simulation have been paused and will no longer make progress until
         * the <code>start()</code> method is called again.
         */
        public void pause() {
            throw Avrora.unimplemented();
        }

        /**
         * The <code>stop()</code> method will terminate all the simulation threads. It is
         * not guaranteed to stop all the simulation threads at the same global time.
         */
        public void stop() {
            throw Avrora.unimplemented();
        }


        /**
         * The <code>synch()</code> method will pause all of the nodes at the same global time.
         * This method can only be called when the simulation is paused. It will run all threads
         * forward until the global time specified and pause them.
         * @param globalTime the global time in clock cycles to run all threads ahead to
         */
        public void synch(long globalTime) {
            throw Avrora.unimplemented();
        }
    }

    public SingleSimulation() {
        super("single", HELP, new SingleSynchronizer());

        addSection("SINGLE NODE SIMULATION OVERVIEW", help);
        addOptionSection("The most basic type of simulation, the single node simulation, is designed to " +
                "simulate a single microcontroller running a single program. Help for specific options " +
                "relating to simulating a single node is below.", options);
    }

    /**
     * The <code>process()</code> method processes options and arguments from the command line. This
     * implementation accepts only a single command line argument that specifies the name of the program
     * to load onto the microcontroller to simulate.
     * @param o the options extracted from the command line
     * @param args the arguments from the command line
     * @throws Exception if there was a problem loading the file or creating the simulation
     */
    public void process(Options o, String[] args) throws Exception {
        options.process(o);
        processMonitorList();

        if ( args.length == 0 )
            Avrora.userError("Simulation error", "No program specified");
        if ( args.length > 1 )
            Avrora.userError("Simulation error", "Single node simulation accepts only one program");
        Main.checkFilesExist(args);

        LoadableProgram p = new LoadableProgram(args[0]);
        p.load();
        PlatformFactory pf = getPlatform();
        createNode(pf, p);

    }
}
