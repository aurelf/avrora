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

package avrora.sim.util;

import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.util.DeltaQueue;

import avrora.util.Verbose;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The <code>GlobalClock</code> class implements a global timer among multiple
 * simulators by inserting periodic timers into each simulator.
 *
 * @author Ben L. Titzer
 */
public class GlobalQueue {

    //protected double period;
    protected final long cycles;
    protected int goal;
    protected int count;
    protected final Object condition;
    protected final HashMap threadMap;
    protected final DeltaQueue eventQueue;

    protected final Verbose.Printer gqPrinter = Verbose.getVerbosePrinter("sim.global");

    public long getCount() {
        return eventQueue.getCount();
    }

    public GlobalQueue(long p) {
        condition = new Object();
        eventQueue = new DeltaQueue();
        cycles = p;
        threadMap = new HashMap();
    }

    public void add(SimulatorThread t) {
        // TODO: synchronization
        if (threadMap.containsKey(t)) return;
        threadMap.put(t, new LocalTimer(t.getSimulator()));
        goal++;
    }

    public void remove(SimulatorThread t) {
        // TODO: synchronization
        if (!threadMap.containsKey(t)) return;
        LocalTimer lt = (LocalTimer) threadMap.get(t);
        lt.remove();
        threadMap.remove(t);
        goal--;
    }

    public void addTimerEvent(Simulator.Event trigger, long ticks) {
        // TODO: synchronization
        eventQueue.add(trigger, ticks);
    }

    public void removeTimerEvent(Simulator.Event trigger) {
        // TODO: synchronization
        eventQueue.remove(trigger);
    }

    public void addLocalMeet(LocalMeetFactory f, long scale, long delay) {
        Iterator threadIterator = threadMap.keySet().iterator();

        while(threadIterator.hasNext()) {
            SimulatorThread thread = (SimulatorThread)threadIterator.next();
            Simulator sim = thread.getSimulator();

            f.produce(sim, scale, delay);
        }
    }



    // I argue it might not be safe to remove LocalEvents...?
    // TODO: determine how safe removing LocalEvents would be..



    public interface LocalMeetFactory {
        public LocalMeet produce(Simulator s, long scale, long delay);
    }


    public abstract class LocalMeet implements Simulator.Event {
        protected final Simulator simulator;
        protected final long scale;
        protected final long delay;
        protected String id = "CLOCK";
        //protected int count;

        /*
        public LocalMeet(Simulator s, double period) {
            simulator = s;
            //cycles = simulator.getMicrocontroller().millisToCycles(period);
            simulator.insertEvent(this, cycles);
        }
        */

        public LocalMeet(Simulator s, long scale, long delay) {
            simulator = s;
            this.scale = scale;
            this.delay = delay;
            simulator.insertEvent(this, delay);
        }

        public void fire() {
            try {
                synchronized (condition) {
                    if(gqPrinter.enabled) {
                        gqPrinter.println("LocalTime at " + id + " Sync " + simulator.getState().getCycles()
                            + " Global " + globalTime() + ", diff " + (simulator.getState().getCycles() - globalTime()));

                    }
                    count++;

                    if (count < goal) {
                        // if all threads have not arrived yet, wait for the last one
                        condition.wait();
                    } else {
                        // last thread to arrive sets the count to zero and notifies all other threads
                        count = 0;
                        action();
                        condition.notifyAll();
                    }
                }
            } catch (java.lang.InterruptedException e) {
                throw new InterruptedException(e);
            }
        }

        public abstract void action();

        // Global time in local cycle units.
        public long globalTime() {
            return eventQueue.getCount() * cycles;
        }

    }


    static int iNum = 0;

    public class LocalTimer extends LocalMeet {
        private boolean removed;

        LocalTimer(Simulator s) {
            super(s, 6106, 6106);  // TODO: cleanup
            id += iNum++;
        }

        public void fire() {
            super.fire();

            //System.err.println("T " + (simulator.getState().getCycles() + scale));

            //if (!removed) {
                //System.err.println("Bahz " + delay);
                simulator.insertEvent(this, delay);
            //}
        }

        protected void remove() {
            simulator.removeEvent(this);
            removed = true;
        }

        public void action() {
            eventQueue.advance(1);
        }

    }


    /**
     * How sad. The <code>InterruptedException</code> wraps an interrupted
     * exception with an unchecked exception so that it doesn't break
     * the interface of the <code>Simulator.Trigger</code> class.
     * It's not clear what useful purpose interrupted exceptions could
     * serve in the implementation of the global clock.
     */
    public class InterruptedException extends RuntimeException {
        public final java.lang.InterruptedException exception;

        InterruptedException(java.lang.InterruptedException e) {
            exception = e;
        }
    }

}
