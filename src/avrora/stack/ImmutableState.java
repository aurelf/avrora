
package avrora.stack;

import avrora.core.Register;
import avrora.sim.IORegisterConstants;

/**
 *
 */
public class ImmutableState implements IORegisterConstants, AbstractState {

    private final int pc;
    private final char SREG;   // canonical status register value
    private final char regs[]; // canonical register values

    private final int hashCode;

    /**
     * The <code>primes</code> field stores the first 32 prime integers
     * that follow 2. This is used in the computation of the hash code.
     */
    public static final int primes[] = {
        3,     5,   7,  11,  13,  17,  19,  23,  29,  31,
        37,   41,  43,  47,  53,  59,  61,  67,  71,  73,
        79,   83,  89,  97, 101, 103, 107, 109, 113, 127,
        131, 137, 139, 149, 151, 157, 163, 167, 173, 179,
        181, 191
    };


    ImmutableState(MutableState s) {
        pc = s.getPC();
        SREG = s.getSREG();
        regs = new char[NUM_REGS];
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ ) {
            regs[cntr] = s.getRegisterAV(cntr);
        }
        hashCode = computeHashCode();
    }

    private int computeHashCode() {
        int hash = pc;
        hash += SREG;
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ )
            hash += regs[cntr] * primes[cntr];
        return hash;
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof ImmutableState) ) return false;
        ImmutableState i = (ImmutableState)o;
        if ( this.pc != i.pc ) return false;
        if ( this.SREG != i.SREG ) return false;
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ )
            if ( this.regs[cntr] != i.regs[cntr] ) return false;
        return true;
    }

    /**
     * The <code>getPC()</code> method returns the concrete value of the program counter.
     * The program counter is known in every abstract state.
     * @return the concrete value of the program counter
     */
    public int getPC() {
        return pc;
    }

    /**
     * The <code>getSREG()</code> method reads the abstract value of the status register.
     * @return the abstract value of the status register
     */
    public char getSREG() {
        return SREG;
    }

    /**
     * The <code>getFlag_I()</code> method returns the abstract value of the I flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_I() { return AbstractArithmetic.getBit(SREG, SREG_I); }

    /**
     * The <code>getFlag_T()</code> method returns the abstract value of the T flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_T() { return AbstractArithmetic.getBit(SREG, SREG_T); }

    /**
     * The <code>getFlag_H()</code> method returns the abstract value of the H flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_H() { return AbstractArithmetic.getBit(SREG, SREG_H); }

    /**
     * The <code>getFlag_S()</code> method returns the abstract value of the S flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_S() { return AbstractArithmetic.getBit(SREG, SREG_S); }

    /**
     * The <code>getFlag_V()</code> method returns the abstract value of the V flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_V() { return AbstractArithmetic.getBit(SREG, SREG_V); }

    /**
     * The <code>getFlag_N()</code> method returns the abstract value of the N flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_N() { return AbstractArithmetic.getBit(SREG, SREG_N); }

    /**
     * The <code>getFlag_Z()</code> method returns the abstract value of the Z flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_Z() { return AbstractArithmetic.getBit(SREG, SREG_Z); }

    /**
     * The <code>getFlag_C()</code> method returns the abstract value of the C flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_C() { return AbstractArithmetic.getBit(SREG, SREG_C); }


    /**
     * The <code>getIORegisterByte()</code> method reads the abstract value of an
     * IO register from the abstract state. For those registers being modelled,
     * this will return an abstract value that represents the current value of
     * the IO register. For IO registers that are not being modelled, it will
     * return the abstract value corresponding to all bits being unknown.
     * @param num the IO register number to read
     * @return the (abstract) value of the specified IO register
     */
    public char getIORegisterAV(int num) {
        if ( num == IORegisterConstants.SREG ) return SREG;
        return AbstractArithmetic.UNKNOWN;
    }

    /**
     * The <code>getRegisterByte()</code> method reads the abstract value of a
     * register in the abstract state.
     * @param r the register to read
     * @return the abstract value of the register
     */
    public char getRegisterAV(Register r) {
        return regs[r.getNumber()];
    }

    public MutableState copy() {
        return new MutableState(pc, SREG, regs);
    }
}
