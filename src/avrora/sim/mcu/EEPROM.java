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

import avrora.sim.RWRegister;
import avrora.sim.Simulator;
import cck.util.Arithmetic;

/**
 * This is an implementation of the non-volatile EEPROM on the ATMega128 microcontroller.
 *
 * @author Daniel Lee
 * @author Sascha Silbe
 */
public class EEPROM extends AtmelInternalDevice {

    final int EEPROM_SIZE, EEPROM_SIZE_numBits;

    public static final int EEARH = 0x1F;
    public static final int EEARL = 0x1E;
    public static final int EEDR = 0x1D;
    public static final int EECR = 0x1C;

    final byte[] EEPROM_data;
    final RWRegister EEDR_reg;
    final EECRReg EECR_reg;
    final RWRegister EEARL_reg;
    final EEARHReg EEARH_reg;

    // flag bits on EECR
    static final int EERIE = 3;
    static final int EEMWE = 2;
    static final int EEWE = 1;
    static final int EERE = 0;

    static final int EEPROM_INTERRUPT = 23;

    boolean interruptEnable;
    boolean masterWriteEnable;
    boolean writeEnable;
    boolean readEnable;

    final EEPROMTicker ticker;
    final EEPROMWriteFinishedEvent writeFinishedEvent;

    int writeCount = -1;
    boolean writeEnableWritten;
    boolean readEnableWritten;

    EEPROM(int size, AtmelMicrocontroller m) {
        super("eeprom", m);

        ticker = new EEPROMTicker();
        writeFinishedEvent = new EEPROMWriteFinishedEvent();

        EEDR_reg = new RWRegister();
        EECR_reg = new EECRReg();
        EEARL_reg = new EEARLReg();
        EEARH_reg = new EEARHReg();

        EEPROM_SIZE = size;
        EEPROM_SIZE_numBits = Arithmetic.log(size);
        //EEPROM_SIZE_numBits = new BigInteger(Integer.toString(size)).bitLength();
        EEPROM_data = new byte[EEPROM_SIZE];

        installIOReg("EEDR", EEDR_reg);
        installIOReg("EECR", EECR_reg);
        installIOReg("EEARL", EEARL_reg);
        installIOReg("EEARH", EEARH_reg);

    }

    public int getSize() {
        return EEPROM_SIZE;
    }

    public void setContent(byte[] contents) {
        for (int addr = 0; addr < contents.length; addr++) {
            EEPROM_data[addr] = contents[addr];
        }
        if (devicePrinter.enabled) devicePrinter.println("EEPROM: content set");
    }

    public byte[] getContent() {
        return EEPROM_data;
    }

    protected class EEARHReg extends RWRegister {

        public void write(byte val) {
            // EEAR access not allowed during write
            if (writeEnable) return;

            value = (byte)(val & ((EEPROM_SIZE >> 8) - 1));
        }

        public void writeBit(int bit, boolean val) {
            // EEAR access not allowed during write
            if (writeEnable) return;

            if (bit < (EEPROM_SIZE_numBits - 8)) {
                super.writeBit(bit, val);
            }
        }

    }

    protected class EEARLReg extends RWRegister {

        public void write(byte val) {
            // EEAR access not allowed during write
            if (writeEnable) return;

            value = (byte)(val & Math.min(EEPROM_SIZE - 1, 255));
        }

        public void writeBit(int bit, boolean val) {
            // EEAR access not allowed during write
            if (writeEnable) return;

            if (bit < EEPROM_SIZE_numBits) {
                super.writeBit(bit, val);
            }
        }

    }

    protected class EECRReg extends RWRegister {

        public void decode(byte val) {
            boolean readEnableOld = readEnable;
            readEnable = readBit(EERE);
            if (!readEnableOld && readEnable) {
                if (devicePrinter.enabled) devicePrinter.println("EEPROM: EERE flagged");
                readEnableWritten = true;
            }
            boolean writeEnableOld = writeEnable;
            writeEnable = readBit(EEWE);
            if (!writeEnableOld && writeEnable) {
                if (devicePrinter.enabled) devicePrinter.println("EEPROM: EEWE flagged");
                writeEnableWritten = true;
            }
            masterWriteEnable = readBit(EEMWE);
            interruptEnable = readBit(EERIE);
            interpreter.setEnabled(EEPROM_INTERRUPT, interruptEnable);
            interpreter.setPosted(EEPROM_INTERRUPT, !writeEnable);
            mainClock.insertEvent(ticker, 1);
        }

        public void write(byte val) {

            boolean masterWriteEnableOld = masterWriteEnable;
            value = (byte)(0xff & val);
            if (devicePrinter.enabled) devicePrinter.println("EEPROM: EECR written to, val = " + value);
            decode(value);
            if (!masterWriteEnableOld && masterWriteEnable) {
                // EEMWE has been written to. reset write count
                if (devicePrinter.enabled) devicePrinter.println("EEPROM: reset write count to 4");
                writeCount = 4;
            }
        }

        public void writeBit(int bit, boolean val) {
            boolean masterWriteEnableOld = masterWriteEnable;
            if (bit < 4) {
                super.writeBit(bit, val);
            }
            if (devicePrinter.enabled) devicePrinter.println("EEPROM: EECR written to, val = " + value);
            decode(value);
            if (!masterWriteEnableOld && masterWriteEnable) {
                // EEMWE has been written to. reset write count
                if (devicePrinter.enabled) devicePrinter.println("EEPROM: reset write count to 4");
                writeCount = 4;
            }
        }
    }

    protected class EEPROMTicker implements Simulator.Event {

        public void fire() {

            if (devicePrinter.enabled) {
                devicePrinter.println("Tick : " + writeCount);
            }

            int address = read16(EEARH_reg, EEARL_reg);
            if (writeCount > 0) {
                // if EEWE has been written to 1 within
                // 4 clock cycles, write data

                // after 4 cycles, clear this bit

                if (writeEnableWritten) {
                    // TODO: disallow EEPROM access during Flash write

                    if (devicePrinter.enabled)
                        devicePrinter.println("EEPROM: " + EEDR_reg.read() + " written to " + address);
                    EEPROM_data[address] = EEDR_reg.read();
                    // EEPROM write takes 8.5ms
                    mainClock.insertEvent(writeFinishedEvent, (long)(mainClock.getHZ() * 0.0085));
                    // CPU halts for 2 cycles
                    simulator.delay(2);
                }

            }
            // read not allowed while write is in progress
            if (readEnableWritten && !writeEnable) {
                // read
                if (devicePrinter.enabled)
                    devicePrinter.println("EEPROM: " + EEPROM_data[address] + " read from " + address);
                EEDR_reg.write(EEPROM_data[address]);
                // reset EERE
                EECR_reg.writeBit(EERE, false);
                // CPU halts for 4 cycles
                simulator.delay(4);
            }
            if (writeCount > 0) {
                writeCount--;
                mainClock.insertEvent(ticker, 1);
            }

            if (writeCount == 0) {
                // clear EEMWE
                if (devicePrinter.enabled) devicePrinter.println("EEPROM: write count hit 0, clearing EEMWE");
                writeCount--;
                EECR_reg.writeBit(EEMWE, false);
            }
            writeEnableWritten = false;
            readEnableWritten = false;
        }
    }

    protected class EEPROMWriteFinishedEvent implements Simulator.Event {

        public void fire() {
            if (devicePrinter.enabled) devicePrinter.println("EEPROM: write finished, clearing EEWE");
            EECR_reg.writeBit(EEWE, false);
        }
    }
}
