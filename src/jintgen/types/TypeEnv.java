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

import jintgen.types.Type;
import jintgen.types.TypeCon;
import jintgen.isdl.Tuple3;
import jintgen.isdl.Tuple2;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The <code>TypeEnv</code> class represents a type environment that contains a
 * list of type constructors (<code>TypeCon</code> instances) and types
 * (<code>Type</code> instances). A new type environment can be constructed
 * and initialized with a language's initial type constructors (e.g. arrays
 * and function types) and populated later with types declared by the program.
 *
 * @author Ben L. Titzer
 */
public abstract class TypeEnv {

    protected final TypeEnv parent;
    protected final HashMap<String, TypeCon> typeCons;
    protected final HashMap<String, Type> types;
    protected final HashMap<Tuple3<String, TypeCon, TypeCon>, Object> binops;
    protected final HashMap<Tuple2<String, TypeCon>, Object> unops;

    /**
     * The constructor for the <code>TypeEnv</code> class creates an internal map
     * used to store type constructors and types and allows them to be resolved
     * by name.
     */
    protected TypeEnv() {
        parent = null;
        typeCons = new HashMap<String, TypeCon>();
        types = new HashMap<String, Type>();
        binops = new HashMap<Tuple3<String, TypeCon, TypeCon>, Object>();
        unops = new HashMap<Tuple2<String, TypeCon>, Object>();
    }

    /**
     * The <code>resolveTypeCon</code> method looks up a type constructor by its
     * unique string name.
     * @param name the name of the type constructor as a string
     * @return a reference to a <code>TypeCon</code> instance representing the
     * type constructor if it exists; null otherwise
     */
    public TypeCon resolveTypeCon(String name) {
        TypeCon typeCon = typeCons.get(name);
        if ( typeCon == null && parent != null ) return parent.resolveTypeCon(name);
        return typeCon;
    }

    /**
     * The <code>addLocalTypeCon()</code> method adds a new, named, type constructor
     * to this type environment. This method is used in the initialization of
     * a language's type environment (e.g. to add type constructors corresponding
     * to arrays, functions, etc) and during the processing of a program to
     * add user-defined types and type constructors to this environment
     * @param tc the new type constructor to add to this type environment
     */
    public void addLocalTypeCon(TypeCon tc) {
        typeCons.put(tc.getName(), tc);
    }

    /**
     * The <code>addGlobalTypeCon()</code> method adds a new, named, type constructor
     * to this type environment. This method is used in the initialization of
     * a language's type environment (e.g. to add type constructors corresponding
     * to arrays, functions, etc) and during the processing of a program to
     * add user-defined types and type constructors to this environment
     * @param tc the new type constructor to add to this type environment
     */
    public void addGlobalTypeCon(TypeCon tc) {
        if ( parent != null ) parent.addGlobalTypeCon(tc);
        else typeCons.put(tc.getName(), tc);
    }

    public void addBinOp(String binop, TypeCon a, TypeCon b) {
        binops.put(new Tuple3<String, TypeCon, TypeCon>(binop, a, b), binop);
    }

    public void addUnOp(String binop, TypeCon a) {
        unops.put(new Tuple2<String, TypeCon>(binop, a), binop);
    }

    /**
     * The <code>addType()</code> method adds a new type to this environment.
     * @param t the type to add to this environment
     */
    public void addType(Type t){
        types.put(t.toString(), t);
    }
}
