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

package avrora.actions;

import avrora.Main;
import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.mcu.Microcontroller;
import avrora.sim.radio.Radio;
import avrora.sim.radio.SimpleAir;
import avrora.sim.radio.freespace.FreeSpaceAir;
import avrora.topology.Topology;
import avrora.util.Option;
import avrora.util.StringUtil;
import avrora.util.Terminal;
import avrora.util.Visual;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * The <code>MultiSimulateAction</code> class represents an action available to the simulator where multiple
 * nodes are run in simulation.
 *
 * @author Simon Han
 * @author Daniel Lee
 * @author Ben L. Titzer
 */
public class MultiSimulateAction extends SimAction {
    public static final String HELP = "The \"multi-simulate\" action launches a set of simulators with " +
            "the specified program loaded onto each. This is useful for simulating a network of " +
            "sensor nodes and monitoring the behavior of the entire network. ";
    public final Option.List NODECOUNT = newOptionList("nodecount", "1",
            "This option is used to specify the number of nodes to be instantiated. " +
            "The format is a list of integers, where each integer specifies the number of " +
            "nodes to instantiate with each program supplied on the command line. For example, " +
            "when set to \"1,2\" one node will be created with the first program loaded onto it, " +
            "and two nodes created with the second program loaded onto them.");
    public final Option.Long RANDOMSEED = newOption("random-seed", 0,
            "This option is used to seed a pseudo-random number generator used in the " +
            "simulation. If this option is set to non-zero, then its value is used as " +
            "the seed for reproducible simulation results. If this option is not set, " +
            "those parts of simulation that rely on random numbers will have seeds " +
            "chosen based on system parameters that vary from run to run.");
    public final Option.Str TOPOLOGY = newOption("topology", "(null)",
            "This option can be used to specify the name of " +
            "a file that contains information about the topology of the network. " +
            "When this option is specified the free space radio model will be used " +
            "to model radio propagation. See sample.top in topology for an example. \n(Status: alpha)");
    public final Option.Interval RANDOM_START = newOption("random-start", 0, 0,
            "This option causes the simulator to insert a random delay before starting " +
            "each node in order to prevent artificial cycle-level synchronization. The " +
            "starting delay is pseudo-randomly chosen with uniform distribution over the " +
            "specified interval, which is measured in clock cycles. If the \"random-seed\" " +
            "option is set to a non-zero value, then its value is used as the seed to the " +
            "pseudo-random number generator.");
    public final Option.Long STAGGER_START = newOption("stagger-start", 0,
            "This option causes the simulator to insert a progressively longer delay " +
            "before starting each node in order to avoid artificial cycle-level " +
            "synchronization between nodes. The starting times are staggered by the specified number " +
            "of clock cycles. For example, if this option is given the " +
            "value X, then node 0 will start at time 0, node 1 at time 1*X, node 2 at " +
            "time 2*X, etc.");
    public final Option.Bool CHANNEL_UTIL = newOption("channel-utilization", false,
            "This option causes the simulator to record statistics about the amount of radio " +
            "traffic in the network and report it after the simulation is complete.");

    public MultiSimulateAction() {
        super("multi-simulate", HELP);
    }

    Program program;
    LinkedList simulatorThreadList = new LinkedList();

    /**
     * The <code>run()</code> method starts the multiple node simulation. It accepts command line
     * arguments that are a list of programs. The programs are instantiating on nodes according to
     * the -nodecount option, which specifies a list of the number of nodes to instantiate with
     * each program. Optionally, a topology file can be specified.
     * @param args the arguments to the action, including a list of programs
     * @throws Exception
     */
    public void run(String[] args) throws Exception {

        initializeSimulatorStatics();

        int cntr = 0;
        int nodes = 0;

        // create the specified number of each type of node
        Iterator i = NODECOUNT.get().iterator();
        String visual = VISUAL.get();
        if (!"".equals(visual)) {
            //visualisation is turned on
            Visual.connect(visual);
        }
        Topology top = null;
        boolean topologyOn = false;
        if (!TOPOLOGY.get().equals("(null)")) {
            topologyOn = true;
            top = new Topology(TOPOLOGY.get());
        }
        while (i.hasNext()) {

            if (args.length <= cntr) break;

            String singleArg[] = {args[cntr++]};
            Program program = Main.getProgramReader().read(singleArg);

            // create a number of nodes with the same program
            int max = StringUtil.evaluateIntegerLiteral((String)i.next());
            for (int node = 0; node < max; node++) {
                Simulator simulator = newSimulator(program);
                Microcontroller microcontroller = simulator.getMicrocontroller();
                SimulatorThread st = new SimulatorThread(simulator);

                simulatorThreadList.addLast(st);
                Radio radio = microcontroller.getRadio();

                //OL: check for radio
                if (radio != null) {
                    radio.setSimulatorThread(st);
                    //no topology defined, use simple air
                    if (!topologyOn) {
                        SimpleAir.simpleAir.addRadio(microcontroller.getRadio());
                    } else {
                        //wonderful, there is a toplogy definded, use the free space air model
                        //activate free space and local air at this radio
                        radio.activateLocalAir(top.getPosition(nodes));
                        FreeSpaceAir.freeSpaceAir.addRadio(microcontroller.getRadio());
                    }
                }
                nodes++;

                processRandom(simulator);
                processStagger(simulator);
            }
        }

        // enable channel utilization accounting
        SimpleAir.simpleAir.recordUtilization(CHANNEL_UTIL.get());

        long startms = System.currentTimeMillis();
        try {
            printSimHeader();
            startSimulationThreads(simulatorThreadList);

        } finally {
            joinSimulationThreads(simulatorThreadList);
            printSeparator();

            // compute simulation time
            long endms = System.currentTimeMillis();
            reportTime(startms, endms);
            reportUtilization();
            reportAllMonitors();
        }
    }

    private void reportAllMonitors() {
        int cntr = 0;
        Iterator i = simulatorThreadList.iterator();
        while (i.hasNext()) {
            Simulator s = ((SimulatorThread)i.next()).getSimulator();
            if (hasMonitors(s)) {
                Terminal.printSeparator(78, "Monitors for node "+cntr);
            }
            reportMonitors(s);
            cntr++;
        }
    }

    private void reportTime(long startms, long endms) {
        reportQuantity("Time for simulation", StringUtil.milliToSecs(endms - startms), "seconds");
    }

    private void reportUtilization() {
        if (CHANNEL_UTIL.get()) {
            reportQuantity("First transmit time", SimpleAir.simpleAir.firstPacketTime, "cycles");
            reportQuantity("Bytes attempted", SimpleAir.simpleAir.bytesAttempted, "");
            reportQuantity("Bytes delivered", SimpleAir.simpleAir.bytesDelivered, "");
            reportQuantity("Bytes corrupted", SimpleAir.simpleAir.bytesCorrupted, "");
            reportQuantity("Bits discarded", SimpleAir.simpleAir.bitsDiscarded, "");
        }
    }

    private void joinSimulationThreads(LinkedList simulatorThreadList) throws InterruptedException {
        // wait for all threads
        Iterator threadIterator = simulatorThreadList.iterator();

        while (threadIterator.hasNext()) {
            SimulatorThread thread = (SimulatorThread)threadIterator.next();
            thread.join();
        }
    }

    private void startSimulationThreads(LinkedList simulatorThreadList) {
        // start up all threads
        Iterator threadIterator = simulatorThreadList.iterator();

        while (threadIterator.hasNext()) {
            SimulatorThread thread = (SimulatorThread)threadIterator.next();
            thread.start();
        }
    }

    Random random;

    void processRandom(Simulator simulator) {
        long size = RANDOM_START.getHigh() - RANDOM_START.getLow();
        long delay = 0;
        if (size > 0) {
            if (random == null) {
                long seed;
                if ((seed = RANDOMSEED.get()) != 0)
                    random = new Random(seed);
                else
                    random = new Random();
            }

            delay = random.nextLong();
            if (delay < 0) delay = -delay;
            delay = delay % size;
        }

        simulator.delay(RANDOM_START.getLow() + delay);
    }

    long stagger;

    void processStagger(Simulator simulator) {
        simulator.delay(stagger);
        stagger += STAGGER_START.get();
    }
}
