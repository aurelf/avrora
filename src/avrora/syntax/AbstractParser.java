package avrora.syntax;

import avrora.syntax.AbstractParseException;

/**
 * The <code>AbstractParser</code> is a superclass of all parsers introduced
 * to give multiple JavaCC-generated parsers a parent class.
 * @author Ben L. Titzer
 */
public abstract class AbstractParser {

    /**
     * The <code>module</code> field stores a reference to the module
     * that this parser is building.
     */
    protected Module module;

    /**
     * The <code>Module()</code> method causes the parser to begin parsing the module.
     * @throws AbstractParseException if the program does not parse correctly
     */
    public abstract void Module() throws AbstractParseException;
}
