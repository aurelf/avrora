/**
 * Copyright (c) 2004-2005, Regents of the University of California
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

package avrora.sim.platform;

import avrora.sim.Simulator;
import avrora.sim.Clock;
import avrora.sim.mcu.USART;
import avrora.Avrora;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * The <code>SerialForwarder</code> class implements a serial forwarder that takes traffic
 * to and from a socket and directs it into the UART chip of a simulated device.
 *
 * @author Olaf Landsiedel
 * @author Ben L. Titzer
 */
public class SerialForwarder implements USART.USARTDevice {

    public static final int BPS = 2400;

    private ServerSocket serverSocket;
    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private USART usart;
    private SFTicker ticker;
    private byte[] data;
    private Clock clock;

    public SerialForwarder(USART usart) {
        // TODO: what should be done about the ticker?

        this.usart = usart;
        clock = usart.getClock();
        ticker = new SFTicker();
        data = new byte[1];
        try{
            serverSocket = new ServerSocket(2390);
            socket = serverSocket.accept();
            out = socket.getOutputStream();
            in = socket.getInputStream();
        } catch( IOException e ){
            throw Avrora.failure("cannot connect to serial forwarder");
        }
    }

    public USART.Frame transmitFrame() {
        try{
            in.read( data, 0, 1);
            return new USART.Frame(data[0], false, 8);
        } catch( IOException e){
            throw Avrora.failure("cannot read from socket");
        }
    }


    public void receiveFrame(USART.Frame frame) {
        try{
            out.write(frame.low);
        } catch( IOException e){
            throw Avrora.failure("cannot write to socket");
        }
    }

    private class SFTicker implements Simulator.Event {
        private final long delta;

        public SFTicker(){
            delta = clock.getHZ() / BPS;
            clock.insertEvent(this, delta);
        }

        public void fire() {
            try{
                if( in.available() >= 1 ) {
                    usart.startReceive();
                }
            } catch( IOException e){
                throw Avrora.failure("cannot read from socket");
            }
            clock.insertEvent(this, delta);
        }
    }
}
