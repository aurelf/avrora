package avrora;

import avrora.core.Program;
import avrora.sim.Microcontroller;
import avrora.sim.SimulateAction;
import avrora.sim.mcu.Microcontrollers;
import avrora.stack.AnalyzeStackAction;
import avrora.syntax.atmel.AtmelProgramReader;
import avrora.syntax.gas.GASProgramReader;
import avrora.util.*;
import avrora.test.AutomatedTester;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
         * @param args the command line arguments
         * @return a program instance representing the program
         * @throws Exception
         */
        public abstract Program read(String[] args) throws Exception;
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
            if ( name == null ) return address;
            else return name.hashCode();
        }

        public boolean equals(Object o) {
            if ( o == this ) return true;
            if ( !(o instanceof Location) ) return false;
            Location l = ((Location)o);
            return l.name == this.name && l.address == this.address;
        }

    }



    static final String VERSION = "0.9.5";

    static final HashMap actions = new HashMap();
    static final HashMap inputs = new HashMap();
    static final Options options = new Options();

    public static final Option.Str INPUT     = options.newOption("input", "atmel");
    public static final Option.Str ACTION    = options.newOption("action", "simulate");
    public static final Option.Str OUTPUT    = options.newOption("output", "");
    public static final Option.Str BREAKS    = options.newOption("breakpoint", "");
    public static final Option.Str COUNTS    = options.newOption("count", "");
    public static final Option.Str BRANCHCOUNTS = options.newOption("branchcount", "");
    public static final Option.Bool PROFILE  = options.newOption("profile", false);
    public static final Option.Bool TIME     = options.newOption("time", false);
    public static final Option.Long TIMEOUT  = options.newOption("timeout", (long)0);
    public static final Option.Bool TOTAL    = options.newOption("total", false);
    public static final Option.Bool CYCLES   = options.newOption("cycles", false);
    public static final Option.Bool TRACE    = options.newOption("trace", false);
    public static final Option.Bool COLORS   = options.newOption("colors", true);
    public static final Option.Bool BANNER   = options.newOption("banner", true);
    public static final Option.Str  VERBOSE  = options.newOption("verbose", "");
    public static final Option.Int  REPEAT   = options.newOption("repeat", 1);
    public static final Option.Str  CHIP     = options.newOption("chip", "atmega128l");
    public static final Option.Str  PLATFORM = options.newOption("platform", "");

    public static final Verbose.Printer configPrinter = Verbose.getVerbosePrinter("config");

    static {
        newAction("simulate", new SimulateAction());
        newAction("analyze-stack", new AnalyzeStackAction());
        newAction("assemble", new AssembleAction());
        newAction("test", new TestAction());
        newAction("list", new ListAction());
        newInput("gas", new GASProgramReader());
        newInput("atmel", new AtmelProgramReader());
    }

    static void newAction(String name, Action a) {
        actions.put(name, a);
    }

    static void newInput(String name, ProgramReader r) {
        inputs.put(name, r);
    }


    static class TestAction extends Action {
        public void run(String[] args) throws Exception {
            new AutomatedTester(new AVRTestHarness()).runTests(args);
        }
    }

    static class AssembleAction extends Action {
        public void run(String[] args) {
            throw Avrora.unimplemented();
        }

    }

    static class ListAction extends Action {
        public void run(String[] args) throws Exception {
            ProgramReader r = (ProgramReader)inputs.get(INPUT.get());
            Program p = r.read(args);
            p.dump();
        }

    }

    public static ProgramReader getProgramReader() {
        return (ProgramReader)inputs.get(INPUT.get());
    }

    public static void main(String[] args) {
        try {
            parseOptions(args);

            if ( BANNER.get() ) banner();

            if ( configPrinter.enabled )
                options.dump("avrora.Main.options", configPrinter);

            Action a = (Action)actions.get(ACTION.get());
            if ( a == null )
                Avrora.userError("Unknown Action", StringUtil.quote(ACTION.get()));

            args = options.getArguments();

            for ( int cntr = 0; cntr < REPEAT.get(); cntr++ )
                a.run(args);

        } catch ( Avrora.Error e ) {
            e.report();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    static void banner() {
        Terminal.printBrightBlue("Avrora " + VERSION);
        Terminal.print(" - (c) 2003-2004 UCLA Compilers Group\n\n");
        Terminal.println("This is a prototype simulator and analysis tool intended for evaluation");
        Terminal.println("and experimentation purposes only. It is provided with absolutely no");
        Terminal.println("warranty, expressed or implied.\n");
    }

    static class LocationComparator implements java.util.Comparator {
        public int compare(Object o1, Object o2) {
            Location l1 = (Location)o1;
            Location l2 = (Location)o2;

            if ( l1.address == l2.address ) {
                if ( l1.name == null ) return 1;
                if ( l2.name == null ) return -1;
                return l1.name.compareTo(l2.name);
            }
            return l1.address - l2.address;
        }
    }

    static void formatError(String f, int i) {
        Avrora.userError("format error for program location(s) "+StringUtil.quote(f)+" @ character "+i);

    }

    public static List getLocationList(Program program, String v) {
        HashSet locset = new HashSet();

        StringCharacterIterator i = new StringCharacterIterator(v);

        while ( i.current() != CharacterIterator.DONE ) {
            if ( Character.isDigit(StringUtil.peek(i)) )
                locset.add(new Location(StringUtil.readHexValue(i, 5)));
            else if ( Character.isJavaIdentifierStart(StringUtil.peek(i))) {
                String ident = StringUtil.readIdentifier(i);
                Program.Label l = program.getLabel(ident);
                if ( l == null ) Avrora.userError("cannot find label "+StringUtil.quote(ident)+" in specified program");
                locset.add(new Location(l.name, l.address));
            } else {
                formatError(v, i.getIndex());
            }
            if ( StringUtil.peek(i) == CharacterIterator.DONE ) break;
            if ( !StringUtil.peekAndEat(i, ',') ) {
                formatError(v, i.getIndex());
            }
        }

        List loclist = Collections.list(Collections.enumeration(locset));
        Collections.sort(loclist, new LocationComparator());

        return loclist;
    }

    public static Microcontroller getMicrocontroller() {
        Microcontroller mcu = Microcontrollers.getMicrocontroller(CHIP.get());
        if ( mcu == null )
            Avrora.userError("unknown microcontroller", CHIP.get());
        return mcu;
    }

    public static void parseOptions(String args[]) {
        options.parseCommandLine(args);
        Terminal.useColors = COLORS.get();

        String verbose = VERBOSE.get();
        CharacterIterator i = new StringCharacterIterator(verbose);
        while ( i.current() != CharacterIterator.DONE ) {
            String ident = StringUtil.readDotIdentifier(i);
            Verbose.setVerbose(ident, true);
            if ( StringUtil.peekAndEat(i, ',') ) continue;
            if ( i.current() == CharacterIterator.DONE ) break;
            if ( !Character.isLetter(i.current()) )
                Avrora.userError("syntax error in -verbose option");
        }
    }

}
