/**
 * Copyright (c) 2004, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package avrora.util.profiling;

import avrora.util.Terminal;

import java.util.Vector;

/**
 * This class represents the proportion of different items with respect to one
 * another. For example, the proportion of cars, trucks, vans, etc on the highway.
 *
 * @author Ben L. Titzer
 **/
public class Proportion extends DataItem {

    protected Vector shares;
    protected int total;

    /**
     * Internal class that encapsulates both a counter and the fraction
     * of the total that this named Share represents.
     **/
    public class Share extends Counter {
        float fraction;

        public Share(String newname) {
            super(newname);
        }

        public Share(String newname, int count) {
            super(newname, count);
        }

        public float getFraction() {
            return fraction;
        }

        public void processData() {
            Proportion.this.processData();
        }

        public void textReport() {
            Terminal.print("\n   " + name + ": " + count + ", " + (100 * fraction) + "%");
        }
    }

    /**
     * Public constructor that takes a string name.
     **/
    public Proportion(String newname) {
        name = newname;
        shares = new Vector(5);
    }

    /**
     * Generate a text report of the shares.
     **/
    public void textReport() {
        int numshares = shares.size();
        Terminal.print("\n " + name);
        Terminal.print("\n---------------------------------------------------------------------");

        for (int cntr = 0; cntr < numshares; cntr++) {
            Share s = (Share) shares.get(cntr);
            s.textReport();
        }

        Terminal.print("\n");
    }

    /**
     * Register a counter object with this proportion.
     **/
    public Share createShare(String name) {
        Share s = new Share(name);
        shares.add(s);
        return s;
    }

    /**
     * Register an integer count with this proportion object and return
     * a Counter object.
     **/
    public Share registerCount(String str, int count) {
        Share s = new Share(str, count);
        shares.add(s);
        return s;
    }

    /**
     * Search for the counter with the specified string name and return
     * it if it is registered.
     **/
    public Share getShareForName(String name) {
        int numshares = shares.size();

        for (int i = 0; i < numshares; i++) {
            Share s = (Share) shares.get(i);
            if (name.equals(s.getName())) return s;
        }

        return null;
    }

    /**
     * Search for the counter with the specified name and
     * report its proportion. Return -1 if not found.
     **/
    public float getFractionForName(String name) {
        int numshares = shares.size();

        processData(); // make sure proportions up to date

        for (int i = 0; i < numshares; i++) {
            Share s = (Share) shares.get(i);
            if (name.equals(s.getName())) return s.fraction;
        }

        return -1;
    }

    /**
     * Do the computations and compute the proportions of each.
     **/
    public void processData() {
        int numshares = shares.size();
        int tmptotal = 0;

        for (int i = 0; i < numshares; i++) {
            Share s = (Share) shares.get(i);
            tmptotal += s.getTotal();
        }

        total = tmptotal;

        for (int i = 0; i < numshares; i++) {
            Share s = (Share) shares.get(i);
            float f = ((float) s.getTotal()) / ((float) total);
            s.fraction = f;
        }

    }

    /**
     * Return true if this proportion has any information available.
     **/
    public boolean hasData() {
        return (shares.size() > 0);
    }
}
