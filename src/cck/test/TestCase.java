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

package cck.test;

import cck.parser.SourceError;
import cck.text.StringUtil;
import cck.util.Util;
import java.util.Properties;

/**
 * The <code>TestCase</code> class encapsulates the notion of a test case in the automated testing framework.
 *
 * @author Ben L. Titzer
 */
public abstract class TestCase {

    public final String filename;
    protected final Properties properties;

    public TestCase(String fname, Properties props) {
        filename = fname;
        properties = props;
    }


    public String getFileName() {
        return filename;
    }

    public abstract void run() throws Exception;

    public TestResult match(Throwable t) {
        // default behavior: no exception = pass
        if (t == null)
            return new TestResult.TestSuccess();

        // internal error encountered.
        if (t instanceof Util.InternalError)
            return new TestResult.InternalError((Util.InternalError)t);

        // default: unexpected exception
        return new TestResult.UnexpectedException(t);
    }

    protected String expectProperty(String prop) {
        String value = properties.getProperty(prop);
        if ( value == null )
            Util.userError("Property "+ StringUtil.quote("Arch")+" not found in testcase");
        return trimString(value);
    }

    protected String trimString(String str) {
        return StringUtil.trimquotes(str.trim());
    }

    public abstract static class ExpectSourceError extends TestCase {

        boolean shouldPass;
        String error;

        public ExpectSourceError(String fname, Properties props) {
            super(fname, props);
            String result = StringUtil.trimquotes(props.getProperty("Result"));
            if (result.equals("PASS"))
                shouldPass = true;
            else {
                // format = "$id @ $num:$num"
                int i = result.indexOf("@");
                if (i >= 0)
                    error = result.substring(0, i).trim();
                else
                    error = result;
            }
        }

        public TestResult match(Throwable t) {

            if (shouldPass) {
                if (t == null) // no exceptions encountered, passed.
                    return new TestResult.TestSuccess();
                if (t instanceof SourceError) { // encountered compilation error.
                    SourceError ce = (SourceError)t;
                    return new TestResult.ExpectedPass(ce);
                }
            } else {
                if (t == null) // expected a compilation error, but passed.
                    return new TestResult.ExpectedError(error);

                if (t instanceof SourceError) {
                    SourceError ce = (SourceError)t;
                    if (ce.getErrorType().equals(error)) // correct error encountered.
                        return new TestResult.TestSuccess();
                    else // incorrect compilation error.
                        return new TestResult.IncorrectError(error, ce);
                }
            }

            return super.match(t);
        }
    }

    public static class Malformed extends TestCase {
        final String error;

        public Malformed(String fname, String e) {
            super(fname, null);
            error = e;
        }

        public void run() {
            // do nothing.
        }

        public TestResult match(Throwable t) {
            return new TestResult.Malformed(error);
        }
    }

    public static class InitFailure extends TestCase {
        final Throwable thrown;

        public InitFailure(String fname, Throwable t) {
            super(fname, null);
            thrown = t;
        }

        public void run() {
            // do nothing.
        }

        public TestResult match(Throwable t) {
            return new TestResult.Malformed(thrown.toString());
        }
    }


}
