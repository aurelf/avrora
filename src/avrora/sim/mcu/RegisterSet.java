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

package avrora.sim.mcu;

import avrora.sim.mcu.Microcontroller;
import avrora.sim.ActiveRegister;
import avrora.sim.RWRegister;
import avrora.Avrora;
import avrora.util.Arithmetic;
import avrora.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.text.StringCharacterIterator;
import java.text.CharacterIterator;

/**
 * The <code>RegisterSet</code> class is a utility that simplifies the implementation
 * of certain IO registers that contain many types of fields whose bits may be spread
 * out and mixed up over multiple IO registers. For example, a 5 bit field used to
 * configure a device might be spread among multiple 1, 2, 3, or 4 bit fields across
 * multiple registers. This class allows a set of those registers to be created,
 * and collects together writes and reads easily.
 *
 * @author Ben L. Titzer
 */
public class RegisterSet {

    /**
     * The <code>Field</code> class represents a collection of bits that represent
     * a quantity used by a device. The bits that make up this quantity might be spread
     * over multiple IO registers or mixed up among one IO register. Also, a field might
     * be in different IO registers depending on the microcontroller model. For this reason,
     * the <code>Field</code> class offers convenience to the device implementer by collecting
     * all of the individual bit updates in different registers into one coherent, contiguous
     * value.
     *
     * <p>
     * Device implementations can simply get a reference to the <code>Field</code> object
     * (such as a timer mode, prescaler value, etc) by calling <code>getField()</code>
     * in <code>RegisterSet</code>.
     */
    public static class Field {
        boolean consistent;
        public int value;

        public void write(int nval, int wmask) {
            value = value & ~wmask | nval;
            consistent = true;
            update();
        }

        public void write(int nval) {
            value = nval;
            consistent = true;
            update();
        }

        public void set(int nval) {
            throw Avrora.unimplemented();
        }

        public int read() {
            throw Avrora.unimplemented();
        }

        public void update() {
            // do nothing.
        }
    }

    static class FieldWriter {
        int value;
        int writtenMask;
        Field fobject;

        void commit() {
            fobject.write(value, writtenMask);
            value = 0;
            writtenMask = 0;
        }
    }

    static abstract class SubRegWriter {
        abstract void write(byte val);
    }

    static class SubFieldWriter extends SubRegWriter {
        FieldWriter fieldWriter;
        RegisterLayout.SubField subField;

        void write(byte val) {
            int wval = (val >> subField.ior_low_bit) & subField.mask;
            fieldWriter.value |= wval << subField.field_low_bit;
            if ( subField.commit ) fieldWriter.commit();
        }
    }

    static class TotalFieldWriter extends SubRegWriter {
        FieldWriter fieldWriter;
        int ior_low_bit;
        int mask;
        void write(byte val) {
            int value = (val >> ior_low_bit) & mask;
            fieldWriter.fobject.write(value);
        }
    }

    static class ReservedWriter extends SubRegWriter {
        int ior_low_bit;
        int mask;
        void write(byte val) {
            // TODO: check that all writes are zeroes
        }
    }

    static class UnusedWriter extends SubRegWriter {
        void write(byte val) {
            // do nothing.
        }
    }

    /**
     * The <code>Register</code> class implements an IO register that is
     * directly read and written by the program. This IO register implements writes
     * that alter multiple fields and subfields in the register set.
     */
    public class MultiFieldRegister implements ActiveRegister {

        byte value;
        SubRegWriter[] subFields;

        public byte read() {
            return value;
        }

        public boolean readBit(int bit) {
            return Arithmetic.getBit(value, bit);
        }

        public void write(byte nval) {
            this.value = nval;
            for ( int cntr = 0; cntr < subFields.length; cntr++ ) {
                SubRegWriter sf = subFields[cntr];
                sf.write(nval);
            }
        }

        public void writeBit(int bit, boolean val) {
            throw Avrora.unimplemented();
        }

    }

    protected final HashMap fields;
    protected final ActiveRegister[] registers;
    protected final RegisterLayout layout;

    /**
     * The constructor for the <code>RegisterSet</code> class creates a new register set with the specified register
     * layout and size.
     * @param rl the layout of all the registers in the set
     */
    public RegisterSet(RegisterLayout rl) {
        fields = new HashMap();
        registers = new ActiveRegister[rl.ioreg_size];
        layout = rl;

        // create the field representations
        Iterator i = rl.fields.values().iterator();
        while ( i.hasNext() ) {
            RegisterLayout.Field f = (RegisterLayout.Field)i.next();
            FieldWriter fw = new FieldWriter();
            fw.fobject = new Field();
            fields.put(f.name, fw);
        }

        // create the active registers
        for ( int ior = 0; ior < rl.ioreg_size; ior++ ) {
            RegisterLayout.RegisterInfo ri = rl.info[ior];
            if ( ri == null || ri.subfields == null ) {
                // no subfields; no special register is necessary
                ActiveRegister ar = new RWRegister();
                registers[ior] = ar;
            } else {
                // there are subfields in this register; create a special ActiveRegister
                MultiFieldRegister mfr = new MultiFieldRegister();
                SubRegWriter[] srw = new SubRegWriter[ri.subfields.length];
                for ( int cntr = 0; cntr < srw.length; cntr++ ) {
                    createSubRegWriter(ri, cntr, srw);
                }
                registers[ior] = mfr;
            }
        }
    }

    private void createSubRegWriter(RegisterLayout.RegisterInfo ri, int cntr, SubRegWriter[] srw) {
        RegisterLayout.SubField sf = ri.subfields[cntr];
        RegisterLayout.Field field = sf.field;

        if ( sf.field == RegisterLayout.RESERVED ) {
            ReservedWriter rw = new ReservedWriter();
            rw.ior_low_bit = sf.ior_low_bit;
            rw.mask = sf.mask;
            srw[cntr] = rw;
        } else if ( sf.field == RegisterLayout.UNUSED ) {
            srw[cntr] = new UnusedWriter();
        } else if ( sf.field.subfields.length == 1) {
            TotalFieldWriter tfw = new TotalFieldWriter();
            tfw.fieldWriter = (FieldWriter)fields.get(field.name);
            tfw.mask = sf.mask;
            tfw.ior_low_bit = sf.ior_low_bit;
            srw[cntr] = tfw;
        } else {
            SubFieldWriter sfw = new SubFieldWriter();
            sfw.fieldWriter = (FieldWriter)fields.get(field.name);
            sfw.subField = sf;
            srw[cntr] = sfw;
        }
    }

    /**
     * The <code>getSize()</code> method returns the total number of registers in this register set.
     * @return the number of IO registers in this set
     */
    public int getSize() {
        return registers.length;
    }

    /**
     * The <code>installIOReg()</code> method installs a new register at the specified address. This is intented
     * to be used only in the device implementations.
     * @param ar the active register to install
     * @param ior the address to install the active register to
     */
    public void installIOReg(ActiveRegister ar, int ior) {
        registers[ior] = ar;
    }

    /**
     * The <code>getRegisterLayout()</code> method gets a reference to the register layout object for this
     * register set. The register layout describes where each IO register is and what fields it contains.
     * @return a reference to the register layout for this register set
     */
    public RegisterLayout getRegisterLayout() {
        return layout;
    }

    /**
     * The <code>share()</code> method is NOT meant for general use. It is used ONLY by the interpreter to
     * share the underlying array representation that maps from register address to an actual register
     * object.
     * @return
     */
    public ActiveRegister[] share() {
        return registers;
    }

    /**
     * The <code>getField()</code> gets an object that represents an entire field which
     * may be stored across multiple registers in multiple bit fields. This object allows
     * access to the field's value without consideration for its underlying representation
     * in the IO register(s).
     * @param fname the name of the fieldWriter to get the fieldWriter representation for
     * @return a reference to the <code>FieldWriter</code> object that represents the field
     */
    public Field getField(String fname) {
        FieldWriter fwriter = getFieldWriter(fname);
        return fwriter.fobject;
    }

    private FieldWriter getFieldWriter(String fname) {
        FieldWriter fwriter = ((FieldWriter)fields.get(fname));
        if ( fwriter == null ) {
            throw Avrora.failure("Field not found in RegisterSet: "+StringUtil.quote(fname));
        }
        return fwriter;
    }

    /**
     * The <code>installField()</code> method allows device implementations to substitute a new field
     * implementation for the named field. The field implementation can then override the appropriate
     * methods of the <code>RegisterSet.Field</code> class to be notified upon writes.
     * @param fname the name of the field
     * @param fo the field object to install for this field
     * @return the new field installed
     */
    public Field installField(String fname, Field fo) {
        FieldWriter fwriter = getFieldWriter(fname);
        fwriter.fobject = fo;
        return fo;
    }

}
