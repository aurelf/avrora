package avrora.sim;

import avrora.core.InstrPrototype;
import avrora.core.Program;
import avrora.Arithmetic;

/**
 * @author Ben L. Titzer
 */
public class ATMega128L implements AbstractProcessor {

    public static final int HZ = 16000000;

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
     * Atmega128L runs at 16MHz, so this method will return 16,000,000.
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
            super(p);
        }

        public static final int RESET_VECT = 1;
        public static final int EXT_VECT = 2;

        protected EIFR_class EIFR_reg;
        protected EIMSK_class EIMSK_reg;

        protected State.RWIOReg TIMSK_reg;
        protected State.RWIOReg TIFR_reg;

        abstract class IMRReg extends State.RWIOReg {

            public void update() {
                int posted = EIMSK_reg.value & EIFR_reg.value;
                long previousPosted = state.getPostedInterrupts() & ~(0xff << EXT_VECT);
                long newPosted = previousPosted | (posted << EXT_VECT);
                state.setPostedInterrupts(newPosted);
            }

            public void update(int bit) {
                int posted = EIMSK_reg.value & EIFR_reg.value & (1 << bit);
                if ( posted != 0 ) state.postInterrupt(EXT_VECT + bit);
                else state.unpostInterrupt(EXT_VECT + bit);
            }
        }

        class EIFR_class extends IMRReg {

            public void write(byte val) {
                value = (byte)(value & ~val);
                update();
            }

            public void setBit(int bit) {
                value = Arithmetic.clearBit(value, bit);
                state.unpostInterrupt(EXT_VECT + bit);
            }

            public void clearBit(int bit) {
                // clearing a bit does nothing.
            }
        }

        class EIMSK_class extends IMRReg {
            public void write(byte val) {
                value = val;
                update();
            }

            public void setBit(int bit) {
                value = Arithmetic.setBit(value, bit);
                update(bit);
            }

            public void clearBit(int bit) {
                value = Arithmetic.clearBit(value, bit);
                state.unpostInterrupt(EXT_VECT + bit);
            }
        }

        class Timer8 {
            State.RWIOReg COUNT_reg;
            State.RWIOReg OUTPUT_reg;
            State.RWIOReg CONTROL_reg;

            int overflowVector;
            int compareMatchVector;

            void tick(int cycles) {
                
            }
        }

        protected State constructState() {
            State ns = new State(program, FLASH_SIZE, IOREG_SIZE, SRAM_SIZE);
            setupState(ns);
            return ns;
        }

        protected State constructTracingState() {
            State ns = new TracingState(program, FLASH_SIZE, IOREG_SIZE, SRAM_SIZE);
            setupState(ns);
            return ns;

        }

        protected void setupState(State ns) {
            // external interrupts, maskable, sticky
            interrupts[2] = new MaskableInterrupt(2, EIMSK, EIFR, 0, true);
            interrupts[3] = new MaskableInterrupt(3, EIMSK, EIFR, 1, true);
            interrupts[4] = new MaskableInterrupt(4, EIMSK, EIFR, 2, true);
            interrupts[5] = new MaskableInterrupt(5, EIMSK, EIFR, 3, true);
            interrupts[6] = new MaskableInterrupt(6, EIMSK, EIFR, 4, true);
            interrupts[7] = new MaskableInterrupt(7, EIMSK, EIFR, 5, true);
            interrupts[8] = new MaskableInterrupt(8, EIMSK, EIFR, 6, true);
            interrupts[9] = new MaskableInterrupt(9, EIMSK, EIFR, 7, true);

            ns.setIOReg(EIMSK, EIMSK_reg = new EIMSK_class());
            ns.setIOReg(EIFR, EIFR_reg = new EIFR_class());
        }

    }
}
