package avrora.syntax.atmel;

/**
 * The <code>FileMarkingTokenManager</code> is a subclass of the TokenManager
 * for the Atmel parser that marks each token that is seen with the name of the
 * file that it came from. This is useful in unifying multiple grammars that
 * each have their own definition of Token, since an AbstractToken can be used
 * by any part of the compiler.
 *
 * @author Ben L. Titzer
 */
public class FileMarkingTokenManager extends AtmelParserTokenManager {

    protected String filename;

    public FileMarkingTokenManager(SimpleCharStream s, String fname) {
        super(s);
        filename = fname;
    }

    public FileMarkingTokenManager(SimpleCharStream s, int lexState, String fname) {
        super(s, lexState);
        filename = fname;
    }

    public void ReInit(SimpleCharStream s, String fname) {
        super.ReInit(s);
        filename = fname;
    }

    public void ReInit(SimpleCharStream s, int lexState, String fname) {
        super.ReInit(s, lexState);
        filename = fname;
    }

    protected Token jjFillToken()
    {
        Token t = super.jjFillToken();
        t.file = filename;
        return t;
    }

}
