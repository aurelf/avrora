/**
 * @author Ben L. Titzer
 * This class is used to record instances of profiling data from a Hashtable.
 **/
package avrora.util.profiling;

import avrora.util.Terminal;


public class HTProfilingData extends ProfilingData {

    private Class htClass;
    private int bucketUsage[];

    private Distribution successes;
    private Distribution failures;
    private Distribution bucketStats;

    private Object htObject = null;
    private int htSize = 0;
    private int totalSearches = 0;
    private float htUsage;
    private float failurePercent;
    private float successPercent;
    private boolean bucketUsed; // true if one or more bucket used.
    private CallStack callStack; // used to get the stack trace

    /**
     * The constructor that creates an object for profiling. Takes an
     * Object representing the Hashtable and a size representing the
     * size of the hashtable.
     **/
    public HTProfilingData(Object ht, int size) {
        htClass = ht.getClass();
        bucketUsage = new int[size];
        htSize = size;
        htObject = ht;
        ProfilingDatabase.register(this);
        successes = new Distribution("Successes");
        failures = new Distribution("Failures");
        bucketUsed = false;
        callStack = new CallStack();
    }

    public boolean dataCollected() {
        return bucketUsed || (failures.total > 0) || (successes.total > 0);
    }

    /**
     * Record a search failure and the number of compares that were
     * performed before reaching the end of the Binding list.
     **/
    public void recordSearchFailure(int length) {
        failures.record(length);
    }

    /**
     * Record a search success and the number of compares that were
     * performed before reaching the item in the Binding list.
     **/
    public void recordSearchSuccess(int length) {
        successes.record(length);
    }

    /**
     * Record a Binding being inserted into a bucket. This is used
     * to compute statistics on the hashtable usage.
     **/
    public void recordBucketUse(int offset) {
        bucketUsed = true;
        bucketUsage[offset]++;
    }

    /**
     * Compute the statistics for hashtable usage. Compute total searches
     * Success and failure percentage, as well as bucket statistics.
     **/
    public void computeStatistics() {
        bucketStats = new Distribution("Bucket Statistics");

        for (int cntr = 0; cntr < htSize; cntr++) {
            bucketStats.record(bucketUsage[cntr]);
        }
        bucketStats.processData();
        successes.processData();
        failures.processData();
        totalSearches = successes.total + failures.total;
        htUsage = ((float) bucketStats.accumulation * 100) / htSize;
        successPercent = ((float) successes.total * 100) / totalSearches;
        failurePercent = ((float) failures.total * 100) / totalSearches;
    }

    /**
     * Merge (average) the results of this profiling data and another.
     **/
    public void merge(ProfilingData d) {
    }

    /**
     * Generate a textual report of the profiling data.
     **/
    public void reportData() {
        Terminal.println("Hashtable profiling data for " + htClass.toString());
        Terminal.print("Stack trace for creation: ");
        callStack.printStackTrace();

        if (bucketStats.accumulation > 0) {
            Terminal.println(
                    "\n Usage: " + bucketStats.accumulation + " Bindings in " + htSize + " buckets, " + htUsage + "%" +
                    "\n Bucket statistics: " +
                    "\n   Minimum usage: " + bucketStats.observedMinimum + ", " + bucketStats.countMinimum + " occurences of min." +
                    "\n   Maximum usage: " + bucketStats.observedMaximum + ", " + bucketStats.countMaximum + " occurences of max." +
                    "\n   Mean usage: " + bucketStats.mean + ", Median: " + bucketStats.median);
            Terminal.print("\nUsage distribution: ");
            printDistribution(bucketStats.observedMinimum, bucketStats.distrib);
            //	    System.out.print("\n\nBucket distribution: ");
            //	    printDistribution(0,bucketUsage);
        } else {
            Terminal.println(" No Bucket Statistics ");
        }
        if (totalSearches > 0) {
            Terminal.println(
                    "\n Search statistics: " +
                    "\n   For search failures: " + failurePercent + "%" +
                    "\n      Minimum compares: " + failures.observedMinimum + ", " + failures.countMinimum + " occurences of min." +
                    "\n      Maximum compares: " + failures.observedMaximum + ", " + failures.countMaximum + " occurences of max." +
                    "\n      Total failing searches: " + failures.total + ", Mean: " + failures.mean + " compares");
            Terminal.print("\nFailure compares distribution: ");
            printDistribution(failures.observedMinimum, failures.distrib);
            Terminal.println(
                    "\n   For search successes: " + successPercent + "%" +
                    "\n      Minimum compares: " + successes.observedMinimum + ", " + successes.countMinimum + " occurences of min." +
                    "\n      Maximum compares: " + successes.observedMaximum + ", " + successes.countMaximum + " occurences of max." +
                    "\n      Total succeeding searches: " + successes.total + ", Mean: " + successes.mean + " compares" +
                    "\n   Total searches: " + (totalSearches));
            Terminal.print("\nSuccess compares distribution: ");
            printDistribution(successes.observedMinimum, successes.distrib);

        } else {
            Terminal.println(" No Search Statistics ");
        }
    }
}

class CallStack extends Exception {
    CallStack() {
    }
}
