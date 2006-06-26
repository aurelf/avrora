/**
 * Copyright (c) 2005, Regents of the University of California
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
 *
 * Creation date: Sep 12, 2005
 */

package cck.stat;

import cck.text.Terminal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The profiling database. Collects information about profiling and is implemented as a static class. This
 * allows other tools to register subclasses of ProfilingData and the Database will collect them and report
 * their statistics.
 *
 * @author Ben L. Titzer
 */
public class ProfilingDatabase {

    static List datalist = new LinkedList();
    static boolean created = false;

    /**
     * LegacyRegister a ProfilingData object
     */
    public static void register(ProfilingData d) {
        datalist.add(d);
    }

    /**
     * Generate report of the profiling statistics.
     */
    public static void reportData() {
        if (datalist == null) {
            Terminal.println("Profiling Database has no information.");
            return;
        }

        Terminal.println("Profiling Database collected " + datalist.size() + " entries");

        Iterator i = datalist.iterator();
        int cntr = 1;

        while (i.hasNext()) {
            ProfilingData d = (ProfilingData) i.next();
            if (d.dataCollected()) {
                Terminal.println("\nProfilingData object " + cntr + ", instance of " + d.getClass().toString());
                d.computeStatistics();
                d.reportData();
            }
            cntr++;
        }
    }
}
