/**
 * @author Ben L. Titzer
 * This class models statistical data.
 */
package avrora.util.profiling;

import avrora.util.Terminal;


/**
 * This models the min, max, mean, accumulation,
 * total, and number of occurrences of min and max
 * in a stream of integers.
 */
public class MinMaxMean extends DataItem {
    public float mean;
    public int observedMaximum;
    public int observedMinimum;
    public int countMinimum;
    public int countMaximum;
    public int total;
    public int accumulation;
    protected boolean someData;
    protected String totalname; // name to report for total field
    protected String cumulname; // name to report for cumul field

    /**
     * Public constructor initializes the statistics for a sequence
     * of integers.
     **/
    public MinMaxMean(String newname) {
        name = newname;
        totalname = "Total";
        cumulname = "Accumulation";
        someData = false;
    }

    /**
     * Public constructor initializes the statistics for a sequence
     * of integers.
     **/
    public MinMaxMean(String newname, String tn, String cn) {
        name = newname;
        totalname = tn;
        cumulname = cn;
        someData = false;
    }

    /**
     * Update the statistical data for the next input value.
     **/
    public void record(int value) {

        if (!someData) {
            observedMinimum = observedMaximum = value;
            countMinimum = countMaximum = 1;
            mean = value;
            accumulation = value;
            someData = true;
            total = 1;
            return;
        }

        if (value > observedMaximum) {
            observedMaximum = value;
            countMaximum = 1;
        } else if (value == observedMaximum)
            countMaximum++;

        if (value < observedMinimum) {
            observedMinimum = value;
            countMinimum = 1;
        } else if (value == observedMinimum)
            countMinimum++;

        accumulation += value;
        total++;
    }

    /**
     * process the data so far and update internal statistics.
     **/
    public void processData() {
        mean = ((float) accumulation) / ((float) total);
    }

    /**
     * Generate a textual report of the data gathered.
     **/
    public void textReport() {
        Terminal.print("\n " + name);
        Terminal.print("\n---------------------------------------------------------------------\n");

        if (totalname != null) {
            Terminal.print("   " + totalname + ": " + total);
        }
        if (cumulname != null) {
            Terminal.print("   " + cumulname + ": " + accumulation);
        }

        Terminal.print("\n Statistics: ");
        Terminal.print("\n   Minimum: " + observedMinimum + ", " + countMinimum + " occurences of min.");
        Terminal.print("\n   Maximum: " + observedMaximum + ", " + countMaximum + " occurences of max.");
        Terminal.print("\n   Mean: " + mean + "\n");
    }

    /**
     * Merge the results of two MinMaxMean objects into
     * one.
     **/
    public MinMaxMean merge(MinMaxMean m) {
        MinMaxMean result = new MinMaxMean(name);

        if (m.observedMaximum > observedMaximum) {
            result.observedMaximum = m.observedMaximum;
            result.countMaximum = m.countMaximum;
        } else if (m.observedMaximum == observedMaximum) {
            result.observedMaximum = observedMaximum;
            result.countMaximum = countMaximum + m.countMaximum;
        } else {
            result.observedMaximum = observedMaximum;
            result.countMaximum = countMaximum;
        }

        if (m.observedMinimum < observedMinimum) {
            result.observedMinimum = m.observedMinimum;
            result.countMinimum = m.countMinimum;
        } else if (m.observedMinimum == observedMinimum) {
            result.observedMinimum = observedMinimum;
            result.countMinimum = countMinimum + m.countMinimum;
        } else {
            result.observedMinimum = observedMinimum;
            result.countMinimum = countMinimum;
        }

        result.total = total + m.total;
        result.accumulation = accumulation + m.accumulation;
        result.mean = ((m.mean * m.total) + (mean * total)) / result.total;
        return result;
    }

    public boolean hasData() {
        return someData;
    }

    public String toString() {
        return name;
    }
}
