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

import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.sim.RWRegister;
import avrora.util.Arithmetic;

/**
 * The <code>ADC</code> class represents an on-chip device on the ATMega series of microcontroller
 * that is capable of converting an analog voltage value into a 10-bit digital value.
 *
 * @author Daniel Lee
 */
public class ADC extends AtmelInternalDevice {

    public static final int ACSR = 0x08;
    public static final int ADMUX = 0x07;
    public static final int ADCSRA = 0x06;
    public static final int ADCH = 0x05;
    public static final int ADCL = 0x04;

    private static final ADCInput VBG_INPUT = new ADCInput() {
        public int getLevel() {
            return 0x3ff; // figure out correct value for this eventually
        }
    };

    private static final ADCInput GND_INPUT = new ADCInput() {
        public int getLevel() {
            return 0x000; // figure out correct value for this eventually
        }
    };

    final MUXRegister ADMUX_reg = new MUXRegister();
    final DataRegister ADC_reg = new DataRegister();
    final ControlRegister ADCSRA_reg = new ControlRegister();

    final int channels;

    final ADCInput[] connectedDevices;

    /**
     * The <code>ADCInput</code> interface is used by inputs into the analog to digital converter.
     */
    public interface ADCInput {

        /**
         * Report the current voltage level of the input.
         * @return an integer value representing the voltage level of the input
         */
        public int getLevel();
    }


    public ADC(AtmelMicrocontroller m, int channels) {
        super("adc", m);

        this.channels = channels;

        connectedDevices = new ADCInput[channels + 2];

        // the last two channels correspond to VBG and GND
        connectedDevices[channels] = VBG_INPUT;
        connectedDevices[channels + 1] = GND_INPUT;

        installIOReg("ADMUX", ADMUX_reg);
        installIOReg("ADCH", ADC_reg.high);
        installIOReg("ADCL", ADC_reg.low);
        installIOReg("ADCSRA", ADCSRA_reg);

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

    /**
     * The <code>connectADCInput()</code> method connects an <code>ADCInput</code> object to the specified
     * input port on the ADC chip.
     *
     * @param input the <code>ADCInput</code> object to attach to the input
     * @param num the input port number to attach the device to
     */
    public void connectADCInput(ADCInput input, int num) {
        connectedDevices[num] = input;
    }

    /**
     * Abstract class grouping together registers related to the ADC.
     */
    // TODO: is this class necessary?
    protected abstract class ADCRegister extends RWRegister {
        public void write(byte val) {
            super.write(val);
            decode(val);
            if (devicePrinter.enabled) {
                printStatus();
            }
        }

        public void writeBit(int bit, boolean val) {
            super.writeBit(bit, val);
            decode(value);
            if (devicePrinter.enabled) {
                printStatus();
            }
        }

        protected abstract void decode(byte val);

        protected void printStatus() {
        }
    }

    static final int[] SINGLE_ENDED_INPUT = {0, 1, 2, 3, 4, 5, 6, 7,
                                      -1, -1, -1, -1, -1, -1, -1, -1,
                                      -1, -1, -1, -1, -1, -1, -1, -1,
                                      -1, -1, -1, -1, -1, -1, 8, 9};

    static final int[] GAIN = {-1, -1, -1, -1, -1, -1, -1, -1,
                        10, 10, 200, 200, 10, 10, 200, 200,
                        1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, -1, -1};

    static final int[] POS_INPUT = {-1, -1, -1, -1, -1, -1, -1, -1,
                             0, 1, 0, 1, 2, 3, 2, 3,
                             0, 1, 2, 3, 4, 5, 6, 7,
                             0, 1, 2, 3, 4, 5, -1, -1};

    static final int[] NEG_INPUT = {-1, -1, -1, -1, -1, -1, -1, -1,
                             0, 0, 0, 0, 2, 2, 2, 2,
                             1, 1, 1, 1, 1, 1, 1, 1,
                             2, 2, 2, 2, 2, 2, -1, 1};

    /**
     * <code>MUXRegister</code> defines the behavior of the ADMUX register.
     */
    protected class MUXRegister extends ADCRegister {

        static final int REFS_AREF = 0;
        static final int REFS_AVCC = 1;
        // 2 reserved
        static final int REFS_INTERNAL = 3;


        int singleInputIndex = 0;
        int gain = -1;
        int positiveInputIndex = -1;
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
            devicePrinter.println("ADC (ADMUX): refs: " + refs + ", adlar: " + adlar + ", mux: " + mux);
        }
    }

    /**
     * <code>DataRegister</code> defines the behavior of the ADC's 10-bit data register.
     */
    // TODO: is this class even necessary?
    protected class DataRegister {
        public final DataRegister.High high = new DataRegister.High();
        public final DataRegister.Low low = new DataRegister.Low();

        private class High extends RWRegister {
            public void write(byte val) {
                super.write(((byte)(val & 0x3)));
                if (devicePrinter.enabled) {
                    devicePrinter.println("ADC (ADCH): " + hex(val));
                }
            }

            public void writeBit(int bit, boolean val) {
                if (bit < 2) super.writeBit(bit, val);
                if (devicePrinter.enabled) {
                    devicePrinter.println("ADC (ADCH): " + hex(value));
                }
            }

            public byte read() {
                if (devicePrinter.enabled) {
                    devicePrinter.println("ADC (ADCH): read " + hex(super.read()));
                }
                return super.read();
            }
        }

        private class Low extends RWRegister {
            // TODO: there should be no need for a special class to print out debugging information
            public byte read() {
                if (devicePrinter.enabled) {
                    devicePrinter.println("ADC (ADCL): read " + hex(super.read()));
                }
                return super.read();
            }

            public void write(byte val) {
                super.write(val);
                if (devicePrinter.enabled) {
                    devicePrinter.println("ADC (ADCL): " + hex(value));
                }

            }
        }

    }

    static final int[] PRESCALER = {2, 2, 4, 8, 16, 32, 64, 128};

    /**
     * <code>ControlRegister</code> defines the behavior of the ADC control register,
     */
    protected class ControlRegister extends ADCRegister {

        static final int ADEN = 7;
        static final int ADSC = 6;
        static final int ADFR = 5;
        static final int ADIF = 4;
        static final int ADIE = 3;
        static final int ADPS2 = 2;
        static final int ADPS1 = 1;
        static final int ADPS0 = 0;

        boolean aden;
        boolean adsc;
        boolean adfr;
        boolean adif;
        boolean adie;

        int prescalerDivider = 2;

        final ControlRegister.Conversion conversion;

        byte oldVal;

        ControlRegister() {
            conversion = new ControlRegister.Conversion();
            installInterrupt("ADC", 22, new ControlRegister.ADCInterrupt());
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
                mainClock.insertEvent(conversion, prescalerDivider * 13);
            }

            oldVal = val;

        }

        protected void printStatus() {
            devicePrinter.println("ADC (ADCSRA): enable: " + aden + ", start conversion: " + adsc +
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

                if (devicePrinter.enabled) {
                    devicePrinter.println("ADC: Conversion complete.");
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
                if ( adie ) {
                    writeBit(ADIF, true);
                    interpreter.postInterrupt(22);
                }
            }

            public void fire() {
                interpreter.unpostInterrupt(22);
                writeBit(ADIF, false);
                firing = false;
            }

        }
    }
}