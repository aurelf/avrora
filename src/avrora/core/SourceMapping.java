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

package avrora.core;

import avrora.util.StringUtil;

import java.util.Comparator;

/**
 * The <code>SourceMapping</code> class embodies the concept of mapping machine code level
 * addresses and constructions in the <code>Program</code> class back to a source code program,
 * either in assembly language (labels), or a high-level programming lagnguage like C. This
 * class is used by the simulator to report information about the program in a higher-level
 * way more readibly understandable, for example to report calls / returns between functions
 * by their names rather than their machine code addresses.
 *
 * @author Ben L. Titzer
 */
public abstract class SourceMapping {

    /**
     * The <code>program</code> field stores a reference to the program for this source mapping.
     */
    protected final Program program;

    /**
     * The <code>LOCATION_COMPARATOR</code> comparator is used in order to sort locations
     * in the program from lowest address to highest address.
     */
    public static Comparator LOCATION_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            Location l1 = (Location)o1;
            Location l2 = (Location)o2;

            if (l1.address == l2.address) {
                if (l1.name == null) return 1;
                if (l2.name == null) return -1;
                return l1.name.compareTo(l2.name);
            }
            return l1.address - l2.address;
        }
    };
    /**
     * The <code>Location</code> class represents a location in the program; either named by
     * a label, or an unnamed integer address. The location may refer to any of the code, data,
     * or eeprom segments.
     */
    public class Location {
        /**
         * The <code>address</code> field records the address of this label as a byte address.
         */
        public final int address;
        /**
         * The <code>name</code> field records the name of this label.
         */
        public final String name;

        /**
         * The constructor for the <code>Location</code> class creates a new location for the
         * specified lable and address. It is used internally to create labels.
         * @param n the name of the label as a string
         * @param addr the integer address of the location
         */
        Location(String n, int addr) {
            if ( n == null ) name = StringUtil.addrToString(addr);
            else name = n;
            address = addr;
        }

        /**
         * The <code>hashCode()</code> method computes the hash code of this location so that
         * it can be used in any of the standard collection libraries.
         * @return an integer value that represents the hash code
         */
        public int hashCode() {
            if (name == null)
                return address;
            else
                return name.hashCode();
        }

        /**
         * The <code>equals()</code> method compares this location against another object. It will return
         * true if and only if the specified object is an instance of <code>Location</code>, the addresses
         * match, and the names match.
         * @param o the other object to test this location for equality
         * @return true if the other object is equal to this label; false otherwise
         */
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Location)) return false;
            Location l = ((Location)o);
            return l.name.equals(this.name) && l.address == this.address;
        }

        public String toString() {
            return name;
        }
    }

    /**
     * The <code>getName()</code> method translates a code address into a name that is more useful to
     * the user, such as a label, a location in a method, a location in a module and the source line, etc.
     * @param address the address of an instruction in the program
     * @return a string representation of the address; e.g. a label
     */
    public abstract String getName(int address);

    /**
     * The <code>getAddress()</code> method translates a source level name into a machine-code level
     * address. For example, the name might represent the beginning of a method or a label in an
     * assembly program. If the name is not known in the program, this method should return -1.
     * @param name the name of some program entity as a string
     * @return the address of that program entity as a byte address in the code of the program; -1 if
     * the name is not present in this program
     */
    public abstract int getAddress(String name);

    /**
     * The constructor for the <code>SourceMapping</code> base class creates a new instance of source mapping
     * information for the specified program. The mapping is tied to the program throughout its lifetime.
     * @param p the program to create the source mapping for
     */
    public SourceMapping(Program p) {
        program = p;
    }

    /**
     * The <code>getProgram()</code> class returns a reference to the program for which this class
     * provides source information.
     * @return the program associated with this source mapping
     */
    public Program getProgram() {
        return program;
    }

    public abstract Location getLocation(String name);
}
