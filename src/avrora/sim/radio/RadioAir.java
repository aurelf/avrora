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

package avrora.sim.radio;

import avrora.sim.clock.Synchronizer;


/**
 * Interface for the <code>RadioAir</code>. An implementation of this interface should provide the policies
 * through which radio transmission is handled. Radios should transmit via the transmit method. The air should
 * deliver packets to the radio through the receive() method in the <code>Radio</code> interface.
 *
 * @author Daniel Lee
 * @author Ben L. Titzer
 */
public interface RadioAir {

    /**
     * The <code>addRadio()</code> method adds a new radio to this radio model.
     * @param r the radio to add to this air implementation
     */
    public void addRadio(Radio r);

    /**
     * The <code>removeRadio()</code> method removes a radio from this radio model.
     * @param r the radio to remove from this air implementation
     */
    public void removeRadio(Radio r);

    /**
     * The <code>transmit()</code> method is called by a radio when it begins to transmit
     * a packet over the air. The radio packet should be delivered to those radios in
     * range which are listening, according to the radio model.
     * @param r the radio transmitting this packet
     * @param f the radio packet transmitted into the air
     */
    public void transmit(Radio r, Radio.Transmission f);

    /**
     * The <code>sampleRSSI()</code> method is called by a radio when it wants to
     * sample the RSSI value of the air around it at the current time. The air may
     * need to block (i.e. wait for neighbors) because this thread may be ahead
     * of other threads in global time. The underlying air implementation should use
     * a <code>Synchronizer</code> for this purpose.
     * @param r the radio sampling the RSSI value
     * @return an integer value representing the received signal strength indicator
     */
    public int sampleRSSI(Radio r);

    /**
     * The <code>readChannel()</code> method reads the value of the channel at the current
     * time so that the last 8 bits transmitted (where the bits are 0 if there are no
     * transmissions) are returned.
     * @param r the radio sampling the channel
     * @return the last 8 bits transmitted in the channel
     */
    public byte readChannel(Radio r);

    /**
     * The <code>getSynchronizer()</code> method gets the synchronizer for this air
     * implementation.
     * @return a reference to the synchronizer for this radio model.
     */
    public Synchronizer getSynchronizer();
}
