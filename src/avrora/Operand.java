package avrora;

import avrora.syntax.ASTNode;
import avrora.syntax.Expr;
import vpc.core.AbstractToken;

/**
 * @author Ben L. Titzer
 */
public abstract class Operand extends ASTNode {

    public boolean isRegister() {
        return false;
    }

    public boolean isConstant() {
        return false;
    }

    public static class Register extends Operand {
        public final AbstractToken name;
        private avrora.sir.Register register;

        public Register(AbstractToken n) {
            name = n;
        }

        public AbstractToken getLeftMostToken() {
            return name;
        }

        public AbstractToken getRightMostToken() {
            return name;
        }

        public boolean isRegister() {
            return true;
        }

        public avrora.sir.Register getRegister() {
            return register;
        }

        public void setRegister(avrora.sir.Register r) {
            register = r;
        }
    }

    public static class Constant extends Operand {
        public final Expr expr;
        private int value;

        public Constant(Expr e) {
            expr = e;
        }

        public AbstractToken getLeftMostToken() {
            return expr.getLeftMostToken();
        }

        public AbstractToken getRightMostToken() {
            return expr.getRightMostToken();
        }

        public boolean isConstant() {
            return true;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
