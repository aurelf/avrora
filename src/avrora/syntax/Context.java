package avrora.syntax;

import avrora.core.Register;
import avrora.syntax.AbstractToken;

/**
 * The <code>Context</code> interface represents a context in which an expression in
 * a program should be evaluated. It provides the environmental bindings necessary to
 * resolve variable references within computed expressions.
 * @author Ben L. Titzer
 */
public interface Context {

    /**
     * The <code>getRegister()</code> method resolves a register that may have been
     * renamed earlier in the program.
     * @param ident the string name of the register or register alias
     * @return a reference to the <code>Register</code> instance representing
     * the register with the specified name or alias
     */
    public Register getRegister(AbstractToken ident);

    /**
     * The <code>getVariable()</code> method looks up the value of a named constant
     * within the current environment and returns its value.
     * @param ident the name of the variable
     * @return the value of the variable within the environment
     */
    public int getVariable(AbstractToken ident);
}
