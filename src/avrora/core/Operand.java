/**
 * Copyright (c) 2004, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
