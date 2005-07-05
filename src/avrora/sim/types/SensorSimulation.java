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
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.clock.IntervalSynchronizer;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.radio.Radio;
import avrora.sim.radio.RadioAir;
import avrora.sim.radio.SimpleAir;
import avrora.sim.radio.freespace.Topology;
import avrora.sim.radio.freespace.FreeSpaceAir;
import avrora.core.LoadableProgram;
import avrora.util.Options;
import avrora.util.Option;
import avrora.util.StringUtil;
import avrora.Avrora;
import avrora.Main;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

/**
 * @author Ben L. Titzer
 */
public class SensorSimulation extends Simulation {

    public static String HELP = "The sensor network simulation is used for simulating multiple sensor nodes " +
            "simultaneously. These nodes can communicate with each other wirelessly to exchange packets that " +
            "include sensor data and routing information for a multi-hop network. Currently, only the \"mica2\" " +
            "platform sensor nodes are supported.";

    public final Option.List NODECOUNT = options.newOptionList("nodecount", "1",
            "This option is used to specify the number of nodes to be instantiated. " +
            "The format is a list of integers, where each integer specifies the number of " +
            "nodes to instantiate with each program supplied on the command line. For example, " +
            "when set to \"1,2\" one node will be created with the first program loaded onto it, " +
            "and two nodes created with the second program loaded onto them.");
    public final Option.Str TOPOLOGY = options.newOption("topology", "",
            "This option can be used to specify the name of " +
            "a file that contains information about the topology of the network. " +
            "When this option is specified the free space radio model will be used " +
            "to model radio propagation. See sample.top in topology for an example. \n(Status: alpha)");
    public final Option.Interval RANDOM_START = options.newOption("random-start", 0, 0,
            "This option causes the simulator to insert a random delay before starting " +
            "each node in order to prevent artificial cycle-level synchronization. The " +
            "starting delay is pseudo-randomly chosen with uniform distribution over the " +
            "specified interval, which is measured in clock cycles. If the \"random-seed\" " +
            "option is set to a non-zero value, then its value is used as the seed to the " +
            "pseudo-random number generator.");
    public final Option.Long STAGGER_START = options.newOption("stagger-start", 0,
            "This option causes the simulator to insert a progressively longer delay " +
            "before starting each node in order to avoid artificial cycle-level " +
            "synchronization between nodes. The starting times are staggered by the specified number " +
            "of clock cycles. For example, if this option is given the " +
            "value X, then node 0 will start at time 0, node 1 at time 1*X, node 2 at " +
            "time 2*X, etc.");

    protected class SensorNode extends Node {
        Radio radio;
        long startup;

        SensorNode(int id, PlatformFactory pf, LoadableProgram p) {
            super(id, pf, p);
        }

        protected void instantiate() {
            thread = new SimulatorThread(this);
            super.instantiate();
            radio = simulator.getMicrocontroller().getRadio();
            air.addRadio(radio);
            simulator.delay(startup);
        }

        protected void remove() {
            air.removeRadio(radio);
        }
    }

    RadioAir air;
    long stagger;

    public SensorSimulation() {
        super("sensor-network", HELP, null);

        addSection("SENSOR NETWORK SIMULATION OVERVIEW", help);
        addOptionSection("This simulation type supports simulating multiple sensor network nodes that communicate " +
                "with each other over radios. There are options to specify how many of each type of sensor node to " +
                "instantiate, as well as the program to be loaded onto each node, and an optional topology file " +
                "that describes the physical layout of the sensor network.", options);

        PLATFORM.setNewDefault("mica2");
    }

    public Node newNode(int id, PlatformFactory pf, LoadableProgram p) {
        return new SensorNode(id, pf, p);
    }

    public void process(Options o, String[] args) throws Exception {
        options.process(o);
        processMonitorList();

        if ( args.length == 0 )
            Avrora.userError("Simulation error", "No program specified");
        Main.checkFilesExist(args);
        PlatformFactory pf = getPlatform();

        // get the radio air model
        air = getRadioAir();
        synchronizer = air.getSynchronizer();

        // instantiate the nodes
        int cntr = 0;
        Iterator i = NODECOUNT.get().iterator();
        while (i.hasNext()) {

            if (args.length <= cntr) break;

            LoadableProgram lp = new LoadableProgram(new File(args[cntr++]));
            lp.load();

            // create a number of nodes with the same program
            int max = StringUtil.evaluateIntegerLiteral((String)i.next());
            for (int node = 0; node < max; node++) {
                SensorNode n = (SensorNode)createNode(pf, lp);
                long r = processRandom();
                long s = processStagger();
                n.startup = r + s;
            }
        }

    }

    private RadioAir getRadioAir() throws IOException {
        if ( "".equals(TOPOLOGY.get()) ) {
            return new SimpleAir();
        } else {
            return new FreeSpaceAir(new Topology(TOPOLOGY.get()));
        }
    }

    long processRandom() {
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

        return (RANDOM_START.getLow() + delay);
    }

    long processStagger() {
        long st = stagger;
        stagger += STAGGER_START.get();
        return st;
    }

}
