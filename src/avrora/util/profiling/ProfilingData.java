/**
 * @author Ben L. Titzer
 * This class is used to record instances of profiling data from a Hashtable.
 **/
package avrora.util.profiling;

import avrora.util.Terminal;


public abstract class ProfilingData {

    abstract void computeStatistics();

    abstract void reportData();

    abstract void merge(ProfilingData d);

    abstract boolean dataCollected();

    void printDistribution(int base, int data[]) {
        int max, cntr;
        float scale = 1;

        if (data.length == 0) return;

        for (max = data[0], cntr = 0; cntr < data.length; cntr++) {
            if (data[cntr] > max) max = data[cntr];
        }

        if (max > 70) scale = ((float) max) / 70;

        for (cntr = 0; cntr < data.length; cntr++) {
            float fstars = ((float) data[cntr]) / scale;
            int stars = (int) fstars;
            if ((fstars - stars) >= 0.5) stars++;

            Terminal.print("\n" + (base + cntr) + ":" + data[cntr] + "\t");

            for (int scntr = 0; scntr < stars; scntr++) {
                Terminal.print("*");
            }
        }
        Terminal.print("\n");
    }
}
