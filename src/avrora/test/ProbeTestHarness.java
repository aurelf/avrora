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

package avrora.test;

import avrora.Main;
import avrora.Avrora;
import avrora.sim.Simulator;
import avrora.sim.GenInterpreter;
import avrora.sim.mcu.MicrocontrollerFactory;
import avrora.sim.mcu.ATMega128L;
import avrora.sim.mcu.Microcontroller;
import avrora.test.probes.ProbeParser;
import avrora.test.probes.ProbeTest;
import avrora.core.Program;
import avrora.core.ProgramReader;
import avrora.syntax.Module;

import java.util.Properties;
import java.io.FileInputStream;

/**
 * The <code>SimulatorTestHarness</code> implements a test harness that interfaces the
 * <code>avrora.test.AutomatedTester</code> in order to automate testing of the AVR parser and simulator.
 *
 * @author Ben L. Titzer
 */
public class ProbeTestHarness implements TestHarness {

    class ProbeTestCase extends TestCase {

        Module module;
        Program program;
        ProbeTest probeTest;
        String progName;

        ProbeTestCase(String fname, Properties props) throws Exception {
            super(fname, props);

            ProbeParser p = new ProbeParser(new FileInputStream(fname));
            probeTest = p.ProbeTest();
            progName = props.getProperty("Program");
        }

        public void run() throws Exception {
            // TODO: better error checking: file not found, not specified, etc
            ProgramReader pr = Main.getProgramReader();
            Program p = pr.read(new String[] { progName });
            Microcontroller m = new ATMega128L(0, new GenInterpreter.Factory(), p, false);
            Simulator s = m.getSimulator();
            probeTest.run(s);
        }

        public TestResult match(Throwable t) {
            if (t instanceof ProbeTest.Failure ) {
                return new TestResult.TestFailure(((ProbeTest.Failure)t).reason);
            }

            return super.match(t);
        }

    }

    public TestCase newTestCase(String fname, Properties props) throws Exception {
        return new ProbeTestCase(fname, props);
    }

}