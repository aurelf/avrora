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

package avrora.test.sim;

import avrora.Defaults;
import avrora.arch.legacy.LegacyState;
import avrora.core.*;
import avrora.sim.Simulator;
import avrora.sim.State;
import avrora.syntax.Module;
import cck.test.*;
import cck.text.StringUtil;
import cck.text.Terminal;
import cck.util.Util;
import java.util.*;

/**
 * The <code>SimulatorTestHarness</code> implements a test harness that interfaces the
 * <code>avrora.test.AutomatedTester</code> in order to automate testing of the AVR parser and simulator.
 *
 * @author Ben L. Titzer
 */
public class SimTestHarness implements TestHarness {

    static class SimulatorTest extends TestCase {

        Module module;
        Simulator simulator;
        List predicates;
        List inits;
        StateAccessor access;

        SimulatorTest(String fname, Properties props) throws Exception {
            super(fname, props);
            String result = trimString(props.getProperty("Result"));
            PredicateParser pp = new PredicateParser();
            predicates = pp.parseResult(result);
            String init = props.getProperty("Init");
            if ( init != null ) inits = pp.parseInitializers(trimString(init));
        }

        private String trimString(String str) {
            return StringUtil.trimquotes(str.trim());
        }

        public void run() throws Exception {
            Program p = readProgram();
            simulator = initSimulator(p);
            simulator.start();
        }

        private Program readProgram() throws Exception {
            String format = properties.getProperty("Format");
            if (format == null) Util.userError("program format not specified");
            ProgramReader r = Defaults.getProgramReader(format);
            String arch = properties.getProperty("Arch");
            if ( arch != null ) r.ARCH.set(arch);
            String[] args = {filename};
            return r.read(args);
        }

        private Simulator initSimulator(Program program) {
            Simulator sim = Defaults.newSimulator(0, program);
            access = new LegacyStateAccessor(program, sim);
            if ( inits != null ) {
                Iterator i = inits.iterator();
                while ( i.hasNext() ) {
                    Predicate p = (Predicate)i.next();
                    p.init(access);
                }
            }
            return sim;
        }

        public TestResult match(Throwable t) {
            if (t != null) return super.match(t);

            LegacyState state = (LegacyState)simulator.getState();
            Iterator i = predicates.iterator();

            try {
                while (i.hasNext()) {
                    Predicate p = (Predicate)i.next();
                    if (!p.check(access))
                        return new StateMismatch(p, state);
                }
            } catch (SimTestExpr.UnknownLabel l) {
                return new TestResult.TestFailure("unknown label specified in predicate: " + l.name);
            }
            return new TestResult.TestSuccess();
        }
    }

    static class StateMismatch extends TestResult.TestFailure {
        Predicate predicate;
        State state;

        StateMismatch(Predicate pred, State st) {
            super("incorrect result: (" + pred.left + " -> " + pred.leftvalue + ") != ("
                    + pred.right + " -> " + pred.rightvalue + ')');
            state = st;
            predicate = pred;
        }

        public void shortReport() {
            Terminal.print(message);
        }

        public void longReport() {
            Terminal.println(message);
            // TODO: print out complete state
        }
    }


    public TestCase newTestCase(String fname, Properties props) throws Exception {
        return new SimulatorTest(fname, props);
    }

}
