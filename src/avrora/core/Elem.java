package avrora.core;

import vpc.VPCBase;
import avrora.Avrora;

/**
 * @author Ben L. Titzer
 */
public abstract class Elem {

    public static final Elem UNINIT = new Elem() {
        public boolean isInitialized() {
            return false;
        }

        public boolean isData() {
            return true;
        }

        public Data asData(int address) {
            return new Data((byte) 0);
        }

        public Instr asInstr(int address) {
            // TODO: define correct error for this.
            throw Avrora.failure("not an instruction @ " + address);
        }
    };

    public static final Elem INSTR_MIDDLE = new Elem() {
        public boolean isInstr() {
            return true;
        }

        public Data asData(int address) {
            // TODO: define correct error for this.
            throw Avrora.failure("not data @ " + address);
        }

        public Instr asInstr(int address) {
            // TODO: define correct error for this.
            throw Avrora.failure("not data @ " + address);
        }
    };

    public abstract Data asData(int address);

    public abstract Instr asInstr(int address);

    public boolean isInitialized() {
        return true;
    }

    public boolean isData() {
        return false;
    }

    public boolean isInstr() {
        return false;
    }
}
