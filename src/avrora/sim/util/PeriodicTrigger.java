package avrora.sim.util;

import avrora.sim.Simulator;

/**
 * The <code>PeriodicTrigger</code> class is a utility that allows a <code>Trigger</code>
 * to be inserted into the simulator that will fire with a specified period of clock
 * cycles. It works by automatically adding itself back into the timer queue at each
 * firing.
 *
 * @author Ben L. Titzer
 * @see avrora.sim.Simulator.Trigger
 */
public class PeriodicTrigger implements Simulator.Trigger {
    /**
     * This field stores the simulator in which the trigger will
     * continually be reinserted.
     */
    public final Simulator simulator;

    /**
     * This field stores the trigger that will be fired after each
     * period.
     */
    public final Simulator.Trigger trigger;

    /**
     * This field stores the period (in clock cycles) that the trigger
     * will be fired.
     */
    public final long period;

    /**
     * The constructor for the <code>PeriodicTrigger</code> class creates a
     * new periodic trigger with the specified period. Each time the trigger
     * fires, it will be added again back into the simulator's timer event
     * queue with the same delta. The result is the specified trigger fires
     * with the precise period specified.
     * <p/>
     * Creating the <code>PeriodicTrigger</code> does not insert it into
     * the simulator. It is important that these instances of
     * <code>Simulator</code> match--this probe will always reinsert itself
     * into the instance passed in the constructor.
     *
     * @param s the simulator in which to reinsert the trigger each time
     * @param t the trigger to fire after each period
     * @param p the period in clock cycles
     */
    public PeriodicTrigger(Simulator s, Simulator.Trigger t, long p) {
        trigger = t;
        period = p;
        simulator = s;
    }

    /**
     * The <code>fire()</code> method is called by the simulator when the
     * timer event occurs. In this implementation, the periodic trigger is
     * reinserted into the timer queue, and then the <code>fire()</code>
     * method of the trigger is called.
     */
    public void fire() {
        simulator.addTimerEvent(this, period);
        trigger.fire();
    }
}
