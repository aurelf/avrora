package avrora.core;

import vpc.VPCBase;
import avrora.Avrora;

/**
 * @author Ben L. Titzer
 */
public class Data extends Elem {

    public byte value;

    public Data(byte val) {
        this.value = val;
    }

    public boolean isData() {
        return true;
    }

    public Instr asInstr(int address) {
        // TODO: define correct error for this.
        throw Avrora.failure("not an instruction @ " + address);
    }

    public Data asData(int address) {
        return this;
    }
}
