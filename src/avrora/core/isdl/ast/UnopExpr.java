
package avrora.core.isdl.ast;

/**
 * @author Ben L. Titzer
 */
public class UnopExpr extends Expr {

    public final Expr operand;

    public UnopExpr(Expr o) {
        operand = o;
    }
}
