/**
 * The profiling database. Collects information about profiling
 * and is implemented as a static class. This allows other tools
 * to register subclasses of ProfilingData and the Database will
 * collect them and report their statistics.
 **/
package avrora.util.profiling;

import avrora.util.Terminal;


public class ProfilingDatabase {

    static ProfilingDataList datalist = null;
    static boolean created = false;

    /**
     * Register a ProfilingData object
     **/
    public static void register(ProfilingData d) {
        datalist = new ProfilingDataList(d, datalist);
    }

    /**
     * Generate report of the profiling statistics.
     **/
    public static void reportData() {
        if (datalist == null) {
            Terminal.println("Profiling Database has no information.");
            return;
        }

        Terminal.println("Profiling Database collected " + datalist.length + " entries");

        ProfilingDataList dlist = datalist;
        int cntr = 1;

        while (dlist != null) {
            ProfilingData d = dlist.data;
            if (d.dataCollected()) {
                Terminal.println("\nProfilingData object " + cntr + ", instance of " + d.getClass().toString());
                d.computeStatistics();
                d.reportData();
            }
            dlist = dlist.next;
            cntr++;
        }
    }
}

/**
 * A class that represents a linked list of ProfilingData objects.
 **/
class ProfilingDataList {
    ProfilingData data;
    ProfilingDataList next;
    int length;

    ProfilingDataList(ProfilingData d, ProfilingDataList l) {
        data = d;
        next = l;
        if (l == null)
            length = 1;
        else
            length = 1 + l.length;
    }
}

