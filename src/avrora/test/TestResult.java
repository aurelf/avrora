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

package avrora.test;

import avrora.Avrora;
import avrora.CompilationError;
import avrora.util.Terminal;

/**
 * The <code>TestResult</code> class represents the result of running a test cases. The test run could succeed, it could
 * cause an internal error, an unexpected exception (e.g. <code>java.lang.NullPointerException</code>), or it could
 * generate an expected error such as a compilation error (for testing generation of error messages).
 *
 * @author Ben L. Titzer
 */
public abstract class TestResult {

    public abstract void shortReport();

    public boolean isSuccess() {
        return false;
    }

    public boolean isInternalError() {
        return false;
    }

    public boolean isUnexpectedException() {
        return false;
    }

    public boolean isMalformed() {
        return false;
    }

    public void longReport() {
        shortReport();
    }

    public static class TestSuccess extends TestResult {
        public void shortReport() {
            Terminal.print("passed");
        }

        public void longReport() {
            Terminal.print("passed");
        }

        public boolean isSuccess() {
            return true;
        }
    }

    public static class TestFailure extends TestResult {

        public final String message;

        public TestFailure() {
            message = "failed";
        }

        public TestFailure(String r) {
            message = r;
        }

        public void shortReport() {
            Terminal.print(message);
        }
    }

    public static class IncorrectError extends TestFailure {
        String expected;
        CompilationError encountered;

        public IncorrectError(String ex, CompilationError ce) {
            expected = ex;
            encountered = ce;
        }

        public void shortReport() {
            Terminal.print("expected error " + expected + ", but received " + encountered.getErrorClass());
        }

        public void longReport() {
            Terminal.println("expected error " + expected + " but received");
            encountered.report();
        }
    }

    public static class ExpectedPass extends TestFailure {
        CompilationError encountered;

        public ExpectedPass(CompilationError e) {
            encountered = e;
        }

        public void shortReport() {
            Terminal.print("expected pass, but received error " + encountered.getErrorClass());
        }

        public void longReport() {
            Terminal.println("expected pass, but received error");
            encountered.report();
        }
    }

    public static class ExpectedError extends TestFailure {
        String expected;

        public ExpectedError(String e) {
            expected = e;
        }

        public void shortReport() {
            Terminal.print("expected error " + expected + ", but passed");
        }
    }

    public static class InternalError extends TestFailure {
        Avrora.InternalError encountered;

        public InternalError(Avrora.InternalError e) {
            encountered = e;
        }

        public void shortReport() {
            Terminal.print("encountered internal error");
        }

        public void longReport() {
            Terminal.print("encountered internal error\n");
            encountered.report();
        }

        public boolean isInternalError() {
            return true;
        }
    }

    public static class UnexpectedException extends TestFailure {
        Throwable encountered;

        public UnexpectedException(Throwable e) {
            encountered = e;
        }

        public void shortReport() {
            Terminal.print("encountered unexpected exception " + encountered.getClass());
        }

        public void longReport() {
            Terminal.println("encountered unexpected exception");
            encountered.printStackTrace();
        }

        public boolean isUnexpectedException() {
            return true;
        }
    }

    public static class Malformed extends TestResult {
        String error;

        public Malformed(String e) {
            error = e;
        }

        public void shortReport() {
            Terminal.print("malformed testcase: " + error);
        }

        public boolean isMalformed() {
            return true;
        }
    }
}
