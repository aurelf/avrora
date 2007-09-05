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

import cck.util.Arithmetic;

/**
 * The <code>Channel</code> class implements a serial channel that represents a communication
 * channel where bits are sent one by one. The channel allows bits to be written into the channel
 * at a particular time and represents their serial transmission over time by an array.
 *
 * <p>
 * The channel is used in simulating radio transmissions; all transmissions write into the channel,
 * and all samples read from the channel.
 *
 * 
 * @author Ben L. Titzer
 */
public class Channel {

    protected final int bits;
    protected final long period;
    protected final long bitPeriod;
    protected long globalTime;
    protected final boolean invert;

    protected final boolean[] channelValues;
    protected final boolean[] channelWritten;

    public Channel(int bits, long period, boolean invert) {
        this.bits = bits;
        this.period = period;
        this.bitPeriod = period / bits;
        this.invert = invert;

        channelValues = new boolean[bits * 3];
        channelWritten = new boolean[bits * 3];
    }

    /**
     * The <code>write()</code> method writes a value into the channel, with the given bit length
     * at the given global time.
     * @param value the value to write into the channel
     * @param bits the number of bits to write
     * @param time the global time at which the write takes place
     */
    public void write(int value, int bits, long time) {
        int off = channelOffset(time);
        if ( invert ) value = ~value;
        for ( int cntr = 0; cntr < bits; cntr++ ) {
            int ind = cntr+off;
            channelValues[ind] |= Arithmetic.getBit(value, (bits-1)-cntr);
            channelWritten[ind] = true;
        }
    }

    /**
     * The <code>advance()</code> method advances the channel to the next period.
     */
    public void advance() {
        globalTime += period;

        // copy the values written into the channel
        for ( int cntr = 0; cntr < bits * 2; cntr++ ) {
            channelValues[cntr] = channelValues[cntr+bits];
            channelWritten[cntr] = channelWritten[cntr+bits];
        }
        // erase the values written for next interval
        for ( int cntr = bits * 2; cntr < bits * 3; cntr++ ) {
            channelValues[cntr] = false;
            channelWritten[cntr] = false;
        }
    }

    /**
     * The <code>read()</code> method reads the value of the channel at the current time, going
     * back by the number of bits.
     * @param time the global time at which to read the channel
     * @param bits the number of bits to read from the channel
     * @return a value representing the channel contents at this global time
     */
    public int read(long time, int bits) {
        int value = 0;
        int off = channelOffset(time) - bits;

        for ( int cntr = 0; cntr < bits; cntr++ ) {
            value = Arithmetic.setBit(value << 1, 0, channelValues[off+cntr]);
        }
        return value;
    }

    /**
     * The <code>occupied()</code> method tests whether this channel has been written to
     * in the window of time specified.
     * @param start the start of the interval to check
     * @param end the end of the interval to check
     * @return true if the channel was written to during the specified time interval; false otherwise
     */
    public boolean occupied(long start, long end) {
        int off = channelOffset(end);
        int diff = (int)((end - start + bitPeriod - 1) / bitPeriod);
        for ( int cntr = off - diff - 1; cntr < off; cntr++) {
            if ( channelWritten[cntr] ) return true;
        }
        return false;
    }

    protected int channelOffset(long gtime) {
        long diff = gtime - globalTime;
        return (int)(diff / period + bits);
    }
}
