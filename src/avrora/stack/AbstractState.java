package avrora.stack;

import vpc.VPCBase;
import avrora.core.Register;
import avrora.sim.IORegisterConstants;

/**
 * The <code>AbstractState</code> class represents the abstract state of
 * the processor. It tracks known and unknown bits through
 * the general purpose registers and a couple of IO registers that
 * control the interrupt behavior.
 *
 * @see AbstractArithmetic
 * @see StateSpace
 * @author Ben L. Titzer
 */
public class AbstractState implements IORegisterConstants {

    public static final int NUM_REGS = 32;

    private int pc;
    private char SREG;   // canonical status register value
    private char regs[]; // canonical register values

    private int hashCode;

    /**
     * The <code>primes</code> field stores the first 32 prime integers.
     * This is used in the computation of the hash code for this state.
     */
    public static final int primes[] = {
        3,     5,   7,  11,  13,  17,  19,  23,  29,  31,
        37,   41,  43,  47,  53,  59,  61,  67,  71,  73,
        79,   83,  89,  97, 101, 103, 107, 109, 113, 127,
        131, 137, 139, 149, 151, 157, 163, 167, 173, 179,
        181, 191
    };

    /**
     * The constructor of the <code>AbstractState</code> class builds the
     * default values of each of the registers and each IO register that
     * is being modelled. The default is that all registers are known zero,
     * all IO registers are known zero, and the program counter is zero.
     */
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

    /**
     * The <code>copy()</code> method returns a deep copy of this state. This is
     * generally used for forking operations and for storing internal copies within
     * the <code>StateSpace</code>.
     * @return a new deep copy of this abstract state
     */
    public AbstractState copy() {
        return new AbstractState(pc, SREG, regs);
    }

    /**
     * The <code>merge()</code> method merges this abstract state with another abstract
     * state and returns a new copy. This abstract state is not update. The operation is
     * a simple pointwise merging operation: each value is merged with its corresponding
     * abstract value in the other abstract state.
     *
     * @see AbstractArithmetic
     * @param s the abstract state to merge with
     * @return a new abstract state that represents the merged abstract states
     */
    public AbstractState merge(AbstractState s) {

        if ( pc != s.pc )
            throw VPCBase.failure("cannot merge abstract states with different program counters");

        AbstractState n = copy();

        n.SREG = AbstractArithmetic.merge(this.SREG, s.SREG);
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ )
            n.regs[cntr] = AbstractArithmetic.merge(this.regs[cntr], s.regs[cntr]);

        return n;
    }

    /**
     * The <code>hashCode()</code> method computes an integer hash code for this
     * state. A good hash code is needed to make hashtables in <code>StateSpace</code>
     * efficient.  
     * @return the hash code of this object
     */
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

    /**
     * The <code>equals()</code> method implements the standard <code>java.lang.Object</code>
     * equality testing contract. First, reference equality is used, and then, a deep compare.
     * @param o the object to test equality against.
     * @return true if these two abstract states are identical
     * @return false otherwise
     */
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

    /**
     * The <code>getPC()</code> method returns the concrete value of the program counter.
     * The program counter is known in every abstract state.
     * @return the concrete value of the program counter
     */
    public int getPC() {
        return pc;
    }

    /**
     * The <code>setPC()</code> method updates the concrete value of the program counter.
     * The program counter is known in ever abstract state.
     * @param npc the new concrete value of the program counter
     */
    public void setPC(int npc) {
        pc = npc;
    }

    /**
     * The <code>readSREG()</code> method reads the abstract value of the status register.
     * @return the abstract value of the status register
     */
    public char readSREG() {
        return SREG;
    }

    /**
     * The <code>writeSREG</code> method updates the abstract value of the status register.
     * @param val the new abstract value to write to the status register.
     */
    public void writeSREG(char val) {
        SREG = AbstractArithmetic.canon(val);
    }

    /**
     * The <code>writeSREG</code> method updates one bit of the abstract value of the
     * status register.
     * @param val the new abstract value of the bit
     */
    public void setSREG_bit(int bit, char val) {
        SREG = AbstractArithmetic.setBit(SREG, bit, val);
    }

    /**
     * The <code>setFlag_I()</code> method updates the abstract value of the I flag.
     * @param val the new abstract bit of the flag
     */
    public void setFlag_I(char val) { setSREG_bit(SREG_I, val); }

    /**
     * The <code>setFlag_T()</code> method updates the abstract value of the T flag.
     * @param val the new abstract bit of the flag
     */
    public void setFlag_T(char val) { setSREG_bit(SREG_T, val); }

    /**
     * The <code>setFlag_H()</code> method updates the abstract value of the H flag.
     * @param val the new abstract bit of the flag
     */
    public void setFlag_H(char val) { setSREG_bit(SREG_H, val); }

    /**
     * The <code>setFlag_S()</code> method updates the abstract value of the S flag.
     * @param val the new abstract bit of the flag
     */
    public void setFlag_S(char val) { setSREG_bit(SREG_S, val); }

    /**
     * The <code>setFlag_V()</code> method updates the abstract value of the V flag.
     * @param val the new abstract bit of the flag
     */
    public void setFlag_V(char val) { setSREG_bit(SREG_V, val); }

    /**
     * The <code>setFlag_N()</code> method updates the abstract value of the N flag.
     * @param val the new abstract bit of the flag
     */
    public void setFlag_N(char val) { setSREG_bit(SREG_N, val); }

    /**
     * The <code>setFlag_Z()</code> method updates the abstract value of the Z flag.
     * @param val the new abstract bit of the flag
     */
    public void setFlag_Z(char val) { setSREG_bit(SREG_Z, val); }

    /**
     * The <code>setFlag_C()</code> method updates the abstract value of the C flag.
     * @param val the new abstract bit of the flag
     */
    public void setFlag_C(char val) { setSREG_bit(SREG_C, val); }

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
     * The <code>readIORegister()</code> method reads the abstract value of an
     * IO register from the abstract state. For those registers being modelled,
     * this will return an abstract value that represents the current value of
     * the IO register. For IO registers that are not being modelled, it will
     * return the abstract value corresponding to all bits being unknown.
     * @param num the IO register number to read
     * @return the (abstract) value of the specified IO register
     */
    public char readIORegister(int num) {
        // TODO: read correct IO registers
        return AbstractArithmetic.UNKNOWN;
    }

    /**
     * The <code>writeIORegister()</code> method writes the abstract value of
     * an IO register. If the register is being modelled, then its value will be
     * updated. Otherwise, the write will be ignored.
     * @param num the IO register number to be updated
     * @param val the new abstract value of the IO register
     */
    public void writeIORegister(int num, char val) {
        // TODO: write known IO registers
    }

    /**
     * The <code>readRegister()</code> method reads the abstract value of a
     * register in the abstract state.
     * @param r the register to read
     * @return the abstract value of the register
     */
    public char readRegister(Register r) {
        return regs[r.getNumber()];
    }

    /**
     * The <code>writeRegister()</code> method writes the abstract value of
     * a register in the abstract state
     * @param r the register to write
     * @param val the new abstract value of the register
     */
    public void writeRegister(Register r, char val) {
        regs[r.getNumber()] = AbstractArithmetic.canon(val);
    }

}
