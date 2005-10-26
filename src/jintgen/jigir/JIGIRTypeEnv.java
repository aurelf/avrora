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

import cck.util.Util;
import jintgen.isdl.parser.Token;
import jintgen.isdl.JIGIRErrorReporter;
import jintgen.isdl.OperandTypeDecl;
import jintgen.isdl.EnumDecl;
import jintgen.types.*;
import java.util.*;

/**
 * The <code>JIGIRTypeEnv</code> class represents a type environment for
 * JIGIR code. It supports void, boolean, integers, maps, and function
 * types.
 *
 * @author Ben L. Titzer
 */
public class JIGIRTypeEnv extends TypeEnv {
    public final Type VOID;
    public final Type BOOLEAN;
    public final TypeCon INT;
    public final TypeCon MAP;
    public final TypeCon FUNCTION;
    public final Relation ASSIGNABLE;
    public final Relation COMPARABLE;
    public final Relation PROMOTABLE;
    public Arith.SHL SHL;
    public Arith.SHR SHR;
    public Arith.AND AND;

    /**
     * The <code>TYPECON_int</code> class represents the type constructor for types
     * based on "int". In JIGIR, such types have both a sign and a size.
     */
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

        public Type newType(TypeEnv te, HashMap<String, List> dims) {
            HashMap<String, Object> dimInst = buildDimensions(te, dims);
            Type type = types.get(dimInst);
            if ( type != null ) return type;
            Boolean sign = (Boolean) dimInst.get("sign");
            if ( sign == null ) sign = true;
            Integer len = (Integer) dimInst.get("size");
            // TODO: assuming 32 bit integers is a hack!
            if ( len == null ) len = 32;
            TYPE_int type_int = new TYPE_int(sign, len, dimInst);
            types.put(dimInst, type_int);
            return type_int;
        }

    }

    /**
     * The <code>TYPE_int</code> class represents integer types that
     * include a sign and a size.
     */
    public class TYPE_int extends Type {
        protected final boolean signed;
        protected final int size;

        protected TYPE_int(boolean sign, int len, HashMap<String, Object> dims) {
            super(INT, dims);
            signed = sign;
            size = len;
        }

        protected TYPE_int(boolean sign, int len) {
            super(INT, new HashMap<String, Object>());
            signed = sign;
            size = len;
        }

        public boolean isSigned() {
            return signed;
        }

        public int getSize() {
            return size;
        }

        public String toString() {
            return (signed ? "-int." : "+int.") + size;
        }
    }

    public class TYPE_enum extends TypeCon {
        public final EnumDecl decl;
        protected TYPE_enum(EnumDecl decl) {
            super(decl.name.image);
            this.decl = decl;
        }
    }

    public class TYPE_enum_kind extends TypeCon {
        public final EnumDecl decl;
        protected TYPE_enum_kind(EnumDecl decl) {
            super(decl.name.image+"$kind");
            this.decl = decl;
        }
    }

    public class TYPE_operand extends TypeCon {
        public final OperandTypeDecl decl;
        protected TYPE_operand(OperandTypeDecl decl) {
            super(decl.name.image);
            this.decl = decl;
        }
    }

    public JIGIRTypeEnv(JIGIRErrorReporter er) {
        super(er);

        ASSIGNABLE = new TransitiveRelation("assignable");
        COMPARABLE = new TransitiveRelation("comparable");
        PROMOTABLE = new Relation("promotable");

        // initialize the boolean type constructor
        TypeCon VTC = new TypeCon("void");
        VOID = VTC.newType(this);
        // initialize the boolean type constructor
        TypeCon BOOL = new TypeCon("boolean");
        BOOLEAN = BOOL.newType(this);

        // initialize the integer type constructor
        INT = new TYPECON_int();

        // initialize the map type constructor
        MAP = new TypeCon("map");
        MAP.addDimension(new ParamDimension("types", variance(ParamDimension.INVARIANT, ParamDimension.INVARIANT)));

        // initialize the function type constructor
        FUNCTION = new TypeCon("function");
        FUNCTION.addDimension(new ParamDimension("param", ParamDimension.CONTRAVARIANT));
        FUNCTION.addDimension(new ParamDimension("return", variance(ParamDimension.COVARIANT)));

        // add the global type constructors
        addTypeCon(VTC);
        addTypeCon(BOOL, COMPARABLE, ASSIGNABLE, PROMOTABLE);
        addTypeCon(INT, COMPARABLE, ASSIGNABLE, PROMOTABLE);
        addTypeCon(MAP, ASSIGNABLE);
        addTypeCon(FUNCTION);

        // add the relations
        addRelation(ASSIGNABLE);
        addRelation(COMPARABLE);
        addRelation(PROMOTABLE);

        // add binops for booleans
        addBinOp(BOOL, BOOL, new Logical.AND());
        addBinOp(BOOL, BOOL, new Logical.OR());
        addBinOp(BOOL, BOOL, new Logical.XOR());
        addBinOp(BOOL, BOOL, new Logical.EQU());
        addBinOp(BOOL, BOOL, new Logical.NEQU());

        // add comparisons for booleans
        addBinOp(BOOL, BOOL, new Logical.EQU());
        addBinOp(BOOL, BOOL, new Logical.NEQU());

        // add comparisons for integers
        addBinOp(INT, INT, new Logical.EQU());
        addBinOp(INT, INT, new Logical.NEQU());
        addBinOp(INT, INT, new Logical.GR());
        addBinOp(INT, INT, new Logical.GREQ());
        addBinOp(INT, INT, new Logical.LESS());
        addBinOp(INT, INT, new Logical.LESSEQU());

        // add binops for integers
        addBinOp(INT, INT, new Arith.ADD());
        addBinOp(INT, INT, new Arith.SUB());
        addBinOp(INT, INT, new Arith.DIV());
        addBinOp(INT, INT, new Arith.MOD());
        addBinOp(INT, INT, new Arith.MUL());
        AND = new Arith.AND();
        addBinOp(INT, INT, AND);
        addBinOp(INT, INT, new Arith.OR());
        addBinOp(INT, INT, new Arith.XOR());
        SHL = new Arith.SHL();
        addBinOp(INT, INT, SHL);
        SHR = new Arith.SHR();
        addBinOp(INT, INT, SHR);

        // add logical not operator
        addUnOp(BOOL, new Logical.NOT());

        // add unary integer operators
        addUnOp(INT, new Arith.NEG());
        addUnOp(INT, new Arith.COMP());
        addUnOp(INT, new Arith.UNSIGN());
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

    public Type newIntType(boolean sign, int size) {
        List sign_list = new LinkedList();
        sign_list.add(sign);
        List size_list = new LinkedList();
        size_list.add(size);
        HashMap<String, List> map = new HashMap<String, List>();
        map.put("size", size_list);
        map.put("sign", sign_list);
        return INT.newType(this, map);
    }

    public Type resolveOperandType(OperandTypeDecl ot) {
        TypeCon tc = resolveTypeCon(ot.name.image);
        return tc.newType(this);
    }

    public Type resolveType(Token tok) {
        TypeCon tc = resolveTypeCon(tok.image);
        if ( tc == null ) ERROR.UnresolvedType(tok);
        return tc.newType(this);
    }

    public void addOperandType(OperandTypeDecl ot) {
        TypeCon tycon = new TYPE_operand(ot);
        addTypeCon(tycon, ASSIGNABLE, COMPARABLE);
    }

    public TYPE_enum_kind addEnum(EnumDecl ot) {
        addTypeCon(new TYPE_enum(ot), ASSIGNABLE, COMPARABLE);
        TYPE_enum_kind tc = new TYPE_enum_kind(ot);
        addTypeCon(tc);
        return tc;
    }
}
