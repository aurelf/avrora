package avrora.sim;

import avrora.sir.Program;
import avrora.sir.Instr;
import avrora.sir.InstrPrototype;

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
