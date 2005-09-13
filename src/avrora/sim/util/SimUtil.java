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

package avrora.sim.util;

import avrora.sim.Simulator;
import avrora.sim.clock.Clock;
import cck.text.StringUtil;

/**
 * The <code>SimUtil</code> class encapsulates a set of utility methods that are used in
 * the simulation, including textual utilities, etc.
 *
 * @author Ben L. Titzer
 */
public class SimUtil {

    public static void toIDTimeString(StringBuffer buf, int id, Clock clk) {
        buf.append(StringUtil.rightJustify(id, StringUtil.ID_LENGTH));
        buf.append("  ");

        if ( StringUtil.REPORT_SECONDS ) {
            StringBuffer buf2 = new StringBuffer(StringUtil.TIME_LENGTH +1);
            long hz = clk.getHZ();
            long count = clk.getCount();
            long seconds = count / hz;
            long fract = count % hz;
            double f = (double)fract / hz;
            StringUtil.appendSecs(buf2, seconds);
            StringUtil.appendFract(buf2, f, StringUtil.SECONDS_PRECISION);
            buf.append(StringUtil.rightJustify(buf2.toString(), StringUtil.TIME_LENGTH));
        } else {
            buf.append(StringUtil.rightJustify(clk.getCount(), StringUtil.TIME_LENGTH));
        }
        buf.append("  ");
    }

    public static String toIDTimeString(int id, Clock clk) {
        StringBuffer buf = new StringBuffer(40);
        toIDTimeString(buf, id, clk);
        return buf.toString();
    }

    public static String getIDTimeString(Simulator s) {
        return toIDTimeString(s.getID(), s.getClock());
    }

    public static void getIDTimeString(StringBuffer buf, Simulator s) {
        toIDTimeString(buf, s.getID(), s.getClock());
    }
}
