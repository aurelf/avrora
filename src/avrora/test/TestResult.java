package avrora.test;

import avrora.Avrora;
import avrora.CompilationError;
import avrora.util.Terminal;

/**
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
