package avrora.syntax;

import avrora.syntax.AbstractToken;


/**
 * @author Ben L. Titzer
 */
public abstract class ASTNode {

    public abstract AbstractToken getLeftMostToken();

    public abstract AbstractToken getRightMostToken();
}
