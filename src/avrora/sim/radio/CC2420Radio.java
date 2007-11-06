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
import avrora.sim.clock.Synchronizer;
import avrora.sim.util.SimUtil;
import avrora.sim.output.SimPrinter;
import avrora.sim.state.*;
import cck.util.Arithmetic;
import cck.util.Util;
import cck.text.StringUtil;

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
    public static final int SFLUSHRX = 0x08;
    public static final int SFLUSHTX = 0x09;
    public static final int SACK     = 0x0a;
    public static final int SACKPEND = 0x0b;
    public static final int SRXDEC   = 0x0c;
    public static final int STXENC   = 0x0d;
    public static final int SAES     = 0x0e;

    //-- Other constants --------------------------------------------------
    private static final int NUM_REGISTERS = 0x40;
    private static final int FIFO_SIZE     = 128;

    private static final int XOSC_START_TIME = 1000; // oscillator start time

    //-- Simulation objects -----------------------------------------------
    protected final Microcontroller mcu;
    protected final Simulator sim;
    protected RadioAir air;

    //-- Radio state ------------------------------------------------------
    protected final int xfreq;
    protected final char[] registers = new char[NUM_REGISTERS];
    protected final ByteFIFO txFIFO = new ByteFIFO(FIFO_SIZE);
    protected final ByteFIFO rxFIFO = new ByteFIFO(FIFO_SIZE);

    protected Medium medium;
    protected Transmitter transmitter;
    protected Receiver receiver;

    //-- state for managing configuration information
    protected int configCommand;
    protected int configByteCnt;
    protected int configRegAddr;
    protected byte configByteHigh;
    protected int configRAMAddr;
    protected int configRAMBank;

    //-- Strobes and status ----------------------------------------------
    // note that there is no actual "status register" on the CC2420.
    // The register here is used in the simulation implementation to
    // simplify the handling of radio states and state transitions.
    protected final Register statusRegister = new Register(8);
    protected boolean startingOscillator;
    protected boolean SRXDEC_switched;
    protected boolean STXENC_switched;

    //-- Views of bits in the status "register" ---------------------------
    protected final BooleanView oscStable = RegisterUtil.booleanView(statusRegister, 6);
    protected final BooleanView txUnderflow = RegisterUtil.booleanView(statusRegister, 5);
    protected final BooleanView txActive = RegisterUtil.booleanView(statusRegister, 3);
    protected final BooleanView signalLock = RegisterUtil.booleanView(statusRegister, 2);
    protected final BooleanView rssiValid = RegisterUtil.booleanView(statusRegister, 1);

    protected final BooleanView CCA_assessor = new ClearChannelAssessor();
    protected BooleanView SFD_value = new BooleanRegister();

    //-- Pins ------------------------------------------------------------
    public final CC2420Pin SCLK_pin  = new CC2420Pin("SCLK");
    public final CC2420Pin MISO_pin  = new CC2420Pin("MISO");
    public final CC2420Pin MOSI_pin  = new CC2420Pin("MOSI");
    public final CC2420Pin CS_pin    = new CC2420Pin("CS");
    public final CC2420Output FIFO_pin  = new CC2420Output("FIFO", new BooleanRegister());
    public final CC2420Output FIFOP_pin = new CC2420Output("FIFOP", new BooleanRegister());
    public final CC2420Output CCA_pin   = new CC2420Output("CCA", CCA_assessor);
    public final CC2420Output SFD_pin   = new CC2420Output("SFD", SFD_value);

    public final SPIInterface spiInterface = new SPIInterface();
    public final ADCInterface adcInterface = new ADCInterface();

    public int FIFOP_interrupt = -1;

    protected final SimPrinter printer;

    // the CC2420 allows reversing the polarity of these outputs.
    protected boolean FIFO_active = true; // selects active high (true) or active low.
    protected boolean FIFOP_active = true;
    protected boolean CCA_active = true;
    protected boolean SFD_active = true;

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

        // create a private medium for this radio
        // the simulation may replace this later with a new one.
        medium = createMedium(null);

        // get debugging channel.
        printer = SimUtil.getPrinter(mcu.getSimulator(), "radio.cc2420");
    }

    public void setSFDView(BooleanView sfd) {
        if (SFD_pin.level == SFD_value) {
            SFD_pin.level = sfd;
        }
        SFD_value = sfd;
    }

    /**
     * The <code>readRegister()</code> method reads the value from the specified register
     * and takes any action(s) that are necessary for the specific register.
     * @param addr the address of the register
     * @return an integer value representing the result of reading the register
     */
    int readRegister(int addr) {
        int val = (int) registers[addr];
        if ( printer.enabled ) {
            printer.println("CC2420 "+ regName(addr)+" -> "+StringUtil.toMultirepString(val, 16));
        }
        return val;
    }

    /**
     * The <code>writeRegister()</code> method writes the specified value to the specified
     * register, taking any action(s) necessary and activating any command strobes as
     * required.
     * @param addr the address of the register
     * @param val the value to write to the specified register
     */
    void writeRegister(int addr, int val) {
        if ( printer.enabled ) {
            printer.println("CC2420 "+ regName(addr) +" <= "+StringUtil.toMultirepString(val, 16));
        }
        registers[addr] = (char)val;
        switch (addr) {
            case MAIN:
                // TODO: main register write
                break;
            case IOCFG1:
                int ccaMux = val & 0x1f;
                int sfdMux = (val >> 5) & 0x1f;
                setCCAMux(ccaMux);
                setSFDMux(sfdMux);
                break;
            case IOCFG0:
                // set the polarities for the output pins.
                FIFO_active = !Arithmetic.getBit(val, 10);
                FIFOP_active = !Arithmetic.getBit(val, 9);
                SFD_active = !Arithmetic.getBit(val, 8);
                CCA_active = !Arithmetic.getBit(val, 7);
                break;
        }
        computeStatus();
    }

    private void setSFDMux(int sfdMux) {
        // TODO: SFD multiplexor
    }

    private void setCCAMux(int ccaMux) {
        // TODO: handle all the possible CCA multiplexing sources
        // and possibility of active low.
        if (ccaMux == 24) CCA_pin.level = oscStable;
        else CCA_pin.level = CCA_assessor;
    }

    void strobe(int addr) {
        if ( printer.enabled ) {
            printer.println("CC2420 Strobe "+ strobeName(addr));
        }
        switch (addr) {
            case SNOP:
                break;
            case SXOSCON:
                startOscillator();
                break;
            case STXCAL:
                break;
            case SRXON:
                transmitter.shutdown();
                receiver.startup();
                break;
            case STXONCCA:
                if (CCA_assessor.getValue()) {
                    receiver.shutdown();
                    transmitter.startup();
                }
                break;
            case STXON:
                receiver.shutdown();
                transmitter.startup();
                break;
            case SRFOFF:
                break;
            case SXOSCOFF:
                break;
            case SFLUSHRX:
                rxFIFO.clear();
                FIFO_pin.level.setValue(!FIFO_active);
                FIFOP_pin.level.setValue(!FIFOP_active);
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

    private void startOscillator() {
        if (!oscStable.getValue() && !startingOscillator) {
            startingOscillator = true;
            sim.insertEvent(new Simulator.Event() {
                public void fire() {
                    oscStable.setValue(true);
                    startingOscillator = false;
                }
            }, toCycles(XOSC_START_TIME));
        }
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
        // do nothing.
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
            byte status = getStatus();
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

    private byte getStatus() {
        byte status = (byte) statusRegister.getValue();
        if (printer.enabled) {
            printer.println("CC2420 status: "+StringUtil.toBin(status, 8));
        }
        return status;
    }

    protected byte readFIFO(ByteFIFO fifo) {
        byte val = fifo.remove();
        if (printer.enabled) {
            printer.println("CC2420 Read "+fifoName(fifo)+" -> "+StringUtil.toMultirepString(val, 8));
        }
        if (fifo == rxFIFO) {
            if (fifo.empty()) {
                // reset the FIFO pin when the read FIFO is empty.
                FIFO_pin.level.setValue(!FIFO_active);
            } else if (fifo.size() < getFIFOThreshold()) {
                FIFOP_pin.level.setValue(!FIFOP_active);
            }
        }
        return val;
    }

    protected byte writeFIFO(ByteFIFO fifo, byte val, boolean st) {
        if (printer.enabled) {
            printer.println("CC2420 Write "+fifoName(fifo)+" <= "+StringUtil.toMultirepString(val, 8));
        }
        byte result = st ? getStatus() : 0;
        fifo.add(val);
        computeStatus();
        return result;
    }

    private int getFIFOThreshold() {
        // get the FIFOP_THR value from the configuration register
        return (int) registers[IOCFG0] & 0x3f;
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

    public class ClearChannelAssessor implements BooleanView {
        public void setValue(boolean val) {
            // ignore writes.
        }
        public boolean getValue() {
            return receiver.isChannelClear();
        }
    }

    public class SPIInterface implements SPIDevice {

        public SPI.Frame exchange(SPI.Frame frame) {
            if ( !CS_pin.level ) {
                // configuration requires CS pin to be held low
                return SPI.newFrame(receiveConfigByte(frame.data));
            } else {
                return SPI.newFrame((byte)0);
            }
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
        // a change in the CS level always restarts a config command.
        configByteCnt = 0;
    }

    private static final int TX_IN_PREAMBLE = 0;
    private static final int TX_SFD_1 = 1;
    private static final int TX_SFD_2 = 2;
    private static final int TX_LENGTH = 3;
    private static final int TX_IN_PACKET = 4;
    private static final int TX_IN_CHKSUM_1 = 5;
    private static final int TX_IN_CHKSUM_2 = 6;
    private static final int TX_END = 7;

    public class Transmitter extends Medium.Transmitter {

        protected int state;
        protected int counter;
        protected int length;
        protected char crc;

        public Transmitter(Medium m) {
            super(m, sim.getClock());
        }

        public byte nextByte() {
            byte val = 0;
            switch (state) {
                case TX_IN_PREAMBLE:
                    counter++;
                    if (counter >= getPreambleLength()) {
                        state = TX_SFD_1;
                    }
                    break;
                case TX_SFD_1:
                    state = TX_SFD_2;
                    val = Arithmetic.low(registers[SYNCWORD]);
                    break;
                case TX_SFD_2:
                    state = TX_LENGTH;
                    val = Arithmetic.high(registers[SYNCWORD]);
                    break;
                case TX_LENGTH:
                    length = txFIFO.remove() & 0x3f;
                    state = TX_IN_PACKET;
                    counter = 0;
                    crc = 0;
                    val = (byte)length;
                    SFD_value.setValue(SFD_active);
                    break;
                case TX_IN_PACKET:
                    // TODO: handle TX underlow.
                    val = txFIFO.remove();
                    crcAccumulate(val);
                    if (++counter >= length - 2) {
                        state = TX_IN_CHKSUM_1;
                    }
                    break;
                case TX_IN_CHKSUM_1:
                    state = TX_IN_CHKSUM_2;
                    //val = Arithmetic.low(crc);
                    val = (byte)0xff;
                    break;
                case TX_IN_CHKSUM_2:
                    //val = Arithmetic.high(crc);
                    val = (byte)0xff;
                    state = TX_END;
                    counter = 0;
                    SFD_value.setValue(!SFD_active);
                    shutdown();
                    receiver.startup(); // auto transition back to receive mode.
                    break;
                    // and fall through.
                default:
                    state = TX_IN_PREAMBLE;
                    counter = 0;
                    break;
            }
            if (printer.enabled) {
                printer.println("CC2420 "+StringUtil.to0xHex(val, 2)+" --------> ");
            }
            return val;
        }

        private void crcAccumulate(byte val) {
            int i = 8;
            do {
                if ((crc & 0x8000) != 0) crc = (char)(crc << 1 ^ 0x1021);
                else crc = (char)(crc << 1);
            } while (--i > 0);
        }

        private int getPreambleLength() {
            int val = registers[MDMCTRL1] & 0xf;
            return val + 7;
        }

        void startup() {
            txActive.setValue(true);
            state = TX_IN_PREAMBLE;
            beginTransmit(0.00);
        }

        void shutdown() {
            txActive.setValue(false);
            endTransmit();
        }
    }


    private static final int RECV_SFD_SCAN = 0;
    private static final int RECV_SFD_MATCHED_1 = 1;
    private static final int RECV_SFD_MATCHED_2 = 2;
    private static final int RECV_IN_PACKET = 3;
    private static final int RECV_END_STATE = 4;

    public class Receiver extends Medium.Receiver {
        protected int state;
        protected int counter;
        protected int length;

        public Receiver(Medium m) {
            super(m, sim.getClock());
        }
        public void nextByte(boolean lock, byte b) {
            if (!lock) {
                // the transmission lock has been lost
                if (state == RECV_END_STATE) ; // packet over.
                if (state == RECV_IN_PACKET) ; // TODO: packet lost in middle.
                state = RECV_SFD_SCAN;
                SFD_value.setValue(!SFD_active);
                return;
            }
            if (printer.enabled) {
                printer.println("CC2420 <======== "+StringUtil.to0xHex(b, 2));
            }
            switch (state) {
                case RECV_SFD_MATCHED_1:
                    // check against the second byte of the SYNCWORD register.
                    if (b == Arithmetic.high(registers[SYNCWORD])) {
                        state = RECV_SFD_MATCHED_2;
                        SFD_value.setValue(SFD_active);
                        break;
                    }
                    // fallthrough if we failed to match the second byte
                    // and try to match the first byte again.
                case RECV_SFD_SCAN:
                    // check against the first byte of the SYNCWORD register.
                    if (b == Arithmetic.low(registers[SYNCWORD])) {
                        state = RECV_SFD_MATCHED_1;
                    } else {
                        state = RECV_SFD_SCAN;
                    }
                    break;
                case RECV_SFD_MATCHED_2:
                    // SFD matched. read the length from the next byte.
                    length = b & 0x1f;
                    rxFIFO.add(b);
                    counter = 0;
                    state = RECV_IN_PACKET;
                    break;
                case RECV_IN_PACKET:
                    // we are in the body of the packet.
                    // TODO: handle RX overflow.
                    rxFIFO.add(b);
                    FIFO_pin.level.setValue(FIFO_active);
                    if (rxFIFO.size() >= getFIFOThreshold()) {
                        signalFIFOP();
                    }
                    counter++;
                    if (counter == length) {
                        // TODO: compute checksum and drop frame if failure.
                        // signal FIFOP and unsignal SFD
                        signalFIFOP();
                        SFD_value.setValue(SFD_active);
                        state = RECV_END_STATE;
                    }
                    break;
            }
        }

        private void signalFIFOP() {
            FIFOP_pin.level.setValue(FIFOP_active);
            if (FIFOP_interrupt > 0) {
                sim.getInterpreter().getInterruptTable().post(FIFOP_interrupt);
            }
        }

        void startup() {
            state = RECV_SFD_SCAN;
            beginReceive();
        }

        void shutdown() {
            endReceive();
        }
    }

    public void connectTo(Medium m) {
        medium = m;
        transmitter = new Transmitter(m);
        receiver = new Receiver(m);
    }

    /**
     * The <code>CC2420Pin</code>() class models pins that are inputs and outputs to the CC2420 chip.
     */
    public class CC2420Pin implements Microcontroller.Pin.Input, Microcontroller.Pin.Output {
        protected final String name;
        protected boolean level;

        public CC2420Pin(String n) {
            name = n;
        }

        public void write(boolean level) {
            if ( this.level != level ) {
                // level changed
                this.level = level;
                if ( this == CS_pin ) pinChange_CS(level);
            }
        }

        public boolean read() {
            if (printer.enabled) {
                printer.println("CC2420 Read pin "+name+" -> "+level);
            }
            return level;
        }
    }

    public class CC2420Output implements Microcontroller.Pin.Input {

        protected BooleanView level;
        protected final String name;

        public CC2420Output(String n, BooleanView lvl) {
            name = n;
            level = lvl;
        }

        public boolean read() {
            boolean val = level.getValue();
            if (printer.enabled) {
                printer.println("CC2420 Read pin "+name+" -> "+val);
            }
            return val;
        }
    }

    public static String regName(int reg) {
        switch (reg) {
        case MAIN    : return "MAIN    ";
        case MDMCTRL0: return "MDMCTRL0";
        case MDMCTRL1: return "MDMCTRL1";
        case RSSI    : return "RSSI    ";
        case SYNCWORD: return "SYNCWORD";
        case TXCTRL  : return "TXCTRL  ";
        case RXCTRL0 : return "RXCTRL0 ";
        case RXCTRL1 : return "RXCTRL1 ";
        case FSCTRL  : return "FSCTRL  ";
        case SECCTRL0: return "SECCTRL0";
        case SECCTRL1: return "SECCTRL1";
        case BATTMON : return "BATTMON ";
        case IOCFG0  : return "IOCFG0  ";
        case IOCFG1  : return "IOCFG1  ";
        case MANFIDL : return "MANFIDL ";
        case MANFIDH : return "MANFIDH ";
        case FSMTC   : return "FSMTC   ";
        case MANAND  : return "MANAND  ";
        case MANOR   : return "MANOR   ";
        case AGCCTRL0: return "AGCCTRL0";
        case AGCTST0 : return "AGCTST0 ";
        case AGCTST1 : return "AGCTST1 ";
        case AGCTST2 : return "AGCTST2 ";
        case FSTST0  : return "FSTST0  ";
        case FSTST1  : return "FSTST1  ";
        case FSTST2  : return "FSTST2  ";
        case FSTST3  : return "FSTST3  ";
        case RXBPFTST: return "RXBPFTST";
        case FSMSTATE: return "FSMSTATE";
        case ADCTST  : return "ADCTST  ";
        case DACTST  : return "DACTST  ";
        case TOPTST  : return "TOPTST  ";
        case TXFIFO  : return "TXFIFO  ";
        case RXFIFO  : return "RXFIFO  ";
            default: return StringUtil.to0xHex(reg,2) + "    ";
        }
    }
    public static String strobeName(int strobe) {
        switch (strobe) {
            case SNOP    : return "SNOP    ";
            case SXOSCON : return "SXOSCON ";
            case STXCAL  : return "STXCAL  ";
            case SRXON   : return "SRXON   ";
            case STXON   : return "STXON   ";
            case STXONCCA: return "STXONCCA";
            case SRFOFF  : return "SRFOFF  ";
            case SXOSCOFF: return "SXOSCOFF";
            case SFLUSHRX: return "SFLUSHRX";
            case SFLUSHTX: return "SFLUSHTX";
            case SACK    : return "SACK    ";
            case SACKPEND: return "SACKPEND";
            case SRXDEC  : return "SRXDEC  ";
            case STXENC  : return "STXENC  ";
            case SAES    : return "SAES    ";
            default: return StringUtil.to0xHex(strobe, 2) + "    ";
        }
    }
    String fifoName(ByteFIFO fifo) {
        if ( fifo == txFIFO ) return "TX FIFO";
        if ( fifo == rxFIFO ) return "RX FIFO";
        return "XX FIFO";
    }

    private long toCycles(long us) {
        return us * sim.getClock().getHZ() / 1000000;
    }

    public static Medium createMedium(Synchronizer synch) {
        return new Medium(synch, 250000, 48, 8, 8 * 128);
    }
}
