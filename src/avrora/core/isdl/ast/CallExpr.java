
package avrora.core.isdl.ast;

import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class CallExpr extends Expr {
    public final String method;
    public final List args;

    public CallExpr(String m, List a) {
        method = m;
        args = a;
    }
}
