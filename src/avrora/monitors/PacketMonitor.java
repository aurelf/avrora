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
import avrora.sim.radio.Radio;
import avrora.sim.util.SimUtil;
import cck.text.*;
import cck.util.Option;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Packet monitor implementation. This class logs the number of packets, e.g. bytes sent and received.
 *
 * @author Olaf Landsiedel
 */
public class PacketMonitor extends MonitorFactory {

    public static final int INTER_PACKET_TIME = 2 * Radio.TRANSFER_TIME;

    protected Option.Bool PACKETS = options.newOption("show-packets", true,
            "This option enables the printing of packets as they are transmitted.");
    protected Option.Bool PREAMBLE = options.newOption("show-preamble", false,
            "This option will show the preamble of a packet when it is printed out.");
    protected Option.Bool DISCARD = options.newOption("discard-first-byte", true,
            "This option will discard the first byte of a packet, since it is often jibberish.");

    class Mon extends Radio.RadioProbe.Empty implements Monitor {
        LinkedList bytes;
        final Simulator simulator;
        final Platform platform;
        int bytesTransmitted;
        int packetsTransmitted;
        PacketEndEvent packetEnd;
        SimUtil.SimPrinter printer;
        boolean showPackets;
        boolean discardFirst;
        boolean showPreamble;

        Mon(Simulator s) {
            simulator = s;
            platform = simulator.getMicrocontroller().getPlatform();
            Radio radio = (Radio)platform.getDevice("radio");
            radio.insertProbe(this);
            packetEnd = new PacketEndEvent();
            printer = SimUtil.getPrinter(simulator, "monitor.packet");
            printer.enabled = true;
            showPackets = PACKETS.get();
            discardFirst = DISCARD.get();
            showPreamble = PREAMBLE.get();
            bytes = new LinkedList();
        }

        public void fireAtTransmit(Radio r, Radio.Transmission t) {
            simulator.removeEvent(packetEnd);
            simulator.insertEvent(packetEnd, INTER_PACKET_TIME);
            bytes.addLast(t);
            bytesTransmitted++;
        }

        void endPacket() {
            packetsTransmitted++;
            if ( showPackets ) {
                StringBuffer buf = buildPacket();
                synchronized ( Terminal.class) {
                    Terminal.println(buf.toString());
                }
            }
            bytes = new LinkedList();
        }

        private StringBuffer buildPacket() {
            StringBuffer buf = new StringBuffer(2 * bytes.size() + 45);
            SimUtil.getIDTimeString(buf, simulator);
            Terminal.append(Terminal.COLOR_BRIGHT_CYAN, buf, "Packet sent");
            buf.append(": ");
            Iterator i = bytes.iterator();
            int cntr = 0;
            boolean inPreamble = true;
            while ( i.hasNext() ) {
                cntr++;
                Radio.Transmission t = (Radio.Transmission)i.next();
                if ( cntr == 1 && discardFirst ) continue;
                if ( inPreamble && !showPreamble && t.data == (byte)0xAA ) continue;
                inPreamble = false;
                StringUtil.toHex(buf, t.data, 2);
                buf.append(":");
            }
            return buf;
        }

        public void report() {
            TermUtil.reportQuantity("Bytes sent", bytesTransmitted, "");
            TermUtil.reportQuantity("Packets sent", packetsTransmitted, "");
        }

        class PacketEndEvent implements Simulator.Event {
            public void fire() {
                endPacket();
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
     * @see avrora.monitors.MonitorFactory#newMonitor(avrora.sim.Simulator)
     */
    public avrora.monitors.Monitor newMonitor(Simulator s) {
        return new Mon(s);
    }
}

