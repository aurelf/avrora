package avrora.syntax;

import avrora.syntax.AbstractToken;


/**
 * The <code>ASTNode</code> class is a unification of all syntax-related items
 * that are dealt with in loading source programs. This allows the error generator
 * to get an easy handle on where the error occurred in the source program.
 * @author Ben L. Titzer
 */
public abstract class ASTNode {

    /**
     * The <code>getLeftMostToken()</code> method gets the first token associated
     * with the abstract syntax tree node.
     * @return an <code>AbstractToken</code> instance representing the first token
     * that is a part of this syntactic item.
     */
    public abstract AbstractToken getLeftMostToken();

    /**
     * The <code>getRightMostToken()</code> method gets the last token associated
     * with the abstract syntax tree node.
     * @return an <code>AbstractToken</code> instance representing the last token
     * that is a part of this syntactic item.
     */
    public abstract AbstractToken getRightMostToken();
}
