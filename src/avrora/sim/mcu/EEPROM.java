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

import avrora.sim.State;
import avrora.sim.Simulator;
import avrora.sim.RWRegister;

/**
 * This is an implementation of the non-volatile EEPROM on the ATMega128 microcontroller.
 *
 * @author Daniel Lee
 */
public class EEPROM extends AtmelInternalDevice {

    // TODO: CPU halting after EEPROM read/write reads/writes.
    final int EEPROM_SIZE;

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

    int writeCount = -1;
    boolean writeEnableWritten;
    boolean readEnableWritten;

    // at some point, we might want to add support for
    // initializing the EEPROM with a file or something
    // and possibly writing back out when the simulator exits
    // to emulate a real EEPROM
    EEPROM(int size, AtmelMicrocontroller m) {
        super("eeprom", m);

        ticker = new EEPROMTicker();

        EEDR_reg = new RWRegister();
        EECR_reg = new EECRReg();
        EEARL_reg = new RWRegister();
        EEARH_reg = new EEARHReg();

        EEPROM_SIZE = size;
        EEPROM_data = new byte[EEPROM_SIZE];

        installIOReg("EEDR", EEDR_reg);
        installIOReg("EECR", EECR_reg);
        installIOReg("EEARL", EEARL_reg);
        installIOReg("EEARH", EEARH_reg);

    }

    protected class EEARHReg extends RWRegister {
        public void write(byte val) {
            // TODO: this code has no effect!
            value = (byte)(0xff & val);
            mainClock.insertEvent(ticker, 1);
        }

        public void writeBit(int bit, boolean val) {
            if (bit < 4) {
                super.writeBit(bit, val);
            }
            mainClock.insertEvent(ticker, 1);
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
            if (!interruptEnable) {
                interpreter.unpostInterrupt(EEPROM_INTERRUPT);
            }


            if ((interruptEnable && !writeEnable)) {
                // post interrupt
                if (devicePrinter.enabled) devicePrinter.println("EEPROM: posting interrupt.");
                interpreter.postInterrupt(EEPROM_INTERRUPT);
            }
            mainClock.insertEvent(ticker, 1);
        }

        public void write(byte val) {

            boolean masterWriteEnableOld = masterWriteEnable;
            value = (byte)(0xff & val);
            if (devicePrinter.enabled) devicePrinter.println("EEPROM: EECR written to, val = " + value);
            decode(value);
            if (!masterWriteEnableOld && masterWriteEnable) {
                // EEWE has been written to. reset write count
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
                // EEWE has been written to. reset write count
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
                // if EEMWE has been written to 1 within
                // 4 clock cycles, write data

                // after 4 cycles, clear this bit
                // implement blocking CPU

                if (writeEnableWritten) {
                    if (devicePrinter.enabled) devicePrinter.println("EEPROM: " + EEDR_reg.read() + " written to " + address);
                    EEPROM_data[address] = EEDR_reg.read();
                }

            }
            if (readEnableWritten) {
                // read
                // implement blocking CPU
                if (devicePrinter.enabled) devicePrinter.println("EEPROM: " + EEPROM_data[address] + " read from " + address);
                EEDR_reg.write(EEPROM_data[address]);

            }
            if (writeCount > 0) {
                writeCount--;
                mainClock.insertEvent(ticker, 1);
            }

            if (writeCount == 0) {
                // clear EEWE
                if (devicePrinter.enabled) devicePrinter.println("EEPROM: write count hit 0, clearing EEWE");
                writeCount--;
                EECR_reg.writeBit(1, false);
            }
            writeEnableWritten = false;
            readEnableWritten = false;
        }
    }
}
