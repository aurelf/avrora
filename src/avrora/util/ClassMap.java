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

package avrora.util;

import avrora.Avrora;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

/**
 * The <code>ClassMap</code> is a class that maps short names (i.e. short,
 * lower case strings) to java classes and can instantiate them. This is
 * useful for dynamic resolution of classes but with a small set of known
 * defaults that have a short name. If the short name is not in the default
 * set, this class will treat the short name as a fully qualified Java class
 * name and load it.
 *
 * @author Ben L. Titzer
 */
public class ClassMap {

    protected final String type;
    protected final Class clazz;
    protected final HashMap classMap;
    protected final HashMap objMap;

    public ClassMap(String t, Class clz) {
        clazz = clz;
        classMap = new HashMap();
        objMap = new HashMap();
        type = t;
    }

    /**
     * The <code>addClass()</code> method adds a short name (alias) for the specified class
     * to the set of default class names.
     * @param shortName the string representation of the alias of the class
     * @param clz the class to which the alias maps
     */
    public void addClass(String shortName, Class clz) {
        classMap.put(shortName, clz);
    }

    public void addInstance(String shortName, Object o) {
        objMap.put(shortName, o);
        classMap.put(shortName, o.getClass());
    }

    public Class getClass(String shortName) {
        Object o = objMap.get(shortName);
        if ( o != null ) return o.getClass();
        return (Class)classMap.get(shortName);
    }

    /**
     * The <code>getObjectOfClass()</code> method looks up the string name of the class
     * in the alias map first, and if not found, attempts to load the class using
     * <code>Class.forName()</code> and instantiates one object.
     * @param name the name of the class or alias
     * @return an instance of the specified class
     * @throws Avrora.Error if there is a problem finding or instantiating the class
     */
    public Object getObjectOfClass(String name) {
        Object o = objMap.get(name);
        if ( o != null ) return o;

        String clname = StringUtil.quote(name);

        Class c = (Class) classMap.get(name);
        if (c == null) {
            try {
                c = Class.forName(name);
            } catch (ClassNotFoundException e) {
                Avrora.userError(type + " class not found", clname);
            }
        } else {
            clname = clname + " (" + c.toString() +")";
        }

        if (!(clazz.isAssignableFrom(c)))
            Avrora.userError("The specified class does not extend " + clazz.getName(), clname);

        try {
            return c.newInstance();
        } catch (InstantiationException e) {
            Avrora.userError("The specified class does not have a default constructor", clname);
        } catch (IllegalAccessException e) {
            Avrora.userError("Illegal access to class", clname);
        }

        // UNREACHABLE
        throw Avrora.failure("Unreachable state in dynamic instantiation of class");
    }

    public List getSortedList() {
        List list = Collections.list(Collections.enumeration(classMap.keySet()));
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    public Iterator iterator() {
        return classMap.keySet().iterator();
    }
}
