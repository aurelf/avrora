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

package avrora.util.parser;

import avrora.syntax.ProgramPoint;


/**
 * The <code>AbstractParseException</code> represents a parse exception that was thrown by one of the parsers
 * generated by JavaCC. It is the supertype of all generated ParseException classes so that they can be
 * unified here.
 *
 * @author Ben L. Titzer
 */
public class AbstractParseException extends RuntimeException {
    /**
     * This constructor is used by the method "generateParseException" in the generated parser.  Calling this
     * constructor generates a new object of this type with the fields "currentToken",
     * "expectedTokenSequences", and "tokenImage" set.  The boolean flag "specialConstructor" is also set to
     * true to indicate that this constructor was used to create this object. This constructor calls its super
     * class with the empty string to force the "toString" method of parent class "Throwable" to print the
     * error message in the form: ParseException: <result of getMessage>
     */
    public AbstractParseException(AbstractToken currentTokenVal,
                                  int[][] expectedTokenSequencesVal,
                                  String[] tokenImageVal) {
        super("");
        specialConstructor = true;
        currentToken = currentTokenVal;
        expectedTokenSequences = expectedTokenSequencesVal;
        tokenImage = tokenImageVal;
        AbstractToken tok = currentToken.getNextToken();
        programPoint = new ProgramPoint(tok.file, tok.beginLine, tok.beginColumn, tok.endColumn);
    }

    /**
     * The following constructors are for use by you for whatever purpose you can think of.  Constructing the
     * exception in this manner makes the exception behave in the normal way - i.e., as documented in the
     * class "Throwable".  The fields "errorToken", "expectedTokenSequences", and "tokenImage" do not contain
     * relevant information.  The JavaCC generated code does not use these constructors.
     */

    public AbstractParseException() {
        super();
        specialConstructor = false;
    }

    public AbstractParseException(String message) {
        super(message);
        specialConstructor = false;
    }


    /**
     * This variable determines which constructor was used to create this object and thereby affects the
     * semantics of the "getMessage" method (see below).
     */
    protected boolean specialConstructor;
    /**
     * This is the last token that has been consumed successfully.  If this object has been created due to a
     * parse error, the token followng this token will (therefore) be the first error token.
     */
    public AbstractToken currentToken;
    /**
     * Each entry in this array is an array of integers.  Each array of integers represents a sequence of
     * tokens (by their ordinal values) that is expected at this point of the parse.
     */
    public int[][] expectedTokenSequences;
    /**
     * This is a reference to the "tokenImage" array of the generated parser within which the parse error
     * occurred. This array is defined in the generated ...Constants interface.
     */
    public String[] tokenImage;
    /**
     * The end of line string for this machine.
     */
    protected String eol = System.getProperty("line.separator", "\n");

    public ProgramPoint programPoint;

    /**
     * Used to convert raw characters to their escaped version when these raw version cannot be used as part
     * of an ASCII string literal.
     */
    protected String add_escapes(String str) {
        StringBuffer retval = new StringBuffer();
        char ch;
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)) {
                case 0:
                    continue;
                case '\b':
                    retval.append("\\b");
                    continue;
                case '\t':
                    retval.append("\\t");
                    continue;
                case '\n':
                    retval.append("\\n");
                    continue;
                case '\f':
                    retval.append("\\f");
                    continue;
                case '\r':
                    retval.append("\\r");
                    continue;
                case '\"':
                    retval.append("\\\"");
                    continue;
                case '\'':
                    retval.append("\\\'");
                    continue;
                case '\\':
                    retval.append("\\\\");
                    continue;
                default:
                    if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                        String s = "0000" + Integer.toString(ch, 16);
                        retval.append("\\u" + s.substring(s.length() - 4, s.length()));
                    } else {
                        retval.append(ch);
                    }
                    continue;
            }
        }
        return retval.toString();
    }
}
