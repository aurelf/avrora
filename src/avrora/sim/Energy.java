/**
 * Created on 18. September 2004, 20:41
 * 
 * Copyright (c) 2004, Olaf Landsiedel, Protocol Engineering and 
 * Distributed Systems, University of Tuebingen
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
 * Neither the name of the Protocol Engineering and Distributed Systems
 * Group, the name of the University of Tuebingen nor the names of its 
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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


package avrora.sim;

/**
 * Class for energy modeling. All consumers create an instance of this class and keep it updated with all
 * state changes concerning power consumption. This class keeps track of all these state changes and cycles
 * spend in each state. The state changes are propagated to monitors based on a subscription system. This
 * enables logging of power consumption.
 *
 * @author Olaf Landsiedel
 */
public class Energy {

    //name of the device, which energy consumption is traced by
    //this class instance
    private String deviceName;
    //current draw for each state
    private double amphere[];
    // name of each state
    private String name[];
    //cycles spend in each state
    private long cycles[];
    // current state, e.g. mode
    private int currentMode = 0;
    // the mode (e.g. state) the system was in before
    private int oldMode = 0;
    // cycle the state was changed last
    private long lastChange = 0;
    // voltage, needed for computation of energy consumption
    private static final double voltage = 3.0d;
    // mcu frequecy
    private int freq;
    // time one mcu cycle takes
    private double cycleTime;
    // there is one energyControl in the simulation
    // it handles the notification of monitrs
    private EnergyControl energyControl;
    // state of the simulation
    private State state;

    /**
     * create new energy class, to enable energy modelling
     *
     * @param deviceName  name of the device to model
     * @param modeAmphere array of current draw for each device state (in Amphere)
     * @param modeName    array of the names of each device state
     * @param cpuFreq     cpu frequency
     * @param startMode   mode or state of the device at startup and reset
     * @param ec          the simulator energy control
     * @param st          the simulator state
     */
    public Energy(String deviceName, double modeAmphere[], String modeName[], int cpuFreq, int startMode, EnergyControl ec, State st) {
        // remember all params
        this.deviceName = deviceName;
        this.amphere = modeAmphere;
        this.name = modeName;
        this.currentMode = startMode;
        this.freq = cpuFreq;
        this.cycleTime = 1.0d / cpuFreq;
        this.energyControl = ec;
        this.state = st;
        // subscribe this consumer to the energy control
        energyControl.addConsumer(this);
        // setup cycle array to store the cycles of each state
        cycles = new long[amphere.length];
        for (int i = 0; i < cycles.length; i++)
            cycles[i] = 0;
    }

    /**
     * set the current mode or state of the device
     *
     * @param mode mode numer to set
     */
    public void setMode(int mode) {
        if (mode != currentMode) {
            cycles[currentMode] += state.getCycles() - lastChange;
            oldMode = currentMode;
            currentMode = mode;
            lastChange = state.getCycles();
            energyControl.stateChange(this);
            return;
        }
        return;
    }

    /**
     * get the power consumption of this device
     *
     * @return power consumption in Joule
     */
    public double getTotalConsumedEnergy() {
        double total = 0.0d;
        for (int i = 0; i < amphere.length; i++)
            total += getConsumedEnergy(i);
        return total;
    }

    /**
     * get the power consumption of a state
     *
     * @param mode the mode or state
     * @return power consumption in Joule
     */
    public double getConsumedEnergy(int mode) {
        return voltage * getCycles(mode) * amphere[mode] * cycleTime;
    }

    /**
     * get the number of modes of this device
     *
     * @return mode number
     */
    public int getModeNumber() {
        return amphere.length;
    }

    /**
     * get the current state or mode of the device
     *
     * @return current mode
     */
    public int getCurrentMode() {
        return currentMode;
    }

    /**
     * get the name of a mode
     *
     * @param mode mode number
     * @return mode name
     */
    public String getModeName(int mode) {
        return name[mode];
    }

    /**
     * get the current draw of a mode
     *
     * @param mode mode number
     * @return current draw in Amphere
     */
    public double getModeAmphere(int mode) {
        return amphere[mode];
    }

    /**
     * get the cycles spend in a device state
     *
     * @param mode mode number
     * @return cycles
     */
    public long getCycles(int mode) {
        long ret = cycles[mode];
        if (mode == currentMode)
            ret += state.getCycles() - lastChange;
        return ret;
    }

    /**
     * get the device name
     *
     * @return device name
     */
    public String getName() {
        return deviceName;
    }

    /**
     * get the names of all modes
     *
     * @return array with all mode names
     */
    public String[] getModeNames() {
        return name;
    }

    /**
     * get old mode
     *
     * @return old mode
     */
    public int getOldMode() {
        return oldMode;
    }

    /**
     * get the current draw
     *
     * @return current draw in Amphere
     */
    public double getCurrentAmphere() {
        return amphere[currentMode];
    }

    /**
     * get the current draw of the old mode
     *
     * @return current draw
     */
    public double getOldAmphere() {
        return amphere[oldMode];
    }

}
