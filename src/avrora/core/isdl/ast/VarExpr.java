
package avrora.core.isdl.ast;

/**
 * @author Ben L. Titzer
 */
public class VarExpr extends Expr {
    public final String variable;

    public VarExpr(String v) {
        variable = v;
    }
}
