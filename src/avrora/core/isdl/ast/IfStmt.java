
package avrora.core.isdl.ast;

import java.util.List;

/**
 * @author Ben L. Titzer
 */
public class IfStmt extends Stmt {
    public final Expr cond;
    public final List trueBranch;
    public final List falseBranch;

    public IfStmt(Expr c, List t, List f) {
        cond = c;
        trueBranch = t;
        falseBranch = f;
    }
}
