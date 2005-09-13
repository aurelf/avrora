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
import avrora.sim.clock.Clock;
import cck.util.Arithmetic;

/**
 * Base class of 8-bit timers. Timer0 and Timer2 are subclasses of this.
 *
 * @author Daniel Lee
 */
public abstract class Timer8Bit extends AtmelInternalDevice {
    public static final int MODE_NORMAL = 0;
    public static final int MODE_PWM = 1;
    public static final int MODE_CTC = 2;
    public static final int MODE_FASTPWM = 3;
    public static final int MAX = 0xff;
    public static final int BOTTOM = 0x00;

    final ControlRegister TCCRn_reg;
    final TCNTnRegister TCNTn_reg;
    final BufferedRegister OCRn_reg;

    final int n; // number of timer. 0 for Timer0, 2 for Timer2

    final Ticker ticker;

    protected final Clock externalClock;
    protected Clock timerClock;

    boolean timerEnabled;
    boolean countUp;
    int timerMode;
    long period;

    final AtmelMicrocontroller.Pin outputComparePin;

    /* pg. 93 of manual. Block compareMatch for one period after
     * TCNTn is written to. */
    boolean blockCompareMatch;

    final int OCIEn;
    final int TOIEn;
    final int OCFn;
    final int TOVn;

    protected ATMegaFamily.FlagRegister TIFR_reg;
    protected ATMegaFamily.MaskRegister TIMSK_reg;

    final int[] periods;

    protected Timer8Bit(AtmelMicrocontroller m, int n, int OCIEn, int TOIEn, int OCFn, int TOVn, int[] periods) {
        super("timer"+n, m);
        ticker = new Ticker();
        TCCRn_reg = new ControlRegister();
        TCNTn_reg = new TCNTnRegister();
        OCRn_reg = new BufferedRegister();

        TIFR_reg = (ATMegaFamily.FlagRegister)m.getIOReg("TIFR");
        TIMSK_reg = (ATMegaFamily.MaskRegister)m.getIOReg("TIMSK");

        externalClock = m.getClock("external");
        timerClock = mainClock;

        outputComparePin = (AtmelMicrocontroller.Pin)microcontroller.getPin("OC"+n);

        this.OCIEn = OCIEn;
        this.TOIEn = TOIEn;
        this.OCFn = OCFn;
        this.TOVn = TOVn;
        this.n = n;
        this.periods = periods;

        installIOReg("TCCR"+n, TCCRn_reg);
        installIOReg("TCNT"+n, TCNTn_reg);
        installIOReg("OCR"+n, OCRn_reg);
    }

    protected void compareMatch() {
        if (devicePrinter.enabled) {
            boolean enabled = TIMSK_reg.readBit(OCIEn);
            devicePrinter.println("Timer" + n + ".compareMatch (enabled: " + enabled + ')');
        }
        // set the compare flag for this timer
        TIFR_reg.flagBit(OCFn);
        // if the mode is correct, modify pin OCn. but if the flag is
        // already connected to the pin, does this happen automatically
        // with the last previous call?
        //compareMatchPin();
    }

    protected void overflow() {
        if (devicePrinter.enabled) {
            boolean enabled = TIMSK_reg.readBit(TOIEn);
            devicePrinter.println("Timer" + n + ".overFlow (enabled: " + enabled + ')');
        }
        // set the overflow flag for this timer
        TIFR_reg.flagBit(TOVn);
    }

    /**
     * Overloads the write behavior of this class of register in order to implement compare match
     * blocking for one timer period.
     */
    protected class TCNTnRegister extends RWRegister {

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
     * <code>BufferedRegister</code> implements a register with a write buffer. In PWN modes, writes
     * to this register are not performed until flush() is called. In non-PWM modes, the writes are
     * immediate.
     */
    protected class BufferedRegister extends RWRegister {
        final RWRegister register;

        protected BufferedRegister() {
            this.register = new RWRegister();
        }

        public void write(byte val) {
            super.write(val);
            if (timerMode == MODE_NORMAL || timerMode == MODE_CTC) {
                flush();
            }
        }

        public void writeBit(int bit, boolean val) {
            super.writeBit(bit, val);
            if (timerMode == MODE_NORMAL || timerMode == MODE_CTC) {
                flush();
            }
        }

        // TODO: this method may be completely unnecessary
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

    protected class ControlRegister extends RWRegister {
        public static final int FOCn = 7;
        public static final int WGMn0 = 6;
        public static final int COMn1 = 5;
        public static final int COMn0 = 4;
        public static final int WGMn1 = 3;
        public static final int CSn2 = 2;
        public static final int CSn1 = 1;
        public static final int CSn0 = 0;

        public void write(byte val) {
            // hardware manual states that high order bit is always read as zero
            value = (byte)(val & 0x7f);

            if ((val & 0x80) != 0) {
                forcedOutputCompare(value);
            }

            // decode modes and update internal state
            decode(val);
        }

        public void writeBit(int bit, boolean val) {
            if (bit == 7 && val) {

                forcedOutputCompare(value);
            } else {
                value = Arithmetic.setBit(value, bit, val);
                decode(value);
            }
        }

        private void forcedOutputCompare(byte val) {

            int count = TCNTn_reg.read() & 0xff;
            int compare = OCRn_reg.read() & 0xff;
            int compareMode = Arithmetic.getBit(val, COMn1) ? 2 : 0;
            compareMode |= Arithmetic.getBit(val, COMn0) ? 1 : 0;


            // the non-PWM modes are NORMAL and CTC
            // under NORMAL, there is no pin action for a compare match
            // under CTC, the action is to clear the pin.

            if (count == compare) {
                switch (compareMode) {
                    case 1:
                        outputComparePin.write(!outputComparePin.read()); // toggle
                        break;
                    case 2:
                        outputComparePin.write(false); // clear
                        break;
                    case 3:
                        outputComparePin.write(true); // set to true
                        break;
                }

            }
        }

        private void decode(byte val) {
            // get the mode of operation
            timerMode = Arithmetic.getBit(val, WGMn1) ? 2 : 0;
            timerMode |= Arithmetic.getBit(val, WGMn0) ? 1 : 0;

            int prescaler = val & 0x7;

            if (prescaler < periods.length)
                resetPeriod(periods[prescaler]);
        }

        private void resetPeriod(int m) {
            if (m == 0) {
                if (timerEnabled) {
                    if (devicePrinter.enabled) devicePrinter.println("Timer" + n + " disabled");
                    timerClock.removeEvent(ticker);
                }
                return;
            }
            if (timerEnabled) {
                timerClock.removeEvent(ticker);
            }
            if (devicePrinter.enabled) devicePrinter.println("Timer" + n + " enabled: period = " + m + " mode = " + timerMode);
            period = m;
            timerEnabled = true;
            timerClock.insertEvent(ticker, period);
        }
    }

    /**
     * The <code>Ticker</class> implements the periodic behavior of the timer. It emulates the
     * operation of the timer at each clock cycle and uses the global timed event queue to achieve the
     * correct periodic behavior.
     */
    protected class Ticker implements Simulator.Event {

        public void fire() {
            // perform one clock tick worth of work on the timer
            int count = TCNTn_reg.read() & 0xff;
            int compare = OCRn_reg.read() & 0xff;
            int countSave = count;
            if (devicePrinter.enabled)
                devicePrinter.println("Timer" + n + " [TCNT" + n + " = " + count + ", OCR" + n + "(actual) = " + compare + ", OCR" + n + "(buffer) = " + (0xff & OCRn_reg.readBuffer()) + ']');

            // TODO: this code has one off counting bugs!!!
            switch (timerMode) {
                case MODE_NORMAL: // NORMAL MODE
                    count++;
                    countSave = count;
                    if (count == MAX) {
                        overflow();
                        count = 0;
                    }

                    break;
                case MODE_PWM: // PULSE WIDTH MODULATION MODE
                    if (countUp)
                        count++;
                    else
                        count--;

                    countSave = count;
                    if (count >= MAX) {
                        countUp = false;
                        count = MAX;
                        OCRn_reg.flush(); // pg. 102. update OCRn at TOP
                    }
                    if (count <= 0) {
                        overflow();
                        countUp = true;
                        count = 0;
                    }
                    break;
                case MODE_CTC: // CLEAR TIMER ON COMPARE MODE
                    countSave = count;
                    count++;
                    if (countSave == compare) {
                        count = 0;
                    }
                    break;
                case MODE_FASTPWM: // FAST PULSE WIDTH MODULATION MODE
                    count++;
                    countSave = count;
                    if (count == MAX) {
                        count = 0;
                        overflow();
                        OCRn_reg.flush(); // pg. 102. update OCRn at TOP
                    }
                    break;
            }

            if (countSave == compare && !blockCompareMatch) {
                compareMatch();
            }
            TCNTn_reg.write((byte)count);
            // I probably want to verify the timing on this.
            blockCompareMatch = false;

            if (period != 0) timerClock.insertEvent(this, period);
        }
    }
}
