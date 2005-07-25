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

package jintgen.jigir;

import jintgen.isdl.parser.Token;
import avrora.util.StringUtil;

/**
 * The <code>MapBitRangeAssignment</code> class represents an assignment to a range of bits of an element
 * within a map.
 *
 * @author Ben L. Titzer
 */
public class MapBitRangeAssignStmt extends AssignStmt {

    /**
     * The <code>typename</code> field stores a reference to the name of the map whose element is being
     * assigned to.
     */
    public final Token mapname;

    /**
     * The <code>expr</code> field stores a references to the expression which is evaluated to yield the expr
     * into the map.
     */
    public final Expr index;

    /**
     * The <code>low_bit</code> field stores the lowest bit of the range of bits being assigned.
     */
    public final int low_bit;

    /**
     * The <code>high_bit</code> field stores the highest bit of the range of bits being assigned.
     */
    public final int high_bit;

    /**
     * The constructor for the <code>MapAssignStmt</code> class initializes the public final fields in this
     * class that refer to the elements of the assignment.
     *
     * @param m the string name of the map as a token
     * @param i the expression representing the expr into the map
     * @param l the low bit of the range as a token
     * @param h the high bit of the range as a token
     * @param e the expression representing the right hand side of the assignment
     */
    public MapBitRangeAssignStmt(Token m, Expr i, Token l, Token h, Expr e) {
        super(e);
        mapname = m;
        index = i;
        int low = Expr.tokenToInt(l);
        int high = Expr.tokenToInt(h);

        low_bit = low < high ? low : high;
        high_bit = low > high ? low : high;
    }

    /**
     * The constructor for the <code>MapAssignStmt</code> class initializes the public final fields in this
     * class that refer to the elements of the assignment.
     *
     * @param m the string name of the map as a token
     * @param i the expression representing the expr into the map
     * @param l the low bit of the range as an integer
     * @param h the high bit of the range as an integer
     * @param e the expression representing the right hand side of the assignment
     */
    public MapBitRangeAssignStmt(Token m, Expr i, int l, int h, Expr e) {
        super(e);
        mapname = m;
        index = i;

        low_bit = l < h ? l : h;
        high_bit = l > h ? l : h;
    }

    /**
     * The constructor for the <code>MapAssignStmt</code> class initializes the public final fields in this
     * class that refer to the elements of the assignment.
     *
     * @param m the string name of the map as a token
     * @param i the expression representing the expr into the map
     * @param l the low bit of the range as an integer
     * @param h the high bit of the range as an integer
     * @param e the expression representing the right hand side of the assignment
     */
    public MapBitRangeAssignStmt(String m, Expr i, int l, int h, Expr e) {
        super(e);
        mapname = new Token();
        mapname.image = m;
        index = i;

        low_bit = l < h ? l : h;
        high_bit = l > h ? l : h;
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
        return StringUtil.embed("$" + mapname, index) + '[' + low_bit + ':' + high_bit + ']'
                + " = " + expr + ';';
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
