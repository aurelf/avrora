package avrora.core;

import avrora.syntax.ASTNode;
import avrora.syntax.Expr;
import avrora.syntax.AbstractToken;

/**
 * The <code>Operand</code> class encapsulates the notion of an operand to
 * an instruction. An operand can be either a register or a constant integer.
 * Whether the register is a source or destination register depends on the
 * particular instruction. Likewise, whether the integer represents an
 * immediate, an offset, or an address depends on the instruction.
 *
 * Operands are used as the arguments to the constructors of instructions.
 *
 * @see InstrPrototype
 *
 * @author Ben L. Titzer
 */
public interface Operand {

    /**
     * The <code>asRegister()</code> method uses virtual dispatch to avoid a
     * cast. If this operand is an instance of <code>Operand.Register</code> it
     * will return itself. Otherwise, it will return null.
     * @return this if this is an instance of <code>Operand.Register</code>; null
     * otherwise
     */
    public Operand.Register asRegister();

    /**
     * The <code>asConstant()</code> method uses virtual dispatch to avoid a
     * cast. If this operand is an instance of <code>Operand.Constant</code> it
     * will return itself. Otherwise, it will return null.
     * @return this if this is an instance of <code>Operand.Constant</code>; null
     * otherwise
     */
    public Operand.Constant asConstant();

    /**
     * The <code>Operand.Register</code> class encapsulates the notion of a
     * register operand to an instruction.
     */
    public interface Register extends Operand {

        public avrora.core.Register getRegister();
    }

    /**
     * The <code>Operand.Constant</code> class encapsulates the notion of a
     * constant operand to an instruction.
     */
    public interface Constant extends Operand {

        public int getValue();
        public int getValueAsWord();
    }
}
