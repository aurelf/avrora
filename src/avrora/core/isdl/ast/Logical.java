
package avrora.core.isdl.ast;

/**
 * @author Ben L. Titzer
 */
public abstract class Logical {

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

    public static class AndExpr extends BinOp {
        public AndExpr(Expr left, Expr right) {
            super(left, "and", right);
        }
    }

    public static class OrExpr extends BinOp {
        public OrExpr(Expr left, Expr right) {
            super(left, "or", right);
        }
    }

    public static class XorExpr extends BinOp {
        public XorExpr(Expr left, Expr right) {
            super(left, "xor", right);
        }
    }

    public static class EquExpr extends BinOp {
        public EquExpr(Expr left, Expr right) {
            super(left, "==", right);
        }
    }

    public static class NequExpr extends BinOp {
        public NequExpr(Expr left, Expr right) {
            super(left, "!=", right);
        }
    }

    public static class LessExpr extends BinOp {
        public LessExpr(Expr left, Expr right) {
            super(left, "<", right);
        }
    }

    public static class LessEquExpr extends BinOp {
        public LessEquExpr(Expr left, Expr right) {
            super(left, "<=", right);
        }
    }

    public static class GreaterExpr extends BinOp {
        public GreaterExpr(Expr left, Expr right) {
            super(left, ">", right);
        }
    }

    public static class GreaterEquExpr extends BinOp {
        public GreaterEquExpr(Expr left, Expr right) {
            super(left, ">=", right);
        }
    }

    public static class NotExpr extends UnOp {
        public NotExpr(Expr l) {
            super("!", l);
        }
    }
}
