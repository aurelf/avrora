package avrora.syntax;

import avrora.syntax.AbstractParseException;

/**
 */
public abstract class AbstractParser {
    protected Module module;

    public abstract void Module() throws AbstractParseException;
}
