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
import avrora.core.ControlFlowGraph;
import avrora.core.Instr;
import avrora.core.isdl.*;
import avrora.sim.mcu.Microcontroller;
import avrora.sim.platform.PlatformFactory;
import avrora.sim.platform.Platforms;
import avrora.sim.mcu.Microcontrollers;
import avrora.sim.mcu.MicrocontrollerFactory;
import avrora.actions.*;
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
import java.io.PrintStream;
import java.io.IOException;

/**
 * This is the main entrypoint to Avrora.
 *
 * @author Ben L. Titzer
 */
public class Main {
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

    static final String VERSION = "Beta 1.1.17";

    static final HashMap actions = new HashMap();
    static final HashMap inputs = new HashMap();
    static final Options mainOptions = new Options();

    public static final Option.Str INPUT = mainOptions.newOption("input", "auto",
            "This option selects among the available program formats as input to Avrora. " +
            "For example, the default input format, \"atmel\" selects the assembly " +
            "language format supported by Atmel's assembler.");
    public static final Option.Str ACTION = mainOptions.newOption("action", "simulate",
            "This option selects the action to perform. For example, an action might " +
            "be to load a program into the simulator and run it. For more information, " +
            "see the section on actions.");
    public static final Option.Str OUTPUT = mainOptions.newOption("output", "",
            "This option selects an output format for the type of actions that output " +
            "a new program, like an assembler, disassembler or optimizer.");
    public static final Option.Bool COLORS = mainOptions.newOption("colors", true,
            "This option is used to enable or disable the terminal colors.");
    public static final Option.Bool BANNER = mainOptions.newOption("banner", true,
            "This option is used to enable or disable the printing of the banner.");
    public static final Option.List VERBOSE = mainOptions.newOptionList("verbose", "",
            "This option allows users to enable verbose printing of individual " +
            "subsystems within Avrora. For more information, see the section on verbose " +
            "printing.");
    public static final Option.Bool HELP = mainOptions.newOption("help", false,
            "Displays this help message.");
    public static final Option.Bool LICENSE = mainOptions.newOption("license", false,
            "Display the detailed copyright and license text.");
    public static final Option.Bool HTML = mainOptions.newOption("html", false,
            "For terminal colors. Display terminal colors as HTML tags for " +
            "easier inclusion in webpages.");
    public static final Option.List INDIRECT_EDGES = mainOptions.newOptionList("indirect-edges", "",
            "This option can be used to specify the possible targets of indirect calls and " +
            "jumps within a program, which may be needed in performing stack analysis or" +
            "building a control flow graph. Each element of the list is a pair of " +
            "program addresses separated by a colon, where a program address can be a " +
            "label or a hexadecimal number preceded by a \"$\". The first program address " +
            "is the address of the indirect call or jump instruction and the second program " +
            "address is a possible target.");


    public static final Verbose.Printer configPrinter = Verbose.getVerbosePrinter("config");

    static {
        newAction(new MultiSimulateAction());
        newAction(new SimulateAction());
        newAction(new AnalyzeStackAction());
        newAction(new TestAction());
        newAction(new ListAction());
        newAction(new CFGAction());
        newAction(new ISDLAction());
        newAction(new CustomAction());
        newAction(new BenchmarkAction());
        newInput("auto", new AutoProgramReader());
        newInput("gas", new GASProgramReader());
        newInput("atmel", new AtmelProgramReader());
        newInput("objdump", new ObjDumpProgramReader());
    }

    static void newAction(Action a) {
        actions.put(a.getShortName(), a);
    }

    static void newInput(String name, ProgramReader r) {
        inputs.put(name, r);
    }

    static class AutoProgramReader extends ProgramReader {
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

            if (extension.equals(".asm"))
                return new AtmelProgramReader().read(args);
            if (extension.equals(".s"))
                return new GASProgramReader().read(args);
            if (extension.equals(".od"))
                return new ObjDumpProgramReader().read(args);

            Avrora.userError("file extension " + StringUtil.quote(extension) + " unknown");
            return null;
        }

        public String getHelp() {
            return "The \"auto\" input format inspects the extension of the given " +
                    "filename and chooses the best format based on that extension. For " +
                    "example, it assumes that the best format for the .asm extenstion is " +
                    "the Atmel syntax.";
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
            loadUserDefaults();

            parseOptions(args);

            if (args.length == 0 || HELP.get()) {
                args = mainOptions.getArguments();
                title();
                printHelp(args);
            } else {
                args = mainOptions.getArguments();

                if (BANNER.get()) banner();

                if (configPrinter.enabled) {
                    listActions();
                    listInputs();
                    mainOptions.dump("avrora.Main.options", configPrinter);
                }

                Action a = (Action) actions.get(ACTION.get());
                if (a == null)
                    Avrora.userError("Unknown Action", StringUtil.quote(ACTION.get()));

                a.options.process(mainOptions);
                a.run(args);
            }

        } catch (Avrora.Error e) {
            e.report();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadUserDefaults() throws IOException {
        String hdir = System.getProperty("user.home");
        if ( hdir == null || hdir.equals("") ) return;

        File f = new File(hdir+"/.avrora");
        if ( f.exists() ) {
            Properties defs = new Properties();
            defs.load(new FileInputStream(f));
            mainOptions.process(defs);
        }
    }

    static void printHelp(String[] args) {
        int colors[] = {Terminal.COLOR_RED,
                        -1,
                        Terminal.COLOR_GREEN,
                        -1,
                        Terminal.COLOR_GREEN,
                        -1,
                        Terminal.COLOR_YELLOW,
                        -1,
                        Terminal.COLOR_YELLOW,
                        -1,
                        Terminal.COLOR_YELLOW,
                        -1};

        String strs[] = {"Usage", ": ", "avrora", " [", "-action", "=", "<action>", "] [", "options", "] ", "<files>"};
        Terminal.print(colors, strs);
        Terminal.nextln();

        int colors2[] = {Terminal.COLOR_RED,
                         -1,
                         Terminal.COLOR_GREEN,
                         -1,
                         Terminal.COLOR_YELLOW,
                         -1};

        String strs2[] = {"Usage", ": ", "avrora -help", " [", "<action>", "]"};
        Terminal.print(colors2, strs2);
        Terminal.println("\n");

        printSection("OVERVIEW", "Avrora is a tool for working with " +
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
                "actions section for more information.");

        if ( args.length == 0 )
            printMainHelp();
        else if ( args.length > 1 )
            Avrora.userError("help available for only one action or input at a time.");
        else
            printHelp(args[0]);

        Terminal.println("For more information, see the online documentation: ");
        Terminal.printBrightCyan("http://compilers.cs.ucla.edu/avrora");
        Terminal.nextln();
    }

    private static void printHelp(String a) {
        Action action = (Action)actions.get(a);
        if ( action == null )
            Avrora.userError("no help available for unknown action "+StringUtil.quote(a));

        String actname = StringUtil.quote(action.getShortName());
        printSection("HELP FOR THE "+actname+" ACTION", action.getHelp());

        printSection("OPTIONS", "Below is a listing of the options available to the "+actname+" action.");
        printOptions(action.options);
    }

    private static void printMainHelp() {
        printSection("OPTIONS", "Options specify the action to be performed as well as the input " +
                "format, the output format (if any), and parameters to the action. The " +
                "available options are listed below along with their types and default " +
                "values.");

        printOptions(mainOptions);
        Iterator i;

        printSection("ACTIONS", "The action to be performed is specified in an option \"action\" " +
                "supplied at the command line. This action might be to assemble the file, " +
                "print a listing, perform a simulation, or run an analysis tool. This " +
                "flexibility allows this single frontend to select from multiple useful " +
                "tools. The currently supported actions are given below.");

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

        Terminal.println("");

        printSection("INPUT FORMATS", "The input format of the program is specified with the \"input\" " +
                "option supplied at the command line. This input format is used by " +
                "actions that operate on programs to determine how to interpret the " +
                "input and build a program from it. For example, the input format might " +
                "be Atmel syntax, GAS syntax, or the output of a disassembler. Currently " +
                "no binary formats are supported.");

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
    }

    private static void printOptions(Options options) {
        Collection c = options.getAllOptions();
        List l = Collections.list(Collections.enumeration(c));
        Collections.sort(l, Option.COMPARATOR);

        Iterator i = l.iterator();
        while (i.hasNext()) {
            Option opt = (Option) i.next();
            opt.printHelp();
        }

        Terminal.println("");
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
        if (!LICENSE.get())
            notice =
                    "This is a prototype simulator and analysis tool intended for evaluation " +
                    "and experimentation purposes only. It is provided with absolutely no " +
                    "warranty, expressed or implied. For more information about the license " +
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
            locset.add(getProgramLocation(val, program));
        }

        List loclist = Collections.list(Collections.enumeration(locset));
        Collections.sort(loclist, new LocationComparator());

        return loclist;
    }

    public static Location getProgramLocation(String val, Program program) {
        if (val.charAt(0) == '$')
            return (new Location(StringUtil.evaluateIntegerLiteral(val)));
        else {
            Program.Label l = program.getLabel(val);
            if (l == null) Avrora.userError("cannot find label " + StringUtil.quote(val) + " in specified program");
            return (new Location(l.name, l.address));
        }
    }

    /**
     * The <code>parseOptions()</code> method takes an array of strings
     * and parses it, extracting the options and storing the option values
     * in the internal state of main.
     * @param args the array of strings to parse into options
     */
    public static void parseOptions(String args[]) {
        mainOptions.parseCommandLine(args);
        Terminal.useColors = COLORS.get();
        Terminal.htmlColors = HTML.get();

        List verbose = VERBOSE.get();
        Iterator i = verbose.iterator();
        while (i.hasNext())
            Verbose.setVerbose((String) i.next(), true);

    }

    public static Program readProgram(String[] args) throws Exception {
        ProgramReader reader = getProgramReader();
        Program p = reader.read(args);

        Iterator i = INDIRECT_EDGES.get().iterator();
        while (i.hasNext()) {
            String s = (String) i.next();
            int ind = s.indexOf(":");
            if (ind <= 0)
                throw Avrora.failure("invalid indirect edge format: " + StringUtil.quote(s));
            Location loc = getProgramLocation(s.substring(0, ind), p);
            Location tar = getProgramLocation(s.substring(ind + 1), p);
            p.addIndirectEdge(loc.address, tar.address);
        }

        return p;
    }

}
