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
import avrora.sim.Clock;
import avrora.util.Arithmetic;

/**
 * The <code>Timer16Bit</code> class emulates the functionality and behavior of a 16-bit timer on the
 * Atmega128. It has several control and data registers and can fire up to six different interrupts
 * depending on the mode that it has been put into. It has three output compare units and one input
 * capture unit. UNIMPLEMENTED: input capture unit.
 *
 * @author Daniel Lee
 */
public abstract class Timer16Bit extends AtmelInternalDevice {

    // Timer/Counter Modes of Operations
    public static final int MODE_NORMAL = 0;
    public static final int MODE_PWM_PHASE_CORRECT_8_BIT = 1;
    public static final int MODE_PWM_PHASE_CORRECT_9_BIT = 2;
    public static final int MODE_PWM_PHASE_CORRECT_10_BIT = 3;
    public static final int MODE_CTC_OCRnA = 4;
    public static final int MODE_FASTPWM_8_BIT = 5;
    public static final int MODE_FASTPWM_9_BIT = 6;
    public static final int MODE_FASTPWM_10_BIT = 7;
    public static final int MODE_PWM_PNF_ICRn = 8;
    public static final int MODE_PWM_PNF_OCRnA = 9;
    public static final int MODE_PWN_PHASE_CORRECT_ICRn = 10;
    public static final int MODE_PWN_PHASE_CORRECT_OCRnA = 11;
    public static final int MODE_CTC_ICRn = 12;
    // 13 is reserved
    public static final int MODE_FASTPWM_ICRn = 14;
    public static final int MODE_FASTPWM_OCRnA = 15;


    public static final int MAX = 0xffff;
    public static final int BOTTOM = 0x0000;

    final ControlRegisterA TCCRnA_reg;
    final ControlRegisterB TCCRnB_reg;
    final ControlRegisterC TCCRnC_reg;

    final State.RWIOReg TCNTnH_reg; // timer counter registers
    final TCNTnRegister TCNTnL_reg;
    final PairedRegister TCNTn_reg;

    final BufferedRegister OCRnAH_reg; // output compare registers
    final BufferedRegister OCRnAL_reg;
    final PairedRegister OCRnA_reg;

    final BufferedRegister OCRnBH_reg;
    final BufferedRegister OCRnBL_reg;
    final PairedRegister OCRnB_reg;

    final BufferedRegister OCRnCH_reg;
    final BufferedRegister OCRnCL_reg;
    final PairedRegister OCRnC_reg;

    final State.RWIOReg highTempReg;

    final State.RWIOReg ICRnH_reg; // input capture registers
    final State.RWIOReg ICRnL_reg;
    final PairedRegister ICRn_reg;

    final AtmelMicrocontroller.Pin outputComparePinA;
    final AtmelMicrocontroller.Pin outputComparePinB;
    final AtmelMicrocontroller.Pin outputComparePinC;

    final ATMegaFamily.MaskRegister ETIMSK_reg;
    final ATMegaFamily.FlagRegister ETIFR_reg;

    final Ticker ticker;

    boolean timerEnabled;
    boolean countUp;
    int timerModeA;
    int timerModeB;
    int timerMode;
    long period;

    boolean blockCompareMatch;

    Simulator.Printer timerPrinter;

    protected final Clock externalClock;
    Clock timerClock;

    // information about registers and flags that specifies
    // which specific registers this 16-bit timer interacts with

    final int n; // number of timer. 1 for Timer1, 3 for Timer3

    // these are the offsets on registers corresponding to these flags
    int OCIEnA;
    int OCIEnB;
    int OCIEnC;
    int TOIEn;
    int TOVn;
    int OCFnA;
    int OCFnB;
    int OCFnC;
    int ICFn;

    protected ATMegaFamily.FlagRegister xTIFR_reg;
    protected ATMegaFamily.MaskRegister xTIMSK_reg;

    protected int[] periods;

    // This method should be overloaded to initialize the above values.
    protected abstract void initValues();

    protected Timer16Bit(int n, AtmelMicrocontroller m) {
        super("timer"+n, m);
        this.n = n;

        initValues();

        ticker = new Ticker();

        highTempReg = new State.RWIOReg();

        TCCRnA_reg = new ControlRegisterA();
        TCCRnB_reg = new ControlRegisterB();
        TCCRnC_reg = new ControlRegisterC();

        TCNTnH_reg = new State.RWIOReg();
        TCNTnL_reg = new TCNTnRegister();
        TCNTn_reg = new PairedRegister(TCNTnH_reg, TCNTnL_reg);

        OCRnAH_reg = new BufferedRegister();
        OCRnAL_reg = new BufferedRegister();
        OCRnA_reg = new OCRnxPairedRegister(OCRnAH_reg, OCRnAL_reg);

        OCRnBH_reg = new BufferedRegister();
        OCRnBL_reg = new BufferedRegister();
        OCRnB_reg = new OCRnxPairedRegister(OCRnBH_reg, OCRnBL_reg);

        OCRnCH_reg = new BufferedRegister();
        OCRnCL_reg = new BufferedRegister();
        OCRnC_reg = new OCRnxPairedRegister(OCRnCH_reg, OCRnCL_reg);

        ICRnH_reg = new State.RWIOReg();
        ICRnL_reg = new State.RWIOReg();
        ICRn_reg = new PairedRegister(ICRnL_reg, ICRnH_reg);

        externalClock = m.getClock("external");
        timerClock = mainClock;

        outputComparePinA = (AtmelMicrocontroller.Pin)m.getPin("OC"+n+"A");
        outputComparePinB = (AtmelMicrocontroller.Pin)m.getPin("OC"+n+"B");
        outputComparePinC = (AtmelMicrocontroller.Pin)m.getPin("OC"+n+"C");

        ETIMSK_reg = (ATMegaFamily.MaskRegister)m.getIOReg("ETIMSK");
        ETIFR_reg = (ATMegaFamily.FlagRegister)m.getIOReg("ETIFR");

        installIOReg("TCCR"+n+"A", TCCRnA_reg);
        installIOReg("TCCR"+n+"B", TCCRnB_reg);
        installIOReg("TCCR"+n+"C", TCCRnC_reg);

        installIOReg("TCNT"+n+"H", highTempReg);
        installIOReg("TCNT"+n+"L", TCNTn_reg);

        installIOReg("OCR"+n+"AH", new OCRnxTempHighRegister(OCRnAH_reg));
        installIOReg("OCR"+n+"AL", OCRnA_reg);

        installIOReg("OCR"+n+"BH", new OCRnxTempHighRegister(OCRnBH_reg));
        installIOReg("OCR"+n+"BL", OCRnB_reg);

        installIOReg("OCR"+n+"CH", new OCRnxTempHighRegister(OCRnCH_reg));
        installIOReg("OCR"+n+"CL", OCRnC_reg);

        installIOReg("ICR"+n+"H", highTempReg);
        installIOReg("ICR"+n+"L", ICRn_reg);
    }

    protected void compareMatchA() {
        if (timerPrinter.enabled) {
            boolean enabled = xTIMSK_reg.readBit(OCIEnA);
            timerPrinter.println("Timer" + n + ".compareMatchA (enabled: " + enabled + ')');
        }
        // set the compare flag for this timer
        xTIFR_reg.flagBit(OCFnA);
    }

    protected void compareMatchB() {
        if (timerPrinter.enabled) {
            boolean enabled = xTIMSK_reg.readBit(OCIEnB);
            timerPrinter.println("Timer" + n + ".compareMatchB (enabled: " + enabled + ')');
        }
        // set the compare flag for this timer
        xTIFR_reg.flagBit(OCFnB);
    }

    protected void compareMatchC() {
        if (timerPrinter.enabled) {
            // the OCIEnC flag is on ETIMSK for both Timer1 and Timer3
            boolean enabled = ETIMSK_reg.readBit(OCIEnC);
            timerPrinter.println("Timer" + n + ".compareMatchC (enabled: " + enabled + ')');
        }
        // the OCFnC flag is on ETIFR for both Timer1 and Timer3
        // set the compare flag for this timer
        ETIFR_reg.flagBit(OCFnC);
    }

    /**
     * Flags the overflow interrupt for this timer.
     */
    protected void overflow() {
        if (timerPrinter.enabled) {
            boolean enabled = xTIMSK_reg.readBit(TOIEn);
            timerPrinter.println("Timer" + n + ".overFlow (enabled: " + enabled + ')' + "  ");
        }
        // set the overflow flag for this timer
        xTIFR_reg.flagBit(TOVn);
    }

    /**
     * <code>ControlRegister</code> is an abstract class describing the control registers of a 16-bit
     * timer.
     */
    protected abstract class ControlRegister extends State.RWIOReg {

        protected abstract void decode(byte val);


        public void write(byte val) {
            value = val;
            decode(val);
        }

        public void writeBit(int bit, boolean val) {
            value = Arithmetic.setBit(value, bit, val);
            decode(value);
        }
    }

    /**
     * The <code>PairedRegister</code> class exists to implement the shared temporary register for the
     * high byte of the 16-bit registers corresponding to a 16-bit timer. Accesses to the high byte of
     * a register pair should go through this temporary byte. According to the manual, writes to the
     * high byte are stored in the temporary register. When the low byte is written to, both the low
     * and high byte are updated. On a read, the temporary high byte is updated when a read occurs on
     * the low byte. The PairedRegister should be installed in place of the low register. Reads/writes
     * on this register will act accordingly on the low register, as well as initiate a read/write on
     * the associated high register.
     */
    protected class PairedRegister extends State.RWIOReg {
        State.RWIOReg high;
        State.RWIOReg low;

        PairedRegister(State.RWIOReg high, State.RWIOReg low) {
            this.high = high;
            this.low = low;
        }

        public void write(byte val) {
            low.write(val);
            high.write(highTempReg.read());
        }

        public void writeBit(int bit, boolean val) {
            low.writeBit(bit, val);
            high.write(highTempReg.read());
        }

        public byte read() {
            highTempReg.write(high.read());
            return low.read();
        }

        public boolean readBit(int bit) {
            byte val = read();
            return Arithmetic.getBit(val, bit);
        }
    }

    /**
     * The normal 16-bit read behavior described in the doc for PairedRegister does not apply for the
     * OCRnx registers. Reads on the OCRnxH registers are direct.
     */
    protected class OCRnxPairedRegister extends PairedRegister {
        OCRnxPairedRegister(State.RWIOReg high, State.RWIOReg low) {
            super(high, low);
        }

        public byte read() {
            return low.read();
        }

        public boolean readBit(int bit) {
            return low.readBit(bit);
        }
    }

    /**
     * See doc for OCRnxPairedRegister.
     */
    protected class OCRnxTempHighRegister extends State.RWIOReg {
        State.RWIOReg register;

        OCRnxTempHighRegister(State.RWIOReg register) {
            this.register = register;
        }

        public void write(byte val) {
            highTempReg.write(val);
        }

        public void writeBit(int bit, boolean val) {
            highTempReg.writeBit(bit, val);
        }

        public byte read() {
            return register.read();
        }

        public boolean readBit(int bit) {
            return register.readBit(bit);
        }
    }

    /**
     * Overloads the write behavior of this class of register in order to implement compare match
     * blocking for one timer period.
     */
    protected class TCNTnRegister extends State.RWIOReg {
        /* expr of the blockCompareMatch corresponding to
         * this register in the array of boolean flags.  */
        public void write(byte val) {
            value = val;
            blockCompareMatch = true;
        }

        public void writeBit(int bit, boolean val) {
            value = Arithmetic.setBit(value, bit, val);
            blockCompareMatch = true;
        }
    }

    /**
     * <code>ControlRegisterA</code> describes the TCCRnA control register associated with a 160bit
     * timer. Changing the values of this register generally alter the mode of operation of the
     * timer.
     */
    protected class ControlRegisterA extends ControlRegister {
        public static final int COMnA1 = 7;
        public static final int COMnA0 = 6;
        public static final int COMnB1 = 5;
        public static final int COMnB0 = 4;
        public static final int COMnC1 = 3;
        public static final int COMnC0 = 2;
        public static final int WGMn1 = 1;
        public static final int WGMn0 = 0;


        protected void decode(byte val) {
            // get the mode of operation
            timerModeA = Arithmetic.getBit(val, WGMn1) ? 2 : 0;
            timerModeA |= Arithmetic.getBit(val, WGMn0) ? 1 : 0;

            timerMode = timerModeA | timerModeB;

        }

    }

    /**
     * <code>ControlRegisterA</code> describes the TCCRnB control register associated with a 160bit
     * timer. Changing the values of this register generally alter the mode of operation of the timer.
     * The low three bits also set the prescalar of the timer.
     */
    protected class ControlRegisterB extends ControlRegister {
        public static final int ICNCn = 7;
        public static final int ICESn = 6;
        // bit 5 here has no meaning
        public static final int WGMn3 = 4;
        public static final int WGMn2 = 3;
        public static final int CSn2 = 2;
        public static final int CSn1 = 1;
        public static final int CSn0 = 0;

        public void write(byte val) {

            value = (byte)(val & 0xdf);
            decode(val);
        }

        public void writeBit(int bit, boolean val) {
            if (bit != 5)
                value = Arithmetic.setBit(value, bit, val);
            decode(value);
        }


        protected void decode(byte val) {
            // get the mode of operation
            timerModeB = Arithmetic.getBit(val, WGMn3) ? 8 : 0;
            timerModeB |= Arithmetic.getBit(val, WGMn2) ? 4 : 0;

            timerMode = timerModeA | timerModeB;

            // The low 3 bits of this register determine the prescaler
            int prescaler = val & 0x7;

            if (prescaler < periods.length)
                resetPeriod(periods[prescaler]);

            // not positive what to do with the high two bits
            // TODO: determine behavior of ICNCn and ICESn
        }

        private void resetPeriod(int m) {
            if (m == 0) {
                if (timerEnabled) {
                    if (timerPrinter.enabled) timerPrinter.println("Timer" + n + " disabled");
                    timerClock.removeEvent(ticker);
                }
                return;
            }
            if (timerEnabled) {
                timerClock.removeEvent(ticker);
            }
            if (timerPrinter.enabled) timerPrinter.println("Timer" + n + " enabled: period = " + m + " mode = " + timerMode);
            period = m;
            timerEnabled = true;
            timerClock.insertEvent(ticker, period);

        }

    }

    /**
     * <code>ControlRegisterA</code> describes the TCCRnA control register associated with a 16-bit
     * timer. Writing to the three high bits of this register will cause a forced output compare on at
     * least one of the three output compare units.
     */
    protected class ControlRegisterC extends ControlRegister {
        public static final int FOCnA = 7;
        public static final int FOCnB = 6;
        public static final int FOCnC = 5;
        //bits 4-0 are unspecified in the manual

        protected void decode(byte val) {
        }

        public void write(byte val) {
            if (Arithmetic.getBit(val,FOCnC)) {
                // force output compareC
                forcedOutputCompareC();
            }
            if (Arithmetic.getBit(val,FOCnB)) {
                // force output compareB
                forcedOutputCompareB();
            }
            if (Arithmetic.getBit(val,FOCnA)) {
                // force output compareA
                forcedOutputCompareA();
            }
        }

        public void writeBit(int bit, boolean val) {
            switch (bit) {
                case FOCnC:
                    forcedOutputCompareC();
                    break;
                case FOCnB:
                    forcedOutputCompareB();
                    break;
                case FOCnA:
                    forcedOutputCompareA();
                    break;
            }

        }

        private void forcedCompare(AtmelMicrocontroller.Pin ocPin, int val, int compare, int COMnx1, int COMnx0) {
            /*
            // the non-PWM modes are NORMAL and CTC
            // under NORMAL, there is no pin action for a compare match
            // under CTC, the action is to clear the pin.
            */

            int count = read16(TCNTnH_reg, TCNTnL_reg);
            int compareMode = Arithmetic.getBit(val, COMnx1) ? 2 : 0;
            compareMode |= Arithmetic.getBit(val, COMnx0) ? 1 : 0;
            if (count == compare) {

                switch (compareMode) {
                    case 1:
                        ocPin.write(!ocPin.read()); // clear
                        break;
                    case 2:
                        ocPin.write(false);
                        break;
                    case 3:
                        ocPin.write(true);
                        break;
                }

            }
        }

        private void forcedOutputCompareA() {
            int compare = read16(OCRnAH_reg, OCRnAL_reg);
            forcedCompare(outputComparePinA, TCCRnA_reg.read(), compare, 7, 6);
        }

        private void forcedOutputCompareB() {
            int compare = read16(OCRnBH_reg, OCRnBL_reg);
            forcedCompare(outputComparePinB, TCCRnB_reg.read(), compare, 5, 4);
        }

        private void forcedOutputCompareC() {
            int compare = read16(OCRnCH_reg, OCRnCL_reg);
            forcedCompare(outputComparePinC, TCCRnC_reg.read(), compare, 3, 2);
        }
    }


    /**
     * In PWN modes, writes to the OCRnx registers are buffered. Specifically, the actual write is
     * delayed until a certain event (the counter reaching either TOP or BOTTOM) specified by the
     * particular PWN mode. BufferedRegister implements this by writing to a buffer register on a
     * write and reading from the buffered register in a read. When the buffered register is to be
     * updated, the flush() method should be called.
     */
    protected class BufferedRegister extends State.RWIOReg {
        final State.RWIOReg register;

        protected BufferedRegister() {
            this.register = new State.RWIOReg();
        }

        public void write(byte val) {
            super.write(val);
            if (timerMode == MODE_NORMAL || timerMode == MODE_CTC_OCRnA
                    || timerMode == MODE_CTC_ICRn) {
                flush();
            }
        }

        public void writeBit(int bit, boolean val) {
            super.writeBit(bit, val);
            if (timerMode == MODE_NORMAL || timerMode == MODE_CTC_OCRnA
                    || timerMode == MODE_CTC_ICRn) {
                flush();
            }
        }

        public byte readBuffer() {
            return super.read();
        }

        public byte read() {
            return register.read();
        }

        public boolean readBit(int bit) {
            return register.readBit(bit);
        }

        protected void flush() {
            register.write(value);
        }
    }

    /**
     * The <code>Ticker</class> implements the periodic behavior of the timer. It emulates the
     * operation of the timer at each clock cycle and uses the global timed event queue to achieve the
     * correct periodic behavior.
     */
    protected class Ticker implements Simulator.Event {

        private void flushOCRnx() {
            OCRnAL_reg.flush();
            OCRnAH_reg.flush();
            OCRnBL_reg.flush();
            OCRnBH_reg.flush();
            OCRnCL_reg.flush();
            OCRnCH_reg.flush();
        }

        private final int[] TOP = {0xffff, 0x00ff, 0x01ff, 0x03ff, 0, 0x0ff, 0x01ff, 0x03ff};


        public void fire() {

            int count = read16(TCNTnH_reg, TCNTnL_reg);
            int countSave = count;
            int compareA = read16(OCRnAH_reg, OCRnAL_reg);
            int compareB = read16(OCRnBH_reg, OCRnBL_reg);
            int compareC = read16(OCRnCH_reg, OCRnCL_reg);
            int compareI = read16(ICRnH_reg, ICRnL_reg);

            if (timerPrinter.enabled) {
                timerPrinter.println("Timer" + n + " [TCNT" + n + " = " + count + ", OCR" + n + "A = " + compareA + "],  OCR" + n + "B = " + compareB + "], OCR" + n + "C = " + compareC + ']');
            }

            // What exactly should I do when I phase/frequency current?
            // registers.
            // Make sure these cases are doing what they are supposed to
            // do.
            switch (timerMode) {
                case MODE_NORMAL:
                    count++;
                    countSave = count;
                    if (count == MAX) {
                        overflow();
                        count = 0;
                    }
                    break;
                case MODE_PWM_PHASE_CORRECT_8_BIT:
                case MODE_PWM_PHASE_CORRECT_9_BIT:
                case MODE_PWM_PHASE_CORRECT_10_BIT:
                    if (countUp)
                        count++;
                    else
                        count--;

                    countSave = count;
                    if (count >= TOP[timerMode]) {
                        countUp = false;
                        count = TOP[timerMode];
                        flushOCRnx();
                    }
                    if (count <= 0) {
                        overflow();
                        countUp = true;
                        count = 0;
                    }
                    break;

                case MODE_CTC_OCRnA:
                    count++;

                    countSave = count;
                    if (count == compareA) {
                        count = 0;
                    }
                    if (count == MAX) {
                        overflow();
                    }
                    break;
                case MODE_FASTPWM_8_BIT:
                case MODE_FASTPWM_9_BIT:
                case MODE_FASTPWM_10_BIT:
                    count++;

                    countSave = count;
                    if (count == TOP[timerMode]) {
                        count = 0;
                        overflow();
                        flushOCRnx();
                    }

                    break;
                case MODE_PWM_PNF_ICRn:
                    if (countUp)
                        count++;
                    else
                        count--;

                    countSave = count;
                    if (count >= compareI) {
                        countUp = false;
                        count = compareI;
                    }
                    if (count <= 0) {
                        overflow();
                        flushOCRnx();
                        countUp = true;
                        count = 0;
                    }
                    break;
                case MODE_PWM_PNF_OCRnA:
                    if (countUp)
                        count++;
                    else
                        count--;

                    countSave = count;
                    if (count >= compareA) {
                        countUp = false;
                        count = compareA;
                    }
                    if (count <= 0) {
                        overflow();
                        flushOCRnx();
                        countUp = true;
                        count = 0;
                    }
                    break;
                case MODE_PWN_PHASE_CORRECT_ICRn:
                    if (countUp)
                        count++;
                    else
                        count--;

                    countSave = count;
                    if (count >= compareI) {
                        flushOCRnx();
                        countUp = false;
                        count = compareI;
                    }
                    if (count <= 0) {
                        overflow();
                        countUp = true;
                        count = 0;
                    }
                    break;
                case MODE_PWN_PHASE_CORRECT_OCRnA:
                    if (countUp)
                        count++;
                    else
                        count--;

                    countSave = count;
                    if (count >= compareA) {
                        flushOCRnx();
                        countUp = false;
                        count = compareA;
                    }
                    if (count <= 0) {
                        overflow();
                        countUp = true;
                        count = 0;
                    }
                    break;
                case MODE_CTC_ICRn:
                    count++;

                    countSave = count;
                    if (count == compareI) {
                        count = 0;
                    }
                    if (count == MAX) {
                        overflow();
                    }
                    break;
                case MODE_FASTPWM_ICRn:
                    count++;

                    countSave = count;
                    if (count == compareI) {
                        count = 0;
                        overflow();
                        flushOCRnx();
                    }
                    break;
                case MODE_FASTPWM_OCRnA:
                    count++;

                    countSave = count;
                    if (count == compareA) {
                        count = 0;
                        overflow();
                        flushOCRnx();
                    }
                    break;
            }

            // the compare match should be performed in any case.
            if (!blockCompareMatch) {
                if (countSave == compareA) {
                    compareMatchA();
                }
                if (countSave == compareB) {
                    compareMatchB();
                }
                if (countSave == compareC) {
                    compareMatchC();
                }
            }
            write16(count, TCNTnH_reg, TCNTnL_reg);
            // make sure timings on this are correct
            blockCompareMatch = false;


            if (period != 0) timerClock.insertEvent(this, period);
        }
    }

}
