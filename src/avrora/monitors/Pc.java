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
import avrora.Avrora;


/**
 * The <code>Pc</code> class is a monitor that that is capable
 * of setting up a virtual usart connection to the pc. You can
 * connect the TinyOS serial forwarder to the port 2390.
 *
 * @author Olaf Landsiedel
 */
public class Pc extends MonitorFactory {

    /**
     * The <code>Pc</code> class is a monitor that connects the USART
     * of Node 0 to the TinyOS serial forwarder. Hack!!
     */
    public class Monitor implements avrora.monitors.Monitor{
 
        /** construct a new monitor
         * @param s Simulator
         */
        Monitor(Simulator s) {
            if( s.getID() == 0) {
                throw Avrora.unimplemented();
            }
        }
                
	public void report() {
            //no report
        }
        
    }

    /**
     * The constructor for the <code>Pc</code> class builds a
     * new <code>MonitorFactory</code> capable of creating monitors for
     * each <code>Simulator</code> instance passed to the <code>newMonitor()</code>
     * method.
     */
    public Pc() {
        super("The \"pc\" monitor connects the USART0 of node 0 to the PC using port 2390 as the default.");
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



