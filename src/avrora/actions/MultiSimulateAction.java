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
import avrora.util.StringUtil;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;

/**
 * TODO: Doc this
 * @author Daniel Lee, Simon Han
 */
public class MultiSimulateAction extends SimAction {
    public static final String HELP = "The \"multi-simulate\" action launches a set of simulators with " +
            "the specified program loaded onto each.";
    public final Option.Long NODECOUNT = newOption("nodecount", 1,
            "This option is used in the multi-node simulation. It specifies the " +
            "number of nodes to be instantiated.");
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

    public MultiSimulateAction() {
        super("multi-simulate", HELP);
    }

    Program program;

    double startms;
    double endms;


    public void run(String[] args) throws Exception {

        Simulator.LEGACY_INTERPRETER = LEGACY_INTERPRETER.get();

        Simulator simulator;

        Microcontroller microcontroller;
        LinkedList simulatorThreadList = new LinkedList();

        //program = Main.readProgram(args);

        //Simulator.LEGACY_INTERPRETER = LEGACY_INTERPRETER.get();

        PlatformFactory pf = getPlatform();
        SimulatorThread st;

        HashMap programs = new HashMap();

        for (int i = 0; i < NODECOUNT.get(); i++) {

            // TODO: Use a hashtable to avoid creating duplicate programs of the same input
            // files.
            Program program = null;
            String[] argS = new String[1];

            if (i < args.length) {
                argS[0] = args[i];
            }

            String name;

            if(args.length < NODECOUNT.get() && i >= args.length) {
                name = args[args.length - 1];
            } else {
                name = args[i];
            }

            program = (Program)programs.get(name);
            if(program == null) {
                program = Main.readProgram(argS);
                programs.put(name, program);
            }

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
            radio.setSimulatorThread(st);

            if (radio != null) {
                SimpleAir.simpleAir.addRadio(microcontroller.getRadio());
            }

            // TODO: port over other "process" routines from SimulateAction
            processTimeout(simulator);
        }

        SimulatorThread first = null;
        startms = System.currentTimeMillis();
        try {
            //simulator.start();
            Iterator threadIterator = simulatorThreadList.iterator();

            while (threadIterator.hasNext()) {
                SimulatorThread thread = (SimulatorThread) threadIterator.next();
                thread.start();
                if(first == null) {
                    first = thread;
                }
            }

        } finally {
            //Thread a = Thread.currentThread();
            first.join();
            endms = System.currentTimeMillis();
        }

        System.err.println("Time for simulator: " + StringUtil.milliAsString((long)(endms - startms)));
    }

    void processTimeout(Simulator simulator) {
        long timeout = TIMEOUT.get();
        if (timeout > 0)
            simulator.insertTimeout(timeout);
    }
}
