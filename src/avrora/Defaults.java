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
import avrora.util.help.HelpSystem;
import avrora.util.help.HelpCategory;
import avrora.util.help.ValueItem;
import avrora.util.help.ClassMapValueItem;
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
import avrora.syntax.objdump.ObjDumpPreprocessor;
import avrora.syntax.objdump.ObjDump2ProgramReader;
import avrora.test.TestHarness;
import avrora.test.SimulatorTestHarness;
import avrora.test.SimplifierTestHarness;
import avrora.test.ProbeTestHarness;
import avrora.monitors.*;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * The <code>Defaults</code> class contains the default mappings for microcontrollers, actions,
 * input formats, constants, etc.
 *
 * @author Ben L. Titzer
 */
public class Defaults {
    private static ClassMap microcontrollers;
    private static ClassMap platforms;
    private static ClassMap actions;
    private static ClassMap inputs;
    private static ClassMap harnessMap;
    private static ClassMap monitorMap;

    private static void addAll() {
        addMicrocontrollers();
        addPlatforms();
        addActions();
        addInputFormats();
        addTestHarnesses();
        addMonitors();
    }

    private static void addMonitors() {
        if (monitorMap == null) {
            monitorMap = new ClassMap("Monitor", MonitorFactory.class);
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
    }

    private static void addTestHarnesses() {
        if (harnessMap == null) {
            harnessMap = new ClassMap("Test Harness", TestHarness.class);
            //-- DEFAULT TEST HARNESSES
            harnessMap.addClass("simulator", SimulatorTestHarness.class);
            harnessMap.addClass("simplifier", SimplifierTestHarness.class);
            harnessMap.addClass("probes", ProbeTestHarness.class);
        }
    }

    private static void addInputFormats() {
        if (inputs == null) {
            inputs = new ClassMap("Input Format", ProgramReader.class);
            //-- DEFAULT INPUT FORMATS
            inputs.addClass("auto", Main.AutoProgramReader.class);
            inputs.addClass("gas", GASProgramReader.class);
            inputs.addClass("atmel", AtmelProgramReader.class);
            inputs.addClass("objdump", ObjDumpProgramReader.class);
            inputs.addClass("odpp", ObjDump2ProgramReader.class);
        }
    }

    private static void addActions() {
        if (actions == null) {
            actions = new ClassMap("Action", Action.class);
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
            actions.addClass("odpp", ObjDumpPreprocessor.class);
        }
    }

    private static void addPlatforms() {
        if (platforms == null) {
            platforms = new ClassMap("Platform", Platform.class);
            //-- DEFAULT PLATFORMS
            platforms.addClass("mica2", Mica2.class);
        }
    }

    private static void addMicrocontrollers() {
        if (microcontrollers == null) {
            microcontrollers = new ClassMap("Microcontroller", MicrocontrollerFactory.class);
            //-- DEFAULT MICROCONTROLLERS
            microcontrollers.addInstance("atmega128", new ATMega128.Factory());
            microcontrollers.addInstance("atmega128l", new ATMega128L.Factory(false));
            microcontrollers.addInstance("atmega128l-103", new ATMega128L.Factory(true));
        }
    }

    /**
     * The <code>getMicrocontroller()</code> method gets the microcontroller factory corresponding
     * to the given name represented as a string. This string can represent a short name for the
     * class (an alias), or a fully qualified Java class name.
     *
     * @param s the name of the microcontroller as string; a class name or an alias such as "atmega128"
     * @return an instance of the <code>MicrocontrollerFactory</code> interface that is capable
     *         of creating repeated instances of the microcontroller.
     */
    public static MicrocontrollerFactory getMicrocontroller(String s) {
        addMicrocontrollers();
        return (MicrocontrollerFactory) microcontrollers.getObjectOfClass(s);
    }

    /**
     * The <code>getPlatform()</code> method gets the platform factory corresponding to the
     * given name represented as a string. This string can represent a short name for the
     * class (an alias), or a fully qualified Java class name.
     *
     * @param s the name of the platform as string; a class name or an alias such as "mica2"
     * @return an instance of the <code>PlatformFactory</code> interface that is capable of
     *         creating repeated instances of the microcontroller.
     */
    public static PlatformFactory getPlatform(String s) {
        addPlatforms();
        return (PlatformFactory) platforms.getObjectOfClass(s);
    }

    /**
     * The <code>getProgramReader()</code> method gets the program reader corresponding to
     * the given name represented as a string. This string can represent a short name for the
     * class (an alias), or a fully qualified Java class name.
     *
     * @param s the name of the program reader format as a string
     * @return an instance of the <code>ProgramReader</code> class that is capable of reading
     *         a program into the internal program representation format.
     */
    public static ProgramReader getProgramReader(String s) {
        addInputFormats();
        return (ProgramReader) inputs.getObjectOfClass(s);
    }

    /**
     * The <code>getAction()</code> method gets the action corresponding to the given name
     * represented as a string. This string can represent a short name for the
     * class (an alias), or a fully qualified Java class name.
     *
     * @param s the name of the action as a string
     * @return an instance of the <code>Action</code> class which can run given the command
     *         line arguments and options provided.
     */
    public static Action getAction(String s) {
        addActions();
        return (Action) actions.getObjectOfClass(s);
    }

    /**
     * The <code>getMonitor()</code> method gets the monitor corresponding to the given name
     * represented as a string. This string can represent a short name for the class (an alias),
     * or a fully qualified Java class name.
     *
     * @param s the name of the monitor as a string
     * @return an instance of the <code>MonitorFactory</code> class that is capable of attaching
     *         monitors to nodes as they are created
     */
    public static MonitorFactory getMonitor(String s) {
        addMonitors();
        return (MonitorFactory) monitorMap.getObjectOfClass(s);
    }

    /**
     * The <code>getTestHarness()</code> method gets the test harness class corresponding to
     * the given name represented as a string. This string can represent a short name for the class (an alias),
     * or a fully qualified Java class name.
     *
     * @param s the name of the test harness as a string
     * @return an instance of the <code>TestHarness</code> class that is capable of creating a
     *         test case for a file and running it
     */
    public static TestHarness getTestHarness(String s) {
        addTestHarnesses();
        return (TestHarness) harnessMap.getObjectOfClass(s);
    }

    /**
     * The <code>getActionList()</code> method returns a list of aliases for actions sorted
     * alphabetically.
     *
     * @return a sorted list of known actions
     */
    public static List getActionList() {
        addActions();
        return actions.getSortedList();
    }

    /**
     * The <code>getProgramReaderList()</code> method returns a list of aliases for program
     * readers sorted alphabetically.
     *
     * @return a sorted list of known program readers
     */
    public static List getProgramReaderList() {
        addInputFormats();
        return inputs.getSortedList();
    }

    private static void addSubCategories(ClassMap vals) {
        List l = vals.getSortedList();
        Iterator i = l.iterator();
        while (i.hasNext()) {
            String val = (String) i.next();
            Class cz = vals.getClass(val);
            if (HelpCategory.class.isAssignableFrom(cz))
                HelpSystem.addCategory(val, cz);
        }
    }

    private void addMainCategory(String name, HelpCategory cat) {
        HelpSystem.addCategory(name, cat);
    }

    private void addOptionSection(HelpCategory hc, String title, String para, String optname, ClassMap optvals) {
        LinkedList list = new LinkedList();
        Iterator i = optvals.iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            list.addLast(new ClassMapValueItem(8, optname, s, optvals));
        }

        hc.addListSection(title, para, list);
    }

    public HelpCategory getHelpCategory(String name) {
        addAll();
        return null;
    }
}
