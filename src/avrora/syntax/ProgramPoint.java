package avrora.syntax;

/**
 * The <code>ProgramPoint</code> class represents a location within a program
 * for the purposes of tracking error messages and debug information. It
 * encapsulates the module (file) contents, the line, the beginning column and
 * ending column.
 *
 * @author Ben L. Titzer
 */
public class ProgramPoint {
    // TODO: turn program point into interface

    public final String file;
    public final int line;
    public final int beginColumn;
    public final int endColumn;

    public ProgramPoint(String m, int l, int bc, int ec) {
        file = (m == null) ? "(unknown)" : m;
        line = l;
        beginColumn = bc;
        endColumn = ec;
    }

    public String toString() {
        return file + " " + line + ":" + beginColumn;
    }
}
