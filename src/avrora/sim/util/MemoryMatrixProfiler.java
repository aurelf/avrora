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

package avrora.sim.util;

import avrora.core.Instr;
import avrora.core.Program;
import avrora.sim.State;
import avrora.Avrora;
import avrora.util.StringUtil;

/**
 * The <code>MemoryMatrixProfiler</code> class collects information about
 * a program's usage of memory. For each instruction in the program, it
 * tracks the memory locations read and written by that instruction. For
 * example, a load instruction that uses an address in a register might
 * load bytes from various locations in the data memory. This class
 * maintains two internal two-dimensional matrices, one for read counts
 * and one for write counts. The matrices are indexed by code address
 * and data address.
 *
 * @author Ben L. Titzer
 */
public class MemoryMatrixProfiler {

    public final long rcount[][];
    public final long wcount[][];

    public final int ramSize;

    public MemoryMatrixProfiler(Program p, int size) {
        ramSize = size;
        ;
        rcount = new long[p.program_end][];
        wcount = new long[p.program_end][];
    }

    public void fireBeforeRead(Instr i, int address, State state, int data_addr, byte value) {
        if (data_addr < ramSize) {
            if (rcount[address] == null) rcount[address] = new long[ramSize];
            rcount[address][data_addr]++;
        }
    }

    public void fireBeforeWrite(Instr i, int address, State state, int data_addr, byte value) {
        if (data_addr < ramSize) {
            if (wcount[address] == null) wcount[address] = new long[ramSize];
            wcount[address][data_addr]++;
        }
    }

    public void fireAfterRead(Instr i, int address, State state, int data_addr, byte value) {
        // do nothing
    }

    public void fireAfterWrite(Instr i, int address, State state, int data_addr, byte value) {
        // do nothing
    }

    public long getReadCount(int address, int data_addr) {
        return getCount(rcount, data_addr, address);
    }

    public long getWriteCount(int address, int data_addr) {
        return getCount(wcount, data_addr, address);
    }

    private long getCount(long matrix[][], int data_addr, int address) {
        if (data_addr < ramSize)
            if (matrix[address] == null)
                return 0;
            else
                return matrix[address][data_addr];
        throw Avrora.failure("no count for data address " + StringUtil.addrToString(data_addr));
    }

}
