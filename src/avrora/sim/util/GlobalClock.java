package avrora.sim.util;

import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;

import java.util.HashSet;
import java.util.HashMap;

/**
 * The <code>GlobalClock</code> class implements a global timer among multiple
 * simulators by inserting periodic timers into each simulator.
 * @author Ben L. Titzer
 */
public class GlobalClock {

    protected double period;
    protected int goal;
    protected int count;
    protected final Object condition;
    protected final HashMap threadMap;
    protected final DeltaQueue eventQueue;

    public GlobalClock(double p) {
        condition = new Object();
        eventQueue = new DeltaQueue();
        period = p;
        threadMap = new HashMap();
    }

    public void add(SimulatorThread t) {
        // TODO: synchronization
        if ( threadMap.containsKey(t) ) return;
        threadMap.put(t, new LocalTimer(t.getSimulator(), period));
        goal++;
    }

    public void remove(SimulatorThread t) {
        // TODO: synchronization
        if ( !threadMap.containsKey(t) ) return;
        LocalTimer lt = (LocalTimer)threadMap.get(t);
        lt.remove();
        threadMap.remove(t);
        goal--;
    }

    public void addTimerEvent(Simulator.Trigger trigger, long ticks) {
        // TODO: synchronization
        eventQueue.add(trigger, ticks);
    }

    public void removeTimerEvent(Simulator.Trigger trigger) {
        // TODO: synchronization
        eventQueue.remove(trigger);
    }

    protected void tick() {
        count = 0;
        eventQueue.advance(1);
    }

    public class LocalTimer implements Simulator.Trigger {
        private final Simulator simulator;
        private final long cycles;
        private boolean removed;

        LocalTimer(Simulator s, double period) {
            simulator = s;
            cycles = simulator.getMicrocontroller().millisToCycles(period);
            simulator.addTimerEvent(this, cycles);
        }

        public void fire() {
            try {
                synchronized ( condition ) {
                    count++;

                    if ( count < goal ) {
                        // if all threads have not arrived yet, wait for the last one
                        condition.wait();
                    }
                    else {
                        // last thread to arrive sets the count to zero and notifies all other threads
                        tick();
                        condition.notifyAll();
                    }
                }
            } catch ( java.lang.InterruptedException e) {
                throw new InterruptedException(e);
            }

            if ( !removed )
                simulator.addTimerEvent(this, cycles);
        }

        protected void remove() {
            simulator.removeTimerEvent(this);
            removed = true;
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
