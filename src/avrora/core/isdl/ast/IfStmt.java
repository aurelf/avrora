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

import java.util.List;

/**
 * The <code>IfStmt</code> class represents a simple branch within the IR.
 * Since loops and switch statements are not allowed, if statements
 * (and subroutine calls) are the only form of control flow.
 *
 * @author Ben L. Titzer
 */
public class IfStmt extends Stmt {
    /**
     * The <code>cond</code> field stores a reference to the expression
     * that is evaluated as the condition determining which branch
     * is executed.
     */
    public final Expr cond;

    /**
     * The <code>trueBranch</code> field stores a reference to the
     * list of statements to be executed if the condition is true.
     */
    public final List trueBranch;

    /**
     * The <code>falseBranch</code> field stores a reference to the
     * list of statements to be executed if the condition is false.
     */
    public final List falseBranch;

    /**
     * The constructor of the <code>IfStmt</code> class simply initializes
     * the internal fields based on the parameters.
     * @param c a reference to the expression representing the condition
     * @param t a reference to the list of statements to execute if the condition
     * evaluates to true
     * @param f a reference to the list of statements to execute if the condition
     * evaluates to false
     */
    public IfStmt(Expr c, List t, List f) {
        cond = c;
        trueBranch = t;
        falseBranch = f;
    }
}
