
package avrora.core.isdl.ast;

/**
 * @author Ben L. Titzer
 */
public class DeclStmt {
    public final String name;
    public final String type;
    public final Expr init;

    public DeclStmt(String n, String t, Expr i) {
        name = n;
        type = t;
        init = i;
    }
}
