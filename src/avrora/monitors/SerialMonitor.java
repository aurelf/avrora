/**
 * Created on 09.11.2004
 * 
 * Copyright (c) 2004-2005, Olaf Landsiedel, Protocol Engineering and 
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

package avrora.monitors;

import avrora.sim.Simulator;
import avrora.sim.mcu.AtmelMicrocontroller;
import avrora.sim.mcu.USART;
import avrora.sim.platform.SerialForwarder;
import avrora.util.Option;


/**
 * The <code>SerialMonitor</code> class is a monitor that that is capable
 * of setting up a virtual usart connection to the pc. You can
 * connect the TinyOS serial forwarder to the port 2390.
 *
 * @author Olaf Landsiedel
 */
public class SerialMonitor extends MonitorFactory {

    protected final Option.Long PORT = options.newOption("port", 2390,
            "The \"port\" option specifies the server port on which the serial forwarder will " +
            "accept a connection for the serial port.");
    protected final Option.Long NODE = options.newOption("node", 0,
            "The \"node\" option specifies which node's serial port the socket will be connected to.");

    /**
     * The <code>SerialMonitor</code> class is a monitor that connects the USART
     * of a node to a socket that allows data to be read and written from the simulation.
     */
    public class Monitor implements avrora.monitors.Monitor{
 
        /** construct a new monitor
         * @param s Simulator
         */
        Monitor(Simulator s) {
            if( s.getID() == NODE.get()) {
                AtmelMicrocontroller mcu = (AtmelMicrocontroller)s.getMicrocontroller();
                USART usart = (USART)mcu.getDevice("usart0");
                new SerialForwarder(usart, (int)PORT.get());
            }
        }
                
	public void report() {
            //no report
        }
        
    }

    /**
     * The constructor for the <code>SerialMonitor</code> class builds a
     * new <code>MonitorFactory</code> capable of creating monitors for
     * each <code>Simulator</code> instance passed to the <code>newMonitor()</code>
     * method.
     */
    public SerialMonitor() {
        super("The \"serial\" monitor allows the serial port (UART) of a node in the simulation to be connected " +
                "to a socket so that data from the program running in the simulation can be outputted, and " +
                "external data can be fed into the serial port of the simulated node.");
    }

    /**
     * The <code>newMonitor()</code> method creates a new monitor that is capable
     * of setting up a virtual usart connection to the pc. You can connect the TinyOS
     * serial forwarder to the port 2390.
     * @param s the simulator to create a monitor for
     * @return an instance of the <code>Monitor</code> interface for the
     * specified simulator
     */
    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Monitor(s);
    }    
}



