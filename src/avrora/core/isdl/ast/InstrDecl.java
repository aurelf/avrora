
package avrora.core.isdl.ast;

/**
 * The <code>InstrDecl</code> class represents the declaration
 * of an instruction in an instruction set description language
 * file.
 * @author Ben L. Titzer
 */
public class InstrDecl {

    public final String name;
    public final String variant;

    public InstrDecl(String n, String v) {
        name = n;
        variant = v;
    }
}
