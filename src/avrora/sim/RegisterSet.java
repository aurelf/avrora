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

import avrora.sim.mcu.Microcontroller;
import avrora.Avrora;
import avrora.util.Arithmetic;
import avrora.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
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
     * The <code>Field</code> interface represents a field that may be spread across
     * multiple IO registers. Clients of the <code>RegisterSet</code> class should implement
     * a field object and install it for the named field of interest. When the field is altered,
     * in whole or in part, the <code>write()</code> method of the field object will be
     * called, passing the value written (and which bits are actually written).
     */
    public interface Field {
        public void write(int nval, int wmask);
        public int read();
    }

    public class FieldWriter {
        int value;
        int writtenMask;
        Field fobject;

        void commit() {
            fobject.write(value, writtenMask);
            writtenMask = 0;
        }
    }

    static class SubField {
        FieldWriter fieldWriter; // the field which this subfield is a part of
        int ior_low_bit; // low bit in the IO register
        int field_low_bit;  // offset (low bit) in the field
        int mask; // low bit in the IO register
        boolean commit;
    }

    /**
     * The <code>Register</code> class implements an IO register that is
     * directly read and written by the program. This IO register implements writes
     * that alter multiple fields and subfields in the register set.
     */
    public class Register implements ActiveRegister {

        byte value;
        SubField[] subFields;

        public byte read() {
            return value;
        }

        public boolean readBit(int bit) {
            return Arithmetic.getBit(value, bit);
        }

        public void write(byte nval) {
            for ( int cntr = 0; cntr < subFields.length; cntr++ ) {
                SubField sf = subFields[cntr];
                writeSubField(nval, sf);
                if ( sf.commit ) sf.fieldWriter.commit();
            }
            this.value = nval;
        }

        public void writeBit(int bit, boolean val) {
            throw Avrora.unimplemented();
        }

        void writeSubField(int value, SubField f) {
            value = (value >> f.ior_low_bit); // shift over the value written
            value = value & f.mask; // get the relavant bits of the value
            int fmask = f.mask << f.field_low_bit;
            f.fieldWriter.writtenMask |= fmask; // remember which bits were written
            int pf = f.fieldWriter.value & ~fmask; // mask out corresponding bits in field value
            f.fieldWriter.value = pf | value << f.field_low_bit; // or the old value with new value
        }
    }

    protected final HashMap fields;
    protected final HashMap registers;

    public RegisterSet() {
        fields = new HashMap();
        registers = new HashMap();
    }

    /**
     * The <code>getField()</code> gets an object that represents an entire field which
     * may be stored across multiple registers in multiple bit fields. This object allows
     * access to the field's value without consideration for its underlying representation
     * in the IO register(s).
     * @param fname the name of the fieldWriter to get the fieldWriter representation for
     * @return a reference to the <code>FieldWriter</code> object that represents the field
     */
    public FieldWriter getField(String fname) {
        return (FieldWriter)fields.get(fname);
    }

    public void installField(String name, Field fo) {
        FieldWriter f = getField(name);
        f.fobject = fo;
    }

    /**
     * The <code>newRegister()</code> method adds an IO register to this register set. The
     * name of the register is given, as well as a description of the portions of what fields
     * it contains. The description is used to create an IO register implementation so that reads
     * and writes to the program update the correct portions of the fields specified.
     * @param name the name of the IO register as a string
     * @param desc the description of the IO register which contains information about what
     * parts of what fields are stored in which locations.
     */
    public void newRegister(String name, String desc) {

        // create a new register
        Register r = new Register();

        // parse the subfields from the description
        r.subFields = parseSubFields(desc);

        // remember the register
        registers.put(name, r);

        // calculate the commit points (i.e. last write to the field)
        HashSet fs = new HashSet();
        for ( int cntr = r.subFields.length - 1; cntr >= 0; cntr-- ) {
            SubField subField = r.subFields[cntr];
            if ( !fs.contains(subField.fieldWriter) ) subField.commit = true;
            fs.add(subField.fieldWriter);
        }
    }

    private SubField[] parseSubFields(String desc) {
        int count = 0;
        SubField[] sfs = new SubField[8];
        StringCharacterIterator i = new StringCharacterIterator(desc);
        int ior_hbit = 7;
        while ( ior_hbit >= 0 && i.current() != CharacterIterator.DONE ) {
            if ( i.current() == '.') {
                // unused field is specified
                ior_hbit = readUnusedField(i, ior_hbit);
            } else if ( i.current() == 'x') {
                // reserved field is specified
                ior_hbit = readReservedField(i, ior_hbit);
            } else {
                // named field is specified
                String fid = StringUtil.readIdentifier(i);
                FieldWriter f = createField(fid);
                if ( StringUtil.peekAndEat(i, '[') ) {
                    // a bit range is specified; create a subfield
                    int fhbit = StringUtil.readDecimalValue(i, 1);
                    int flbit = fhbit;
                    if ( StringUtil.peekAndEat(i, ':')) {
                        flbit =  StringUtil.readDecimalValue(i, 1);
                    }
                    int length = fhbit - flbit + 1;
                    int ior_lbit = ior_hbit - length + 1;
                    SubField sf = new SubField();
                    sfs[count++] = sf;
                    sf.fieldWriter = f;
                    sf.ior_low_bit = ior_lbit;
                    sf.field_low_bit = flbit;
                    sf.mask = 0xff >> (8 - length);
                    StringUtil.peekAndEat(i, ']');
                    ior_hbit -= length;
                } else {
                    // no bit range is specified; assume the rest of the register
                    // is the field
                    int length = ior_hbit + 1;
                    SubField sf = new SubField();
                    sfs[count++] = sf;
                    sf.fieldWriter = f;
                    sf.ior_low_bit = 0;
                    sf.field_low_bit = 0;
                    sf.mask = 0xff >> (8 - length);
                    break;
                }
                StringUtil.peekAndEat(i, ',');
                StringUtil.skipWhiteSpace(i);
            }
        }
        // resize the array to be smaller
        SubField[] subFields = new SubField[count];
        System.arraycopy(sfs, 0, subFields, 0, count);
        return subFields;
    }

    private int readUnusedField(StringCharacterIterator i, int ior_hbit) {
        while ( i.current() == '.' ) {
            ior_hbit--;
            i.next();
        }
        return ior_hbit;
    }

    private int readReservedField(StringCharacterIterator i, int ior_hbit) {
        while ( i.current() == 'x' ) {
            ior_hbit--;
            i.next();
        }
        return ior_hbit;
    }

    private FieldWriter createField(String fid) {
        FieldWriter f = (FieldWriter)fields.get(fid);
        if ( f == null ) {
            f = new FieldWriter();
            fields.put(fid, f);
        }
        return f;
    }

}
