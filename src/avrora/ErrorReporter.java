package avrora;

import avrora.syntax.ProgramPoint;
import avrora.util.StringUtil;
import avrora.CompilationError;

/**
 * The <code>ErrorReporter</code> is the super class of all error reporters in
 * Avrora. It contains several utility methods that subclasses can use to generate
 * compilation errors that are thrown with much less code.
 *
 * @author Ben L. Titzer
 */
public class ErrorReporter {
    protected void error(String report, String name, ProgramPoint p) {
        throw new CompilationError(p, report, name, StringUtil.EMPTY_STRING_ARRAY);
    }

    protected void error(String report, String name, String p1, ProgramPoint p) {
        String[] ps = {p1};
        throw new CompilationError(p, report, name, ps);
    }

    protected void error(String report, String name, String p1, String p2, ProgramPoint p) {
        String[] ps = {p1, p2};
        throw new CompilationError(p, report, name, ps);
    }

    protected void error(String report, String name, String p1, String p2, String p3, ProgramPoint p) {
        String[] ps = {p1, p2, p3};
        throw new CompilationError(p, report, name, ps);
    }
}
