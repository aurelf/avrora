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
 */

package jintgen.arch.avr;

import avrora.util.Util;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Ben L. Titzer
 */
public class AVRSymbol {

    public final String symbol;
    public final int value;

    AVRSymbol(String sym, int v) {
        symbol = sym;
        value = v;
    }

    public int getValue() {
        return value;
    }

    //===============================================================
    // Enum types: one class per enum type
    //===============================================================

    public static class GPR extends AVRSymbol {
        GPR(String sym, int v) { super(sym, v); }

        // Enum values: one field per enum value
        public static final GPR R0 = new GPR("r0", 0);
        public static final GPR R1 = new GPR("r1", 1);
        public static final GPR R2 = new GPR("r2", 2);
        public static final GPR R3 = new GPR("r3", 3);
        public static final GPR R4 = new GPR("r4", 4);
        public static final GPR R5 = new GPR("r5", 5);
        public static final GPR R6 = new GPR("r6", 6);
        public static final GPR R7 = new GPR("r7", 7);
        public static final GPR R8 = new GPR("r8", 8);
        public static final GPR R9 = new GPR("r9", 9);
        public static final GPR R10 = new GPR("r10", 10);
        public static final GPR R11 = new GPR("r11", 11);
        public static final GPR R12 = new GPR("r12", 12);
        public static final GPR R13 = new GPR("r13", 13);
        public static final GPR R14 = new GPR("r14", 14);
        public static final GPR R15 = new GPR("r15", 15);
        public static final GPR R16 = new GPR("r16", 16);
        public static final GPR R17 = new GPR("r17", 17);
        public static final GPR R18 = new GPR("r18", 18);
        public static final GPR R19 = new GPR("r19", 19);
        public static final GPR R20 = new GPR("r20", 20);
        public static final GPR R21 = new GPR("r21", 21);
        public static final GPR R22 = new GPR("r22", 22);
        public static final GPR R23 = new GPR("r23", 23);
        public static final GPR R24 = new GPR("r24", 24);
        public static final GPR R25 = new GPR("r25", 25);
        public static final GPR R26 = new GPR("r26", 26);
        public static final GPR R27 = new GPR("r27", 27);
        public static final GPR R28 = new GPR("r28", 28);
        public static final GPR R29 = new GPR("r29", 29);
        public static final GPR R30 = new GPR("r30", 30);
        public static final GPR R31 = new GPR("r31", 31);
    }

    public static class ADR extends AVRSymbol {
        ADR(String sym, int v) { super(sym, v); }

        public static final ADR X = new ADR("X", 26);
        public static final ADR Y = new ADR("Y", 28);
        public static final ADR Z = new ADR("Z", 30);
    }

}
