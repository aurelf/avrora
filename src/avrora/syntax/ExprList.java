package avrora.syntax;

import avrora.syntax.AbstractToken;

/**
 * @author Ben L. Titzer
 */
public class ExprList extends ASTNode {

    public ExprItem head, tail;

    private int length;

    public class ExprItem {
        public final Expr expr;
        public ExprItem next;

        public ExprItem(Expr e) {
            expr = e;
        }

    }

    public AbstractToken getLeftMostToken() {
        return head.expr.getLeftMostToken();
    }

    public AbstractToken getRightMostToken() {
        return tail.expr.getLeftMostToken();
    }

    public void add(Expr e) {
        if (head == null)
            head = tail = new ExprItem(e);
        else {
            tail.next = new ExprItem(e);
            tail = tail.next;
        }

        length++;
    }

    public int length() {
        return length;
    }
}
