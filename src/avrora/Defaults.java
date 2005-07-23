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

import avrora.actions.*;
import avrora.core.Program;
import avrora.core.ProgramReader;
import avrora.monitors.*;
import avrora.sim.GenInterpreter;
import avrora.sim.InterpreterFactory;
import avrora.sim.Simulation;
import avrora.sim.Simulator;
import avrora.sim.clock.ClockDomain;
import avrora.sim.mcu.ATMega128;
import avrora.sim.mcu.ATMega16;
import avrora.sim.mcu.ATMega32;
import avrora.sim.mcu.MicrocontrollerFactory;
import avrora.sim.platform.Mica2;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.platform.Seres;
import avrora.sim.types.SensorSimulation;
import avrora.sim.types.SingleSimulation;
import avrora.syntax.atmel.AtmelProgramReader;
import avrora.syntax.gas.GASProgramReader;
import avrora.syntax.objdump.ODPPAction;
import avrora.syntax.objdump.ObjDump2ProgramReader;
import avrora.syntax.objdump.ObjDumpProgramReader;
import avrora.test.*;
import avrora.util.ClassMap;
import avrora.util.StringUtil;
import avrora.util.help.ClassMapValueItem;
import avrora.util.help.HelpCategory;
import avrora.util.help.HelpSystem;

import java.util.*;

/**
 * The <code>Defaults</code> class contains the default mappings for microcontrollers, actions,
 * input formats, constants, etc.
 *
 * @author Ben L. Titzer
 */
public class Defaults {
    private static final HashMap mainCategories = new HashMap();

    private static ClassMap microcontrollers;
    private static ClassMap platforms;
    private static ClassMap actions;
    private static ClassMap inputs;
    private static ClassMap harnessMap;
    private static ClassMap monitorMap;
    private static ClassMap simMap;

    private static void addAll() {
        addMicrocontrollers();
        addPlatforms();
        addActions();
        addInputFormats();
        addTestHarnesses();
        addMonitors();
        addSimulations();
    }

    private static void addMonitors() {
        if (monitorMap == null) {
            monitorMap = new ClassMap("Monitor", MonitorFactory.class);
            //-- DEFAULT MONITORS AVAILABLE
            monitorMap.addClass("calls", CallMonitor.class);
            monitorMap.addClass("profile", ProfileMonitor.class);
            monitorMap.addClass("memory", MemoryMonitor.class);
            monitorMap.addClass("sleep", SleepMonitor.class);
            monitorMap.addClass("stack", StackMonitor.class);
            monitorMap.addClass("energy", EnergyMonitor.class);
            monitorMap.addClass("energy-log", EnergyMonitorLog.class);
            monitorMap.addClass("interrupts", InterruptMonitor.class);
            monitorMap.addClass("interactive", InteractiveMonitor.class);
            monitorMap.addClass("trace", TraceMonitor.class);
            monitorMap.addClass("energy-profile", EnergyProfiler.class);
            monitorMap.addClass("packet", PacketMonitor.class);
            monitorMap.addClass("gdb", GDBServer.class);
            monitorMap.addClass("simperf", SimPerfMonitor.class);
            monitorMap.addClass("serial", SerialMonitor.class);
            monitorMap.addClass("trip-time", TripTimeMonitor.class);
            monitorMap.addClass("ioregs", IORegMonitor.class);
            monitorMap.addClass("real-time", RealTimeMonitor.class);

            HelpCategory hc = new HelpCategory("monitors", "Help for the supported simulation monitors.");
            addOptionSection(hc, "SIMULATION MONITORS", "Avrora's simulator offers the ability to install execution " +
                    "monitors that instrument the program in order to study and analyze its behavior. The " +
                    "\"simulate\" action supports this option that allows a monitor class " +
                    "to be loaded which will instrument the program before it is run and then generate a report " +
                    "after the program has completed execution.", "-monitors", monitorMap);
            addMainCategory(hc);
            addSubCategories(monitorMap);
        }
    }

    private static void addTestHarnesses() {
        if (harnessMap == null) {
            harnessMap = new ClassMap("Test Harness", TestHarness.class);
            //-- DEFAULT TEST HARNESSES
            harnessMap.addClass("simulator", SimulatorTestHarness.class);
            harnessMap.addClass("simplifier", SimplifierTestHarness.class);
            harnessMap.addClass("probes", ProbeTestHarness.class);
            harnessMap.addClass("disassembler", DisassemblerTestHarness.class);
            harnessMap.addClass("interrupt", InterruptTestHarness.class);
        }
    }

    private static void addInputFormats() {
        if (inputs == null) {
            inputs = new ClassMap("Input Format", ProgramReader.class);
            //-- DEFAULT INPUT FORMATS
            inputs.addClass("auto", AutoProgramReader.class);
            inputs.addClass("gas", GASProgramReader.class);
            inputs.addClass("atmel", AtmelProgramReader.class);
            inputs.addClass("objdump", ObjDumpProgramReader.class);
            inputs.addClass("odpp", ObjDump2ProgramReader.class);

            HelpCategory hc = new HelpCategory("inputs", "Help for the supported program input formats.");
            addOptionSection(hc, "INPUT FORMATS", "The input format of the program is specified with the \"-input\" " +
                "option supplied at the command line. This input format is used by " +
                "actions that operate on programs to determine how to interpret the " +
                "input and build a program from the files specified. For example, the input format might " +
                "be Atmel syntax, GAS syntax, or the output of a disassembler such as avr-objdump. Currently " +
                "no binary formats are supported.", "-input", inputs);
            addMainCategory(hc);
            addSubCategories(inputs);
        }
    }

    private static void addActions() {
        if (actions == null) {
            actions = new ClassMap("Action", Action.class);
            //-- DEFAULT ACTIONS
            actions.addClass("simulate", SimulateAction.class);
            actions.addClass("analyze-stack", AnalyzeStackAction.class);
            actions.addClass("test", TestAction.class);
            actions.addClass("cfg", CFGAction.class);
            actions.addClass("gui", GUIAction.class);
            actions.addClass("isea", ISEAAction.class);
            //--BEGIN EXPERIMENTAL: isdl
            actions.addClass("isdl", ISDLAction.class);
            //--END EXPERIMENTAL: isdl
            //--BEGIN EXPERIMENTAL: dbbc
            actions.addClass("dbbc", DBBCAction.class);
            //--END EXPERIMENTAL: dbbc
            actions.addClass("odpp", ODPPAction.class);

            // plug in a new help category for actions accesible with "-help actions"
            HelpCategory hc = new HelpCategory("actions", "Help for Avrora actions.");
            addOptionSection(hc, "ACTIONS", "Avrora accepts the \"-action\" command line option " +
                    "that you can use to select from the available functionality that Avrora " +
                    "provides. This action might be to assemble the file, " +
                    "print a listing, perform a simulation, or run an analysis tool. This " +
                    "flexibility allows this single frontend to select from multiple useful " +
                    "tools. The currently supported actions are given below.", "-action", actions);
            addMainCategory(hc);
            addSubCategories(actions);
        }
    }

    private static void addSimulations() {
        if (simMap == null) {
            simMap = new ClassMap("Simulation", Simulation.class);
            //-- DEFAULT ACTIONS
            simMap.addClass("single", SingleSimulation.class);
            simMap.addClass("sensor-network", SensorSimulation.class);

            // plug in a new help category for simulations accesible with "-help simulations"
            HelpCategory hc = new HelpCategory("simulations", "Help for supported simulation types.");
            addOptionSection(hc, "SIMULATION TYPES",
                    "When running a simulation, Avrora accepts the \"-simulation\" command line option " +
                    "that selects the simulation type from multiple different types provided, or a " +
                    "user-supplied Java class of your own. For example, a simulation might be for a " +
                    "sensor network application, a single node simulation, or a robotics simulation. ",
                    "-simulation", simMap);
            addMainCategory(hc);
            addSubCategories(simMap);
        }
    }

    private static void addPlatforms() {
        if (platforms == null) {
            platforms = new ClassMap("Platform", PlatformFactory.class);
            //-- DEFAULT PLATFORMS
            platforms.addClass("mica2", Mica2.Factory.class);
            platforms.addClass("seres", Seres.Factory.class);
        }
    }

    private static void addMicrocontrollers() {
        if (microcontrollers == null) {
            microcontrollers = new ClassMap("Microcontroller", MicrocontrollerFactory.class);
            //-- DEFAULT MICROCONTROLLERS
            microcontrollers.addInstance("atmega128", new ATMega128.Factory());
            microcontrollers.addInstance("atmega32", new ATMega32.Factory());
            microcontrollers.addInstance("atmega16", new ATMega16.Factory());
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

    public static Simulation getSimulation(String s) {
        addSimulations();
        // TODO: add a simulation factory
        return (Simulation)simMap.getObjectOfClass(s);
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

    public static void addMainCategory(HelpCategory cat) {
        HelpSystem.addCategory(cat.name, cat);
        mainCategories.put(cat.name, cat);
    }

    private static void addOptionSection(HelpCategory hc, String title, String para, String optname, ClassMap optvals) {
        LinkedList list = new LinkedList();
        Iterator i = optvals.getSortedList().iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            list.addLast(new ClassMapValueItem(4, optname, s, optvals));
        }

        hc.addListSection(title, para, list);
    }

    public static HelpCategory getHelpCategory(String name) {
        addAll();
        return HelpSystem.getCategory(name);
    }

    public static List getMainCategories() {
        addAll();
        List list = Collections.list(Collections.enumeration(mainCategories.values()));
        Collections.sort(list, HelpCategory.COMPARATOR);
        return list;
    }

    public static List getAllCategories() {
        addAll();
        List l = HelpSystem.getSortedList();
        LinkedList nl = new LinkedList();
        Iterator i = l.iterator();
        while ( i.hasNext() ) {
            String s = (String)i.next();
            nl.addLast(HelpSystem.getCategory(s));
        }
        return nl;
    }

    private static final InterpreterFactory DEFAULT_IF = new GenInterpreter.Factory();

    public static Simulator newSimulator(int id, Program p) {
        return newSimulator(id, "atmega128", 8000000, 8000000, DEFAULT_IF, p);
    }

    public static InterpreterFactory getInterpreterFactory() {
        return DEFAULT_IF;
    }

    public static Simulator newSimulator(int id, String mcu, long hz, long exthz, InterpreterFactory factory, Program p) {
        MicrocontrollerFactory f = getMicrocontroller(mcu);
        ClockDomain cd = new ClockDomain(hz);
        cd.newClock("external", exthz);

        return f.newMicrocontroller(id, cd, factory, p).getSimulator();
    }

    public static class AutoProgramReader extends ProgramReader {
        public AutoProgramReader() {
            super("The \"auto\" input format reads a program from a single file at a time. " +
                    "It uses the extension of the filename as a clue to decide what input " +
                    "reader to use for that file. For example, an extension of \".asm\" is " +
                    "considered to be a program in Atmel assembly syntax.");
        }

        public Program read(String[] args) throws Exception {
            if (args.length == 0)
                Avrora.userError("no input files");
            if (args.length != 1)
                Avrora.userError("input type \"auto\" accepts only one file at a time.");

            String n = args[0];
            int offset = n.lastIndexOf(".");
            if (offset < 0)
                Avrora.userError("file " + StringUtil.quote(n) + " does not have an extension");

            String extension = n.substring(offset).toLowerCase();

            ProgramReader reader = null;
            if (".asm".equals(extension))
                reader = new AtmelProgramReader();
            else if (".s".equals(extension))
                reader = new GASProgramReader();
            else if (".od".equals(extension))
                reader = new ObjDumpProgramReader();
            else if (".odpp".equals(extension))
                reader = new ObjDump2ProgramReader();

            if ( reader == null ) {
                Avrora.userError("file extension " + StringUtil.quote(extension) + " unknown");
                return null;
            }

            // TODO: this is a hack; all inherited options should be available
            reader.INDIRECT_EDGES.set(INDIRECT_EDGES.stringValue());
            reader.options.process(options);
            return reader.read(args);
        }

    }
}
