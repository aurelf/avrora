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

import avrora.sim.util.MulticastWatch;
import avrora.util.StringUtil;
import avrora.util.Util;
import java.util.Arrays;

/**
 * The <code>Segment</code> class represents a segment of byte-addressable memory that
 * supports probing. It is used to represent the SRAM, the flash, and the EEPROM. For
 * efficiency, it sharing of the underlying byte array representing the memory.
 *
 * @author Ben L. Titzer
 */
public class Segment {

    protected final BaseInterpreter interpreter;
    protected final String name;
    protected final int size;
    protected final byte value;
    protected byte[] segment_data;
    protected final ErrorReporter errorReporter;
    protected MulticastWatch[] segment_watches;

    protected Sharer sharer;

    /**
     * The constructor for the <code>Segment</code> class creates an object that represents this segment.
     * The segment is byte-addressable and allows watches to be placed on individual bytes in memory.
     * @param name the name of the segment as a string
     * @param size the size of the segment in bytes
     * @param defvalue the default value of bytes in this segment
     * @param er an object that is notified when an attempt is made to read or write outside of the bounds
     * of this segment
     */
    public Segment(String name, int size, byte defvalue, BaseInterpreter bi, ErrorReporter er) {
        this.name = name;
        this.size = size;
        this.value = defvalue;
        this.errorReporter = er;
        this.segment_data = new byte[size];
        this.interpreter = bi;

        // if the default value is something other than zero, initialize the array
        if ( defvalue != 0 ) {
            Arrays.fill(segment_data, defvalue);
        }
    }

    /**
     * The <code>Sharer</code> interface must be implemented by a class that needs to
     * share the underlying data representation for efficiency reasons. Whenever the
     * reference to the underlying data is changed (i.e. the array is resized), the
     * sharer will be notified and it must update its reference.
     */
    public interface Sharer {
        public void update(byte[] segment);
    }

    /**
     * The <code>ErrorReporter</code> class is used to intercept errors caused by trying to
     * either read or write outside the bounds of this segment. Since this could be done by
     * the program being simulated, the interpreter would like the ability to detect this
     * case and continue simulation rather than an exception being thrown. In other situations,
     * it might be better to throw an exception. Therefore, this object allows the policy
     * to be set separately by an outside object.
     */
    public interface ErrorReporter {
        public byte readError(int address);
        public void writeError(int address, byte value);
    }

    /**
     * The <code>AddressOutOfBoundsException</code> class represents an error when
     * using the <code>get()</code> and <code>set()</code> methods where the user
     * specifies an address that is out of the bounds of the segment. Note that this
     * exception is not thrown in the case of <code>read()</code> and <code>write()</code>
     * methods making accesses out of bounds; in that case, the <code>ErrorReporter</code>
     * object is consulted.
     */
    public class AddressOutOfBoundsException extends Util.Error {
        public final int data_addr;

        protected AddressOutOfBoundsException(int da) {
            super("Segment access error", "illegal access of "+ StringUtil.quote(name) + " at " + StringUtil.addrToString(da));
            this.data_addr = da;
        }
    }

    /**
     * The <code>share()</code> method allows sharing of the underlying array representing the values of
     * memory. This is used for performance.
     * @param s the sharer to allow access to this segment's internal data
     * @return the byte array that is the underlying representation of the memory
     */
    public byte[] share(Sharer s) {
        sharer = s;
        return segment_data;
    }

    /**
     * The <code>read()</code> method of the segment reads a byte from a location in the segment,
     * firing any watches on the address before and after the read. This is intended to be called
     * ONLY by the interpreter and device implementations, and NOT by other parts of the simulation
     * that simply want to inspect the state of simulation. A call to the <code>read()</code> method
     * will result in a call to the <code>readError()</code> method of the <code>ErrorReporter</code>
     * object associated with this segment if the access is not within the bounds of the segment.
     * @param address the address in the segment from which to read the byte
     * @return the byte value at this location
     */
    public byte read(int address) {
        MulticastWatch p;
        // FAST PATH 1: no watches
        if ( segment_watches == null ) {
            return checked_read(address);
        }

        // FAST PATH 2: no watches for this address
        p = segment_watches[address];
        if ( p == null) {
            return checked_read(address);
        }

        // SLOW PATH: consult with memory watches
        p.fireBeforeRead(interpreter.state, address);
        byte val = checked_read(address);
        p.fireAfterRead(interpreter.state, address, val);
        return val;
    }

    /**
     * The <code>checked_read()</code> method simply reads from the segment's array representing the
     * actual raw values of memory. It will catch out of bounds accesses and notify the
     * <code>ErrorReporter</code> instance for this segment.
     * @param address the address in the segment from which to read the byte
     * @return the byte value at this location if the access is within the bounds of the segment; the
     * result of the <code>readError()</code> method of the <code>ErrorReporter</code> object associated
     * with this segment otherwise
     */
    private byte checked_read(int address) {
        try {
            return segment_data[address];
        } catch ( ArrayIndexOutOfBoundsException e ) {
            return errorReporter.readError(address);
        }
    }

    /**
     * The <code>get()</code> method simply retrieves the value of a byte at a particular location in the
     * segment. This method will NOT result in triggering any watches installed for this address. This method
     * is intended for use by user code outside of the simulation (such as probes and watches) to inspect
     * the values in this segment.
     * @param address the address in the segment for which to retrieve the value
     * @return the value of the byte at the specified location, without triggering any monitors on this location
     * @throws AddressOutOfBoundsException if the specified address is not within the bounds of this segment
     */
    public byte get(int address) {
        try {
            return segment_data[address];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new AddressOutOfBoundsException(address);
        }
    }

    /**
     * The <code>write()</code> method of the segment writes a byte to a location in the segment,
     * firing any watches on the address before and after the write. This is intended to be called
     * ONLY by the interpreter and device implementations, and NOT by other parts of the simulation
     * that want to alter the state of the simulation. A call to the <code>write()</code> method
     * will result in a call to the <code>writeError()</code> method of the <code>ErrorReporter</code>
     * object associated with this segment if the access is not within the bounds of the segment.
     * @param address the address in the segment which should be written
     * @param val the value to write to the location in the segment
     */
    public void write(int address, byte val) {
        MulticastWatch p;
        // FAST PATH 1: no watches
        if ( segment_watches == null ) {
            checked_write(address, val);
            return;
        }

        // FAST PATH 2: no watches for this address
        p = segment_watches[address];
        if ( p == null) {
            checked_write(address, val);
            return;
        }

        // SLOW PATH: consult with memory watches
        p.fireBeforeWrite(interpreter.state, address, val);
        checked_write(address, val);
        p.fireAfterWrite(interpreter.state, address, val);
    }

    /**
     * The <code>checked_write()</code> method simply writes to the segment's array representing the
     * actual raw values of memory. It will catch out of bounds accesses and notify the
     * <code>ErrorReporter</code> instance for this segment.
     * @param address the address in the segment which should be written
     * @param val the value to write to this byte in memory
     */
    private void checked_write(int address, byte val) {
        try {
            segment_data[address] = val;
        } catch (ArrayIndexOutOfBoundsException e) {
            errorReporter.writeError(address, val);
        }
    }

    /**
     * The <code>set()</code> method simply sets the value of a byte at a particular location in the
     * segment. This method will NOT result in triggering any watches installed for this address. This method
     * is intended for use by code outside of the simulation (such as probes and watches) to alter the
     * state of the memory segment, which should not be done by user code.
     * @param address the address in the segment which should be written
     * @param val the value to write to this byte in memory
     * @throws AddressOutOfBoundsException if the specified address is not within the bounds of this segment
     */
    public void set(int address, byte val) {
        try {
            segment_data[address] = val;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new AddressOutOfBoundsException(address);
        }
    }

    /**
     * The <code>insertWatch()</code> allows user code to insert a watch on a particular memory location.
     * The watch will be triggered when a <code>read()</code> or <code>write()</code> to the memory location
     * occurs in during simulation.
     * @param data_addr the address of the byte in memory for which to insert the watch
     * @param p the watch to insert on the memory byte
     */
    public void insertWatch(int data_addr, Simulator.Watch p) {
        if (segment_watches == null)
            segment_watches = new MulticastWatch[size];

        // add the probe to the multicast probe present at the location (if there is one)
        MulticastWatch mcw = segment_watches[data_addr];
        if (mcw == null) mcw = segment_watches[data_addr] = new MulticastWatch();
        mcw.add(p);
    }

    /**
     * The <code>removeWatch()</code> removes a watch on a particular memory location.
     * The watch will no longer be triggered for subseqent <code>read()</code>s or <code>write()</code>s
     * occur in during simulation. Reference equality is used to match watches, and NOT <code>Object.equals()</code>.
     * @param data_addr the address of the byte in memory for which to remove the watch
     * @param p the watch to remove
     */
    public void removeWatch(int data_addr, Simulator.Watch p) {
        if (segment_watches == null)
            return;

        // remove the probe from the multicast probe present at the location (if there is one)
        MulticastWatch w = segment_watches[data_addr];
        if (w == null) return;
        w.remove(p);
    }
}
