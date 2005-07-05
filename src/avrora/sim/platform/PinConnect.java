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

import avrora.core.Instr;
import avrora.sim.Simulator;
import avrora.sim.SimulatorThread;
import avrora.sim.State;
import avrora.sim.radio.Radio;
import avrora.sim.clock.Synchronizer;
import avrora.sim.clock.StepSynchronizer;
import avrora.sim.mcu.Microcontroller;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Very simple implementation of pin interconnect between microcontrollers
 *
 * @author Jacob Everist
 */

public class PinConnect {

    public static final PinConnect pinConnect;

    static {
        pinConnect = new PinConnect();
    }

    private final PinEvent pinEvent;
    private final Synchronizer synchronizer;

    // List of all the pin relationships
    protected LinkedList pinNodes;
    protected LinkedList pinConnections;

    public PinConnect() {
        // period = 1
        pinNodes = new LinkedList();
        pinConnections = new LinkedList();
        pinEvent = new PinEvent();
        synchronizer = new StepSynchronizer(pinEvent);
    }

    public void addSeresNode(Microcontroller mcu, PinWire northTx, PinWire eastTx,
                             PinWire southTx, PinWire westTx, PinWire northRx, PinWire eastRx,
                             PinWire southRx, PinWire westRx, PinWire northInt, PinWire eastInt,
                             PinWire southInt, PinWire westInt) {

        pinNodes.add(new PinNode(mcu, northTx, eastTx, southTx, westTx, northRx, eastRx, southRx, westRx,
                northInt, eastInt, southInt, westInt));

    }

    public void addSimulatorThread(SimulatorThread simThread) {
        Simulator sim = simThread.getSimulator();
        Microcontroller currMCU = sim.getMicrocontroller();

        // iterator over PinNodes
        Iterator i = pinNodes.iterator();

        // go through the complete list of PinNodes
        while (i.hasNext()) {

            // get the next PinNode on the list
            PinConnect.PinNode p = (PinConnect.PinNode) i.next();

            // does this node have the equivalent Microcontroller?
            if (currMCU == p.mcu) {

                // register the simulator thread with the appropriate PinNode
                p.addSimulatorThread(simThread);

                // add simulator thread to PinClock and PinMeet
                synchronizer.addNode(simThread.getNode());
            }
        }
    }

    /**
     * Initialize the connections with a default topology of
     * a chain with connections on the north and south ports
     */
    public void initializeConnections() {

        // iterator over PinNodes
        Iterator i = pinNodes.iterator();

        if (!i.hasNext()) {
            return;
        }

        // the previous PinNode
        PinNode prevNode = (PinConnect.PinNode) i.next();

        // connect the nodes from North to South to create a long chain
        while (i.hasNext()) {
            // get next node on the list
            PinNode currNode = (PinConnect.PinNode) i.next();
            // two-way communication links between neighboring modules

            // output pin for northern module
            PinLink southTxToNorthRx = new PinLink(prevNode.southPinTx);

            // input pins for this connection
            southTxToNorthRx.addInputPin(currNode.northPinRx);
            southTxToNorthRx.addInputPin(currNode.northPinInt);

            // output pin for southern module
            PinLink northTxToSouthRx = new PinLink(currNode.northPinTx);

            // input pins for this connection
            northTxToSouthRx.addInputPin(prevNode.southPinRx);
            northTxToSouthRx.addInputPin(prevNode.southPinInt);

            // add connections to the list
            pinConnections.add(southTxToNorthRx);
            pinConnections.add(northTxToSouthRx);

            // set this node as previous node
            prevNode = currNode;
        }

    }

    /**
     * This class stores all the information for a single controller node
     * and its PinWires.
     *
     * @author Jacob Everist
     */
    protected class PinNode {

        // node microcontroller
        public Microcontroller mcu;

        // transmit pins
        public PinWire northPinTx;
        public PinWire eastPinTx;
        public PinWire southPinTx;
        public PinWire westPinTx;

        // receive pins
        public PinWire northPinRx;
        public PinWire eastPinRx;
        public PinWire southPinRx;
        public PinWire westPinRx;

        // interrupt pins
        public PinWire northPinInt;
        public PinWire eastPinInt;
        public PinWire southPinInt;
        public PinWire westPinInt;

        // simulator thread
        public SimulatorThread simThread;

        public PinNode(Microcontroller mcu, PinWire northTx, PinWire eastTx,
                       PinWire southTx, PinWire westTx, PinWire northRx, PinWire eastRx,
                       PinWire southRx, PinWire westRx, PinWire northInt, PinWire eastInt,
                       PinWire southInt, PinWire westInt) {

            this.mcu = mcu;
            northPinTx = northTx;
            eastPinTx = eastTx;
            southPinTx = southTx;
            westPinTx = westTx;
            northPinRx = northRx;
            eastPinRx = eastRx;
            southPinRx = southRx;
            westPinRx = westRx;
            northPinInt = northInt;
            eastPinInt = eastInt;
            southPinInt = southInt;
            westPinInt = westInt;
        }

        public void addSimulatorThread(SimulatorThread simThread) {
            this.simThread = simThread;
        }
    }

    /**
     * This class connects two PinNode devices together in two-way communication
     *
     * @author Jacob Everist
     */
    protected class PinLink {

        protected LinkedList pinWires;

        // must start PinLink with an output pin
        public PinLink(PinWire outputPin) {

            pinWires = new LinkedList();

            // make sure it is set as output
            outputPin.wireOutput.enableOutput();

            // add to list of pins on this connection
            pinWires.add(outputPin);

        }

        // add an input pin on this connection
        public void addInputPin(PinWire inputPin) {

            // make sure it is set as input
            inputPin.wireInput.enableInput();

            // add to list of pins on this connection
            pinWires.add(inputPin);

        }

        // transmit the signals on this connection
        public void propagateSignals() {

            // iterator over PinWires
            Iterator i = pinWires.iterator();

            PinWire currOutput = null;

            // go through the complete list of PinWires to find the output wire
            while (i.hasNext()) {

                PinWire curr = (PinWire) i.next();

                // if this wire accepts output
                if (curr.outputReady()) {

                    // check that we haven't already found an output wire
                    if (currOutput != null) {
                        String s = "ERROR: More than one output wire on this PinLink";
                        System.out.println(s);
                        return;
                    } else {
                        // set this pin as the output wire
                        currOutput = curr;
                    }

                }
            }

            // check if we have an output wire
            if (currOutput == null) {
                // there is no output wire, so do nothing
                return;
            }
            // if we have an output wire, propagate its signal
            else {

                // reset the iterator
                i = pinWires.iterator();

                // go through all wires
                while (i.hasNext()) {

                    PinWire curr = (PinWire) i.next();

                    // if this is not the output, propagate the signal
                    if (curr != currOutput) {
                        // write the value of output pin to the input pins
                        curr.wireOutput.write(currOutput.wireInput.read());
                    }
                }
            }
        }
    }

    protected class PinEvent implements Simulator.Event {
        public void fire() {
            // iterator over PinLinks
            Iterator i = pinConnections.iterator();

            while (i.hasNext()) {
                PinLink currLink = (PinConnect.PinLink) i.next();

                currLink.propagateSignals();
            }
        }
    }
}
