package avrora.core.isdl;

import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class EncodingDecl {

    public final Token name;

    public final List fields;

    public EncodingDecl(Token n, List f) {
        name = n;
        fields = f;
    }

    public static class Derived extends EncodingDecl {
        public final Token parent;
        public final List subst;

        public Derived(Token n, Token p, List s) {
            super(n, null);
            parent = p;
            subst = s;
        }

    }

}
