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
 * Creation date: Sep 28, 2005
 */

package jintgen.jigir;

import java.util.List;
import java.util.HashSet;

/**
 * The <code>TypeCon</code> class represents a type constructor that given a list
 * of type qualifiers (e.g. non-null, positive, etc), and a list of type parameters
 * (e.g. parameters to HashMap), can create a new <code>Type</code> instance.
 *
 * </p>
 * <code>TypeCon</code> instances correspond to flexible types. For example,
 * "array" is a type constructor which accepts an element type, "function"
 * constructs function types from parameter and return types, and each new
 * class in the program creates a new type constructor corresponding to its
 * name.
 *
 * </p>
 * <code>TypeCon</code> instances also contain information about what types
 * of operations are supported by values of types constructed by this type
 * constructor. For example, arrays support the <code>[]</code> indexing operation
 * and the <code>.length</code> member access, while function types support
 * the application operation.
 *
 * @author Ben L. Titzer
 */
public abstract class TypeCon {

    protected final String name;
    protected final HashSet<String> supportedQuals;
    protected final HashSet<String> supportedBinOps;
    protected final HashSet<String> supportedUnOps;

    /**
     * The protected constructor for the <code>TypeCon</code> class initializes
     * the name field.
     * @param n the name of this type constructor
     * @param qual the set of qualifiers supported by this type constructor
     * @param binop the set of binary operations supported by this type constructor
     * @param unop the set of unary operations supported by this type constructor
     */
    protected TypeCon(String n, String[] qual, String[] binop, String[] unop) {
        name = n;
        supportedQuals = set(qual);
        supportedBinOps = set(binop);
        supportedUnOps = set(unop);
    }

    private HashSet<String> set(String[] str) {
        HashSet<String> ret = new HashSet<String>();
        if ( str != null ) {
            for ( String s : str ) ret.add(s);
        }
        return ret;
    }

    /**
     * The <code>getName()</code> method returns the name of this type constructor.
     * @return a string representing the name of this type constructor
     */
    public final String getName() {
        return name;
    }

    /**
     * The <code>newType()</code> method creates a new <code>Type</code> instance
     * with the given qualifiers and type parameters.
     * @param qualifiers a list of type qualifiers for this type
     * @param types a list of type parameters (e.g. types of a HashMap) to this type
     * @return a new <code>Type</code> instance that represents the type
     */
    public abstract Type newType(TypeEnv te, List qualifiers, List types);

    /**
     * The <code>supportsQualifier()</code> method checks whether this type constructor
     * supports the specified type qualifier. Not all qualifiers can apply to all types;
     * for example, the "non-null" qualifier would not apply to primitive types, and
     * "positive" qualifier would not apply to reference types.
     * @param qual the qualifier represented as a string
     * @return true if types constructed by this <code>TypeCon</code> can legally have
     * the specified qualifier.
     */
    public boolean supportsQualifier(String qual) {
        return supportedQuals.contains(qual);
    }

    /**
     * The <code>supportsBinOp()</code> method checks whether this type constructor
     * supports the specified (infix) binary operator. Not all binary operators can
     * apply to all types; for example, the bitwise operators apply only to integers,
     * and the logical operators apply only to booleans. Reference types do not
     * support any binary operators.
     * @param binop the binary operator represented as a string
     * @return true if types constructed by this <code>TypeCon</code> can legally
     * support the specified binary operator
     */
    public boolean supportsBinOp(String binop) {
        return supportedBinOps.contains(binop);
    }

    /**
     * The <code>supportsUnOp()</code> method checks whether this type constructor
     * supports the specified unary operator. Not all unary operators can
     * apply to all types; for example, the bitwise complement operator "~" applies
     * only to integers, and the logical not operator "!" applies only to booleans.
     * Reference types do not support any binary operators.
     * @param unop the unary operator represented as a string
     * @return true if types constructed by this <code>TypeCon</code> can legally
     * support the specified unary operator
     */
    public boolean supportsUnOp(String unop) {
        return supportedUnOps.contains(unop);
    }

    /**
     * The <code>supportsIndex()</code> method checks whether this type constructor
     * supports indexing with the <code>[]</code> brackets. Not all types support
     * indexing; for example, in Java, only array types support this type
     * of indexing.
     * @return true if types constructed by this <code>TypeCon</code> can legally
     * support indexing with the <code>[]</code> brackets
     */
    public abstract boolean supportsIndex();

    /**
     * The <code>supportsRange()</code> method checks whether this type constructor
     * supports sub-range addressing with <code>[h:l]</code>. Not all types support
     * indexing; for example, in Virgil, only integer types support this type
     * of indexing.
     * @return true if types constructed by this <code>TypeCon</code> can legally
     * support range indexing with the <code>[h:l]</code> brackets
     */
    public abstract boolean supportsRange();

    /**
     * The <code>supportsMembers()</code> method checks whether this type constructor
     * supports access of members with the <code>.</code> operator. Not all types support
     * member access; for example, in Java, object types support field access and
     * array types support the <code>.length</code> idiom.
     * @return true if types constructed by this <code>TypeCon</code> can legally
     * support member access
     */
    public abstract boolean supportsMembers();
}
