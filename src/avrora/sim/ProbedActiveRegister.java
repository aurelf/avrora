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

package avrora.sim;

import avrora.sim.util.MulticastIORWatch;

/**
 * The <code>ProbedActiveRegister</code> class implements a register that has
 * probes attached to it.
 *
 * @author Ben L. Titzer
 */
public class ProbedActiveRegister implements ActiveRegister {
    protected final int ioreg_num;
    protected final ActiveRegister ioreg;
    protected final MulticastIORWatch watches;
    private BaseInterpreter interpreter;

    protected ProbedActiveRegister(BaseInterpreter interpreter, ActiveRegister ar, int inum) {
        this.interpreter = interpreter;
        ioreg_num = inum;
        ioreg = ar;
        watches = new MulticastIORWatch();
    }

    void add(Simulator.IORWatch w) {
        watches.add(w);
    }

    void remove(Simulator.IORWatch p) {
        watches.remove(p);
    }

    boolean isEmpty() {
        return watches.isEmpty();
    }

    public void write(byte value) {
        watches.fireBeforeWrite(interpreter.state, ioreg_num, value);
        ioreg.write(value);
        watches.fireAfterWrite(interpreter.state, ioreg_num, value);
    }

    public void writeBit(int bit, boolean val) {
        watches.fireBeforeBitWrite(interpreter.state, ioreg_num, bit, val);
        ioreg.writeBit(bit, val);
        watches.fireAfterBitWrite(interpreter.state, ioreg_num, bit, val);
    }

    public byte read() {
        watches.fireBeforeRead(interpreter.state, ioreg_num);
        byte value = ioreg.read();
        watches.fireAfterRead(interpreter.state, ioreg_num, value);
        return value;
    }

    public boolean readBit(int bit) {
        watches.fireBeforeBitRead(interpreter.state, ioreg_num, bit);
        boolean value = ioreg.readBit(bit);
        watches.fireAfterBitRead(interpreter.state, ioreg_num, bit, value);
        return value;
    }
}
