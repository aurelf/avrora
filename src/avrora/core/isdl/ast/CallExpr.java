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

package avrora.core.isdl.ast;
import avrora.core.isdl.Token;

import java.util.List;

/**
 * The <code>CallExpr</code> class represents a subroutine call within
 * the IR. Subroutines can be called for side effects and produce results
 * in the IR, allowing factoring of common pieces of code.
 *
 * @author Ben L. Titzer
 */
public class CallExpr extends Expr {

    /**
     * The <code>method</code> field stores a string that represents
     * the name of the subroutine being called.
     */
    public final Token method;

    /**
     * The <code>args</code> fields stores a reference to a list of expressions
     * that are evaluated and passed as arguments to the subroutine.
     */
    public final List args;

    /**
     * The constructor of the <code>CallExpr</code> class simply initializes the
     * references to the subroutine name and arguments.
     * @param m the name of the subroutine as a string
     * @param a list of expressions representing the arguments to the subroutine
     */
    public CallExpr(Token m, List a) {
        method = m;
        args = a;
    }

    /**
     * The <code>accept()</code> method implements one half of the visitor
     * pattern so that client visitors can traverse the syntax tree easily
     * and in an extensible way.
     * @param v the visitor to accept
     */
    public void accept(ExprVisitor v) {
        v.visit(this);
    }

    /**
     * The <code>accept()</code> method implements one half of the visitor
     * pattern so that client visitors can traverse the syntax tree easily
     * and in an extensible way.
     * @param v the visitor to accept
     */
    public void accept(CodeVisitor v) {
        v.visit(this);
    }
}
