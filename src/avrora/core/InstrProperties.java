package avrora.core;

/**
 * The <code>InstrProperties</code> represents a grab bag of the properties of
 * an instruction. The fields are public and final, which allows fast access from
 * the interpreter.
 *
 * @author Ben L. Titzer
 */
public class InstrProperties {

    public final String name;
    public final String variant;
    public final int size;
    public final int cycles;

    public InstrProperties(String n, String v, int s, int c) {
        name = n;
        variant = v;
        size = s;
        cycles = c;
    }
}
