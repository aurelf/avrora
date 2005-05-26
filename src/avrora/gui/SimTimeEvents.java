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
import avrora.sim.radio.SimpleAir;

/**
 * In order to speed up and slow down the simulator, we insert events
 * into the simulator queue which trigger the time changes.  These
 * events are declared here and used by <code> Avrora Gui </code> and
 * <code> ManageSimTime </code>
 * <p>
 * I believe everything about how the GUI interacts with the sim is
 * changing in the new version.  Thus, this class may just go away.
 *
 * @author UCLA Compilers Group
 */
public class SimTimeEvents {

    private SimpleAir simpleAir;

    /**
     * This PauseEvent can be inserted in the sim directly
     */
    public PauseEvent pause;

    /**
     * @param psA The class needs to know about the Toplogy used in the sim
     */
    public SimTimeEvents(SimpleAir psA) {
        simpleAir = psA;

        pause = new PauseEvent();
    }

    /**
     * An instance of this class can be inserted into the sim
     * it will pause the sim until <code> unpause() </code> is called.
     */
    public class PauseEvent implements Simulator.Event {
        Object syncObj;
        public boolean ispaused;

        public PauseEvent() {
            syncObj = new Object();
            ispaused = false;
        }

        public void fire() {
            synchronized (syncObj) {
                try {
                    syncObj.wait();
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
        }

        public void unpause() {
            synchronized (syncObj) {
                syncObj.notifyAll();
                ispaused = false;
            }
        }

        public void pause() {
            ispaused = true;
            // TODO: fix implementation of pause
            //simpleAir.insertEvent((Simulator.Event) this, new Long(1).longValue());
        }
    }
}

