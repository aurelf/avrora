
package avrora.syntax;

import vpc.core.AbstractParseException;
import avrora.Module;

/**
 */
public abstract class AbstractParser {
    protected Module module;

    public abstract void Module() throws AbstractParseException;
}
