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

import avrora.sim.Simulator;
import avrora.sim.BaseInterpreter;
import avrora.sim.State;
import avrora.sim.Clock;
import avrora.util.Arithmetic;

/**
 * Serial Peripheral Interface. Used on the <code>Mica2</code> platform for radio communication.
 *
 * @author Daniel Lee, Simon Han
 */
public class SPI extends AtmelInternalDevice implements ATMega128L.SPIDevice {
    final SPDReg SPDR_reg;
    final SPCRReg SPCR_reg;
    final SPSReg SPSR_reg;
    final SPIInterrupt SPI_int;

    public final int SPDR = 0x0F;
    public final int SPSR = 0x0E;
    public final int SPCR = 0x0D;

    ATMega128L.SPIDevice connectedDevice;

    final TransmitReceive transmitReceive = new TransmitReceive();

    int SPR;
    boolean SPI2x;
    boolean master;
    boolean SPIenabled;

    protected int period;

    public void connect(ATMega128L.SPIDevice d) {
        connectedDevice = d;
    }

    public void receiveFrame(ATMega128L.SPIDevice.SPIFrame frame) {
        SPDR_reg.receiveReg.write(frame.data);
        if (!master && !transmitReceive.transmitting) SPSR_reg.writeBit(7, true); // flag interrupt

    }

    public ATMega128L.SPIDevice.SPIFrame transmitFrame() {
        return new ATMega128L.SPIDevice.SPIFrame(SPDR_reg.transmitReg.read());
    }

    public SPI(AtmelMicrocontroller m) {
        super("spi", m);
        SPDR_reg = new SPDReg();
        SPCR_reg = new SPCRReg();
        SPSR_reg = new SPSReg();
        SPI_int = new SPIInterrupt();

        // add SPI interrupt to simulator
        installInterrupt("SPI", 18, SPI_int);

        installIOReg("SPDR", SPDR, SPDR_reg);
        installIOReg("SPSR", SPSR, SPSR_reg);
        installIOReg("SBCR", SPCR, SPCR_reg);
    }

    /**
     * Post SPI interrupt
     */
    private void postSPIInterrupt() {
        if (SPCR_reg.readBit(7)) {
            interpreter.postInterrupt(18);
        }
    }

    private void unpostSPIInterrupt() {
        interpreter.unpostInterrupt(18);

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

        ATMega128L.SPIDevice.SPIFrame myFrame;
        ATMega128L.SPIDevice.SPIFrame connectedFrame;
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
         * Notes. The way this delay is setup right now, when the ATMega128L is in master mode and
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


    class SPIInterrupt implements Simulator.Interrupt {
        public void force() {
            postSPIInterrupt();
        }

        public void fire() {
            SPSR_reg.clearSPIF();

            // should this also unpost the interrupt?
            unpostSPIInterrupt();
        }
    }


    /**
     * SPI data register. Writes to this register are transmitted to the connected device and reads
     * from the register read the data received from the connected device.
     */
    class SPDReg implements State.IOReg {

        protected final ReceiveRegister receiveReg;
        protected final TransmitRegister transmitReg;

        protected class ReceiveRegister extends State.RWIOReg {

            public byte read() {
                byte val = super.read();
                if (devicePrinter.enabled)
                    devicePrinter.println("SPI: read " + hex(val) + " from SPDR ");
                return val;

            }

            public boolean readBit(int bit) {
                if (devicePrinter.enabled)
                    devicePrinter.println("SPI: read bit " + bit + " from SPDR");
                return super.readBit(bit);
            }
        }

        protected class TransmitRegister extends State.RWIOReg {

            byte oldData;

            public void write(byte val) {
                if (devicePrinter.enabled && oldData != val)
                    devicePrinter.println("SPI: wrote " + Integer.toHexString(0xff & val) + " to SPDR");
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
    protected class SPCRReg extends State.RWIOReg {

        final int SPIE = 7;
        final int SPE = 6;
        final int DORD = 5; // does not really matter, because we are fastforwarding data
        final int MSTR = 4;
        final int CPOL = 3; // does not really matter, because we are fastforwarding data
        final int CPHA = 2; // does not really matter, because we are fastforwarding data
        final int SPR1 = 1;
        final int SPR0 = 0;
        //OL: remember old state of spi enable bit
        boolean oldSpiEnable = false;

        public void write(byte val) {
            if (devicePrinter.enabled)
                devicePrinter.println("SPI: wrote " + hex(val) + " to SPCR");
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
            //this is not described in the Atmega128l handbook
            //however, the chip seems to work like this, as S-Mac
            //does not work without it
            if (Arithmetic.getBit(val, SPIE) && !oldSpiEnable) {
                oldSpiEnable = true;
                SPSR_reg.writeBit(SPSR_reg.SPI, false);
            }
            if (!Arithmetic.getBit(val, SPIE) && oldSpiEnable)
                oldSpiEnable = false;
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
    class SPSReg extends State.RWIOReg {
        // TODO: implement write collision
        // TODO: finish implementing interrupt

        final int SPI = 7;
        final int WCOL = 6;

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

            if (!Arithmetic.getBit(oldVal, SPI) && Arithmetic.getBit(val, SPI) && SPCR_reg.readBit(SPI)) {
                postSPIInterrupt();
            }
            // TODO: write COLlision

            SPI2x = Arithmetic.getBit(value, 0);
            oldVal = val;
        }

        public void setSPIF() {
            writeBit(SPI, true);
        }

        public void clearSPIF() {
            writeBit(SPI, false);
        }

        public boolean getSPIF() {
            return readBit(SPI);
        }

    }


}
