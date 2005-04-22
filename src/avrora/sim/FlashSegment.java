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

import avrora.core.*;
import avrora.Avrora;
import avrora.sim.util.MulticastProbe;

/**
 * @author Ben L. Titzer
 */
public abstract class FlashSegment extends Segment {

    public static final byte DEFAULT_VALUE = (byte)0xff;

    protected Instr[] segment_instr;

    public interface FlashSharer {
        public void update(Instr[] segment);
    }

    public FlashSegment(String name, int size, BaseInterpreter bi, ErrorReporter er) {
        super(name, size, DEFAULT_VALUE, bi, er);
        segment_instr = new Instr[size];
    }

    public abstract void load(Program p);

    /**
     * The <code>readInstr()</code> method reads an Instr from the specified address in
     * the flash. For misaligned accesses and accesses to places in the flash where there
     * are no valid instructions, this method returns <code>null</code>. This method does
     * not return <code>ProbedInstr</code> or <code>DisassembleInstr</code> objects which
     * are used internally to accomplish probing and dynamically updated code.
     *
     * @param address the address in the flash from which to read the instruction
     * @return a reference to the <code>Instr</code> object at this address in the flash;
     * <code>null</code> if there is no instruction at this address.
     */
    public Instr readInstr(int address) {
        try {
            return segment_instr[address].asInstr();
        } catch ( ArrayIndexOutOfBoundsException e) {
            throw new AddressOutOfBoundsException(address);
        }
    }

    /**
     * The <code>getInstr()</code> method reads an Instr from the specified address in
     * the flash. For misaligned accesses and accesses to places in the flash where there
     * are no valid instructions, this method may return instances of <code>MisalignedInstr</code>
     * or <code>DisassembleInstr</code> objects which are used internally to check for errors
     * and support dynamically updated code. Additionally, this method may return instances of
     * <code>ProbedInstr</code> that are used internally to support probing of instructions.
     *
     * @param address the address in the flash from which to read the instruction
     * @return a reference to the <code>Instr</code> object at this address in the flash;
     * <code>null</code> if there is no instruction at this address.
     */
    public Instr getInstr(int address) {
        try {
            return segment_instr[address];
        } catch ( ArrayIndexOutOfBoundsException e) {
            throw new AddressOutOfBoundsException(address);
        }
    }

    /**
     * The <code>insertProbe()</code> method inserts a probe on an instruction at the
     * specified address. No equality testing is done; i.e. inserting the same probe
     * on the same address more than once will result in it being triggered more than
     * once when the instruction executes.
     * @param address the address of the instruction on which to insert the probe
     * @param p the probe to insert on this instruction
     */
    public void insertProbe(int address, Simulator.Probe p) {
        throw Avrora.unimplemented();
    }

    /**
     * The <code>removeProbe()</code> method removes a probe from a particular instruction
     * in the program. The probe will no longer be triggered for subsequent executions of
     * this instruction. Reference equality is used for testing whether the probe should be
     * removed; i.e. the <code>Object.equals()</code> method is not consulted.
     * @param address the address of the instructiobn on which to insert the probe
     * @param p the probe to isnert on this instruction
     */
    public void removeProbe(int address, Simulator.Probe p) {
        throw Avrora.unimplemented();
    }

    protected void writeInstr(int address, Instr i) {
        segment_instr[address] = i;
    }

    /**
     * The ProbedInstr class represents a wrapper around an instruction in the program that executes the
     * probes before executing the instruction and after the instruction. For most methods on the
     * <code>Instr</code> class, it simply forwards the call to the original instruction.
     */
    protected class ProbedInstr extends Instr {
        protected final int address;
        protected final Instr instr;
        protected final MulticastProbe probe;

        protected ProbedInstr(Instr i, int a) {
            super(new InstrProperties(i.properties.name, i.properties.variant, i.properties.size, 0));
            instr = i;
            address = a;
            probe = new MulticastProbe();
        }

        void add(Simulator.Probe p) {
            probe.add(p);
        }

        void remove(Simulator.Probe p) {
            probe.remove(p);
        }

        boolean isEmpty() {
            return probe.isEmpty();
        }

        public void accept(InstrVisitor v) {
            probe.fireBefore(interpreter, address);
            instr.accept(interpreter);
            interpreter.commit();
            probe.fireAfter(interpreter, address);

            if ( probe.isEmpty() ) {
                // if the probed instruction has no more probes, remove it altogether
                writeInstr(address, instr);
            }
        }

        public Instr build(int address, Operand[] ops) {
            throw Avrora.failure("ProbedInstr should be confined to BaseInterpreter");
        }

        public String getOperands() {
            return instr.getOperands();
        }

    }

}
