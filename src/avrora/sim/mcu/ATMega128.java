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
import avrora.util.Arithmetic;
import avrora.sim.radio.Radio;
import avrora.sim.*;
import avrora.sim.platform.Platform;
import avrora.core.InstrPrototype;
import avrora.core.Program;

import java.util.HashMap;

/**
 * The <code>ATMega128</code> class represents the ATMega128 microcontroller from Atmel. This
 * microcontroller has 128Kb code, 4KB SRAM, 4KB EEPROM, and a host of internal devices such as
 * ADC, SPI, and timers.
 *
 * @author Ben L. Titzer
 */
public class ATMega128 extends ATMegaFamily {

    public static final int _1kb = 1024;

    public static final int ATMEGA128_IOREG_SIZE = 256 - 32;
    public static final int ATMEGA128_SRAM_SIZE = 4 * _1kb;
    public static final int ATMEGA128_FLASH_SIZE = 128 * _1kb;
    public static final int ATMEGA128_EEPROM_SIZE = 4 * _1kb;
    public static final int ATMEGA128_NUM_PINS = 65;

    public static final int MODE_ACTIVE     = 0;
    public static final int MODE_IDLE       = 1;
    public static final int MODE_RESERVED1  = 2;
    public static final int MODE_ADCNRED    = 3;
    public static final int MODE_RESERVED2  = 4;
    public static final int MODE_POWERDOWN  = 5;
    public static final int MODE_STANDBY    = 6;
    public static final int MODE_POWERSAVE  = 7;
    public static final int MODE_EXTSTANDBY = 8;

    protected static final String[] idleModeName = {
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

    protected final FiniteStateMachine sleepState;
    protected final State.IOReg MCUCR_reg;

    private static final int[][] transitionTimeMatrix  = FiniteStateMachine.buildBimodalTTM(idleModeName.length, 0, wakeupTimes, new int[wakeupTimes.length]);


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
        // TODO: verify addresses of extended IO registers
        addIOReg(ioregAssignments, "UCSR1C", 0x7D);
        addIOReg(ioregAssignments, "UDR1", 0x7C);
        addIOReg(ioregAssignments, "UCSR1A", 0x7B);
        addIOReg(ioregAssignments, "UCSR1B", 0x7A);
        addIOReg(ioregAssignments, "UBRR1L", 0x79);
        addIOReg(ioregAssignments, "UBRR1H", 0x78);

        addIOReg(ioregAssignments, "UCSR0C", 0x75);

        addIOReg(ioregAssignments, "UBRR0H", 0x70);

        addIOReg(ioregAssignments, "TCCR3C", 0x6C);
        addIOReg(ioregAssignments, "TCCR3A", 0x6B);
        addIOReg(ioregAssignments, "TCCR3B", 0x6A);
        addIOReg(ioregAssignments, "TCNT3H", 0x69);
        addIOReg(ioregAssignments, "TCNT3L", 0x68);
        addIOReg(ioregAssignments, "OCR3AH", 0x67);
        addIOReg(ioregAssignments, "OCR3AL", 0x66);
        addIOReg(ioregAssignments, "OCR3BH", 0x65);
        addIOReg(ioregAssignments, "OCR3BL", 0x64);
        addIOReg(ioregAssignments, "OCR3CH", 0x63);
        addIOReg(ioregAssignments, "OCR3CL", 0x62);
        addIOReg(ioregAssignments, "ICR3H", 0x61);
        addIOReg(ioregAssignments, "ICR3L", 0x60);

        addIOReg(ioregAssignments, "ETIMSK", 0x5D);
        addIOReg(ioregAssignments, "ETIFR", 0x5C);

        addIOReg(ioregAssignments, "TCCR1C", 0x5A);
        addIOReg(ioregAssignments, "OCR1CH", 0x59);
        addIOReg(ioregAssignments, "OCR1CL", 0x58);

        addIOReg(ioregAssignments, "TWCR", 0x54);
        addIOReg(ioregAssignments, "TWDR", 0x53);
        addIOReg(ioregAssignments, "TWAR", 0x52);
        addIOReg(ioregAssignments, "TWSR", 0x51);
        addIOReg(ioregAssignments, "TWBR", 0x50);
        addIOReg(ioregAssignments, "OSCCAL", 0x4F);

        addIOReg(ioregAssignments, "XMCRA", 0x4D);
        addIOReg(ioregAssignments, "XMCRB", 0x4C);

        addIOReg(ioregAssignments, "EICRA", 0x4A);

        addIOReg(ioregAssignments, "SPMCSR", 0x48);

        addIOReg(ioregAssignments, "PORTG", 0x45);
        addIOReg(ioregAssignments, "DDRG", 0x44);
        addIOReg(ioregAssignments, "PING", 0x43);
        addIOReg(ioregAssignments, "PORTF", 0x42);
        addIOReg(ioregAssignments, "DDRF", 0x41);

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
            return new ATMega128(id, cd, f, p);
        }

    }

    protected ATMega128(int id, ClockDomain cd, InterpreterFactory f, Program p) {
        super(cd, props);
        simulator = new Simulator(id, f, this, p);
        interpreter = simulator.getInterpreter();
        sleepState = new FiniteStateMachine(mainClock, 0, idleModeName, transitionTimeMatrix);
        MCUCR_reg = getIOReg("MCUCR");
        installPins();
        installDevices();
    }

    public boolean isSupported(InstrPrototype i) {
        return true;
    }

    protected void installPins() {
        for (int cntr = 0; cntr < properties.num_pins; cntr++)
            pins[cntr] = new ATMegaFamily.Pin(cntr);
    }

    protected void installDevices() {
        // set up the external interrupt mask and flag registers and interrupt range
        EIFR_reg = buildInterruptRange(true, "EIMSK", "EIFR", 2, 8);
        EIMSK_reg = EIFR_reg.maskRegister;

        // set up the timer mask and flag registers and interrupt range
        TIFR_reg = buildInterruptRange(false, "TIMSK", "TIFR", 17, 8);
        TIMSK_reg = TIFR_reg.maskRegister;


        /* For whatever reason, the ATMega128 engineers decided
           against having the bitorder for the ETIFR/ETIMSK
           registers line up with the corresponding block of the
           interrupt vector. Therefore, we have to line this up by
           hand.
        */

        int[] ETIFR_mapping = {25, 29, 30, 28, 27, 26, -1, -1};
        ETIFR_reg = new FlagRegister(interpreter, ETIFR_mapping); // false, 0 are just placeholder falues
        ETIMSK_reg = ETIFR_reg.maskRegister;
        installIOReg("ETIMSK", ETIMSK_reg);
        installIOReg("ETIFR", ETIFR_reg);


        // Timer1 COMPC
        installInterrupt("Timer1 COMPC", 25, new MaskableInterrupt(25, ETIMSK_reg, ETIFR_reg, 0, false));
        // Timer3 CAPT
        installInterrupt("Timer3 CAPT", 26, new MaskableInterrupt(26, ETIMSK_reg, ETIFR_reg, 5, false));
        // Timer3 COMPA
        installInterrupt("Timer3 COMPA", 27, new MaskableInterrupt(27, ETIMSK_reg, ETIFR_reg, 4, false));
        // Timer3 COMPB
        installInterrupt("Timer3 COMPB", 28, new MaskableInterrupt(28, ETIMSK_reg, ETIFR_reg, 3, false));
        // Timer3 COMPC
        installInterrupt("Timer3 COMPC", 29, new MaskableInterrupt(29, ETIMSK_reg, ETIFR_reg, 1, false));
        // Timer3 OVF
        installInterrupt("Timer3 OVF", 30, new MaskableInterrupt(30, ETIMSK_reg, ETIFR_reg, 2, false));

        addDevice(new Timer0());
        addDevice(new Timer1());
        addDevice(new Timer2());
        addDevice(new Timer3());

        buildPort('A');
        buildPort('B');
        buildPort('C');
        buildPort('D');
        buildPort('E');
        buildPort('F');

        addDevice(new EEPROM(properties.eeprom_size, this));
        addDevice(new USART0());
        addDevice(new USART1());

        addDevice(new SPI(this));
        addDevice(new ADC(this));
        //pm = new PowerManagement(interpreter);

    }


    /**
     * send to mcu to sleep
     *
     * @see avrora.sim.mcu.Microcontroller#sleep()
     */
    public void sleep() {
        // transition to the sleep state in the MCUCR register
        sleepState.transition(getSleepMode());
    }

    // permutation of sleep mode bits in the register (high order bits first)
    private static final int[] MCUCR_sm_perm = { 2, 4, 3 };

    private int getSleepMode() {
        byte value = MCUCR_reg.read();
        boolean sleepEnable = Arithmetic.getBit(value, 5);

        if ( sleepEnable )
            return Arithmetic.getBitField(value, MCUCR_sm_perm);
        else
            return MODE_IDLE;
    }

    /**
     * wake the mcu up
     *
     * @return cycles it takes to wake up
     * @see avrora.sim.mcu.Microcontroller#wakeup()
     */
    public int wakeup() {
        // transition to the active state (may insert transition event into event queue)
        sleepState.transition(MODE_ACTIVE);
        // return the number of cycles consumed by waking up
        return sleepState.getTransitionTime(sleepState.getCurrentState(), MODE_ACTIVE);
    }

    /**
     * get the current mode of the mcu
     *
     * @return current mode
     * @see avrora.sim.mcu.Microcontroller#getMode()
     */
    public byte getMode() {
        return (byte)sleepState.getCurrentState();
    }

    /**
     * get the name of the current mode
     *
     * @return name of the current mode
     */
    public String getModeName() {
        return sleepState.getCurrentStateName();
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
}
