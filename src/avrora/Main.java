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
import avrora.syntax.atmel.AtmelProgramReader;
import avrora.syntax.gas.GASProgramReader;
import avrora.syntax.objdump.ObjDumpProgramReader;
import avrora.syntax.objdump.ObjDump2ProgramReader;
import avrora.util.*;
import avrora.util.help.HelpCategory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * This is the main entrypoint to Avrora. It is responsible for parsing the options to the main program and
 * selecting the appropriate action. Currently, it also implements the help system.
 *
 * @author Ben L. Titzer
 */
public class Main {

    static final String VERSION = Version.getVersion().toString();

    static final Options mainOptions = new Options();

    public static final Option.Str INPUT = mainOptions.newOption("input", "auto",
            "This option selects among the available program formats as input to Avrora. " +
            "For example, the default input format, \"atmel\" selects the assembly " +
            "language format supported by Atmel's assembler.");
    public static final Option.Str ACTION = mainOptions.newOption("action", "simulate",
            "This option selects the action to perform. For example, an action might " +
            "be to load a program into the simulator and run it. For more information, " +
            "see the section on actions.");
    public static final Option.Bool COLORS = mainOptions.newOption("colors", true,
            "This option is used to enable or disable the terminal colors.");
    public static final Option.Bool BANNER = mainOptions.newOption("banner", true,
            "This option is used to enable or disable the printing of the banner.");
    public static final Option.List VERBOSE = mainOptions.newOptionList("verbose", "",
            "This option allows users to enable verbose printing of individual " +
            "subsystems within Avrora. A list can be given with individual items separated " +
            "by commas. For example: -verbose=sim.pin,sim.interrupt,sim.event");
    public static final Option.Bool HELP = mainOptions.newOption("help", false,
            "Displays this help message.");
    public static final Option.Bool LICENSE = mainOptions.newOption("license", false,
            "Display the detailed copyright and license text.");
    public static final Option.Bool HTML = mainOptions.newOption("html", false,
            "For terminal colors. Display terminal colors as HTML tags for " +
            "easier inclusion in webpages.");
    public static final Option.List INDIRECT_EDGES = mainOptions.newOptionList("indirect-edges", "",
            "This option can be used to specify the possible targets of indirect calls and " +
            "jumps within a program, which may be needed in performing stack analysis or " +
            "building a control flow graph. Each element of the list is a pair of " +
            "program addresses separated by a colon, where a program address can be a " +
            "label or a hexadecimal number preceded by \"0x\". The first program address " +
            "is the address of the indirect call or jump instruction and the second program " +
            "address is a possible target.");
    public static final Option.Str FOREGROUND = mainOptions.newOption("foreground-color", "lightgray",
            "This option can be used to specify the default foreground color of " +
            "text outputted from Avrora. For terminals with a light-colored background, " +
            "the default choice may be hard or impossible to read. In that case, set this " +
            "option to a darker color such as \"black\" for readable output.");
    public static final Option.Str CONFIGFILE = mainOptions.newOption("config-file", "",
            "This option can be used to specify a file that contains additional command " +
            "line options to Avrora. Any command-line option can be specified in this " +
            "file. For repeated runs with similar options, the common options can be stored" +
            "in this file for use over multiple runs. Options are processed in the following " +
            "order: \n   1) The .avrora file in your home directory \n   2) A configuration " +
            "file specified on the command line \n   3) Command line options to Avrora");


    public static class AutoProgramReader extends ProgramReader {
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

            if (".asm".equals(extension))
                return new AtmelProgramReader().read(args);
            if (".s".equals(extension))
                return new GASProgramReader().read(args);
            if (".od".equals(extension))
                return new ObjDumpProgramReader().read(args);
            if (".od2".equals(extension))
                return new ObjDump2ProgramReader().read(args);

            Avrora.userError("file extension " + StringUtil.quote(extension) + " unknown");
            return null;
        }

        public String getHelp() {
            return "The \"auto\" input format inspects the extension of the given " +
                    "filename and chooses the best format based on that extension. For " +
                    "example, it assumes that the best format for the .asm extension is " +
                    "the Atmel syntax.";
        }
    }

    public static ProgramReader getProgramReader() {
        return Defaults.getProgramReader(INPUT.get());
    }

    /**
     * The <code>main()</code> method is the entrypoint into Avrora. It processes the command line options,
     * looks up the action, and prints help (if there are no arguments or the <code>-help</code> option is
     * specified.
     *
     * @param args an array of strings representing the command line arguments passed by the user
     */
    public static void main(String[] args) {
        try {
            loadUserDefaults();

            parseOptions(args);
            Terminal.setForegroundColor(FOREGROUND.get());

            if ( !"".equals(CONFIGFILE.get()) ) {
                loadFile(CONFIGFILE.get());
                parseOptions(args);
            }

            if (args.length == 0 || HELP.get()) {
                args = mainOptions.getArguments();
                title();
                printHelp(args);
            } else {
                args = mainOptions.getArguments();

                if (BANNER.get()) banner();

                Action a = Defaults.getAction(ACTION.get());
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
        if (hdir == null || "".equals(hdir)) return;

        File f = new File(hdir + "/.avrora");
        if (f.exists()) {
            Properties defs = new Properties();
            defs.load(new FileInputStream(f));
            mainOptions.process(defs);
        }
    }

    private static void loadFile(String fname) throws IOException {
        File f = new File(fname);
        if (f.exists()) {
            Properties defs = new Properties();
            defs.load(new FileInputStream(f));
            mainOptions.process(defs);
        } else {
            Avrora.userError("Configuration file does not exist", fname);
        }
    }

    static HelpCategory buildHelpCategory() {
        HelpCategory hc = new HelpCategory("main", "");
        hc.addSection("OVERVIEW","Avrora is a tool for working with " +
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
        hc.addOptionSection("The main options to Avrora specify the action to be performed as well as the input " +
                "format, the output format (if any), and any general configuration parameters for " +
                "Avrora. The available main options are listed below along with their types and default " +
                "values. Each action also has its own set of options. To access help for the options " +
                "related to an action, specify the name of the action along with the \"help\" option.", mainOptions);

        hc.addSection("ADDITIONAL HELP CATEGORIES", "Additional help is available on a category by category " +
                "basis. Below is a list of the additional categories available to provide help with actions, " +
                "input formats, monitors, and more. To access help for a specific category, specify the " +
                "\"-help\" option followed by the name of category.");

        hc.addSection(null, "For more information, see the online documentation:\n" +
                "http://compilers.cs.ucla.edu/avrora");
        return hc;
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

        String strs[] = {"Usage", ": ", "avrora", " [", "-action", "=", "action", "] [", "options", "] ", "<files>"};
        Terminal.print(colors, strs);
        Terminal.nextln();

        int colors2[] = {Terminal.COLOR_RED,
                         -1,
                         Terminal.COLOR_GREEN,
                         -1,
                         Terminal.COLOR_YELLOW,
                         -1};

        String strs2[] = {"Usage", ": ", "avrora -help", " [", "action", "]"};
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

        if (args.length == 0)
            printMainHelp();
        else if (args.length > 1)
            Avrora.userError("help available for only one action or input at a time.");
        else
            printHelp(args[0]);

        Terminal.println("For more information, see the online documentation: ");
        Terminal.printBrightCyan("http://compilers.cs.ucla.edu/avrora");
        Terminal.nextln();
    }

    private static void printHelp(String a) {
        Action action = Defaults.getAction(a);
        if (action == null)
            Avrora.userError("no help available for unknown action " + StringUtil.quote(a));

        String actname = StringUtil.quote(action.getShortName());
        printSection("HELP FOR THE " + actname + " ACTION", action.getHelp());

        if (action.options.size() > 0) {
            printSection("OPTIONS", "Below is a listing of the options available to the " + actname + " action.");
            printOptions(action.options);
        }
    }

    private static void printMainHelp() {
        printSection("OPTIONS", "Options specify the action to be performed as well as the input " +
                "format, the output format (if any), and any general configuration parameters for " +
                "Avrora. The available main options are listed below along with their types and default " +
                "values. Each action also has its own set of options. To access help for the options " +
                "related to an action, specify the name of the action along with the \"help\" option.");

        printOptions(mainOptions);
        Iterator i;

        printSection("ACTIONS", "The action to be performed is specified in an option \"action\" " +
                "supplied at the command line. This action might be to assemble the file, " +
                "print a listing, perform a simulation, or run an analysis tool. This " +
                "flexibility allows this single frontend to select from multiple useful " +
                "tools. The currently supported actions are given below.");

        List list = Defaults.getActionList();
        i = list.iterator();
        while (i.hasNext()) {
            String a = (String)i.next();
            printGreenEqYellow("    -action", a);
            Terminal.nextln();
            String help = (Defaults.getAction(a)).getHelp();
            Terminal.println(StringUtil.makeParagraphs(help, 8, 0, Terminal.MAXLINE));
        }

        Terminal.println("");

        printSection("INPUT FORMATS", "The input format of the program is specified with the \"input\" " +
                "option supplied at the command line. This input format is used by " +
                "actions that operate on programs to determine how to interpret the " +
                "input and build a program from the files specified. For example, the input format might " +
                "be Atmel syntax, GAS syntax, or the output of a disassembler such as avr-objdump. Currently " +
                "no binary formats are supported.");

        list = Defaults.getProgramReaderList();
        i = list.iterator();
        while (i.hasNext()) {
            String a = (String)i.next();
            printGreenEqYellow("    -input", a);
            Terminal.nextln();
            String help = (Defaults.getProgramReader(a)).getHelp();
            Terminal.println(StringUtil.makeParagraphs(help, 8, 0, Terminal.MAXLINE));
        }
        Terminal.println("");
    }

    private static void printOptions(Options options) {
        Collection c = options.getAllOptions();
        List l = Collections.list(Collections.enumeration(c));
        Collections.sort(l, Option.COMPARATOR);

        Iterator i = l.iterator();
        while (i.hasNext()) {
            Option opt = (Option)i.next();
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
        Terminal.println(StringUtil.makeParagraphs(paragraphs, 0, 4, Terminal.MAXLINE));
        Terminal.nextln();
    }

    static void banner() {
        title();
        String notice;
        if (!LICENSE.get())
            notice =
                    "This simulator and analysis tool is provided with absolutely no " +
                    "warranty, either expressed or implied. It is provided to you with the hope " +
                    "that it be useful for evaluation of and experimentation with microcontroller " +
                    "and sensor network programs. For more information about the license " +
                    "that this software is provided to you under, specify the \"license\" " +
                    "option.\n\n";
        else
            notice =

                    "Copyright (c) 2003-2005, Regents of the University of California \n" +
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

        Terminal.print(StringUtil.makeParagraphs(notice, 0, 0, Terminal.MAXLINE));
    }

    static void title() {
        Terminal.printBrightBlue("Avrora ");
        Terminal.print("[");
        Terminal.printBrightBlue(VERSION);
        Terminal.print("]");
        Terminal.print(" - (c) 2003-2005 UCLA Compilers Group\n\n");
    }

    /**
     * The <code>parseOptions()</code> method takes an array of strings and parses it, extracting the options
     * and storing the option values in the internal state of main.
     *
     * @param args the array of strings to parse into options
     */
    public static void parseOptions(String args[]) {
        mainOptions.parseCommandLine(args);
        Terminal.useColors = COLORS.get();
        Terminal.htmlColors = HTML.get();

        List verbose = VERBOSE.get();
        Iterator i = verbose.iterator();
        while (i.hasNext())
            Verbose.setVerbose((String)i.next(), true);

    }

    /**
     * The <code>readProgram()</code> method reads a program from the command line arguments given the format
     * specified at the command line. It will also process the indirect-call edge information and add it to
     * the <code>Program</code> instance returned. This method is primarily used by actions that manipulate
     * programs.
     *
     * @param args an array of strings representing command line arguments with the options removed
     * @return an instance of the <code>Program</code> class if the program can be loaded correctly
     * @throws Exception if there is an error loading the program, such as a file not found exception, parse
     *                   error, etc
     */
    public static Program readProgram(String[] args) throws Exception {
        ProgramReader reader = Defaults.getProgramReader(INPUT.get());
        Program p = reader.read(args);

        Iterator i = INDIRECT_EDGES.get().iterator();
        while (i.hasNext()) {
            String s = (String)i.next();
            int ind = s.indexOf(":");
            if (ind <= 0)
                throw Avrora.failure("invalid indirect edge format: " + StringUtil.quote(s));
            Program.Location loc = p.getProgramLocation(s.substring(0, ind));
            Program.Location tar = p.getProgramLocation(s.substring(ind + 1));
            p.addIndirectEdge(loc.address, tar.address);
        }

        return p;
    }

}
