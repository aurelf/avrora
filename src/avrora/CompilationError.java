package avrora;

import avrora.syntax.ProgramPoint;
import avrora.util.Terminal;

/**
 * The <code>CompilationError</code> class represents an error in a
 * user program, including the module contents and line and column
 * numbers.
 *
 * @author Ben L. Titzer
 */
public class CompilationError extends Avrora.Error {
    public final ProgramPoint point;
    public final String errclass;
    public final String[] errparams;

    public static boolean CLASSES = false;

    public CompilationError(ProgramPoint p, String msg, String ec, String ps[]) {
        super(msg, null);
        point = p;
        errclass = ec;
        errparams = ps;
    }

    public void report() {
        Terminal.printRed(point.file);
        Terminal.print(" ");
        Terminal.printBrightBlue(point.line + ":" + point.beginColumn);
        Terminal.print(": ");
        if (CLASSES)
            Terminal.print(errclass + ": ");
        Terminal.print(message);
        Terminal.print("\n");
        if (STACKTRACES) {
            printStackTrace();
        }
    }

    public String getErrorClass() {
        return errclass;
    }

    public String[] getErrorParams() {
        return errparams;
    }
}
