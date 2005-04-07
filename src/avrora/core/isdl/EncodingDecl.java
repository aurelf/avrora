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

package avrora.core.isdl;

import avrora.core.isdl.ast.Expr;
import avrora.core.isdl.parser.Token;
import avrora.util.StringUtil;

import java.util.Iterator;
import java.util.List;

/**
 * The <code>EncodingDecl</code> class represents the encoding of an instruction in machine code, describing
 * how to encode the mnemonic and operands into binary and vice versa.
 *
 * @author Ben L. Titzer
 */
public class EncodingDecl {

    public final Token name;

    protected final int prio;

    public final List fields;

    protected int bitWidth = -1;

    protected Cond condition;

    public EncodingDecl(Token n, Token pr, List f) {
        name = n;
        fields = f;
        if ( pr == null ) {
            prio = 0;
        } else {
            prio = StringUtil.evaluateIntegerLiteral(pr.image);
        }
    }

    public static class Cond {
        public final Token name;
        public final Expr expr;

        public Cond(Token n, Expr e) {
            name = n;
            expr = e;
        }
    }

    public static class Substitution {
        public final Token name;
        public final Expr expr;

        public Substitution(Token n, Expr e) {
            name = n;
            expr = e;
        }
    }

    public static class Derived extends EncodingDecl {
        public final Token pname;
        public final List subst;
        public EncodingDecl parent;

        public Derived(Token n, Token pr, Token p, List s) {
            super(n, pr, null);
            pname = p;
            subst = s;
        }

        public void setParent(EncodingDecl p) {
            parent = p;
        }

        public int getBitWidth() {
            if (bitWidth < 0)
                bitWidth = parent.getBitWidth();
            return bitWidth;
        }

    }

    public int getBitWidth() {
        if (bitWidth < 0)
            bitWidth = computeBitWidth();
        return bitWidth;
    }

    private int computeBitWidth() {
        int accum = 0;
        Iterator i = fields.iterator();
        while (i.hasNext()) {
            Expr e = (Expr)i.next();
            accum += e.getBitWidth();
        }
        return accum;
    }

    public void setCond(Cond c) {
        condition = c;
    }

    public Cond getCond() {
        return condition;
    }

    public boolean isConditional() {
        return condition != null;
    }

    public int getPriority() {
        return prio;
    }
}
