package avrora.sim.util;

import avrora.sim.Simulator;

/**
 * The <code>PeriodicTrigger</code> class is a utility that allows a <code>Trigger</code>
 * to be inserted into the simulator that will fire with a specified period of clock
 * cycles. It works by automatically adding itself back into the timer queue at each
 * firing.
 * @author Ben L. Titzer
 */
public class PeriodicTrigger implements Simulator.Trigger {
    protected final Simulator simulator;
    protected final Simulator.Trigger trigger;
    protected final long period;

    PeriodicTrigger(Simulator s, Simulator.Trigger t, long p) {
        trigger = t;
        period = p;
        simulator = s;
    }

    public void fire() {
        simulator.addTimerEvent(this, period);
        trigger.fire();
    }
}
