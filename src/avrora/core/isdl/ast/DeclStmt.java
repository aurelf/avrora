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

/**
 * The <code>DeclStmt</code> represents a declaration of a local, temporary
 * value in the IR. A named temporary is given a type and an initial value
 * at declaration time, allowing typechecking and ensuring that every
 * variable is initialized before it is used.
 *
 * @author Ben L. Titzer
 */
public class DeclStmt extends Stmt {
    /**
     * The <code>name</code> field stores a reference to the name of the local.
     */
    public final Token name;

    /**
     * The <code>type</code> field stores a reference to a token representing
     * the type of the local.
     */
    public final Token type;

    /**
     * The <code>init</code> field stores a reference to the expression which is
     * evaluated to give an initial value to the local.
     */
    public final Expr init;

    /**
     * The constructor of the <code>DeclStmt</code> class initializes the references
     * to the name, type, and initial value of the declared local.
     * @param n the name of the local as a token
     * @param t the type of the local as a token
     * @param i a reference to the expression evaluated to give the local an initial
     * value
     */
    public DeclStmt(Token n, Token t, Expr i) {
        name = n;
        type = t;
        init = i;
    }
}
