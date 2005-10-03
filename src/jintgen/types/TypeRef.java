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

import cck.parser.AbstractToken;
import java.util.List;
import java.util.HashMap;

/**
 * The <code>TypeRef</code> class represents a reference to a type within
 * a source program. This class is used to
 *
 * @author Ben L. Titzer
 */
public class TypeRef {

    /**
     * The <code>tcName</code> field stores a reference to the token in the source program
     * that mentions the type constructor's name (such as "int", "boolean", "array", etc).
     */
    protected final AbstractToken tcName;

    /**
     * The <code>dimensions</code> field stores a reference to a map that contains, for
     * each dimension, the values of the parameters to that dimension.
     */
    protected final HashMap<String, List> dimensions;

    /**
     * The <code>typeCon</code> field stores a reference to the type constructor for
     * this type reference. This field is not initialized until the type environment
     * is fully constructed and this type reference is resolved.
     */
    protected TypeCon typeCon;

    /**
     * The <code>type</code> field stores a reference to the actual type that this
     * type reference to refers to. This field is not initialized until the type
     * environment is fully constructed and this type reference is resolved.
     */
    protected Type type;

    /**
     * The constructor for the <code>TypeRef</code> class creates a new type reference
     * that refers to the specified type constructor.
     * @param n a token that represents the name of the specified type constructor
     */
    public TypeRef(AbstractToken n) {
        tcName = n;
        dimensions = new HashMap<String, List>();
    }

    public void addDimension(String name, List val) {
        dimensions.put(name, val);
    }

    public Type resolve(TypeEnv te) {
        if ( type == null ) {
            typeCon = te.resolveTypeCon(tcName.image);
            type = typeCon.newType(te, dimensions);
        }
        return type;
    }

    public TypeCon resolveTypeCon(TypeEnv te) {
        return te.resolveTypeCon(tcName.image);
    }
}
