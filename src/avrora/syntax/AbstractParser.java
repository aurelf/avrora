
package avrora.syntax;

import vpc.core.AbstractParseException;
import vpc.VPCBase;
import avrora.Module;

/**
 */
public abstract class AbstractParser extends VPCBase {
    protected Module module;

    public abstract void Module() throws AbstractParseException;
}
