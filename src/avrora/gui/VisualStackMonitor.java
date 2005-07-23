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

import avrora.sim.Simulation;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.mcu.MicrocontrollerProperties;

import javax.swing.*;
import java.awt.*;

/**
 * The <code>VisualStackMonitor</code> class is a monitor that tracks the current value of the PC
 * and displays it visually
 *
 * @author UCLA Compilers Group
 */
public class VisualStackMonitor extends SingleNodeMonitor implements Simulation.Monitor {

    public class SPMon extends SingleNodePanel implements Simulator.Event, MonitorPanel.Updater {
        public Simulator simulator;
        public GraphNumbers graph;
        boolean spinit;
        int stacktop;
        int SPL_REG;
        int SPH_REG;

        InitWatch spl;
        InitWatch sph;

        class InitWatch extends Simulator.IORWatch.Empty {
            boolean init;
            public void fireAfterWrite(State s, int addr, byte val) {
                init = true;
                if ( spl.init && sph.init ) {
                    spinit = true;
                    stacktop = s.getSP();
                    simulator.removeIORWatch(spl, SPL_REG);
                    simulator.removeIORWatch(sph, SPH_REG);
                }
            }
        }

        public void fire() {
            // this method is fired every cycle and records the stack pointer
            int height = spinit ? stacktop - simulator.getState().getSP() : 0;
            graph.recordNumber(height);
            simulator.insertEvent(this, 1);
        }

        SPMon(Simulation.Node n, MonitorPanel p) {
            super(n, p);
        }

        public void construct(Simulator s) {
            //This is where we should set up the graph panel itself (aka the chalkbord)
            JPanel displayPanel = panel.displayPanel;
            displayPanel.removeAll();
            displayPanel.setLayout(new BorderLayout());
            graph = new GraphNumbers(displayPanel);
            displayPanel.add(graph.chalkboardAndBar(), BorderLayout.CENTER);
            displayPanel.validate();

            //And we should set up the options panel
            JPanel optionsPanel = panel.optionsPanel;
            optionsPanel.removeAll();
            optionsPanel.setLayout(new BorderLayout());
            optionsPanel.add(graph.getOptionsPanel(), BorderLayout.CENTER);
            optionsPanel.validate();

            panel.setUpdater(this);
            simulator = s;
            simulator.insertEvent(this, 1);

            spl = new InitWatch();
            sph = new InitWatch();

            MicrocontrollerProperties mp = simulator.getMicrocontroller().getProperties();
            SPL_REG = mp.getIOReg("SPL");
            SPH_REG = mp.getIOReg("SPH");

            simulator.insertIORWatch(spl, SPL_REG);
            simulator.insertIORWatch(sph, SPH_REG);
        }

        public void destruct() {
            simulator.removeEvent(this);
        }

        public void remove() {
            simulator.removeEvent(this);
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
    public VisualStackMonitor() {
        super("stack");
    }

    protected SingleNodePanel newPanel(Simulation.Node n, MonitorPanel p) {
        return new SPMon(n, p);
    }
}
