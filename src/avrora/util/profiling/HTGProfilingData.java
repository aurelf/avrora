/**
 * @author Ben L. Titzer
 **/
package avrora.util.profiling;


/**
 * This class contains profiling data that the hashtables collect.
 **/
public class HTGProfilingData extends Database {
    public final Object instance;
    public final int hashtableSize;
    public final Proportion puts; // proportion of puts in L1 and L2
    public final Proportion gets; // proportino of gets in L1 and L2

    public final Proportion.Share putL1; // share of L1 puts
    public final Proportion.Share putL2; // share of L2 puts

    public final Distribution putL2_dist; // distribution of search length in L2

    public final Proportion.Share getL1s; // share of L1 successful gets
    public final Proportion.Share getL2s; // share of L2 successful gets
    public final Proportion.Share getL1f; // share of L1 failed gets
    public final Proportion.Share getL2f; // share of L2 failed gets

    public final Distribution getL2s_dist; // distribution of search length in L2
    public final Distribution getL2f_dist; // distribution of search length in L2

    public final Distribution bindingDist;

    public HTGProfilingData(Object o, int size) {
        Databank.registerDatabase(this);
        instance = o;

        hashtableSize = size;

        puts = new Proportion("Proportion of puts in L1 and L2");
        putL1 = puts.createShare("L1 successful puts");
        putL2 = puts.createShare("L2 successful puts");
        putL2_dist = new Distribution("L2 Put Statistics");

        gets = new Proportion("Proportion of gets in L1 and L2");
        getL1s = gets.createShare("L1 successful gets");
        getL2s = gets.createShare("L2 successful gets");
        getL1f = gets.createShare("L1 failed gets");
        getL2f = gets.createShare("L2 failed gets");
        getL2s_dist = new Distribution("L2 Successful Get Statistics");
        getL2f_dist = new Distribution("L2 Failed Get Statistics");

        bindingDist = new Distribution("Binding Distribution", "Total", null);

        this.registerDataItem(puts);
        this.registerDataItem(gets);
        this.registerDataItem(putL2_dist);
        this.registerDataItem(getL2s_dist);
        this.registerDataItem(getL2f_dist);
        this.registerDataItem(bindingDist);
    }

    public void freeze() {
        processData();
    }

    public String toString() {
        return "Profiling data for " + instance.getClass().getName() +
                " of size " + hashtableSize;
    }

    public boolean isEmpty() {
        return false;
    }
}

