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


    public int getRamSize() {
        return SRAM_SIZE;
    }

    public int getIORegSize() {
        return IOREG_SIZE;
    }

    public int getFlashSize() {
        return FLASH_SIZE;
    }

    public int getEEPromSize() {
        return EEPROM_SIZE;
    }

    public boolean isSupported(InstrPrototype i) {
        return true;
    }

    public Simulator loadProgram(Program p) {
        return new SimImpl(p);
    }

    public int getHz() {
        return HZ;
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

            ns.setIORegister(EIMSK, EIMSK_reg = new EIMSK_class());
            ns.setIORegister(EIFR, EIFR_reg = new EIFR_class());
        }

    }
}
