/**
 * Copyright (c) 2004, Regents of the University of California
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

import avrora.Avrora;
import avrora.Main;
import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.radio.SimpleAir;
import avrora.sim.radio.Radio;
import avrora.sim.mcu.Microcontroller;
import avrora.sim.platform.PlatformFactory;
import avrora.util.Option;
import avrora.util.Terminal;
import avrora.util.StringUtil;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Random;

/**
 * TODO: Doc this
 * @author Daniel Lee, Simon Han
 */
public class MultiSimulateAction extends SimAction {
    public static final String HELP = "The \"multi-simulate\" action launches a set of simulators with " +
            "the specified program loaded onto each. This is useful for simulating a network of " +
            "sensor nodes and monitoring the behavior of the entire network. ";
    public final Option.Long NODECOUNT = newOption("nodecount", 1,
            "This option is used in the multi-node simulation. It specifies the " +
            "number of nodes to be instantiated.");
    public final Option.Long RANDOMSEED = newOption("random-seed", 0,
            "This option is used to seed a pseudo-random number generator used in the " +
            "simulation. If this option is set to non-zero, then its value is used as " +
            "the seed for reproducible simulation results. If this option is not set, " +
            "those parts of simulation that rely on random numbers will have seeds " +
            "chosen based on system parameters that vary from run to run.");
    public final Option.Str TOPOLOGY = newOption("topology", "",
            "This option is used in the multi-node simulation to specify the name of " +
            "a file that contains information about the topology of the network.");
    public final Option.Bool LEGACY_INTERPRETER = newOption("legacy-interpreter", false,
            "This option is used in the \"simulate\" action. It causes the simulator " +
            "to use the legacy (hand-written) interpreter rather than the interpreter " +
            "generated from the architecture description language. It is used for " +
            "benchmarking and regression purposes.");
    public final Option.Bool TIME = newOption("time", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the time used in executing the simulation. When combined with " +
            "the \"cycles\" and \"total\" options, it will report performance " +
            "information about the simulation.");
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
            "synchronization between nodes. The starting times are staggered by the number " +
            "of clock cycles given as a value. For example, if this option is given the" +
            "value X, then node 0 will start at time 0, node 1 at time 1*X, node 2 at " +
            "time 2*X, etc.");

    public MultiSimulateAction() {
        super("multi-simulate", HELP);
    }

    Program program;

    double startms;
    double endms;


    public void run(String[] args) throws Exception {

        Simulator.LEGACY_INTERPRETER = LEGACY_INTERPRETER.get();

        LinkedList simulatorThreadList = new LinkedList();
        PlatformFactory pf = getPlatform();
        Program program = Main.readProgram(args);

        for (int i = 0; i < NODECOUNT.get(); i++) {

            Simulator simulator;
            Microcontroller microcontroller;
            SimulatorThread st;

            if (pf != null) {
                microcontroller = pf.newPlatform(program).getMicrocontroller();
                simulator = microcontroller.getSimulator();
                st = new SimulatorThread(simulator);
                simulatorThreadList.addFirst(st);

            } else {
                microcontroller = getMicrocontroller().newMicrocontroller(program);
                simulator = microcontroller.getSimulator();
                st = new SimulatorThread(simulator);
                simulatorThreadList.addFirst(st);
            }

            Radio radio = microcontroller.getRadio();

            if (radio != null) {
                radio.setSimulatorThread(st);
                SimpleAir.simpleAir.addRadio(microcontroller.getRadio());
            }

            processTimeout(simulator);
            processRandom(simulator);
            processStagger(simulator);
        }

        startms = System.currentTimeMillis();
        try {
            startSimulationThreads(simulatorThreadList);

        } finally {
            joinSimulationThreads(simulatorThreadList);

            // compute simulation time
            endms = System.currentTimeMillis();
            Terminal.printBrightGreen("Time for simulation: ");
            Terminal.println(StringUtil.milliAsString((long)(endms - startms)));
        }
    }

    private void joinSimulationThreads(LinkedList simulatorThreadList) throws InterruptedException {
        // wait for all threads
        Iterator threadIterator = simulatorThreadList.iterator();

        while (threadIterator.hasNext()) {
            SimulatorThread thread = (SimulatorThread) threadIterator.next();
            thread.join();
        }
    }

    private void startSimulationThreads(LinkedList simulatorThreadList) {
        // start up all threads
        Iterator threadIterator = simulatorThreadList.iterator();

        while (threadIterator.hasNext()) {
            SimulatorThread thread = (SimulatorThread) threadIterator.next();
            thread.start();
        }
    }

    void processTimeout(Simulator simulator) {
        long timeout = TIMEOUT.get();
        if (timeout > 0)
            simulator.insertTimeout(timeout);
    }

    Random random;

    void processRandom(Simulator simulator) {
        long size = RANDOM_START.getHigh() - RANDOM_START.getLow();
        long delay = 0;
        if ( size > 0 ) {
            if ( random == null ) {
                long seed;
                if ( (seed = RANDOMSEED.get()) != 0 ) random = new Random(seed);
                else random = new Random();
            }

            delay = random.nextLong();
            if ( delay < 0 ) delay = -delay;
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
