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
import avrora.Avrora;
import avrora.Defaults;
import avrora.core.Program;
import avrora.gui.*;
import avrora.monitors.*;
import avrora.sim.GenInterpreter;
import avrora.sim.InterpreterFactory;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.platform.PlatformFactory;
import avrora.util.*;

import javax.swing.*;
import java.util.*;

//The VisualAction provides a link between the GUI and the simulator.
//It physically starts the GUI and also dispatchs simulator threads

public class VisualAction extends Action {


    public AvroraGui app; //allows us to access GUI

    public static final String HELP = "The \"visual\" action launches a GUI from which you can start simulations.";

    public VisualAction() {
        super(HELP);
    }

    /**
     * The <code>run()</code> method is called by the main class.
     * This just starts the GUI
     * If a file was specified by the command line, it'll be passed
     * to args
     */
    public void run(String[] args) throws Exception {

        //Let's turn off colors
        Terminal.useColors = false;

        //Provide nothing to the array if it's empty
        if (args.length < 1) {
            args = new String[1];
            args[0] = "";
        }

        //For now, we don't run it in a seperate thread.
        //We'll wait until we have problems until we try the whole seperate thread thing
        AvroraGui.init(options, args);
        AvroraGui.instance.showGui();
    }


    public void stopSim() {
        app.topMenu.updateMenuBar();
        app.masterFrame.setJMenuBar(app.topMenu.menuBar);
    }

}
