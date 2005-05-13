/**
 * Copyright (c) 2004-2005, Regents of the University of California
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

package avrora.sim.util;

import avrora.sim.Simulator;
import avrora.util.Terminal;
import avrora.Avrora;

import java.io.File;
import java.io.IOException;

/**
 * The <code>InterruptScheduler</code> class is a testing utility that reads an interrupt schedule
 * from a file and then posts the interrupts at the appropriate times (according to the schedule)
 * to the simulator.
 *
 * @author John Regehr
 * @author Ben L. Titzer
 */
public class InterruptScheduler {
    final Simulator simulator;
    final File schedFile;
    final java.io.StreamTokenizer tokens;

    private int currentLine;


    public InterruptScheduler(String fname, Simulator s) {
        simulator = s;
        schedFile = new File(fname);
        currentLine = 1;

        try {
            Terminal.println("Loading interrupt schedule from " + schedFile+"...");
            java.io.FileReader inf_reader = new java.io.FileReader(schedFile);
            tokens = new java.io.StreamTokenizer(inf_reader);
            scheduleNextInterrupt();
        } catch ( IOException e ) {
            throw Avrora.unexpected(e);
        }
    }

    private void scheduleNextInterrupt() {
        try {
            if (tokens.nextToken() != tokens.TT_EOF) {
                if (tokens.ttype != tokens.TT_NUMBER) {
                    throw Avrora.failure("interrupt schedule format expected integer in field 1, line " + currentLine + " of " +
                            schedFile);
                }
                int vec = (int) tokens.nval;
                if (tokens.nextToken() != tokens.TT_NUMBER) {
                    throw Avrora.failure("interrupt schedule format expected integer in field 2, line " + currentLine + " of " +
                            schedFile);
                }
                long time = (int) tokens.nval;
                scheduleInterrupt(vec, time);
                currentLine++;
            }
        } catch (IOException e) {
            throw Avrora.unexpected(e);
        }
    }

    /**
     * The <code>ScheduledInterrupt</code> class is an event that is inserted into the queue of a simulator.
     * When the event fires, it will force the interrupt to be posted and then it will schedule the next
     * interrupt.
     *
     * @author John Regehr
     */
    public class ScheduledInterrupt implements Simulator.Event {
        public final int vec;

        public ScheduledInterrupt(int i) {
            vec = i;
        }

        public void fire() {
            /*
             * NOTE: Don't use this code without checking to make sure that
             * each interrupt vector that you are forcing provides a proper
             * forceInterrupt method
             */
            simulator.forceInterrupt(vec);
            scheduleNextInterrupt();
        }

    }

    private void scheduleInterrupt(int vector, long cycles) {
        ScheduledInterrupt e = new ScheduledInterrupt(vector);
        simulator.insertEvent(e, cycles);
    }

}
