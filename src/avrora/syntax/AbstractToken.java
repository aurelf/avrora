package avrora.syntax;

/**
 * This class is used to unify the Token classes from all JavaCC-generated parsers.
 * @author Ben L. Titzer
 */
public abstract class AbstractToken {
    /**
     * beginLine and beginColumn describe the position of the first character
     * of this token; endLine and endColumn describe the position of the
     * last character of this token.
     */
    public int beginLine, beginColumn, endLine, endColumn;

    /**
     * The string image of the token.
     */
    public String image;

    /**
     * The file in which the token originated.
     */
    public String file;

    /**
     * Returns the image.
     */
    public String toString() {
        return image;
    }

    public abstract AbstractToken getNextToken();

}
