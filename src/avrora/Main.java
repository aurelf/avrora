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

package avrora;

import avrora.core.Program;
import avrora.core.CFGBuilder;
import avrora.core.ControlFlowGraph;
import avrora.core.Instr;
import avrora.core.isdl.*;
import avrora.sim.mcu.Microcontroller;
import avrora.sim.SimulateAction;
import avrora.sim.MultiSimulateAction;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.platform.Platforms;
import avrora.sim.mcu.Microcontrollers;
import avrora.sim.mcu.MicrocontrollerFactory;
import avrora.stack.AnalyzeStackAction;
import avrora.syntax.atmel.AtmelProgramReader;
import avrora.syntax.objdump.ObjDumpProgramReader;
import avrora.syntax.gas.GASProgramReader;
import avrora.syntax.Module;
import avrora.util.*;
import avrora.test.AutomatedTester;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;

/**
 * This is the main entrypoint to Avrora.
 *
 * @author Ben L. Titzer
 */
public class Main {

    /**
     * The <code>Action</code> class defines a new action that the main driver is
     * capable of executing. Each instance of <code>Action</code> is inserted into
     * a hash map in the main class, with the key being its name. For example,
     * the action to simulate a program is inserted into this hash map with the key
     * "simulate", and an instance of <code>avrora.sim.SimulateAction</code>.
     */
    public static abstract class Action {
        /**
         * The <code>run()</code> method is called by the main class and is passed
         * the remaining command line arguments after options have been stripped out.
         *
         * @param args the command line arguments
         * @throws Exception
         */
        public abstract void run(String[] args) throws Exception;

        public abstract String getHelp();
    }

    /**
     * The <code>ProgramReader</code> class represents an object capable of reading
     * a program given the special command line arguments. It may for example read
     * source assembly and produce a simplified program.
     */
    public static abstract class ProgramReader {
        /**
         * The <code>read()</code> method will read a program in and produce a
         * simplified format.
         *
         * @param args the command line arguments
         * @return a program instance representing the program
         * @throws Exception
         */
        public abstract Program read(String[] args) throws Exception;

        public abstract String getHelp();
    }

    /**
     * The <code>Location</code> class encapsulates a location within a program
     * that is specified on the command line. The <code>getLocationList</code> method
     * can parse a string of locations separated by commas and then return
     * a list of these locations.
     */
    public static class Location {
        public final String name;
        public final int address;

        Location(int addr) {
            name = null;
            address = addr;
        }

        Location(String n, int addr) {
            name = n;
            address = addr;
        }

        public int hashCode() {
            if (name == null)
                return address;
            else
                return name.hashCode();
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Location)) return false;
            Location l = ((Location) o);
            return l.name == this.name && l.address == this.address;
        }

    }

    static final String VERSION = "Beta 1.1.5";

    static final HashMap actions = new HashMap();
    static final HashMap inputs = new HashMap();
    static final Options options = new Options();

    public static final Option.Str INPUT = options.newOption("input", "atmel",
            "This option selects among the available program formats as input to Avrora. " +
            "For example, the default input format, \"atmel\" selects the assembly " +
            "language format supported by Atmel's assembler.");
    public static final Option.Str ACTION = options.newOption("action", "simulate",
            "This option selects the action to perform. For example, an action might " +
            "be to load a program into the simulator and run it. For more information, " +
            "see the section on actions.");
    public static final Option.Str OUTPUT = options.newOption("output", "",
            "This option selects an output format for the type of actions that output " +
            "a new program, like an assembler, disassembler or optimizer.");
    public static final Option.List BREAKS = options.newOptionList("breakpoint", "",
            "This option is used in the simulate action. It allows the user to " +
            "insert a series of breakpoints in the program from the command line. " +
            "The address of the breakpoint can be given in hexadecimal or as a label " +
            "within the program. Hexadecimal constants are denoted by a leading '$'.");
    public static final Option.List COUNTS = options.newOptionList("count", "",
            "This option is used in the simulate action. It allows the user to " +
            "insert a list of profiling counters in the program that collect profiling " +
            "information during the execution of the program.");
    public static final Option.List BRANCHCOUNTS = options.newOptionList("branchcount", "",
            "This option is used in the simulate action. It allows the user to " +
            "insert a list of branch counters in the program that collect information " +
            "about taken and not taken counts for branches.");
    public static final Option.Bool PROFILE = options.newOption("profile", false,
            "This option is used in the simulate action. It compiles a histogram of " +
            "instruction counts for each instruction in the program and presents the " +
            "results in a tabular format.");
    public static final Option.Bool TIME = options.newOption("time", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the time used in executing the simulation. When combined with " +
            "the \"cycles\" and \"total\" options, it will report performance " +
            "information about the simulation.");
    public static final Option.Long ICOUNT = options.newOption("icount", 0,
            "This option is used in the simulate action. It will terminate the " +
            "simulation after the specified number of instructions have been executed. " +
            "It is useful for non-terminating programs.");
    public static final Option.Long TIMEOUT = options.newOption("timeout", 0,
            "This option is used in the simulate action. It will terminate the " +
            "simulation after the specified number of clock cycles have passed. " +
            "It is useful for non-terminating programs and benchmarks.");
    public static final Option.Bool TOTAL = options.newOption("total", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the total instructions executed in the simulation. When combined " +
            "with the \"time\" option, it will report performance information.");
    public static final Option.Bool CYCLES = options.newOption("cycles", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to report the total cycles executed in the simulation. When combined " +
            "with the \"time\" option, it will report performance information.");
    public static final Option.Bool TRACE = options.newOption("trace", false,
            "This option is used in the simulate action. It will cause the simulator " +
            "to print each instruction as it is executed.");
    public static final Option.Bool COLORS = options.newOption("colors", true,
            "This option is used to enable or disable the terminal colors.");
    public static final Option.Bool BANNER = options.newOption("banner", true,
            "This option is used to enable or disable the printing of the banner.");
    public static final Option.List VERBOSE = options.newOptionList("verbose", "",
            "This option allows users to enable verbose printing of individual " +
            "subsystems within Avrora. For more information, see the section on verbose " +
            "printing.");
    public static final Option.Long REPEAT = options.newOption("repeat", 1,
            "This option is used to repeat the specified action a given number of times. " +
            "It is useful for testing and profiling the simulator itself and collecting " +
            "measurements on simulations. It is mostly useful for warming up the Java " +
            "Virtual Machine that is executing Avrora.");
    public static final Option.Str CHIP = options.newOption("chip", "atmega128l",
            "This option selects the microcontroller from a library of supported " +
            "microcontroller models.");
    public static final Option.Str PLATFORM = options.newOption("platform", "",
            "This option selects the platform on which the microcontroller is built, " +
            "including the external devices such as LEDs and radio. If the platform " +
            "option is not set, the default platform is the microcontroller specified " +
            "in the \"chip\" option, with no external devices.");
    public static final Option.Bool HELP = options.newOption("help", false,
            "Displays this help message.");
    public static final Option.Str CLASS = options.newOption("class", "",
            "This option is only used in the \"custom\" action to specify which Java " +
            "class contains an action to load and execute.");
    public static final Option.Bool LICENSE = options.newOption("license", false,
            "Display the copyright and license text.");
    public static final Option.Bool HTML = options.newOption("html", false,
            "For terminal colors. Display terminal colors as HTML tags for " +
            "easier inclusion in webpages.");
    public static final Option.Long NODECOUNT = options.newOption("nodecount", 1,
            "This option is used in the multi-node simulation. It specifies the " +
            "number of nodes to be instantiated.");
    public static final Option.Str TOPOLOGY = options.newOption("topology", "",
            "This option is used in the multi-node simulation to specify the name of " +
            "a file that contains information about the topology of the network.");

    public static final Verbose.Printer configPrinter = Verbose.getVerbosePrinter("config");

    static {
        newAction("multi-simulate", new MultiSimulateAction());
        newAction("simulate", new SimulateAction());
        newAction("analyze-stack", new AnalyzeStackAction());
        newAction("assemble", new AssembleAction());
        newAction("test", new TestAction());
        newAction("list", new ListAction());
        newAction("cfg", new CFGAction());
        newAction("isdl", new ISDLAction());
        newAction("custom", new CustomAction());
        newInput("gas", new GASProgramReader());
        newInput("atmel", new AtmelProgramReader());
        newInput("objdump", new ObjDumpProgramReader());
    }

    static void newAction(String name, Action a) {
        actions.put(name, a);
    }

    static void newInput(String name, ProgramReader r) {
        inputs.put(name, r);
    }

    static class ISDLAction extends Action {

        public void run(String[] args) throws Exception {
            if (args.length == 0)
                Avrora.userError("no input files");
            if (args.length != 1)
                Avrora.userError("isdl tool accepts only one file at a time.");

            File f = new File(args[0]);
            FileInputStream fis = new FileInputStream(f);
            ISDLParser parser = new ISDLParser(fis);
            parser.Architecture();
        }

        public String getHelp() {
            return "The \"isdl\" action invokes the instruction set description language " +
                    "(ISDL) tool, which is used internally in Avrora to describe the AVR " +
                    "instruction set.";
        }
    }


    static class CFGAction extends Action {

        public void run(String[] args) throws Exception {
            ProgramReader r = (ProgramReader) inputs.get(INPUT.get());
            Program p = r.read(args);
            ControlFlowGraph cfg = new CFGBuilder(p).buildCFG();
            Iterator biter = cfg.getSortedBlockIterator();

            while ( biter.hasNext() ) {
                ControlFlowGraph.Block block = (ControlFlowGraph.Block)biter.next();
                Terminal.print("[");
                Terminal.printBrightCyan(StringUtil.addrToString(block.getAddress()));
                Terminal.println(":"+block.getSize()+"]");
                Iterator iiter = block.getInstrIterator();
                while ( iiter.hasNext() ) {
                    Instr instr = (Instr)iiter.next();
                    Terminal.printBrightBlue("    "+instr.getName());
                    Terminal.println(" "+instr.getOperands());
                }
                Terminal.print("    [");
                printPair("next", block.getNextBlock());
                Terminal.print(", ");
                printPair("other", block.getOtherBlock());
                Terminal.println("]");
            }

        }

        private void printPair(String t, ControlFlowGraph.Block b) {
            String v = b == null ? "null" : StringUtil.addrToString(b.getAddress());
            Terminal.printPair(Terminal.COLOR_BRIGHT_GREEN, Terminal.COLOR_BRIGHT_CYAN, t, ": ", v);
        }

        public String getHelp() {
            return "The \"cfg\" action builds and displays a control flow graph of the " +
                    "given input program. This is useful for better program understanding " +
                    "and for optimizations.";
        }

    }

    static class CustomAction extends Action {
        public void run(String[] args) throws Exception {
            String clname = CLASS.get();
            if (clname.equals(""))
                Avrora.userError("Custom action class must be specified in -class option");
            try {
                Class cl = Class.forName(clname);
                Action a = (Action) cl.newInstance();
                a.run(args);
            } catch (java.lang.ClassNotFoundException e) {
                Avrora.userError("Could not find custom action class", StringUtil.quote(clname));
            } catch (java.lang.ClassCastException e) {
                Avrora.userError("Specified class does not extend avrora.Main.Action", StringUtil.quote(clname));
            }
        }

        public String getHelp() {
            return "The \"custom\" action allows a user to specify a Java class that " +
                    "contains an action to run. This is useful for external actions that " +
                    "are not part of the standard Avrora distribution. The \"class\" option " +
                    "specifies which Java class to load, instantiate and run. This class " +
                    "must extend the avrora.Main.Action class within Avrora.";
        }
    }

    static class TestAction extends Action {
        public void run(String[] args) throws Exception {
            new AutomatedTester(new AVRTestHarness()).runTests(args);
        }

        public String getHelp() {
            return "The \"test\" action invokes the internal automated testing framework " +
                    "that runs testcases supplied at the command line. The testcases are " +
                    "used in regressions for diagnosing bugs.";
        }
    }

    static class AssembleAction extends Action {
        public void run(String[] args) {
            throw Avrora.unimplemented();
        }

        public String getHelp() {
            return "The \"assemble\" action will invoke the assembler. This action is " +
                    "currently unimplemented.";
        }
    }

    static class ListAction extends Action {
        public void run(String[] args) throws Exception {
            ProgramReader r = (ProgramReader) inputs.get(INPUT.get());
            Program p = r.read(args);
            p.dump();
        }

        public String getHelp() {
            return "The \"list\" action prints a digest of the program.";
        }
    }

    public static ProgramReader getProgramReader() {
        return (ProgramReader) inputs.get(INPUT.get());
    }

    public static ProgramReader getProgramReader(String format) {
        return (ProgramReader) inputs.get(format);
    }

    public static void main(String[] args) {
        try {
            parseOptions(args);

            if (HELP.get()) {
                title();
                printHelp();
            } else {
                if (BANNER.get()) banner();

                if (configPrinter.enabled) {
                    listActions();
                    listInputs();
                    options.dump("avrora.Main.options", configPrinter);
                }

                Action a = (Action) actions.get(ACTION.get());
                if (a == null)
                    Avrora.userError("Unknown Action", StringUtil.quote(ACTION.get()));

                args = options.getArguments();

                for (int cntr = 0; cntr < REPEAT.get(); cntr++)
                    a.run(args);
            }

        } catch (Avrora.Error e) {
            e.report();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The <code>OptionComparator</code> is an implementation of the
     * <code>java.util.Comparator</code> interface that is used to sort options
     * alphabetically for printing in the help system.
     */
    static final Comparator OptionComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            Option opt1 = (Option) o1;
            Option opt2 = (Option) o2;
            return String.CASE_INSENSITIVE_ORDER.compare(opt1.getName(), opt2.getName());
        }
    };

    static void printHelp() {
        Collection c = options.getAllOptions();
        List l = Collections.list(Collections.enumeration(c));
        Collections.sort(l, OptionComparator);

        String usage = "avrora [-action=<action>] [options] <files>";

        Terminal.println("Usage: " + usage);
        Terminal.nextln();

        String overview = "Avrora is a tool for working with " +
                "assembly language programs for the AVR architecture microcontrollers. " +
                "It contains tools to read AVR programs in multiple formats, perform " +
                "actions on them, and generate output in multiple formats.\n" +
                "Typical usage is to specify a list of files that contain a program " +
                "in some format supported by Avrora and then specifying the action " +
                "to perform on that program. For example, giving the name of a file " +
                "that contains a program written in assembly language and a simulate " +
                "action might look like: \n\n" +
                "avrora -action=simulate -input=atmel program.asm \n\n" +
                "Other actions that are available include giving a listing of the " +
                "program or running one of the analysis tools on the program. See the " +
                "actions section for more information.";

        printSection("OVERVIEW", overview);

        String optstr = "Options specify the action to be performed as well as the input " +
                "format, the output format (if any), and parameters to the action. The " +
                "available options are listed below along with their types and default " +
                "values.";

        printSection("OPTIONS", optstr);

        Iterator i = l.iterator();
        while (i.hasNext()) {
            Option opt = (Option) i.next();
            opt.printHelp();
        }

        Terminal.println("");

        String actstr = "The action to be performed is specified in an option \"action\" " +
                "supplied at the command line. This action might be to assemble the file, " +
                "print a listing, perform a simulation, or run an analysis tool. This " +
                "flexibility allows this single frontend to select from multiple useful " +
                "tools.";

        printSection("ACTIONS", actstr);

        List list = Collections.list(Collections.enumeration(actions.keySet()));
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        i = list.iterator();
        while (i.hasNext()) {
            String a = (String) i.next();
            printGreenEqYellow("    -action", a);
            Terminal.nextln();
            String help = ((Action) actions.get(a)).getHelp();
            Terminal.println(StringUtil.makeParagraphs(help, 8, 0, 78));
        }


        String inpstr = "The input format of the program is specified with the \"input\" " +
                "option supplied at the command line. This input format is used by " +
                "actions that operate on programs to determine how to interpret the " +
                "input and build a program from it. For example, the input format might " +
                "be Atmel syntax, GAS syntax, or the output of a disassembler. Currently " +
                "no binary formats are supported.";

        printSection("INPUT FORMATS", inpstr);


        list = Collections.list(Collections.enumeration(inputs.keySet()));
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        i = list.iterator();
        while (i.hasNext()) {
            String a = (String) i.next();
            printGreenEqYellow("    -input", a);
            Terminal.nextln();
            String help = ((ProgramReader) inputs.get(a)).getHelp();
            Terminal.println(StringUtil.makeParagraphs(help, 8, 0, 78));
        }


        Terminal.println("For more information, see the online documentation: ");
        Terminal.printBrightCyan("http://compilers.cs.ucla.edu/avrora");
        Terminal.nextln();
    }

    private static void printGreenEqYellow(String s1, String s2) {
        Terminal.printPair(Terminal.COLOR_BRIGHT_GREEN, Terminal.COLOR_YELLOW, s1, "=", s2);
    }

    static void printSection(String title, String paragraphs) {
        Terminal.printBrightBlue(title);
        Terminal.println("\n");
        Terminal.println(StringUtil.makeParagraphs(paragraphs, 0, 4, 78));
        Terminal.nextln();
    }

    static void listActions() {
        configPrinter.startblock("avrora.Main.actions");
        printList(actions);
        configPrinter.endblock();
    }

    private static void printList(HashMap map) {
        List opts = Collections.list(Collections.enumeration(map.keySet()));
        Collections.sort(opts, String.CASE_INSENSITIVE_ORDER);

        int max = 0;

        Iterator i = opts.iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            if (key.length() > max) max = key.length();
        }


        i = opts.iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            configPrinter.println(StringUtil.leftJustify(key, max) + " : " + map.get(key).getClass());
        }
    }

    static void listInputs() {
        configPrinter.startblock("avrora.Main.inputs");
        printList(inputs);
        configPrinter.endblock();
    }

    static void banner() {
        title();
        String notice;
        if ( !LICENSE.get() )
            notice =
                    "This is a prototype simulator and analysis tool intended for evaluation " +
                    "and experimentation purposes only. It is provided with absolutely no " +
                    "warranty, expressed or implied. For more information about the license "+
                    "that this software is provided to you under, specify the \"license\" " +
                    "option.\n\n";
        else
            notice =

                    "Copyright (c) 2004, Regents of the University of California \n" +
                    "All rights reserved.\n\n" +

                    "Redistribution and use in source and binary forms, with or without " +
                    "modification, are permitted provided that the following conditions " +
                    "are met:\n\n" +

                    "Redistributions of source code must retain the above copyright notice, " +
                    "this list of conditions and the following disclaimer.\n\n" +

                    "Redistributions in binary form must reproduce the above copyright " +
                    "notice, this list of conditions and the following disclaimer in the " +
                    "documentation and/or other materials provided with the distribution.\n\n" +

                    "Neither the name of the University of California, Los Angeles nor the " +
                    "names of its contributors may be used to endorse or promote products " +
                    "derived from this software without specific prior written permission.\n\n" +

                    "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS " +
                    "\"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT " +
                    "LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR " +
                    "A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT " +
                    "OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, " +
                    "SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT " +
                    "LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, " +
                    "DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY " +
                    "THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT " +
                    "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE " +
                    "OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n\n";

        Terminal.print(StringUtil.makeParagraphs(notice, 0, 0, 60));
    }

    static void title() {
        Terminal.printBrightBlue("Avrora ");
        Terminal.print("[");
        Terminal.printBrightBlue(VERSION);
        Terminal.print("]");
        Terminal.print(" - (c) 2003-2004 UCLA Compilers Group\n\n");
    }

    static class LocationComparator implements java.util.Comparator {
        public int compare(Object o1, Object o2) {
            Location l1 = (Location) o1;
            Location l2 = (Location) o2;

            if (l1.address == l2.address) {
                if (l1.name == null) return 1;
                if (l2.name == null) return -1;
                return l1.name.compareTo(l2.name);
            }
            return l1.address - l2.address;
        }
    }

    static void formatError(String f, int i) {
        Avrora.userError("format error for program location(s) " + StringUtil.quote(f) + " @ character " + i);

    }

    /**
     * The <code>getLocationList()</code> method is to used to parse a list of
     * program locations and turn them into a list of <code>Main.Location</code>
     * instances.
     * @param program the program to look up labels in
     * @param v the list of strings that are program locations
     * @return a list of program locations
     */
    public static List getLocationList(Program program, List v) {
        HashSet locset = new HashSet();

        Iterator i = v.iterator();

        while (i.hasNext()) {
            String val = (String) i.next();
            if (val.charAt(0) == '$')
                locset.add(new Location(StringUtil.evaluateIntegerLiteral(val)));
            else {
                Program.Label l = program.getLabel(val);
                if (l == null) Avrora.userError("cannot find label " + StringUtil.quote(val) + " in specified program");
                locset.add(new Location(l.name, l.address));
            }
        }

        List loclist = Collections.list(Collections.enumeration(locset));
        Collections.sort(loclist, new LocationComparator());

        return loclist;
    }

    /**
     * The <code>getMicrocontroller()</code> method is used to get the current
     * microcontroller from the library of implemented ones, based on the
     * command line option that was specified (-chip=xyz).
     * @return an instance of <code>MicrocontrollerFactory</code> for the
     * microcontroller specified on the command line.
     */
    public static MicrocontrollerFactory getMicrocontroller() {
        MicrocontrollerFactory mcu = Microcontrollers.getMicrocontroller(CHIP.get());
        if (mcu == null)
            Avrora.userError("unknown microcontroller", CHIP.get());
        return mcu;
    }

    /**
     * The <code>getPlatform()</code> method is used to get the current
     * platform from the library of implemented ones, based on the command
     * line option that was specified (-platform=xyz).
     * @return an instance of <code>PlatformFactory</code> for the
     * platform specified on the command line
     */
    public static PlatformFactory getPlatform() {
        String pf = PLATFORM.get();
        if (pf.equals("")) return null;
        PlatformFactory pff = Platforms.getPlatform(pf);
        if (pff == null)
            Avrora.userError("Unknown platform", StringUtil.quote(pf));
        return pff;
    }

    /**
     * The <code>parseOptions()</code> method takes an array of strings
     * and parses it, extracting the options and storing the option values
     * in the internal state of main.
     * @param args the array of strings to parse into options
     */
    public static void parseOptions(String args[]) {
        options.parseCommandLine(args);
        Terminal.useColors = COLORS.get();
        Terminal.htmlColors = HTML.get();

        List verbose = VERBOSE.get();
        Iterator i = verbose.iterator();
        while (i.hasNext())
            Verbose.setVerbose((String) i.next(), true);

    }

}
