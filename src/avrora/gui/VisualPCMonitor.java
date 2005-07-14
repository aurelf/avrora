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

import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.Simulation;
import avrora.sim.util.ProgramProfiler;
import avrora.gui.*;
import avrora.actions.VisualAction;
import avrora.monitors.MonitorFactory;
import avrora.Avrora;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * The <code>PCMonitor</code> class is a monitor that tracks the current value of the PC
 * and displays it visually
 *
 * @author UCLA Compilers Group
 */
public class VisualPCMonitor extends SingleNodeMonitor implements Simulation.Monitor {

    public class PCMonitor extends SingleNodePanel implements Simulator.Probe, MonitorPanel.Updater {
        public Simulator simulator;
        public JPanel visualOptionsPanel;
        public GraphNumbers graph;
        public Object vSync;


        public void fireBefore(State s, int address) {
            // do nothing
        }

        /**
         * After each instruction, we add the PC value to a Graph Numbers
         * database
         */
        public void fireAfter(State s, int address) {
            //add address to our vector
            graph.addToVector(address);
        }

        PCMonitor(Simulation.Node n, MonitorPanel p) {
            super(n, p);
        }

        public void construct(Simulator s) {
            //This is where we should set up the graph panel itself (aka the chalkbord)
            JPanel displayPanel = panel.displayPanel;
            displayPanel.removeAll();
            displayPanel.setLayout(new BorderLayout());
            graph = new GraphNumbers(displayPanel, 0, 500, 3);
            displayPanel.add(graph.chalkboardAndBar(), BorderLayout.CENTER);
            displayPanel.validate();
            panel.setUpdater(this);
            simulator = s;
            simulator.insertProbe(this);
        }

        public void destruct() {
            simulator.removeProbe(this);
        }

        public void remove() {
            simulator.removeProbe(this);
        }

        public void update() {
            if ( graph.internalUpdate() ) {
                graph.paint(graph.getGraphics());
            }
        }

    }

    /**
     * The constructor for the <code>VisualPCMonitor</code> class builds a new <code>MonitorFactory</code>
     * capable of creating monitors for each <code>Simulator</code> instance passed to the
     * <code>newMonitor()</code> method.
     */
    public VisualPCMonitor() {
        super("pc");
    }

    protected SingleNodePanel newPanel(Simulation.Node n, MonitorPanel p) {
        return new PCMonitor(n, p);
    }
}
