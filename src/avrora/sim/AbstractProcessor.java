package vpc.mach.avr.sim;

import vpc.mach.avr.sir.Program;
import vpc.mach.avr.sir.Instr;
import vpc.mach.avr.sir.InstrPrototype;

/**
 * @author Ben L. Titzer
 */
public interface AbstractProcessor {

    public int getRamSize();
    public int getIORegSize();
    public int getFlashSize();
    public int getEEPromSize();
    public int getHz();

    public boolean isSupported(InstrPrototype i);

    public Simulator loadProgram(Program p);
}
