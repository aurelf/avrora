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
import avrora.sim.radio.Radio;
import avrora.sim.util.DeltaQueue;

import avrora.util.Verbose;
import avrora.Avrora;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The <code>GlobalClock</code> class implements a global timer among multiple
 * simulators by inserting periodic timers into each simulator. It is an alternate version
 * of the <code>GlobalClock</code> class that was developed for use with the synchronization
 * policy used in the CC1000 - SimpleAir radio implementation.
 *
 * A verbose printer for this class can be accessed through "sim.global".
 *
 * @author Ben L. Titzer, Daniel Lee
 */
public class GlobalClock {

    /**
     * <code>cycles</code> is the number of cycles on a member local clock per cycle
     * on the global clock. Some re-coding must be done if microcontrollers running
     * at difference speeds are to be accurately simulated.
     */
    public final long period;
    protected final HashMap threadMap;
    public final Ticker ticker;

    protected final Verbose.Printer gqPrinter = Verbose.getVerbosePrinter("sim.global");

    public GlobalClock(long p) {
        this(p, new Ticker(p));
    }

    protected GlobalClock(long p, Ticker t) {
        ticker = t;
        period = p;
        threadMap = new HashMap();
    }

    public synchronized void add(SimulatorThread t) {
        if (threadMap.containsKey(t)) return;

        threadMap.put(t, ticker);
        t.getSimulator().insertEvent(ticker, period);
        ticker.goal++;
    }

    public void remove(SimulatorThread t) {
        throw Avrora.unimplemented();
    }

    /**
     * Adds an <code>Event</code> to this global event queue. It is important to note
     * that this method adds an event executed once at the appropriate global time.
     * It does not execute once in each thread participating in the clock. For such
     * functionality, see <code>LocalMeet</code>. Note that the event, when fired,
     * may run in any one of the threads participating in the global clock, and not
     * necessarily the same thread each time.
     */
    public synchronized void insertEvent(Simulator.Event event, long ticks) {
        ticker.eventQueue.add(event, ticks);
    }

    public synchronized void removeEvent(Simulator.Event event) {
        ticker.eventQueue.remove(event);
    }

    /**
     * Adds a <code>LocalMeet</code> event to the event queue of every simulator
     * participating in the global clock.
     */
    public void addLocalMeet(LocalMeet m, long delay) {
        Iterator threadIterator = threadMap.keySet().iterator();

        while (threadIterator.hasNext()) {
            SimulatorThread thread = (SimulatorThread) threadIterator.next();
            Simulator sim = thread.getSimulator();

            sim.insertEvent(m, delay);
        }
    }

    public static abstract class LocalMeet implements Simulator.Event {
        protected final String id;
        protected final Object condition;
        protected int goal;
        protected int count;

        protected LocalMeet(String id) {
            this.id = id;
            condition = new Object();
        }

        /**
         * The <code>fire()</code> method of this event is called by the individual
         * event queues of each simulator as they reach this point in time. The implementation
         * of this method waits for all threads to join. It will then execute the
         * <code>serialAction()</code> method in the last thread to join the clock, and execute
         * the <code>parallelAction</code> in each of the threads, in parallel, and then release
         * the threads by returning back to the Simulator.
         */
        public void fire() {
            try {
                synchronized (condition) {
                    // increment the count of the number of threads that have entered
                    count++;

                    // run the code that should happen just before synchronization (parallel)
                    preSynchAction();

                    if (count < goal) {
                        // if all threads have not arrived yet, wait for the last one
                        condition.wait();
                    } else {
                        // last thread to arrive sets the count to zero and notifies all other threads
                        count = 0;
                        // perform the action that should be run while all threads are stopped (serial)
                        serialAction();
                        // release threads
                        condition.notifyAll();
                    }
                    // perform action that should be run after synchronization (parallel)
                    parallelAction((SimulatorThread)Thread.currentThread());
                }
            } catch (java.lang.InterruptedException e) {
                throw new InterruptedException(e);
            }
        }

        /**
         * The <code>preSynchAction()</code> method implements the functionality that
         * must be performed just after the thread enters the local meet, but before
         * it blocks waiting for the other threads. It is called with the
         * <code>condition</code> monitor held.
         */
        public abstract void preSynchAction();

        /**
         * The <code>serialAction()</code> method implements the functionality that
         * must be performed in serial when the threads have joined at this local meet.
         * This method will execute in the last thread to enter the fire() method.
         */
        public abstract void serialAction();

        /**
         * The <code>parallelAction()</code> method implements the functionality that
         * must be performed in parallel when the threads have joined at this local
         * meet, and after the serial action has been completed. It will be called in
         * each thread, with the parameter passed being the current SimulatorThread.
         * @param st the current <code>SimulatorThread</code> instance for this thread
         */
        public abstract void parallelAction(SimulatorThread st);

    }

    public long getCount() {
        return ticker.eventQueue.getCount();
    }

    public long globalTime() {
        return ticker.eventQueue.getCount() * period;
    }

    public int getNumberOfThreads() {
        return ticker.goal;
    }

    /**
     * The <code>Ticker</code> class is an event that fires in the local queues
     * of participating threads. This class is necessary for ensuring the integrity of the
     * global clock.
     */
    public static class Ticker extends LocalMeet {

        public final long period;
        protected final DeltaQueue eventQueue;

        protected Ticker(long p) {
            super("GLOBAL CLOCK");
            period = p;
            eventQueue = new DeltaQueue();
        }

        public void preSynchAction() {
            // do nothing.
        }

        public void serialAction() {
            eventQueue.advance(1);
        }

        public void parallelAction(SimulatorThread s) {
            s.getSimulator().insertEvent(this, period);
        }

    }


    /**
     * How sad. The <code>InterruptedException</code> wraps an interrupted
     * exception with an unchecked exception so that it doesn't break
     * the interface of the <code>Simulator.Event</code> class.
     * It's not clear what useful purpose interrupted exceptions could
     * serve in the implementation of the global clock.
     */
    public static class InterruptedException extends RuntimeException {
        public final java.lang.InterruptedException exception;

        public InterruptedException(java.lang.InterruptedException e) {
            exception = e;
        }
    }

}
