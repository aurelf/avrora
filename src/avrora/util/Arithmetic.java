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

package avrora.util;

/**
 * The <code>Arithmetic</code> class implements a set of useful methods that are used by the simulator and
 * assembler for converting java types to various data types used by the machine.
 *
 * @author Ben L. Titzer
 */
public class Arithmetic {
    public static short word(byte b1, byte b2) {
        return (short)((b1 & 0xff) | (b2 << 8));
    }

    public static char uword(byte b1, byte b2) {
        return (char)((b1 & 0xff) | ((b2 & 0xff) << 8));
    }

    public static char ubyte(byte b1) {
        return (char)(b1 & 0xff);
    }

    public static byte low(short val) {
        return (byte)val;
    }

    public static byte high(short val) {
        return (byte)(val >> 8);
    }

    public static byte low(int val) {
        return (byte)val;
    }

    public static byte high(int val) {
        return (byte)((val & 0xff00) >> 8);
    }

    public static char ulow(char val) {
        return (char)(val & 0xff);
    }

    public static char uhigh(char val) {
        return (char)(val >> 8);
    }

    public static char ulow(short val) {
        return (char)(val & 0xff);
    }

    public static char uhigh(short val) {
        return (char)((val & 0xff00) >> 8);
    }

    public static boolean getBit(byte val, int bit) {
        return (val & (1 << bit)) != 0;
    }

    public static boolean getBit(int val, int bit) {
        return (val & (1 << bit)) != 0;
    }

    public static byte setBit(byte val, int bit) {
        return (byte)(val | (1 << bit));
    }

    public static byte setBit(byte val, int bit, boolean on) {
        if (on)
            return setBit(val, bit);
        else
            return clearBit(val, bit);
    }

    public static int setBit(int val, int bit, boolean on) {
        return on ? (val | (1 << bit)) : (val & ~(1 << bit));
    }

    public static byte clearBit(byte val, int bit) {
        return (byte)(val & ~(1 << bit));
    }

    public static int lowestBit(long value) {
        int low = 0;

        if ((value & 0xFFFFFFFF) == 0) {
            low += 32;
            value = value >> 32;
        }
        if ((value & 0xFFFF) == 0) {
            low += 16;
            value = value >> 16;
        }
        if ((value & 0xFF) == 0) {
            low += 8;
            value = value >> 8;
        }
        if ((value & 0xF) == 0) {
            low += 4;
            value = value >> 4;
        }
        if ((value & 0x3) == 0) {
            low += 2;
            value = value >> 2;
        }
        if ((value & 0x1) == 0) {
            low += 1;
            value = value >> 1;
        }

        return (value == 0) ? -1 : low;
    }

    // bit patterns for reversing the order of bits of a 4 bit quantity.
    private static final int reverseKey[] = {
        0, 4, 8, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15
    };

    public static byte reverseBits(byte value) {
        return (byte)(reverseKey[value & 0x0f] << 4 | reverseKey[(value >> 4) & 0x0f]);
    }

    // key for the number of bits set to one in a 4 bit quantity
    private static final int bitcountKey[] = {
        0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4
    };

    public static int bitCount(byte value) {
        return bitcountKey[value & 0x0f] + bitcountKey[(value >> 4) & 0x0f];
    }
}
