package avrora.core.isdl;

import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class SubroutineDecl {

    public final Token name;
    public final List args;
    public final Token ret;
    public List execute;

    public SubroutineDecl(Token n, List a, Token r, List e) {
        name = n;
        args = a;
        ret = r;
        execute = e;
    }

    public static class Formal {
        public final Token name;
        public final Token type;

        public Formal(Token n, Token t) {
            name = n;
            type = t;
        }
    }
}
