package vpc.mach.avr.syntax;

import vpc.core.AbstractToken;


/**
 * @author Ben L. Titzer
 */
public abstract class ASTNode {

    public abstract AbstractToken getLeftMostToken();
    public abstract AbstractToken getRightMostToken();
}
