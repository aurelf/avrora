
package avrora.core.isdl.ast;

/**
 * @author Ben L. Titzer
 */
public class BitRangeExpr {
    public final Expr operand;
    public final int low_bit;
    public final int high_bit;

    public BitRangeExpr(Expr o, int l, int h) {
        operand = o;
        low_bit = l;
        high_bit = h;
    }
}
