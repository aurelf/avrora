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

import avrora.sim.*;
import avrora.sim.clock.ClockDomain;
import avrora.sim.radio.Radio;
import avrora.util.Arithmetic;

/**
 * The <code>ATMegaFamily</code> class encapsulates much of the common functionality among the
 * ATMega family microcontrollers from Atmel.
 *
 * @author Ben L. Titzer
 */
public abstract class ATMegaFamily extends AtmelMicrocontroller {

    /**
     * The Radio connected to this microcontroller.
     */
    protected Radio radio;

    public void setRadio(Radio radio) {
        this.radio = radio;
    }

    public Radio getRadio() {
        return radio;
    }

    /**
     * The <code>IMRReg</code> class is the base class of IO registers that corresponds to interrupt masks
     * and flags. This class contains much of the functionality necessary to implement the interrupt registers
     * and is specialized for the mask register and flag register cases.
     */
    abstract static class IMRReg extends RWRegister {

        /**
         * The <code>mapping</code> array maps a bit number (0-7) to an interrupt number (0-35). This is used
         * for calculating the posted interrupts.
         */
        protected final int[] mapping;
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

        public void unflagBit(int bit) {
            value = Arithmetic.clearBit(value, bit);
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

    public static class DirectionRegister extends RWRegister {

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

    public static class PortRegister extends RWRegister {
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

    public static class PinRegister implements ActiveRegister {
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

    protected ATMegaFamily(ClockDomain cd, MicrocontrollerProperties p, FiniteStateMachine fsm) {
        super(cd, p, fsm);
    }

    /**
     * The <code>getHZ()</code> method returns the number of cycles per second at which this hardware device
     * is designed to run.
     *
     * @return the number of cycles per second on this device
     */
    public long getHZ() {
        return HZ;
    }

    protected static final int[] periods0 = {0, 1, 8, 32, 64, 128, 256, 1024};

    /**
     * <code>Timer0</code> is the default 8-bit timer on the ATMega128.
     */
    protected class Timer0 extends Timer8Bit {

        protected Timer0() {
            super(ATMegaFamily.this, 0, 1, 0, 1, 0, periods0);
            installIOReg("ASSR", new ASSRRegister());
        }

        // See pg. 104 of the ATmega128 doc
        protected class ASSRRegister extends RWRegister {
            static final int AS0 = 3;
            static final int TCN0UB = 2;
            static final int OCR0UB = 1;
            static final int TCR0UB = 0;

            public void write(byte val) {
                super.write((byte)(0xf & val));
                decode(val);
            }

            public void writeBit(int bit, boolean val) {
                super.writeBit(bit, val);
                decode(value);
            }

            protected void decode(byte val) {
                // TODO: if there is a change, remove ticker and requeue?
                timerClock = Arithmetic.getBit(val, AS0) ? externalClock : mainClock;
            }


        }


    }

    protected static final int[] periods2 = {0, 1, 8, 64, 256, 1024};

    /**
     * <code>Timer2</code> is an additional 8-bit timer on the ATMega128. It is not available in
     * ATMega103 compatibility mode.
     */
    protected class Timer2 extends Timer8Bit {
        protected Timer2() {
            super(ATMegaFamily.this, 2, 7, 6, 7, 6, periods2);
        }
    }

    protected static final int[] periods1 = new int[]{0, 1, 8, 64, 256, 1024};

    /**
     * <code>Timer1</code> is a 16-bit timer available on the ATMega128.
     */
    protected class Timer1 extends Timer16Bit {

        protected void initValues() {
            // bit numbers

            OCIEnA = 4;
            OCIEnB = 3;
            OCIEnC = 0; // on ETIMSK
            TOIEn = 2;
            TOVn = 2;
            OCFnA = 4;
            OCFnB = 3;
            OCFnC = 0; // on ETIFR
            ICFn = 5;
            periods = periods1;
        }

        protected Timer1() {
            super(1, ATMegaFamily.this);
            xTIFR_reg = TIFR_reg;
            xTIMSK_reg = TIMSK_reg;
        }

    }

    protected static final int[] periods3 = {0, 1, 8, 64, 256, 1024};

    /**
     * <code>Timer3</code> is an additional 16-bit timer available on the ATMega128, but not in ATMega103
     * compatability mode.
     */
    protected class Timer3 extends Timer16Bit {

        protected void initValues() {
            // bit numbers
            OCIEnA = 4;
            OCIEnB = 3;
            OCIEnC = 1; // on ETIMSK
            TOIEn = 2;
            TOVn = 2;
            OCFnA = 4;
            OCFnB = 3;
            OCFnC = 1; // on ETIFR
            ICFn = 5;

            periods = periods3;
        }

        protected Timer3() {
            super(3, ATMegaFamily.this);
            xTIFR_reg = ETIFR_reg;
            xTIMSK_reg = ETIMSK_reg;
        }

    }

    protected static final int[] UCSR0A_mapping = {-1, -1, -1, -1, -1, 20, 21, 19};
    // TODO: test these interrupt mappings--not sure if they are correct!
    protected static final int[] UCSR1A_mapping = {-1, -1, -1, -1, -1, 32, 33, 31};

    /**
     * Emulates the behavior of USART0 on the ATMega128 microcontroller.
     */
    protected class USART0 extends USART {

        USART0() {
            super(0, ATMegaFamily.this);
        }

        protected void initValues() {
            USARTnRX = 19;
            USARTnUDRE = 20;
            USARTnTX = 21;

            INTERRUPT_MAPPING = UCSR0A_mapping;

        }
    }

    /**
     * Emulates the behavior of USART1 on the ATMega128 microcontroller.
     */
    protected class USART1 extends USART {

        USART1() {
            super(1, ATMegaFamily.this);
        }

        protected void initValues() {
            USARTnRX = 31;
            USARTnUDRE = 32;
            USARTnTX = 33;

            INTERRUPT_MAPPING = UCSR1A_mapping;
        }
    }

    /**
     * The <code>buildPort()</code> method builds the IO registers corresponding to a general purpose IO port.
     * These ports are named A-G, and each consist of a PORT register (for writing), a PIN register (for reading),
     * and a direction register for setting whether each pin in the port is input or output. This method
     * is a utility to build these registers for each port given the last character of the name (e.g. 'A' in
     * PORTA).
     * @param p the last character of the port name
     */
    protected void buildPort(char p) {
        ATMegaFamily.Pin[] portPins = new ATMegaFamily.Pin[8];
        for (int cntr = 0; cntr < 8; cntr++)
            portPins[cntr] = (ATMegaFamily.Pin)getPin("P" + p + cntr);
        installIOReg("PORT"+p, new PortRegister(portPins));
        installIOReg("DDR"+p, new DirectionRegister(portPins));
        installIOReg("PIN"+p, new PinRegister(portPins));
    }

    /**
     * The <code>buildInterruptRange()</code> method creates the IO registers and <code>MaskableInterrupt</code>
     * instances corresponding to a complete range of interrupts.
     * @param increasing a flag indicating that the vector numbers increase with bit number of the IO register
     * @param maskRegNum the IO register number of the mask register
     * @param flagRegNum the IO register number of the flag register
     * @param baseVect the beginning vector of this range of interrupts
     * @param numVects the number of vectors in this range
     * @return a flag register that corresponds to the interrupt range
     */
    protected FlagRegister buildInterruptRange(boolean increasing, String maskRegNum, String flagRegNum, int baseVect, int numVects) {
        int[] mapping = new int[8];
        if (increasing) {
            for (int cntr = 0; cntr < 8; cntr++) mapping[cntr] = baseVect + cntr;
        } else {
            for (int cntr = 0; cntr < 8; cntr++) mapping[cntr] = baseVect - cntr;
        }

        FlagRegister fr = new FlagRegister(interpreter, mapping);
        for (int cntr = 0; cntr < numVects; cntr++) {
            int inum = increasing ? baseVect + cntr : baseVect - cntr;
            installInterrupt("", inum, new MaskableInterrupt(inum, fr.maskRegister, fr, cntr, false));
            installIOReg(maskRegNum, fr.maskRegister);
            installIOReg(flagRegNum, fr);
        }
        return fr;
    }


    protected FlagRegister EIFR_reg;
    protected MaskRegister EIMSK_reg;

    protected FlagRegister TIFR_reg;
    protected MaskRegister TIMSK_reg;

    protected FlagRegister ETIFR_reg;
    protected MaskRegister ETIMSK_reg;

    /**
     * The getEIFR_reg() method is used to access the external interrupt flag register.
     * @return the <code>ActiveRegister</code> object corresponding to the EIFR IO register
     */
    public FlagRegister getEIFR_reg() {
        return EIFR_reg;
    }

}
