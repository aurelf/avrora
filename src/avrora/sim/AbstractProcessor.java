package avrora.sim;

import avrora.core.Program;
import avrora.core.Instr;
import avrora.core.InstrPrototype;

/**
 * @author Ben L. Titzer
 */
public interface AbstractProcessor {

    public int getRamSize();
    public int getIORegSize();
    public int getFlashSize();
    public int getEEPromSize();
    public int getHz();

    public long millisToCycles(double ms);
    public double cyclesToMillis(long ms);

    public boolean isSupported(InstrPrototype i);

    public Simulator loadProgram(Program p);
}
