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
import avrora.core.Register;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.text.StringCharacterIterator;
import java.text.CharacterIterator;

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
        BreakpointProbe BREAKPROBE = new BreakpointProbe();
        StepProbe STEPPROBE = new StepProbe();
        Simulator.Printer printer;


        GDBMonitor(Simulator s, int p) {
            simulator = s;
            port = p;
            printer = simulator.getPrinter("sim.gdb-server");
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

        // keep executing commands until a continue is sent
        synchronized void monitorLoop(String reply) {
            try {
                if ( reply != null ) {
                    sendPacket(reply);
                }

                while ( true ) {
                    String command = readCommand();
                    if ( command == null ) {
                        printer.println("Null command: stopping simulator");
                        // TODO: report the hang up
                        simulator.stop();
                        break;
                    }
                    printer.println(" --> "+command);
                    if ( executeCommand(command) )
                        break;
                }
            } catch ( IOException e ) {
                throw Avrora.failure("Unexpected IOException: "+e);
            }
        }

        boolean executeCommand(String command) throws IOException {
            CharacterIterator i = new StringCharacterIterator(command);
            if ( i.current() == '+' ) i.next();
            if ( !StringUtil.peekAndEat(i, '$') ) {
                commandError();
                return false;
            }

            char c = i.current();
            i.next();

            switch ( c ) {
                case 'c':
                    // CONTINUE WITH EXECUTION
                    sendPlus();
                    return true;
                case 'D':
                    // DISCONNECT
                    // TODO: do more work at disconnection time?
                    sendPlus();
                    simulator.stop();
                    return true;
                case 'g':
                    // READ REGISTERS
                    readAllRegisters();
                    return false;
                case 'G':
                    // WRITE REGISTERS
                    break;
                case 'H':
                    // TODO: more work at connection time?
                    sendPacketOK("OK");
                    return false;
                case 'i':
                    // STEP CYCLE
                    break;
                case 'k':
                    // KILL
                    // TODO: do more work at disconnection time?
                    sendPlus();
                    simulator.stop();
                    return true;
                case 'm':
                    readMemory(i);
                    return false;
                case 'M':
                    // WRITE MEMORY
                    break;
                case 'p':
                    // READ SELECTED REGISTERS
                    break;
                case 'P':
                    // WRITE SELECTED REGISTERS
                    break;
                case 'q':
                    // QUERY A VARIABLE
                    break;
                case 's':
                    // STEP INSTRUCTION
                    int pc = simulator.getState().getPC();
                    printer.println("--INSERTING STEP PROBE @ "+StringUtil.addrToString(pc)+"--");
                    simulator.insertProbe(STEPPROBE, pc);
                    sendPlus();
                    return true;
                case 'z':
                    // REMOVE BREAKPOINT
                    setBreakPoint(i, false);
                    return false;
                case 'Z':
                    // SET BREAKPOINT
                    setBreakPoint(i, true);
                    return false;
                case '?':
                    // TODO: is it ok to always reply with S05?
                    sendPacketOK("S05");
                    return false;
            }

            // didn't understand the comand
            sendPacketOK("");
            return false;
        }

        private void sendPlus() throws IOException {
            output.write((byte)'+');
        }


        void commandError() throws IOException {
            output.write((byte)'-');
        }

        void setBreakPoint(CharacterIterator i, boolean on) throws IOException {
            char num = i.current();
            i.next();
            switch ( num ) {
                case '0':
                case '1':
                    if ( !StringUtil.peekAndEat(i, ',') ) break;
                    int addr = StringUtil.readHexValue(i, 4);
                    if ( !StringUtil.peekAndEat(i, ',') ) break;
                    int len = StringUtil.readHexValue(i, 4);
                    for ( int cntr = addr; cntr < addr+len; cntr += 2 )
                        setBreakPoint(addr, on);
                    sendPacketOK("OK");
                    return;
                case '2':
                    // TODO: other breakpoint types?
                case '3':
                    // TODO: other breakpoint types?
                default:
            }

            sendPacketOK("");
        }

        void setBreakPoint(int addr, boolean on) {
            if ( on )
                simulator.insertProbe(BREAKPROBE, addr);
            else
                simulator.removeProbe(BREAKPROBE, addr);
        }

        void readAllRegisters() throws IOException {
            StringBuffer buf = new StringBuffer(84);
            State s = simulator.getState();
            for ( int cntr = 0; cntr < 32; cntr++ ) {
                byte value = s.getRegisterByte(Register.getRegisterByNumber(cntr));
                buf.append(StringUtil.toHex(value & 0xff, 2));
            }
            buf.append(StringUtil.toHex(s.getSREG() & 0xff, 2));
            buf.append(StringUtil.toHex(s.getSP() & 0xff, 2));
            buf.append(StringUtil.toHex((s.getSP() >> 8) & 0xff, 2));
            int pc = s.getPC();
            buf.append(StringUtil.toHex(pc, 2));
            buf.append(StringUtil.toHex(pc >> 8, 2));
            buf.append(StringUtil.toHex(pc >> 16, 2));
            buf.append(StringUtil.toHex(pc >> 24, 2));
            sendPacketOK(buf.toString());
        }

        private static final int MEMMASK = 0xff0000;
        private static final int MEMBEGIN = 0x800000;

        void readMemory(CharacterIterator i) throws IOException {
            int addr = StringUtil.readHexValue(i, 8);
            int length = 1;
            if ( StringUtil.peekAndEat(i, ',') )
                length = StringUtil.readHexValue(i, 8);
            State s = simulator.getState();
            StringBuffer buf = new StringBuffer(length*2);

            if ( (addr & MEMMASK) == MEMBEGIN ) {
                // reading from SRAM
                addr = addr & (~MEMMASK);
                for ( int cntr = 0; cntr < length; cntr++ ) {
                    byte value = s.getDataByte(addr+cntr);
                    buf.append(StringUtil.toHex(value & 0xff, 2));
                }
            } else {
                // reading from program memory
                for ( int cntr = 0; cntr < length; cntr++ ) {
                    byte value = s.getProgramByte(addr+cntr);
                    buf.append(StringUtil.toHex(value & 0xff, 2));
                }
            }


            sendPacketOK(buf.toString());
        }

        void sendPacketOK(String s) throws IOException {
            sendPlus();
            sendPacket(s);
        }

        void sendPacket(String packet) throws IOException {
            byte[] bytes = packet.getBytes();

            int cksum = 0;
            for ( int cntr = 0; cntr < bytes.length; cksum += bytes[cntr++] ) ;

            String np = "$"+packet+"#"+StringUtil.toHex(cksum & 0xff, 2);
            printer.println("   <-- "+np+"");

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
                printer.println("--IN STARTUP PROBE @ "+StringUtil.addrToString(address)+"--");
                printer.println("GDBMonitor listening on port "+port+"...");
                try {
                    socket = serverSocket.accept();
                    input = socket.getInputStream();
                    output = socket.getOutputStream();
                    printer.println("Connected established with: "+socket.getInetAddress().getCanonicalHostName());
                    serverSocket.close();
                } catch ( IOException e ) {
                    throw Avrora.failure("Unexpected IOException: "+e);
                }

                monitorLoop(null);
            }

            public void fireAfter(Instr i, int address, State s) {
                // remove ourselves from the beginning of the program after it has started
                simulator.removeProbe(this, address);
            }
        }

        protected class BreakpointProbe implements Simulator.Probe {
            public void fireBefore(Instr i, int address, State s) {
                printer.println("--IN BREAKPOINT PROBE @ "+StringUtil.addrToString(address)+"--");
                monitorLoop("T05");
            }

            public void fireAfter(Instr i, int address, State s) {
                // do nothing
            }
        }

        protected class StepProbe implements Simulator.Probe {
            public void fireBefore(Instr i, int address, State s) {
                printer.println("--IN STEP PROBE @ "+StringUtil.addrToString(address)+"--");
            }

            public void fireAfter(Instr i, int address, State s) {
                printer.println("--AFTER STEP PROBE @ "+StringUtil.addrToString(address)+"--");
                monitorLoop("T05");
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
