package avrora.sim.mcu;

import avrora.util.Arithmetic;
import avrora.util.Verbose;
import avrora.core.InstrPrototype;
import avrora.core.Program;
import avrora.sim.Microcontroller;
import avrora.sim.Simulator;
import avrora.sim.State;

/**
 * The <code>ATMega128L</code> class represents the <code>Microcontroller</code>
 * instance that has all the hardware parameters of the ATMega128L microcontroller
 * as produced by Atmel Corporatation.
 * @author Ben L. Titzer
 */
public class ATMega128L implements Microcontroller {

    public static final int HZ = 7327800;

    public static final int SRAM_SIZE = 4096;
    public static final int IOREG_SIZE = 256 - 32;
    public static final int FLASH_SIZE = 128 * 1024;
    public static final int EEPROM_SIZE = 4096;


    /**
     * The <code>getRamSize()</code> method returns the number of bytes of
     * SRAM present on this hardware device. On the Atmega128L
     * this number is 4096 (4KB).
     * @return the number of bytes of SRAM on this hardware device
     */
    public int getRamSize() {
        return SRAM_SIZE;
    }

    /**
     * The <code>getIORegSize()</code> method returns the number of IO registers
     * that are present on this hardware device. On the Atmega128L
     * this number is 224 (256 - 32).
     * @return the number of IO registers supported on this hardware device
     */
    public int getIORegSize() {
        return IOREG_SIZE;
    }

    /**
     * The <code<getFlashSize()</code> method returns the size in bytes of
     * the flash memory on this hardware device. The flash memory stores the
     * initialized data and the machine code instructions of the program. On
     * the Atmega128L, this number is 131,072 (128K).
     * @return the size of the flash memory in bytes
     */
    public int getFlashSize() {
        return FLASH_SIZE;
    }

    /**
     * The <code>getEEPromSize()</code> method returns the size in bytes of
     * the EEPROM on this hardware device. On the ATmega128L, this number is
     * 4096.
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
     * @param i the instruction prototype of the instruction
     * @return true if the specified instruction is supported on this device;
     * false otherwise
     */
    public boolean isSupported(InstrPrototype i) {
        return true;
    }

    /**
     * The <code>loadProgram()</code> method is used to instantiate a simulator
     * for the particular program. It will construct an instance of the
     * <code>Simulator</code> class that has all the properties of this hardware
     * device and has been initialized with the specified program.
     * @param p the program to load onto the simulator
     * @return a <code>Simulator</code> instance that is capable of simulating
     * the hardware device's behavior on the specified program
     */
    public Simulator loadProgram(Program p) {
        return new SimImpl(p);
    }

    /**
     * The <code>getHZ()</code> method returns the number of cycles per second
     * at which this hardware device is designed to run. The
     * Atmega128L runs at 7.3278MHz, so this method will return 7,327,800.
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
     * @param ms a time quantity in milliseconds as a double
     * @return the same time quantity in clock cycles, rounded up to the nearest
     * integer
     */
    public long millisToCycles(double ms) {
        return (long)(ms * HZ / 1000);
    }

    /**
     * The <code>cyclesToMillis()</code> method converts the specified number
     * of cycles to a time quantity in milliseconds. The conversion factor used
     * is the number of cycles per second of this device. This method serves
     * as a utility so that clients need not do repeated work in converting
     * milliseconds to cycles and back.
     * @param cycles the number of cycles
     * @return the same time quantity in milliseconds
     */
    public double cyclesToMillis(long cycles) {
        return 1000*((double)cycles) / HZ;
    }

    public class SimImpl extends Simulator {

        public SimImpl(Program p) {
            super(ATMega128L.this, p);
        }

        public static final int RESET_VECT = 1;
        public static final int EXT_VECT = 2;

        protected FlagRegister EIFR_reg;
        protected MaskRegister EIMSK_reg;

        protected FlagRegister TIFR_reg;
        protected MaskRegister TIMSK_reg;

        /**
         * The <code>Timer0</code> class emulates the functionality and behavior of the
         * 8-bit timer on the Atmega128L. It has several control and data registers and
         * can fire two different interrupts depending on the mode that it has been
         * put into.
         */
        protected class Timer0 {
            public static final int MODE_NORMAL = 0;
            public static final int MODE_PWM = 1;
            public static final int MODE_CTC = 2;
            public static final int MODE_FASTPWM = 3;
            public static final int MAX = 0xff;
            public static final int BOTTOM = 0x00;

            final ControlRegister TCCR0_reg;
            final State.RWIOReg TCNT0_reg;
            final State.RWIOReg OCR0_reg;

            final Ticker ticker;

            boolean timerEnabled;
            boolean countUp;
            int timerMode;
            long period;

            Verbose.Printer timerPrinter = Verbose.getVerbosePrinter("sim.timer0");

            protected Timer0(State ns) {
                ticker = new Ticker();
                TCCR0_reg = new ControlRegister();
                TCNT0_reg = new State.RWIOReg();
                OCR0_reg = new State.RWIOReg();

                ns.setIOReg(TCCR0, TCCR0_reg);
                ns.setIOReg(TCNT0, TCNT0_reg);
                ns.setIOReg(OCR0, OCR0_reg);
            }

            protected void compareMatch() {
                timerPrinter.println("Timer0.compareMatch");
                // set the compare flag for this timer
                TIFR_reg.flagBit(1);
            }

            protected void overflow() {
                timerPrinter.println("Timer0.overflow");
                // set the overflow flag for this timer
                TIFR_reg.flagBit(0);
            }

            protected class ControlRegister extends State.RWIOReg {
                public static final int FOC0 = 7;
                public static final int WGM00 = 6;
                public static final int COM01 = 5;
                public static final int COM00 = 4;
                public static final int WGM01 = 3;
                public static final int CS02 = 2;
                public static final int CS01 = 1;
                public static final int CS00 = 0;

                public void write(byte val) {
                    // hardware manual states that high order bit is always read as zero
                    value = (byte)(val & 0x7f);

                    // decode modes and update internal state
                    decode(val);
                }

                public void setBit(int bit) {
                    if ( bit == 7 ) {
                        // TODO: force output compare
                    } else {
                        value = Arithmetic.setBit(value, bit);
                        decode(value);
                    }
                }

                public void clearBit(int bit) {
                    if ( bit == 7 ) {
                        // do nothing
                    } else {
                        value = Arithmetic.clearBit(value, bit);
                        decode(value);
                    }
                }

                private void decode(byte val) {
                    // get the mode of operation
                    timerMode = Arithmetic.getBit(val, WGM01) ? 2 : 0;
                    timerMode |= Arithmetic.getBit(val, WGM00) ? 1 : 0;

                    int prescaler  = val & 0x7;
                    switch ( prescaler ) {
                        case 0:
                            timerEnabled = false;
                            removeTimerEvent(ticker);
                            break;
                        case 1:
                            resetPeriod(1);
                            break;
                        case 2:
                            resetPeriod(8);
                            break;
                        case 3:
                            resetPeriod(32);
                            break;
                        case 4:
                            resetPeriod(64);
                            break;
                        case 5:
                            resetPeriod(128);
                            break;
                        case 6:
                            resetPeriod(256);
                            break;
                        case 7:
                            resetPeriod(1024);
                            break;
                    }
                }

                private void resetPeriod(int n) {
                    if ( timerEnabled ) removeTimerEvent(ticker);
                    period = n;
                    timerEnabled = true;
                    addTimerEvent(ticker, period);
                }
            }

            /**
             * The <code>Ticker</class> implements the periodic behavior of the timer.
             * It emulates the operation of the timer at each clock cycle and uses the
             * global timed event queue to achieve the correct periodic behavior.
             */
            protected class Ticker implements Simulator.Trigger {

                public void fire() {
                    timerPrinter.println("Timer0.tick");
                    // perform one clock tick worth of work on the timer
                    int count = TCNT0_reg.read() & 0xff;
                    switch ( timerMode ) {
                        case MODE_NORMAL:
                            count++;
                            if ( count == MAX ) {
                                compareMatch();
                                overflow();
                                count = 0;
                            }
                            break;
                        case MODE_PWM:
                            if ( countUp ) count++;
                            else count--;

                            if ( count >= MAX ) {
                                countUp = false;
                                count = MAX;
                            }
                            if ( count <= 0 ) {
                                overflow();
                                countUp = true;
                                count = 0;
                            }
                            if ( count == OCR0_reg.read() ) {
                                compareMatch();
                            }
                            break;
                        case MODE_CTC:
                            count++;
                            if ( count == OCR0_reg.read() ) {
                                compareMatch();
                                count = 0;
                            }
                            break;
                        case MODE_FASTPWM:
                            count++;
                            if ( count == MAX ) {
                                count = 0;
                                overflow();
                            }
                            if ( count == OCR0_reg.read() ) {
                                compareMatch();
                            }
                            break;
                    }
                    TCNT0_reg.write((byte)count);
                    addTimerEvent(this, period);
                }
            }
        }

        protected State constructState() {
            State ns = new State(program, FLASH_SIZE, IOREG_SIZE, SRAM_SIZE);
            // set up the external interrupt mask and flag registers and interrupt range
            EIFR_reg = buildInterruptRange(ns, true, EIMSK, EIFR, 2, 8);
            EIMSK_reg = EIFR_reg.maskRegister;

            // set up the timer mask and flag registers and interrupt range
            TIFR_reg = buildInterruptRange(ns, false, TIMSK, TIFR, 17, 8);
            TIMSK_reg = TIFR_reg.maskRegister;

            // build timer 0
            new Timer0(ns);

            // TODO: build other devices
            return ns;
        }

        private FlagRegister buildInterruptRange(State ns, boolean increasing, int maskRegNum, int flagRegNum, int baseVect, int numVects) {
            FlagRegister fr = new FlagRegister(increasing, baseVect);
            for ( int cntr = 0; cntr < numVects; cntr++ ) {
                int inum = increasing ? baseVect + cntr : baseVect - cntr;
                interrupts[inum] = new Simulator.MaskableInterrupt(inum, fr.maskRegister, fr, cntr, false);
                ns.setIOReg(maskRegNum, fr.maskRegister);
                ns.setIOReg(flagRegNum, fr);
            }
            return fr;
        }

    }
}
