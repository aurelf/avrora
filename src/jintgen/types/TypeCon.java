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

package jintgen.types;

import jintgen.isdl.HashList;
import java.util.*;
import cck.util.Util;

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
public class TypeCon {

    protected final String name;
    protected final HashList<String, Dimension> dimensions;

    /**
     * The <code>Dimension</code> class represents a new type dimension that applies
     * to a type constructor. A dimension might be the size of an integer type,
     * the types of parameters to a function, the element type of an array, or a
     * qualifier that has been introduced by the user.
     */
    protected abstract static class Dimension {

        protected final String name;

        /**
         * The default constructor for the <code>Dimension</code> class simply accepts
         * the name of the dimension as a string that is used to distinguish it from
         * other type dimensions.
         * @param n the name of the type dimension as a string
         */
        protected Dimension(String n) {
            name = n;
        }

        public abstract Object build(List params);
    }

    /**
     * The protected constructor for the <code>TypeCon</code> class initializes
     * the name field.
     * @param n the name of this type constructor
     */
    public TypeCon(String n) {
        name = n;
        dimensions = new HashList<String, Dimension>();
    }

    public Type newType(TypeEnv te) {
        throw Util.unimplemented();
    }

    public Type newType(TypeEnv te, HashMap<String, List> dims) {
        throw Util.unimplemented();
    }

    /**
     * The <code>addDimension()</code> method adds a new dimension to this type constructor.
     * The new dimension has a string name that can be used to retrieve the dimension later.
     * @param d the new type dimension to add to this type constructor
     */
    public void addDimension(Dimension d) {
        dimensions.add(d.name, d);
    }

    /**
     * The <code>getDimension()</code> method looks up the specified type dimension for this
     * type constructor given the name of the dimension as a string.
     * @param name the name of the type dimension as a string
     * @return a reference to the <code>Dimension</code> instance if this type constructor
     * supports the dimension; null otherwise
     */
    public Dimension getDimension(String name) {
        return dimensions.get(name);
    }

    /**
     * The <code>getName()</code> method returns the name of this type constructor.
     * @return a string representing the name of this type constructor
     */
    public final String getName() {
        return name;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(name);
        buf.append("(");
        boolean first = true;
        for ( Dimension d : dimensions ) {
            if ( !first ) buf.append(", ");
            buf.append(d.name);
            first = false;
        }
        buf.append("(");
        return name;
    }

    /**
     * The <code>supportsIndex()</code> method checks whether this type constructor
     * supports indexing with the <code>[]</code> brackets. Not all types support
     * indexing; for example, in Java, only array types support this type
     * of indexing.
     * @return true if types constructed by this <code>TypeCon</code> can legally
     * support indexing with the <code>[]</code> brackets
     */
    public boolean supportsIndex() {
        return false;
    }

    /**
     * The <code>supportsRange()</code> method checks whether this type constructor
     * supports sub-range addressing with <code>[h:l]</code>. Not all types support
     * indexing; for example, in Virgil, only integer types support this type
     * of indexing.
     * @return true if types constructed by this <code>TypeCon</code> can legally
     * support range indexing with the <code>[h:l]</code> brackets
     */
    public boolean supportsRange() {
        return false;
    }

    /**
     * The <code>supportsMembers()</code> method checks whether this type constructor
     * supports access of members with the <code>.</code> operator. Not all types support
     * member access; for example, in Java, object types support field access and
     * array types support the <code>.length</code> idiom.
     * @return true if types constructed by this <code>TypeCon</code> can legally
     * support member access
     */
    public boolean supportsMembers() {
        return false;
    }

    /**
     * The <code>supportsApplication()</code> method checks whether this type constructor
     * supports function application. Few types support application to an expression
     * or list of expressions; in Virgil, only the "function" family of types do.
     * @return true if types constructed by this <code>TypeCon</code> can legally
     * support application
     */
    public boolean supportsApplication() {
        return false;
    }
}
