/**
 * @author Ben L. Titzer
 **/
package avrora.util.profiling;

import avrora.util.Terminal;


/**
 * This class implements a simple counter as a data item. It is
 * used by more complicated data item classes.
 **/
public class Counter extends DataItem {
    protected int count;

    public Counter(String newname) {
        name = newname;
    }

    public Counter(String newname, int newcount) {
        name = newname;
        count = newcount;
    }

    public int getTotal() {
        return count;
    }

    public void reset() {
        count = 0;
    }

    public void increment() {
        count++;
    }

    public void increment(int num) {
        count += num;
    }

    public void textReport() {
        Terminal.print("\n " + name + ": " + count);
    }

    public void processData() {
    }

    public boolean hasData() {
        return true;
    }
}
