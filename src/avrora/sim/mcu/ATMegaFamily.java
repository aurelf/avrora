/**
 * Copyright (c) 2004, Regents of the University of California
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
public abstract class ATMegaFamily implements Microcontroller {
    /**
     * The <code>HZ</code> field stores a public static final integer that represents the clockspeed of the
     * AtMega128L microcontroller (7.327mhz).
     */
    public final int HZ;
    public final int SRAM_SIZE;
    public final int IOREG_SIZE;
    public final int FLASH_SIZE;
    public final int EEPROM_SIZE;
    public final int NUM_PINS;

    protected Simulator.Printer pinPrinter;
    protected final Pin[] pins;

    protected Clock clock;
    protected Simulator simulator;
    protected BaseInterpreter interpreter;

    /**
     * The <code>Pin</code> class implements a model of a pin on the ATMegaFamily for the general purpose IO
     * ports.
     */
    protected class Pin implements Microcontroller.Pin {
        protected final int number;

        boolean level;
        boolean outputDir;
        boolean pullup;

        Microcontroller.Pin.Input input;
        Microcontroller.Pin.Output output;

        protected Pin(int num) {
            number = num;
        }

        public void connect(Output o) {
            output = o;
        }

        public void connect(Input i) {
            input = i;
        }

        protected void setOutputDir(boolean out) {
            outputDir = out;
            if (out) write(level);
        }

        protected void setPullup(boolean pull) {
            pullup = pull;
        }

        protected boolean read() {
            boolean result;
            if (!outputDir) {
                if (input != null)
                    result = input.read();
                else
                    result = pullup;

            } else {
                result = level;
            }
            // print the result of the read
            printRead(result);
            return result;
        }

        private void printRead(boolean result) {
            if (pinPrinter == null) pinPrinter = simulator.getPrinter("sim.pin");
            if (pinPrinter.enabled) {
                String dir = getDirection();
                pinPrinter.println("READ PIN: " + number + ' ' + dir + "<- " + result);
            }
        }

        private String getDirection() {
            if (!outputDir) {
                if (input != null)
                    return "[input] ";
                else
                    return "[pullup:" + pullup + "] ";

            } else {
                return "[output] ";
            }
        }

        protected void write(boolean value) {
            level = value;
            // print the write
            printWrite(value);
            if (outputDir && output != null) output.write(value);
        }

        private void printWrite(boolean value) {
            if (pinPrinter == null) pinPrinter = simulator.getPrinter("sim.pin");
            if (pinPrinter.enabled) {
                String dir = getDirection();
                pinPrinter.println("WRITE PIN: " + number + ' ' + dir + "-> " + value);
            }
        }
    }

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
        HZ = hz;
        SRAM_SIZE = sram_size;
        IOREG_SIZE = ioreg_size;
        FLASH_SIZE = flash_size;
        EEPROM_SIZE = eeprom_size;
        NUM_PINS = num_pins;
        pins = new Pin[NUM_PINS];
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

    /**
     * The <code>millisToCycles()</code> method converts the specified number of milliseconds to a cycle
     * count. The conversion factor used is the number of cycles per second of this device. This method serves
     * as a utility so that clients need not do repeated work in converting milliseconds to cycles and back.
     *
     * @param ms a time quantity in milliseconds as a double
     * @return the same time quantity in clock cycles, rounded up to the nearest integer
     */
    public long millisToCycles(double ms) {
        return (long)(ms * HZ / 1000);
    }

    /**
     * The <code>cyclesToMillis()</code> method converts the specified number of cycles to a time quantity in
     * milliseconds. The conversion factor used is the number of cycles per second of this device. This method
     * serves as a utility so that clients need not do repeated work in converting milliseconds to cycles and
     * back.
     *
     * @param cycles the number of cycles
     * @return the same time quantity in milliseconds
     */
    public double cyclesToMillis(long cycles) {
        return 1000 * ((double)cycles) / HZ;
    }

    /**
     * The <code>getPin()</code> method looks up the specified pin by its number and returns a reference to
     * that pin. The intended users of this method are external device implementors which connect their
     * devices to the microcontroller through the pins.
     *
     * @param num the pin number to look up
     * @return a reference to the <code>Pin</code> object corresponding to the named pin if it exists; null
     *         otherwise
     */
    public Microcontroller.Pin getPin(int num) {
        if (num < 0 || num > pins.length) return null;
        return pins[num];
    }

}
