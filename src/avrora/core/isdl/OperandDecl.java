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

import avrora.core.isdl.parser.Token;
import avrora.util.StringUtil;

import java.util.List;

/**
 * The <code>OperandDecl</code> class represents the declaration of a set of values (or registers) that can
 * serve as an operand to a particular instruction. For example, an operand declaration might be the set of
 * all general purpose registers, or it might be the set of high general purpose registers, or the set of
 * address registers, etc.
 *
 * @author Ben L. Titzer
 */
public abstract class OperandDecl {

    public final Token name;
    public final Token kind;
    public final int bitSize;

    protected OperandDecl(Token n, Token b, Token k) {
        name = n;
        kind = k;
        bitSize = StringUtil.evaluateIntegerLiteral(b.image);
    }

    public static class Immediate extends OperandDecl {

        public final int low;
        public final int high;

        public Immediate(Token n, Token b, Token k, Token l, Token h) {
            super(n, b, k);
            low = StringUtil.evaluateIntegerLiteral(l.image);
            high = StringUtil.evaluateIntegerLiteral(h.image);
        }

        public boolean isImmediate() {
            return true;
        }

        public String getSomeMember() {
            return (low+((high-low)/2))+"";
        }
    }

    public static class RegisterSet extends OperandDecl {
        public final List members;

        public RegisterSet(Token n, Token b, Token k, List l) {
            super(n, b, k);
            members = l;
        }

        public boolean isRegister() {
            return true;
        }

        public String getSomeMember() {
            RegisterEncoding enc = (RegisterEncoding)members.get(0);
            return enc.name.toString();
        }
    }

    public static class RegisterEncoding {
        public final Token name;
        public final int value;

        public RegisterEncoding(Token n, Token v) {
            name = n;
            value = StringUtil.evaluateIntegerLiteral(v.image);
        }
    }

    public boolean isRegister() {
        return false;
    }

    public boolean isImmediate() {
        return false;
    }

    public abstract String getSomeMember();
}
