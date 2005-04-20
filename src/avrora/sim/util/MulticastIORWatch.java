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

import avrora.core.Instr;
import avrora.sim.Simulator;
import avrora.sim.State;

/**
 * The <code>MulticastIORWatch</code> is a wrapper around multiple watches that allows them to act as a single
 * watch. It is useful for composing multiple watches into one and is used internally in the simulator.
 *
 * @author Ben L. Titzer
 * @see avrora.sim.Simulator
 */
public class MulticastIORWatch extends TransactionalList implements Simulator.IORWatch {

    /**
     * The <code>fireBeforeRead()</code> method is called before the probed address is read by the program. In
     * the implementation of the multicast probe, it simply calls the <code>fireBeforeRead()</code> method on
     * each of the probes in the multicast set in the order in which they were inserted.
     *
     * @param state   the state of the simulation
     */
    public void fireBeforeRead(State state, int data_addr) {
        beginTransaction();
        for (Link pos = head; pos != null; pos = pos.next)
            ((Simulator.IORWatch)pos.object).fireBeforeRead(state, data_addr);
    }

    /**
     * The <code>fireAfterRead()</code> method is called after the probed address is read by the program. In
     * the implementation of the multicast probe, it simply calls the <code>fireAfterRead()</code> method on
     * each of the probes in the multicast set in the order in which they were inserted.
     *
     * @param state   the state of the simulation
     * @param val     the value of the memory location being read
     */
    public void fireAfterRead(State state, int data_addr, byte val) {
        for (Link pos = head; pos != null; pos = pos.next)
            ((Simulator.IORWatch)pos.object).fireAfterRead(state, data_addr, val);
        endTransaction();
    }

    /**
     * The <code>fireBeforeWrite()</code> method is called before the probed address is written by the
     * program. In the implementation of the multicast probe, it simply calls the
     * <code>fireBeforeWrite()</code> method on each of the probes in the multicast set in the order in which
     * they were inserted.
     *
     * @param state   the state of the simulation
     * @param val     the value being written to the memory location
     */
    public void fireBeforeWrite(State state, int data_addr, byte val) {
        beginTransaction();
        for (Link pos = head; pos != null; pos = pos.next)
            ((Simulator.IORWatch)pos.object).fireBeforeWrite(state, data_addr, val);
    }

    /**
     * The <code>fireAfterWrite()</code> method is called after the probed address is written by the program.
     * In the implementation of the multicast probe, it simply calls the <code>fireAfterWrite()</code> method
     * on each of the probes in the multicast set in the order in which they were inserted.
     *
     * @param state   the state of the simulation
     * @param val     the value being written to the memory location
     */
    public void fireAfterWrite(State state, int data_addr, byte val) {
        for (Link pos = head; pos != null; pos = pos.next)
            ((Simulator.IORWatch)pos.object).fireAfterWrite(state, data_addr, val);
        endTransaction();
    }

    /**
     * The <code>fireBeforeBitRead()</code> method is called before the data address is read by the program.
     * In the implementation of the Empty watch, this method does nothing.
     *
     * @param state     the state of the simulation
     * @param ioreg_num the number of the IO register being read
     */
    public void fireBeforeBitRead(State state, int ioreg_num, int bit) {
        beginTransaction();
        for (Link pos = head; pos != null; pos = pos.next)
            ((Simulator.IORWatch)pos.object).fireBeforeBitRead(state, ioreg_num, bit);
    }

    /**
     * The <code>fireBeforeBitWrite()</code> method is called before the data address is written by the
     * program.
     * In the implementation of the Empty watch, this method does nothing.
     *
     * @param state     the state of the simulation
     * @param ioreg_num the number of the IO register being read
     * @param value     the value being written to the memory location
     */
    public void fireBeforeBitWrite(State state, int ioreg_num, int bit, boolean value) {
        beginTransaction();
        for (Link pos = head; pos != null; pos = pos.next)
            ((Simulator.IORWatch)pos.object).fireBeforeBitWrite(state, ioreg_num, bit, value);
    }

    /**
     * The <code>fireAfterBitRead()</code> method is called after the data address is read by the program.
     * In the implementation of the Empty watch, this method does nothing.
     *
     * @param state     the state of the simulation
     * @param ioreg_num the number of the IO register being read
     * @param value     the value of the memory location being read
     */
    public void fireAfterBitRead(State state, int ioreg_num, int bit, boolean value) {
        for (Link pos = head; pos != null; pos = pos.next)
            ((Simulator.IORWatch)pos.object).fireAfterBitRead(state, ioreg_num, bit, value);
        endTransaction();
    }

    /**
     * The <code>fireAfterBitWrite()</code> method is called after the data address is written by the
     * program.
     * In the implementation of the Empty watch, this method does nothing.
     *
     * @param state     the state of the simulation
     * @param ioreg_num the number of the IO register being read
     * @param value     the value being written to the memory location
     */
    public void fireAfterBitWrite(State state, int ioreg_num, int bit, boolean value) {
        for (Link pos = head; pos != null; pos = pos.next)
            ((Simulator.IORWatch)pos.object).fireAfterBitWrite(state, ioreg_num, bit, value);
        endTransaction();
    }

}
