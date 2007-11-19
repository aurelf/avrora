/**
 * Created on 17.11.2004
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
import avrora.sim.radio.*;
import avrora.sim.util.SimUtil;
import avrora.sim.output.SimPrinter;
import avrora.syntax.Expr;
import cck.text.*;
import cck.util.Option;

import java.util.*;
import java.text.StringCharacterIterator;


/**
 * Packet monitor implementation. This class logs the number of packets, e.g. bytes sent and received.
 *
 * @author Olaf Landsiedel
 * @author Ben L. Titzer
 */
public class PacketMonitor extends MonitorFactory {

    protected Option.Bool BITS = newOption("show-bits", false,
            "This option enables the printing of packets as they are transmitted.");
    protected Option.Bool PACKETS = newOption("show-packets", true,
            "This option enables the printing of packet contents in bits rather than in bytes.");
    protected Option.Str START_SYMBOL = newOption("start-symbol", "",
            "When this option is not blank, the packet monitor will attempt to match the " +
            "start symbol of packet data in order to display both the preamble, start " +
            "symbol, and packet contents.");

    protected List monitors = new LinkedList();

    class Mon implements Monitor, Medium.Probe {
        LinkedList bytes;
        final Simulator simulator;
        final SimPrinter printer;
        final boolean showPackets;
        final boolean bits;

        int bytesTransmitted;
        int packetsTransmitted;
        int bytesReceived;
        int packetsReceived;
        int bytesCorrupted;
        boolean matchStart;
        byte startSymbol;
        long startCycle;

        Mon(Simulator s) {
            simulator = s;
            Platform platform = simulator.getMicrocontroller().getPlatform();
            Radio radio = (Radio)platform.getDevice("radio");
            radio.getTransmitter().insertProbe(this);
            radio.getReceiver().insertProbe(this);
            printer = SimUtil.getPrinter(simulator, "monitor.packet");
            printer.enabled = true;
            showPackets = PACKETS.get();
            bytes = new LinkedList();
            bits = BITS.get();

            getStartSymbol(radio);
            monitors.add(this);
        }

        private void getStartSymbol(Radio radio) {
            if (!START_SYMBOL.isBlank()) {
                matchStart = true;
                startSymbol = (byte) StringUtil.readHexValue(new StringCharacterIterator(START_SYMBOL.get()), 2);
            } else {
                if (radio instanceof CC1000Radio) {
                    matchStart = true;
                    startSymbol = (byte)0x33;
                }
                if (radio instanceof CC2420Radio) {
                    matchStart = true;
                    startSymbol = (byte)0xA7;
                }
            }
        }

        public void fireBeforeTransmit(Medium.Transmitter t, byte val) {
            if (bytes.size() == 0) startCycle = simulator.getClock().getCount();
            bytes.addLast(new Character((char)(0xff & val)));
            bytesTransmitted++;
        }

        public void fireBeforeTransmitEnd(Medium.Transmitter t) {
            packetsTransmitted++;
            if ( showPackets ) {
                StringBuffer buf = renderPacket("----> ");
                synchronized ( Terminal.class) {
                    Terminal.println(buf.toString());
                }
            }
            bytes = new LinkedList();
        }

        public void fireAfterReceive(Medium.Receiver r, char val) {
            if (bytes.size() == 0) startCycle = simulator.getClock().getCount();
            if (Medium.isCorruptedByte(val)) bytesCorrupted++;
            bytes.addLast(new Character(val));
            bytesReceived++;
        }

        public void fireAfterReceiveEnd(Medium.Receiver r) {
            packetsReceived++;
            if ( showPackets ) {
                StringBuffer buf = renderPacket("<==== ");
                synchronized ( Terminal.class) {
                    Terminal.println(buf.toString());
                }
            }
            bytes = new LinkedList();
        }

        private StringBuffer renderPacket(String prefix) {
            StringBuffer buf = new StringBuffer(3 * bytes.size() + 45);
            SimUtil.getIDTimeString(buf, simulator);
            Terminal.append(Terminal.COLOR_BRIGHT_CYAN, buf, prefix);
            Iterator i = bytes.iterator();
            int cntr = 0;
            boolean inPreamble = true;
            while ( i.hasNext() ) {
                cntr++;
                char t = ((Character)i.next()).charValue();
                inPreamble = renderByte(cntr, t, inPreamble, buf);
                if (i.hasNext()) buf.append('.');
            }
            appendTime(buf);
            return buf;
        }

        private void appendTime(StringBuffer buf) {
            long cycles = simulator.getClock().getCount() - startCycle;
            double ms = simulator.getClock().cyclesToMillis(cycles);
            buf.append("  ");
            buf.append(StringUtil.toFixedFloat((float)ms, 3));
            buf.append(" ms");
        }

        private boolean renderByte(int cntr, char value, boolean inPreamble, StringBuffer buf) {
            int color = Terminal.COLOR_DEFAULT;
            byte bval = (byte)value;
            if (!bits && Medium.isCorruptedByte(value)) {
                // this byte was corrupted during transmission.
                color = Terminal.COLOR_RED;
            } else if (matchStart && cntr > 1) {
                // should we match the start symbol?
                if (inPreamble) {
                    if (bval == startSymbol) {
                        color = Terminal.COLOR_YELLOW;
                        inPreamble = false;
                    }
                } else {
                    color = Terminal.COLOR_GREEN;
                }
            }
            renderByte(buf, color, value);
            return inPreamble;
        }

        private void renderByte(StringBuffer buf, int color, char value) {
            if (bits) {
                byte corrupted = Medium.getCorruptedBits(value);
                for (int i = 7; i >= 0; i--) {
                    boolean bit = (value >> i & 1) != 0;
                    if ( ((corrupted >> i) & 1) != 0 )
                        Terminal.append(Terminal.COLOR_RED, buf, bit ? "1" : "0");
                    else
                        Terminal.append(color, buf, bit ? "1" : "0");
                }
            } else {
                Terminal.append(color, buf, StringUtil.toHex((byte)value, 2));
            }
        }

        public void report() {
            if (monitors != null) {
                TermUtil.printSeparator(Terminal.MAXLINE, "Packet monitor results");
                Terminal.printGreen("Node     sent (b/p)          recv (b/p)    corrupted (b)");
                Terminal.nextln();
                TermUtil.printThinSeparator();
                Iterator i = monitors.iterator();
                while (i.hasNext()) {
                    Mon mon = (Mon)i.next();
                    Terminal.print(StringUtil.rightJustify(mon.simulator.getID(), 4));
                    Terminal.print(StringUtil.rightJustify(mon.bytesTransmitted, 10));
                    Terminal.print(" / ");
                    Terminal.print(StringUtil.leftJustify(mon.packetsTransmitted, 8));

                    Terminal.print(StringUtil.rightJustify(mon.bytesReceived, 10));
                    Terminal.print(" / ");
                    Terminal.print(StringUtil.leftJustify(mon.packetsReceived, 8));
                    Terminal.print(StringUtil.rightJustify(mon.bytesCorrupted, 10));
                    Terminal.nextln();
                }
                monitors = null;
                Terminal.nextln();
                
            }
        }

    }

    /**
     * create a new monitor
     */
    public PacketMonitor() {
        super("The \"packet\" monitor tracks packets sent and received by nodes in a sensor network.");
    }


    /**
     * create a new monitor, calls the constructor
     *
     * @see MonitorFactory#newMonitor(Simulator)
     */
    public Monitor newMonitor(Simulator s) {
        return new Mon(s);
    }
}

