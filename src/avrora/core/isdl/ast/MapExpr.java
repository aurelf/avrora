
package avrora.core.isdl.ast;

/**
 * @author Ben L. Titzer
 */
public class MapExpr {
    public final String space;
    public final Expr index;

    public MapExpr(String s, Expr i) {
        space = s;
        index = i;
    }
}
