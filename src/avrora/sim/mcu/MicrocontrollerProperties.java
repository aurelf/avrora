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

import avrora.util.StringUtil;

import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * The <code>MicrocontrollerProperties</code> class is simply a wrapper class around several
 * properties of a microcontroller including the size of the IO registers, the size of SRAM,
 * flash, and EEPROM, as well as the mapping between names of pins and their physical pin
 * number.
 *
 * @author Ben L. Titzer
 */
public class MicrocontrollerProperties {

    public final int ioreg_size;
    public final int sram_size;
    public final int flash_size;
    public final int eeprom_size;
    public final int num_pins;

    protected final HashMap pinAssignments;
    protected final HashMap ioregAssignments;

    /**
     * The constructor for the <code>MicrocontrollerProperties</code> class creates a new
     * instance with the specified register size, flash size, etc. All such fields are immutable,
     * and the pin assignments and IO register assignments cannot be changed.
     *
     * @param is the number of IO registers on this microcontroller
     * @param ss the size of the SRAM in bytes
     * @param fs the size of the flash in bytes
     * @param es the size of the EEPROM in bytes
     * @param np the number of physical pins on the microcontroller
     * @param pa a <code>HashMap</code> instance mapping string names to <code>Integer</code>
     * indexes for the pins
     * @param ia a <code>HashMap</code> instance mapping string names to <code>Integer</code>
     * indexes for the IO registers
     */
    public MicrocontrollerProperties(int is, int ss, int fs, int es, int np, HashMap pa, HashMap ia) {
        ioreg_size = is;
        sram_size = ss;
        flash_size = fs;
        eeprom_size = es;
        num_pins = np;

        pinAssignments = pa;
        ioregAssignments = ia;
    }

    /**
     * The <code>getPin()</code> method retrieves the pin number for the given pin name for this
     * microcontroller.
     * @param n the name of the pin such as "OC0"
     * @return an integer representing the physical pin number if it exists;
     * @throws NoSuchElementException if the specified pin name does not have an assignment
     */
    public int getPin(String n) {
        Integer i = (Integer)pinAssignments.get(n);
        if ( i == null )
            throw new NoSuchElementException(StringUtil.quote(n)+" pin not found");
        return i.intValue();
    }

    /**
     * The <code>getIOReg()</code> method retrieves the IO register number for the given IO
     * Register name for this microcontroller.
     * @param n the name of the IO register such as "TCNT0"
     * @return an integer representing the IO register number if it exists
     * @throws NoSuchElementException if the specified IO register name does not have an assignment
     */
    public int getIOReg(String n) {
        Integer i = (Integer)ioregAssignments.get(n);
        if ( i == null )
            throw new NoSuchElementException(StringUtil.quote(n)+" IO register not found");
        return i.intValue();
    }
}
