package avrora.test;

import avrora.Avrora;
import avrora.CompilationError;
import avrora.test.TestResult;
import avrora.util.StringUtil;

import java.util.Properties;

/**
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
        if (t instanceof Avrora.InternalError)
            return new TestResult.InternalError((Avrora.InternalError) t);

        // default: unexpected exception
        return new TestResult.UnexpectedException(t);
    }

    public abstract static class ExpectCompilationError extends TestCase {

        boolean shouldPass;
        String error;

        public ExpectCompilationError(String fname, Properties props) {
            super(fname, props);
            String result = StringUtil.trimquotes(props.getProperty("Result"));
            if (result.equals("PASS"))
                shouldPass = true;
            else {
                // TODO: could clean up some using StringUtil
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
                if (t instanceof CompilationError) { // encountered compilation error.
                    CompilationError ce = (CompilationError) t;
                    return new TestResult.ExpectedPass(ce);
                }
            } else {
                if (t == null) // expected a compilation error, but passed.
                    return new TestResult.ExpectedError(error);

                if (t instanceof CompilationError) {
                    CompilationError ce = (CompilationError) t;
                    if (ce.getErrorClass().equals(error)) // correct error encountered.
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


}
