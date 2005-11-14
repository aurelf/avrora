/**
 * Copyright (c) 2005, Regents of the University of California
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
 *
 * Creation date: Nov 11, 2005
 */

package avrora.arch.msp430;

import avrora.sim.CodeSegment;
import avrora.sim.mcu.RegisterLayout;
import cck.text.StringUtil;
import java.util.*;

/**
 * @author Ben L. Titzer
 */
public class MSP430Properties {
    /**
     * The <code>ioreg_size</code> field stores the number of IO registers on this microcontroller.
     */
    public final int ioreg_size;

    /**
     * The <code>sram_size</code> field stores the size of the SRAM (excluding the general purpose
     * registers and IO registers) on this microcontroller.
     */
    public final int sram_size;

    /**
     * The <code>flash_size</code> field stores the size of the code segment (flash) on this microcontroller.
     */
    public final int code_start;

    /**
     * The <code>num_pins</code> field stores the number of physical pins on this microcontroller.
     */
    public final int num_pins;

    /**
     * The <code>num_interrupts</code> field stores the number of interrupts supported on this
     * microcontroller.
     */
    public final int num_interrupts;

    public final CodeSegment.Factory codeSegmentFactory;

    protected final HashMap pinAssignments;
    protected final RegisterLayout layout;
    protected final HashMap interruptAssignments;
    protected final String[] ioreg_name;
    protected final String[] interrupt_name;

    /**
     * The constructor for the <code>MicrocontrollerProperties</code> class creates a new
     * instance with the specified register size, flash size, etc. All such fields are immutable,
     * and the pin assignments and IO register assignments cannot be changed.
     *
     * @param is the number of IO registers on this microcontroller
     * @param ss the size of the SRAM in bytes
     * @param fs the size of the flash in bytes
     * @param np the number of physical pins on the microcontroller
     * @param ni the number of interrupts on the microcontroller
     * @param pa a <code>HashMap</code> instance mapping string names to <code>Integer</code>
     * indexes for the pins
     * @param rl a <code>RegisterLayout</code> instance mapping string names to IO register addresses
     * @param inta a <code>HashMap</code> instance mapping string names to <code>Integer</code>
     * indexes for each type of interrupt
     */
    public MSP430Properties(int is, int ss, int fs, int np, int ni, CodeSegment.Factory csf, HashMap pa, RegisterLayout rl, HashMap inta) {
        ioreg_size = is;
        sram_size = ss;
        code_start = fs;
        num_pins = np;
        num_interrupts = ni;

        codeSegmentFactory = csf;

        ioreg_name = new String[is];
        interrupt_name = new String[ni];

        pinAssignments = pa;
        layout = rl;
        interruptAssignments = inta;

        initIORNames();
        initInterruptNames();
    }

    public RegisterLayout getRegisterLayout() {
        return layout;
    }

    private void initInterruptNames() {
        Iterator i = interruptAssignments.keySet().iterator();
        while ( i.hasNext() ) {
            String s = (String)i.next();
            Integer iv = (Integer)interruptAssignments.get(s);
            interrupt_name[iv.intValue()] = s;
        }
    }

    private void initIORNames() {
        for ( int cntr = 0; cntr < layout.ioreg_size; cntr++ )
            ioreg_name[cntr] = layout.getRegisterName(cntr);
    }

    /**
     * The <code>getPin()</code> method retrieves the pin number for the given pin name for this
     * microcontroller.
     * @param n the name of the pin such as "OC0"
     * @return an integer representing the physical pin number if it exists;
     * @throws java.util.NoSuchElementException if the specified pin name does not have an assignment
     */
    public int getPin(String n) {
        Integer i = (Integer)pinAssignments.get(n);
        if ( i == null )
            throw new NoSuchElementException(StringUtil.quote(n)+" pin not found");
        return i.intValue();
    }

    /**
     * The <code>getIOReg()</code> method retrieves the IO register number for the given IO
     * LegacyRegister name for this microcontroller.
     * @param n the name of the IO register such as "TCNT0"
     * @return an integer representing the IO register number if it exists
     * @throws NoSuchElementException if the specified IO register name does not have an assignment
     */
    public int getIOReg(String n) {
        return layout.getIOReg(n);
    }

    /**
     * The <code>hasIOReg()</code> method queries whether the IO register exists on this device.
     * @param n the name of the IO register
     * @return true if the IO register exists on this device; false otherwise
     */
    public boolean hasIOReg(String n) {
        return layout.hasIOReg(n);
    }

    /**
     * The <code>getInterrupt()</code> method retrieves the interrupt number for the given interrupt
     * name for this microcontroller
     * @param n the name of the interrupt such as "RESET"
     * @return an integer representing the interrupt number if it exists
     * @throws NoSuchElementException if the specified interrupt name does not have an assignment
     */
    public int getInterrupt(String n) {
        Integer i = (Integer)interruptAssignments.get(n);
        if ( i == null )
            throw new NoSuchElementException(StringUtil.quote(n)+" interrupt not found");
        return i.intValue();
    }

    /**
     * The <code>getIORegName()</code> method returns the name of the IO register specified by
     * the given number.
     * @param ioreg the io register number for which to get a string name
     * @return the string name of the IO register if there is such a name
     */
    public String getIORegName(int ioreg) {
        return ioreg_name[ioreg];
    }

    /**
     * The <code>getInterruptName()</code> method returns the name of an interrupt specified by
     * the given number.
     * @param inum the interrupt number for which to get a string name
     * @return the string name of the interrupt if there is such a name
     */
    public String getInterruptName(int inum) {
        return interrupt_name[inum];
    }
}
