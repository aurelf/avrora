package avrora.sim;

import avrora.sim.Simulator;

/**
 * The <code>SimulatorThread</code> class is a thread intended to run a
 * <code>Simulator</code> in a multiple-node simulation. The mapping is
 * one-to-one: each simulator is expected to be run in its own thread.
 * Multiple simulators are then synchronized by being inserted into a group
 * using the <code>GlobalClock</code> class.
 *
 * @see avrora.sim.util.GlobalClock
 * @author Ben L. Titzer
 */
public class SimulatorThread extends Thread {
    protected final Simulator simulator;

    /**
     * The constructor for the simulator thread accepts an instance of <code>Simulator</code>
     * as a parameter and stores it internally.
     * @param s the simulator this thread is intended to run.
     */
    SimulatorThread(Simulator s) {
        simulator = s;
    }

    /**
     * The <code>getSimulator()</code> method gets the <code>Simulator</code> instance that
     * this thread is bound to.
     * @return the instance of <code>Simulator</code> this thread is intended to run.
     */
    public Simulator getSimulator() {
        return simulator;
    }

    /**
     * The <code>run()</code> method begins the simulation, calling the <code>start()</code>
     * method of the <code>Simulator</code> instance associated with this thread.
     */
    public void run() {
        simulator.start();
    }
}
