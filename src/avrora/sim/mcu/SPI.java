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
import cck.text.StringUtil;
import cck.util.Arithmetic;

/**
 * Serial Peripheral Interface. Used on the <code>Mica2</code> platform for radio communication.
 *
 * @author Daniel Lee, Simon Han
 */
public class SPI extends AtmelInternalDevice implements SPIDevice, InterruptTable.Notification {

    // TODO: unpost SPI interrupt when fired

    final SPDReg SPDR_reg;
    final SPCRReg SPCR_reg;
    final SPSReg SPSR_reg;

    SPIDevice connectedDevice;

    final TransmitReceive transmitReceive = new TransmitReceive();

    int SPR;
    boolean SPI2x;
    boolean master;
    boolean SPIenabled;

    final int interruptNum = 18;

    protected int period;

    /**
     * A single byte data frame for the SPI.
     */
    public static class Frame {
        public final byte data;

        protected Frame(byte data) {
            this.data = data;
        }
    }

    private final static Frame[] frames = new Frame[256];
    public final static Frame ZERO_FRAME;
    public final static Frame FF_FRAME;

    static {
        for ( int cntr = 0; cntr < 256; cntr++ )
            frames[cntr] = new Frame((byte)cntr);
        ZERO_FRAME = frames[0];
        FF_FRAME = frames[0xff];
    }

    public static Frame newFrame(byte data) {
        return frames[data & 0xff];
    }

    public void connect(SPIDevice d) {
        connectedDevice = d;
    }

    public void receiveFrame(Frame frame) {
        SPDR_reg.receiveReg.write(frame.data);
        if (!master && !transmitReceive.transmitting) SPSR_reg.writeBit(7, true); // flag interrupt

    }

    public Frame transmitFrame() {
        return newFrame(SPDR_reg.transmitReg.read());
    }

    public SPI(AtmelMicrocontroller m) {
        super("spi", m);
        SPDR_reg = new SPDReg();
        SPCR_reg = new SPCRReg();
        SPSR_reg = new SPSReg();

        installIOReg("SPDR", SPDR_reg);
        installIOReg("SPSR", SPSR_reg);
        installIOReg("SPCR", SPCR_reg);

        interpreter.getInterruptTable().registerInternalNotification(this, interruptNum);
    }

    /**
     * Post SPI interrupt
     */
    private void postSPIInterrupt() {
        interpreter.setPosted(interruptNum, true);
    }

    private void unpostSPIInterrupt() {
        interpreter.setPosted(interruptNum, false);
    }

    private void calculatePeriod() {
        int divider = 0;

        switch (SPR) {
            case 0:
                divider = 4;
                break;
            case 1:
                divider = 16;
                break;
            case 2:
                divider = 64;
                break;
            case 3:
                divider = 128;
                break;
        }

        if (SPI2x) {
            divider /= 2;
        }

        period = divider * 8;
    }


    /**
     * The SPI transfer event.
     */
    protected class TransmitReceive implements Simulator.Event {

        Frame myFrame;
        Frame connectedFrame;
        boolean transmitting;

        protected void enableTransfer() {

            if (master && SPIenabled && !transmitting) {
                if (devicePrinter.enabled) {
                    devicePrinter.println("SPI: Master mode. Enabling transfer. ");
                }
                transmitting = true;
                myFrame = transmitFrame();
                connectedFrame = connectedDevice.transmitFrame();
                mainClock.insertEvent(this, period);
            }
        }


        /**
         * Notes. The way this delay is setup right now, when the ATMega128 is in master mode and
         * transmits, the connected device has a delayed receive. For the radio, this is not a
         * problem, as the radio is the master and is responsible for ensuring correct delivery time
         * for the SPI.
         */
        public void fire() {
            if (SPIenabled) {
                connectedDevice.receiveFrame(myFrame);
                receiveFrame(connectedFrame);
                transmitting = false;
                postSPIInterrupt();
            }
        }
    }

    public void force(int inum) {
        // TODO: set SPIF
    }

    public void invoke(int inum) {
        unpostSPIInterrupt();
        SPSR_reg.clearSPIF();
    }

    /**
     * SPI data register. Writes to this register are transmitted to the connected device and reads
     * from the register read the data received from the connected device.
     */
    class SPDReg implements ActiveRegister {

        protected final ReceiveRegister receiveReg;
        protected final TransmitRegister transmitReg;

        protected class ReceiveRegister extends RWRegister {

            // TODO: there is no need for a special class to print out debugging information
            public byte read() {
                byte val = super.read();
                if (devicePrinter.enabled)
                    devicePrinter.println("SPI: read " + StringUtil.toMultirepString(val, 8) + " from SPDR ");
                return val;

            }

            public boolean readBit(int bit) {
                if (devicePrinter.enabled)
                    devicePrinter.println("SPI: read bit " + bit + " from SPDR");
                return super.readBit(bit);
            }
        }

        protected class TransmitRegister extends RWRegister {

            byte oldData;

            public void write(byte val) {
                if (devicePrinter.enabled && oldData != val)
                    devicePrinter.println("SPI: wrote " + StringUtil.toMultirepString(val, 8) + " to SPDR");
                super.write(val);
                oldData = val;

                // the enableTransfer method has the necessary checks to make sure a transfer at this point
                // is necessary
                transmitReceive.enableTransfer();

            }

            public void writeBit(int bit, boolean val) {
                if (devicePrinter.enabled)
                    devicePrinter.println("SPI: wrote " + val + " to SPDR, bit " + bit);
                super.writeBit(bit, val);
                transmitReceive.enableTransfer();
            }

        }

        SPDReg() {

            receiveReg = new ReceiveRegister();
            transmitReg = new TransmitRegister();
        }

        /**
         * The <code>read()</code> method
         *
         * @return the value from the receive buffer
         */
        public byte read() {
            return receiveReg.read();
        }

        /**
         * The <code>write()</code> method
         *
         * @param val the value to transmit buffer
         */
        public void write(byte val) {
            transmitReg.write(val);
        }

        /**
         * The <code>readBit()</code> method
         *
         * @param num
         * @return false
         */
        public boolean readBit(int num) {
            return receiveReg.readBit(num);

        }

        /**
         * The <code>writeBit()</code>
         *
         * @param num
         */
        public void writeBit(int num, boolean val) {
            transmitReg.writeBit(num, val);

        }
    }

    /**
     * SPI control register.
     */
    protected class SPCRReg extends RWRegister {

        static final int SPIE = 7;
        static final int SPE = 6;
        static final int DORD = 5; // does not really matter, because we are fastforwarding data
        static final int MSTR = 4;
        static final int CPOL = 3; // does not really matter, because we are fastforwarding data
        static final int CPHA = 2; // does not really matter, because we are fastforwarding data
        static final int SPR1 = 1;
        static final int SPR0 = 0;
        //OL: remember old state of spi enable bit
        boolean SPIEnable = false;

        public void write(byte val) {
            if (devicePrinter.enabled)
                devicePrinter.println("SPI: wrote " + StringUtil.toMultirepString(val, 8) + " to SPCR");
            super.write(val);
            decode(val);

        }

        public void writeBit(int bit, boolean val) {
            if (devicePrinter.enabled)
                devicePrinter.println("SPI: wrote " + val + " to SPCR, bit " + bit);
            super.writeBit(bit, val);
            decode(value);
        }

        protected void decode(byte val) {

            SPIenabled = Arithmetic.getBit(val, SPE);

            //OL: reset spi interrupt flag, when enabling SPI
            //this is not described in the Atmega128 handbook
            //however, the chip seems to work like this, as S-Mac
            //does not work without it
            boolean spie = Arithmetic.getBit(val, SPIE);
            interpreter.setEnabled(interruptNum, spie);
            if (spie && !SPIEnable) {
                SPIEnable = true;
                SPSR_reg.writeBit(SPSReg.SPIF, false);
            }
            if (!spie && SPIEnable)
                SPIEnable = false;
            //end OL

            boolean oldMaster = master;
            master = Arithmetic.getBit(val, MSTR);

            SPR = 0;
            SPR |= Arithmetic.getBit(val, SPR1) ? 0x02 : 0;
            SPR |= Arithmetic.getBit(val, SPR0) ? 0x01 : 0;
            calculatePeriod();

            if (!oldMaster && master) {
                transmitReceive.enableTransfer();
            }
        }

    }

    /**
     * SPI status register.
     */
    class SPSReg extends RWRegister {
        // TODO: implement write collision
        // TODO: finish implementing interrupt

        static final int SPIF = 7;
        static final int WCOL = 6;

        public void write(byte val) {
            if (devicePrinter.enabled)
                devicePrinter.println("SPI: wrote " + val + " to SPSR");
            super.write(val);
            decode(val);
        }

        public void writeBit(int bit, boolean val) {
            if (devicePrinter.enabled)
                devicePrinter.println("SPI: wrote " + val + " to SPSR " + bit);
            super.writeBit(bit, val);
            decode(value);
        }

        byte oldVal;

        protected void decode(byte val) {

            if (!Arithmetic.getBit(oldVal, SPIF) && Arithmetic.getBit(val, SPIF)) {
                postSPIInterrupt();
            }
            // TODO: handle write collisions

            SPI2x = Arithmetic.getBit(value, 0);
            oldVal = val;
        }

        public void setSPIF() {
            writeBit(SPIF, true);
        }

        public void clearSPIF() {
            writeBit(SPIF, false);
        }

        public boolean getSPIF() {
            return readBit(SPIF);
        }

    }


}
