/**
 * @author Ben L. Titzer
 **/
package avrora.util.profiling;


/**
 * This class represents a data item that can be placed inside a database.
 * Things like Proportion, Distribution, and MinMaxMean inherit from this.
 **/
abstract public class DataItem {
    protected String name;

    public abstract void textReport();

    public abstract void processData();

    public abstract boolean hasData();

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
