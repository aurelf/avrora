package avrora.stack;

import vpc.VPCBase;
import avrora.core.Register;
import avrora.sim.IORegisterConstants;

/**
 * The <code>AbstractState</code> class represents a state in the
 * analysis of the stack. It tracks known and unknown bits through
 * the general purpose registers and a couple of IO registers that
 * control the interrupt behavior.
 *
 * @author Ben L. Titzer
 */
public class AbstractState implements IORegisterConstants {

    public static final int  NUM_REGS = 32;

    private int pc;
    private char SREG;   // canonical status register value
    private char regs[]; // canonical register values

    private int hashCode;

    public static final int primes[] = {
        3,     5,   7,  11,  13,  17,  19,  23,  29,  31,
        37,   41,  43,  47,  53,  59,  61,  67,  71,  73,
        79,   83,  89,  97, 101, 103, 107, 109, 113, 127,
        131, 137, 139, 149, 151, 157, 163, 167, 173, 179,
        181, 191
    };

    public AbstractState() {
        regs = new char[NUM_REGS];
        SREG = AbstractArithmetic.ZERO;
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ ) {
            regs[cntr] = AbstractArithmetic.ZERO;
        }
    }

    private AbstractState(int npc, char nSREG, char[] nregs) {
        regs = new char[NUM_REGS];
        System.arraycopy(nregs, 0, regs, 0, NUM_REGS);
        pc = npc;
        SREG = nSREG;
    }

    public AbstractState copy() {
        return new AbstractState(pc, SREG, regs);
    }

    public int hashCode() {
        if ( hashCode == 0 ) computeHashCode();
        return hashCode;
    }

    private void computeHashCode() {
        hashCode = pc;
        hashCode += SREG;
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ )
            hashCode += regs[cntr] * primes[cntr];
    }

    public boolean equals(Object o) {
        if ( o == this ) return true;
        if ( !(o instanceof AbstractState) ) return false;
        AbstractState as = (AbstractState)o;

        if ( pc != as.pc ) return false;
        if ( SREG != as.SREG ) return false;

        for ( int cntr = 0; cntr < NUM_REGS; cntr++ ) {
            if ( regs[cntr] != as.regs[cntr] ) return false;
        }

        return true;
    }

    public int getPC() {
        return pc;
    }

    public void setPC(int npc) {
        pc = npc;
    }

    public char readSREG() {
        return SREG;
    }

    public void writeSREG(char val) {
        SREG = AbstractArithmetic.canon(val);
    }

    public void setSREG_bit(int bit, char val) {
        SREG = (char)((SREG & ~(AbstractArithmetic.ON << bit)) | (val << bit));
    }

    public void setFlag_I(char val) { setSREG_bit(SREG_I, val); }
    public void setFlag_T(char val) { setSREG_bit(SREG_T, val); }
    public void setFlag_H(char val) { setSREG_bit(SREG_H, val); }
    public void setFlag_S(char val) { setSREG_bit(SREG_S, val); }
    public void setFlag_V(char val) { setSREG_bit(SREG_V, val); }
    public void setFlag_N(char val) { setSREG_bit(SREG_N, val); }
    public void setFlag_Z(char val) { setSREG_bit(SREG_Z, val); }
    public void setFlag_C(char val) { setSREG_bit(SREG_C, val); }

    public char getFlag_I() { return AbstractArithmetic.getBit(SREG, SREG_I); }
    public char getFlag_T() { return AbstractArithmetic.getBit(SREG, SREG_T); }
    public char getFlag_H() { return AbstractArithmetic.getBit(SREG, SREG_H); }
    public char getFlag_S() { return AbstractArithmetic.getBit(SREG, SREG_S); }
    public char getFlag_V() { return AbstractArithmetic.getBit(SREG, SREG_V); }
    public char getFlag_N() { return AbstractArithmetic.getBit(SREG, SREG_N); }
    public char getFlag_Z() { return AbstractArithmetic.getBit(SREG, SREG_Z); }
    public char getFlag_C() { return AbstractArithmetic.getBit(SREG, SREG_C); }

    public char readIORegister(int num) {
        // TODO: read correct IO registers
        return AbstractArithmetic.UNKNOWN;
    }

    public void writeIORegister(int num, char val) {
        // TODO: write known IO registers
    }

    public char readRegister(Register r) {
        return regs[r.getNumber()];
    }

    public void writeRegister(Register r, char val) {
        regs[r.getNumber()] = AbstractArithmetic.canon(val);
    }

}
