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

import avrora.sim.BaseInterpreter;
import avrora.sim.Clock;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.util.Arithmetic;

/**
 * @author Ben L. Titzer
 */
public abstract class ATMegaFamily extends AtmelMicrocontroller {

    public final int SRAM_SIZE;
    public final int IOREG_SIZE;
    public final int FLASH_SIZE;
    public final int EEPROM_SIZE;

    abstract class IMRReg extends State.RWIOReg {

        /**
         * The <code>mapping</code> array maps a bit number (0-7) to an interrupt number (0-35). This is used
         * for calculating the posted interrupts.
         */
        protected final int mapping[];
        protected final long interruptMask;

        IMRReg(int[] map) {
            long mask = 0;
            mapping = new int[8];
            for (int cntr = 0; cntr < 8; cntr++) {
                mapping[cntr] = map[cntr];
                if (mapping[cntr] >= 0)
                    mask |= 1 << mapping[cntr];
            }
            interruptMask = mask;
        }

        public void update(IMRReg other) {
            int posts = this.value & other.value & 0xff;
            long posted = 0;

            // calculate all posted interrupts at correct bit positions
            for (int count = 0; count < 8; count++) {
                if (Arithmetic.getBit(posts, count)) {
                    int vnum = getVectorNum(count);
                    if (vnum >= 0) posted |= (0x1 << vnum);
                }
            }

            long previousPosted = interpreter.getPostedInterrupts() & ~(interruptMask);
            long newPosted = previousPosted | posted;
            interpreter.setPostedInterrupts(newPosted);
        }

        public void update(int bit, IMRReg other) {
            if (Arithmetic.getBit((byte)(this.value & other.value), bit))
                interpreter.postInterrupt(getVectorNum(bit));
            else
                interpreter.unpostInterrupt(getVectorNum(bit));
        }

        protected int getVectorNum(int bit) {
            return mapping[bit];
        }
    }

    public class FlagRegister extends IMRReg {

        public MaskRegister maskRegister;

        public FlagRegister(int[] map) {
            super(map);
            maskRegister = new MaskRegister(map, this);
        }

        public void write(byte val) {
            value = (byte)(value & ~val);
            update(maskRegister);
        }

        public void flagBit(int bit) {
            value = Arithmetic.setBit(value, bit);
            update(bit, maskRegister);
        }

        public void writeBit(int bit, boolean val) {
            if (val) {
                value = Arithmetic.clearBit(value, bit);
                interpreter.unpostInterrupt(getVectorNum(bit));
            }
        }

    }

    public class MaskRegister extends IMRReg {

        public final FlagRegister flagRegister;

        public MaskRegister(int[] map, FlagRegister fr) {
            super(map);
            flagRegister = fr;
        }

        public void write(byte val) {
            value = val;
            update(flagRegister);
        }

        public void writeBit(int bit, boolean val) {
            if (val) {
                value = Arithmetic.setBit(value, bit);
                update(bit, flagRegister);
            } else {
                value = Arithmetic.clearBit(value, bit);
                interpreter.unpostInterrupt(getVectorNum(bit));
            }
        }
    }

    /**
     * The <code>MaskableInterrupt</code> class represents an interrupt that is controlled by
     * two bits in two registers: a mask bit in a mask register and a flag bit in a flag register,
     * at the same offset. When this interrupt fires, it automatically clears the flag bit in
     * the flag register.
     */
    public class MaskableInterrupt implements Simulator.Interrupt {
        protected final int interruptNumber;

        protected final MaskRegister maskRegister;
        protected final FlagRegister flagRegister;
        protected final int bit;

        protected final boolean sticky;

        public MaskableInterrupt(int num, MaskRegister mr, FlagRegister fr, int b, boolean e) {
            interruptNumber = num;
            maskRegister = mr;
            flagRegister = fr;
            bit = b;
            sticky = e;
        }

        public void force() {
            // flagging the bit will cause the interrupt to be posted if it is not masked
            flagRegister.flagBit(bit);
        }

        public void fire() {
            if (!sticky) {
                // setting the flag register bit will actually clear and unpost the interrupt
                flagRegister.writeBit(bit, true);
            }
        }
    }


    protected ATMegaFamily(int hz, int sram_size, int ioreg_size, int flash_size, int eeprom_size, int num_pins) {
        super(hz, num_pins);
        SRAM_SIZE = sram_size;
        IOREG_SIZE = ioreg_size;
        FLASH_SIZE = flash_size;
        EEPROM_SIZE = eeprom_size;
    }

    /**
     * The <code>getRamSize()</code> method returns the number of bytes of SRAM present on this hardware
     * device.
     *
     * @return the number of bytes of SRAM on this hardware device
     */
    public int getRamSize() {
        return SRAM_SIZE;
    }

    /**
     * The <code>getIORegSize()</code> method returns the number of IO registers that are present on this
     * hardware device.
     *
     * @return the number of IO registers supported on this hardware device
     */
    public int getIORegSize() {
        return IOREG_SIZE;
    }

    /**
     * The <code>getFlashSize()</code> method returns the size in bytes of the flash memory on this hardware
     * device. The flash memory stores the initialized data and the machine code instructions of the program.
     *
     * @return the size of the flash memory in bytes
     */
    public int getFlashSize() {
        return FLASH_SIZE;
    }

    /**
     * The <code>getEEPromSize()</code> method returns the size in bytes of the EEPROM on this hardware
     * device.
     *
     * @return the size of the EEPROM in bytes
     */
    public int getEEPromSize() {
        return EEPROM_SIZE;
    }

    /**
     * The <code>getHZ()</code> method returns the number of cycles per second at which this hardware device
     * is designed to run.
     *
     * @return the number of cycles per second on this device
     */
    public int getHz() {
        return HZ;
    }
}
