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
import avrora.Defaults;
import avrora.core.LoadableProgram;
import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.Simulation;
import avrora.sim.platform.PinConnect;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.mcu.Microcontroller;
import avrora.sim.radio.Radio;
import avrora.sim.radio.RadioAir;
import avrora.sim.radio.SimpleAir;
import avrora.sim.radio.freespace.FreeSpaceAir;
import avrora.sim.radio.freespace.Topology;
import avrora.util.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.io.IOException;
import java.io.File;

/**
 * The <code>MultiSimulateAction</code> class has been deprecated. It was originally used to
 * start a multiple-node simulation, but that functionality has been subsumed by the <code>SimulateAction</code>
 * and the <code>Simulation</code> extension point.
 *
 * @author Simon Han
 * @author Daniel Lee
 * @author Ben L. Titzer
 * @see avrora.actions.SimulateAction
 * @see avrora.sim.Simulation
 */
public class MultiSimulateAction extends SimAction {

    public MultiSimulateAction() {
        super("Deprecated. See simulate.");
    }

    /**
     * The <code>run()</code> method starts the multiple node simulation. It accepts command line
     * arguments that are a list of programs. The programs are instantiating on nodes according to
     * the -nodecount option, which specifies a list of the number of nodes to instantiate with
     * each program. Optionally, a topology file can be specified.
     * @param args the arguments to the action, including a list of programs
     * @throws Exception
     */
    public void run(String[] args) throws Exception {

        String notice = "The \"multi-simulate\" action has been deprecated. Please use the " +
                "\"simulate\" action and specify the \"-simulation=sensor-network\" option to initiate " +
                "a simulation of a multi-node sensor network. For more information, please use the built-in " +
                "command line help system.";
        Terminal.println(StringUtil.makeParagraphs(notice, 0, 0, 78));
    }

}
