package avrora.stack;

import vpc.VPCBase;
import avrora.sir.Register;

/**
 * The <code>AbstractState</code> class represents a state in the
 * analysis of the stack. It tracks known and unknown bits through
 * the general purpose registers and a couple of IO registers that
 * control the interrupt behavior.
 *
 * The abstract values (e.g. register values) are represented as
 * characters. Thus, an 8 bit register is modelled using a 16-bit
 * character. The upper 8 bits represent the "mask", those bits
 * which are known. The lower 8 bits represent the known bits
 * of the value. Thus, if bit(regs[R], i+8) is set, then bit(R, i)
 * is known and its value is bit(regs[R], i). If bit(regs[R], i+8)
 * is clear, then the value of bit(regs[R], i) is unknown in
 * this abstract value.
 *
 * Since there are 3 possible states (on, off, unknown) for each
 * bit in the abstract state and there are two bits reserved for
 * representing each of these states, there are 4 bit states
 * and 3 abstract states. We canonicalize the values when the
 * bit value is unknown, i.e. when the known mask bit is clear,
 * then the value bit is clear as well. This makes comparison
 * of canonical abstract values the same as character equality.
 * All abstract values stored within <code>AbstractState</code>
 * are canonical for efficiency and clarity.
 *
 * @author Ben L. Titzer
 */
public class AbstractState {

    private static final char KNOWN_MASK = 0xFF00;
    private static final char BIT_MASK = 0x00FF;
    private static final int SHIFT = 8;

    public static final int NUM_REGS = 32;
    public static final char ZERO = KNOWN_MASK;
    public static final char ON = 0x101;
    public static final char OFF = 0x100;
    public static final char UNKNOWN = 0;

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

    public static final int FLAG_I = 7;
    public static final int FLAG_T = 6;
    public static final int FLAG_H = 5;
    public static final int FLAG_S = 4;
    public static final int FLAG_V = 3;
    public static final int FLAG_N = 2;
    public static final int FLAG_Z = 1;
    public static final int FLAG_C = 0;


    public AbstractState() {
        regs = new char[NUM_REGS];
        SREG = ZERO;
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ ) {
            regs[cntr] = ZERO;
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
        SREG = canon(val);
    }

    public void setSREG_bit(int bit, char val) {
        SREG = canon((char)(SREG ^ getBit(val, bit)));
    }

    public void setFlag_I(char val) { setSREG_bit(FLAG_I, val); }
    public void setFlag_T(char val) { setSREG_bit(FLAG_T, val); }
    public void setFlag_H(char val) { setSREG_bit(FLAG_H, val); }
    public void setFlag_S(char val) { setSREG_bit(FLAG_S, val); }
    public void setFlag_V(char val) { setSREG_bit(FLAG_V, val); }
    public void setFlag_N(char val) { setSREG_bit(FLAG_N, val); }
    public void setFlag_Z(char val) { setSREG_bit(FLAG_Z, val); }
    public void setFlag_C(char val) { setSREG_bit(FLAG_C, val); }

    public char getFlag_I() { return getBit(SREG, FLAG_I); }
    public char getFlag_T() { return getBit(SREG, FLAG_T); }
    public char getFlag_H() { return getBit(SREG, FLAG_H); }
    public char getFlag_S() { return getBit(SREG, FLAG_S); }
    public char getFlag_V() { return getBit(SREG, FLAG_V); }
    public char getFlag_N() { return getBit(SREG, FLAG_N); }
    public char getFlag_Z() { return getBit(SREG, FLAG_Z); }
    public char getFlag_C() { return getBit(SREG, FLAG_C); }

    public char readIORegister(int num) {
        // TODO: read correct IO registers
        return UNKNOWN;
    }

    public void writeIORegister(int num, char val) {
        // TODO: write known IO registers
    }

    public char readRegister(Register r) {
        return regs[r.getNumber()];
    }

    public void writeRegister(Register r, char val) {
        regs[r.getNumber()] = canon(val);
    }

    /**
     *  O P E R A T I O N S   O N   A B S T R A C T   V A L U E S
     * -----------------------------------------------------------------
     *
     *    Abstract values are represented as characters. These utility
     * functions allow operations on abstract values to be expressed
     * more clearly.
     *
     */

    public static char merge(char val1, char val2) {
        if ( val1 == val2 ) return val1;

        char v1k = maskOf(val1); // known mask of val1
        char v2k = maskOf(val2); // known mask of val2

        int mm = ~(bitsOf(val1) ^ bitsOf(val2)); // matched bits
        int rk = v1k & v2k & mm & 0xff; // known bits of result

        return canon((char)rk, val1);
    }

    public static boolean isUnknown(char val) {
        return (val & KNOWN_MASK) != KNOWN_MASK;
    }

    public static boolean areEqual(char val1, char val2) {
        if ( val1 == val2 ) return true;
        if ( canon(val1) == canon(val2) ) return true;
        return false;
    }

    public static char canon(char val) {
        char vk = maskOf(val);
        return (char)(vk | (val & (vk >> SHIFT)));
    }

    public static char canon(char vk, char val) {
        return (char)((vk << SHIFT) | (val & vk));
    }

    public static char knownVal(byte val) {
        return (char)(KNOWN_MASK | (val & 0xff));
    }

    public static byte knownBitsOf(char c) {
        return (byte)(((c & KNOWN_MASK) >> SHIFT) & c);
    }

    public static char bitsOf(char c) {
        return (char)(c & BIT_MASK);
    }

    public static char maskOf(char c) {
        return (char)((c & KNOWN_MASK) >> SHIFT);
    }

    public static char getBit(char val, int bit) {
        int mask = 0x101 << bit;
        return (char)(val & mask);
    }

    public static char commonMask(char c, char d) {
        return (char)(maskOf(c) & maskOf(d));
    }
}
