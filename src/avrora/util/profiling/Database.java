/**
 * @author Ben L. Titzer
 **/
package avrora.util.profiling;

import java.util.Vector;

abstract public class Database {

    protected Vector items;

    protected Database() {
        items = new Vector(25);
    }

    /**
     * Register a data item into this database.
     **/
    public void registerDataItem(DataItem d) {
        items.add(d);
    }

    /**
     * Generate a textual report of the information in this database.
     * Default behavior is to traverse all items in order of registering.
     **/
    public void textReport() {
        int numitems = items.size();

        for (int cntr = 0; cntr < numitems; cntr++) {
            DataItem i = (DataItem) items.get(cntr);
            i.textReport();
        }
    }

    /**
     * Process the data in the database.
     * Default behavior is to traverse all items in order of registering.
     **/
    public void processData() {
        int numitems = items.size();

        for (int cntr = 0; cntr < numitems; cntr++) {
            DataItem i = (DataItem) items.get(cntr);
            i.processData();
        }
    }

    /**
     * Accept a visitor into this database.
     * Default behavior is to traverse all items in order of registering.
     **/
    public void accept(DatabaseVisitor v) {
        int numitems = items.size();

        for (int cntr = 0; cntr < numitems; cntr++) {
            DataItem i = (DataItem) items.get(cntr);
            v.visit(i);
        }
    }

    public abstract boolean isEmpty();

    public abstract void freeze();

    public abstract String toString();
}
