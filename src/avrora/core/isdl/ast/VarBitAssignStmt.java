/**
 * Copyright (c) 2004-2005, Regents of the University of California
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

package avrora.core.isdl.ast;

import avrora.core.isdl.parser.Token;

/**
 * The <code>VarBitAssignStmt</code> class represents an assignment to a single bit within a local or global
 * variable.
 *
 * @author Ben L. Titzer
 */
public class VarBitAssignStmt extends AssignStmt {

    /**
     * The <code>variable</code> field stores a reference to the token that represents the name of the
     * variable being assigned to.
     */
    public final Token variable;

    /**
     * The <code>bit</code> field stores a reference to the expression that represents the expr of the bit to
     * assign to.
     */
    public final Expr bit;

    /**
     * The constructor for the <code>VarAssignStmt</code> class simply initializes the internal references to
     * the internal members of this assignment.
     *
     * @param m the string name of the variable as a token
     * @param b an expression representing the expr of the bit
     * @param e an expression representing the right hand side of the assignment
     */
    public VarBitAssignStmt(Token m, Expr b, Expr e) {
        super(e);
        variable = m;
        bit = b;
    }

    /**
     * The constructor for the <code>VarAssignStmt</code> class simply initializes the internal references to
     * the internal members of this assignment.
     *
     * @param m the string name of the variable as a token
     * @param b an expression representing the expr of the bit
     * @param e an expression representing the right hand side of the assignment
     */
    public VarBitAssignStmt(String m, Expr b, Expr e) {
        super(e);
        variable = new Token();
        variable.image = m;
        bit = b;
    }

    /**
     * The <code>accept()</code> method implements one half of the visitor pattern for visiting the abstract
     * syntax trees representing the code of a particular instruction or subroutine.
     *
     * @param v the visitor to accept
     */
    public void accept(StmtVisitor v) {
        v.visit(this);
    }

    /**
     * The <code>toString()</code> method recursively converts this statement to a string.
     *
     * @return a string representation of this statement
     */
    public String toString() {
        return variable.image + '[' + bit + "] = " + expr + ';';
    }

    /**
     * The <code>accept()</code> method implements one half of the visitor pattern for visiting the abstract
     * syntax trees representing the code of a particular instruction or subroutine. The
     * <code>StmtRebuilder</code> interface allows visitors to rearrange and rebuild the statements.
     *
     * @param r the visitor to accept
     * @return the result of calling the appropriate <code>visit()</code> of the rebuilder passed
     */
    public Stmt accept(StmtRebuilder r, Object env) {
        return r.visit(this, env);
    }

}
