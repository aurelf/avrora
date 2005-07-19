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

import avrora.sim.Simulator;
import avrora.sim.platform.Platform;
import avrora.sim.energy.Energy;
import avrora.sim.energy.*;
import avrora.sim.radio.Radio;
import avrora.util.StringUtil;
import avrora.util.Terminal;
import avrora.util.Option;

import java.util.*;

/**
 * energy monitor implementation this class handles logging and
 * recording of power consumption.
 *
 * Furthermore the monitor shutsdown the node, when an energy limit is exceeded.
 *
 * @author Olaf Landsiedel
 */
public class EnergyMonitor extends MonitorFactory {

    protected final Option.Double BATTERY = options.newOption("battery", 0.0,
            "This option specifies the number of joules in each node's battery. During " +
            "simulation, the energy consumption of each node is tracked, and if the node " +
            "runs out of battery, it will be shut down and removed from the " +
            "simulation.");

    /**
     * @author Olaf Landsiedel
     *
     * The <code>EnergyMonitor</code> class implements an energy monitor that provides detailed
     * feedback of the power consumption of nodes as they execute. Furthermore, the monitor shuts down
     * the node when an energy limit is exceeded.
     *
     */
    public class Monitor implements avrora.monitors.Monitor{

        // the simulator
        protected Simulator simulator;
        protected Platform platform;
        // list of consumers
        protected LinkedList consumer;
        //energy a node is allowed to consume (in joule)
        private double energy;

        /**
         * Create a new energy monitor. Creates a file with logging information: temp.log that contains the
         * current draw of all devices, and the state changes can be loaded into Matlab, Gnuplot, Excel... for
         * further processing and visualization.
         *
         * @param s the simulator
         */
        Monitor(Simulator s) {
            this.simulator = s;
            this.platform = s.getMicrocontroller().getPlatform();
            //activate energy monitoring....
            //so the state machine is set up for energy monitoring when needed
            EnergyControl.activate();
            this.consumer = EnergyControl.getConsumers();

            energy = BATTERY.get();
            if ( energy > 0 ) {
                new BatteryCheck();
            }

        }


        /**
         * implemenation of report of Monitor class. Called when the simulation ends and reports summaries for
         * the power consumption of all devices to the stdout
         *
         * @see avrora.monitors.Monitor#report()
         */
        public void report() {
            //simulation will end
            //provide component energy breakdown
            Terminal.printCyan("\nEnergy Consumption Component Breakdown:\n\n");
            long cycles = simulator.getState().getCycles();
            Terminal.println("Node lifetime: " + cycles + " cycles,  " + simulator.getMicrocontroller().cyclesToMillis(cycles) / 1000.0+ " seconds\n");
            // get energy information for each device
            Iterator it = consumer.iterator();
            while( it.hasNext() ){
                //get energy information
                Energy en = (Energy)it.next();
                int modes = en.getModeNumber();
                Terminal.println(en.getName() + ": " + en.getTotalConsumedEnergy() + " Joule");
                // get information for each state
                for (int j = 0; j < modes; j++)
                    //when there are more than 10 modes, only print the ones the system was in
                    if (modes <= 10 || en.getCycles(j) > 0)
                        Terminal.println("   " + en.getModeName(j) + ": " + en.getConsumedEnergy(j) + " Joule, " + en.getCycles(j) + " cycles");
                Terminal.nextln();
            }
        }


        public class BatteryCheck implements Simulator.Event{
            //check 10 times per second
            private static final int interval = 737280;
            
            public BatteryCheck(){
                simulator.insertEvent(this, interval);
            }

            public void fire(){
                double totalEnergy = 0.0d;
                Iterator it = consumer.iterator();
                //for (int i = 0; i < consumer.size(); ++i) {
                while(it.hasNext()){
                    //get energy information
                    totalEnergy += ((Energy)it.next()).getTotalConsumedEnergy();
                }
                if( totalEnergy <= energy ){
                    //lets go on
                    simulator.insertEvent(this, interval);
                } else {
                    //shutdown this node
                    String idstr = StringUtil.rightJustify(simulator.getID(), 4);
                    String cycstr = StringUtil.rightJustify(simulator.getClock().getCount(), 10);
                    Terminal.print(idstr + " " + cycstr + "   ");
                    Terminal.print("energy limit exceed, shutdown node: ");
                    Terminal.println("consumed " + totalEnergy + " Joule");

                    //remove radio
                    Radio radio = (Radio)platform.getDevice("radio");
                    radio.getAir().removeRadio(radio);
                    //stop loop
                    simulator.stop();
                }

            }
        }
    }


    /**
     * create a new monitor
     */
    public EnergyMonitor() {
        super("The \"energy\" is a monitor to trace energy consumption");
    }

    public EnergyMonitor(String s1, String s2) {
        super(s2);
    }

    /**
     * create a new monitor, calls the constructor
     *
     * @see avrora.monitors.MonitorFactory#newMonitor(avrora.sim.Simulator)
     */
    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }
}

