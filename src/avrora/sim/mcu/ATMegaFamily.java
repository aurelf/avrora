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

    abstract static class IMRReg extends State.RWIOReg {

        /**
         * The <code>mapping</code> array maps a bit number (0-7) to an interrupt number (0-35). This is used
         * for calculating the posted interrupts.
         */
        protected final int mapping[];
        protected final long interruptMask;
        protected final BaseInterpreter interpreter;

        IMRReg(BaseInterpreter interp, int[] map) {
            long mask = 0;
            mapping = new int[8];
            for (int cntr = 0; cntr < 8; cntr++) {
                mapping[cntr] = map[cntr];
                if (mapping[cntr] >= 0)
                    mask |= 1 << mapping[cntr];
            }
            interpreter = interp;
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

    public static class FlagRegister extends IMRReg {

        public MaskRegister maskRegister;

        public FlagRegister(BaseInterpreter interp, int[] map) {
            super(interp, map);
            maskRegister = new MaskRegister(interp, map, this);
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

    public static class MaskRegister extends IMRReg {

        public final FlagRegister flagRegister;

        public MaskRegister(BaseInterpreter interp, int[] map, FlagRegister fr) {
            super(interp, map);
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
    public static class MaskableInterrupt implements Simulator.Interrupt {
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

    public static class DirectionRegister extends State.RWIOReg {

        protected ATMegaFamily.Pin[] pins;

        protected DirectionRegister(ATMegaFamily.Pin[] p) {
            pins = p;
        }

        public void write(byte val) {
            for (int cntr = 0; cntr < 8; cntr++)
                pins[cntr].setOutputDir(Arithmetic.getBit(val, cntr));
            value = val;
        }

        public void writeBit(int bit, boolean val) {
            pins[bit].setOutputDir(val);
            value = Arithmetic.setBit(value, bit, val);
        }
    }

    public static class PortRegister extends State.RWIOReg {
        protected ATMegaFamily.Pin[] pins;

        protected PortRegister(ATMegaFamily.Pin[] p) {
            pins = p;
        }

        public void write(byte val) {
            for (int cntr = 0; cntr < 8; cntr++)
                pins[cntr].write(Arithmetic.getBit(val, cntr));
            value = val;
        }

        public void writeBit(int bit, boolean val) {
            pins[bit].write(val);
            value = Arithmetic.setBit(value, bit, val);
        }
    }

    public static class PinRegister implements State.IOReg {
        protected ATMegaFamily.Pin[] pins;

        protected PinRegister(ATMegaFamily.Pin[] p) {
            pins = p;
        }

        public byte read() {
            int value = 0;
            value |= pins[0].read() ? 1 << 0 : 0;
            value |= pins[1].read() ? 1 << 1 : 0;
            value |= pins[2].read() ? 1 << 2 : 0;
            value |= pins[3].read() ? 1 << 3 : 0;
            value |= pins[4].read() ? 1 << 4 : 0;
            value |= pins[5].read() ? 1 << 5 : 0;
            value |= pins[6].read() ? 1 << 6 : 0;
            value |= pins[7].read() ? 1 << 7 : 0;
            return (byte)value;
        }

        public boolean readBit(int bit) {
            return pins[bit].read();
        }

        public void write(byte val) {
            // ignore writes.
        }

        public void writeBit(int num, boolean val) {
            // ignore writes
        }
    }

    protected ATMegaFamily(int hz, MicrocontrollerProperties p) {
        super(hz, p);
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
