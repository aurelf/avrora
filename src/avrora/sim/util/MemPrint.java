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

package avrora.sim.util;

import avrora.sim.*;
import cck.text.StringUtil;
import cck.text.Terminal;


/**
 * <code>MemPrint</code> is a simple utility that allows the simulated
 * program to send output to the screen.
 *
 * @author John Regehr
 */
public class MemPrint extends Simulator.Watch.Empty {

    int base;
    int max;

    public MemPrint(int b, int m) {
        base = b;
        max = m;
    }

    public void fireBeforeWrite(State state, int data_addr, byte value) {

        if (data_addr != base) {
            Terminal.printRed("Unexpected interception by printer!");
        }

        Simulator sim = state.getSimulator();
        AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();

        String idstr = SimUtil.getIDTimeString(sim);
        Terminal.printRed(idstr + " ");

        int pid = a.getDataByte(base + 1);
        Terminal.printRed("[" + pid + "] ");

        switch (value) {
            case 0x1:
            case 0x3:
                int l = a.getDataByte(base + 2);
                int h = a.getDataByte(base + 3);
                int v = ((h & 0xff) << 8) + (l & 0xff);
                if (value == 0x1) {
                    Terminal.printRed("hex: " + StringUtil.toHex(v, 4));
                }
                if (value == 0x3) {
                    Terminal.printRed("int: " + v);
                }
                Terminal.nextln();
                break;
            case 0x2:
                Terminal.printRed("str: ");
                for (int i = 0; i <= max; i++) {
                    byte b = a.getDataByte(base + 2 + i);
                    if (b == 0) break;
                    String s = String.valueOf((char) b);
                    Terminal.printRed(s);
                }
                Terminal.nextln();
                break;
            default:
                Terminal.printRed("Unexpected command to printer!");
        }
    }
}
