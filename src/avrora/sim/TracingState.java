package avrora.sim;

import avrora.sir.Program;
import avrora.sir.Register;

/**
 * The <code>TracingState</code> class is an extended version of the <code>State</code>
 * class that records which portions of the state have been modified over a series
 * of updates.
 * @author Ben L. Titzer
 */
public class TracingState extends State {

    TracingState(Program p, int fsize, int isize, int ssize) {
        super(p, fsize, isize, ssize);
    }

    public void writeRegister(Register r1, byte val) {
        super.writeRegister(r1, val);
        reg_delta[r1.getNumber()] = true;
    }

    public void writeSP(int address) {
        super.writeSP(address);
        sp_delta = true;
    }

    public void writePC(int address) {
        super.writePC(address);
        pc_delta = true;
    }

    public byte popByte() {
        byte val = super.popByte();
        sp_delta = true;
        return val;
    }

    public void pushByte(byte val) {
        super.pushByte(val);
        sp_delta = true;
    }

    public void writeSREG(byte val) {
        super.writeSREG(val);
    }

    public void setFlag_I(boolean val) {
        super.setFlag_I(val);
        flag_delta[FLAG_I] = true;
    }

    public void setFlag_T(boolean val) {
        super.setFlag_T(val);
        flag_delta[FLAG_T] = true;
    }

    public void setFlag_H(boolean val) {
        super.setFlag_H(val);
        flag_delta[FLAG_H] = true;
    }

    public void setFlag_S(boolean val) {
        super.setFlag_S(val);
        flag_delta[FLAG_S] = true;
    }

    public void setFlag_V(boolean val) {
        super.setFlag_V(val);
        flag_delta[FLAG_V] = true;
    }

    public void setFlag_N(boolean val) {
        super.setFlag_N(val);
        flag_delta[FLAG_N] = true;
    }

    public void setFlag_Z(boolean val) {
        super.setFlag_Z(val);
        flag_delta[FLAG_Z] = true;
    }

    public void setFlag_C(boolean val) {
        super.setFlag_C(val);
        flag_delta[FLAG_C] = true;
    }

    public void clearTracingState() {
        for (int cntr = 0; cntr < 32; cntr++)
            reg_delta[cntr] = false;

        for (int cntr = 0; cntr < 8; cntr++)
            flag_delta[cntr] = false;

        sp_delta = false;
        pc_delta = false;
    }
}
