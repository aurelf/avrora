
package avrora.stack;

import avrora.core.Register;

/**
 * @author Ben L. Titzer
 */
public interface AbstractState {
    /**
     * The <code>getPC()</code> method returns the concrete value of the program counter.
     * The program counter is known in every abstract state.
     * @return the concrete value of the program counter
     */
    int getPC();

    /**
     * The <code>getSREG()</code> method reads the abstract value of the status register.
     * @return the abstract value of the status register
     */
    char getSREG();

    /**
     * The <code>getFlag_I()</code> method returns the abstract value of the I flag.
     * @return the new abstract bit of the flag
     */
    char getFlag_I();

    /**
     * The <code>getFlag_T()</code> method returns the abstract value of the T flag.
     * @return the new abstract bit of the flag
     */
    char getFlag_T();

    /**
     * The <code>getFlag_H()</code> method returns the abstract value of the H flag.
     * @return the new abstract bit of the flag
     */
    char getFlag_H();

    /**
     * The <code>getFlag_S()</code> method returns the abstract value of the S flag.
     * @return the new abstract bit of the flag
     */
    char getFlag_S();

    /**
     * The <code>getFlag_V()</code> method returns the abstract value of the V flag.
     * @return the new abstract bit of the flag
     */
    char getFlag_V();

    /**
     * The <code>getFlag_N()</code> method returns the abstract value of the N flag.
     * @return the new abstract bit of the flag
     */
    char getFlag_N();

    /**
     * The <code>getFlag_Z()</code> method returns the abstract value of the Z flag.
     * @return the new abstract bit of the flag
     */
    char getFlag_Z();

    /**
     * The <code>getFlag_C()</code> method returns the abstract value of the C flag.
     * @return the new abstract bit of the flag
     */
    char getFlag_C();

    /**
     * The <code>getIORegisterAV()</code> method reads the abstract value of an
     * IO register from the abstract state. For those registers being modelled,
     * this will return an abstract value that represents the current value of
     * the IO register. For IO registers that are not being modelled, it will
     * return the abstract value corresponding to all bits being unknown.
     * @param num the IO register number to read
     * @return the (abstract) value of the specified IO register
     */
    char getIORegisterAV(int num);

    /**
     * The <code>getRegisterAV()</code> method reads the abstract value of a
     * register in the abstract state.
     * @param r the register to read
     * @return the abstract value of the register
     */
    char getRegisterAV(Register r);
}
