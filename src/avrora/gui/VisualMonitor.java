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

import javax.swing.*;

import avrora.actions.VisualAction;
import avrora.gui.GraphEvents;
import avrora.monitors.Monitor;

/**
 * The <code>Monitor</code> class represents a monitor attached to a <code>Simulator</code> instance. Created
 * by the <code>MonitorFactory</code> class, a monitor collects statistics about a program as it runs, and
 * then when the simulation is complete, generates a report.
 *
 * @author Ben L. Titzer
 */
public interface VisualMonitor extends Monitor {

    public void updateDataAndPaint(); //to be called by the thread that is periodically
    //repainting this monitor's chalkboard

    public GraphEvents getGraph();

    public void setVisualPanel(JPanel thePanel, JPanel theOptionsPanel, VisualAction vAction);

    /**
     * The <code>report()</code> method is called after the simulation is complete. The monitor generates a
     * textual or other format representation of the information collected during the execution of the
     * program.
     */
    public void report();
}