/**
 * Created on 18. September 2004, 22:02
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


package avrora.monitors;

import avrora.Avrora;
import avrora.sim.Energy;
import avrora.sim.EnergyControl;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.radio.Radio;
import avrora.sim.radio.RadioAir;
import avrora.util.Options;
import avrora.util.StringUtil;
import avrora.util.Terminal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * energy monitor implementation this class handles logging and 
 * recording of power consumption.
 * 
 * Furthermore the monitor shutsdown the node, when an energy limit is exceeded.
 *
 * @author Olaf Landsiedel
 */
public class EnergyMonitorLog extends EnergyMonitor {

    // TODO: merge this class with EnergyMonitor and introduce options for logging

    /**
     * @author Olaf Landsiedel
     *
     * The <code>EnergyMonitorLog</code>  
     * energy monitor implementation
     * Creates a file with logging information: energyNODEIS.log that contains the
     * current draw of all devices, and the state changes can be loaded into 
     * Matlab, Gnuplot, Excel... for further processing and visualization. 
     * 
     * this class extends EneryMonitor
     * 
     * Furthermore the monitor shutsdown the node, when an energy limit is exceeded.
     * 
     */    
    public class Monitor extends EnergyMonitor.Monitor implements EnergyMonitorBase {

        // file for data logging
        private BufferedWriter file;

        
        /**
         * Create a new energy monitor. Creates a file with logging information: temp.log that contains the
         * current draw of all devices, and the state changes can be loaded into Matlab, Gnuplot, Excel... for
         * further processing and visualization.
         *
         * @param s the simulator
         */
        Monitor(Simulator s, Options options) {
            super(s, options);
            // subscribe the monitor to the energy  control
            energyControl.subscribe((EnergyMonitorBase)this);

            //open file for logging, currently with fixed path and file name
            String fileName = "energy" + simulator.getID() + ".log";
            try {
                this.file = new BufferedWriter(new FileWriter(fileName));
            } catch (IOException e) {
                Avrora.userError("Cannot create log file", fileName);
            }
            
            //write headlines 
            //first: cycle
            write("cycle ");
            //and than all consumers names
            Iterator it = consumer.iterator();
            while( it.hasNext() ){
            //for (int i = 0; i < consumer.size(); ++i) {
                Energy en = (Energy)it.next();
                write(en.getName() + " ");
            }
            write("total");
            newLine();
            
            //log the startup state
            logCurrentState();
        }

        /**
         * write text or data to the log file
         *
         * @param text data or text to write
         */
        private void write(String text) {
            try {
                file.write(text);
            } catch (IOException e) {
                throw Avrora.failure("cannot write to log file");
            }
        }

        /**
         * add new line to the log file
         */
        private void newLine() {
            try {
                file.newLine();
            } catch (IOException e) {
                throw Avrora.failure("cannot write to log file");
            }
        }

        /**
         * implemenation of report of Monitor class. Called when the simulation ends and reports summaries for
         * the power consumption of all devices to the stdout
         *
         * @see avrora.monitors.Monitor#report()
         */
        public void report() {
            super.report();
            
            //simulation will end
            //update log file
            logCurrentState();
            
            //close log file
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                throw Avrora.failure("could not flush and / or close log file");
            }
        }


        /**
         * called when the state of the device changes this compoent logs these state changes
         *
         * @see avrora.monitors.EnergyMonitorBase#fire(avrora.sim.Energy)
         */
        public void fire(Energy energy) {
            logOldState(energy);
            logCurrentState();
            return;
        }


        /**
         * log the current state
         */
        private void logCurrentState() {
            //write new state
            //first: current cycles
            write(state.getCycles() + " ");
            //and than all consumers
            double total = 0.0f;
            Iterator it = consumer.iterator();
            while(it.hasNext()){
                Energy en = (Energy)it.next();
                double amphere = en.getCurrentAmphere();
                total += amphere;
                write(amphere + " ");
            }
            write(total + "");
            newLine();
        }


        /**
         * log the old state
         *
         * @param energy device, which state changed
         */
        private void logOldState(Energy energy) {
            //write old state
            //first: old cycles
            write((state.getCycles() - 1) + " ");
            //and than all consumers
            double total = 0.0f;
            Iterator it = consumer.iterator();
            //for (int i = 0; i < consumer.size(); ++i) {
            while( it.hasNext() ){
                Energy en = (Energy)it.next();
                double amphere = 0.0f;
                if (en != energy) {
                    amphere = en.getCurrentAmphere();
                } else {
                    amphere = en.getOldAmphere();
                }
                total += amphere;
                write(amphere + " ");
            }
            write(total + "");
            newLine();
        }
    }
    
    
    /**
     * create a new monitor
     */
    public EnergyMonitorLog() {
        super("energy-log", "The \"energy-log\" is a monitor to trace energy consumption and log it," +
                "the trace file is energyNODEID.log");
    }


    /**
     * create a new monitor, calls the constructor
     *
     * @see avrora.monitors.MonitorFactory#newMonitor(avrora.sim.Simulator)
     */
    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s, options);
    }
}

