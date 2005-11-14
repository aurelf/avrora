/**
 * Copyright (c) 2005, Regents of the University of California
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

package avrora.sim.radio;

import avrora.sim.FiniteStateMachine;
import avrora.sim.Simulator;
import avrora.sim.energy.Energy;
import avrora.sim.mcu.*;
import avrora.sim.util.TransactionalList;
import cck.text.StringUtil;
import cck.util.Arithmetic;
import cck.util.Util;
import java.util.Arrays;

/**
 * The <code>CC2420Radio</code> class implements a simulation of the Chipcon CC2420 radio
 * chip that implements the 802.3 wireless standard.
 *
 * @author Keith Mayoral
 */
public class CC2420Radio implements Radio {
    protected RadioRegister registers [] = new RadioRegister[0x40];
    protected TXFIFOBuffer TXFIFOBuf;
    protected RXFIFOBuffer RXFIFOBuf;
    protected boolean receiving;
    protected byte statusByte;   //CC2420 status byte which is set by the SNOP commmand strobe
    //sent back during addressing byte of most trnasfers, during all command strobes,
    // and first byte of transmit
    /**
     * LegacyRegister addresses.
     */
    public static final int MAIN = 0x10;
    public static final int MDMCTRL0 = 0x11;
    public static final int MDMCTRL1 = 0x12;
    public static final int RSSI = 0x13;
    public static final int SYNCWORD = 0x14;
    public static final int TXCTRL = 0x15;
    public static final int RXCTRL0 = 0x16;
    public static final int RXCTRL1 = 0x17;
    public static final int FSCTRL = 0x18;
    public static final int SECCTRL0 = 0x19;
    public static final int SECCTRL1 = 0x1a;
    public static final int BATTMON = 0x1b;
    public static final int IOCFG0 = 0x1c;
    public static final int IOCFG1 = 0x1d;
    public static final int MANFIDL = 0x1e;
    public static final int MANFIDH = 0x1f;
    public static final int FSMTC = 0x20;
    public static final int MANAND = 0x21;
    public static final int MANOR = 0x22;
    public static final int AGCCTRL0 = 0x23;
    public static final int AGCTST0 = 0x24;
    public static final int AGCTST1 = 0x25;
    public static final int AGCTST2 = 0x26;
    public static final int FSTST0 = 0x27;
    public static final int FSTST1 = 0x28;
    public static final int FSTST2 = 0x29;
    public static final int FSTST3 = 0x2a;
    public static final int RXBPFTST = 0x2b;
    public static final int FSMSTATE = 0x2c;
    public static final int ADCTST = 0x2d;
    public static final int DACTST = 0x2e;
    public static final int TOPTST = 0x2f;
    public static final int TXFIFO = 0x3e;
    public static final int RXFIFO = 0x3f;
    /**
     * Command Strobe LegacyRegister Addresses (instuctions that take actions when conditions are met)
     */
    public static final int SNOP = 0x00;
    public static final int SXOSCON = 0x01;
    public static final int STXCAL = 0x02;
    public static final int SRXON = 0x03;
    public static final int STXON = 0x04;
    public static final int STXONCCA = 0x05;
    public static final int SRFOFF = 0x06;
    public static final int SXOSCOFF = 0x07;
    public static final int SLFUSHRX = 0x08;
    public static final int SFLUSHTX = 0x09;
    public static final int SACK = 0x0a;
    public static final int SACKPEND = 0x0b;
    public static final int SRXDEC = 0x0c;
    public static final int STXENC = 0x0d;
    public static final int SAES = 0x0e;
    /**
     * Registers
     */
    protected final MainRegister MAIN_reg;
    protected final Modem0Register MDMCTRL0_reg;
    protected final Modem1Register MDMCTRL1_reg;
    protected final RSSIRegister RSSI_reg;
    protected final SyncRegister SYNC_reg;
    protected final TxCtrlRegister TXCTRL_reg;
    protected final RxCtrl0Register RXCTRL0_reg;
    protected final RxCtrl1Register RXCTRL1_reg;
    protected final FreqSynthRegister FSCTRL_reg;
    protected final IORegister0 IOCFG0_reg;
    protected final IORegister1 IOCFG1_reg;
    protected final TXFIFORegister TXFIFO_reg;
    protected final RXFIFORegister RXFIFO_reg;
    /**
     * Command Strobes
     */
    protected final SnopCommand SNOP_cmd;
    protected final SxosconCommand SXOSCON_cmd;
    protected final StxcalCommand STXCAL_cmd;
    protected final SrxonCommand SRXON_cmd;
    protected final StxonCommand STXON_cmd;
    protected final StxonccaCommand STXONCCA_cmd;
    protected final SrfoffCommand SRFOFF_cmd;
    protected final SxoscoffCommand SXOSCOFF_cmd;
    protected final SflushrxCommand SFLUSHRX_cmd;
    protected final SflushtxCommand SFLUSHTX_cmd;
    protected final SackCommand SACK_cmd;
    protected final SackpendCommand SACKPEND_cmd;
    protected final SrxdecCommand SRXDEC_cmd;
    protected final StxencCommand STXENC_cmd;
    protected final SaesCommand SAES_cmd;
    protected static final String[] allModeNames = RadioEnergy.allModeNames();
    protected static final int[][] ttm = FiniteStateMachine.buildSparseTTM(allModeNames.length, 0);
    protected final Simulator.Printer radioPrinter;
    protected final ProbeList probes;
    protected final long xoscFrequency;
    /**
     * Connected Microcontroller, Simulator and SimulatorThread should all correspond.
     */
    protected final Microcontroller mcu;
    protected final Simulator sim;
    protected final FiniteStateMachine stateMachine;
    protected ATMegaController controller;
    /**
     * Radio environment into which this radio broadcasts.
     */
    protected RadioAir air;

    /**
     * The <code>ProbeList</code> class just keeps track of a list of probes.
     */
    class ProbeList extends TransactionalList implements Radio.RadioProbe {
        public void fireAtPowerChange(Radio r, int newPower) {
            beginTransaction();
            for (Link pos = head; pos != null; pos = pos.next)
                ((RadioProbe) pos.object).fireAtPowerChange(r, newPower);
            endTransaction();
        }

        public void fireAtFrequencyChange(Radio r, double freq) {
            beginTransaction();
            for (Link pos = head; pos != null; pos = pos.next)
                ((RadioProbe) pos.object).fireAtFrequencyChange(r, freq);
            endTransaction();
        }

        public void fireAtBitRateChange(Radio r, int newbitrate) {
            beginTransaction();
            for (Link pos = head; pos != null; pos = pos.next)
                ((RadioProbe) pos.object).fireAtBitRateChange(r, newbitrate);
            endTransaction();
        }

        public void fireAtTransmit(Radio r, Radio.Transmission p) {
            beginTransaction();
            for (Link pos = head; pos != null; pos = pos.next)
                ((RadioProbe) pos.object).fireAtTransmit(r, p);
            endTransaction();
        }

        public void fireAtTransmit(Radio r, Radio.Transmission[] p) {
            beginTransaction();
            for (Link pos = head; pos != null; pos = pos.next) {
                for (int i = 0; i < 128; i++)
                    ((RadioProbe) pos.object).fireAtTransmit(r, p[i]);
            }
            endTransaction();
        }

        public void fireAtReceive(Radio r, Radio.Transmission p) {
            beginTransaction();
            for (Link pos = head; pos != null; pos = pos.next)
                ((RadioProbe) pos.object).fireAtReceive(r, p);
            endTransaction();
        }
    }

    public CC2420Radio(Microcontroller mcu, int xfreq) {
        xoscFrequency = 2048 + xfreq;
        probes = new ProbeList();
        this.mcu = mcu;
        this.sim = mcu.getSimulator();
        radioPrinter = sim.getPrinter("radio.cc2420");
        for (int i = 0x14; i < registers.length; i++) {
            registers[i] = new DummyRegister(i);
        }

        registers[MAIN] = MAIN_reg = new MainRegister();
        registers[MDMCTRL0] = MDMCTRL0_reg = new Modem0Register();
        registers[MDMCTRL1] = MDMCTRL1_reg = new Modem1Register();
        registers[RSSI] = RSSI_reg = new RSSIRegister();
        registers[SYNCWORD] = SYNC_reg = new SyncRegister();
        registers[TXCTRL] = TXCTRL_reg = new TxCtrlRegister();
        registers[RXCTRL0] = RXCTRL0_reg = new RxCtrl0Register();
        registers[RXCTRL1] = RXCTRL1_reg = new RxCtrl1Register();
        registers[FSCTRL] = FSCTRL_reg = new FreqSynthRegister();
        registers[IOCFG0] = IOCFG0_reg = new IORegister0();
        registers[IOCFG1] = IOCFG1_reg = new IORegister1();
        registers[TXFIFO] = TXFIFO_reg = new TXFIFORegister();
        registers[RXFIFO] = RXFIFO_reg = new RXFIFORegister();

        FSCTRL_reg.setFreq(xfreq);
        TXFIFOBuf = new TXFIFOBuffer();
        RXFIFOBuf = new RXFIFOBuffer();

        registers[SNOP] = SNOP_cmd = new SnopCommand();
        registers[SXOSCON] = SXOSCON_cmd = new SxosconCommand();
        registers[STXCAL] = STXCAL_cmd = new StxcalCommand();
        registers[SRXON] = SRXON_cmd = new SrxonCommand();
        registers[STXON] = STXON_cmd = new StxonCommand();
        registers[STXONCCA] = STXONCCA_cmd = new StxonccaCommand();
        registers[SRFOFF] = SRFOFF_cmd = new SrfoffCommand();
        registers[SXOSCOFF] = SXOSCOFF_cmd = new SxoscoffCommand();
        registers[SLFUSHRX] = SFLUSHRX_cmd = new SflushrxCommand();
        registers[SFLUSHTX] = SFLUSHTX_cmd = new SflushtxCommand();
        registers[SACK] = SACK_cmd = new SackCommand();
        registers[SACKPEND] = SACKPEND_cmd = new SackpendCommand();
        registers[SRXDEC] = SRXDEC_cmd = new SrxdecCommand();
        registers[STXENC] = STXENC_cmd = new StxencCommand();
        registers[SAES] = SAES_cmd = new SaesCommand();

        // If there are other microcontroller implementations in the future,
        // this code should be adjusted to account for that.
        controller = new ATMegaController();
        controller.install(mcu);
        //setup energy recording
        Simulator simulator = mcu.getSimulator();
        stateMachine = new FiniteStateMachine(simulator.getClock(), RadioEnergy.startMode, allModeNames, ttm);
        new Energy("Radio", RadioEnergy.modeAmpere, stateMachine);
    }

    /**
     * The <code>getFiniteStateMachine()</code> method gets a reference to the finite state
     * machine that represents this radio's state. For example, there are states corresponding
     * to "on", "off", "transmitting", and "receiving". The state names and numbers will vary
     * by radio implementation. The <code>FiniteStateMachine</code> instance allows the user
     * to instrument the state transitions in order to gather information during simulation.
     *
     * @return a reference to the finite state machine for this radio
     */
    public FiniteStateMachine getFiniteStateMachine() {
        return stateMachine;
    }

    /**
     * The <code>insertProbe()</code> method inserts a probe into a radio. The probe is then
     * notified when the radio changes power, frequency, baud rate, or transmits or receives
     * a byte.
     *
     * @param p the probe to insert on this radio
     */
    public void insertProbe(RadioProbe p) {
        probes.add(p);
    }

    /**
     * The <code>removeProbe()</code> method removes a probe on this radio.
     *
     * @param p the probe to remove from this radio instance
     */
    public void removeProbe(RadioProbe p) {
        probes.remove(p);
    }

    /**
     * The <code>RadioRegister</code> is an abstract register grouping together registers on the CC1000
     * radio.
     */
    protected abstract class RadioRegister {
        protected final String id; // name of this register
        protected final int def; // default value
        protected int value; // current value of this register

        RadioRegister(String id, int def) {
            this.id = id;
            this.def = def;
            this.value = def;
        }

        public void write(int val) {
            value = val;
            decode(value);
            if (radioPrinter.enabled) {
                printStatus();
            }
        }

        public void writeBit(int bit, boolean val) {
            value = Arithmetic.setBit(value, bit, val);
            decode(value);
            if (radioPrinter.enabled) {
                printStatus();
            }
        }

        protected abstract void decode(int val);

        protected void action() {
            // default: do nothing
        }

        protected byte read() {
            // TODO: is high() correct?
            return Arithmetic.high(value);
        }

        protected void printStatus() {
            radioPrinter.println("CC2420[" + id + "]: ...");
        }

        protected void reset() {
            write(def);
        }
    }

    protected abstract class RadioStrobe extends RadioRegister {

        public void decode(int val) {
            // default: do nothing
        }

        RadioStrobe(String id) {
            super(id, 0);
        }

        protected void printResult() {
            // default: do nothing
        }
    }

    /**
     * The <code>DummyRegister</code> is a filler class for registers within the 7-bit address space of the
     * radio registers, but do not actually exist/do anything in the real radio.
     */
    protected class DummyRegister extends RadioRegister {
        DummyRegister(int i) {
            super("Dummy " + Integer.toHexString(i), (byte) 0);
        }

        protected void decode(int val) {
        }
    }

    /**
     * The main register on the CC1000.
     */
    protected class MainRegister extends RadioRegister {
        public final int RESETn = 15;        //Active low reset , equivelent to using RESETn pin
        public final int ENC_RESETn = 14;    //Active low reset of encryp. module (test only)
        public final int DEMOD_RESETn = 13;  //Active low reset of demodulator module (test only)
        public final int MOD_RESETn = 12;    //Active low reset of modulator module (test only)
        public final int FS_RESETn = 11;     //Active low reset of freq. synth. module (test only)
        public final int XOSC16M_BYPASS = 0; //Bypassed the crystal oscillator and uses a buffered
        //version of the signal on Q1 directly.
        boolean resetn;
        boolean encresetn;
        boolean demodresetn;
        boolean modresetn;
        boolean fsresetn;
        boolean xosc16m;
        int oldVal;
        boolean oldRn;

        MainRegister() {
            super("MAIN", 0xf800);
        }

        protected void decode(int val) {
            oldRn = resetn;
            resetn = Arithmetic.getBit(val, RESETn);
            encresetn = Arithmetic.getBit(val, ENC_RESETn);
            demodresetn = Arithmetic.getBit(val, DEMOD_RESETn);
            modresetn = Arithmetic.getBit(val, MOD_RESETn);
            fsresetn = Arithmetic.getBit(val, FS_RESETn);
            xosc16m = Arithmetic.getBit(val, XOSC16M_BYPASS);
            if (resetn && !oldRn) {
                controller.disable();
            } else {
                controller.enable();
                receiving = true;
            }
            // TODO: Figure out how radio really resets..
            if (resetn && !Arithmetic.getBit(oldVal, RESETn)) {
                oldVal = val;
                //resetRadio();
                return;
            }
            oldVal = val;
        }

        protected void printStatus() {
            radioPrinter.println("CC2420[MAIN]: " + resetn + ", encryption module: " + encresetn + ", demodulator module: " + demodresetn + ", modulator module: " + modresetn + ", freq. synthesizer module: " + fsresetn + ", crystal oscillator bypass: " + xosc16m);
        }
    }

    protected class RSSIRegister extends RadioRegister {
        byte ccathr = 0;
        byte rssival = 0;    //meant to be read-only

        RSSIRegister() {
            super("RSSI", 0x0000);
            decode(value);
        }

        protected void decode(int val) {
            ccathr = (byte) (val & 0xFF00 >> 8);
            rssival = (byte) (val & 0xFF);
        }

        protected void reset() {
            write((short) 0xe080); //e0 is 2's comp of -32 which is reset for cca_thr, and 80 is 2's comp of -128
        }                          //which is reset for rssi_val
    }

    protected class Modem1Register extends RadioRegister {
        public final int DEMOD_AVG_MODE = 5;  //pin number 5
        public final int MOD_MODE = 4;        //pin number 4
        boolean demodavg;   //mode 0, lock freq. offset filter after preamble match
        boolean modmode;    //mode 0, IEEE 802.15.4 compliant, mode 1 isn't  (reversed phase)
        int corrthr = 0x14;   // == #20 == 0b10100
        int txmode = 0;    //mode from 0 - 3, 0: buffered normal operation
        int rxmode = 0;    //mode from 0 - 3, 0: buffered normal operation

        Modem1Register() {
            super("MODEM1", 0x0000);
            decode(value);
        }

        protected void reset() {
            write(0x0000);   //should be at least 0x0500 before operation though
        }

        protected void decode(int val) {
            demodavg = Arithmetic.getBit(val, DEMOD_AVG_MODE);
            modmode = Arithmetic.getBit(val, MOD_MODE);
            corrthr = (val & 0x07c0) >> 6;
            txmode = (val & 0x000C) >> 2;
            rxmode = (val & 0x0003);
        }
    }

    /**
     * The baud rate of the system is determined by values on the MODEM0 register. TinyOS uses a baud rate of
     * 19.2 kBaud with manchester encoding, which translates into 9.6 kbps of data.
     */
    protected class Modem0Register extends RadioRegister {
        public final int RSVRED_FRAME_MODE = 13;
        public final int PAN_COORD = 12;
        public final int ADR_DECODE = 11;
        public final int AUTOCRC = 5;
        public final int AUTOACK = 4;
        boolean rsvrdframe;
        boolean pancoord;
        boolean adrdecode;
        boolean autocrc;
        boolean autoack;
        int ccahyst = 2;
        int ccamode = 3;
        int prelength = 2;  //3 leading 0 bytes

        Modem0Register() {
            super("MODEM0", 0x0ae2);
            decode(value);
            // default value is 0b 000 1010 1110 0010
        }

        protected void decode(int val) {
            rsvrdframe = Arithmetic.getBit(val, RSVRED_FRAME_MODE);
            pancoord = Arithmetic.getBit(val, PAN_COORD);
            adrdecode = Arithmetic.getBit(val, ADR_DECODE);
            autocrc = Arithmetic.getBit(val, AUTOCRC);
            autoack = Arithmetic.getBit(val, AUTOACK);
            ccahyst = (val & 0x0700) >> 8;
            ccamode = (val & 0x00c0) >> 6;
            prelength = (val & 0x000F);
        }

        protected void reset() {
            write(0x0ae2);
        }
    }

    /**
     * The synchoronisation word is stored in the SYNCWORD reg.  It is processed from the least significant nibble
     * to the most significant nibble.  It is used both during modulation and during demodulation.
     */
    protected class SyncRegister extends RadioRegister {
        int syncword = 0xa70F;        //Should be 0xa70F

        SyncRegister() {
            super("SYNCWORD", 0xa70f);
            decode(value);
        }

        protected void decode(int val) {
            syncword = val;
        }

        protected void reset() {
            write(0xa70f);
        }
    }

    /**
     * Transmit Control LegacyRegister controls TX mixer currents, wait time required after STXON before transmitting,
     * and PA output level and current programming.
     */
    protected class TxCtrlRegister extends RadioRegister {
        //public final int TX_TURNAROUND = 13;
        public final int RESERVED = 5;
        public final int[] TXMIXBUF_CUR = {690, 980, 1160, 1440};    //measured in uA
        public final int[] TX_TURNAROUND = {8, 12};    // measured in symbol periods 8 = 128 us, 12 = 192 us
        public final int[] TXMIX_CURRENT = {1720, 1880, 2050, 2210}; //measured in uA
        public final int[] PA_CURRENT = {-3, -2, -1, 0, 1, 2, 3, 4};
        int txmixbuff_cur = 1160;
        int txmix_cap_array = 0;
        int txmix_current = 1720;
        int pa_current = 0;
        int pa_level = 31;
        int tx_turnaround = 12;
        boolean reserved;

        TxCtrlRegister() {
            super("Transmit Control LegacyRegister", 0xa0ff);
            decode(value);
        }

        protected void decode(int val) {
            txmixbuff_cur = TXMIXBUF_CUR[(val & 0xc000) >> 14];
            txmix_cap_array = (val & 0x1800) >> 11;
            txmix_current = TXMIX_CURRENT[(val & 0x0600) >> 9];
            pa_current = PA_CURRENT[(val & 0x01c0) >> 6];
            pa_level = (val & 0x001f);
            tx_turnaround = TX_TURNAROUND[(val & 0x2000) >> 13];
            reserved = Arithmetic.getBit(val, RESERVED);
        }

        protected void reset() {
            write(0xa0ff);
        }
    }

    /**
     * Receive Control LegacyRegister 0 controls the rx mixer current as well as current compensation and main current
     * for the LNA in the AGC high, med, and low gain modes
     */
    protected class RxCtrl0Register extends RadioRegister {
        public final int[] RXMIXBUF_CUR = {690, 980, 1160, 1440}; //in uA
        public final int[] HIGH_LNA_GAIN = {0, 100, 300, 1000}; //in microA
        public final int[] HIGH_LNA_CURRENT = {240, 480, 640, 1280}; // in microA
        int rxmixbuff_cur = 980;
        int high_lna_gain = 0;
        int med_lna_gain = 2;
        int low_lna_gain = 3;
        int high_lna_cur = 640;
        int med_lna_cur = 1;
        int low_lna_cur = 1;

        RxCtrl0Register() {
            super("RX Control LegacyRegister 0", 0x12e5);
            decode(value);
        }

        protected void decode(int val) {
            rxmixbuff_cur = RXMIXBUF_CUR[(val & 0x3000) >> 12];
            high_lna_gain = HIGH_LNA_GAIN[(val & 0x0c00) >> 10];
            med_lna_gain = (val & 0x0300) >> 8;
            low_lna_gain = (val & 0x00c0) >> 6;
            high_lna_cur = HIGH_LNA_CURRENT[(val & 0x0030) >> 4];
            med_lna_cur = (val & 0x000c) >> 2;
            low_lna_cur = (val & 0x0003);
        }

        protected void reset() {
            write(0x12e5);
        }
    }

    /**
     * Receive Control LegacyRegister 1 controls settings of RX mixers, LNA modes, controls current to RX bandpass filters,
     * receiver mixers output, and the RX mixer.  Also controls VCM level.
     */
    protected class RxCtrl1Register extends RadioRegister {
        public final int LOW_LOWGAIN = 11;
        public final int MED_LOWGAIN = 10;
        public final int HIGH_HGM = 9;
        public final int MED_HGM = 8;
        public final int[] RXBPF_LOCUR = {4, 3};  //in uA
        public final double[] RXBPF_MIDCUR = {4, 3.5};  //in units of uA
        public final double[] LNA_CAP_ARRAY = {Double.NaN, 0.1, 0.2, 0.3}; // in units of pF
        //NaN means varactor array setting in LNA is off
        public final int[] RXMIX_TAIL = {12, 16, 20, 24};  //in units of microA
        public final int[] RXMIX_VCM = {8, 12, 16, 20};  //in units of microA
        public final int[] RXMIX_CURRENT = {360, 720, 900, 1260}; //in units of microA
        int rxbpf_locur;
        double rxbpf_midcur;
        boolean low_lowgain;
        boolean med_lowgain;
        boolean high_hgm;
        boolean med_hgm;
        double lna_cap_array = 0.1;
        int rxmix_tail = 16;
        int rxmix_vcm = 12;
        int rxmix_current = 900;

        RxCtrl1Register() {
            super("RX Control LegacyRegister 1", 0x0a56);
            decode(value);
        }

        protected void decode(int val) {
            rxbpf_locur = RXBPF_LOCUR[(val & 0x2000) >> 13];
            rxbpf_midcur = RXBPF_MIDCUR[(val & 0x1000) >> 12];
            low_lowgain = Arithmetic.getBit(val, LOW_LOWGAIN);
            med_lowgain = Arithmetic.getBit(val, MED_LOWGAIN);
            high_hgm = Arithmetic.getBit(val, HIGH_HGM);
            med_hgm = Arithmetic.getBit(val, MED_HGM);
            lna_cap_array = LNA_CAP_ARRAY[(val & 0x00c0) >> 6];
            rxmix_tail = RXMIX_TAIL[(val & 0x0030) >> 4];
            rxmix_vcm = RXMIX_VCM[(val & 0x000c) >> 2];
            rxmix_current = RXMIX_CURRENT[(val & 0x0003)];
        }

        protected void reset() {
            write(0x0a56);
        }
    }

    /**
     * The Frequency Synthesizer Control and Status register configures how many consecutive reference clock periods
     * required to indicate a lock, the RF operating frequency, as well as showing the status of calibration and
     * frequency synthesizer lock status.
     */
    protected class FreqSynthRegister extends RadioRegister {
        public final int CAL_DONE = 13;
        public final int CAL_RUNNING = 12;
        public final int LOCK_LENGTH = 11;
        public final int LOCK_STATUS = 10;
        public final int[] LOCK_THR = {64, 128, 256, 512};
        boolean cal_done;
        boolean cal_running;
        boolean lock_length;
        boolean lock_status;
        int lock_thr;
        int freq;

        FreqSynthRegister() {
            super("Frequency Synthesizer Control and Status", 0x4165);
            decode(value);
        }

        protected void decode(int val) {
            cal_done = Arithmetic.getBit(val, CAL_DONE);
            cal_running = Arithmetic.getBit(val, CAL_RUNNING);
            lock_length = Arithmetic.getBit(val, LOCK_LENGTH);
            lock_status = Arithmetic.getBit(val, LOCK_STATUS);
            lock_thr = LOCK_THR[(val & 0xc000) >> 14];
            freq = (val & 0x03ff);
        }

        protected void reset() {
            write(0x03ff);
        }

        protected void setFreq(int val) {
            freq = val;
        }
    }

    /**
     * I/O Configuration LegacyRegister 0 configures the polarity of the SFD, CCA, FIFO, FIFOP pins, beacon frames,
     * and the FIFOP threshold required to go high
     */
    protected class IORegister0 extends RadioRegister {
        public final int BCN_ACCEPT = 11;
        public final int FIFO_POLARITY = 10;
        public final int FIFOP_POLARITY = 9;
        public final int SFD_POLARITY = 8;
        public final int CCA_POLARITY = 7;
        int fifop_thr;
        boolean bcn_accept;
        boolean fifo_polarity;
        boolean fifop_polarity;
        boolean sfd_polarity;
        boolean cca_polarity;

        IORegister0() {
            super("I/O Config LegacyRegister 0", 0x0040);
            decode(value);
        }

        protected void decode(int val) {
            fifop_thr = (val & 0x007f);
            bcn_accept = Arithmetic.getBit(val, BCN_ACCEPT);
            fifo_polarity = Arithmetic.getBit(val, FIFO_POLARITY);
            fifop_polarity = Arithmetic.getBit(val, FIFOP_POLARITY);
            sfd_polarity = Arithmetic.getBit(val, SFD_POLARITY);
            cca_polarity = Arithmetic.getBit(val, CCA_POLARITY);
        }

        protected void reset() {
            write(0x0040);
        }
    }

    /**
     * I/O Configuration LegacyRegister 1 configures the HSSD module as well as adjusting multiplexer settings of the
     * SFD and CCA pins
     */
    protected class IORegister1 extends RadioRegister {
        int hssd_src = 0;
        int sfdmux = 0;
        int ccamux = 0;

        IORegister1() {
            super("I/O Config LegacyRegister 1", 0x0000);
            decode(value);
        }

        protected void decode(int val) {
            hssd_src = (val & 0x1c00) >> 10;
            sfdmux = (val & 0x03e0) >> 5;
            ccamux = (val & 0x001f);
        }

        protected void reset() {
            write(0x0000);
        }
    }

    protected class TXFIFORegister extends RadioRegister {
        TXFIFORegister() {
            super("TXFIFO LegacyRegister", 0x0000);
        }

        protected void decode(int val) {
            this.write(Arithmetic.low(val));  //byte to be added to buffer is sent on lower byte of word
        }

        public void write(byte val) {
            TXFIFOBuf.write(val);
        }

        public byte read() {
            value = TXFIFOBuf.read();
            return (byte) value;
        }
    }

    protected class RXFIFORegister extends RadioRegister {
        RXFIFORegister() {
            super("RXFIFO LegacyRegister", 0x0000);
        }

        protected void decode(int val) {
            this.write(Arithmetic.high(val)); //byte to be added to buffer is sent on upper byte of word
        }

        public void write(byte val) {
            RXFIFOBuf.write(val);
        }

        protected byte read() {
            value = RXFIFOBuf.read();
            return (byte) value;
        }
    }

    protected class TXFIFOBuffer {
        int counter;
        byte lengthField;
        int remCount;
        final byte[] txBuffer;

        public TXFIFOBuffer() {
            counter = 0;
            remCount = 0;
            txBuffer = new byte[128];
        }

        public void write(byte val) {
            if (counter < 127) {
                for (int i = counter + 1; i > 0; i--) {
                    txBuffer[i] = txBuffer[i - 1];
                }
                txBuffer[0] = val;
                if (counter == 0)
                    lengthField = (byte) (val - 2);
                counter++;
            }
        }

        public void writeRAM(byte ind, byte val) {
            txBuffer[ind] = val;
        }

        public byte readRAM(byte ind) {
            return txBuffer[ind];
        }

        public byte read() {
            if (counter < 0) {
                if (radioPrinter.enabled)
                    radioPrinter.println("Error reading TXFIFO, empty buffer");
                return 0;
            } else {
                remCount++;
                counter--;
                byte send = txBuffer[counter];

                if (remCount == lengthField + 1) {
                    remCount = 0;
                    lengthField = txBuffer[1];
                }
                txBuffer[counter] = 0;
                return send;
            }
        }

        public void clearBuffer() {
            lengthField = 0;
            counter = 0;
            remCount = 0;
            Arrays.fill(txBuffer, (byte)0);
        }
    }

    protected class RXFIFOBuffer {
        int counter;
        byte lengthField;
        int remCount;
        final byte[] rxBuffer;

        public RXFIFOBuffer() {
            lengthField = 0;
            counter = 0;
            remCount = 0;
            rxBuffer = new byte[128];
        }

        public void write(byte val) {
            if (counter > 127) {
                controller.setPinValue("FIFO", false);
                controller.setPinValue("FIFOP", true);  //signals overflow to MCU
                //counter must be reset by SFLUSHRX_cmd
                return;
            }
            if (counter == 0)
                lengthField = val;
            if (!(controller.getPinValue("FIFO")) && controller.getPinValue("FIFOP")) {
                for (int i = counter + 1; i > 0; i--) {
                    rxBuffer[i] = rxBuffer[i - 1];
                }
                counter++;
                controller.setPinValue("FIFO", true);  //there are bytes in RXFIFO
            }
        }

        public void writeRAM(byte ind, byte val) {
            rxBuffer[ind] = val;
        }

        public byte readRAM(byte ind) {
            return rxBuffer[ind];
        }

        public byte read() {
            if (counter < 0) {
                if (radioPrinter.enabled)
                    radioPrinter.println("Error reading RXFIFO, empty buffer");
                return 0;
            } else {
                counter--;
                remCount++;
                if (remCount == lengthField + 1) {
                    remCount = 0;
                    lengthField = rxBuffer[1];
                }
                if (counter == 0)
                    controller.setPinValue("FIFO", false);  //just emptied RXFIFO
                if (counter < IOCFG0_reg.fifop_thr)
                    controller.setPinValue("FIFOP", false); //after removal will be under FIFOP threshold
                byte send = rxBuffer[counter];
                rxBuffer[counter] = 0;
                return send;
            }
        }

        public void clearBuffer() {
            Arrays.fill(rxBuffer, (byte)0);
            controller.setPinValue("FIFO", false);//RXFIFO empty
            counter = 0;
            remCount = 0;
            lengthField = 0;
        }

        public int countZero() {
            return 128 - counter;
        }
    }

    protected class SnopCommand extends RadioStrobe {
        boolean switched;

        SnopCommand() {
            super("SNOP Command Strobe");
            switched = false;
        }

        protected void action() {
            boolean XOSC16_M = SXOSCON_cmd.switched && !SXOSCOFF_cmd.switched;
            boolean TX_UNDERFLOW = (128 - RXFIFOBuf.countZero()) < RXFIFOBuf.lengthField;
            /*something describing length field*/
            boolean ENC_BUSY = SRXDEC_cmd.switched || STXENC_cmd.switched;
            boolean TX_ACTIVE = SRXON_cmd.switched || STXON_cmd.switched;
            boolean LOCK = false;//doesKeithGetIt ?  PLL.locked: false;  //not sure how to implement yet
            boolean RSSI_VALID = (Arithmetic.low(RSSI_reg.value) != -128);
            //bits 7 and 0 are reserved bits in the Status Byte
            statusByte = Arithmetic.clearBit(statusByte, 7);
            statusByte = Arithmetic.setBit(statusByte, 6, XOSC16_M);
            statusByte = Arithmetic.setBit(statusByte, 5, TX_UNDERFLOW);
            statusByte = Arithmetic.setBit(statusByte, 4, ENC_BUSY);
            statusByte = Arithmetic.setBit(statusByte, 3, TX_ACTIVE);
            statusByte = Arithmetic.setBit(statusByte, 2, LOCK);
            statusByte = Arithmetic.setBit(statusByte, 1, RSSI_VALID);
            statusByte = Arithmetic.clearBit(statusByte, 0);
        }
    }

    protected class SxosconCommand extends RadioStrobe {
        boolean switched;

        SxosconCommand() {
            super("Crystal Osc. Enable Command Strobe");
            switched = false;
        }

        protected void action() {
            switched = true;    //enable Crystal Oscillator
            SXOSCOFF_cmd.switched = false;
            /*
            MANAND_reg.write(0,7);    //set bit 7 in MANAND (XOSC16M_PD) to 0
            MANAND_reg.write(0,14);   //set bit 14 in MANAND (BIAS_PD) to 0
            */
        }
    }

    protected class StxcalCommand extends RadioStrobe {
        boolean switched;

        StxcalCommand() {
            super("Enable and calibrate freq synth Command Strobe");
            switched = false;
        }

        protected void action() {
            switched = true;
            SRXON_cmd.switched = false;
            STXON_cmd.switched = false;
            //TODO:implement command which goes from RX/TX to a wait state where only the frequency synthesizer runs
        }
    }

    protected class SrxonCommand extends RadioStrobe {
        boolean switched;

        SrxonCommand() {
            super("Enable RX Command Strobe");
            switched = false;
        }

        protected void action() {
            switched = true;
            STXCAL_cmd.switched = false;
            SRFOFF_cmd.switched = false;
            SXOSCOFF_cmd.switched = false;
            //RX is enabled
        }
    }

    protected class StxonCommand extends RadioStrobe {
        boolean switched;

        StxonCommand() {
            super("Enable TX Command Strobe");
            switched = false;
        }

        protected void action() {
            switched = true;
            STXCAL_cmd.switched = false;
            SRFOFF_cmd.switched = false;
            SXOSCOFF_cmd.switched = false;
        }
    }

    protected class StxonccaCommand extends RadioStrobe {
        boolean switched;

        StxonccaCommand() {
            super("Calibrate and Enable TX on CCA Command Strobe");
            switched = false;
        }

        protected void action() {
            switched = true;
            STXON_cmd.action();               //if SPI_SEC_MODE != 0, should also start in-line encryption....
        }
    }

    protected class SrfoffCommand extends RadioStrobe {
        boolean switched;

        SrfoffCommand() {
            super("Disable RX/TX and Freq. Synth. Command Strobe");
            switched = false;
        }

        protected void action() {
            switched = true;
            SRXON_cmd.switched = false;
            STXON_cmd.switched = false;
            STXONCCA_cmd.switched = false;
            STXCAL_cmd.switched = false;
            //disables RX/TX and Freq Synth.
        }
    }

    protected class SxoscoffCommand extends RadioStrobe {
        boolean switched;

        SxoscoffCommand() {
            super("Turn off crystal osc. and RF Command Strobe");
            switched = false;
        }

        protected void action() {
            switched = true;
            SXOSCON_cmd.switched = false;
        }
    }

    protected class SflushrxCommand extends RadioStrobe {
        boolean switched;

        SflushrxCommand() {
            super("flush RXFIFO buffer Command Strobe");
            switched = false;
        }

        public void action() {
            RXFIFOBuf.clearBuffer();
            controller.setPinValue("FIFO", false);   //clears RXFIFO overflow
            controller.setPinValue("FIFOP", false);  //clears RXFIFO overflow
        }
    }

    protected class SflushtxCommand extends RadioStrobe {
        boolean switched;

        SflushtxCommand() {
            super("Flush TXFIFO buffer Command Strobe");
            switched = false;
        }

        public void action() {
            TXFIFOBuf.clearBuffer();
        }
    }

    protected class SackCommand extends RadioStrobe {
        boolean switched;

        SackCommand() {
            super("Send Ack. frame Commmand Strobe");
            switched = false;
        }

        public void action() {
            //send acknowledging frame
        }
    }

    protected class SackpendCommand extends RadioStrobe {
        boolean switched;

        SackpendCommand() {
            super("Send Ack. frame w/ Pend. set Command Strobe");
            switched = false;
        }

        public void action() {
            //send acknowledging frame with pending field set
        }
    }

    protected class SrxdecCommand extends RadioStrobe {
        boolean switched;

        SrxdecCommand() {
            super("Start RXFIFO in-line decryption Command Strobe");
            switched = false;
        }

        public void action() {
            //start RXFIFO in-line decryption/authentication as set by SPI_SEC_MODE
        }
    }

    protected class StxencCommand extends RadioStrobe {
        boolean switched;

        StxencCommand() {
            super("Start TXFIFO in-lin encryption Command Strobe");
            switched = false;
        }

        public void action() {
            //start TXFIFO in-line encryption/authentication as set by SPI_SEC_MODE
        }
    }

    protected class SaesCommand extends RadioStrobe {
        boolean switched;

        SaesCommand() {
            super("AES Stand alone encryption Strobe");
            switched = false;
        }

        public void action() {
            //  SPI_SEC_MODE is not required to be 0, but the encrypt. module must be idle; else strobe is ignored
        }
    }

    /**
     * A CC2420 Controller class for the ATMega microcontroller family. Installing an ATMega128 into this
     * class connects the microcontroller to this radio. Data is communicated over the SPI interface, on which
     * the CC2420 is the master. RSSI data from the CC1000 is available to the ATMega128 though the ADC
     * (analog to digital converter).
     */
    public class ATMegaController implements Radio.RadioController, ADC.ADCInput, SPIDevice {
        public CC2420Radio.PinInterface pinReader;
        private SPIDevice spiDevice;
        private final Simulator.Printer printer;
        private int bytesToCome;
        private int regAdd;
        private int ramBank;
        private byte highByte;
        private boolean transmitting;
        private boolean receiving;
        private TransmitToAir transmittingBytes;
        private StartReceiving receivingBytes;

        public void setPinValue(String pin, boolean val) {
            if (pin.equals("FIFO")) {
                pinReader.readValueFIFO = val;
                pinReader.print("CC2420: writing " + val + " to FIFO pin");
            }
            if (pin.equals("FIFOP")) {
                pinReader.readValueFIFOP = val;
                pinReader.print("CC2420: writing " + val + " to FIFOP pin");
            }
            if (pin.equals("CCA")) {
                pinReader.readValueCCA = val;
                pinReader.print("CC2420: writing " + val + " to CCA pin");
            }
            if (pin.equals("SFD")) {
                pinReader.readValueSFD = val;
                pinReader.print("CC2420: writing " + val + " to SFD pin");
            }
            if (pin.equals("OldCS")) {
                pinReader.readOldCS = val;
            }
        }

        public boolean getPinValue(String pin) {
            if (pin.equals("FIFO"))
                return pinReader.readValueFIFO;
            if (pin.equals("FIFOP"))
                return pinReader.readValueFIFOP;
            if (pin.equals("CCA"))
                return pinReader.readValueCCA;
            if (pin.equals("SFD"))
                return pinReader.readValueSFD;
            if (pin.equals("CS"))
                return pinReader.readValueCS;
            if (pin.equals("OldCS"))
                return pinReader.readOldCS;
            return false;
        }

        ATMegaController() {
            printer = sim.getPrinter("radio.cc2420.data");
            bytesToCome = 3;
            highByte = 0;
            //CSnOld = true;
        }

        public void enable() {
        }

        public void disable() {
        }

        public long transmitFrameStart(long startTime, long originalTime) {
            for (int i = 0; i < (MDMCTRL0_reg.prelength + 1); i++) {  //send preamble
                new TransmitLater(new Transmission((byte) 0x00, 0, startTime), (int) ((startTime - originalTime) / Radio.TRANSFER_TIME));
                startTime += (Radio.TRANSFER_TIME);
            }
            byte SFD = Arithmetic.high(SYNC_reg.syncword);
            new TransmitLater(new Transmission(SFD, 0, startTime), (int) ((startTime - originalTime) / Radio.TRANSFER_TIME));
            startTime += (Radio.TRANSFER_TIME);
            // 2 bytes for FCF, 1 for Data Seq Numb, 2 bytes for Address Recog (can be from 0-20)
            //2 bytes for the Frame check Sequence after the payload = 7 + TXFIFOBuf.lengthField
            return startTime;
        }

        public class TransmitToAir implements Simulator.Event {
            int i;
            long startTime = sim.getState().getCycles();
            long originalTime = startTime;

            TransmitToAir() {
                transmitting = true;
                i = 0;
                startTime = transmitFrameStart(startTime, originalTime);
                controller.setPinValue("SFD", true);
                sim.insertEvent(this, 3 * (Radio.TRANSFER_TIME));
                startTime += (Radio.TRANSFER_TIME);
            }

            public void end() {
                transmitting = false;
                sim.removeEvent(this);
            }

            public void fire() {
                if (i <= TXFIFOBuf.lengthField) {
                    byte element = TXFIFOBuf.read();
                    new Transmit(new Transmission(element, 0, startTime));
                    startTime += (Radio.TRANSFER_TIME);
                    sim.insertEvent(this, (Radio.TRANSFER_TIME));
                    i++;
                } else if (i == (TXFIFOBuf.lengthField + 1)) {
                    controller.setPinValue("SFD", false);
                    i++;
                    sim.insertEvent(this, (Radio.TRANSFER_TIME));
                } else {
                    //controller.setPinValue("SFD",false);
                    //TODO: send back real 2 byte FCS instead of this
                    new TransmitLater(new Transmission((byte) 0x00, 0, startTime), (int) ((startTime - originalTime) / Radio.TRANSFER_TIME));
                    startTime += Radio.TRANSFER_TIME;
                    new TransmitLater(new Transmission((byte) 0x00, 0, startTime), (int) ((startTime - originalTime) / Radio.TRANSFER_TIME));
                    startTime += Radio.TRANSFER_TIME;
                    transmitting = false;
                }
            }
        }

        public class StartReceiving implements Simulator.Event {
            byte nextByte;
            ContinueReceiving cont;

            StartReceiving() {
                receiving = true;
                nextByte = 0;
                start();
            }

            public void start() {
                nextByte = transmitFrame().data;
                if (nextByte == Arithmetic.high(SYNC_reg.syncword)) {
                    cont = new ContinueReceiving();
                } else
                    sim.insertEvent(this, Radio.TRANSFER_TIME);
            }

            public void end() {
                if (cont != null)
                    cont.end();
                sim.removeEvent(this);
            }

            public void fire() {
                nextByte = transmitFrame().data;
                if (nextByte == Arithmetic.high(SYNC_reg.syncword)) {
                    cont = new ContinueReceiving();
                } else
                    sim.insertEvent(this, Radio.TRANSFER_TIME);
            }
        }

        public class ContinueReceiving implements Simulator.Event {
            byte nextByte;
            byte length;
            int counter;

            ContinueReceiving() {
                length = 0;
                counter = 0;
                nextByte = 0;
                sim.insertEvent(this, Radio.TRANSFER_TIME);
            }

            public void end() {
                receiving = false;
                sim.removeEvent(this);
            }

            public void fire() {
                nextByte = transmitFrame().data;
                if (counter == 0) {
                    length = nextByte;
                    sim.insertEvent(this, Radio.TRANSFER_TIME);
                } else {
                    RXFIFOBuf.write(nextByte);
                    print("CC2420: Writing " + StringUtil.toMultirepString(nextByte, 8) + " to RXFIFO");
                }
                if (counter < length)
                    sim.insertEvent(this, Radio.TRANSFER_TIME);
                else if (counter == length) {
                    setPinValue("SFD", false);
                    receiving = false;
                }
                counter++;
            }
        }

        /**
         * <code>receiveFrame</code> receives an <code>SPIFrame</code> from a connected device.
         */
        public void receiveFrame(SPI.Frame frame) {
            bytesToCome--;
            if (!controller.getPinValue("CS") && controller.getPinValue("OldCS")) {  //then this is a new transfer of bytes together
                controller.setPinValue("OldCS", false);  //in order to not come here next time
                bytesToCome = 2;
                regAdd = frame.data;
            }
            if (regAdd < 0) {
                ramAccess(frame);
            } else if ((regAdd & 0x3F) >= 0 && (regAdd & 0x3F) <= 0x0E) {  //if command strobe, then do action associated with it
                bytesToCome = 3;
                registers[regAdd & 0x3F].action();   //do actions associated with command strobe
                if ((regAdd & 0x3F) != 0x00)
                    registers[0x00].action();      //if command strobe wasnt SNOP, then fig. StatusByte
                spiDevice.receiveFrame(SPI.newFrame(statusByte));   //send back statusbyte
                if (!transmitting && (STXON_cmd.switched || STXONCCA_cmd.switched)) {
                    if (receiving) {
                        receivingBytes.end();
                    }
                    transmittingBytes = new TransmitToAir();
                }
                if (!receiving && SRXON_cmd.switched) {
                    receivingBytes = new StartReceiving();
                }
                if (receiving && (SRFOFF_cmd.switched || SXOSCOFF_cmd.switched)) {
                    receivingBytes.end();
                    transmittingBytes.end();
                }
            } else if (((regAdd & 0x3F) > 0x0F) && ((regAdd & 0x3F) < 0x40)) {        //if register access, then read or write accordingly
                regAccess(frame);
            } else {
                bytesToCome = 3;
                print("CC2420: discarding " + StringUtil.toMultirepString(frame.data, 8) + " from SPI");

            }
            if (!controller.getPinValue("CS") && bytesToCome == 0) { //a transfer of bytes still going, keep same regAdd
                bytesToCome = 2;
            }
        }

        public void ramAccess(SPI.Frame frame) {
            boolean bit6 = Arithmetic.getBit(6, ramBank);
            boolean bit7 = Arithmetic.getBit(7, ramBank);
            boolean TXBank = !(bit6 && bit7);  //if bit 6 and 7 not set
            boolean RXBank = bit6 && !bit7;
            boolean secBank = !bit6 && bit7;

            if (bytesToCome == 2) {
                registers[0x00].action();
                spiDevice.receiveFrame(SPI.newFrame(statusByte)); //Sends back the statusByte through SPI
            } else if (bytesToCome == 1) {
                ramBank = frame.data;
            } else if (bytesToCome == 0) {
                byte index = (byte) (regAdd & 0x3F);
                if (TXBank) {
                    if (Arithmetic.getBit(5, ramBank)) {  //then just read RAM
                        new RAMread(TXFIFOBuf.readRAM(index));
                        bytesToCome++; //will keep reading unless CS goes down
                        regAdd++; //add 1 to address for next time
                    } else { //read and write
                        new RAMread(TXFIFOBuf.readRAM(index));
                        TXFIFOBuf.writeRAM(index, frame.data);
                        bytesToCome++;
                        regAdd++;
                    }
                } else if (RXBank) {
                    if (Arithmetic.getBit(5, ramBank)) {  //then just read RAM
                        new RAMread(RXFIFOBuf.readRAM(index));
                        bytesToCome++;
                        regAdd++;
                    } else { //read and write
                        new RAMread(RXFIFOBuf.readRAM(index));
                        RXFIFOBuf.writeRAM(index, frame.data);
                        bytesToCome++;
                        regAdd++;
                    }
                } else if (secBank) {
                    //TODO: implement me, need to have RAM access to security bank
                }
            }
        }

        public class RAMread implements Simulator.Event {
            byte value;

            RAMread(byte val) {
                value = val;
                sim.insertEvent(this, 4);
            }

            public void fire() {
                spiDevice.receiveFrame(SPI.newFrame(value));
            }
        }

        public void regAccess(SPI.Frame frame) {
            if (bytesToCome == 2) {
                registers[0x00].action();
                spiDevice.receiveFrame(SPI.newFrame(statusByte));
                if ((regAdd & 0x3F) == TXFIFO || (regAdd & 0x3F) == RXFIFO)
                    bytesToCome--;
            } else if (bytesToCome == 1) {
                highByte = frame.data;
            } else if (bytesToCome == 0) {
                byte lowByte = frame.data;
                if ((regAdd & 0x3F) == TXFIFO || (regAdd & 0x3F) == RXFIFO)
                    bytesToCome++;
                else
                    bytesToCome = 3;
                if (Arithmetic.getBit(regAdd, 6)) { //if set, read from register address, send back 16 bits
                    new ReadReg(regAdd, highByte, lowByte);
                } else {
                    new WriteReg(regAdd, highByte, lowByte);
                }
            }
        }

        /**
         * The <code>ReadReg</code> event is used when a LegacyRegister Read instruction is issused and sends either the
         * first or second data bytes in the given address  back to the MCU across the SPI
         */
        private class ReadReg implements Simulator.Event {
            final byte regValueHigh;
            final byte regValueLow;
            final byte regAdd;

            ReadReg(int regAdd, byte regValueHigh, byte regValueLow) {
                this.regAdd = (byte)(regAdd & 0x3F);
                this.regValueHigh = regValueHigh;
                this.regValueLow = regValueLow;
                firing();
            }

            public void firing() {
                if (regAdd == TXFIFO || regAdd == RXFIFO) { //the low byte will hold FIFO data
                    spiDevice.receiveFrame(SPI.newFrame(registers[regAdd].read()));
                } else {    //means high byte
                    spiDevice.receiveFrame(SPI.newFrame(Arithmetic.high(registers[regAdd].value)));
                    sim.insertEvent(this, 4);
                    //sends upper byte of register contents back to MCU
                }
            }

            public void fire() {
                //means low byte
                spiDevice.receiveFrame(SPI.newFrame(Arithmetic.low(registers[regAdd].value)));
                //sends lower byte of LegacyRegister contents back to MCU
            }
        }

        /**
         * The <code>WriteReg</code> event is used when a LegacyRegister Write instruction is issued and writes either the
         * first or second data byte to the register from data received through the SPI
         */
        private class WriteReg implements Simulator.Event {
            final byte regValueHigh;
            final byte regValueLow;
            final byte regAdd;

            WriteReg(int regAdd, byte regValueHigh, byte regValueLow) {
                this.regAdd = (byte)(regAdd & 0x3F);
                this.regValueHigh = regValueHigh;
                this.regValueLow = regValueLow;
                firing();
            }

            public void firing() {
                if (regAdd == RXFIFO || regAdd == TXFIFO) {

                    registers[regAdd].decode(Arithmetic.word(regValueLow, (byte) 0));
                    //If this was a FIFO access, byte to write would come on regValueLow byte
                    //writes one byte to the FIFO buffer
                } else {
                    registers[regAdd].decode(Arithmetic.word(regValueHigh, regValueLow));
                    //writes and decodes new value of register
                }
            }

            public void fire() {
                // TODO: why is this empty?
            }
        }

        /**
         * <code>Transmit</code> is an event that transmits a packet of data after a one bit period delay.
         */
        protected class Transmit implements Simulator.Event {

            Radio.Transmission packet;

            Transmit(Radio.Transmission packet) {
                this.packet = packet;
                sim.insertEvent(this, Radio.TRANSFER_TIME / 8);
                //sim.insertEvent(this, Radio.TRANSFER_TIME );
            }

            public void fire() {
                print("CC2420: transmitting " + StringUtil.toMultirepString(packet.data, 8));
                // send packet into air...
                if (air != null)
                    air.transmit(CC2420Radio.this, packet);
                probes.fireAtTransmit(CC2420Radio.this, packet);
            }
        }

        protected class TransmitLater implements Simulator.Event {
            //this class is just like Transmit except that it allows for the transfer of
            //data at different lengths of time, so if you create multiple instances
            //of this class in a code, you can just increase the multiple each time so that
            //they wont all fire at the same time.
            Radio.Transmission packet;

            TransmitLater(Radio.Transmission packet, int multiple) {
                this.packet = packet;
                sim.insertEvent(this, multiple * (Radio.TRANSFER_TIME / 8));
                //sim.insertEvent(this, multiple * (Radio.TRANSFER_TIME ) );
            }

            public void fire() {
                print("CC2420: transmitting " + StringUtil.toMultirepString(packet.data, 8));
                if (air != null)
                    air.transmit(CC2420Radio.this, packet);
                probes.fireAtTransmit(CC2420Radio.this, packet);
            }
        }

        /**
         * Transmits an <code>SPIFrame</code> to be received by the connected device. This frame is either the
         * last byte of data received or a zero byte.
         */
        public SPI.Frame transmitFrame() {
            SPI.Frame frame;
            if (!SRXON_cmd.switched) {
                frame = SPI.ZERO_FRAME;
            } else {
                byte data = air != null ? air.readChannel(CC2420Radio.this) : 0;
                frame = SPI.newFrame(data);
                print("CC2420: received " + StringUtil.toMultirepString(frame.data, 8));
            }
            return frame;
        }

        public void connect(SPIDevice d) {
            spiDevice = d;
        }

        public int getLevel() {
            // ask the air for the current RSSI value
            if (air != null)
                return air.sampleRSSI(CC2420Radio.this);
            else
                return ADC.VBG_LEVEL; // return a default value of some sort
        }

        public void install(Microcontroller mcu) {
            pinReader = new CC2420Radio.PinInterface(mcu);
            if (mcu instanceof ATMegaFamily) {
                ATMegaFamily atm = (ATMegaFamily) mcu;
                // get ADC device and connect
                ADC adc = (ADC) atm.getDevice("adc");
                adc.connectADCInput(this, 0);
                // get SPI device and connect
                SPI spi = (SPI) atm.getDevice("spi");
                spi.connect(this);
                connect(spi); // don't forget to connect ourselves to this device
            } else {
                throw Util.failure("CC2420: only ATMegaFamily is supported");
            }
        }

        public void print(String message) {
            if (printer.enabled) {
                printer.println(message);
            }
        }
    }

    public boolean isListening() {
        boolean listening = receiving;
        if (radioPrinter.enabled) {
            radioPrinter.println("CC2420: Listening? " + listening);
        }
        return listening;
    }

    public class PinInterface {
        byte address;
        short data;
        boolean value;
        boolean write;
        byte readData;
        boolean readValue;
        int bitsRead;
        boolean readValueCS;
        boolean readOldCS;
        boolean readValueFIFO;
        boolean readValueFIFOP;
        boolean readValueCCA;
        boolean readValueSFD;
        boolean SCLK;
        boolean MOSI;
        boolean MISO;
        Simulator.Printer readerPrinter;

        PinInterface(Microcontroller mcu) {
            readerPrinter = sim.getPrinter("radio.cc2420.pinconfig");
            mcu.getPin(11).connect(new SCLKOutput());
            mcu.getPin(12).connect(new MOSIOutput());
            mcu.getPin(13).connect(new MISOInput());
            mcu.getPin(17).connect(new FIFOInput());
            mcu.getPin(8).connect(new FIFOPInput());
            mcu.getPin(31).connect(new CCAInput());
            mcu.getPin(29).connect(new SFDInput());
            mcu.getPin(10).connect(new CSOutput());
        }

        protected abstract class Output implements Microcontroller.Pin.Output {
            final String name;

            Output(String n) {
                name = n;
            }

            public void enableOutput() {
                print("CC240: enabled "+name+" pin");
            }

            public void disableOutput() {
                print("CC240: disabled "+name+" pin");
            }
        }

        protected abstract class Input implements Microcontroller.Pin.Input {
            final String name;

            Input(String n) {
                name = n;
            }

            public void enableInput() {
                print("CC240: enabled "+name+" pin");
            }

            public void disableInput() {
                print("CC240: disabled "+name+" pin");
            }
        }

        protected class SCLKOutput extends Output {

            SCLKOutput() {
                super("SCLK");
            }

            public void write(boolean val) {
                print("CC2420: writing " + val + " to SCLK pin");
                SCLK = val;
            }
        }

        protected class MISOInput extends Input {

            MISOInput() {
                super("MISO");
            }

            public boolean read() {
                print("CC2420: reading MISO pin value: " + MISO);
                return MISO;
            }
        }

        protected class MOSIOutput extends Output {
            public byte value;
            int counter;

            MOSIOutput() {
                super("MOSI");
                counter = 0;
                value = 0;
            }

            public void write(boolean val) {
                print("CC2420: writing " + val + " to MOSI pin");
                MOSI = val;
                if (counter < 8) {
                    value = (byte) Arithmetic.setBit(1, counter, val);
                    counter++;
                    if (counter == 7) {
                        counter = 0;
                        //receiveFrame(SPI.newFrame(value));
                    }
                }
            }
        }

        protected class CSOutput extends Output {

            CSOutput() {
                super("CS");
            }

            public void write(boolean val) {
                print("CC2420: writing CS pin value : " + val);
                readOldCS = readValueCS;
                readValueCS = val;
            }
        }

        public class FIFOInput extends Input {

            FIFOInput() {
                super("FIFO");
            }

            public boolean read() {
                print("CC2420: reading FIFO pin value");
                return readValueFIFO;
            }
        }

        protected class FIFOPInput extends Input {
            FIFOPInput() {
                super("FIFOP");
            }

            public boolean read() {
                print("CC2420: reading FIFOP pin value");
                return readValueFIFOP;
            }
        }

        protected class CCAInput extends Input {
            CCAInput() {
                super("CCA");
            }

            public boolean read() {
                print("CC2420: reading CCA pin value");
                return readValueCCA;
            }
        }

        protected class SFDInput extends Input {
            SFDInput() {
                super("SFD");
            }

            public boolean read() {
                print("CC2420: reading SFD pin value");
                return readValueSFD;
            }
        }

        public void print(String message) {
            if (readerPrinter.enabled) {
                readerPrinter.println(message);
            }
        }
    }

    /**
     * Get the <code>Simulator</code> on which this radio is running.
     */
    public Simulator getSimulator() {
        return sim;
    }

    /**
     * get the transmission power
     *
     * @see avrora.sim.radio.Radio#getPower()
     */
    public int getPower() {
        return TXCTRL_reg.pa_level;
    }

    /**
     * get transmission frequency
     *
     * @see avrora.sim.radio.Radio#getFrequency()
     */
    public double getFrequency() {
        // according to CC1000 handbook
        // fRef = fXosc / REFDIV
        // frequency = fRef * ( ( FREQ + 8192 ) / 16384 )
        return (double)(2048 + FSCTRL_reg.freq);
    }

    public RadioAir getAir() {
        return air;
    }

    public void setAir(RadioAir nair) {
        air = nair;
    }
}