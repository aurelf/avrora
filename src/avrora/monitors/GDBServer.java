/**
 * Copyright (c) 2004, Regents of the University of California
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
import avrora.sim.State;
import avrora.util.Option;
import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.Avrora;
import avrora.core.Instr;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;

/**
 * @author Ben L. Titzer
 */
public class GDBServer extends MonitorFactory {

    public static String HELP = "The \"gdb\" monitor implements the GNU Debugger (gdb) remote serial " +
            "protocol. The server will open a socket for listening which GDB can connect to in order to " +
            "send commands to Avrora. This allows gdb to be used as a front end for debugging a program " +
            "running inside of Avrora.";

    private final Option.Long PORT = options.newOption("port", 10001,
            "This option specifies the port on which the GDB server will listen for a connection from " +
            "the GDB frontend.");

    protected class GDBMonitor implements Monitor {

        final Simulator simulator;
        ServerSocket serverSocket;
        Socket socket;
        InputStream input;
        OutputStream output;
        final int port;

        GDBMonitor(Simulator s, int p) {
            simulator = s;
            port = p;
            try {
                serverSocket = new ServerSocket(port);
            } catch ( IOException e ) {
                Avrora.userError("GDBMonitor could not create socket on port "+port, e.getMessage());
            }
            // insert the startup probe at the beginning of the program
            simulator.insertProbe(new StartupProbe(), 0);
        }

        public void report() {
            try {
                if ( socket != null )
                socket.close();
            } catch ( IOException e ) {
                throw Avrora.failure("Unexpected IOException: "+e);
            }
        }

        synchronized void monitorLoop() {
            try {
            while ( true ) {
                    String command = readCommand();
                    if ( command == null ) break;
                    Terminal.println(" --> {"+command+"}");
                    sendPacket("OK");
            }
            } catch ( IOException e ) {
                throw Avrora.failure("Unexpected IOException: "+e);
            }
        }

        void sendPacket(String packet) throws IOException {
            byte[] bytes = packet.getBytes();

            int cksum = 0;
            for ( int cntr = 0; cntr < bytes.length; cksum += bytes[cntr++] ) ;

            String np = "+$"+packet+"#"+StringUtil.toHex(cksum & 0xff, 2);
            Terminal.println("   <-- {"+np+"}");

            output.write(np.getBytes());
        }

        String readCommand() throws IOException {
                int i = input.read();
                if ( i < 0 ) return null;

                StringBuffer buf = new StringBuffer(32);
                buf.append((char)i);

                while ( true ) {
                    i = input.read();
                    if ( i < 0 ) return buf.toString();

                    buf.append((char)i);
                    if ( i == '#') {
                        int i2 = input.read();
                        int i3 = input.read();

                        if ( i2 >= 0 ) buf.append((char)i2);
                        if ( i3 >= 0 ) buf.append((char)i3);
                        return buf.toString();
                    }
                }
        }

        //---------------------------------------------------------------------------
        //-- Probe inserted at beginning of program to wait for GDB connection
        //---------------------------------------------------------------------------
        protected class StartupProbe implements Simulator.Probe {
            public void fireBefore(Instr i, int address, State s) {
                Terminal.println("GDBMonitor listening on port "+port+"...");
                try {
                    socket = serverSocket.accept();
                    input = socket.getInputStream();
                    output = socket.getOutputStream();
                    Terminal.println("Connected established with: "+socket.getInetAddress().getCanonicalHostName());
                    serverSocket.close();
                } catch ( IOException e ) {
                    throw Avrora.failure("Unexpected IOException: "+e);
                }

                monitorLoop();
            }

            public void fireAfter(Instr i, int address, State s) {
                // remove ourselves from the beginning of the program after it has started
                simulator.removeProbe(this, address);
            }
        }

    }


    public GDBServer() {
        super("gdb", HELP);
    }

    public Monitor newMonitor(Simulator s) {
        return new GDBMonitor(s, (int)PORT.get());
    }
}
