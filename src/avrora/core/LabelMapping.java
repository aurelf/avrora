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

import cck.text.StringUtil;
import java.util.HashMap;
import java.util.Iterator;

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
     * The <code>getLocation()</cdoe> method retrieves an object that represents a location for the given name,
     * if the name exists in the program. If the name does not exist in the program, this method will return null.
     * For strings beginning with "0x", this method will evaluate them as hexadecimal literals and return a
     * location corresponding to an unnamed location at that address.
     * @param name the name of a program location as a label or a hexadecimal constant
     * @return a <code>Location</code> object representing that program location; <code>null</code> if the
     * specified label is not contained in the program
     */
    public Location getLocation(String name) {
        if ( StringUtil.isHex(name) )
            return new Location(null, null, StringUtil.evaluateIntegerLiteral(name));
        return (Location)labels.get(name);
    }

    /**
     * The <code>newLocatiobn()</code> method creates a new program location with the specified label name that
     * is stored internally.
     * @param section
     * @param name the name of the label
     * @param address the address in the program for which to create and store a new location
     */
    public void newLocation(String section, String name, int address) {
        Location l = new Location(section, name, address);
        labels.put(name, l);
        reverseMap.put(new Integer(address), name);
    }

    public Iterator getIterator() {
        return labels.values().iterator();
    }
}
