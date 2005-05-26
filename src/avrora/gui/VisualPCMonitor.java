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
public class VisualPCMonitor extends SingleNodeMonitor implements VisualSimulation.MonitorFactory {

    public class PCMonitor extends Monitor implements avrora.gui.VisualMonitor, Simulator.Probe {
        public final Simulator simulator;
        public final Program program;
        public JPanel visualPanel;
        public JPanel visualOptionsPanel;
        public GraphNumbers theGraph;
        public Object vSync;

        //ugly temp hack - delete once you figure out permanent solution for
        //global monitors
        GraphEvents tempEvent;

        public GraphEvents getGraph() {
            return tempEvent;
        }


        /**
         * This will defer to the GraphNumbers internal methods
         * to handle the update of the data and the repaint
         */
        public void updateDataAndPaint() {
            //So if there are new numbers that we added,
            //we repaint the thing.
            if (theGraph.internalUpdate()) {
                //So I know, I know - I'm suppose to call repaint()
                //But it doesn't work in this case...Java batches
                //the repaint request and gets to it when it feels like
                //it....destroying the illusion of seeing the graph
                //update in real time.
                //I guess my point is, you can change this this to repaint,
                //but we REALLY want paint ot be called and not have the AWT
                //mess anything up or decide to do something else.
                theGraph.paint(theGraph.getGraphics());
            }
        }


        /**
         * allows vAction to link the GUI and our monitor via the passed panels..
         * it is also where we init our graph and start the paint thread
         * Think of it as the constructor for the visual elements of this monitor
         */
        public void setVisualPanel(JPanel thePanel, JPanel theOptionsPanel) {
            visualPanel = thePanel;

            //This is where we should set up the graph panel itself (aka the chalkbord)
            visualPanel.removeAll();
            visualPanel.setLayout(new BorderLayout());
            theGraph = new GraphNumbers(0, 500, 3);
            theGraph.setParentPanel(visualPanel);
            visualPanel.add(theGraph.chalkboardAndBar(), BorderLayout.CENTER);
            visualPanel.validate();

            //And we should set up the options panel
            visualOptionsPanel = theOptionsPanel;
            visualOptionsPanel.removeAll();
            visualOptionsPanel.setLayout(new BorderLayout());
            visualOptionsPanel.add(theGraph.getOptionsPanel(), BorderLayout.CENTER);
            visualOptionsPanel.validate();
        }

        public void fireBefore(State s, int address) {
            // do nothing
        }

        /**
         * After each instruction, we add the PC value to a Graph Numbers
         * database
         */
        public void fireAfter(State s, int address) {
            //add address to our vector
            theGraph.addToVector(address);
        }

        PCMonitor(Simulator s) {
            simulator = s;
            program = s.getProgram();
            // insert the global probe
            s.insertProbe(this);
        }

        /**
         * The <code>report()</code> method generates a textual report after the simulation is complete.
         * The report does nothing in this case, because this is a visual monitor
         */
        public void report() {
            updateDataAndPaint();  //in case there is still stuff in the queue...
            //we better take it out
        }

        protected void remove() {
            // TODO: is report necessary when we remove?
            report();
            simulator.removeProbe(this);
        }

    }

    /**
     * The constructor for the <code>VisualPCMonitor</code> class builds a new <code>MonitorFactory</code>
     * capable of creating monitors for each <code>Simulator</code> instance passed to the
     * <code>newMonitor()</code> method.
     */
    public VisualPCMonitor() {
        super();
    }

    protected Monitor newMonitor(VisualSimulation.Node n) {
        return new PCMonitor(n.getSimulator());
    }
}
