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

import avrora.Avrora;
import avrora.sim.radio.Radio;
import avrora.sim.InterpreterFactory;
import avrora.sim.platform.Platform;
import avrora.core.InstrPrototype;
import avrora.core.Program;

import java.util.HashMap;

/**
 * @author Ben L. Titzer
 */
public abstract class ATMega128 extends ATMegaFamily {

    public static final int _1kb = 1024;

    public static final int ATMEGA128_IOREG_SIZE = 256 - 32;
    public static final int ATMEGA128_SRAM_SIZE = 4 * _1kb;
    public static final int ATMEGA128_FLASH_SIZE = 128 * _1kb;
    public static final int ATMEGA128_EEPROM_SIZE = 4 * _1kb;
    public static final int ATMEGA128_NUM_PINS = 64;

    /**
     * The <code>props</code> field stores a static reference to a properties
     * object shared by all of the instances of this microcontroller. This object
     * stores the IO register size, SRAM size, pin assignments, etc.
     */
    public static final MicrocontrollerProperties props;

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

        props = new MicrocontrollerProperties(ATMEGA128_IOREG_SIZE, // number of io registers
                ATMEGA128_SRAM_SIZE, // size of sram in bytes
                ATMEGA128_FLASH_SIZE, // size of flash in bytes
                ATMEGA128_EEPROM_SIZE, // size of eeprom in bytes
                ATMEGA128_NUM_PINS, // number of pins
                pinAssignments, // the assignment of names to physical pins
                ioregAssignments); // the assignment of names to IO registers

    }

    public abstract static class Factory implements MicrocontrollerFactory {

        public Microcontroller newMicrocontroller(int id, InterpreterFactory i, Program p) {
            throw Avrora.unimplemented();
        }

    }

    public ATMega128(int hz) {
        super(hz, ATMEGA128_SRAM_SIZE, ATMEGA128_IOREG_SIZE, ATMEGA128_FLASH_SIZE, ATMEGA128_EEPROM_SIZE, ATMEGA128_NUM_PINS);
    }

    public boolean isSupported(InstrPrototype i) {
        return true;
    }

    public void sleep() {
        throw Avrora.unimplemented();
    }

    public Radio getRadio() {
        throw Avrora.unimplemented();
    }
}
