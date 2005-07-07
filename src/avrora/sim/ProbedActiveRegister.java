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
 * probes attached to it. It behaves like a normal <code>ActiveRegister</code>,
 * except that it will notify its watches before each read and write.
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

    /**
     * The <code>add()</code> method adds another watch to the list of watches for this active register.
     * @param w the watch to add to this active register
     */
    void add(Simulator.IORWatch w) {
        watches.add(w);
    }

    /**
     * The <code>remove()</code> method removes a watch from the list of watches for this active register.
     * @param w the watch to remove from this active register
     */
    void remove(Simulator.IORWatch w) {
        watches.remove(w);
    }

    /**
     * The <code>isEmpty()</code> method checks whether there are any watches for this active register.
     * @return true if there is at least one watch on this active register; false otherwise
     */
    boolean isEmpty() {
        return watches.isEmpty();
    }

    /**
     * The <code>write()</code> method writes an 8-bit value to the IO register as a byte. For special IO
     * registers, this may cause some action like device activity, masking/unmasking of interrupts, etc.
     * In the implementation of the <code>ProbedActiveRegister</code> class,
     * this method will notify the watch(es) for this register before and after the write.
     *
     * @param value the value to write
     */
    public void write(byte value) {
        watches.fireBeforeWrite(interpreter.state, ioreg_num, value);
        ioreg.write(value);
        watches.fireAfterWrite(interpreter.state, ioreg_num, value);
    }

    /**
     * The <code>writeBit()</code> method writes a single bit value into the IO register at the specified
     * bit offset. In the implementation of the <code>ProbedActiveRegister</code> class,
     * this method will notify the watch(es) for this register before and after the write.
     *
     * @param bit the number of the bit to write
     * @param val the value of the bit to write
     */
    public void writeBit(int bit, boolean val) {
        watches.fireBeforeBitWrite(interpreter.state, ioreg_num, bit, val);
        ioreg.writeBit(bit, val);
        watches.fireAfterBitWrite(interpreter.state, ioreg_num, bit, val);
    }

    /**
     * The <code>read()</code> method reads the 8-bit value of the IO register as a byte. For special IO
     * registers, this may cause some action like device activity, or the actual value of the register may
     * need to be fetched or computed. In the implementation of the <code>ProbedActiveRegister</code> class,
     * this method will notify the watch(es) for this register before and after the read.
     *
     * @return the value of the register as a byte
     */
    public byte read() {
        watches.fireBeforeRead(interpreter.state, ioreg_num);
        byte value = ioreg.read();
        watches.fireAfterRead(interpreter.state, ioreg_num, value);
        return value;
    }

    /**
     * The <code>readBit()</code> method reads a single bit from the IO register.
     * In the implementation of the <code>ProbedActiveRegister</code> class,
     * this method will notify the watch(es) for this register before and after the read.
     *
     * @param bit the number of the bit to read
     * @return the value of the bit as a boolean
     */
    public boolean readBit(int bit) {
        watches.fireBeforeBitRead(interpreter.state, ioreg_num, bit);
        boolean value = ioreg.readBit(bit);
        watches.fireAfterBitRead(interpreter.state, ioreg_num, bit, value);
        return value;
    }
}
