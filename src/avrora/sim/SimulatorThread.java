package avrora.sim;

import avrora.sim.Simulator;

/**
 * @author Ben L. Titzer
 */
public class SimulatorThread extends Thread {
    protected final Simulator simulator;

    SimulatorThread(Simulator s) {
        simulator = s;
    }

    public Simulator getSimulator() {
        return simulator;
    }

    public void run() {
        simulator.start();
    }
}
