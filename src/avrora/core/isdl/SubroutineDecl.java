package avrora.core.isdl;

import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class SubroutineDecl extends CodeRegion {

    public final Token name;
    public final Token ret;
    public final boolean inline;

    public SubroutineDecl(boolean i, Token n, List o, Token r, List s) {
        super(o, s);
        inline = i;
        name = n;
        ret = r;
    }

}
