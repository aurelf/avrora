package vpc.mach.avr.sir;

import vpc.mach.avr.Operand;

/**
 * @author Ben L. Titzer
 */
public interface InstrPrototype {
    public Instr build(int pc, Operand[] ops);

    public int getSize();

    public String getVariant();

    public String getName();
}
