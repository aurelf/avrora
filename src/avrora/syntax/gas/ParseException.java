/* Generated By:JavaCC: Do not edit this line. ParseException.java Version 3.0 */
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

package avrora.syntax.gas;

import avrora.util.parser.AbstractParseException;

/**
 * This exception is thrown when parse errors are encountered. You can explicitly create objects of this
 * exception type by calling the method generateParseException in the generated parser.
 * <p/>
 * You can modify this class to customize your error reporting mechanisms so long as you retain the public
 * fields.
 */
public class ParseException extends AbstractParseException {

    /**
     * This constructor is used by the method "generateParseException" in the generated parser.  Calling this
     * constructor generates a new object of this type with the fields "currentToken",
     * "expectedTokenSequences", and "tokenImage" set.  The boolean flag "specialConstructor" is also set to
     * true to indicate that this constructor was used to create this object. This constructor calls its super
     * class with the empty string to force the "toString" method of parent class "Throwable" to print the
     * error message in the form: ParseException: <result of getMessage>
     */
    public ParseException(Token currentTokenVal,
                          int[][] expectedTokenSequencesVal,
                          String[] tokenImageVal) {
        super(currentTokenVal, expectedTokenSequencesVal, tokenImageVal);
    }

    /**
     * The following constructors are for use by you for whatever purpose you can think of.  Constructing the
     * exception in this manner makes the exception behave in the normal way - i.e., as documented in the
     * class "Throwable".  The fields "errorToken", "expectedTokenSequences", and "tokenImage" do not contain
     * relevant information.  The JavaCC generated code does not use these constructors.
     */

    public ParseException() {
        super();
    }

    public ParseException(String message) {
        super(message);
    }

    /**
     * This method has the standard behavior when this object has been created using the standard
     * constructors. Otherwise, it uses "currentToken" and "expectedTokenSequences" to generate a parse error
     * message and returns it. If this object has been created due to a parse error, and you do not catch it
     * (it gets thrown from the parser), then this method is called during the printing of the final stack
     * trace, and hence the correct error message gets displayed.
     */
    public String getMessage() {
        if (!specialConstructor) {
            return super.getMessage();
        }
        String expected = "";
        int maxSize = 0;
        for (int i = 0; i < expectedTokenSequences.length; i++) {
            if (maxSize < expectedTokenSequences[i].length) {
                maxSize = expectedTokenSequences[i].length;
            }
            for (int j = 0; j < expectedTokenSequences[i].length; j++) {
                expected += tokenImage[expectedTokenSequences[i][j]] + ' ';
            }
            if (expectedTokenSequences[i][expectedTokenSequences[i].length - 1] != 0) {
                expected += "...";
            }
            expected += eol + "    ";
        }
        String retval = "Encountered \"";
        Token tok = ((Token)currentToken).next;
        Token next = tok;
        for (int i = 0; i < maxSize; i++) {
            if (i != 0) retval += " ";
            if (tok.kind == 0) {
                retval += tokenImage[0];
                break;
            }
            retval += add_escapes(tok.image);
            tok = tok.next;
        }
        retval += "\" at line " + next.beginLine + ", column " + next.beginColumn;
        retval += '.' + eol;
        if (expectedTokenSequences.length == 1) {
            retval += "Was expecting:" + eol + "    ";
        } else {
            retval += "Was expecting one of:" + eol + "    ";
        }
        retval += expected;
        return retval;
    }


}
