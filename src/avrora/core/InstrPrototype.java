package avrora.core;


/**
 * The <code>InstrPrototype</code> interface represents an object that is
 * capable of building <code>Instr</code> instances given an array of
 * <code>Operand</code> instances. It also contains methods that describe
 * the instructions such as their name, their variant name, and their
 * size in bytes.
 *
 * @author Ben L. Titzer
 */
public interface InstrPrototype {
    /**
     * The <code>build()</code> method constructs a new <code>Instr</code>
     * instance with the given operands, checking the operands against
     * the constraints that are specific to each instruction.
     *
     * @param pc  the address at which the instruction will be located
     * @param ops the operands to the instruction
     * @return a new <code>Instr</code> instance representing the
     *         instruction with the given operands
     */
    public Instr build(int pc, Operand[] ops);

    /**
     * The <code>getSize()</code> method returns the size of the instruction
     * in bytes. Since each prototype corresponds to exactly one instruction
     * variant, all instructions built by this prototype will have the
     * same size.
     *
     * @return the size of the instruction in bytes
     */
    public int getSize();

    /**
     * The <code>getVariant()</code> method returns the variant name of the
     * instruction as a string. Since instructions like load and store have
     * multiple variants, they each have specific variant names to distinguish
     * them internally in the core of Avrora. For example, for "ld x+, (addr)",
     * the variant is "ldpi" (load with post increment), but the actual instruction
     * is "ld", so this method will return "ldpi".
     *
     * @return the variant of the instruction that this prototype represents
     */
    public String getVariant();

    /**
     * The <code>getName()</code> method returns the name of the instruction as
     * a string. For instructions that are variants of instructions, this method
     * returns the actual name of the instruction. For example, for "ld x+, (addr)",
     * the variant is "ldpi" (load with post increment), but the actual instruction
     * is "ld", so this method will return "ld".
     *
     * @return the name of the instruction
     */
    public String getName();
}
