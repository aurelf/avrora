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

/**
 * @author Ben L. Titzer This class is used to record instances of profiling data from a Hashtable.
 */
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

        if (max > 70) scale = ((float)max) / 70;

        for (cntr = 0; cntr < data.length; cntr++) {
            float fstars = ((float)data[cntr]) / scale;
            int stars = (int)fstars;
            if ((fstars - stars) >= 0.5) stars++;

            Terminal.print("\n" + (base + cntr) + ':' + data[cntr] + '\t');

            for (int scntr = 0; scntr < stars; scntr++) {
                Terminal.print("*");
            }
        }
        Terminal.print("\n");
    }
}
