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
 * Created Sep 20, 2005
 */
package jintgen.jigir;

import jintgen.isdl.parser.Token;
import cck.text.StringUtil;

/**
 * The <code>Type</code> class represents a type within the IR representation
 * of interpreter and operand code. A type is either an enum (which has an
 * underlying integral type) or an integral type with a size.
 *
 * @author Ben L. Titzer
 */
public class Type {

    public static Type BOOLEAN = new Type(false, "boolean", 1);

    protected final boolean signed;
    protected final Token baseType;
    protected final int width;

    public Type(Token s, Token bt, Token w) {
        baseType = bt;

        if ( s == null ) signed = true;
        else signed = s.image.equals("-");
        if ( w == null ) width = 32;
        else width = StringUtil.evaluateIntegerLiteral(w.image);
    }

    public Type(boolean s, String bt, int w) {
        baseType = new Token();
        baseType.image = bt;
        signed = s;
        width = w;
    }

    /**
     * The <code>isSigned()</code> method returns whether this type is signed or not.
     * @return true if this type is signed (i.e. contains a sign bit); false otherwise
     */
    public boolean isSigned() {
        return signed;
    }

    /**
     * The <code>getWidth()</code> method returns the width of this type in bits, including
     * the sign bit, if it exists.
     * @return the size of this type in bits, including any sign bit
     */
    public int getWidth() {
        return width;
    }

    /**
     * The <code>isAssignableFrom()</code> method is the main mechanism in type checking. For
     * each variable, parameter, or field that has a declared type, when an assignment occurs,
     * this method is called with the computed type of the expression and the declared type
     * to check that the assignment is allowed.
     * @param other the type of the expression being assigned
     * @return true if this type can be assigned from the specified type; false otherwise
     */
    public boolean isAssignableFrom(Type other) {
        // base types have to match.
        if ( !baseType.image.equals(other.baseType.image) ) return false;
        // if the same sign, then the width has to be big enough
        if ( signed == other.signed ) return (width >= other.width);
        // if this type is signed (and the other is not), need an extra bit
        return signed && (this.width > other.width);
    }

    /**
     * The <code>isComparableTo()</code> method checks whether this type can be compared to
     * values of the specified type.
     * @param other the other type that this type is being compared to
     * @return true if this type can be compared to the specified type; false otherwise
     */
    public boolean isComparableTo(Type other) {
        if ( !baseType.image.equals(other.baseType.image) ) return false;
        return true;
    }

    /**
     * The <code>toString()</code> method returns a string representation of this type, including
     * whether it is signed (or unsigned) and its width in bits.
     * @return a string representation of this type
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(10);
        buf.append(signed ? '-' : '+');
        buf.append(baseType.image);
        buf.append(".");
        buf.append(width);
        return buf.toString();
    }

    /**
     * The <code>getBaseType()</code> method returns a reference to a <code>Token</code> that represents
     * the base type (such as "int") of this type.
     * @return a the <code>Token</code> corresponding to the base type of this type
     */
    public Token getBaseType() {
        return baseType;
    }

    /**
     * The <code>getBaseName()</code> method returns a string representation of the name of this
     * type's base type.
     * @return a string that represents the name of the base type
     */
    public String getBaseName() {
        return baseType.image;
    }

    /**
     * The <code>getJavaType()</code> method returns a string that represents the java type that
     * is used to represent values of this type. This is used in generating Java code that implements
     * the instruction IR and interpreter.
     * @return a string representing the name of the Java type used to represent values of this
     * type
     */
    public String getJavaType() {
        // TODO: fix java type for smaller bit fields, etc
        return baseType.image;
    }

    /**
     * The <code>isBasedOn()</code> method checks whether this type matches the specified base type name.
     * @param s the name of the base type to check against
     * @return true if this type is based on the specified base type; false otherwise
     */
    public boolean isBasedOn(String s) {
        return baseType.image.equals(s);
    }
}
