package avrora.syntax;

import avrora.core.Register;
import avrora.syntax.atmel.Token;
import vpc.core.AbstractToken;

/**
 * @author Ben L. Titzer
 */
public interface Context {

    public Register getRegister(AbstractToken ident);
    public int getVariable(AbstractToken ident);
}
