package avrora.stack;

import vpc.VPCBase;
import avrora.sir.Register;

/**
 * The <code>AbstractState</code> class represents a state in the
 * analysis of the stack. It tracks known and unknown bits through
 * the general purpose registers and a couple of IO registers that
 * control the interrupt behavior.
 *
 * @author Ben L. Titzer
 */
public class AbstractState {

    private static final char KNOWN_MASK = 0xFF00;
    private static final char BIT_MASK = 0x00FF;
    private static final int SHIFT = 8;

    public static final int NUM_REGS = 32;
    public static final char ZERO = KNOWN_MASK;
    public static final char UNKNOWN = 0;

    private int pc;
    private char SREG;   // canonical status register value
    private char regs[]; // canonical register values

    public static final int primes[] = {
        3,     5,   7,  11,  13,  17,  19,  23,  29,  31,
        37,   41,  43,  47,  53,  59,  61,  67,  71,  73,
        79,   83,  89,  97, 101, 103, 107, 109, 113, 127,
        131, 137, 139, 149, 151, 157, 163, 167, 173, 179,
        181, 191
    };

    public AbstractState() {
        regs = new char[NUM_REGS];
        SREG = ZERO;
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ ) {
            regs[cntr] = ZERO;
        }
    }

    public int hashCode() {
        int cumul = pc;
        cumul += SREG;
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ )
            cumul += regs[cntr] * primes[cntr];
        return cumul;
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

    public char readRegister(Register r) {
        return regs[r.getNumber()];
    }

    public void writeRegister(Register r, char val) {
        regs[r.getNumber()] = canon(val);
    }

    public static char merge(char val1, char val2) {
        if ( val1 == val2 ) return val1;

        char v1k = maskOf(val1); // known mask of val1
        char v2k = maskOf(val2); // known mask of val2

        int mm = ~(bitsOf(val1) ^ bitsOf(val2)); // matched bits
        int rk = v1k & v2k & mm; // known bits of result

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
        return (char)(vk | (val & (vk >> SHIFT)));
    }

    public static char knownVal(byte val) {
        return canon((char)(KNOWN_MASK >> SHIFT), (char)(val & 0xff));
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
}
