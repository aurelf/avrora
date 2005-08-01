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

package jintgen.isdl;

import avrora.util.Util;
import jintgen.jigir.Expr;
import jintgen.jigir.CodeRegion;
import jintgen.jigir.Stmt;
import jintgen.isdl.parser.Token;
import avrora.util.StringUtil;

import java.util.List;

/**
 * The <code>InstrDecl</code> class represents the declaration of an instruction in an instruction set
 * description language file. It include a description of the encoding format of the instruction as well as a
 * block of IR code that describe how to execute the instruction.
 *
 * @author Ben L. Titzer
 */
public class InstrDecl extends CodeRegion {

    /**
     * The <code>name</code> field stores a string representing the name of the instruction.
     */
    public final Token name;

    public List<EncodingDecl> encodingList;

    public List<Property> propertyList;

    public final String className;

    public final String innerClassName;

    public final boolean pseudo;

    private int size = -1;

    /**
     * The constructor of the <code>InstrDecl</code> class initializes the fields based on the parameters.
     *
     * @param n the name of the instruction as a string
     */
    public InstrDecl(boolean ps, Token n, List<CodeRegion.Operand> o, List<Property> p, List<Stmt> s, List<EncodingDecl> el) {
        super(o, s);
        pseudo = ps;
        name = n;
        encodingList = el;
        propertyList = p;
        innerClassName = StringUtil.trimquotes(name.image).toUpperCase();
        className = "Instr." + innerClassName;
    }

    public int getEncodingSize() {
        if ( size <= 0 )
            throw Util.failure("size for instruction "+name+" has not been computed");
        return size;
    }

    public String getClassName() {
        return className;
    }

    public String getInnerClassName() {
        return innerClassName;
    }

    public void setEncodingSize(int bits) {
        size = bits;
    }

    public int getCycles() {
        throw Util.unimplemented();
    }

    public String getSyntax() {
        throw Util.unimplemented();
    }

    public Iterable<EncodingDecl> getEncodings() {
        return encodingList;
    }
}
