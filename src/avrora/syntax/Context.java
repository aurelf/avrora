package vpc.mach.avr.syntax;

import vpc.mach.avr.sir.Register;
import vpc.mach.avr.syntax.atmel.Token;
import vpc.core.AbstractToken;

/**
 * @author Ben L. Titzer
 */
public interface Context {

    public Register getRegister(AbstractToken ident);
    public int getVariable(AbstractToken ident);
}
