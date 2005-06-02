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

package avrora.stack.isea;

import avrora.core.Register;
import avrora.Avrora;
import avrora.util.Terminal;
import avrora.util.StringUtil;

/**
 * @author Ben L. Titzer
 */
public class ISEState {

    public static final int NUM_REGISTERS = 32;
    public static final int SREG_NUM = 63;

    protected final byte[] registers;
    protected byte SREG;

    public ISEState() {
        registers = new byte[NUM_REGISTERS];
        for ( int cntr = 0; cntr < NUM_REGISTERS; cntr++ )
            registers[cntr] = (byte)(ISEValue.R0 + cntr);
        SREG = ISEValue.SREG;
    }

    protected ISEState(ISEState s) {
        registers = new byte[NUM_REGISTERS];
        for ( int cntr = 0; cntr < NUM_REGISTERS; cntr++ )
            registers[cntr] = s.registers[cntr];
        SREG = s.SREG;
    }

    public byte readRegister(Register r) {
        return registers[r.getNumber()];
    }

    public void writeRegister(Register r, byte val) {
        registers[r.getNumber()] = val;
    }

    public byte readIORegister(int reg) {
        switch ( reg ) {
            case SREG_NUM: return SREG;
        }
        return ISEValue.UNKNOWN;
    }

    public void writeIORegister(int reg, byte val) {
        switch ( reg ) {
            case SREG_NUM:
                SREG = val;
                break;
        }
    }

    public byte getSREG() {
        return SREG;
    }

    public void writeSREG(byte val) {
        SREG = val;
    }

    public void merge(ISEState s) {
        // merge all registers
        for ( int cntr = 0; cntr < NUM_REGISTERS; cntr++ ) {
            registers[cntr] = ISEValue.merge(registers[cntr], s.registers[cntr]);
        }
        // merge the status register
        SREG = ISEValue.merge(SREG, s.SREG);
    }

    public boolean equals(Object o) {
        if ( !(o instanceof ISEState) ) return false;
        ISEState s = (ISEState)o;
        // check each register
        for ( int cntr = 0; cntr < NUM_REGISTERS; cntr++ ) {
            if ( registers[cntr] != s.registers[cntr] ) return false;
        }
        // check status register
        if ( SREG != s.SREG ) return false;

        return true;
    }

    public ISEState copy() {
        return new ISEState(this);
    }

    public void push(byte val) {
        // TODO: implement a stack in the state
    }

    public byte pop() {
        return ISEValue.UNKNOWN;
    }

    public void print(int pc) {
        Terminal.print(StringUtil.to0xHex(pc, 4)+": ");
        for ( int cntr = 0; cntr < NUM_REGISTERS; cntr++ ) {
            String str = ISEValue.toString(registers[cntr]);
            Terminal.print(StringUtil.rightJustify(str, 4));
            if ( cntr == 15 ) Terminal.print("\n        ");
        }
        Terminal.nextln();
        String str = ISEValue.toString(SREG);
        Terminal.print("        "+str);
        Terminal.nextln();
    }
}
