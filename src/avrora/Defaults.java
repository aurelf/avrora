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

package avrora;

import avrora.util.ClassMap;
import avrora.sim.mcu.MicrocontrollerFactory;
import avrora.sim.mcu.ATMega128;
import avrora.sim.mcu.ATMega128L;
import avrora.sim.mcu.Microcontroller;
import avrora.sim.platform.Platform;
import avrora.sim.platform.Mica2;
import avrora.sim.platform.PlatformFactory;
import avrora.actions.*;
import avrora.core.ProgramReader;
import avrora.syntax.gas.GASProgramReader;
import avrora.syntax.atmel.AtmelProgramReader;
import avrora.syntax.objdump.ObjDumpProgramReader;
import avrora.test.TestHarness;
import avrora.test.SimulatorTestHarness;
import avrora.test.SimplifierTestHarness;
import avrora.test.ProbeTestHarness;
import avrora.monitors.*;

import java.util.List;

/**
 * The <code>Defaults</code> class contains the default mappings for microcontrollers, actions,
 * input formats, constants, etc.
 *
 * @author Ben L. Titzer
 */
public class Defaults {
    private static final ClassMap microcontrollers;
    private static final ClassMap platforms;
    private static final ClassMap actions;
    private static final ClassMap inputs;
    private static final ClassMap harnessMap;
    private static final ClassMap monitorMap;


    static {
        microcontrollers = new ClassMap("Microcontroller", MicrocontrollerFactory.class);
        platforms = new ClassMap("Platform", Platform.class);
        actions = new ClassMap("Action", Action.class);
        inputs = new ClassMap("Input Format", ProgramReader.class);
        harnessMap = new ClassMap("Test Harness", TestHarness.class);
        monitorMap = new ClassMap("Monitor", MonitorFactory.class);

        //-- DEFAULT MICROCONTROLLERS
        microcontrollers.addInstance("atmega128", new ATMega128.Factory());
        microcontrollers.addInstance("atmega128l", new ATMega128L.Factory(false));
        microcontrollers.addInstance("atmega128l-103", new ATMega128L.Factory(true));

        //-- DEFAULT PLATFORMS
        platforms.addClass("mica2", Mica2.class);

        //-- DEFAULT ACTIONS
        actions.addClass("multi-simulate", MultiSimulateAction.class);
        actions.addClass("simulate", SimulateAction.class);
        actions.addClass("analyze-stack", AnalyzeStackAction.class);
        actions.addClass("test", TestAction.class);
        actions.addClass("list", ListAction.class);
        actions.addClass("cfg", CFGAction.class);
        actions.addClass("benchmark", BenchmarkAction.class);
        //--BEGIN EXPERIMENTAL: isdl
        actions.addClass("isdl", ISDLAction.class);
        //--END EXPERIMENTAL: isdl
        //--BEGIN EXPERIMENTAL: dbbc
        actions.addClass("dbbc", DBBCAction.class);
        //--END EXPERIMENTAL: dbbc

        //-- DEFAULT INPUT FORMATS
        inputs.addClass("auto", Main.AutoProgramReader.class);
        inputs.addClass("gas", GASProgramReader.class);
        inputs.addClass("atmel", AtmelProgramReader.class);
        inputs.addClass("objdump", ObjDumpProgramReader.class);

        //-- DEFAULT TEST HARNESSES
        harnessMap.addClass("simulator", SimulatorTestHarness.class);
        harnessMap.addClass("simplifier", SimplifierTestHarness.class);
        harnessMap.addClass("probes", ProbeTestHarness.class);

        //-- DEFAULT MONITORS AVAILABLE
        monitorMap.addClass("profile", ProfileMonitor.class);
        monitorMap.addClass("memory", MemoryMonitor.class);
        monitorMap.addClass("sleep", SleepMonitor.class);
        monitorMap.addClass("stack", StackMonitor.class);
        monitorMap.addClass("energy", EnergyMonitor.class);
        monitorMap.addClass("energy-log", EnergyMonitorLog.class);
        monitorMap.addClass("trace", TraceMonitor.class);
        monitorMap.addClass("energy-profile", EnergyProfiler.class);
        monitorMap.addClass("packet", PacketMonitor.class);
        monitorMap.addClass("gdb", GDBServer.class);
        monitorMap.addClass("simperf", SimPerfMonitor.class);
        monitorMap.addClass("pc", Pc.class);
    }

    public static MicrocontrollerFactory getMicrocontroller(String s) {
        return (MicrocontrollerFactory)microcontrollers.getObjectOfClass(s);
    }

    public static PlatformFactory getPlatform(String s) {
        return (PlatformFactory)platforms.getObjectOfClass(s);
    }

    public static ProgramReader getProgramReader(String s) {
        return (ProgramReader)inputs.getObjectOfClass(s);
    }

    public static Action getAction(String s) {
        return (Action)actions.getObjectOfClass(s);
    }

    public static MonitorFactory getMonitor(String s) {
        return (MonitorFactory)monitorMap.getObjectOfClass(s);
    }

    public static TestHarness getTestHarness(String s) {
        return (TestHarness)harnessMap.getObjectOfClass(s);
    }

    public static List getActionList() {
        return actions.getSortedList();
    }

    public static List getProgramReaderList() {
        return inputs.getSortedList();
    }


}
