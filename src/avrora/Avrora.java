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

package avrora;

import avrora.util.Terminal;

/**
 * The <code>Avrora</code> class contains several utilities relating to exceptions and errors within Avrora.
 *
 * @author Ben L. Titzer
 */
public class Avrora {

    /**
     * The <code>Error</code> class is the base class of all errors in Avrora. It provides a few extra utility
     * functions and is useful for distinguishing exceptions generated by Avrora itself and the Java runtime.
     */
    public static class Error extends java.lang.Error {

        protected final String message, param;
        public static boolean STACKTRACES;

        public Error(String p) {
            message = "Avrora Error";
            param = p;
        }

        public Error(String n, String p) {
            super(n);
            message = n;
            param = p;
        }

        public String getParam() {
            return param;
        }

        public void report() {
            Terminal.print(Terminal.ERROR_COLOR, message);
            Terminal.print(": " + param + '\n');
            if (STACKTRACES) {
                printStackTrace();
            }
        }
    }

    /**
     * The <code>InternalError</code> class is a class of errors corresponding to exceptional conditions
     * within Avrora.
     */
    public static class InternalError extends Error {
        public InternalError(String param) {
            super(param);
        }

        public void report() {
            Terminal.print(Terminal.ERROR_COLOR, "Avrora Internal Error");
            Terminal.print(": " + param + '\n');
            printStackTrace();
        }
    }

    public static InternalError unimplemented() {
        return new InternalError("unimplemented");
    }

    public static InternalError failure(String s) {
        return new InternalError(s);
    }

    public static void userError(String s) {
        throw new Error(s);
    }

    public static void userError(String s, String p) {
        throw new Error(s, p);
    }


}
