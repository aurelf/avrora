
package avrora.stack;

import avrora.core.Register;
import avrora.sim.IORegisterConstants;
import vpc.util.StringUtil;
import vpc.VPCBase;

/**
 * The <code>AbstractState</code> class represents an abstract state within
 * the state space. The program counter, the status register, the registers,
 * and the interrupt mask register are modelled.
 * @author Ben L. Titzer
 */
public abstract class AbstractState implements IORegisterConstants {
    protected int pc;
    protected char av_SREG;   // canonical status register value
    protected char av_EIFR;   // canonical interrupt flag register value
    protected char av_EIMSK;  // canonical interrupt mask register value
    protected char av_REGISTERS[]; // canonical register values

    /**
     * The <code>primes</code> field stores the first 32 prime integers
     * that follow 2. This is used in the computation of the hash code.
     */
    public static final int primes[] = {
        3,     5,   7,  11,  13,  17,  19,  23,  29,  31,
        37,   41,  43,  47,  53,  59,  61,  67,  71,  73,
        79,   83,  89,  97, 101, 103, 107, 109, 113, 127,
        131, 137 
    };


    AbstractState() {
        // default is everything is unknown!
        av_REGISTERS = new char[NUM_REGS];
    }

    protected int computeHashCode() {
        int hash = pc;
        hash += av_SREG;
        hash += av_EIFR;
        hash += av_EIMSK;
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ )
            hash += av_REGISTERS[cntr] * primes[cntr];
        return hash;
    }

    public abstract int hashCode();
    public abstract boolean equals(Object o);

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
        return av_SREG;
    }

    /**
     * The <code>getFlag_I()</code> method returns the abstract value of the I flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_I() { return AbstractArithmetic.getBit(av_SREG, SREG_I); }

    /**
     * The <code>getFlag_T()</code> method returns the abstract value of the T flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_T() { return AbstractArithmetic.getBit(av_SREG, SREG_T); }

    /**
     * The <code>getFlag_H()</code> method returns the abstract value of the H flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_H() { return AbstractArithmetic.getBit(av_SREG, SREG_H); }

    /**
     * The <code>getFlag_S()</code> method returns the abstract value of the S flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_S() { return AbstractArithmetic.getBit(av_SREG, SREG_S); }

    /**
     * The <code>getFlag_V()</code> method returns the abstract value of the V flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_V() { return AbstractArithmetic.getBit(av_SREG, SREG_V); }

    /**
     * The <code>getFlag_N()</code> method returns the abstract value of the N flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_N() { return AbstractArithmetic.getBit(av_SREG, SREG_N); }

    /**
     * The <code>getFlag_Z()</code> method returns the abstract value of the Z flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_Z() { return AbstractArithmetic.getBit(av_SREG, SREG_Z); }

    /**
     * The <code>getFlag_C()</code> method returns the abstract value of the C flag.
     * @return the new abstract bit of the flag
     */
    public char getFlag_C() { return AbstractArithmetic.getBit(av_SREG, SREG_C); }


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
        if ( num == IORegisterConstants.SREG ) return av_SREG;
        if ( num == IORegisterConstants.EIMSK ) return av_EIMSK;
        if ( num == IORegisterConstants.EIFR ) return av_EIFR;
        return AbstractArithmetic.UNKNOWN;
    }

    /**
     * The <code>getRegisterByte()</code> method reads the abstract value of a
     * register in the abstract state.
     * @param r the register to read
     * @return the abstract value of the register
     */
    public char getRegisterAV(Register r) {
        return av_REGISTERS[r.getNumber()];
    }

    public char getRegisterAV(int num) {
        return av_REGISTERS[num];
    }

    public MutableState copy() {
        return new MutableState(pc, av_SREG, av_REGISTERS);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("PC: ");
        buf.append(VPCBase.toHex(pc, 4));

        //ITHSVNZC
        appendBit('I', getFlag_I(), buf);
        appendBit('T', getFlag_T(), buf);
        appendBit('H', getFlag_H(), buf);
        appendBit('S', getFlag_S(), buf);
        appendBit('V', getFlag_V(), buf);
        appendBit('N', getFlag_N(), buf);
        appendBit('Z', getFlag_Z(), buf);
        appendBit('C', getFlag_C(), buf);

        for ( int cntr = 0; cntr < NUM_REGS; cntr++ ) {
            buf.append(" R");
            buf.append(cntr);
            buf.append(": ");
            AbstractArithmetic.toString(av_REGISTERS[cntr], buf);
        }

        return buf.toString();
    }

    public String toShortString() {
        StringBuffer buf = new StringBuffer();
        buf.append(VPCBase.toHex(pc, 4));
        buf.append(' ');

        //ITHSVNZC
        buf.append(AbstractArithmetic.toString(av_SREG));

        for ( int cntr = 0; cntr < NUM_REGS; cntr++ ) {
            buf.append(' ');
            AbstractArithmetic.toString(av_REGISTERS[cntr], buf);
        }

        return buf.toString();
    }

    static String headerString;

    public static String getHeaderString() {
        if ( headerString != null ) return headerString;

        StringBuffer buf = new StringBuffer();
        buf.append("PC   ");
        buf.append("ITHSVNZC");
        for ( int cntr = 0; cntr < NUM_REGS; cntr++ ) {
            buf.append(' ');
            buf.append(StringUtil.leftJustify("R"+cntr, 8));
        }

        return buf.toString();
    }


    private void appendBit(char bit, char av1, StringBuffer buf) {
        buf.append(' ');
        buf.append(bit);
        buf.append(": ");
        buf.append(AbstractArithmetic.bitToChar(av1));
    }
}
