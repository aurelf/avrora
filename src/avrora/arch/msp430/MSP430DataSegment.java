/**
 * Copyright (c) 2005, Regents of the University of California
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
 *
 * Creation date: Nov 15, 2005
 */

package avrora.arch.msp430;

import avrora.sim.ActiveRegister;
import avrora.sim.Segment;
import avrora.arch.avr.AVRState;

/**
 * The <code>MSP430DataSegment</code> class represents a data segment on the MSP430
 * microcontroller, which consists of IO registers, RAM, and flash (which contains
 * both initialized data and executable instructions).
 *
 * @author Ben L. Titzer
 */
public class MSP430DataSegment extends Segment {
    private final int ioreg_end;
    private final int sram_end;
    private final int flash_start;
    private final ActiveRegister[] ioregs;
    private final MSP430Instr[] code;
    public static final int _1kb = 1024;
    public static final int DATA_SIZE = 64 * _1kb;

    public MSP430DataSegment(int se, int fs, ActiveRegister[] ior, MSP430State st) {
        super("data", DATA_SIZE, (byte)0, st, null);
        sram_end = se;
        flash_start = fs;
        ioregs = ior;
        ioreg_end = ior.length;
        code = new MSP430Instr[DATA_SIZE];
    }

    /**
     * The <code>direct_read()</code> method accesses the actual values stored in the segment, after
     * watches and instrumentation have been applied. It is intended to be used ONLY internally to the
     * segment. It is protected to allow architecture-specific implementations such as sparse memories
     * or memory mapped IO. In the implementation of the AVR, this method checks to see whether the
     * address lies in the register file, in the active register space, or in the rest of RAM.
     * @param address the address in the segment to read
     * @return the value of the of segment at the specified address
     */
    protected byte direct_read(int address) {
        if (address < ioreg_end)
            return ioregs[address].read();
        if (address < sram_end)
            return segment_data[address];
        if (address < flash_start)
            return errorReporter.readError(address);
        return segment_data[address];
    }

    /**
     * The <code>direct_write()</code> method writes the value directly into the array. This
     * method is protected and is ONLY used internally. It is protected to allow architecture-
     * specific implementations such as segments that are not contiguous, contain active register
     * ranges, etc. In the implementation of the AVR, this method checks to see whether the
     * address lies in the register file, in the active register space, or in the rest of RAM.
     * @param address the address to write
     * @param val the value to write into the segment
     */
    protected void direct_write(int address, byte val) {
        if (address < ioreg_end)
            ioregs[address].write(val);
        else if (address < sram_end)
            segment_data[address] = val;
        else // attempt to write beyond RAM
            errorReporter.writeError(address, val);
    }

    protected MSP430Instr[] shareInstr() {
        return code;
    }
}
