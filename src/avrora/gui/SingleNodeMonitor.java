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

import avrora.sim.Simulator;
import avrora.Avrora;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

/**
 * @author Ben L. Titzer
 */
public abstract class SingleNodeMonitor implements VisualSimulation.MonitorFactory {

    final HashMap panelMap; // maps VisualSimulation.Node -> MonitorPanel
    final HashMap monitorMap; // maps MonitorPanel -> PCMonitor

    public SingleNodeMonitor() {
        panelMap = new HashMap();
        monitorMap = new HashMap();
    }

    public void attach(List nodes) {
        Iterator i = nodes.iterator();
        while ( i.hasNext()) {
            VisualSimulation.Node n = (VisualSimulation.Node)i.next();
            if ( panelMap.containsKey(n) ) continue;
            MonitorPanel p = AvroraGui.instance.createMonitorPanel("PC - "+n.id);
            panelMap.put(n, p);
            n.addMonitor(this);
        }
    }

    public void instantiate(VisualSimulation.Node n, Simulator s) {
        throw Avrora.unimplemented();
    }

    public void remove(List nodes) {
        Iterator i = nodes.iterator();
        while ( i.hasNext()) {
            VisualSimulation.Node n = (VisualSimulation.Node)i.next();
            removeOne(n);
        }
    }

    private void removeOne(VisualSimulation.Node n) {
        MonitorPanel p = (MonitorPanel)panelMap.get(n);
        if ( p == null ) return;

        Monitor pc = (Monitor)monitorMap.get(p);
        if ( pc != null ) {
            pc.remove();
            monitorMap.remove(p);
        }

        AvroraGui.instance.removeMonitorPanel(p);
        panelMap.remove(n);
        n.removeMonitor(this);
    }

    protected abstract class Monitor {
        protected abstract void remove();
    }

    protected abstract Monitor newMonitor(VisualSimulation.Node n);
}
