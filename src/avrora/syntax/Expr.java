package avrora.syntax;

import avrora.Avrora;
import avrora.util.StringUtil;
import vpc.core.AbstractToken;

/**
 * @author Ben L. Titzer
 */
public abstract class Expr extends ASTNode {

    public abstract int evaluate(Context c);

    public boolean isConstant() {
        return false;
    }

    private static int asInt(boolean b) {
        return b ? 1 : 0;
    }

    private static boolean asBool(int i) {
        return i != 0;
    }

    public static class BinOp extends Expr {
        public final AbstractToken op;
        public final Expr left;
        public final Expr right;

        public BinOp(AbstractToken tok, Expr l, Expr r) {
            op = tok;
            left = l;
            right = r;
        }

        public int evaluate(Context c) {
            int lval = left.evaluate(c);
            int rval = right.evaluate(c);
            String o = op.image;

            // TODO: this is a bit ugly.
            if (o.equals("*")) return lval * rval;
            if (o.equals("/")) return lval / rval;
            if (o.equals("-")) return lval - rval;
            if (o.equals("+")) return lval + rval;
            if (o.equals("<<")) return lval << rval;
            if (o.equals(">>")) return lval >> rval;
            if (o.equals("<")) return asInt(lval < rval);
            if (o.equals(">")) return asInt(lval > rval);
            if (o.equals("<=")) return asInt(lval <= rval);
            if (o.equals(">=")) return asInt(lval >= rval);
            if (o.equals("==")) return asInt(lval == rval);
            if (o.equals("!=")) return asInt(lval != rval);
            if (o.equals("&")) return lval & rval;
            if (o.equals("^")) return lval ^ rval;
            if (o.equals("|")) return lval | rval;
            if (o.equals("&&")) return asInt(asBool(lval) && asBool(rval));
            if (o.equals("||")) return asInt(asBool(lval) || asBool(rval));

            throw Avrora.failure("unknown binary operator: " + op);
        }

        public AbstractToken getLeftMostToken() {
            return left.getLeftMostToken();
        }

        public AbstractToken getRightMostToken() {
            return right.getRightMostToken();
        }

    }

    public static class UnOp extends Expr {
        public final AbstractToken op;
        public final Expr operand;

        public UnOp(AbstractToken tok, Expr oper) {
            op = tok;
            operand = oper;
        }

        public int evaluate(Context c) {
            int oval = operand.evaluate(c);
            String o = op.image;

            if (o.equals("!")) return asInt(!asBool(oval));
            if (o.equals("~")) return ~oval;
            if (o.equals("-")) return -oval;

            throw Avrora.failure("unknown unary operator: " + op);
        }

        public AbstractToken getLeftMostToken() {
            return op;
        }

        public AbstractToken getRightMostToken() {
            return operand.getRightMostToken();
        }

    }

    public static class Func extends Expr {
        public final AbstractToken func;
        public final Expr argument;
        public final AbstractToken last;

        public Func(AbstractToken tok, Expr arg, AbstractToken l) {
            func = tok;
            argument = arg;
            last = l;
        }

        public int evaluate(Context c) {
            int aval = argument.evaluate(c);
            String f = func.image;

            // TODO: verify correctness of these functions
            if (f.equalsIgnoreCase("byte")) return aval & 0xff;
            if (f.equalsIgnoreCase("low") || f.equals("lo8")) return aval & 0xff;
            if (f.equalsIgnoreCase("high") || f.equals("hi8")) return (aval >>> 8) & 0xff;
            if (f.equalsIgnoreCase("byte2")) return (aval >>> 8) & 0xff;
            if (f.equalsIgnoreCase("byte3")) return (aval >>> 16) & 0xff;
            if (f.equalsIgnoreCase("byte4")) return (aval >>> 24) & 0xff;
            if (f.equalsIgnoreCase("lwrd")) return aval & 0xffff;
            if (f.equalsIgnoreCase("hwrd")) return (aval >>> 16) & 0xffff;
            if (f.equalsIgnoreCase("page")) return (aval >>> 16) & 0x3f;
            if (f.equalsIgnoreCase("exp2")) return 1 << aval;
            if (f.equalsIgnoreCase("log2")) return log(aval);

            throw Avrora.failure("unknown function: " + func);
        }

        private int log(int val) {
            int log = 1;

            // TODO: verify correctness of this calculation
            if ((val & 0xffff0000) != 0) {
                log += 16;
                val = val >> 16;
            }
            if ((val & 0xffff00) != 0) {
                log += 8;
                val = val >> 8;
            }
            if ((val & 0xffff0) != 0) {
                log += 4;
                val = val >> 4;
            }
            if ((val & 0xffffc) != 0) {
                log += 2;
                val = val >> 2;
            }
            if ((val & 0xffffe) != 0) {
                log += 1;
            }


            return log;
        }

        public AbstractToken getLeftMostToken() {
            return func;
        }

        public AbstractToken getRightMostToken() {
            return last;
        }
    }

    public abstract static class Term extends Expr {
        public final AbstractToken token;

        Term(AbstractToken tok) {
            token = tok;
        }

        public AbstractToken getLeftMostToken() {
            return token;
        }

        public AbstractToken getRightMostToken() {
            return token;
        }
    }

    public static class Variable extends Term {

        public Variable(AbstractToken n) {
            super(n);
        }

        public int evaluate(Context c) {
            return c.getVariable(token);
        }

    }

    public static class Constant extends Term {
        public final int value;

        public Constant(AbstractToken tok) {
            super(tok);
            value = evaluateLiteral(tok.image);
        }

        public int evaluate(Context c) {
            return value;
        }

        public boolean isConstant() {
            return true;
        }

        private static int evaluateLiteral(String val) {
            if (val.startsWith("$"))                          // hexadecimal
                return Integer.parseInt(val.substring(1), 16);
            else
                return StringUtil.evaluateIntegerLiteral(val);
        }
    }

    public static class CharLiteral extends Term {
        public final int value;

        public CharLiteral(AbstractToken tok) {
            super(tok);
            value = StringUtil.evaluateCharLiteral(tok.image);
        }

        public int evaluate(Context c) {
            return value;
        }

        public boolean isConstant() {
            return true;
        }
    }

    public static class StringLiteral extends Term {
        public final String value;

        public StringLiteral(AbstractToken tok) {
            super(tok);
            value = StringUtil.evaluateStringLiteral(tok.image);
        }

        public int evaluate(Context c) {
            throw Avrora.failure("cannot evaluate a string to an integer");
        }
    }

}
