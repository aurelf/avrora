
package avrora.core.isdl.ast;

/**
 * @author Ben L. Titzer
 */
public abstract class Arith extends Expr {

    public abstract static class BinOp extends Arith {
        public final String operation;
        public final Expr left;
        public final Expr right;

        public BinOp(Expr l, String o, Expr r) {
            left = l;
            right = r;
            operation = o;
        }
    }

    public abstract static class UnOp extends Arith {
        public final String operation;
        public final Expr operand;

        public UnOp(String op, Expr o) {
            operand = o;
            operation = op;
        }
    }

    public static class AddExpr extends BinOp {
        public AddExpr(Expr left, Expr right) {
            super(left, "+", right);
        }
    }

    public static class SubExpr extends BinOp {
        public SubExpr(Expr left, Expr right) {
            super(left, "-", right);
        }
    }

    public static class MulExpr extends BinOp {
        public MulExpr(Expr left, Expr right) {
            super(left, "*", right);
        }
    }

    public static class DivExpr extends BinOp {
        public DivExpr(Expr left, Expr right) {
            super(left, "/", right);
        }
    }

    public static class AndExpr extends BinOp {
        public AndExpr(Expr left, Expr right) {
            super(left, "&", right);
        }
    }

    public static class OrExpr extends BinOp {
        public OrExpr(Expr left, Expr right) {
            super(left, "|", right);
        }
    }

    public static class XorExpr extends BinOp {
        public XorExpr(Expr left, Expr right) {
            super(left, "^", right);
        }
    }

    public static class ShiftLeftExpr extends BinOp {
        public ShiftLeftExpr(Expr left, Expr right) {
            super(left, "<<", right);
        }
    }

    public static class ShiftRightExpr extends BinOp {
        public ShiftRightExpr(Expr left, Expr right) {
            super(left, ">>", right);
        }
    }

    public static class CompExpr extends UnOp {
        public CompExpr(Expr l) {
            super("~", l);
        }
    }

    public static class NegExpr extends UnOp {
        public NegExpr(Expr l) {
            super("-", l);
        }
    }
}
