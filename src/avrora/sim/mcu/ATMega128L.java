/**
 * Copyright (c) 2004, Regents of the University of California
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

import avrora.util.Arithmetic;
import avrora.util.Verbose;
import avrora.core.InstrPrototype;
/***/
import avrora.core.Instr;
/***/
import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.GenInterpreter;
import avrora.sim.BaseInterpreter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;


/**
 * The <code>ATMega128L</code> class represents the <code>Microcontroller</code>
 * instance that has all the hardware parameters of the ATMega128L microcontroller
 * as produced by Atmel Corporatation.
 *
 * @author Ben L. Titzer
 */
public class ATMega128L implements Microcontroller, MicrocontrollerFactory {

    /**
     * The <code>HZ</code> field stores a public static final integer that
     * represents the clockspeed of the AtMega128L microcontroller (7.327mhz).
     */
    public static final int HZ = 7327800;

    public static final int SRAM_SIZE = 4096;
    public static final int IOREG_SIZE = 256 - 32;
    public static final int IOREG_SIZE_103 = 64;
    public static final int FLASH_SIZE = 128 * 1024;
    public static final int EEPROM_SIZE = 4096;

    public static final int NUM_PINS = 65;

    static Verbose.Printer pinPrinter = Verbose.getVerbosePrinter("sim.pin");

    protected static final HashMap pinNumbers;
    private final boolean compatibilityMode;

    static {
        pinNumbers = new HashMap();

        newPin(1, "PEN");
        newPin(2, "PE0", "RXD0", "PDI");
        newPin(3, "PE1", "TXD0", "PDO");
        newPin(4, "PE2", "XCK0", "AIN0");
        newPin(5, "PE3", "OC3A", "AIN1");
        newPin(6, "PE4", "OC3B", "INT4");
        newPin(7, "PE5", "OC3C", "INT5");
        newPin(8, "PE6", "T3", "INT6");
        newPin(9, "PE7", "IC3", "INT7");
        newPin(10, "PB0", "SS");
        newPin(11, "PB1", "SCK");
        newPin(12, "PB2", "MOSI");
        newPin(13, "PB3", "MISO");
        newPin(14, "PB4", "OC0");
        newPin(15, "PB5", "OC1A");
        newPin(16, "PB6", "OC1B");
        newPin(17, "PB7", "OC2", "OC1C");
        newPin(18, "PG3", "TOSC2");
        newPin(19, "PG4", "TOSC1");
        newPin(20, "RESET");
        newPin(21, "VCC");
        newPin(22, "GND");
        newPin(23, "XTAL2");
        newPin(24, "XTAL1");
        newPin(25, "PD0", "SCL", "INT0");
        newPin(26, "PD1", "SDA", "INT1");
        newPin(27, "PD2", "RXD1", "INT2");
        newPin(28, "PD3", "TXD1", "INT3");
        newPin(29, "PD4", "IC1");
        newPin(30, "PD5", "XCK1");
        newPin(31, "PD6", "T1");
        newPin(32, "PD7", "T2");
        newPin(33, "PG0", "WR");
        newPin(34, "PG1", "RD");
        newPin(35, "PC0", "A8");
        newPin(36, "PC1", "A9");
        newPin(37, "PC2", "A10");
        newPin(38, "PC3", "A11");
        newPin(39, "PC4", "A12");
        newPin(40, "PC5", "A13");
        newPin(41, "PC6", "A14");
        newPin(42, "PC7", "A15");
        newPin(43, "PG2", "ALE");
        newPin(44, "PA7", "AD7");
        newPin(45, "PA6", "AD5");
        newPin(46, "PA5", "AD5");
        newPin(47, "PA4", "AD4");
        newPin(48, "PA3", "AD3");
        newPin(49, "PA2", "AD2");
        newPin(50, "PA1", "AD1");
        newPin(51, "PA0", "AD0");
        newPin(52, "VCC.b");
        newPin(53, "GND.b");
        newPin(54, "PF7", "ADC7", "TDI");
        newPin(55, "PF6", "ADC6", "TDO");
        newPin(56, "PF5", "ADC5", "TMS");
        newPin(57, "PF4", "ADC4", "TCK");
        newPin(58, "PF3", "ADC3");
        newPin(59, "PF2", "ADC2");
        newPin(60, "PF1", "ADC1");
        newPin(61, "PF0", "ADC0");
        newPin(62, "AREF");
        newPin(63, "GND.c");
        newPin(64, "AVCC");
    }

    /**
     * The <code>Pin</code> class implements a model of a pin on the ATmega128L for
     * the general purpose IO ports.
     */
    protected class Pin implements Microcontroller.Pin {
        protected final int number;

        boolean level;
        boolean outputDir;
        boolean pullup;

        Microcontroller.Pin.Input input;
        Microcontroller.Pin.Output output;

        protected Pin(int num) {
            number = num;
        }

        public void connect(Output o) {
            output = o;
        }

        public void connect(Input i) {
            input = i;
        }

        protected void setOutputDir(boolean out) {
            outputDir = out;
        }

        protected void setPullup(boolean pull) {
            pullup = pull;
        }

        protected boolean read() {
            boolean result;
            if (pinPrinter.enabled) printRead();
            if (!outputDir) {
                if (input != null)
                    result = input.read();
                else
                    result = pullup;

            } else {
                result = level;
            }
            if (pinPrinter.enabled) pinPrinter.println(" -> " + result);
            return result;
        }

        private void printRead() {
            pinPrinter.print("Pin[" + number + "].read() ");
            printDirection();
        }

        private void printDirection() {
            if (!outputDir) {
                if (input != null)
                    pinPrinter.print("[input] ");
                else
                    pinPrinter.print("[pullup:" + pullup + "] ");

            } else {
                pinPrinter.print("[output] ");
            }
        }

        protected void write(boolean value) {
            level = value;
            if (pinPrinter.enabled) printWrite(value);
            if (outputDir && output != null) output.write(value);
        }

        private void printWrite(boolean value) {
            pinPrinter.print("Pin[" + number + "].write(" + value + ") ");
            printDirection();
            pinPrinter.nextln();
        }
    }

    static void newPin(int ind, String name) {
        pinNumbers.put(name, new Integer(ind));
    }

    static void newPin(int ind, String name, String n2) {
        Integer integer = new Integer(ind);
        pinNumbers.put(name, integer);
        pinNumbers.put(n2, integer);
    }

    static void newPin(int ind, String name, String n2, String n3) {
        Integer integer = new Integer(ind);
        pinNumbers.put(name, integer);
        pinNumbers.put(n2, integer);
        pinNumbers.put(n3, integer);
    }

    protected final Simulator simulator;
    protected final Pin[] pins;

    /**
     * The constructor for the default instance.
     */
    public ATMega128L(boolean compatibility) {
        simulator = null;
        pins = new Pin[NUM_PINS];
        compatibilityMode = compatibility;
        //compatibilityMode = true;
    }

    protected ATMega128L(Program p, boolean compatibility) {
        compatibilityMode = compatibility;
        //compatibilityMode = true;
        pins = new Pin[NUM_PINS];
        installPins();
        simulator = new SimImpl(p);
    }

    protected void installPins() {
        for (int cntr = 0; cntr < NUM_PINS; cntr++)
            pins[cntr] = new Pin(cntr);

        // TODO: install reserved pins like VCC
    }

    /**
     * The <code>getRamSize()</code> method returns the number of bytes of
     * SRAM present on this hardware device. On the Atmega128L
     * this number is 4096 (4KB).
     *
     * @return the number of bytes of SRAM on this hardware device
     */
    public int getRamSize() {
        return SRAM_SIZE;
    }

    /**
     * The <code>getIORegSize()</code> method returns the number of IO registers
     * that are present on this hardware device. On the Atmega128L
     * this number is 224 (256 - 32).
     *
     * @return the number of IO registers supported on this hardware device
     */
    public int getIORegSize() {
        if (compatibilityMode)
            return IOREG_SIZE_103;
        else
            return IOREG_SIZE;
    }

    /**
     * The <code>getFlashSize()</code> method returns the size in bytes of
     * the flash memory on this hardware device. The flash memory stores the
     * initialized data and the machine code instructions of the program. On
     * the Atmega128L, this number is 131,072 (128K).
     *
     * @return the size of the flash memory in bytes
     */
    public int getFlashSize() {
        return FLASH_SIZE;
    }

    /**
     * The <code>getEEPromSize()</code> method returns the size in bytes of
     * the EEPROM on this hardware device. On the ATmega128L, this number is
     * 4096.
     *
     * @return the size of the EEPROM in bytes
     */
    public int getEEPromSize() {
        return EEPROM_SIZE;
    }

    /**
     * The <code>isSupported()</code> method allows a client to query whether
     * a particular instruction is implemented on this hardware device. Older
     * implementations of the AVR instruction set preceded the introduction
     * of certain instructions, and therefore did not support the new
     * instructions.
     *
     * @param i the instruction prototype of the instruction
     * @return true if the specified instruction is supported on this device;
     *         false otherwise
     */
    public boolean isSupported(InstrPrototype i) {
        return true;
    }

    /**
     * The <code>getSimulator()</code> method gets a simulator instance that is
     * capable of emulating this hardware device.
     *
     * @return a <code>Simulator</code> instance corresponding to this
     *         device
     */
    public Simulator getSimulator() {
        return simulator;
    }

    /**
     * The <code>getHZ()</code> method returns the number of cycles per second
     * at which this hardware device is designed to run. The
     * Atmega128L runs at 7.3278MHz, so this method will return 7,327,800.
     *
     * @return the number of cycles per second on this device
     */
    public int getHz() {
        return HZ;
    }

    /**
     * The <code>millisToCycles()</code> method converts the specified number
     * of milliseconds to a cycle count. The conversion factor used is the
     * number of cycles per second of this device. This method serves as a
     * utility so that clients need not do repeated work in converting
     * milliseconds to cycles and back.
     *
     * @param ms a time quantity in milliseconds as a double
     * @return the same time quantity in clock cycles, rounded up to the nearest
     *         integer
     */
    public long millisToCycles(double ms) {
        return (long) (ms * HZ / 1000);
    }

    /**
     * The <code>cyclesToMillis()</code> method converts the specified number
     * of cycles to a time quantity in milliseconds. The conversion factor used
     * is the number of cycles per second of this device. This method serves
     * as a utility so that clients need not do repeated work in converting
     * milliseconds to cycles and back.
     *
     * @param cycles the number of cycles
     * @return the same time quantity in milliseconds
     */
    public double cyclesToMillis(long cycles) {
        return 1000 * ((double) cycles) / HZ;
    }

    /**
     * The <code>getPinNumber()</code> method looks up the named pin and returns
     * its number. Names of pins should be UPPERCASE. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     *
     * @param name the name of the pin; for example "PA0" or "OC1A"
     * @return the number of the pin if it exists; -1 otherwise
     */
    public int getPinNumber(String name) {
        Integer i = (Integer) pinNumbers.get(name);
        return i == null ? -1 : i.intValue();
    }

    /**
     * The <code>newMicrocontroller()</code> method is used to instantiate a
     * microcontroller instance for the particular program. It will construct
     * an instance of the <code>Simulator</code> class that has all the
     * properties of this hardware device and has been initialized with the
     * specified program.
     *
     * @param p the program to load onto the microcontroller
     * @return a <code>Microcontroller</code> instance that represents the
     *         specific hardware device with the program loaded onto it
     */
    public Microcontroller newMicrocontroller(Program p) {
        return new ATMega128L(p, compatibilityMode);
    }

    /**
     * The <code>getPin()</code> method looks up the specified pin by its number
     * and returns a reference to that pin. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     *
     * @param num the pin number to look up
     * @return a reference to the <code>Pin</code> object corresponding to
     *         the named pin if it exists; null otherwise
     */
    public Microcontroller.Pin getPin(int num) {
        if (num < 0 || num > pins.length) return null;
        return pins[num];
    }

    /**
     * The <code>getPin()</code> method looks up the named pin and returns a
     * reference to that pin. Names of pins should be UPPERCASE. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     *
     * @param name the name of the pin; for example "PA0" or "OC1A"
     * @return a reference to the <code>Pin</code> object corresponding to
     *         the named pin if it exists; null otherwise
     */
    public Microcontroller.Pin getPin(String name) {
        return getPin(getPinNumber(name));
    }

    public class SimImpl extends Simulator {

        public SimImpl(Program p) {
            super(ATMega128L.this, p);
            populateState(interpreter);
        }

        public static final int RES_VECT = 1;
        public static final int EXT_VECT = 2;

        protected FlagRegister EIFR_reg;
        protected MaskRegister EIMSK_reg;

        protected FlagRegister TIFR_reg;
        protected MaskRegister TIMSK_reg;

        /** The ETIFR register. Address 0x7c.*/
        protected UnorderedFlagRegister ETIFR_reg;

        /** The ETIMSK register. Address 0x7d.*/
        protected UnorderedMaskRegister ETIMSK_reg;

        protected class DirectionRegister extends State.RWIOReg {

            protected Pin[] pins;

            protected DirectionRegister(Pin[] p) {
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

        protected class PortRegister extends State.RWIOReg {
            protected Pin[] pins;

            protected PortRegister(Pin[] p) {
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

        protected class PinRegister implements State.IOReg {
            protected Pin[] pins;

            protected PinRegister(Pin[] p) {
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
                return (byte) value;
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

        int[] periods0 = {0, 1, 8, 32, 64, 128, 256, 1024};

        /** <code>Timer0</code> is the default 8-bit timer on the
         * ATMega128L. */
        protected class Timer0 extends Timer8Bit {

            protected Timer0(BaseInterpreter ns) {
                super(ns, 0, TCCR0, TCNT0, OCR0, 1, 0, 1, 0, periods0);
            }
        }

        int[] periods2 = {0, 1, 8, 64, 256, 1024};

        /** <code>Timer2</code> is an additional 8-bit timer on the
         * ATMega128L. It is not available in ATMega103 compatibility
         * mode. */
        protected class Timer2 extends Timer8Bit {
            protected Timer2(BaseInterpreter ns) {
                super(ns, 2, TCCR2, TCNT2, OCR2, 7, 6, 7, 6, periods2);
            }
        }


        /** <code>Timer1</code> is a 16-bit timer available on the
         * ATMega128L.*/
        protected class Timer1 extends Timer16Bit {

            protected void initValues() {
                n = 1;

                TCNTnL = TCNT1L;
                TCNTnH = TCNT1H;
                TCCRnA = TCCR1A;
                TCCRnB = TCCR1B;
                TCCRnC = TCCR1C;

                OCRnAH = OCR1AH;
                OCRnAL = OCR1AL;
                OCRnBH = OCR1BH;
                OCRnBL = OCR1BL;
                OCRnCH = OCR1CH;
                OCRnCL = OCR1CL;
                ICRnH = ICR1H;
                ICRnL = ICR1L;

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
                int[] periods1 = {0, 1, 8, 64, 256, 1024};
                periods = periods1;
            }

            protected Timer1(BaseInterpreter ns) {
                super(ns);
                xTIFR_reg = TIFR_reg;
                xTIMSK_reg = TIMSK_reg;
            }

        }

        /** <code>Timer3</code> is an additional 16-bit timer available on the
         * ATMega128L, but not in ATMega103 compatability mode. */
        protected class Timer3 extends Timer16Bit {

            protected void initValues() {
                n = 3;

                TCNTnL = TCNT3L;
                TCNTnH = TCNT3H;
                TCCRnA = TCCR3A;
                TCCRnB = TCCR3B;
                TCCRnC = TCCR3C;

                OCRnAH = OCR3AH;
                OCRnAL = OCR3AL;
                OCRnBH = OCR3BH;
                OCRnBL = OCR3BL;
                OCRnCH = OCR3CH;
                OCRnCL = OCR3CL;
                ICRnH = ICR3H;
                ICRnL = ICR3L;

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
                int[] periods3 = {0, 1, 8, 64, 256, 1024};
                periods = periods3;
            }

            protected Timer3(BaseInterpreter ns) {
                super(ns);
                xTIFR_reg = ETIFR_reg;
                xTIMSK_reg = ETIMSK_reg;
            }

        }


        /**
         * The <code>Timer16Bit</code> class emulates the
         * functionality and behavior of a 16-bit timer on the
         * Atmega128L. It has several control and data registers and
         * can fire up to six different interrupts depending on the
         * mode that it has been put into. It has three output compare
         * units and one input capture unit. UNIMPLEMENTED: input
         * capture unit.
         * @author Daniel Lee
         */
        protected abstract class Timer16Bit {

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

            final Ticker ticker;

            boolean timerEnabled;
            boolean countUp;
            int timerModeA;
            int timerModeB;
            int timerMode;
            long period;

            //boolean blockCompareMatch;
            boolean blockCompareMatch;
            //final int[] periods;

            Verbose.Printer timerPrinter;

            // information about registers and flags that specifies
            // which specific registers this 16-bit timer interacts with
            //final TimerValues timerValues;

            /* should define these fields in an actual instance of this class */

            int n; // number of timer. 1 for Timer1, 3 for Timer3

            int TCNTnH;
            int TCNTnL;
            int TCCRnA;
            int TCCRnB;
            int TCCRnC;

            int OCRnAH;
            int OCRnAL;
            int OCRnBH;
            int OCRnBL;
            int OCRnCH;
            int OCRnCL;
            int ICRnH;
            int ICRnL;

            // these are the offsets on registers corresponding to
            // these flags
            int OCIEnA;
            int OCIEnB;
            int OCIEnC;
            int TOIEn;
            int TOVn;
            int OCFnA;
            int OCFnB;
            int OCFnC;
            int ICFn;

            protected FlagRegister xTIFR_reg;
            protected MaskRegister xTIMSK_reg;

            protected int[] periods;

            /* end of fields that should be defined. */

            // This method should be overloaded to initialize the above values.
            abstract protected void initValues();

            private Timer16Bit(BaseInterpreter ns) {
                //timerValues = tv;
                initValues();
                //Integer i = null;
                //if(n == 0) i.toString();

                timerPrinter = Verbose.getVerbosePrinter("sim.timer" + n);
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

                installIOReg(ns, TCCRnA, TCCRnA_reg);
                installIOReg(ns, TCCRnB, TCCRnB_reg);
                installIOReg(ns, TCCRnC, TCCRnC_reg);

                //installIOReg(ns, TCNTnH, TCNTnH_reg);
                //installIOReg(ns, TCNTnL, TCNTnL_reg);
                installIOReg(ns, TCNTnH, highTempReg);
                installIOReg(ns, TCNTnL, TCNTn_reg);

                //installIOReg(ns, OCRnAH, OCRnAH_reg);
                //installIOReg(ns, OCRnAL, OCRnAL_reg);
                installIOReg(ns, OCRnAH, new OCRnxTempHighRegister(OCRnAH_reg));
                installIOReg(ns, OCRnAL, OCRnA_reg);

                //installIOReg(ns, OCRnBH, OCRnBH_reg);
                //installIOReg(ns, OCRnBL, OCRnBL_reg);
                installIOReg(ns, OCRnBH, new OCRnxTempHighRegister(OCRnBH_reg));
                installIOReg(ns, OCRnBL, OCRnB_reg);


                //installIOReg(ns, OCRnCH, OCRnCH_reg);
                //installIOReg(ns, OCRnCL, OCRnCL_reg);
                installIOReg(ns, OCRnCH, new OCRnxTempHighRegister(OCRnCH_reg));
                installIOReg(ns, OCRnCL, OCRnC_reg);

                //installIOReg(ns, ICRnH, ICRnH_reg);
                //installIOReg(ns, ICRnL, ICRnL_reg);
                installIOReg(ns, ICRnH, highTempReg);
                installIOReg(ns, ICRnL, ICRn_reg);
            }

            /* Rembmer to clarify the behavior of these methods.*/

            protected void compareMatchA() {
                if (timerPrinter.enabled) {
                    boolean enabled = xTIMSK_reg.readBit(OCIEnA);
                    timerPrinter.println("Timer" + n + ".compareMatchA (enabled: " + enabled + ")");
                }
                // set the compare flag for this timer
                xTIFR_reg.flagBit(OCFnA);
            }

            protected void compareMatchB() {
                if (timerPrinter.enabled) {
                    boolean enabled = xTIMSK_reg.readBit(OCIEnB);
                    timerPrinter.println("Timer" + n + ".compareMatchB (enabled: " + enabled + ")");
                }
                // set the compare flag for this timer
                xTIFR_reg.flagBit(OCFnB);
            }

            protected void compareMatchC() {
                if (timerPrinter.enabled) {
                    // the OCIEnC flag is on ETIMSK for both Timer1 and Timer3
                    boolean enabled = ETIMSK_reg.readBit(OCIEnC);
                    timerPrinter.println("Timer" + n + ".compareMatchC (enabled: " + enabled + ")");
                }
                // the OCFnC flag is on ETIFR for both Timer1 and Timer3
                // set the compare flag for this timer
                ETIFR_reg.flagBit(OCFnC);
            }

            /** Flags the overflow interrupt for this timer. */
            protected void overflow() {
                if (timerPrinter.enabled) {
                    boolean enabled = xTIMSK_reg.readBit(TOIEn);
                    timerPrinter.println("Timer" + n + ".overFlow (enabled: " + enabled + ")" + "  ");
                }
// set the overflow flag for this timer
                xTIFR_reg.flagBit(TOVn);
            }

            /** <code>ControlRegister</code> is an abstract class
             * describing the control registers of a 16-bit timer. */
            protected abstract class ControlRegister extends State.RWIOReg {


                private void decode(byte val) {
                }

                public void write(byte val) {
                    value = val;
                    decode(val);
                }

                public void writeBit(int bit, boolean val) {
                    value = Arithmetic.setBit(value, bit, val);
                    decode(value);
                }
            }

            /** The <code>PairedRegister</code> class exists to
             * implement the shared temporary register for the high
             * byte of the 16-bit registers corresponding to a 16-bit
             * timer. Accesses to the high byte of a register pair
             * should go through this temporary byte. According to the
             * manual, writes to the high byte are stored in the
             * temporary register. When the low byte is written to,
             * both the low and high byte are updated. On a read, the
             * temporary high byte is updated when a read occurs on
             * the low byte. The PairedRegister should be installed in
             * place of the low register. Reads/writes on this
             * register will act accordingly on the low register, as
             * well as initiate a read/write on the associated high
             * register. */
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

            /** The normal 16-bit read behavior described in the doc
             * for PairedRegister does not apply for the OCRnx
             * registers. Reads on the OCRnxH registers are direct. */
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

            /** See doc for OCRnxPairedRegister. */
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

            /** Overloads the write behavior of this class of register
             * in order to implement compare match blocking for one
             * timer period. */
            protected class TCNTnRegister extends State.RWIOReg {
                /* index of the blockCompareMatch corresponding to
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

            /** <code>ControlRegisterA</code> describes the TCCRnA
             * control register associated with a 160bit
             * timer. Changing the values of this register generally
             * alter the mode of operation of the timer.*/
            protected class ControlRegisterA extends ControlRegister {
                public static final int COMnA1 = 7;
                public static final int COMnA0 = 6;
                public static final int COMnB1 = 5;
                public static final int COMnB0 = 4;
                public static final int COMnC1 = 3;
                public static final int COMnC0 = 2;
                public static final int WGMn1 = 1;
                public static final int WGMn0 = 0;


                private void decode(byte val) {
// get the mode of operation
                    timerModeA = Arithmetic.getBit(val, WGMn1) ? 2 : 0;
                    timerModeA |= Arithmetic.getBit(val, WGMn0) ? 1 : 0;

                    timerMode = timerModeA | timerModeB;

                }

            }

            /** <code>ControlRegisterA</code> describes the TCCRnB
             * control register associated with a 160bit
             * timer. Changing the values of this register generally
             * alter the mode of operation of the timer. The low three
             * bits also set the prescalar of the timer. */
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

                    value = (byte) (val & 0xdf);
                    decode(val);
                }

                public void writeBit(int bit, boolean val) {
                    if (bit != 5)
                        value = Arithmetic.setBit(value, bit, val);
                    decode(value);
                }


                private void decode(byte val) {
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
                            removeEvent(ticker);
                        }
                        return;
                    }
                    if (timerEnabled) {
                        removeEvent(ticker);
                    }
                    if (timerPrinter.enabled) timerPrinter.println("Timer" + n + " enabled: period = " + m + " mode = " + timerMode);
                    period = m;
                    timerEnabled = true;
                    insertEvent(ticker, period);

                }

            }

            /** <code>ControlRegisterA</code> describes the TCCRnA
             * control register associated with a 16-bit
             * timer. Writing to the three high bits of this register
             * will cause a forced output compare on at least one of
             * the three output compare units.*/
            protected class ControlRegisterC extends ControlRegister {
                public static final int FOCnA = 7;
                public static final int FOCnB = 6;
                public static final int FOCnC = 5;
                //bits 4-0 are unspecified in the manual

                public void write(byte val) {
                    if ((val & 0x20) != 0) {
                        // force output compareC
                        forcedOutputCompareC();
                    }
                    if ((val & 0x40) != 0) {
                        // force output compareB
                        forcedOutputCompareB();
                    }
                    if ((val & 0x80) != 0) {
                        // force output compareA
                        forcedOutputCompareA();
                    }
                }

                public void writeBit(int bit, boolean val) {
                    switch (bit) {
                        case 5:
                            forcedOutputCompareC();
                            break;
                        case 6:
                            forcedOutputCompareB();
                            break;
                        case 7:
                            forcedOutputCompareA();
                            break;
                    }

                }

                private void forcedCompare(String s, int val, int compare, int COMnx1, int COMnx0) {
                    /*
                    // the non-PWM modes are NORMAL and CTC
                    // under NORMAL, there is no pin action for a compare match
                    // under CTC, the action is to clear the pin.
                    */

                    int count = read16(TCNTnH_reg, TCNTnL_reg);
                    Pin pin = ((Pin) getPin("OC" + n + s));
                    int compareMode = Arithmetic.getBit(val, COMnx1) ? 2 : 0;
                    compareMode |= Arithmetic.getBit(val, COMnx0) ? 1 : 0;
                    if (count == compare) {

                        switch (compareMode) {
                            case 1:
                                pin.write(!pin.read()); // clear
                                break;
                            case 2:
                                pin.write(false);
                                break;
                            case 3:
                                pin.write(true);
                                break;
                        }

                    }
                }

                private void forcedOutputCompareA() {
                    int compare = read16(OCRnAH_reg, OCRnAL_reg);
                    forcedCompare("A", TCCRnA_reg.read(), compare, 7, 6);
                }

                private void forcedOutputCompareB() {
                    int compare = read16(OCRnBH_reg, OCRnBL_reg);
                    forcedCompare("B", TCCRnA_reg.read(), compare, 5, 4);
                }

                private void forcedOutputCompareC() {
                    int compare = read16(OCRnCH_reg, OCRnCL_reg);
                    forcedCompare("C", TCCRnA_reg.read(), compare, 3, 2);
                }
            }


            /** In PWN modes, writes to the OCRnx registers are
             * buffered. Specifically, the actual write is delayed
             * until a certain event (the counter reaching either TOP
             * or BOTTOM) specified by the particular PWN
             * mode. BufferedRegister implements this by writing to a
             * buffer register on a write and reading from the
             * buffered register in a read. When the buffered register
             * is to be updated, the flush() method should be
             * called.  */
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
             * The <code>Ticker</class> implements the periodic behavior of the timer.
             * It emulates the operation of the timer at each clock cycle and uses the
             * global timed event queue to achieve the correct periodic behavior.
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
                        timerPrinter.println("Timer" + n + " [TCNT" + n + " = " + count + ", OCR" + n + "A = " + compareA + "],  OCR" + n + "B = " + compareB + "], OCR" + n + "C = " + compareC + "]");
                    }

                    // What exactly should I do when I phase/frequency current?
                    // TODO: Figure out what differentiates the OCRnA, B, C
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
                                //if (count >= 0x00ff) {
                                countUp = false;
                                //count = 0x0ff;
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
                                //compareMatch();
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
                            compareMatchA();
                        }
                        if (countSave == compareC) {
                            compareMatchC();
                        }
                    }
                    write16(count, TCNTnH_reg, TCNTnL_reg);
                    // make sure timings on this are correct
                    blockCompareMatch = false;


                    if (period != 0) insertEvent(this, period);
                }
            }
        }


        protected class Timer8Bit {
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

            boolean timerEnabled;
            boolean countUp;
            int timerMode;
            long period;

            /* pg. 93 of manual. Block compareMatch for one period after
             * TCNTn is written to. */
            boolean blockCompareMatch;

            final int OCIEn;
            final int TOIEn;
            final int OCFn;
            final int TOVn;

            final int[] periods;

            Verbose.Printer timerPrinter;

            /** Timer8Bit(ns, TCNT0, TCNT0, OCR0) should initialize
             * Timer0 as before. Assuming this translation from the
             * Timer0 code was generic enough, Timer8Bit(ns, TCNT2,
             * TCNT2, OCR2) should initialize a mostly functional
             * Timer2. OCRn is the offset on TIMSK that corresponds to */
            private Timer8Bit(BaseInterpreter ns, int n, int TCCRn, int TCNTn, int OCRn, int OCIEn, int TOIEn, int OCFn, int TOVn, int[] periods) {
                timerPrinter = Verbose.getVerbosePrinter("sim.timer" + n);
                ticker = new Ticker();
                TCCRn_reg = new ControlRegister();
                TCNTn_reg = new TCNTnRegister();
                OCRn_reg = new BufferedRegister();

                this.OCIEn = OCIEn;
                this.TOIEn = TOIEn;
                this.OCFn = OCFn;
                this.TOVn = TOVn;
                this.n = n;
                this.periods = periods;

                installIOReg(ns, TCCRn, TCCRn_reg);
                installIOReg(ns, TCNTn, TCNTn_reg);
                installIOReg(ns, OCRn, OCRn_reg);
            }

            protected void compareMatch() {
                if (timerPrinter.enabled) {
                    boolean enabled = TIMSK_reg.readBit(OCIEn);
                    timerPrinter.println("Timer" + n + ".compareMatch (enabled: " + enabled + ")");
                }
                // set the compare flag for this timer
                TIFR_reg.flagBit(OCFn);
                // if the mode is correct, modify pin OCn. but if the flag is
                // already connected to the pin, does this happen automatically
                // with the last previous call?
                //compareMatchPin();
            }

            protected void overflow() {
                if (timerPrinter.enabled) {
                    boolean enabled = TIMSK_reg.readBit(TOIEn);
                    timerPrinter.println("Timer" + n + ".overFlow (enabled: " + enabled + ")");
                }
                // set the overflow flag for this timer
                TIFR_reg.flagBit(TOVn);
            }

            /** Overloads the write behavior of this class of register
             * in order to implement compare match blocking for one
             * timer period. */
            protected class TCNTnRegister extends State.RWIOReg {

                public void write(byte val) {
                    value = val;
                    blockCompareMatch = true;
                }

                public void writeBit(int bit, boolean val) {
                    value = Arithmetic.setBit(value, bit, val);
                    blockCompareMatch = true;
                }
            }

            /** <code>BufferedRegister</code> implements a register
             * with a write buffer. In PWN modes, writes to this
             * register are not performed until flush() is called. In
             * non-PWM modes, the writes are immediate. */
            protected class BufferedRegister extends State.RWIOReg {
                final State.RWIOReg register;

                protected BufferedRegister() {
                    this.register = new State.RWIOReg();
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

            protected class ControlRegister extends State.RWIOReg {
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
                    value = (byte) (val & 0x7f);

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

                    Pin pin = (Pin) getPin("OC" + n);

                    if (count == compare) {
                        switch (compareMode) {
                            case 1:
                                pin.write(!pin.read()); // clear
                                break;
                            case 2:
                                pin.write(false);
                                break;
                            case 3:
                                pin.write(true);
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
                            if (timerPrinter.enabled) timerPrinter.println("Timer" + n + " disabled");
                            removeEvent(ticker);
                        }
                        return;
                    }
                    if (timerEnabled) {
                        removeEvent(ticker);
                    }
                    if (timerPrinter.enabled) timerPrinter.println("Timer" + n + " enabled: period = " + m + " mode = " + timerMode);
                    period = m;
                    timerEnabled = true;
                    insertEvent(ticker, period);
                }
            }

            /**
             * The <code>Ticker</class> implements the periodic behavior of the timer.
             * It emulates the operation of the timer at each clock cycle and uses the
             * global timed event queue to achieve the correct periodic behavior.
             */
            protected class Ticker implements Simulator.Event {

                public void fire() {
                    // perform one clock tick worth of work on the timer
                    int count = TCNTn_reg.read() & 0xff;
                    int compare = OCRn_reg.read() & 0xff;
                    int countSave = count;
                    if (timerPrinter.enabled)
                        timerPrinter.println("Timer" + n + " [TCNT" + n + " = " + count + ", OCR" + n + "(actual) = " + compare + ", OCR" + n + "(buffer) = " + (0xff & OCRn_reg.readBuffer()) + "]");

                    switch (timerMode) {
                        case MODE_NORMAL: // NORMAL MODE
                            count++;
                            countSave = count;
                            if (count == MAX) {
                                // is this compareMatch occuring at the correct
                                // time? CHECK.
                                // I'm moving this out.  -- Daniel
                                //compareMatch();
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
                            count++;
                            countSave = count;
                            if (count == compare) {
                                //compareMatch();
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
                    TCNTn_reg.write((byte) count);
                    // I probably want to verify the timing on this.
                    blockCompareMatch = false;

                    if (period != 0) insertEvent(this, period);
                }
            }
        }


        private void populateState(BaseInterpreter ns) {
            // set up the external interrupt mask and flag registers and interrupt range
            EIFR_reg = buildInterruptRange(ns, true, EIMSK, EIFR, 2, 8);
            EIMSK_reg = EIFR_reg.maskRegister;

            // set up the timer mask and flag registers and interrupt range
            TIFR_reg = buildInterruptRange(ns, false, TIMSK, TIFR, 17, 8);
            TIMSK_reg = TIFR_reg.maskRegister;


            /* For whatever reason, the ATMega128 engineers decided
               against having the bitorder for the ETIFR/ETIMSK
               registers line up with the corresponding block of the
               interrupt vector. Therefore, we have to line this up by
               hand.
            */
            //ETIFR_reg = new FlagRegister(true, 25);
            int[] ETIFR_mapping = {25, 29, 30, 28, 27, 26, -1, -1};
            ETIFR_reg = new UnorderedFlagRegister(true, 25, ETIFR_mapping); // false, 0 are just placeholder falues
            ETIMSK_reg = (UnorderedMaskRegister) ETIFR_reg.maskRegister;


            // Timer1 COMPC
            interrupts[25] = new Simulator.MaskableInterrupt(25, ETIMSK_reg, ETIFR_reg, 0, false);
            // Timer3 CAPT
            interrupts[26] = new Simulator.MaskableInterrupt(26, ETIMSK_reg, ETIFR_reg, 5, false);
            // Timer3 COMPA
            interrupts[27] = new Simulator.MaskableInterrupt(27, ETIMSK_reg, ETIFR_reg, 4, false);
            // Timer3 COMPB
            interrupts[28] = new Simulator.MaskableInterrupt(28, ETIMSK_reg, ETIFR_reg, 3, false);
            // Timer3 COMPC
            interrupts[29] = new Simulator.MaskableInterrupt(29, ETIMSK_reg, ETIFR_reg, 1, false);
            // Timer3 OVF
            interrupts[30] = new Simulator.MaskableInterrupt(30, ETIMSK_reg, ETIFR_reg, 2, false);

            installIOReg(ns, ETIMSK, ETIMSK_reg);
            installIOReg(ns, ETIFR, ETIFR_reg);

            // 8-Bit Timers
            // build timer 0
            new Timer0(ns);
            // build timer 2
            if (!compatibilityMode) new Timer2(ns);

            // 16-Bit Timers
            // build timer 1
            new Timer1(ns);
            // build Timer 3
            if (!compatibilityMode) new Timer3(ns);

            buildPorts(ns);
            // TODO: build other devices

            new EEPROM(ns);

            new USART0(ns);
            //new USART1(ns);

            // Add SPI device by Simon
            new SPI(ns);


        }


        private void buildPorts(BaseInterpreter ns) {
            buildPort(ns, 'A', PORTA, DDRA, PINA);
            buildPort(ns, 'B', PORTB, DDRB, PINB);
            buildPort(ns, 'C', PORTC, DDRC, PINC);
            buildPort(ns, 'D', PORTD, DDRD, PIND);
            buildPort(ns, 'E', PORTE, DDRE, PINE);
            buildPort(ns, 'F', PORTF, DDRF, PINF);
        }

        private void buildPort(BaseInterpreter ns, char p, int portreg, int dirreg, int pinreg) {
            Pin[] pins = new Pin[8];
            for (int cntr = 0; cntr < 8; cntr++)
                pins[cntr] = (Pin) getPin("P" + p + cntr);
            installIOReg(ns, portreg, new PortRegister(pins));
            installIOReg(ns, dirreg, new DirectionRegister(pins));
            installIOReg(ns, pinreg, new PinRegister(pins));
        }

        private void installIOReg(BaseInterpreter ns, int num, State.IOReg ior) {
            // in compatbility mode, the upper IO registers do not exist.
            if (compatibilityMode && num > IOREG_SIZE_103) return;
            ns.setIOReg(num, ior);
        }

        private FlagRegister buildInterruptRange(BaseInterpreter ns, boolean increasing, int maskRegNum, int flagRegNum, int baseVect, int numVects) {
            FlagRegister fr = new FlagRegister(increasing, baseVect);
            for (int cntr = 0; cntr < numVects; cntr++) {
                int inum = increasing ? baseVect + cntr : baseVect - cntr;
                interrupts[inum] = new Simulator.MaskableInterrupt(inum, fr.maskRegister, fr, cntr, false);
                installIOReg(ns, maskRegNum, fr.maskRegister);
                installIOReg(ns, flagRegNum, fr);
            }
            return fr;
        }

        private int read16(State.RWIOReg high, State.RWIOReg low) {
            int result = low.read() & 0xff;
            result |= (high.read() & 0xff) << 8;
            return result;
        }

        private void write16(int val, State.RWIOReg high, State.RWIOReg low) {

            high.write((byte) ((val & 0xff00) >> 8));
            low.write((byte) (val & 0x00ff));
        }

        /**
         * This is an implementation of the non-volatile EEPROM on the
         * ATMega128 microcontroller.  TODO: CPU halting after
         * reads/writes.
         * @author Daniel Lee
         */
        protected class EEPROM {
            final byte[] EEPROM_data = new byte[EEPROM_SIZE];
            final State.RWIOReg EEDR_reg;
            final EECRReg EECR_reg;
            final State.RWIOReg EEARL_reg;
            final EEARHReg EEARH_reg;

            final BaseInterpreter interpreter;

            final Verbose.Printer eepromPrinter;

            // flag bits on EECR
            final int EERIE = 3;
            final int EEMWE = 2;
            final int EEWE = 1;
            final int EERE = 0;

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
            EEPROM(BaseInterpreter ns) {
                eepromPrinter = Verbose.getVerbosePrinter("sim.eeprom");
                interpreter = ns;

                ticker = new EEPROMTicker();

                //EEDR_reg = new State.RWIOReg();
                EEDR_reg = new TestingRegister("EEDR");
                EECR_reg = new EECRReg();
                EEARL_reg = new State.RWIOReg();
                //EEARL_reg = new TestingRegister("EEARL_reg");
                EEARH_reg = new EEARHReg();

                installIOReg(ns, EEDR, EEDR_reg);
                installIOReg(ns, EECR, EECR_reg);
                installIOReg(ns, EEARL, EEARL_reg);
                installIOReg(ns, EEARH, EEARH_reg);

            }

            protected class EEARHReg extends State.RWIOReg {
                public void write(byte val) {
                    value = (byte) (0xff & val);
                    insertEvent(ticker, 1);
                }

                public void writeBit(int bit, boolean val) {
                    if (bit < 4) {
                        super.writeBit(bit, val);
                    }
                    insertEvent(ticker, 1);
                }

            }

            protected class EECRReg extends State.RWIOReg {

                public void decode(byte val) {
                    boolean readEnableOld = readEnable;
                    readEnable = (value & 0x1) != 0;
                    if (!readEnableOld && readEnable) {
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: EERE flagged");
                        readEnableWritten = true;
                    }
                    boolean writeEnableOld = writeEnable;
                    writeEnable = (value & 0x2) != 0;
                    if (!writeEnableOld && writeEnable) {
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: EEWE flagged");
                        writeEnableWritten = true;
                    }
                    masterWriteEnable = (value & 0x4) != 0;
                    interruptEnable = (value & 0x8) != 0;
                    insertEvent(ticker, 1);
                }

                public void write(byte val) {

                    boolean masterWriteEnableOld = masterWriteEnable;
                    value = (byte) (0xff & val);
                    if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: EECR written to, val = " + value);
                    decode(value);
                    if (!masterWriteEnableOld && masterWriteEnable) {
                        // EEWE has been written to. reset write count
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: reset write count to 4");
                        writeCount = 4;
                    }
                }

                public void writeBit(int bit, boolean val) {
                    boolean masterWriteEnableOld = masterWriteEnable;
                    if (bit < 4) {
                        super.writeBit(bit, val);
                    }
                    if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: EECR written to, val = " + value);
                    decode(value);
                    if (!masterWriteEnableOld && masterWriteEnable) {
                        // EEWE has been written to. reset write count
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: reset write count to 4");
                        writeCount = 4;
                    }
                }
            }

            protected class EEPROMTicker implements Simulator.Event {
                public void fire() {
                    /*if(eepromPrinter.enabled) {
                    eepromPrinter.println("EEPROM: " + EECR_reg.read() + " " + EEARH_reg.read() + " " + EEARL_reg.read());
                    }
                    */
                    if (eepromPrinter.enabled) {
                        eepromPrinter.println("Tick : " + writeCount);
                    }

                    if (interruptEnable && !writeEnable) {
                        // post interrupt
                        // do I need to check SREG[I] ?
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: posting interrupt.");
                        interpreter.postInterrupt(23);
                    }
                    int address = read16(EEARH_reg, EEARL_reg);
                    if (writeCount > 0) {
                        // if EEMWE has been written to 1 within
                        // 4 clock cycles, write data

                        // after 4 cycles, clear this bit
                        // implement blocking CPU

                        if (writeEnableWritten) {
                            if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: " + EEDR_reg.read() + " written to " + address);
                            EEPROM_data[address] = EEDR_reg.read();
                        }

                    }
                    if (readEnableWritten) {
                        // read
                        // implement blocking CPU
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: " + EEPROM_data[address] + " read from " + address);
                        EEDR_reg.write(EEPROM_data[address]);

                    }
                    if (writeCount > 0) {
                        writeCount--;
                        insertEvent(ticker, 1);
                    }

                    if (writeCount == 0) {
                        // clear EEWE
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: write count hit 0, clearing EEWE");
                        writeCount--;
                        EECR_reg.writeBit(1, false);
                    }
                    writeEnableWritten = false;
                    readEnableWritten = false;
                }
            }

        }

        protected class TestingRegister extends State.RWIOReg {
            String name;
            Verbose.Printer uartPrinter = Verbose.getVerbosePrinter("testing");

            TestingRegister(String name) {
                this.name = name;
            }

            public void write(byte val) {
                if (uartPrinter.enabled) uartPrinter.println("TESTING: " + name + " written to value " + Integer.toBinaryString(0xff & val));
                super.write(val);
            }

            public void writeBit(int bit, boolean val) {
                if (uartPrinter.enabled) uartPrinter.println("TESTING: " + name + " written to value " + Integer.toBinaryString(0xff & value));
                super.writeBit(bit, val);
            }

            public byte read() {
                if (uartPrinter.enabled) uartPrinter.println("TESTING: " + name + " read from, value " + Integer.toBinaryString(0xff & value));
                return super.read();
            }

            public boolean readBit(int bit) {
                if (uartPrinter.enabled) uartPrinter.println("TESTING: " + name + " read from, value " + Integer.toBinaryString(0xff & value));
                return super.readBit(bit);
            }
        }

        /** Emulates the behavior of USART0 on the ATMega128L
         * microcontroller. */
        protected class USART0 extends USART {

            USART0(BaseInterpreter ns) {
                super(ns);
            }

            protected void initValues() {
                n = 0;
                UDRn = UDR0;
                UCSRnA = UCSR0A;
                UCSRnB = UCSR0B;
                UCSRnC = UCSR0C;
                UBRRnL = UBRR0L;
                UBRRnH = UBRR0H;

                USARTnRX = 19;
                USARTnUDRE = 20;
                USARTnTX = 21;

                int[] UCSR1A_mapping = {-1, -1, -1, -1, -1, 20, 21, 19};
                INTERRUPT_MAPPING = UCSR1A_mapping;

            }
        }

        /** Emulates the behavior of USART1 on the ATMega128L
         * microcontroller. */
        protected class USART1 extends USART {

            USART1(BaseInterpreter ns) {
                super(ns);
            }

            protected void initValues() {
                n = 1;
                UDRn = UDR1;
                UCSRnA = UCSR1A;
                UCSRnB = UCSR1B;
                UCSRnC = UCSR1C;
                UBRRnL = UBRR1L;
                UBRRnH = UBRR1H;

                USARTnRX = 31;
                USARTnUDRE = 32;
                USARTnTX = 33;
            }
        }

        /** The USART class implements a Universal Synchronous
         * Asynchronous Receiver/Transmitter, which is a serial device
         * on the ATMega128L. The ATMega128L has two USARTs, USART0
         * and USART1. */
        protected abstract class USART implements USARTDevice {

            // UNIMPLEMENTED:
            // Synchronous Mode
            // Multi-processor communication mode

            /*
              Ways in which this USART is not accurate: Whole frame
              delayed transmission, as opposed to sending single bits
              at a time.  Parity errors are not searched for.
              Presumably, parity errors will not occur. Similarly,
              frame errors are should not occur.
             */


            final DataRegister UDRn_reg;
            final ControlRegisterA UCSRnA_reg;
            final ControlRegisterB UCSRnB_reg;
            final ControlRegisterC UCSRnC_reg;
            final UBRRnLReg UBRRnL_reg;
            final UBRRnHReg UBRRnH_reg;

            final BaseInterpreter interpreter;

            final Transmitter transmitter;
            final Receiver receiver;

            final Verbose.Printer usartPrinter;

            USARTDevice connectedDevice;

            int n;
            int UDRn;
            int UCSRnA;
            int UCSRnB;
            int UCSRnC;
            int UBRRnL;
            int UBRRnH;

            int USARTnRX;
            int USARTnUDRE;
            int USARTnTX;

            int[] INTERRUPT_MAPPING;

            //boolean UDREnFlagged;

            final int RXCn = 7;
            final int TXCn = 6;
            final int UDREn = 5;
            final int FEn = 4;
            final int DORn = 3;
            final int UPEn = 2;
            final int U2Xn = 1;
            final int MPCMn = 0;

            final int RXCIEn = 7;
            final int TXCIEn = 6;
            final int UDRIEn = 5;
            final int RXENn = 4;
            final int TXENn = 3;
            final int UCSZn2 = 2;
            final int RXB8n = 1;
            final int TXB8n = 0;

            // bit 7 is reserved
            final int UMSELn = 6;
            final int UPMn1 = 5;
            final int UPMn0 = 4;
            final int USBSn = 3;
            final int UCSZn1 = 2;
            final int UCSZn0 = 1;
            final int UCPOLn = 0;

            // parity modes
            final int PARITY_DISABLED = 0;
            // 2 is reserved
            final int PARITY_EVEN = 2;
            final int PARITY_ODD = 3;


            final int[] SIZE = {5, 6, 7, 8, 8, 8, 8, 9};

            int period = 0;
            int UBRRMultiplier = 16;
            int frameSize = 8; // does this default to 5?

            /* *********************************************** */
            /* Methods to implement the USARTDevice interface */

            public USARTFrame transmitFrame() {
                return new USARTFrame(UDRn_reg.transmitRegister.read(), UCSRnB_reg.readBit(TXB8n), frameSize);
            }

            public void receiveFrame(USARTFrame frame) {
                UDRn_reg.receiveRegister.writeFrame(frame);
            }

            /* *********************************************** */

            /*
            //ETIFR_reg = new FlagRegister(true, 25);
            int [] ETIFR_mapping = {25, 29, 30, 28, 27, 26, -1, -1};
            ETIFR_reg = new UnorderedFlagRegister(true, 25, ETIFR_mapping); // false, 0 are just placeholder falues
            ETIMSK_reg = (UnorderedMaskRegister)ETIFR_reg.maskRegister;


            // increasing?

            int [] UCSR2A_mapping = {-1, -1, -1, -1, -1, 33, 31, 32};

             */

            abstract protected void initValues();

            USART(BaseInterpreter ns) {
                initValues();
                //UDRn_reg = new TestingRegister("UDR" + n);
                UDRn_reg = new DataRegister();

                UCSRnA_reg = new ControlRegisterA();
                UCSRnB_reg = new ControlRegisterB();
                UCSRnC_reg = new ControlRegisterC();
                UBRRnL_reg = new UBRRnLReg();
                UBRRnH_reg = new UBRRnHReg();

                interpreter = ns;

                transmitter = new Transmitter();
                receiver = new Receiver();

                connectedDevice = new SerialPrinter();

                usartPrinter = Verbose.getVerbosePrinter("sim.usart" + n);

                installIOReg(ns, UDRn, UDRn_reg);
                installIOReg(ns, UCSRnA, UCSRnA_reg);
                installIOReg(ns, UCSRnB, UCSRnB_reg);
                installIOReg(ns, UCSRnC, UCSRnC_reg);
                installIOReg(ns, UBRRnL, UBRRnL_reg);
                installIOReg(ns, UBRRnH, UBRRnH_reg);

                // USART Receive Complete
                interrupts[19] = new Simulator.MaskableInterrupt(19, UCSRnB_reg, UCSRnA_reg, 7, false);
                // USART Data Register Empty
                interrupts[20] = new Simulator.MaskableInterrupt(20, UCSRnB_reg, UCSRnA_reg, 5, false);
                // USART Transmit Complete
                interrupts[21] = new Simulator.MaskableInterrupt(21, UCSRnB_reg, UCSRnA_reg, 6, false);

            }

            void updatePeriod() {
                period = read16(UBRRnH_reg, UBRRnL_reg) + 1;
                period *= UBRRMultiplier;
            }

            protected class Transmitter {
                boolean transmitting = false;
                Transmit transmit = new Transmit();

                protected void enableTransmit() {
                    if (!transmitting) {
                        //System.err.println("Enabled transmitter"  + " " + simulator.getState().getCycles() + " " + count++);
                        transmit.frame = transmitFrame();
                        UCSRnA_reg.flagBit(UDREn);
                        transmitting = true;
                        insertEvent(transmit, (1 + frameSize + stopBits) * period);
                    }
                }

                protected class Transmit implements Simulator.Event {
                    USARTFrame frame;

                    public void fire() {
                        connectedDevice.receiveFrame(frame);


                        if (usartPrinter.enabled)
                            usartPrinter.println("USART: Transmitted frame " + frame /*+ " " + simulator.getState().getCycles()*/);
                        transmitting = false;
                        UCSRnA_reg.flagBit(TXCn);
                        if (!UCSRnA_reg.readBit(UDREn)) {
                            transmitter.enableTransmit();
                        }
                    }
                }
            }

            /** Initiate a receive between the UART and the connected device. */
            public void startReceive() {
                receiver.enableReceive();
            }

            protected class Receiver {

                boolean receiving = false;
                Receive receive = new Receive();

                protected void enableReceive() {
                    if (!receiving) {
                        //System.err.println("Enabled receiver" /* + " " + simulator.getState().getCycles()*/);
                        receive.frame = connectedDevice.transmitFrame();
                        insertEvent(receive, (1 + frameSize + stopBits) * period);
                        receiving = true;
                    }
                }


                protected class Receive implements Simulator.Event {
                    USARTFrame frame;

                    public void fire() {
                        receiveFrame(frame);

                        if (usartPrinter.enabled)
                            usartPrinter.println("USART: Received frame " + frame + " " + simulator.getState().getCycles() + " " + UBRRnH_reg.read() + " " + UBRRnL_reg.read() + " " + UBRRMultiplier + " ");

                        UCSRnA_reg.flagBit(RXCn);

                        receiving = false;

                    }
                }
            }

            /** The <code>DataRegister</code> class represents a Transmit
             * Data Buffer Register for a USART. It is really two
             * registers, a transmit register and a receive
             * register. The transmit register is the destination of
             * data written to the register at this address. The
             * receive register is the source of data read from this
             * address.*/
            protected class DataRegister extends State.RWIOReg {
                State.RWIOReg transmitRegister;
                TwoLevelFIFO receiveRegister;
                /* figure out what the two level fifo for the receive
                   register is */

                DataRegister() {
                    //transmitRegister = new State.RWIOReg();
                    transmitRegister = new FooReg();
                    receiveRegister = new TwoLevelFIFO();
                }

                private class FooReg extends State.RWIOReg {
                    public void write(byte val) {
                        super.write(val);
                        //System.err.println("Wrote to transmit buffer " + val);
                    }
                }

                public void write(byte val) {
                    // check UDREn flag

                    if (UCSRnA_reg.readBit(UDREn)) {
                        transmitRegister.write(val);
                        //UCSRnA_reg.flagBit(UDREn);
                        UCSRnA_reg.writeBit(UDREn, false);
                        if (UCSRnB_reg.readBit(TXENn)) {
                            transmitter.enableTransmit();
                        }
                    }
                }

                public void writeBit(int bit, boolean val) {
                    // check UDREn flag
                    if (UCSRnA_reg.readBit(UDREn)) {
                        transmitRegister.writeBit(bit, val);
                        UCSRnA_reg.writeBit(UDREn, false);
                        if (UCSRnB_reg.readBit(TXENn)) transmitter.enableTransmit();

                    }
                }

                public byte read() {
                    return receiveRegister.read();
                }

                public boolean readBit(int bit) {
                    return receiveRegister.readBit(bit);
                }

                private class TwoLevelFIFO extends State.RWIOReg {

                    LinkedList readyQueue;
                    LinkedList waitQueue;

                    TwoLevelFIFO() {
                        readyQueue = new LinkedList();
                        waitQueue = new LinkedList();
                        waitQueue.add(new USARTFrameWrapper());
                        waitQueue.add(new USARTFrameWrapper());
                        waitQueue.add(new USARTFrameWrapper());
                    }

                    public boolean readBit(int bit) {
                        return Arithmetic.getBit(bit, read());
                    }

                    public byte read() {
                        if (readyQueue.isEmpty()) {
                            System.err.println("read 0 empty");
                            return (byte) 0;
                        }
                        USARTFrameWrapper current = (USARTFrameWrapper) readyQueue.removeLast();
                        if (readyQueue.isEmpty()) {
                            UCSRnA_reg.writeBit(RXCn, false);
                        }
                        UCSRnB_reg.writeBit(RXB8n, current.frame.high);
                        waitQueue.add(current);
                        System.err.println("read " + current.frame.low);
                        return current.frame.low;
                    }

                    public void writeFrame(USARTFrame frame) {
                        if (waitQueue.isEmpty()) {
                            // data overrun. drop frame
                            UCSRnA_reg.writeBit(DORn, true);
                        } else {
                            USARTFrameWrapper current = (USARTFrameWrapper) (waitQueue.removeLast());
                            current.frame = frame;
                            readyQueue.addFirst(current);
                        }
                    }

                    protected void flush() {
                        // clear RXCn
                    }

                    private class USARTFrameWrapper {
                        USARTFrame frame;
                    }

                }

            }


            protected class ControlRegisterA extends UnorderedFlagRegister {

                public ControlRegisterA() {
                    super(true, 19, INTERRUPT_MAPPING);
                    value = 0x20; // init UDREn to true


                }

                public void write(byte val) {
                    //System.err.println("Control A BYTE " + Integer.toBinaryString(val) + " " + read() + " " + UCSRnB_reg.read());
                    super.write((byte) (0xe3 & val));
                    decode(val);

                }

                public void writeBit(int bit, boolean val) {
                    //System.err.println("Control A BIT  " + Integer.toBinaryString(value) + " " + bit + " " + val);


                    byte old = value;
                    if (bit < 1 || bit > 4) {
                        super.writeBit(bit, val);
                    }
                    decode(value);

                }

                protected void decode(byte val) {
                    boolean U2XnVal = readBit(U2Xn);
                    boolean MPCMnVal = UCSRnC_reg.readBit(UMSELn);

                    int multiplierState = U2XnVal ? 0x1 : 0;
                    multiplierState |= MPCMnVal ? 0x2 : 0;


                    switch (multiplierState) {
                        case 0:
                            UBRRMultiplier = 16;
                            break;
                        case 1:
                            UBRRMultiplier = 8;
                            break;
                        case 2:
                            UBRRMultiplier = 2;
                            break;
                        default:
                            UBRRMultiplier = 2;
                            break;
                    }
                }

            }

            protected class ControlRegisterB extends UnorderedMaskRegister {
                int count = 0;

                ControlRegisterB() {
                    super(true, 19, UCSRnA_reg, INTERRUPT_MAPPING);
                    UCSRnA_reg.maskRegister = this;
                }

                public void write(byte val) {

                    boolean oldReadEnable = readBit(RXENn);
                    super.write(val);

                    decode(val);
                }

                public void writeBit(int bit, boolean val) {
                    boolean oldReadEnable = readBit(RXENn);
                    super.writeBit(bit, val);

                    decode(value);
                }

                protected void decode(byte value) {


                    if (readBit(UCSZn2) && UCSRnC_reg.readBit(UCSZn1)
                            && UCSRnC_reg.readBit(UCSZn0)) {
                        frameSize = 9;
                    }

                }
            }

            int stopBits = 1;

            protected class ControlRegisterC extends State.RWIOReg {

                protected void decode(byte val) {

                    stopBits = readBit(USBSn) ? 2 : 1;

                    int UCSZVal = UCSRnB_reg.readBit(UCSZn2) ? 0x4 : 0x0;
                    UCSZVal |= readBit(UCSZn1) ? 0x2 : 0x0;
                    UCSZVal |= readBit(UCSZn0) ? 0x1 : 0x0;

                    //frameSize = SIZE[UCSZVal];
                    // why are they using a 5 bit frame size?
                    frameSize = 8;
                }

                public void write(byte val) {
                    super.write((byte) (0x7f & val));
                    decode(val);
                }

                public void writeBit(int bit, boolean val) {
                    if (bit < 7) {
                        super.writeBit(bit, val);
                    }
                    decode(value);
                }

            }

            protected class UBRRnHReg extends State.RWIOReg {

                public void write(byte val) {
                    super.write((byte) (0x0f & val));
                }

                public void writeBit(int bit, boolean val) {
                    if (bit < 4) {
                        super.writeBit(bit, val);
                    }
                }
            }

            protected class UBRRnLReg extends State.RWIOReg {

                public void write(byte val) {
                    super.write(val);
                    updatePeriod();
                }

                public void writeBit(int bit, boolean val) {
                    super.writeBit(bit, val);
                    updatePeriod();
                }
            }

            protected class SerialPrinter implements USARTDevice {

                Verbose.Printer serialPrinter = Verbose.getVerbosePrinter("serialprinter");
                //char [] stream = {0};

                //char[] stream=  {'h', 'e', 'l', 'l', 'o',  'w', 'o', 'r', 'l', 'd'};
                char[] stream = {0, 0, 0, 2, 0, 0, 2};
                int count = 0;

                public USARTFrame transmitFrame() {
                    //return new SerialFrame((byte)0, false, 8);
                    return new USARTFrame((byte) stream[count++ % stream.length], false, 8);
                }

                public void receiveFrame(USARTFrame frame) {
                    if (serialPrinter.enabled) serialPrinter.println("Serial Printer " + frame.toString());
                    //System.err.println("Serial Printer " + frame.toString());
                }

                SerialPrinter() {
                    PrinterTicker printerTicker = new PrinterTicker();

                    insertPeriodicEvent(printerTicker, 2000);
                }

                private class PrinterTicker implements Simulator.Event {
                    public void fire() {
                        receiver.enableReceive();
                    }
                }


            }


        }

        protected class LCDScreen implements USARTDevice {
            Verbose.Printer lcdPrinter = Verbose.getVerbosePrinter("sim.lcd");

            boolean mode;

            final boolean MODE_DATA = false;
            final boolean MODE_INSTRUCTION = true;

            final int CLEAR_SCREEN = 1;
            final int SCROLL_LEFT = 24;
            final int SCROLL_RIGHT = 28;
            final int HOME = 2;
            final int CURSOR_UNDERLINE = 14;
            final int CURSOR_BLOCK = 13;
            final int CURSOR_INVIS = 12;
            final int BLANK_DISPLAY = 8;
            final int RESTORE_DISPLAY = 12;


            int cursor;

            final Character dump = new Character('c');

            public Character memory(byte cursor) {
                if (cursor < 40) {
                    return (Character) line1.get(cursor);
                } else if (cursor < 80) {
                    return (Character) line2.get(cursor - 40);
                } else {
                    return dump;
                }
            }

            final Vector line1 = new Vector(40);
            final Vector line2 = new Vector(40);

            public USARTFrame transmitFrame() {
                return new USARTFrame((byte) 0, false, 8);
            }

            public void receiveFrame(USARTFrame frame) {
                byte data = frame.low;

                if (mode) { // Instruction mode
                    switch (data) {
                        case CLEAR_SCREEN:
                            break;
                        case SCROLL_LEFT:
                            break;
                        case SCROLL_RIGHT:
                            break;
                        case HOME:
                            cursor = 128;
                            break;
                        default:
                            if (data >= 192) {
                                cursor = data + 40 - 192;
                            } else if (data >= 128) {
                                cursor = data - 128;
                            }
                            break;
                    }
                    mode = MODE_DATA;
                } else if (data == 254) {
                    mode = MODE_INSTRUCTION;
                } else {
                    //memory[cursor] = frame.low;
                    cursor = (cursor + 1) % 80;
                    lcdPrinter.print(this.toString());
                }
            }

            public String toString() {
                return "";
            }
        }


        /**
         * SPI Class by Simon
         */
        protected class SPI {
            final SPDReg SPDR_reg;
            final State.RWIOReg SPCR_reg;
            final SPSReg SPSR_reg;
            final SPIInterrupt SPI_int;
            final SPITicker ticker;

            byte radio_in;
            byte radio_out;

// need to complete alternative SPIF bit clear rule!!!

            SPI(BaseInterpreter ns) {
                ticker = new SPITicker();
                SPDR_reg = new SPDReg();
                SPCR_reg = new State.RWIOReg();
                SPSR_reg = new SPSReg();
                SPI_int = new SPIInterrupt();

// add SPI interrupt to simulator
                interrupts[18] = SPI_int;

                installIOReg(ns, SPDR, SPDR_reg);
                installIOReg(ns, SPSR, SPSR_reg);
                installIOReg(ns, SPCR, SPCR_reg);
                insertPeriodicEvent(ticker, millisToCycles(0.418));
            }

            /**
             * Post SPI interrupt
             */
            private void postSPIInterrupt() {
                if (SPCR_reg.readBit(7)) {
                    SPSR_reg.setSPIF();
// TODO: fix access rights for this method
                    interpreter.postInterrupt(18);
                }
            }

            private void unpostSPIInterrupt() {

            }

            class SPITicker implements Simulator.Event {
                public void fire() {
// put raiod_out in the environment
// env = raiod_out;

// read environment and store in raido_in
// radio_in = env;

// post interrupt
                    postSPIInterrupt();

                }
            }

            class SPIInterrupt implements Interrupt {
                public void force() {
                    postSPIInterrupt();
                }

                public void fire() {
                    SPSR_reg.clearSPIF();
                }
            }


            /**
             * The <code>SPDReg</code> class implement fake radio channel
             * Currently, only read and write the whole byte are implemented
             */
            class SPDReg implements State.IOReg {
                /**
                 * The <code>read()</code> method
                 * @return the value from radio environment
                 */
                public byte read() {
                    return radio_in;
                }

                /**
                 * The <code>write()</code> method
                 * @param val the value to radio environment
                 */
                public void write(byte val) {
                    radio_out = val;
                }

                /**
                 * The <code>readBit()</code> method
                 *
                 * @param num
                 * @return false
                 */
                public boolean readBit(int num) {
// XXX not yet implemented
                    return false;
                }

                /**
                 * The <code>writeBit()</code>
                 * @param num
                 */
                public void writeBit(int num, boolean val) {
// XXX not yet implemented
                }
            }

            class SPSReg implements State.IOReg {
                byte value;

                /**
                 * The <code>read()</code> method
                 * @return the value from radio environment
                 */
                public byte read() {
                    return value;
                }

                /**
                 * The <code>write()</code> method
                 * @param val the value to radio environment
                 */
                public void write(byte val) {
                    boolean bit0 = Arithmetic.getBit(val, 0);

                    value = Arithmetic.setBit(value, 0, bit0);
                }

                /**
                 * The <code>readBit()</code> method
                 *
                 * @param num
                 * @return false
                 */
                public boolean readBit(int num) {
                    return Arithmetic.getBit(value, num);
                }

                public void writeBit(int num, boolean val) {
                    if (num == 0) {
                        value = Arithmetic.setBit(value, 0, val);
                    }
                }

                public void setSPIF() {
                    value = Arithmetic.setBit(value, 7);
                }

                public void clearSPIF() {
                    value = Arithmetic.clearBit(value, 7);
                }

                public boolean getSPIF() {
                    return Arithmetic.getBit(value, 7);
                }

            }
        }

    }

    /** A <code>USARTFrame</code> is a representation of the serial
     * frames being passed between the USART and a connected
     * device. */
    public class USARTFrame {
        public byte low;
        public boolean high;
        int size;

        public USARTFrame(byte low, boolean high, int size) {
            this.low = low;
            this.high = high;
            this.size = size;
        }

        public int value() {
            int value = 0;
            switch (size) {
                case 9:
                    value = high ? 0x100 : 0x0;
                    value |= 0xff & low;
                    break;
                case 8:
                    value = 0xff & low;
                    break;
                case 7:
                    value = 0x7f & low;
                    break;
                case 6:
                    value = 0x3f & low;
                    break;
                case 5:
                    value = 0x1f & low;
                    break;
            }
            return value;
        }

        public String toString() {
            if (size == 9) {
                return "" + value();
            } else {
                return "" + (char) value() + " (" + value() + ")";
            }
        }
    }

    /** The <code>USARTDevice</code> interface describes USARTs and
     * other serial devices which can be connected to the USART. For
     * simplicity, a higher-level interface communicating by frames of
     * data is used, rather than bits or a representation of changing
     * voltages.  */
    public interface USARTDevice {
        public USARTFrame transmitFrame();

        public void receiveFrame(USARTFrame frame);
    }

    private class TestWatch implements Simulator.Watch {
        public void fireBeforeRead(Instr i, int address, State state, int data_addr, byte value) {

        }

        public void fireBeforeWrite(Instr i, int address, State state, int data_addr, byte value) {

        }


        public void fireAfterRead(Instr i, int address, State state, int data_addr, byte value) {

        }

        public void fireAfterWrite(Instr i, int address, State state, int data_addr, byte value) {
            if (data_addr == 0xcc) {
                System.err.println("Watched address 0xcc changed to " + value);
            }
        }
    }

}