package avrora.core.isdl;

import java.util.List;
import java.util.Iterator;

/**
 * @author Ben L. Titzer
 */
public class CodeRegion {

    public static class Operand {
        public final Token name;
        public final Token type;
        protected OperandDecl operandType;

        Operand(Token n, Token t) {
            name = n;
            type = t;
        }

        public void setOperandType(OperandDecl d) {
            operandType = d;
        }

        public boolean isRegister() {
            return operandType.isRegister();
        }

        public boolean isImmediate() {
            return operandType.isImmediate();        
        }

    }

    public final List operands;
    protected List stmts;

    public CodeRegion(List o, List s) {
        operands = o;
        stmts = s;
    }

    public int numOperands() {
        return operands.size();
    }

    public List getOperands() {
        return operands;
    }

    public Iterator getOperandIterator() {
        return operands.iterator();
    }

    public List getCode() {
        return stmts;
    }

    public void setCode(List s) {
        stmts = s;
    }

    public boolean hasBody() {
        return stmts != null;
    }

}
