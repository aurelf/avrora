package avrora.core.isdl;

import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class OperandDecl {

    public final Token name;
    public final Token kind;

    public final List members;

    public OperandDecl(Token n, Token k, List m) {
        name = n;
        kind = k;
        members = m;
    }

    public boolean isRegister() {
        return kind.image.equals("register");
    }

    public boolean isImmediate() {
        return kind.image.equals("immediate");
    }
}
