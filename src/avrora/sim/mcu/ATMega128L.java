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

import avrora.core.InstrPrototype;
import avrora.core.Program;
import avrora.sim.*;
import avrora.sim.radio.Radio;
import avrora.util.Arithmetic;
import avrora.Avrora;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;


/**
 * The <code>ATMega128L</code> class represents the <code>Microcontroller</code> instance that has all the
 * hardware parameters of the ATMega128L microcontroller as produced by Atmel Corporatation.
 *
 * @author Ben L. Titzer
 */
public class ATMega128L extends ATMegaFamily implements IORegisterConstants, Microcontroller {

    public static final int ATMEGA128L_IOREG_SIZE = 256 - 32;
    public static final int ATMEGA128L_IOREG_SIZE_103 = 64;
    public static final int ATMEGA128L_SRAM_SIZE = 4096;
    public static final int ATMEGA128L_SRAM_SIZE_103 = 4000;

    public static final int ATMEGA128L_FLASH_SIZE = 128 * 1024;
    public static final int ATMEGA128L_EEPROM_SIZE = 4 * 1024;
    public static final int ATMEGA128L_NUM_PINS = 65;

    private final boolean compatibilityMode;

    //this class records energy consumption
    private Energy energy;
    //mode names of the mcu
    private final String modeName[] = {
        "Active:         ",
        "Idle:           ",
        "RESERVED 1:     ",
        "ADC Noise Red.: ",
        "RESERVED 2:     ",
        "Power Down:     ",
        "Standby:        ",
        "Power Save:     ",
        "Ext. Standby:   "
    };

    //power consumption of each mode
    private final double modeAmpere[] = {
        0.0075667,
        0.0033433,
        0.0,
        0.0009884,
        0.0,
        0.0001158,
        0.0002356,
        0.0001237,
        0.0002433
    };

    private final int startMode = 0;

    public static class Factory implements MicrocontrollerFactory {
        boolean compatibilityMode;

        /**
         * The constructor for the default instance.
         */
        public Factory(boolean compatibility) {
            compatibilityMode = compatibility;
        }

        /**
         * The <code>newMicrocontroller()</code> method is used to instantiate a microcontroller instance for the
         * particular program. It will construct an instance of the <code>Simulator</code> class that has all the
         * properties of this hardware device and has been initialized with the specified program.
         *
         * @param p the program to load onto the microcontroller
         * @return a <code>Microcontroller</code> instance that represents the specific hardware device with the
         *         program loaded onto it
         */
        public Microcontroller newMicrocontroller(int id, InterpreterFactory f, Program p) {
            return new ATMega128L(id, f, p, compatibilityMode);
        }

    }

    /**
     * The <code>props</code> field stores a static reference to a properties
     * object shared by all of the instances of this microcontroller. This object
     * stores the IO register size, SRAM size, pin assignments, etc.
     */
    public static final MicrocontrollerProperties props;
    public static final MicrocontrollerProperties compat_props;

    static {
        // statically initialize the pin assignments for this microcontroller
        HashMap pinAssignments = new HashMap();
        HashMap ioregAssignments = new HashMap();

        addPin(pinAssignments, 1, "PEN");
        addPin(pinAssignments, 2, "PE0", "RXD0", "PDI");
        addPin(pinAssignments, 3, "PE1", "TXD0", "PDO");
        addPin(pinAssignments, 4, "PE2", "XCK0", "AIN0");
        addPin(pinAssignments, 5, "PE3", "OC3A", "AIN1");
        addPin(pinAssignments, 6, "PE4", "OC3B", "INT4");
        addPin(pinAssignments, 7, "PE5", "OC3C", "INT5");
        addPin(pinAssignments, 8, "PE6", "T3", "INT6");
        addPin(pinAssignments, 9, "PE7", "IC3", "INT7");
        addPin(pinAssignments, 10, "PB0", "SS");
        addPin(pinAssignments, 11, "PB1", "SCK");
        addPin(pinAssignments, 12, "PB2", "MOSI");
        addPin(pinAssignments, 13, "PB3", "MISO");
        addPin(pinAssignments, 14, "PB4", "OC0");
        addPin(pinAssignments, 15, "PB5", "OC1A");
        addPin(pinAssignments, 16, "PB6", "OC1B");
        addPin(pinAssignments, 17, "PB7", "OC2", "OC1C");
        addPin(pinAssignments, 18, "PG3", "TOSC2");
        addPin(pinAssignments, 19, "PG4", "TOSC1");
        addPin(pinAssignments, 20, "RESET");
        addPin(pinAssignments, 21, "VCC");
        addPin(pinAssignments, 22, "GND");
        addPin(pinAssignments, 23, "XTAL2");
        addPin(pinAssignments, 24, "XTAL1");
        addPin(pinAssignments, 25, "PD0", "SCL", "INT0");
        addPin(pinAssignments, 26, "PD1", "SDA", "INT1");
        addPin(pinAssignments, 27, "PD2", "RXD1", "INT2");
        addPin(pinAssignments, 28, "PD3", "TXD1", "INT3");
        addPin(pinAssignments, 29, "PD4", "IC1");
        addPin(pinAssignments, 30, "PD5", "XCK1");
        addPin(pinAssignments, 31, "PD6", "T1");
        addPin(pinAssignments, 32, "PD7", "T2");
        addPin(pinAssignments, 33, "PG0", "WR");
        addPin(pinAssignments, 34, "PG1", "RD");
        addPin(pinAssignments, 35, "PC0", "A8");
        addPin(pinAssignments, 36, "PC1", "A9");
        addPin(pinAssignments, 37, "PC2", "A10");
        addPin(pinAssignments, 38, "PC3", "A11");
        addPin(pinAssignments, 39, "PC4", "A12");
        addPin(pinAssignments, 40, "PC5", "A13");
        addPin(pinAssignments, 41, "PC6", "A14");
        addPin(pinAssignments, 42, "PC7", "A15");
        addPin(pinAssignments, 43, "PG2", "ALE");
        addPin(pinAssignments, 44, "PA7", "AD7");
        addPin(pinAssignments, 45, "PA6", "AD5");
        addPin(pinAssignments, 46, "PA5", "AD5");
        addPin(pinAssignments, 47, "PA4", "AD4");
        addPin(pinAssignments, 48, "PA3", "AD3");
        addPin(pinAssignments, 49, "PA2", "AD2");
        addPin(pinAssignments, 50, "PA1", "AD1");
        addPin(pinAssignments, 51, "PA0", "AD0");
        addPin(pinAssignments, 52, "VCC.b");
        addPin(pinAssignments, 53, "GND.b");
        addPin(pinAssignments, 54, "PF7", "ADC7", "TDI");
        addPin(pinAssignments, 55, "PF6", "ADC6", "TDO");
        addPin(pinAssignments, 56, "PF5", "ADC5", "TMS");
        addPin(pinAssignments, 57, "PF4", "ADC4", "TCK");
        addPin(pinAssignments, 58, "PF3", "ADC3");
        addPin(pinAssignments, 59, "PF2", "ADC2");
        addPin(pinAssignments, 60, "PF1", "ADC1");
        addPin(pinAssignments, 61, "PF0", "ADC0");
        addPin(pinAssignments, 62, "AREF");
        addPin(pinAssignments, 63, "GND.c");
        addPin(pinAssignments, 64, "AVCC");

        // extended IO registers
        // TODO: should these be be 0x20 less?
        addIOReg(ioregAssignments, "UCSR1C", 0x9D);
        addIOReg(ioregAssignments, "UDR1", 0x9C);
        addIOReg(ioregAssignments, "UCSR1A", 0x9B);
        addIOReg(ioregAssignments, "UCSR1B", 0x9A);
        addIOReg(ioregAssignments, "UBRR1L", 0x99);
        addIOReg(ioregAssignments, "UBRR1H", 0x98);

        addIOReg(ioregAssignments, "UCSR0C", 0x95);

        addIOReg(ioregAssignments, "UBRR0H", 0x90);

        addIOReg(ioregAssignments, "TCCR3C", 0x8C);
        addIOReg(ioregAssignments, "TCCR3A", 0x8B);
        addIOReg(ioregAssignments, "TCCR3B", 0x8A);
        addIOReg(ioregAssignments, "TCNT3H", 0x89);
        addIOReg(ioregAssignments, "TCNT3L", 0x88);
        addIOReg(ioregAssignments, "OCR3AH", 0x87);
        addIOReg(ioregAssignments, "OCR3AL", 0x86);
        addIOReg(ioregAssignments, "OCR3BH", 0x85);
        addIOReg(ioregAssignments, "OCR3BL", 0x84);
        addIOReg(ioregAssignments, "OCR3CH", 0x83);
        addIOReg(ioregAssignments, "OCR3CL", 0x82);
        addIOReg(ioregAssignments, "ICR3H", 0x81);
        addIOReg(ioregAssignments, "ICR3L", 0x80);

        addIOReg(ioregAssignments, "ETIMSK", 0x7D);
        addIOReg(ioregAssignments, "ETIFR", 0x7C);

        addIOReg(ioregAssignments, "TCCR1C", 0x7A);
        addIOReg(ioregAssignments, "OCR1CH", 0x79);
        addIOReg(ioregAssignments, "OCR1CL", 0x78);

        addIOReg(ioregAssignments, "TWCR", 0x74);
        addIOReg(ioregAssignments, "TWDR", 0x73);
        addIOReg(ioregAssignments, "TWAR", 0x72);
        addIOReg(ioregAssignments, "TWSR", 0x71);
        addIOReg(ioregAssignments, "TWBR", 0x70);
        addIOReg(ioregAssignments, "OSCCAL", 0x6F);

        addIOReg(ioregAssignments, "XMCRA", 0x6D);
        addIOReg(ioregAssignments, "XMCRB", 0x6C);

        addIOReg(ioregAssignments, "EICRA", 0x6A);

        addIOReg(ioregAssignments, "SPMCSR", 0x68);

        addIOReg(ioregAssignments, "PORTG", 0x65);
        addIOReg(ioregAssignments, "DDRG", 0x64);
        addIOReg(ioregAssignments, "PING", 0x63);
        addIOReg(ioregAssignments, "PORTF", 0x62);
        addIOReg(ioregAssignments, "DDRF", 0x61);

        // lower 64 IO registers
        addIOReg(ioregAssignments, "SREG", 0x3F);
        addIOReg(ioregAssignments, "SPH", 0x3E);
        addIOReg(ioregAssignments, "SPL", 0x3D);
        addIOReg(ioregAssignments, "XDIV", 0x3C);
        addIOReg(ioregAssignments, "RAMPZ", 0x3B);
        addIOReg(ioregAssignments, "EICRB", 0x3A);
        addIOReg(ioregAssignments, "EIMSK", 0x39);
        addIOReg(ioregAssignments, "EIFR", 0x38);
        addIOReg(ioregAssignments, "TIMSK", 0x37);
        addIOReg(ioregAssignments, "TIFR", 0x36);
        addIOReg(ioregAssignments, "MCUCR", 0x35);
        addIOReg(ioregAssignments, "MCUCSR", 0x34);
        addIOReg(ioregAssignments, "TCCR0", 0x33);
        addIOReg(ioregAssignments, "TCNT0", 0x32);
        addIOReg(ioregAssignments, "OCR0", 0x31);
        addIOReg(ioregAssignments, "ASSR", 0x30);
        addIOReg(ioregAssignments, "TCCR1A", 0x2F);
        addIOReg(ioregAssignments, "TCCR1B", 0x2E);
        addIOReg(ioregAssignments, "TCNT1H", 0x2D);
        addIOReg(ioregAssignments, "TCNT1L", 0x2C);
        addIOReg(ioregAssignments, "OCR1AH", 0x2B);
        addIOReg(ioregAssignments, "OCR1AL", 0x2A);
        addIOReg(ioregAssignments, "OCR1BH", 0x29);
        addIOReg(ioregAssignments, "OCR1BL", 0x28);
        addIOReg(ioregAssignments, "ICR1H", 0x27);
        addIOReg(ioregAssignments, "ICR1L", 0x26);
        addIOReg(ioregAssignments, "TCCR2", 0x25);
        addIOReg(ioregAssignments, "TCNT2", 0x24);
        addIOReg(ioregAssignments, "OCR2", 0x23);
        addIOReg(ioregAssignments, "OCDR", 0x22);
        addIOReg(ioregAssignments, "WDTCR", 0x21);
        addIOReg(ioregAssignments, "SFIOR", 0x20);
        addIOReg(ioregAssignments, "EEARH", 0x1F);
        addIOReg(ioregAssignments, "EEARL", 0x1E);
        addIOReg(ioregAssignments, "EEDR", 0x1D);
        addIOReg(ioregAssignments, "EECR", 0x1C);
        addIOReg(ioregAssignments, "PORTA", 0x1B);
        addIOReg(ioregAssignments, "DDRA", 0x1A);
        addIOReg(ioregAssignments, "PINA", 0x19);
        addIOReg(ioregAssignments, "PORTB", 0x18);
        addIOReg(ioregAssignments, "DDRB", 0x17);
        addIOReg(ioregAssignments, "PINB", 0x16);
        addIOReg(ioregAssignments, "PORTC", 0x15);
        addIOReg(ioregAssignments, "DDRC", 0x14);
        addIOReg(ioregAssignments, "PINC", 0x13);
        addIOReg(ioregAssignments, "PORTD", 0x12);
        addIOReg(ioregAssignments, "DDRD", 0x11);
        addIOReg(ioregAssignments, "PIND", 0x10);
        addIOReg(ioregAssignments, "SPDR", 0x0F);
        addIOReg(ioregAssignments, "SPSR", 0x0E);
        addIOReg(ioregAssignments, "SPCR", 0x0D);
        addIOReg(ioregAssignments, "UDR0", 0x0C);
        addIOReg(ioregAssignments, "UCSR0A", 0x0B);
        addIOReg(ioregAssignments, "UCSR0B", 0x0A);
        addIOReg(ioregAssignments, "UBRR0L", 0x09);
        addIOReg(ioregAssignments, "ACSR", 0x08);
        addIOReg(ioregAssignments, "ADMUX", 0x07);
        addIOReg(ioregAssignments, "ADCSRA", 0x06);
        addIOReg(ioregAssignments, "ADCH", 0x05);
        addIOReg(ioregAssignments, "ADCL", 0x04);
        addIOReg(ioregAssignments, "PORTE", 0x03);
        addIOReg(ioregAssignments, "DDRE", 0x02);
        addIOReg(ioregAssignments, "PINE", 0x01);
        addIOReg(ioregAssignments, "PINF", 0x00);

        props = new MicrocontrollerProperties(ATMEGA128L_IOREG_SIZE, // number of io registers
                ATMEGA128L_SRAM_SIZE, // size of sram in bytes
                ATMEGA128L_FLASH_SIZE, // size of flash in bytes
                ATMEGA128L_EEPROM_SIZE, // size of eeprom in bytes
                ATMEGA128L_NUM_PINS, // number of pins
                pinAssignments, // the assignment of names to physical pins
                ioregAssignments); // the assignment of names to IO registers

        compat_props = new MicrocontrollerProperties(ATMEGA128L_IOREG_SIZE_103, // number of io registers
                ATMEGA128L_SRAM_SIZE_103, // size of sram in bytes
                ATMEGA128L_FLASH_SIZE, // size of flash in bytes
                ATMEGA128L_EEPROM_SIZE, // size of eeprom in bytes
                ATMEGA128L_NUM_PINS, // number of pins
                pinAssignments, // the assignment of names to physical pins
                ioregAssignments); // the assignment of names to IO registers

    }

    public ATMega128L(int id, InterpreterFactory f, Program p, boolean compatibility) {
        super(7372800, compatibility ? compat_props : props);
        compatibilityMode = compatibility;
        installPins();
        simulator = new SimImpl(id, f, p);
        clock = simulator.getClock();
        interpreter = simulator.getInterpreter();
        ((SimImpl)simulator).populateState();
        //init the energy profiling system for the CPU
        energy = new Energy("CPU",
                modeAmpere, modeName,
                this.getHz(), startMode,
                this.getSimulator().getEnergyControl(),
                this.getSimulator().getState());
    }


    protected void installPins() {
        for (int cntr = 0; cntr < properties.num_pins; cntr++)
            pins[cntr] = new ATMegaFamily.Pin(cntr);
    }

    /**
     * The <code>isSupported()</code> method allows a client to query whether a particular instruction is
     * implemented on this hardware device. Older implementations of the AVR instruction set preceded the
     * introduction of certain instructions, and therefore did not support the new instructions.
     *
     * @param i the instruction prototype of the instruction
     * @return true if the specified instruction is supported on this device; false otherwise
     */
    public boolean isSupported(InstrPrototype i) {
        return true;
    }

    protected SimImpl.SPI spi;
    protected SimImpl.ADC adc;
    protected SimImpl.PowerManagement pm;
    protected SimImpl.USART0 usart0;

    // TODO: move all devices out of SimImpl and remove
    public class SimImpl extends Simulator {

        public SimImpl(int id, InterpreterFactory f, Program p) {
            super(id, f, ATMega128L.this, p);
        }

        protected FlagRegister EIFR_reg;
        protected MaskRegister EIMSK_reg;

        protected FlagRegister TIFR_reg;
        protected MaskRegister TIMSK_reg;

        protected FlagRegister ETIFR_reg;
        protected MaskRegister ETIMSK_reg;

        protected class StandardClock implements Clock {
            public void scheduleEvent(Simulator.Event e, long cycles) {
                insertEvent(e, cycles);
            }
        }

        protected class Timer0ExternalClock extends StandardClock {
            long delay;

            Timer0ExternalClock() {
                long Hz = 32768; // Cycles per second
                double secondsPerCycle = 1.0 / Hz;
                double millisPerCycle = secondsPerCycle * 1000.0;
                delay = millisToCycles(millisPerCycle);
            }

            public void scheduleEvent(Simulator.Event e, long cycles) {
                insertEvent(e, cycles * delay);
            }

        }

        final int[] periods0 = {0, 1, 8, 32, 64, 128, 256, 1024};

        /**
         * <code>Timer0</code> is the default 8-bit timer on the ATMega128L.
         */
        protected class Timer0 extends Timer8Bit {

            final Timer0ExternalClock externalClock = new Timer0ExternalClock();

            protected Timer0(BaseInterpreter ns) {
                super(ns, 0, TCCR0, TCNT0, OCR0, 1, 0, 1, 0, periods0);
                installIOReg(ns, ASSR, new ASSRRegister());
            }

            // See pg. 104 of the ATmega128L doc
            protected class ASSRRegister extends State.RWIOReg {
                final int AS0 = 3;
                final int TCN0UB = 2;
                final int OCR0UB = 1;
                final int TCR0UB = 0;

                public void write(byte val) {
                    super.write((byte)(0xf & val));
                    decode(val);
                }

                public void writeBit(int bit, boolean val) {
                    super.writeBit(bit, val);
                    decode(value);
                }

                protected void decode(byte val) {
                    // TODO: if there is a change, remove ticker and requeue?
                    clock = Arithmetic.getBit(val, AS0) ? externalClock : standardClock;
                }


            }


        }

        final int[] periods2 = {0, 1, 8, 64, 256, 1024};

        /**
         * <code>Timer2</code> is an additional 8-bit timer on the ATMega128L. It is not available in
         * ATMega103 compatibility mode.
         */
        protected class Timer2 extends Timer8Bit {
            protected Timer2(BaseInterpreter ns) {
                super(ns, 2, TCCR2, TCNT2, OCR2, 7, 6, 7, 6, periods2);
            }
        }


        /**
         * <code>Timer1</code> is a 16-bit timer available on the ATMega128L.
         */
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

        /**
         * <code>Timer3</code> is an additional 16-bit timer available on the ATMega128L, but not in ATMega103
         * compatability mode.
         */
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
         * The <code>Timer16Bit</code> class emulates the functionality and behavior of a 16-bit timer on the
         * Atmega128L. It has several control and data registers and can fire up to six different interrupts
         * depending on the mode that it has been put into. It has three output compare units and one input
         * capture unit. UNIMPLEMENTED: input capture unit.
         *
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

            boolean blockCompareMatch;

            Simulator.Printer timerPrinter;

            // information about registers and flags that specifies
            // which specific registers this 16-bit timer interacts with

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

            protected FlagRegister xTIFR_reg;
            protected MaskRegister xTIMSK_reg;

            protected int[] periods;

            // This method should be overloaded to initialize the above values.
            abstract protected void initValues();

            private Timer16Bit(BaseInterpreter ns) {
                initValues();

                timerPrinter = simulator.getPrinter("atmega.timer" + n);
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

                installIOReg(ns, TCNTnH, highTempReg);
                installIOReg(ns, TCNTnL, TCNTn_reg);

                installIOReg(ns, OCRnAH, new OCRnxTempHighRegister(OCRnAH_reg));
                installIOReg(ns, OCRnAL, OCRnA_reg);

                installIOReg(ns, OCRnBH, new OCRnxTempHighRegister(OCRnBH_reg));
                installIOReg(ns, OCRnBL, OCRnB_reg);

                installIOReg(ns, OCRnCH, new OCRnxTempHighRegister(OCRnCH_reg));
                installIOReg(ns, OCRnCL, OCRnC_reg);

                installIOReg(ns, ICRnH, highTempReg);
                installIOReg(ns, ICRnL, ICRn_reg);
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

                abstract protected void decode(byte val);


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
                    ATMegaFamily.Pin pin = ((ATMegaFamily.Pin)getPin("OC" + n + s));
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


                    if (period != 0) insertEvent(this, period);
                }
            }
        }


        /**
         * Base class of 8-bit timers. Timer0 and Timer2 are subclasses of this.
         *
         * @author Daniel Lee
         */
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

            protected final StandardClock standardClock = new StandardClock();

            protected Clock clock = standardClock;

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

            Simulator.Printer timerPrinter;

            /**
             * Timer8Bit(ns, TCNT0, TCNT0, OCR0) should initialize Timer0 as before. Assuming this translation
             * from the Timer0 code was generic enough, Timer8Bit(ns, TCNT2, TCNT2, OCR2) should initialize a
             * mostly functional Timer2. OCRn is the offset on TIMSK that corresponds to
             */
            private Timer8Bit(BaseInterpreter ns, int n, int TCCRn, int TCNTn, int OCRn, int OCIEn, int TOIEn, int OCFn, int TOVn, int[] periods) {
                timerPrinter = simulator.getPrinter("atmega.timer" + n);
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
                    timerPrinter.println("Timer" + n + ".compareMatch (enabled: " + enabled + ')');
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
                    timerPrinter.println("Timer" + n + ".overFlow (enabled: " + enabled + ')');
                }
                // set the overflow flag for this timer
                TIFR_reg.flagBit(TOVn);
            }

            /**
             * Overloads the write behavior of this class of register in order to implement compare match
             * blocking for one timer period.
             */
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

            /**
             * <code>BufferedRegister</code> implements a register with a write buffer. In PWN modes, writes
             * to this register are not performed until flush() is called. In non-PWM modes, the writes are
             * immediate.
             */
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

                    ATMegaFamily.Pin pin = (ATMegaFamily.Pin)getPin("OC" + n);

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
                    clock.scheduleEvent(ticker, period);
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
                    if (timerPrinter.enabled)
                        timerPrinter.println("Timer" + n + " [TCNT" + n + " = " + count + ", OCR" + n + "(actual) = " + compare + ", OCR" + n + "(buffer) = " + (0xff & OCRn_reg.readBuffer()) + ']');

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
                    TCNTn_reg.write((byte)count);
                    // I probably want to verify the timing on this.
                    blockCompareMatch = false;

                    if (period != 0) clock.scheduleEvent(this, period);
                }
            }
        }


        /**
         * Implementation of MCU Power Managment, e.g. MCUCR register
         *
         * @author Olaf Landsiedel
         */
        protected class PowerManagement {
            // the MCUCU register
            private final ControlRegister MCUCR_reg;
            // sleep enable bit
            private byte sleepEnableChip = 0;
            // sleep mode bits
            private byte sleepModeChip = 0;

            // current mode, default: active
            private byte mode = -1;

            // constants for modes
            public static final byte ACTIVE = -1;
            public static final byte IDLE = 0;
            public static final byte RESERVED1 = 1;
            public static final byte ADC_NOISE = 2;
            public static final byte RESERVED2 = 3;
            public static final byte POWER_DOWN = 4;
            public static final byte STANDBY = 5;
            public static final byte POWER_SAVE = 6;
            public static final byte EXTENDED_STANDBY = 7;

            //actually for power down and power save, the wakeup time is 1000 cycles
            //for mica2 fuse setup
            private final int[] wakeupCycles = {0, 0, 0, 0, 1000, 6, 1000, 6};
            //sleep mode names
            public final String[] sleepModeName = {"Idle", "ERROR: RESERVED",
                                                   "ADC Noise Reduction", "ERROR: RESERVED", "Power Down",
                                                   "Standby", "Power Save", "Extended Standby"};

            // active mode name ;-)
            public static final String activeModeName = "Active";

            /**
             * create a new Power Management
             *
             * @param ns base interpreter
             */
            PowerManagement(BaseInterpreter ns) {
                MCUCR_reg = new ControlRegister();
                installIOReg(ns, MCUCR, MCUCR_reg);
            }

            /**
             * called when the sleep opcode gets executed
             */
            protected void sleep() {
                // goto sleep when sleep enable is set
                // e.g. change to the mode according to the sleep mode bits
                if (sleepEnableChip == 1) {
                    mode = sleepModeChip;
                } else {
                    //else, only idle mode is possible
                    mode = IDLE;
                }
                // update the energy control and attached loggers
                energy.setMode(mode + 1);
				//Terminal.print(simulator.getState().getCycles() + ": " );
				//Terminal.println("sleep start, mode: " + getModeName() + ", " + mode);
				return;
            }

            /**
             * called, when the system wakes up commonly the system wakes up via interrupt ;-) Do not call
             * this method, when the system is not sleeping. Such a check is not done, to provide high
             * performace.
             *
             * @return the cycles it takes to wake up
             */
            protected int wakeup() {
                byte temp = mode;
                mode = ACTIVE;
                // update the energy control
                energy.setMode(0);
                //Terminal.print(simulator.getState().getCycles() + ": " );
                //Terminal.println("sleep end");
                return wakeupCycles[temp];

            }

            /**
             * get the mode, the system is in
             *
             * @return mode
             */
            protected byte getMode() {
                return mode;
            }

            /**
             * get the name of the current mode
             *
             * @return mode name
             */
            protected String getModeName() {
                if (mode >= 0)
                    return sleepModeName[mode];
                return activeModeName;
            }

            /**
             * implementation of the MCUCR control register
             *
             * @author Olaf Landsiedel
             */
            protected class ControlRegister extends State.RWIOReg {
                public static final byte SRE = 7;
                public static final byte SRW10 = 6;
                public static final byte SE = 5;
                public static final byte SM1 = 4;
                public static final byte SM0 = 3;
                public static final byte SM2 = 2;
                public static final byte IVSEL = 1;
                public static final byte IVCE = 0;

                /**
                 * write byte to the register
                 *
                 * @param val value to write
                 * @see avrora.sim.State.IOReg#write(byte)
                 */
                public void write(byte val) {
                    // decode modes and update internal state
                    decode(val);
                }

                /**
                 * write bit to the register
                 *
                 * @param bit bit to write on
                 * @param val value to write
                 * @see avrora.sim.State.IOReg#writeBit(int, boolean)
                 */
                public void writeBit(int bit, boolean val) {
                    value = Arithmetic.setBit(value, bit, val);
                    decode(value);
                }

                /**
                 * interprete the data written to the register
                 *
                 * @param val data
                 */
                private void decode(byte val) {
                    // set the modes of operation
                    value = val;
                    sleepEnableChip = (byte)(Arithmetic.getBit(val, SE) ? 1 : 0);
                    sleepModeChip = (byte)(Arithmetic.getBit(val, SM1) ? 4 : 0);
                    sleepModeChip |= (byte)(Arithmetic.getBit(val, SM0) ? 2 : 0);
                    sleepModeChip |= (byte)(Arithmetic.getBit(val, SM2) ? 1 : 0);
                    //if( sleepModeChip == 1 || sleepModeChip == 3 ){
                    //    Terminal.print(simulator.getState().getCycles() + ": " );
                    //    Terminal.println("applications put mcu in undefinded sleepmode ");
                    //}
                    //Terminal.print(simulator.getState().getCycles() + ": " );
                    //Terminal.println("sleepEnable: " + sleepEnableChip + " sleepMode: " + sleepModeName[sleepModeChip]+ ", "+ sleepModeChip);
                }
            }
        }


        private void populateState() {
            // set up the external interrupt mask and flag registers and interrupt range
            EIFR_reg = buildInterruptRange(true, EIMSK, EIFR, 2, 8);
            EIMSK_reg = EIFR_reg.maskRegister;

            // set up the timer mask and flag registers and interrupt range
            TIFR_reg = buildInterruptRange(false, TIMSK, TIFR, 17, 8);
            TIMSK_reg = TIFR_reg.maskRegister;


            /* For whatever reason, the ATMega128 engineers decided
               against having the bitorder for the ETIFR/ETIMSK
               registers line up with the corresponding block of the
               interrupt vector. Therefore, we have to line this up by
               hand.
            */

            if (!compatibilityMode) {
                int[] ETIFR_mapping = {25, 29, 30, 28, 27, 26, -1, -1};
                ETIFR_reg = new FlagRegister(interpreter, ETIFR_mapping); // false, 0 are just placeholder falues
                ETIMSK_reg = ETIFR_reg.maskRegister;
                installIOReg(interpreter, ETIMSK, ETIMSK_reg);
                installIOReg(interpreter, ETIFR, ETIFR_reg);
            }


            // Timer1 COMPC
            interrupts[25] = new MaskableInterrupt(25, ETIMSK_reg, ETIFR_reg, 0, false);
            // Timer3 CAPT
            interrupts[26] = new MaskableInterrupt(26, ETIMSK_reg, ETIFR_reg, 5, false);
            // Timer3 COMPA
            interrupts[27] = new MaskableInterrupt(27, ETIMSK_reg, ETIFR_reg, 4, false);
            // Timer3 COMPB
            interrupts[28] = new MaskableInterrupt(28, ETIMSK_reg, ETIFR_reg, 3, false);
            // Timer3 COMPC
            interrupts[29] = new MaskableInterrupt(29, ETIMSK_reg, ETIFR_reg, 1, false);
            // Timer3 OVF
            interrupts[30] = new MaskableInterrupt(30, ETIMSK_reg, ETIFR_reg, 2, false);

            new Timer0(interpreter);
            if (!compatibilityMode) new Timer2(interpreter);

            new Timer1(interpreter);
            if (!compatibilityMode) new Timer3(interpreter);

            buildPorts(interpreter);

            new EEPROM(interpreter);
            usart0 = new USART0(interpreter);
            if (!compatibilityMode) new USART1(interpreter);

            spi = new SPI(interpreter);
            adc = new ADC(interpreter);
            pm = new PowerManagement(interpreter);
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
            ATMegaFamily.Pin[] pins = new ATMegaFamily.Pin[8];
            for (int cntr = 0; cntr < 8; cntr++)
                pins[cntr] = (ATMegaFamily.Pin)getPin("P" + p + cntr);
            installIOReg(ns, portreg, new PortRegister(pins));
            installIOReg(ns, dirreg, new DirectionRegister(pins));
            installIOReg(ns, pinreg, new PinRegister(pins));
        }

        private void installIOReg(BaseInterpreter ns, int num, State.IOReg ior) {
            // in compatbility mode, the upper IO registers do not exist.
            if (compatibilityMode && num > ATMEGA128L_IOREG_SIZE_103) return;
            ns.setIOReg(num, ior);
        }

        private FlagRegister buildInterruptRange(boolean increasing, int maskRegNum, int flagRegNum, int baseVect, int numVects) {
            int[] mapping = new int[8];
            if (increasing) {
                for (int cntr = 0; cntr < 8; cntr++) mapping[cntr] = baseVect + cntr;
            } else {
                for (int cntr = 0; cntr < 8; cntr++) mapping[cntr] = baseVect - cntr;
            }

            FlagRegister fr = new FlagRegister(interpreter, mapping);
            for (int cntr = 0; cntr < numVects; cntr++) {
                int inum = increasing ? baseVect + cntr : baseVect - cntr;
                interrupts[inum] = new MaskableInterrupt(inum, fr.maskRegister, fr, cntr, false);
                installIOReg(interpreter, maskRegNum, fr.maskRegister);
                installIOReg(interpreter, flagRegNum, fr);
            }
            return fr;
        }

        /**
         * This is an implementation of the non-volatile EEPROM on the ATMega128 microcontroller.
         *
         * @author Daniel Lee
         */
        protected class EEPROM {

            // TODO: CPU halting after EEPROM read/write reads/writes.
            final byte[] EEPROM_data = new byte[properties.eeprom_size];
            final State.RWIOReg EEDR_reg;
            final EECRReg EECR_reg;
            final State.RWIOReg EEARL_reg;
            final EEARHReg EEARH_reg;

            final BaseInterpreter interpreter;

            final Simulator.Printer eepromPrinter;

            // flag bits on EECR
            final int EERIE = 3;
            final int EEMWE = 2;
            final int EEWE = 1;
            final int EERE = 0;

            final int EEPROM_INTERRUPT = 23;

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
                eepromPrinter = simulator.getPrinter("atmega.eeprom");
                interpreter = ns;

                ticker = new EEPROMTicker();

                EEDR_reg = new State.RWIOReg();
                EECR_reg = new EECRReg();
                EEARL_reg = new State.RWIOReg();
                EEARH_reg = new EEARHReg();

                installIOReg(ns, EEDR, EEDR_reg);
                installIOReg(ns, EECR, EECR_reg);
                installIOReg(ns, EEARL, EEARL_reg);
                installIOReg(ns, EEARH, EEARH_reg);

            }

            protected class EEARHReg extends State.RWIOReg {
                public void write(byte val) {
                    value = (byte)(0xff & val);
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
                    readEnable = readBit(EERE);
                    if (!readEnableOld && readEnable) {
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: EERE flagged");
                        readEnableWritten = true;
                    }
                    boolean writeEnableOld = writeEnable;
                    writeEnable = readBit(EEWE);
                    if (!writeEnableOld && writeEnable) {
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: EEWE flagged");
                        writeEnableWritten = true;
                    }
                    masterWriteEnable = readBit(EEMWE);
                    interruptEnable = readBit(EERIE);
                    if (!interruptEnable) {
                        interpreter.unpostInterrupt(EEPROM_INTERRUPT);
                    }


                    if ((interruptEnable && !writeEnable)) {
                        // post interrupt
                        if (eepromPrinter.enabled) eepromPrinter.println("EEPROM: posting interrupt.");
                        interpreter.postInterrupt(EEPROM_INTERRUPT);
                    }
                    insertEvent(ticker, 1);
                }

                public void write(byte val) {

                    boolean masterWriteEnableOld = masterWriteEnable;
                    value = (byte)(0xff & val);
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

                    if (eepromPrinter.enabled) {
                        eepromPrinter.println("Tick : " + writeCount);
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


        /**
         * Emulates the behavior of USART0 on the ATMega128L microcontroller.
         */
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

                int[] UCSR0A_mapping = {-1, -1, -1, -1, -1, 20, 21, 19};
                INTERRUPT_MAPPING = UCSR0A_mapping;

            }
        }

        /**
         * Emulates the behavior of USART1 on the ATMega128L microcontroller.
         */
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

                // TODO: test these interrupt mappings--not sure if they are correct!
                int[] UCSR1A_mapping = {-1, -1, -1, -1, -1, 32, 33, 31};
                INTERRUPT_MAPPING = UCSR1A_mapping;
            }
        }

        /**
         * The USART class implements a Universal Synchronous Asynchronous Receiver/Transmitter, which is a
         * serial device on the ATMega128L. The ATMega128L has two USARTs, USART0 and USART1.
         *
         * @author Daniel Lee
         */
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

            final Simulator.Printer usartPrinter;

            public USARTDevice connectedDevice;

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

            // Frame sizes
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

            /**
             * Initialize the parameters such as interrupt numbers and I/O register numbers that make this
             * USART unique.
             */
            abstract protected void initValues();

            USART(BaseInterpreter ns) {
                initValues();
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
                //connectedDevice = new LCDScreen();

                usartPrinter = simulator.getPrinter("atmega.usart" + n);

                installIOReg(ns, UDRn, UDRn_reg);
                installIOReg(ns, UCSRnA, UCSRnA_reg);
                installIOReg(ns, UCSRnB, UCSRnB_reg);
                installIOReg(ns, UCSRnC, UCSRnC_reg);
                installIOReg(ns, UBRRnL, UBRRnL_reg);
                installIOReg(ns, UBRRnH, UBRRnH_reg);

                // USART Receive Complete
                interrupts[USARTnRX] = new MaskableInterrupt(USARTnRX, UCSRnB_reg, UCSRnA_reg, RXCn, false);
                // USART Data Register Empty
                interrupts[USARTnUDRE] = new MaskableInterrupt(USARTnUDRE, UCSRnB_reg, UCSRnA_reg, UDREn, false);
                // USART Transmit Complete
                interrupts[USARTnTX] = new MaskableInterrupt(USARTnTX, UCSRnB_reg, UCSRnA_reg, TXCn, false);

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

            /**
             * Initiate a receive between the UART and the connected device.
             */
            public void startReceive() {
                receiver.enableReceive();
            }

            protected class Receiver {

                boolean receiving = false;
                Receive receive = new Receive();

                protected void enableReceive() {
                    if (!receiving) {
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
                            usartPrinter.println("USART: Received frame " + frame + ' ' + simulator.getState().getCycles() + ' ' + UBRRnH_reg.read() + ' ' + UBRRnL_reg.read() + ' ' + UBRRMultiplier + ' ');

                        UCSRnA_reg.flagBit(RXCn);

                        receiving = false;
                    }
                }
            }

            /**
             * The <code>DataRegister</code> class represents a Transmit Data Buffer Register for a USART. It
             * is really two registers, a transmit register and a receive register. The transmit register is
             * the destination of data written to the register at this address. The receive register is the
             * source of data read from this address.
             */
            protected class DataRegister extends State.RWIOReg {
                State.RWIOReg transmitRegister;
                TwoLevelFIFO receiveRegister;

                DataRegister() {
                    transmitRegister = new State.RWIOReg();
                    receiveRegister = new TwoLevelFIFO();
                }

                public void write(byte val) {
                    // check UDREn flag

                    if (UCSRnA_reg.readBit(UDREn)) {
                        transmitRegister.write(val);
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
                    UCSRnA_reg.writeBit(RXCn,true);
                    return receiveRegister.read();
                }

                public boolean readBit(int bit) {
                    return receiveRegister.readBit(bit);
                }


                /**
                 * An implementation of the FIFO used to buffer the received frames. This is not quite a
                 * two-level FIFO, as the shift-receive register in the actual implementation can act as a
                 * third level to the buffer. In order to account for this, the FIFO is implemented as a queue
                 * that can hold at most three elements (limited by the implementation). Although the
                 * implementation does not mirror the how the hardware does this, functionally it should
                 * behave the same way.
                 */
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
                            return (byte)0;
                        }
                        USARTFrameWrapper current = (USARTFrameWrapper)readyQueue.removeLast();
                        if (readyQueue.isEmpty()) {
                            UCSRnA_reg.writeBit(RXCn, false);
                        }
                        UCSRnB_reg.writeBit(RXB8n, current.frame.high);
                        waitQueue.add(current);
                        return current.frame.low;
                    }

                    public void writeFrame(USARTFrame frame) {
                        if (waitQueue.isEmpty()) {
                            // data overrun. drop frame
                            UCSRnA_reg.writeBit(DORn, true);
                        } else {
                            USARTFrameWrapper current = (USARTFrameWrapper)(waitQueue.removeLast());
                            current.frame = frame;
                            readyQueue.addFirst(current);
                        }
                    }

                    protected void flush() {
                        while (!waitQueue.isEmpty()) {
                            // empty the wait queue. fill the ready queue.
                            readyQueue.add(waitQueue.removeLast());
                        }
                    }

                    private class USARTFrameWrapper {
                        USARTFrame frame;
                    }

                }

            }


            /**
             * UCSRnA (<code>ControlRegisterA</code>) is one of three control/status registers for the USART.
             * The high three bits are actually interrupt flag bits.
             */
            protected class ControlRegisterA extends FlagRegister {

                public ControlRegisterA() {
                    super(ATMega128L.this.interpreter, INTERRUPT_MAPPING);
                    value = 0x20; // init UDREn to true

                }

                public void write(byte val) {
                    super.write((byte)(0xe3 & val));
                    decode(val);

                }

                public void writeBit(int bit, boolean val) {

                    if( bit == 7){
                        //OL: just unpost the int, do not clear RCXn
                        //thus, we cannot use the super call
                        interpreter.unpostInterrupt(getVectorNum(bit));
                    } else if (bit < 1 || bit > 4) {
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

            /**
             * UCSRnB (<code>ControlRegisterB</code>) is one of three control/status registers for the USART.
             * The high three bits are actually interrupt mask bits.
             */
            protected class ControlRegisterB extends MaskRegister {
                int count = 0;

                ControlRegisterB() {
                    super(ATMega128L.this.interpreter, INTERRUPT_MAPPING, UCSRnA_reg);
                    UCSRnA_reg.maskRegister = this;
                }

                public void write(byte val) {
                    super.write(val);
                    decode(val);
                }

                public void writeBit(int bit, boolean val) {
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


            /**
             * UCSRnC (<code>ControlRegisterC</code>) is one of three control/status registers for the USART.
             */
            protected class ControlRegisterC extends State.RWIOReg {

                protected void decode(byte val) {

                    stopBits = readBit(USBSn) ? 2 : 1;

                    int UCSZVal = UCSRnB_reg.readBit(UCSZn2) ? 0x4 : 0x0;
                    UCSZVal |= readBit(UCSZn1) ? 0x2 : 0x0;
                    UCSZVal |= readBit(UCSZn0) ? 0x1 : 0x0;

                    //frameSize = SIZE[UCSZVal];
                    // why does it look like they are using a 5 bit frame size?
                    frameSize = 8;
                }

                public void write(byte val) {
                    super.write((byte)(0x7f & val));
                    decode(val);
                }

                public void writeBit(int bit, boolean val) {
                    if (bit < 7) {
                        super.writeBit(bit, val);
                    }
                    decode(value);
                }

            }

            /**
             * The high byte of the Baud Rate register.
             */
            protected class UBRRnHReg extends State.RWIOReg {

                public void write(byte val) {
                    super.write((byte)(0x0f & val));
                }

                public void writeBit(int bit, boolean val) {
                    if (bit < 4) {
                        super.writeBit(bit, val);
                    }
                }
            }

            /**
             * The low byte of the Baud Rate register. The baud rate is not updated until the low bit is
             * updated.
             */
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

            /**
             * A simple implementation of the USARTDevice interface that connects to a USART on the processor.
             * It simply prints out a representation of each frame it receives.
             */
            protected class SerialPrinter implements USARTDevice {

                Simulator.Printer serialPrinter = simulator.getPrinter("atmega.usart.printer");

                char[] stream = {'h', 'e', 'l', 'l', 'o', 'w', 'o', 'r', 'l', 'd'};

                int count = 0;

                public USARTFrame transmitFrame() {
                    return new USARTFrame((byte)stream[count++ % stream.length], false, 8);
                }

                public void receiveFrame(USARTFrame frame) {
                    if (serialPrinter.enabled) serialPrinter.println("Serial Printer " + frame.toString());
                }

                SerialPrinter() {
                    PrinterTicker printerTicker = new PrinterTicker();
                }

                private class PrinterTicker implements Simulator.Event {
                    public void fire() {
                        if (UCSRnB_reg.readBit(RXENn)) receiver.enableReceive();
                    }
                }
            }
        }

        /**
         * Debug class. Connect this for TestUart test case. "Formats" LCD display.
         *
         * @author Daniel Lee
         */
        // TODO: this has major performance problems
        protected class LCDScreen implements USARTDevice {
            Simulator.Printer lcdPrinter = simulator.getPrinter("atmega.usart.lcd");

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
                    return (Character)line1.get(cursor);
                } else if (cursor < 80) {
                    return (Character)line2.get(cursor - 40);
                } else {
                    return dump;
                }
            }

            final Vector line1;
            final Vector line2;

            public LCDScreen() {
                line1 = new Vector(40);
                line2 = new Vector(40);
                initScreen();
            }

            public USARTFrame transmitFrame() {
                return new USARTFrame((byte)0, false, 8);
            }

            private void scrollRight() {
                line1.add(0, line1.remove(39));
                line2.add(0, line2.remove(39));
            }

            private void scrollLeft() {
                line1.add(39, line1.remove(0));
                line2.add(39, line2.remove(0));
            }

            private void clearScreen() {
                for (int i = 0; i < 40; i++) {
                    line1.set(i, new Character(' '));
                    line2.set(i, new Character(' '));

                }
            }

            private void initScreen() {
                for (int i = 0; i < 40; i++) {
                    line1.add(new Character(' '));
                    line2.add(new Character(' '));

                }
            }

            public void receiveFrame(USARTFrame frame) {
                byte data = frame.low;

                if (mode) { // Instruction mode
                    switch (data) {
                        case CLEAR_SCREEN:
                            clearScreen();
                            break;
                        case SCROLL_LEFT:
                            scrollLeft();
                            break;
                        case SCROLL_RIGHT:
                            scrollRight();
                            break;
                        case HOME:
                            cursor = 0;
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
                } else if (data == (byte)254) {
                    mode = MODE_INSTRUCTION;
                    return;
                } else {
                    setCursor(frame.low);
                    cursor = (cursor + 1) % 80;

                }
                lcdPrinter.println(this.toString());
            }

            private void setCursor(byte b) {
                if (cursor < 40) {
                    line1.set(cursor, new Character((char)b));
                } else if (cursor < 80) {
                    line2.set(cursor - 40, new Character((char)b));
                }
            }

            public String toString() {
                String sline1 = "";
                String sline2 = "";
                for (int i = 0; i < 40; i++) {
                    sline1 += line1.get(i);
                    sline2 += line2.get(i);
                }
                return '\n' + sline1 + '\n' + sline2 + '\n';
            }
        }


        /**
         * Serial Peripheral Interface. Used on the <code>Mica2</code> platform for radio communication.
         *
         * @author Daniel Lee, Simon Han
         */
        protected class SPI implements SPIDevice {
            final SPDReg SPDR_reg;
            final SPCRReg SPCR_reg;
            final SPSReg SPSR_reg;
            final SPIInterrupt SPI_int;

            final Simulator.Printer spiPrinter = simulator.getPrinter("atmega.spi");

            SPIDevice connectedDevice;

            final TransmitReceive transmitReceive = new TransmitReceive();

            int SPR;
            boolean SPI2x;
            boolean master;
            boolean SPIenabled;

            protected int period;

            public void connect(SPIDevice d) {
                connectedDevice = d;
            }

            public void receiveFrame(SPIFrame frame) {
                SPDR_reg.receiveReg.write(frame.data);
                if (!master && !transmitReceive.transmitting) SPSR_reg.writeBit(7, true); // flag interrupt

            }

            public SPIFrame transmitFrame() {
                return new SPIFrame(SPDR_reg.transmitReg.read());
            }

            SPI(BaseInterpreter ns) {
                SPDR_reg = new SPDReg();
                SPCR_reg = new SPCRReg();
                SPSR_reg = new SPSReg();
                SPI_int = new SPIInterrupt();

                // add SPI interrupt to simulator
                interrupts[18] = SPI_int;

                installIOReg(ns, SPDR, SPDR_reg);
                installIOReg(ns, SPSR, SPSR_reg);
                installIOReg(ns, SPCR, SPCR_reg);
            }

            /**
             * Post SPI interrupt
             */
            private void postSPIInterrupt() {
                if (SPCR_reg.readBit(7)) {
                    interpreter.postInterrupt(18);
                }
            }

            private void unpostSPIInterrupt() {
                interpreter.unpostInterrupt(18);

            }

            private void calculatePeriod() {
                int divider = 0;

                switch (SPR) {
                    case 0:
                        divider = 4;
                        break;
                    case 1:
                        divider = 16;
                        break;
                    case 2:
                        divider = 64;
                        break;
                    case 3:
                        divider = 128;
                        break;
                }

                if (SPI2x) {
                    divider /= 2;
                }

                period = divider * 8;
            }


            /**
             * The SPI transfer event.
             */
            protected class TransmitReceive implements Simulator.Event {

                SPIFrame myFrame;
                SPIFrame connectedFrame;
                boolean transmitting;

                protected void enableTransfer() {

                    if (master && SPIenabled && !transmitting) {
                        if (spiPrinter.enabled) {
                            spiPrinter.println("SPI: Master mode. Enabling transfer. ");
                        }
                        transmitting = true;
                        myFrame = transmitFrame();
                        connectedFrame = connectedDevice.transmitFrame();
                        insertEvent(this, period);
                    }
                }


                /**
                 * Notes. The way this delay is setup right now, when the ATMega128L is in master mode and
                 * transmits, the connected device has a delayed receive. For the radio, this is not a
                 * problem, as the radio is the master and is responsible for ensuring correct delivery time
                 * for the SPI.
                 */
                public void fire() {
                    if (SPIenabled) {
                        connectedDevice.receiveFrame(myFrame);
                        receiveFrame(connectedFrame);
                        transmitting = false;
                        postSPIInterrupt();
                    }
                }
            }


            class SPIInterrupt implements Interrupt {
                public void force() {
                    postSPIInterrupt();
                }

                public void fire() {
                    SPSR_reg.clearSPIF();

                    // should this also unpost the interrupt?
                    unpostSPIInterrupt();
                }
            }


            /**
             * SPI data register. Writes to this register are transmitted to the connected device and reads
             * from the register read the data received from the connected device.
             */
            class SPDReg implements State.IOReg {

                protected final ReceiveRegister receiveReg;
                protected final TransmitRegister transmitReg;

                protected class ReceiveRegister extends State.RWIOReg {

                    public byte read() {
                        byte val = super.read();
                        if (spiPrinter.enabled) spiPrinter.println("SPI: read " + hex(val) + " from SPDR ");
                        return val;

                    }

                    public boolean readBit(int bit) {
                        if (spiPrinter.enabled) spiPrinter.println("SPI: read bit " + bit + " from SPDR");
                        return super.readBit(bit);
                    }
                }

                protected class TransmitRegister extends State.RWIOReg {

                    byte oldData;

                    public void write(byte val) {
                        if (spiPrinter.enabled && oldData != val) spiPrinter.println("SPI: wrote " + Integer.toHexString(0xff & val) + " to SPDR");
                        super.write(val);
                        oldData = val;

                        // the enableTransfer method has the necessary checks to make sure a transfer at this point
                        // is necessary
                        transmitReceive.enableTransfer();

                    }

                    public void writeBit(int bit, boolean val) {
                        if (spiPrinter.enabled) spiPrinter.println("SPI: wrote " + val + " to SPDR, bit " + bit);
                        super.writeBit(bit, val);
                        transmitReceive.enableTransfer();
                    }

                }

                SPDReg() {

                    receiveReg = new ReceiveRegister();
                    transmitReg = new TransmitRegister();
                }

                /**
                 * The <code>read()</code> method
                 *
                 * @return the value from the receive buffer
                 */
                public byte read() {
                    return receiveReg.read();
                }

                /**
                 * The <code>write()</code> method
                 *
                 * @param val the value to transmit buffer
                 */
                public void write(byte val) {
                    transmitReg.write(val);
                }

                /**
                 * The <code>readBit()</code> method
                 *
                 * @param num
                 * @return false
                 */
                public boolean readBit(int num) {
                    return receiveReg.readBit(num);

                }

                /**
                 * The <code>writeBit()</code>
                 *
                 * @param num
                 */
                public void writeBit(int num, boolean val) {
                    transmitReg.writeBit(num, val);

                }
            }

            /**
             * SPI control register.
             */
            protected class SPCRReg extends State.RWIOReg {

                final int SPIE = 7;
                final int SPE = 6;
                final int DORD = 5; // does not really matter, because we are fastforwarding data
                final int MSTR = 4;
                final int CPOL = 3; // does not really matter, because we are fastforwarding data
                final int CPHA = 2; // does not really matter, because we are fastforwarding data
                final int SPR1 = 1;
                final int SPR0 = 0;  
                //OL: remember old state of spi enable bit
                boolean oldSpiEnable = false;

                public void write(byte val) {
                    if (spiPrinter.enabled) spiPrinter.println("SPI: wrote " + hex(val) + " to SPCR");
                    super.write(val);
                    decode(val);

                }

                public void writeBit(int bit, boolean val) {
                    if (spiPrinter.enabled) spiPrinter.println("SPI: wrote " + val + " to SPCR, bit " + bit);
                    super.writeBit(bit, val);
                    decode(value);
                }

                protected void decode(byte val) {

                    SPIenabled = Arithmetic.getBit(val, SPE);
                    
                    //OL: reset spi interrupt flag, when enabling SPI
                    //this is not described in the Atmega128l handbook
                    //however, the chip seems to work like this, as S-Mac
                    //does not work without it
                    if( Arithmetic.getBit(val, SPIE) && !oldSpiEnable ){
                        oldSpiEnable = true;
                        SPSR_reg.writeBit(SPSR_reg.SPI, false);
                    }
                    if( !Arithmetic.getBit(val, SPIE) && oldSpiEnable )
                        oldSpiEnable = false;
                    //end OL
                    
                    boolean oldMaster = master;
                    master = Arithmetic.getBit(val, MSTR);

                    SPR = 0;
                    SPR |= Arithmetic.getBit(val, SPR1) ? 0x02 : 0;
                    SPR |= Arithmetic.getBit(val, SPR0) ? 0x01 : 0;
                    calculatePeriod();

                    if (!oldMaster && master) {
                        transmitReceive.enableTransfer();
                    }
                }

            }

            /**
             * SPI status register.
             */
            class SPSReg extends State.RWIOReg {
                // TODO: implement write collision
                // TODO: finish implementing interrupt

                final int SPI = 7;
                final int WCOL = 6;

                public void write(byte val) {
                    if (spiPrinter.enabled) spiPrinter.println("SPI: wrote " + val + " to SPSR");
                    super.write(val);
                    decode(val);
                }

                public void writeBit(int bit, boolean val) {
                    if (spiPrinter.enabled) spiPrinter.println("SPI: wrote " + val + " to SPSR " + bit);
                    super.writeBit(bit, val);
                    decode(value);
                }

                byte oldVal;

                protected void decode(byte val) {

                    if (!Arithmetic.getBit(oldVal, SPI) && Arithmetic.getBit(val, SPI) && SPCR_reg.readBit(SPI)) {
                        postSPIInterrupt();
                    }
                    // TODO: write COLlision

                    SPI2x = Arithmetic.getBit(value, 0);
                    oldVal = val;
                }

                public void setSPIF() {
                    writeBit(SPI, true);
                }

                public void clearSPIF() {
                    writeBit(SPI, false);
                }

                public boolean getSPIF() {
                    return readBit(SPI);
                }

            }

        }

        /**
         * Debug class. Connect to SPI to give the SPI a duped input.
         */
        protected class SPIPrinter implements SPIDevice {

            public SPIDevice connectedDevice;
            private PrinterTicker ticker;

            private Simulator.Printer printer = simulator.getPrinter("atmega.spi.printer");

            public void connect(SPIDevice d) {
                connectedDevice = d;
            }

            SPIPrinter(SPIDevice cd) {
                connectedDevice = cd;
                ticker = new PrinterTicker();
                insertEvent(ticker, 3000);

            }


            private class PrinterTicker implements Simulator.Event {

                public void fire() {
                    receiveFrame(connectedDevice.transmitFrame());
                    connectedDevice.receiveFrame(transmitFrame());
                    insertEvent(this, 15000);


                }
            }

            byte oldData;

            public void receiveFrame(SPIFrame frame) {

                if (printer.enabled && (frame.data != oldData)) {
                    printer.println("SPIPrinter: " + (char)frame.data + ", " + frame.data);
                }
                oldData = frame.data;
            }

            public SPIFrame transmitFrame() {
                if (printer.enabled) {
                    printer.println("SPIPrinter: transmitting...");
                }
                return new SPIFrame((byte)0xff);
            }
        }

        /**
         * Analog to digital converter.
         *
         * @author Daniel Lee
         */
        protected class ADC {

            final Simulator.Printer adcPrinter = simulator.getPrinter("atmega.adc");

            final MUXRegister ADMUX_reg = new MUXRegister();
            final DataRegister ADC_reg = new DataRegister();
            final ControlRegister ADCSRA_reg = new ControlRegister();

            final ADCInput[] connectedDevices = new ADCInput[10];

            final BaseInterpreter interpreter;

            ADC(BaseInterpreter ns) {

                interpreter = ns;

                connectedDevices[8] = new VBG();
                connectedDevices[9] = new GND();

                installIOReg(ns, ADMUX, ADMUX_reg);
                installIOReg(ns, ADCH, ADC_reg.high);
                installIOReg(ns, ADCL, ADC_reg.low);
                installIOReg(ns, ADCSRA, ADCSRA_reg);

            }

            private class VBG implements ADCInput {
                public int getLevel() {
                    return 0x3ff; // figure out correct value for this eventually
                }
            }

            private class GND implements ADCInput {
                public int getLevel() {
                    return 0;
                }
            }

            private int calculateADC() {

                if (ADMUX_reg.singleEndedInput) {
                    //return 1;// VIN * 102/ VREG
                    if (connectedDevices[ADMUX_reg.singleInputIndex] != null)
                        return connectedDevices[ADMUX_reg.singleInputIndex].getLevel();
                    else
                        return 0;
                } else {
                    return 2; // (VPOS - VNEG) * GAIN * 512 / VREF
                }
            }

            public void connectADCInput(ADCInput input, int bit) {
                connectedDevices[bit] = input;
            }

            /**
             * Abstract class grouping together registers related to the ADC.
             */
            protected abstract class ADCRegister extends State.RWIOReg {
                public void write(byte val) {
                    super.write(val);
                    decode(val);
                    if (adcPrinter.enabled) {
                        printStatus();
                    }
                }

                public void writeBit(int bit, boolean val) {
                    super.writeBit(bit, val);
                    decode(value);
                    if (adcPrinter.enabled) {
                        printStatus();
                    }
                }

                protected abstract void decode(byte val);

                protected void printStatus() {
                }
            }

            /**
             * <code>MUXRegister</code> defines the behavior of the ADMUX register.
             */
            protected class MUXRegister extends ADCRegister {

                final int REFS_AREF = 0;
                final int REFS_AVCC = 1;
                // 2 reserved
                final int REFS_INTERNAL = 3;

                final int[] SINGLE_ENDED_INPUT = {0, 1, 2, 3, 4, 5, 6, 7,
                                                  -1, -1, -1, -1, -1, -1, -1, -1,
                                                  -1, -1, -1, -1, -1, -1, -1, -1,
                                                  -1, -1, -1, -1, -1, -1, 8, 9};

                int singleInputIndex = 0;

                final int[] GAIN = {-1, -1, -1, -1, -1, -1, -1, -1,
                                    10, 10, 200, 200, 10, 10, 200, 200,
                                    1, 1, 1, 1, 1, 1, 1, 1,
                                    1, 1, 1, 1, 1, 1, -1, -1};

                int gain = -1;


                final int[] POS_INPUT = {-1, -1, -1, -1, -1, -1, -1, -1,
                                         0, 1, 0, 1, 2, 3, 2, 3,
                                         0, 1, 2, 3, 4, 5, 6, 7,
                                         0, 1, 2, 3, 4, 5, -1, -1};

                int positiveInputIndex = -1;

                final int[] NEG_INPUT = {-1, -1, -1, -1, -1, -1, -1, -1,
                                         0, 0, 0, 0, 2, 2, 2, 2,
                                         1, 1, 1, 1, 1, 1, 1, 1,
                                         2, 2, 2, 2, 2, 2, -1, 1};

                int negativeInputIndex = -1;

                boolean singleEndedInput = true;

                int refs;
                boolean adlar;
                int mux;

                protected void decode(byte val) {
                    refs = (val & 0xc0) >> 6;
                    adlar = Arithmetic.getBit(val, 5);
                    mux = (val & 0x1f);

                    singleInputIndex = SINGLE_ENDED_INPUT[mux];
                    positiveInputIndex = POS_INPUT[mux];
                    negativeInputIndex = NEG_INPUT[mux];

                    singleEndedInput = (val < 8 || val == 0x1e || val == 0x1f);
                }

                protected void printStatus() {
                    adcPrinter.println("ADC (ADMUX): refs: " + refs + ", adlar: " + adlar + ", mux: " + mux);
                }
            }

            /**
             * <code>DataRegister</code> defines the behavior of the ADC's 10-bit data register.
             */
            protected class DataRegister extends State.RWIOReg {
                public final High high = new High();
                public final Low low = new Low();

                private class High extends State.RWIOReg {
                    public void write(byte val) {
                        super.write(((byte)(val & 0x3)));
                        if (adcPrinter.enabled) {
                            adcPrinter.println("ADC (ADCH): " + hex(val));
                        }
                    }

                    public void writeBit(int bit, boolean val) {
                        if (bit < 2) super.writeBit(bit, val);
                        if (adcPrinter.enabled) {
                            adcPrinter.println("ADC (ADCH): " + hex(value));
                        }
                    }

                    public byte read() {
                        if (adcPrinter.enabled) {
                            adcPrinter.println("ADC (ADCH): read " + hex(super.read()));
                        }
                        return super.read();
                    }
                }

                private class Low extends State.RWIOReg {
                    public byte read() {
                        if (adcPrinter.enabled) {
                            adcPrinter.println("ADC (ADCL): read " + hex(super.read()));
                        }
                        return super.read();
                    }

                    public void write(byte val) {
                        super.write(val);
                        if (adcPrinter.enabled) {
                            adcPrinter.println("ADC (ADCL): " + hex(value));
                        }

                    }
                }

            }

            /**
             * <code>ControlRegister</code> defines the behavior of the ADC control register,
             */
            protected class ControlRegister extends ADCRegister {

                final int ADEN = 7;
                final int ADSC = 6;
                final int ADFR = 5;
                final int ADIF = 4;
                final int ADIE = 3;
                final int ADPS2 = 2;
                final int ADPS1 = 1;
                final int ADPS0 = 0;

                boolean aden;
                boolean adsc;
                boolean adfr;
                boolean adif;
                boolean adie;

                int prescalerDivider = 2;

                final int[] PRESCALER = {2, 2, 4, 8, 16, 32, 64, 128};

                final Conversion conversion;

                byte oldVal;

                ControlRegister() {
                    conversion = new Conversion();
                    interrupts[22] = new ADCInterrupt();
                }

                protected void decode(byte val) {

                    aden = Arithmetic.getBit(val, ADEN);
                    adsc = Arithmetic.getBit(val, ADSC);
                    adfr = Arithmetic.getBit(val, ADFR);
                    adif = Arithmetic.getBit(val, ADIF);
                    adie = Arithmetic.getBit(val, ADIE);

                    prescalerDivider = PRESCALER[(val & 0x7)];

                    if (aden && adsc && !Arithmetic.getBit(oldVal, ADSC) && !firing) {
                        // queue event for converting
                        firing = true;
                        insertEvent(conversion, prescalerDivider * 13);
                    }

                    oldVal = val;

                }

                protected void printStatus() {
                    adcPrinter.println("ADC (ADCSRA): enable: " + aden + ", start conversion: " + adsc +
                            ", free running: " + adfr + ", interrupt flag: " + adif + ", interrupt enable: " + adie +
                            ", prescaler divider: " + prescalerDivider);
                }

                boolean firing;

                /**
                 * The conversion event for the ADC. It is first at a certain interval after the start
                 * conversion bit in the control register is set.
                 */
                private class Conversion implements Simulator.Event {
                    public void fire() {

                        if (adcPrinter.enabled) {
                            adcPrinter.println("ADC: Conversion complete.");
                        }

                        writeBit(ADSC, false);

                        if (aden) {
                            write16(calculateADC(), ADC_reg.high, ADC_reg.low);
                            if (adie) {
                                writeBit(ADIF, true);
                                interpreter.postInterrupt(22);
                            }
                        }
                    }
                }

                protected class ADCInterrupt implements Simulator.Interrupt {
                    public void force() {
                        //fire();
                    }

                    public void fire() {
                        interpreter.unpostInterrupt(22);
                        writeBit(ADIF, false);
                        firing = false;
                    }

                }
            }

        }

    }

    /**
     * TODO: Cleanup. Move SPIDevice, ADCInput, USARTDevice out of this and into their own package.
     * These interfaces really aren't ATMega128 specific.
     */

    /**
     * Interface for devices that can connect to the SPI. Rather than communicating over the MISO, MOSI pins,
     * the process is expedited and simplified through the use of the transmitFrame() and receiveFrame()
     * methods in the intefact.
     */
    public interface SPIDevice {
        /**
         * Transmit a frame from this device.
         *
         * @return the frame for transmission
         */
        public SPIFrame transmitFrame();

        /**
         * Receive a frame.
         *
         * @param frame the frame to be received
         */
        public void receiveFrame(SPIFrame frame);

        public void connect(SPIDevice d);

        /**
         * A single byte data frame for the SPI.
         */
        public class SPIFrame {
            public final byte data;

            public SPIFrame(byte data) {
                this.data = data;
            }
        }
    }


    /**
     * The <code>USARTDevice</code> interface describes USARTs and other serial devices which can be connected
     * to the USART. For simplicity, a higher-level interface communicating by frames of data is used, rather
     * than bits or a representation of changing voltages.
     */
    public interface USARTDevice {
        /**
         * Transmit a frame from this device.
         *
         * @return the frame for transmission
         */
        public USARTFrame transmitFrame();


        /**
         * Receive a frame.
         *
         * @param frame the frame to be received
         */
        public void receiveFrame(USARTFrame frame);

        /**
         * A <code>USARTFrame</code> is a representation of the serial frames being passed between the USART
         * and a connected device.
         */
        public class USARTFrame {
            public final byte low;
            public final boolean high;
            final int size;

            /**
             * Constructor for a USARTFrame. The <code>high</code> bit is used for 9 bit frame sizes.
             */
            public USARTFrame(byte low, boolean high, int size) {
                this.low = low;
                this.high = high;
                this.size = size;
            }

            /**
             * Returns the integer value of this data frame.
             *
             * @return intended value of this data frame
             */
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
                    return "" + (char)value() + " (" + value() + ')';
                }
            }
        }
    }

    /**
     * The <code>ADCInput</code> interface is used by inputs into the analog to digital converter.
     */
    public interface ADCInput {

        /**
         * Report the current voltage level of the input.
         */
        public int getLevel();
    }

    /**
     * Connect an instance of the <code>SPIDevice</code> interface to the SPI of this microcontroller.
     */
    public void connectSPIDevice(SPIDevice d) {
        spi.connectedDevice = d;
        d.connect(spi);
    }

    public void connectPc() {
        usart0.connectedDevice = new Pc(usart0);       
    }

    /**
     * Connect an instance of the <code>ADCInput</code> interface to the ADC of this microcontroller. The ADC
     * unit on the ATMega128L can support up to 8 ADC inputs, on bits 0 - 7.
     */
    public void connectADCInput(ADCInput d, int bit) {
        adc.connectADCInput(d, bit);
    }

    /**
     * Helper function to get a 16 bit value from a pair of registers.
     */
    private static int read16(State.RWIOReg high, State.RWIOReg low) {
        int result = low.read() & 0xff;
        result |= (high.read() & 0xff) << 8;
        return result;
    }

    /**
     * Helper function to write a 16-bit value to a pair of registers.
     */
    private static void write16(int val, State.RWIOReg high, State.RWIOReg low) {
        high.write((byte)((val & 0xff00) >> 8));
        low.write((byte)(val & 0x00ff));
    }

    /**
     * Helper function to get a hex string representation of a byte.
     */
    private static String hex(byte val) {
        return Integer.toHexString(0xff & val);
    }

    /**
     * Interface for an alternate clock. In practice, it is used to create a clock that runs at a multiplier
     * of the simulation base clock.
     */
    protected interface Clock {
        public void scheduleEvent(Simulator.Event e, long cycles);

    }

    /**
     * The Radio connected to this microcontroller.
     */
    protected Radio radio;

    public void setRadio(Radio radio) {
        this.radio = radio;
    }

    public Radio getRadio() {
        return radio;
    }

    /**
     * send to mcu to sleep
     *
     * @see avrora.sim.mcu.Microcontroller#sleep()
     */
    public void sleep() {
        pm.sleep();
    }

    /**
     * wake the mcu up
     *
     * @return cycles it takes to wake up
     * @see avrora.sim.mcu.Microcontroller#wakeup()
     */
    public int wakeup() {
        return pm.wakeup();
    }

    /**
     * get the current mode of the mcu
     *
     * @return current mode
     * @see avrora.sim.mcu.Microcontroller#getMode()
     */
    public byte getMode() {
        return pm.getMode();
    }

    /**
     * get the name of the current mode
     *
     * @return name of the current mode
     */
    public String getModeName() {
        return pm.getModeName();
    }
    
    /**
     * PC connection. Connect system to TinyOS Serial Forwarder
     *
     * @author Olaf Landsiedel
     */
    protected class Pc implements USARTDevice {
        Simulator.Printer pcPrinter = simulator.getPrinter("sim.pc");
        private ServerSocket serverSocket;
        private Socket socket;
        private OutputStream out;
        private InputStream in;
        private SimImpl.USART usart;
        private PcTicker ticker;
        private byte[] data; 

        public Pc(SimImpl.USART usart) {                
            this.usart = usart;
            ticker = new PcTicker();
            data = new byte[1];
            try{            
                serverSocket = new ServerSocket(2390);
                socket = serverSocket.accept();
                out = socket.getOutputStream();
                in = socket.getInputStream();
            } catch( IOException e ){
                throw Avrora.failure("cannot connect to serial forwarder");
            }
        }

        public USARTFrame transmitFrame() {
            try{ 
                in.read( data, 0, 1);
                return new USARTFrame(data[0], false, 8);
            } catch( IOException e){
                throw Avrora.failure("cannot read from socket");
            }
        }


        public void receiveFrame(USARTFrame frame) {
            try{
                out.write(frame.low);
            } catch( IOException e){
                throw Avrora.failure("cannot write to socket");
            }
        }

        private class PcTicker implements Simulator.Event {
            private final long delta = 3072;
            
            public PcTicker(){
                simulator.insertEvent(this, delta);                    
            }
            
            public void fire() {
                try{
                    if( in.available() >= 1 ) {
                        usart.startReceive();                            
                    }
            	} catch( IOException e){
                    throw Avrora.failure("cannot read from socket");
            	}
                simulator.insertEvent(this, delta);                                        
            }
        }
    }

}
