package avrora.sim.mcu;

import avrora.util.Arithmetic;
import avrora.util.Verbose;
import avrora.core.InstrPrototype;
import avrora.core.Program;
import avrora.sim.Simulator;
import avrora.sim.State;

import java.util.HashMap;

/**
 * The <code>ATMega128L</code> class represents the <code>Microcontroller</code>
 * instance that has all the hardware parameters of the ATMega128L microcontroller
 * as produced by Atmel Corporatation.
 * @author Ben L. Titzer
 */
public class ATMega128L implements Microcontroller, MicrocontrollerFactory {

    public static final int HZ = 7327800;

    public static final int SRAM_SIZE = 4096;
    public static final int IOREG_SIZE = 256 - 32;
    public static final int FLASH_SIZE = 128 * 1024;
    public static final int EEPROM_SIZE = 4096;

    public static final int NUM_PINS = 65;

    static Verbose.Printer pinPrinter = Verbose.getVerbosePrinter("sim.pin");

    protected static final HashMap pinNumbers;

    static {
        pinNumbers = new HashMap();

        newPin(1,  "PEN");
        newPin(2,  "PEO", "RXD0", "PDI");
        newPin(3,  "PE1", "TXD0", "PDO");
        newPin(4,  "PE2", "XCK0", "AIN0");
        newPin(5,  "PE3", "OC3A", "AIN1");
        newPin(6,  "PE4", "OC3B", "INT4");
        newPin(7,  "PE5", "OC3C", "INT5");
        newPin(8,  "PE6", "T3",   "INT6");
        newPin(9,  "PE7", "IC3",  "INT7");
        newPin(10, "PB0", "SS");
        newPin(11, "PB1", "SCK");
        newPin(12, "PB2", "MOSI");
        newPin(13, "PB3", "MISO");
        newPin(14, "PB4", "OC0");
        newPin(15, "PB5", "OC1A");
        newPin(16, "PB6", "OC1B");
        newPin(17, "PB7", "OC2",  "OC1C");
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
        boolean level;
        boolean outputDir;
        boolean pullup;

        Microcontroller.Pin.Input input;
        Microcontroller.Pin.Output output;

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
            pinPrinter.println("Pin.read()");
            if ( !outputDir ) {
                pinPrinter.println("  -> 1");
                if ( input != null ) return input.read();
                else return pullup;
            } else {
                pinPrinter.println("  -> 2 ("+level+")");
                return level;
            }
        }

        protected void write(boolean value) {
            pinPrinter.println("Pin.write("+value+")");
            level = value;
            if ( outputDir && output != null ) output.write(value);
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
    public ATMega128L() {
        simulator = null;
        pins = new Pin[NUM_PINS];
    }

    protected ATMega128L(Program p) {
        pins = new Pin[NUM_PINS];
        installPins();
        simulator = new SimImpl(p);
    }

    protected void installPins() {
        for ( int cntr = 0; cntr < NUM_PINS; cntr++ )
            pins[cntr] = new Pin();

        // TODO: install reserved pins like VCC
    }

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
     * The <code>getSimulator()</code> method gets a simulator instance that is
     * capable of emulating this hardware device.
     * @return a <code>Simulator</code> instance corresponding to this
     * device
     */
    public Simulator getSimulator() {
        return simulator;
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

    /**
     * The <code>getPinNumber()</code> method looks up the named pin and returns
     * its number. Names of pins should be UPPERCASE. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     * @param name the name of the pin; for example "PA0" or "OC1A"
     * @return the number of the pin if it exists; -1 otherwise
     */
    public int getPinNumber(String name) {
        Integer i = (Integer)pinNumbers.get(name);
        return i == null ? -1 : i.intValue();
    }

    /**
     * The <code>newMicrocontroller()</code> method is used to instantiate a
     * microcontroller instance for the particular program. It will construct
     * an instance of the <code>Simulator</code> class that has all the
     * properties of this hardware device and has been initialized with the
     * specified program.
     * @param p the program to load onto the microcontroller
     * @return a <code>Microcontroller</code> instance that represents the
     * specific hardware device with the program loaded onto it
     */
    public Microcontroller newMicrocontroller(Program p) {
        return new ATMega128L(p);
    }

    /**
     * The <code>getPin()</code> method looks up the specified pin by its number
     * and returns a reference to that pin. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     * @param num the pin number to look up
     * @return a reference to the <code>Pin</code> object corresponding to
     * the named pin if it exists; null otherwise
     */
    public Microcontroller.Pin getPin(int num) {
        if ( num < 0 || num > pins.length ) return null;
        return pins[num];
    }

    /**
     * The <code>getPin()</code> method looks up the named pin and returns a
     * reference to that pin. Names of pins should be UPPERCASE. The intended
     * users of this method are external device implementors which connect
     * their devices to the microcontroller through the pins.
     * @param name the name of the pin; for example "PA0" or "OC1A"
     * @return a reference to the <code>Pin</code> object corresponding to
     * the named pin if it exists; null otherwise
     */
    public Microcontroller.Pin getPin(String name) {
        return getPin(getPinNumber(name));
    }

    public class SimImpl extends Simulator {

        public SimImpl(Program p) {
            super(ATMega128L.this, p);
            populateState(getState());
        }

        public static final int RESET_VECT = 1;
        public static final int EXT_VECT = 2;

        protected FlagRegister EIFR_reg;
        protected MaskRegister EIMSK_reg;

        protected FlagRegister TIFR_reg;
        protected MaskRegister TIMSK_reg;

        protected class DirectionRegister extends State.RWIOReg {

            protected Pin[] pins;

            protected DirectionRegister(Pin[] p) {
                pins = p;
            }

            public void write(byte val) {
                for ( int cntr = 0; cntr < 8; cntr++ )
                    pins[cntr].setOutputDir(Arithmetic.getBit(val, cntr));
                value = val;
            }

            public void setBit(int bit) {
                pins[bit].setOutputDir(true);
                value = Arithmetic.setBit(value, bit);
            }

            public void clearBit(int bit) {
                pins[bit].setOutputDir(false);
                value = Arithmetic.clearBit(value, bit);
            }
        }

        protected class PortRegister extends State.RWIOReg {
            protected Pin[] pins;

            protected PortRegister(Pin[] p) {
                pins = p;
            }

            public void write(byte val) {
                for ( int cntr = 0; cntr < 8; cntr++ )
                    pins[cntr].write(Arithmetic.getBit(val, cntr));
                value = val;
            }

            public void setBit(int bit) {
                pins[bit].write(true);
                value = Arithmetic.setBit(value, bit);
            }

            public void clearBit(int bit) {
                pins[bit].write(false);
                value = Arithmetic.clearBit(value, bit);
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
                return (byte)value;
            }

            public boolean readBit(int bit) {
                return pins[bit].read();
            }

            public void write(byte val) {
                // ignore writes.
            }

            public void setBit(int bit) {
                // ignore writes.
            }

            public void clearBit(int bit) {
                // ignore writes.
            }
        }

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
            return new State(program, FLASH_SIZE, IOREG_SIZE, SRAM_SIZE);
        }

        private void populateState(State ns) {
            // set up the external interrupt mask and flag registers and interrupt range
            EIFR_reg = buildInterruptRange(ns, true, EIMSK, EIFR, 2, 8);
            EIMSK_reg = EIFR_reg.maskRegister;

            // set up the timer mask and flag registers and interrupt range
            TIFR_reg = buildInterruptRange(ns, false, TIMSK, TIFR, 17, 8);
            TIMSK_reg = TIFR_reg.maskRegister;

            // build timer 0
            new Timer0(ns);

            buildPorts(ns);
            // TODO: build other devices
        }


        private void buildPorts(State ns) {
            buildPort(ns, 'A', PORTA, DDRA, PINA);
            buildPort(ns, 'B', PORTB, DDRB, PINB);
            buildPort(ns, 'C', PORTC, DDRC, PINC);
            buildPort(ns, 'D', PORTD, DDRD, PIND);
            buildPort(ns, 'E', PORTE, DDRE, PINE);
            buildPort(ns, 'F', PORTF, DDRF, PINF);
        }

        private void buildPort(State ns, char p, int portreg, int dirreg, int pinreg) {
            Pin[] pins = new Pin[8];
            for ( int cntr = 0; cntr < 8; cntr++ )
                pins[cntr] = (Pin)getPin("P"+p+cntr);
            ns.setIOReg(portreg, new PortRegister(pins));
            ns.setIOReg(dirreg, new DirectionRegister(pins));
            ns.setIOReg(pinreg, new PinRegister(pins));
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
