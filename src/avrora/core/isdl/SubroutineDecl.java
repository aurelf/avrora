package avrora.core.isdl;

import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class SubroutineDecl {

    public final Token name;
    public final List args;
    public final Token ret;

    public SubroutineDecl(Token n, List a, Token r) {
        name = n;
        args = a;
        ret = r;
    }
}
