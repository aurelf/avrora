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
import avrora.util.StringUtil;
import avrora.Avrora;

/**
 * The <code>Expr</code> class represents an expression in the IR. Expressions
 * are evaluated and produce a value that can be assigned to locals, globals,
 * maps, used in if statements, and passed as parameters.
 *
 * @author Ben L. Titzer
 */
public abstract class Expr {

    public static final int PREC_L_OR    =  0;
    public static final int PREC_L_XOR   =  1;
    public static final int PREC_L_AND   =  2;
    public static final int PREC_A_OR    =  3;
    public static final int PREC_A_XOR   =  4;
    public static final int PREC_A_AND   =  5;
    public static final int PREC_L_EQU   =  6;
    public static final int PREC_L_REL   =  7;
    public static final int PREC_A_SHIFT =  8;
    public static final int PREC_A_ADD   =  9;
    public static final int PREC_A_MUL   = 10;
    public static final int PREC_UN      = 11;
    public static final int PREC_TERM    = 12;



    /**
     * The <code>isVariable()</code> method tests whether this expression is
     * a single variable use.
     * @return true if this expression is a direct use of a variable; false otherwise
     */
    public boolean isVariable() {
        return false;
    }

    /**
     * The <code>isLiteral()</code> method tests whether this expression is
     * a known constant directly (i.e. a literal).
     * @return true if this expression is a literal; false otherwise
     */
    public boolean isLiteral() {
        return false;
    }

    /**
     * The <code>isConstantExpr()</code> method tests whether this expression
     * is a constant expression (i.e. it is reducable to a constant and has
     * no references to variables, maps, etc).
     * @return true if this expression can be evaluated to a constant; false otherwise
     */
    public boolean isConstantExpr() {
        return false;
    }

    /**
     * The <code>getBitWidth()</code> method gets the number of bits needed to
     * represent this value. This is needed in the case of encoding formats, which
     * need to compute the size of an instruction based on the width of its
     * internal fields.
     * @return
     */
    public int getBitWidth() {
        throw Avrora.unimplemented();
    }

    public abstract int getPrecedence();

    public static int tokenToInt(Token i) {
        return StringUtil.evaluateIntegerLiteral(i.image);
    }

    public static boolean tokenToBool(Token i) {
        return Boolean.valueOf(i.image).booleanValue();
    }

    public abstract void accept(ExprVisitor v);

    public abstract void accept(CodeVisitor v);

    public abstract Expr accept(CodeRebuilder r);

    public String innerString(Expr e) {
        if ( e.getPrecedence() < this.getPrecedence() ) return StringUtil.embed(e.toString());
        else return e.toString();
    }
}
