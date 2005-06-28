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

import avrora.util.Arithmetic;
import avrora.sim.radio.Radio;
import avrora.sim.*;
import avrora.sim.clock.ClockDomain;
import avrora.core.InstrPrototype;
import avrora.core.Program;

import java.util.HashMap;

/**
 * The <code>ATMega32</code> class represents the ATMega32 microcontroller from Atmel. This
 * microcontroller has 32Kb code, 2KB SRAM, 1KB EEPROM, and a host of internal devices such as
 * ADC, SPI, and timers.
 *
 * @author Ben L. Titzer
 */
public class ATMega32 extends ATMegaFamily {

    public static final int _1kb = 1024;

    public static final int ATMEGA32_IOREG_SIZE = 64;
    public static final int ATMEGA32_SRAM_SIZE = 2 * _1kb;
    public static final int ATMEGA32_FLASH_SIZE = 32 * _1kb;
    public static final int ATMEGA32_EEPROM_SIZE = 1 * _1kb;
    public static final int ATMEGA32_NUM_PINS = 45;
    public static final int ATMEGA32_NUM_INTS = 20;

    public static final int MODE_IDLE       = 1;
    public static final int MODE_RESERVED1  = 2;
    public static final int MODE_ADCNRED    = 3;
    public static final int MODE_RESERVED2  = 4;
    public static final int MODE_POWERDOWN  = 5;
    public static final int MODE_STANDBY    = 6;
    public static final int MODE_POWERSAVE  = 7;
    public static final int MODE_EXTSTANDBY = 8;

    protected static final String[] idleModeNames = {
        "Active",
        "Idle",
        "RESERVED 1",
        "ADC Noise Reduction",
        "RESERVED 2",
        "Power Down",
        "Standby",
        "Power Save",
        "Extended Standby"
    };

    protected static final int[] wakeupTimes = {
        0, 0, 0, 0, 0, 1000, 6, 1000, 6
    };

    protected final ActiveRegister MCUCR_reg;

    private static final int[][] transitionTimeMatrix  = FiniteStateMachine.buildBimodalTTM(idleModeNames.length, 0, wakeupTimes, new int[wakeupTimes.length]);


    /**
     * The <code>props</code> field stores a static reference to a properties
     * object shared by all of the instances of this microcontroller. This object
     * stores the IO register size, SRAM size, pin assignments, etc.
     */
    public static final MicrocontrollerProperties props;

    static {
        // statically initialize the pin assignments for this microcontroller
        HashMap pinAssignments = new HashMap(150);
        HashMap ioregAssignments = new HashMap(120);
        HashMap interruptAssignments = new HashMap(30);

        addPin(pinAssignments, 1, "MOSI", "PB5");
        addPin(pinAssignments, 2, "MSIO", "PB6");
        addPin(pinAssignments, 3, "SCK", "PB7");
        addPin(pinAssignments, 4, "RESET");
        addPin(pinAssignments, 5, "VCC.1");
        addPin(pinAssignments, 6, "GND.1");
        addPin(pinAssignments, 7, "XTAL2");
        addPin(pinAssignments, 8, "XTAL1");
        addPin(pinAssignments, 9, "RXD", "PD0");
        addPin(pinAssignments, 10, "TXD", "PD1");
        addPin(pinAssignments, 11, "INT0", "PD2");
        addPin(pinAssignments, 12, "INT1", "PD3");
        addPin(pinAssignments, 13, "OC1B", "PD4");
        addPin(pinAssignments, 14, "OC1A", "PD5");
        addPin(pinAssignments, 15, "ICP1", "PD6");
        addPin(pinAssignments, 16, "OC2", "PD7");
        addPin(pinAssignments, 17, "VCC.2");
        addPin(pinAssignments, 18, "GND.2");
        addPin(pinAssignments, 19, "SCL", "PC0");
        addPin(pinAssignments, 20, "SDA", "PC1");
        addPin(pinAssignments, 21, "TCK", "PC2");
        addPin(pinAssignments, 22, "TMS", "PC3");
        addPin(pinAssignments, 23, "TDO", "PC4");
        addPin(pinAssignments, 24, "TDI", "PC5");
        addPin(pinAssignments, 25, "TOSC1", "PC6");
        addPin(pinAssignments, 26, "TOSC2", "PC7");
        addPin(pinAssignments, 27, "AVCC");
        addPin(pinAssignments, 28, "GND.3");
        addPin(pinAssignments, 29, "AREF");
        addPin(pinAssignments, 30, "ADC7", "PA7");
        addPin(pinAssignments, 31, "ADC6", "PA6");
        addPin(pinAssignments, 32, "ADC5", "PA5");
        addPin(pinAssignments, 33, "ADC4", "PA4");
        addPin(pinAssignments, 34, "ADC3", "PA3");
        addPin(pinAssignments, 35, "ADC2", "PA2");
        addPin(pinAssignments, 36, "ADC1", "PA1");
        addPin(pinAssignments, 37, "ADC0", "PA0");
        addPin(pinAssignments, 38, "VCC.3");
        addPin(pinAssignments, 39, "GND.4");
        addPin(pinAssignments, 40, "XCK", "T0", "PB0");
        addPin(pinAssignments, 41, "T1", "PB1");
        addPin(pinAssignments, 42, "AIN0", "INT2", "PB2");
        addPin(pinAssignments, 43, "AIN1", "OC0", "PB3");
        addPin(pinAssignments, 44, "SS", "PB4");

        // lower 64 IO registers
        addIOReg(ioregAssignments, "SREG", 0x3F);
        addIOReg(ioregAssignments, "SPH", 0x3E);
        addIOReg(ioregAssignments, "SPL", 0x3D);
        addIOReg(ioregAssignments, "OCR0", 0x3C);
        addIOReg(ioregAssignments, "GICR", 0x3B);
        addIOReg(ioregAssignments, "GIFR", 0x3A);
        addIOReg(ioregAssignments, "TIMSK", 0x39);
        addIOReg(ioregAssignments, "TIFR", 0x38);
        addIOReg(ioregAssignments, "SPMCR", 0x37);
        // TODO: this this register is called different names on different models
        addIOReg(ioregAssignments, "SPMCSR", 0x37);
        addIOReg(ioregAssignments, "TWCR", 0x36);
        addIOReg(ioregAssignments, "MCUCR", 0x35);
        addIOReg(ioregAssignments, "MCUCSR", 0x34);
        addIOReg(ioregAssignments, "TCCR0", 0x33);
        addIOReg(ioregAssignments, "TCNT0", 0x32);
        addIOReg(ioregAssignments, "OSCCAL", 0x31);
        addIOReg(ioregAssignments, "SFIOR", 0x30);
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
        addIOReg(ioregAssignments, "ASSR", 0x22);
        addIOReg(ioregAssignments, "WDTCR", 0x21);
        addIOReg(ioregAssignments, "UBRRH", 0x20);
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
        addIOReg(ioregAssignments, "UDR", 0x0C);
        addIOReg(ioregAssignments, "UCSRA", 0x0B);
        addIOReg(ioregAssignments, "UCSRB", 0x0A);
        addIOReg(ioregAssignments, "UBRRL", 0x09);
        addIOReg(ioregAssignments, "ACSR", 0x08);
        addIOReg(ioregAssignments, "ADMUX", 0x07);
        addIOReg(ioregAssignments, "ADCSRA", 0x06);
        addIOReg(ioregAssignments, "ADCH", 0x05);
        addIOReg(ioregAssignments, "ADCL", 0x04);
        addIOReg(ioregAssignments, "TWDR", 0x03);
        addIOReg(ioregAssignments, "TWAR", 0x02);
        addIOReg(ioregAssignments, "TWSR", 0x01);
        addIOReg(ioregAssignments, "TWBR", 0x00);

        // note: the UART implementation assumes the names are UDRn, etc.
        addIOReg(ioregAssignments, "UBRR0H", 0x20);
        addIOReg(ioregAssignments, "UDR0", 0x0C);
        addIOReg(ioregAssignments, "UCSR0A", 0x0B);
        addIOReg(ioregAssignments, "UCSR0B", 0x0A);
        addIOReg(ioregAssignments, "UBRR0L", 0x09);

        addInterrupt(interruptAssignments, "RESET", 1);
        addInterrupt(interruptAssignments, "INT0", 2);
        addInterrupt(interruptAssignments, "INT1", 3);
        addInterrupt(interruptAssignments, "INT2", 4);
        addInterrupt(interruptAssignments, "TIMER2 COMP", 5);
        addInterrupt(interruptAssignments, "TIMER2 OVF", 6);
        addInterrupt(interruptAssignments, "TIMER1 CAPT", 7);
        addInterrupt(interruptAssignments, "TIMER1 COMPA", 8);
        addInterrupt(interruptAssignments, "TIMER1 COMPB", 9);
        addInterrupt(interruptAssignments, "TIMER1 OVF", 10);
        addInterrupt(interruptAssignments, "TIMER0 COMP", 11);
        addInterrupt(interruptAssignments, "TIMER0 OVF", 12);
        addInterrupt(interruptAssignments, "SPI, STC", 13);
        addInterrupt(interruptAssignments, "USART, RXC", 14);
        addInterrupt(interruptAssignments, "USART, UDRE", 15);
        addInterrupt(interruptAssignments, "USART, TXC", 16);
        addInterrupt(interruptAssignments, "ADC", 17);
        addInterrupt(interruptAssignments, "EE READY", 18);
        addInterrupt(interruptAssignments, "ANALOG COMP", 19);
        addInterrupt(interruptAssignments, "TWI", 20);
        addInterrupt(interruptAssignments, "SPM READY", 21);

        props = new MicrocontrollerProperties(ATMEGA32_IOREG_SIZE, // number of io registers
                ATMEGA32_SRAM_SIZE, // size of sram in bytes
                ATMEGA32_FLASH_SIZE, // size of flash in bytes
                ATMEGA32_EEPROM_SIZE, // size of eeprom in bytes
                ATMEGA32_NUM_PINS, // number of pins
                ATMEGA32_NUM_INTS, // number of interrupts
                new ReprogrammableCodeSegment.Factory(ATMEGA32_FLASH_SIZE, 6),
                pinAssignments, // the assignment of names to physical pins
                ioregAssignments, // the assignment of names to IO registers
                interruptAssignments);

    }

    public static class Factory implements MicrocontrollerFactory {

        /**
         * The <code>newMicrocontroller()</code> method is used to instantiate a microcontroller instance for the
         * particular program. It will construct an instance of the <code>Simulator</code> class that has all the
         * properties of this hardware device and has been initialized with the specified program.
         *
         * @param p the program to load onto the microcontroller
         * @return a <code>Microcontroller</code> instance that represents the specific hardware device with the
         *         program loaded onto it
         */
        public Microcontroller newMicrocontroller(int id, ClockDomain cd, InterpreterFactory f, Program p) {
            return new ATMega32(id, cd, f, p);
        }

    }

    public ATMega32(int id, ClockDomain cd, InterpreterFactory f, Program p) {
        super(cd, props, new FiniteStateMachine(cd.getMainClock(), MODE_ACTIVE, idleModeNames, transitionTimeMatrix));
        simulator = new Simulator(id, f, this, p);
        interpreter = simulator.getInterpreter();
        MCUCR_reg = getIOReg("MCUCR");
        installPins();
        installDevices();
    }

    public boolean isSupported(InstrPrototype i) {
        return true;
    }

    protected void installPins() {
        for (int cntr = 0; cntr < properties.num_pins; cntr++)
            pins[cntr] = new Pin(cntr);
    }

    protected void installDevices() {
        // TODO: the interrupt mappings of this processor are incorrect!!!

        // set up the external interrupt mask and flag registers and interrupt range
        EIFR_reg = buildInterruptRange(true, "GICR", "GIFR", 2, 8);

        // set up the timer mask and flag registers and interrupt range
        TIFR_reg = buildInterruptRange(false, "TIMSK", "TIFR", 12, 8);
        TIMSK_reg = (MaskRegister)interpreter.getIOReg(props.getIOReg("TIMSK"));


        addDevice(new Timer0());
        addDevice(new Timer1());
        addDevice(new Timer2());

        buildPort('A');
        buildPort('B');
        buildPort('C');
        buildPort('D');

        addDevice(new EEPROM(properties.eeprom_size, this));
        addDevice(new USART("", this));

        addDevice(new SPI(this));
        addDevice(new ADC(this, 8));
    }


    // permutation of sleep mode bits in the register (high order bits first)
    private static final int[] MCUCR_sm_perm = { 2, 4, 3 };

    protected int getSleepMode() {
        byte value = MCUCR_reg.read();
        boolean sleepEnable = Arithmetic.getBit(value, 5);

        if ( sleepEnable )
            return Arithmetic.getBitField(value, MCUCR_sm_perm) + 1;
        else
            return MODE_IDLE;
    }

}
