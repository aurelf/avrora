
package avrora.core.isdl.ast;

/**
 * @author Ben L. Titzer
 */
public class AssignStmt extends Stmt {

    public final Expr result;

    public AssignStmt(Expr r) {
        result = r;
    }
}
