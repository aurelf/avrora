package avrora.sim.util;

import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;

/**
 * The <code>GlobalClock</code> class implements a global timer among multiple
 * simulators by inserting periodic timers into each simulator.
 * @author Ben L. Titzer
 */
public class GlobalClock {

    int goal;
    int count;
    final Object condition;

    public GlobalClock(double period, SimulatorThread[] sims) {

        for ( int cntr = 0; cntr < sims.length; cntr++ ) {
            new LocalTimer(sims[cntr].getSimulator(), period);
        }

        goal = sims.length;
        condition = new Object();
    }

    public void add(SimulatorThread t) {

    }

    public void remove(SimulatorThread t) {
        
    }

    public class LocalTimer implements Simulator.Trigger {
        private final Simulator simulator;
        private final long cycles;

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
                        count = 0;
                        condition.notifyAll();
                    }
                }
            } catch ( java.lang.InterruptedException e) {
                throw new InterruptedException(e);
            }

            simulator.addTimerEvent(this, cycles);
        }

    }

    public class InterruptedException extends RuntimeException {
        public final java.lang.InterruptedException exception;

        InterruptedException(java.lang.InterruptedException e) {
            exception = e;
        }
    }

}
