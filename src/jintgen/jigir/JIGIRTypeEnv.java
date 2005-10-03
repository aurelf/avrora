/**
 * Copyright (c) 2005, Regents of the University of California
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
 *
 * Creation date: Oct 3, 2005
 */

package jintgen.jigir;

import jintgen.types.*;
import jintgen.isdl.parser.Token;
import java.util.*;
import cck.util.Util;

/**
 * @author Ben L. Titzer
 */
public class JIGIRTypeEnv extends TypeEnv {
    public final Type BOOLEAN;
    public final TypeCon BOOL;
    public final TypeCon INT;
    public final TypeCon MAP;
    public final TypeCon FUNCTION;
    public final TypeCon TUPLE;

    protected class TYPECON_int extends TypeCon {
        final SignDimension sign;
        final SizeDimension size;

        TYPECON_int() {
            super("int");
            sign = new SignDimension();
            size = new SizeDimension();
            addDimension(sign);
            addDimension(size);
        }

        public Type newType(HashMap<String, List> dims) {
            throw Util.unimplemented();
        }
    }

    protected class TYPE_int extends Type {
        protected final boolean signed;
        protected final int length;

        protected TYPE_int(boolean sign, int len) {
            super(null, null, null);
            signed = sign;
            length = len;
        }
    }

    public JIGIRTypeEnv() {
        // initialize the boolean type constructor
        BOOL = new TypeCon("boolean");
        BOOLEAN = BOOL.newType(this);

        // initialize the integer type constructor
        INT = new TYPECON_int();

        // initialize the map type constructor
        MAP = new TypeCon("map");
        MAP.addDimension(new ParamDimension("index", variance(ParamDimension.INVARIANT)));
        MAP.addDimension(new ParamDimension("elem", variance(ParamDimension.INVARIANT)));

        // initialize the function type constructor
        FUNCTION = new TypeCon("function");
        FUNCTION.addDimension(new ParamDimension("param", ParamDimension.CONTRAVARIANT));
        FUNCTION.addDimension(new ParamDimension("return", variance(ParamDimension.COVARIANT)));

        // initialize the function type constructor
        TUPLE = new TypeCon("tuple");
        TUPLE.addDimension(new ParamDimension("elems", ParamDimension.COVARIANT));

        // add the global type constructors
        addGlobalTypeCon(BOOL);
        addGlobalTypeCon(INT);
        addGlobalTypeCon(MAP);
        addGlobalTypeCon(FUNCTION);

        // add binops for booleans
        addBinOp("and", BOOL, BOOL);
        addBinOp("or", BOOL, BOOL);
        addBinOp("xor", BOOL, BOOL);

        // add binops for integers
        addBinOp("+", INT, INT);
        addBinOp("-", INT, INT);
        addBinOp("/", INT, INT);
        addBinOp("*", INT, INT);
        addBinOp("&", INT, INT);
        addBinOp("|", INT, INT);
        addBinOp("^", INT, INT);
        addBinOp("<<", INT, INT);
        addBinOp(">>", INT, INT);

        // add logical not operator
        addUnOp("!", BOOL);

        // add unary integer operators
        addUnOp("-", INT);
        addUnOp("~", INT);

    }

    List<Integer> variance(int... i) {
        List<Integer> l = new LinkedList<Integer>();
        for ( int j : i) l.add(j);
        return l;
    }

    public TypeRef newIntTypeRef(Token sign, Token n, Token size) {
        List sign_list = new LinkedList();
        sign_list.add(sign);
        List size_list = new LinkedList();
        size_list.add(size);
        TypeRef ref = new TypeRef(n);
        ref.addDimension("sign", sign_list);
        ref.addDimension("size", size_list);
        return ref;
    }
}
