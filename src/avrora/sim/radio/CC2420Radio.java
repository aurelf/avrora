/**
 * Copyright (c) 2007, Regents of the University of California
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
 * Created Oct 10, 2007
 */
package avrora.sim.radio;

import avrora.sim.mcu.*;
import avrora.sim.Simulator;
import avrora.sim.state.ByteFIFO;
import cck.util.Arithmetic;
import cck.util.Util;

/**
 * The <code>CC2420Radio</code> implements a simulation of the CC2420 radio
 * chip.
 *
 * @author Ben L. Titzer
 */
public class CC2420Radio {

    //-- Register addresses ---------------------------------------------------
    public static final int MAIN     = 0x10;
    public static final int MDMCTRL0 = 0x11;
    public static final int MDMCTRL1 = 0x12;
    public static final int RSSI     = 0x13;
    public static final int SYNCWORD = 0x14;
    public static final int TXCTRL   = 0x15;
    public static final int RXCTRL0  = 0x16;
    public static final int RXCTRL1  = 0x17;
    public static final int FSCTRL   = 0x18;
    public static final int SECCTRL0 = 0x19;
    public static final int SECCTRL1 = 0x1a;
    public static final int BATTMON  = 0x1b;
    public static final int IOCFG0   = 0x1c;
    public static final int IOCFG1   = 0x1d;
    public static final int MANFIDL  = 0x1e;
    public static final int MANFIDH  = 0x1f;
    public static final int FSMTC    = 0x20;
    public static final int MANAND   = 0x21;
    public static final int MANOR    = 0x22;
    public static final int AGCCTRL0 = 0x23;
    public static final int AGCTST0  = 0x24;
    public static final int AGCTST1  = 0x25;
    public static final int AGCTST2  = 0x26;
    public static final int FSTST0   = 0x27;
    public static final int FSTST1   = 0x28;
    public static final int FSTST2   = 0x29;
    public static final int FSTST3   = 0x2a;
    public static final int RXBPFTST = 0x2b;
    public static final int FSMSTATE = 0x2c;
    public static final int ADCTST   = 0x2d;
    public static final int DACTST   = 0x2e;
    public static final int TOPTST   = 0x2f;
    public static final int TXFIFO   = 0x3e;
    public static final int RXFIFO   = 0x3f;

    //-- Command strobes ---------------------------------------------------
    public static final int SNOP     = 0x00;
    public static final int SXOSCON  = 0x01;
    public static final int STXCAL   = 0x02;
    public static final int SRXON    = 0x03;
    public static final int STXON    = 0x04;
    public static final int STXONCCA = 0x05;
    public static final int SRFOFF   = 0x06;
    public static final int SXOSCOFF = 0x07;
    public static final int SLFUSHRX = 0x08;
    public static final int SFLUSHTX = 0x09;
    public static final int SACK     = 0x0a;
    public static final int SACKPEND = 0x0b;
    public static final int SRXDEC   = 0x0c;
    public static final int STXENC   = 0x0d;
    public static final int SAES     = 0x0e;

    //-- Other constants --------------------------------------------------
    private static final int NUM_REGISTERS = 0x40;
    private static final int FIFO_SIZE     = 128;

    //-- Simulation objects -----------------------------------------------
    protected final Microcontroller mcu;
    protected final Simulator sim;
    protected RadioAir air;

    //-- Radio state ------------------------------------------------------
    protected final int xfreq;
    protected final char[] registers = new char[NUM_REGISTERS];
    protected final ByteFIFO txFIFO = new ByteFIFO(FIFO_SIZE);
    protected final ByteFIFO rxFIFO = new ByteFIFO(FIFO_SIZE);

    protected Transmitter transmitter;
    protected Receiver receiver;

    //-- State needed for config protocol ---------------------------------
    protected boolean configActive;
    protected int configCommand;
    protected int configByteCnt;
    protected int configRegAddr;
    protected byte configByteHigh;
    protected int configRAMAddr;
    protected int configRAMBank;

    //-- Strobes and status ----------------------------------------------
    protected byte status;
    protected boolean SXOSCON_switched;
    protected boolean SXOSCOFF_switched;
    protected boolean SRXDEC_switched;
    protected boolean STXENC_switched;
    protected boolean STXCAL_switched;
    protected boolean SRXON_switched;
    protected boolean STXON_switched;
    protected boolean SRFOFF_switched;
    protected boolean STXONCCA_switched;

    //-- Pins ------------------------------------------------------------
    public final CC2420Pin SCLK_pin  = new CC2420Pin();
    public final CC2420Pin MISO_pin  = new CC2420Pin();
    public final CC2420Pin MOSI_pin  = new CC2420Pin();
    public final CC2420Pin CS_pin    = new CC2420Pin();
    public final CC2420Pin FIFO_pin  = new CC2420Pin();
    public final CC2420Pin FIFOP_pin = new CC2420Pin();
    public final CC2420Pin CCA_pin   = new CC2420Pin();
    public final CC2420Pin SFD_pin   = new CC2420Pin();

    public final SPIInterface spiInterface = new SPIInterface();
    public final ADCInterface adcInterface = new ADCInterface();

    /**
     * The constructor for the CC2420 class creates a new instance connected
     * to the specified microcontroller with the given external clock frequency.
     * @param mcu the microcontroller unit to which this radio is attached
     * @param xfreq the external clock frequency supplied to the CC2420 radio chip
     */
    public CC2420Radio(Microcontroller mcu, int xfreq) {
        // set up references to MCU and simulator
        this.mcu = mcu;
        this.sim = mcu.getSimulator();
        this.xfreq = xfreq;

        // reset all registers
        for ( int cntr = 0; cntr < NUM_REGISTERS; cntr++ ) resetRegister(cntr);

    }

    /**
     * The <code>readRegister()</code> method reads the value from the specified register
     * and takes any action(s) that are necessary for the specific register.
     * @param addr the address of the register
     * @return an integer value representing the result of reading the register
     */
    int readRegister(int addr) {
        return (int) registers[addr];
    }

    /**
     * The <code>writeRegister()</code> method writes the specified value to the specified
     * register, taking any action(s) necessary and activating any command strobes as
     * required.
     * @param addr the address of the register
     * @param val the value to write to the specified register
     */
    void writeRegister(int addr, int val) {
        registers[addr] = (char)val;
        switch (addr) {
            case MAIN:
                // TODO: main register write
                break;
        }
        computeStatus();
    }

    void strobe(int addr) {
        switch (addr) {
            case SNOP:
                break;
            case SXOSCON:
                SXOSCON_switched = true;
                SXOSCOFF_switched = false;
                break;
            case STXCAL:
                STXCAL_switched = true;
                SRXON_switched = false;
                STXON_switched = false;
                break;
            case SRXON:
                endTransmit();
                beginReceive();
                SRXON_switched = true;
                STXCAL_switched = false;
                SRFOFF_switched = false;
                SXOSCOFF_switched = false;
                break;
            case STXONCCA:
                STXONCCA_switched = true;
                // fall through!
            case STXON:
                endReceive();
                beginTransmit();
                STXON_switched = true;
                STXCAL_switched = false;
                SRFOFF_switched = false;
                SXOSCOFF_switched = false;
                break;
            case SRFOFF:
                SRFOFF_switched = true;
                SRXON_switched = false;
                STXON_switched = false;
                STXONCCA_switched = false;
                STXCAL_switched = false;
                break;
            case SXOSCOFF:
                SXOSCOFF_switched = true;
                SXOSCON_switched = false;
                break;
            case SLFUSHRX:
                rxFIFO.clear();
                FIFO_pin.level = false;
                FIFOP_pin.level = false;
                break;
            case SFLUSHTX:
                txFIFO.clear();
                break;
            case SACK:
                // TODO: send acknowledging frame
                break;
            case SACKPEND:
                // TODO: send acknowledging frame with pending set
                break;
            case SRXDEC:
                // start RXFIFO in-line decryption/authentication as set by SPI_SEC_MODE
                break;
            case STXENC:
                // start TXFIFO in-line encryption/authentication as set by SPI_SEC_MODE
                break;
            case SAES:
                // SPI_SEC_MODE is not required to be 0, but the encrypt. module must be idle; else strobe is ignored
                break;
        }
    }

    private void beginTransmit() {
        if ( transmitter != null ) transmitter.beginTransmit(0.00);
    }

    private void endTransmit() {
        if ( transmitter != null ) transmitter.endTransmit();
    }

    private void beginReceive() {
        if ( receiver != null ) receiver.beginReceive();
    }

    private void endReceive() {
        if ( receiver != null ) receiver.endReceive();
    }

    /**
     * The <code>resetRegister()</code> method resets the specified register's value
     * to its default.
     * @param addr the address of the register to reset
     */
    void resetRegister(int addr) {
        char val = 0x0000;
        switch (addr) {
            case MAIN: val = 0xf800; break;
            case MDMCTRL0: val = 0x0ae2; break;
            case SYNCWORD: val = 0xa70f; break;
            case TXCTRL: val = 0xa0ff; break;
            case RXCTRL0: val = 0x12e5; break;
            case RXCTRL1: val = 0x0a56; break;
            case FSCTRL: val = 0x4165; break;
            case IOCFG0: val = 0x0040; break;
        }
        registers[addr] = val;
    }

    /**
     * The <code>computeStatus()</code> method computes the status byte of the radio.
     */
    void computeStatus() {
        boolean XOSC16_M = SXOSCON_switched && !SXOSCOFF_switched;
        boolean TX_UNDERFLOW = false; // TODO: compute underflow
        /*something describing length field*/
        boolean ENC_BUSY = SRXDEC_switched || STXENC_switched;
        boolean TX_ACTIVE = SRXON_switched || STXON_switched;
        boolean LOCK = false; // TODO: compute lock
        boolean RSSI_VALID = (Arithmetic.low(readRegister(RSSI)) != -128);
        //bits 7 and 0 are reserved bits in the Status Byte
        status = Arithmetic.packBits(false, RSSI_VALID, LOCK, TX_ACTIVE, ENC_BUSY, TX_UNDERFLOW, XOSC16_M, false);
    }

    protected static final int CMD_R_REG = 0;
    protected static final int CMD_W_REG = 1;
    protected static final int CMD_R_RX = 2;
    protected static final int CMD_W_RX = 3;
    protected static final int CMD_R_TX = 4;
    protected static final int CMD_W_TX = 5;
    protected static final int CMD_R_RAM = 6;
    protected static final int CMD_W_RAM = 7;

    protected byte receiveConfigByte(byte val) {
        configByteCnt++;
        if ( configByteCnt == 1 ) {
            // the first byte is the address byte
            boolean ramop = Arithmetic.getBit(val, 7);
            boolean readop = Arithmetic.getBit(val, 6);
            configRegAddr = val & 0x3f;
            configRAMAddr = val & 0x7f;
            computeStatus();
            if ( configRegAddr <= 15 ) {
                // execute the command strobe
                strobe(configRegAddr);
                configByteCnt = 0;
            } else {
                if ( ramop ) configCommand = CMD_R_RAM;
                else if ( configRegAddr == TXFIFO ) configCommand = readop ? CMD_R_TX : CMD_W_TX;
                else if ( configRegAddr == RXFIFO ) configCommand = readop ? CMD_R_RX : CMD_W_RX;
                else configCommand = readop ? CMD_R_REG : CMD_W_REG;
            }
            return status;
        } else if ( configByteCnt == 2 ) {
            // the second byte is the MSB for a write, unused for read
            switch (configCommand) {
                case CMD_R_REG: return Arithmetic.high(readRegister(configRegAddr));
                case CMD_W_REG: configByteHigh = val; return 0;
                case CMD_R_TX: return readFIFO(txFIFO);
                case CMD_R_RX: return readFIFO(rxFIFO);
                case CMD_W_TX: return writeFIFO(txFIFO, val, true);
                case CMD_W_RX: return writeFIFO(rxFIFO, val, false);
                case CMD_R_RAM:
                    configRAMBank = (val >> 6) & 0x3;
                    if ( Arithmetic.getBit(val, 5)) configCommand = CMD_R_RAM;
                    else configCommand = CMD_W_RAM;
                    return 0;
            }
        } else {
            // the third byte completes a read or write register
            // while subsequent bytes are valid for fifo and RAM accesses
            switch (configCommand) {
                case CMD_R_REG:
                    configByteCnt = 0;
                    return Arithmetic.low(readRegister(configRegAddr));
                case CMD_W_REG:
                    configByteCnt = 0;
                    writeRegister(configRegAddr, Arithmetic.word(val, configByteHigh));
                    return 0;
                case CMD_R_TX: return readFIFO(txFIFO);
                case CMD_R_RX: return readFIFO(rxFIFO);
                case CMD_W_TX: return writeFIFO(txFIFO, val, true);
                case CMD_W_RX: return writeFIFO(rxFIFO, val, false);
                case CMD_R_RAM:
                    if ( configRAMBank == 0x00 ) return txFIFO.peek(configRAMAddr);
                    else if ( configRAMBank == 0x01 ) return rxFIFO.peek(configRAMAddr);
                    // TODO: security bank not implemented.
                    return 0;
                case CMD_W_RAM:
                    if ( configRAMBank == 0x00 ) return txFIFO.poke(configRAMAddr, val);
                    else if ( configRAMBank == 0x01 ) return rxFIFO.poke(configRAMAddr, val);
                    // TODO: security bank not implemented.
                    return 0;
            }
        }
        return 0;
    }

    protected byte readFIFO(ByteFIFO fifo) {
        return fifo.remove();
    }

    protected byte writeFIFO(ByteFIFO fifo, byte val, boolean st) {
        byte result = st ? status : 0;
        fifo.add(val);
        computeStatus();
        return result;
    }

    public Simulator getSimulator() {
        return sim;
    }

    public int getPower() {
        return readRegister(TXCTRL) & 0x1f;
    }

    public double getFrequency() {
        return (double)(2048 + readRegister(FSCTRL) & 0x03ff);
    }

    public RadioAir getAir() {
        return air;
    }

    public void setAir(RadioAir nair) {
        air = nair;
    }

    public class SPIInterface implements SPIDevice {

        byte result;

        public SPI.Frame transmitFrame() {
            return SPI.newFrame(result);
        }

        public void receiveFrame(SPI.Frame frame) {
            if ( configActive ) result = receiveConfigByte(frame.data);
        }

        public void connect(SPIDevice d) {
            // do nothing.
        }
    }

    public class ADCInterface implements ADC.ADCInput {

        public float getVoltage() {
            throw Util.unimplemented();
        }
    }

    private void pinChange_CS(boolean level) {
        if ( level ) {
            // end configuration transfer
            configActive = false;
            configByteCnt = 0;
        } else {
            // start configuration transfer
            configActive = true;
            configByteCnt = 0;
        }
    }

    public class Transmitter extends Medium.Transmitter {

        public Transmitter(Medium m) {
            super(m, sim.getClock());
        }

        public byte nextByte() {
            // TODO: implement sending.
            return 0;
        }
    }

    public class Receiver extends Medium.Receiver {
        public Receiver(Medium m) {
            super(m, sim.getClock());
        }
        public void nextByte(byte b) {
            // TODO: implement byte reception
        }
    }

    public void connectTo(Medium m) {
        transmitter = new Transmitter(m);
        receiver = new Receiver(m);
    }

    /**
     * The <code>CC2420Pin</code>() class models pins that are inputs and outputs to the CC2420 chip.
     */
    public class CC2420Pin implements Microcontroller.Pin.Input, Microcontroller.Pin.Output {
        protected boolean prev;
        protected boolean level;

        public void write(boolean level) {
            if ( this.level != level ) {
                // level changed
                this.prev = this.level;
                this.level = level;
                if ( this == CS_pin ) pinChange_CS(level);
            }
        }

        public boolean read() {
            return level;
        }
    }
}
