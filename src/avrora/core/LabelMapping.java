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

import java.util.HashMap;

/**
 * The <code>LabelMapping</code> class is a simple implementation of the <code>SourceMapping</code>
 * class that maps labels to addresses in the program. It does not record module information, line
 * number information, etc.
 *
 * @author Ben L. Titzer
 */
public class LabelMapping extends SourceMapping {

    protected final HashMap labels;
    protected final HashMap reverseMap;

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
     * The constructor for the <code>LabelMapping</code> class constructs a new instance of this source
     * mapping for the specified program.
     * @param p the program to create a new source mapping for
     */
    public LabelMapping(Program p) {
        super(p);
        labels = new HashMap();
        reverseMap = new HashMap();
    }

    /**
     * The <code>getName()</code> method translates a code address into a name that is more useful to
     * the user, such as a label. In the implementation of the label mapping, this method will return
     * the label name for this address if there is one. If there is no label for the specified address,
     * this method will render the address as a hexadecimal string via the <code>StringUtil.addrToString()<.code>
     * method.
     * @param address the address of an instruction in the program
     * @return a string representation of the address as a label or a hexadecimal string
     */
    public String getName(int address) {
        String s = (String)reverseMap.get(new Integer(address));
        return s == null ? StringUtil.addrToString(address) : s;
    }

    /**
     * The <code>getAddress()</code> method translates a source level name into a machine-code level
     * address. In this implementation, the name is considered to be a label within the assembly program.
     * If the name is not known in the program, this method will return -1.
     * @param name the name of some program label as a string
     * @return the address of that program entity as a byte address in the code of the program; -1 if
     * the name is not present in this program
     */
    public int getAddress(String name) {
        if ( isHexInteger(name) )
            return StringUtil.evaluateIntegerLiteral(name);
        Location l = (Location)labels.get(name);
        return l == null ? -1 : l.address;
    }

    private boolean isHexInteger(String name) {
        if ( name.length() < 2 ) return false;
        if ( name.charAt(0) != '0' ) return false;
        char c = name.charAt(1);
        if ( c == 'x' || c == 'X') return true;
        return false;
    }

    /**
     * The <code>getLocation()</cdoe> method retrieves an object that represents a location for the given name,
     * if the name exists in the program. If the name does not exist in the program, this method will return null.
     * For strings beginning with "0x", this method will evaluate them as hexadecimal literals and return a
     * location corresponding to an unnamed location at that address.
     * @param name the name of a program location as a label or a hexadecimal constant
     * @return a <code>Location</code> object representing that program location; <code>null</code> if the
     * specified label is not contained in the program
     */
    public Location getLocation(String name) {
        if ( isHexInteger(name) )
            return new Location(null, StringUtil.evaluateIntegerLiteral(name));
        return (Location)labels.get(name);
    }

    /**
     * The <code>newLocatiobn()</code> method creates a new program location with the specified label name that
     * is stored internally.
     * @param name the name of the label
     * @param address the address in the program for which to create and store a new location
     */
    public void newLocation(String name, int address) {
        Location l = new Location(name, address);
        labels.put(name, l);
        reverseMap.put(new Integer(address), name);
    }

}
