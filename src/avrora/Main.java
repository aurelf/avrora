package avrora;

import vpc.Option;
import vpc.VPCBase;
import vpc.VPCError;
import vpc.test.AutomatedTester;
import vpc.util.Options;
import vpc.util.ColorTerminal;
import vpc.util.StringUtil;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import avrora.core.Program;
import avrora.core.Instr;
import avrora.sim.*;
import avrora.sim.util.Counter;
import avrora.sim.util.BranchCounter;
import avrora.syntax.atmel.AtmelParser;
import avrora.syntax.gas.GASParser;
import avrora.stack.Analyzer;

/**
 * This is the main entrypoint to Avrora.
 *
 * @author Ben L. Titzer
 */
public class Main extends VPCBase {

    static final String VERSION = "0.9.2";

    static final HashMap actions = new HashMap();
    static final HashMap inputs = new HashMap();
    static final Options options = new Options();

    static final Option.Str INPUT     = options.newOption("input", "atmel");
    static final Option.Str ACTION    = options.newOption("action", "simulate");
    static final Option.Str OUTPUT    = options.newOption("output", "");
    static final Option.Str BREAKS    = options.newOption("breakpoint", "");
    static final Option.Str COUNTS    = options.newOption("count", "");
    static final Option.Str BRANCHCOUNTS = options.newOption("branchcount", "");
    static final Option.Bool TIME     = options.newOption("time", false);
    static final Option.Long TIMEOUT  = options.newOption("timeout", (long)0);
    static final Option.Bool TOTAL    = options.newOption("total", false);
    static final Option.Bool TRACE    = options.newOption("trace", false);
    static final Option.Bool COLORS   = options.newOption("colors", true);
    static final Option.Bool BANNER   = options.newOption("banner", true);
    static final Option.Bool VERBOSE  = options.newOption("verbose", false);

    static {
        newAction("simulate", new SimulateAction());
        newAction("analyze-stack", new AnalyzeStackAction());
        newAction("assemble", new AssembleAction());
        newAction("test", new TestAction());
        newAction("list", new ListAction());
        newInput("gas", new GASInput());
        newInput("atmel", new AtmelInput());
    }

    static void newAction(String name, Action a) {
        actions.put(name, a);
    }

    static void newInput(String name, ProgramReader r) {
        inputs.put(name, r);
    }

    static abstract class Action {
        public abstract void run(String[] args) throws Exception;
    }

    static class TestAction extends Action {
        public void run(String[] args) throws Exception {
            new AutomatedTester(new AVRTestHarness()).runTests(args);
        }
    }

    static class Location {
        final String name;
        final int address;
        Object object;

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

    static class Comparator implements java.util.Comparator {
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

    static class SimulateAction extends Action {
        Program program;
        Simulator simulator;
        Counter total;
        long startms, endms;

        List counters;
        List branchcounters;

        public void run(String[] args) throws Exception {
            ProgramReader r = (ProgramReader)inputs.get(INPUT.get());
            program = r.read(args);
            simulator = new ATMega128L().loadProgram(program);

            processBreakPoints();
            processCounters();
            processBranchCounters();
            processTotal();
            processTimeout();

            if ( TRACE.get() ) {
                simulator.insertProbe(Simulator.TRACEPROBE);
            }

            startms = System.currentTimeMillis();
            try {
                simulator.start();
            } finally {
                endms = System.currentTimeMillis();

                reportCounters();
                reportBranchCounters();
                reportTotal();
                reportTime();
            }
        }

        void processBreakPoints() {
            Iterator i = getLocationList(BREAKS.get()).iterator();
            while ( i.hasNext() ) {
                Location l = (Location)i.next();
                simulator.insertBreakPoint(l.address);
            }
        }

        void processCounters() {
            HashSet cset = getLocationList(COUNTS.get());
            Iterator i = cset.iterator();
            while ( i.hasNext() ) {
                Location l = (Location)i.next();
                Counter c = new Counter();
                l.object = c;
                simulator.insertProbe(c, l.address);
            }
            counters = Collections.list(Collections.enumeration(cset));
            Collections.sort(counters, new Comparator());
        }

        void reportCounters() {
            if ( counters.isEmpty() ) return;
            ColorTerminal.printGreen("Counter results\n");
            Iterator i = counters.iterator();
            // TODO: clean up, make multicolumn, better justified.
            while ( i.hasNext() ) {
                Location l = (Location)i.next();
                Counter c = (Counter)l.object;
                String cnt = StringUtil.rightJustify(c.count, 8);
                String addr = addrToString(l.address);
                String name;
                if ( l.name != null )
                    name = "    "+l.name+" @ "+addr;
                else
                    name = "    "+addr;
                reportQuantity(name, cnt, "");
            }
        }

        void processBranchCounters() {
            HashSet bcset = getLocationList(BRANCHCOUNTS.get());
            Iterator i = bcset.iterator();
            while ( i.hasNext() ) {
                Location l = (Location)i.next();
                BranchCounter c = new BranchCounter();
                l.object = c;
                simulator.insertProbe(c, l.address);
            }
            branchcounters = Collections.list(Collections.enumeration(bcset));
            Collections.sort(branchcounters, new Comparator());
        }

        void reportBranchCounters() {
            if ( branchcounters.isEmpty() ) return;
            ColorTerminal.printGreen("Branch counter results\n");
            Iterator i = branchcounters.iterator();
            while ( i.hasNext() ) {
                Location l = (Location)i.next();
                BranchCounter c = (BranchCounter)l.object;
                String tcnt = StringUtil.rightJustify(c.takenCount, 8);
                String ntcnt = StringUtil.rightJustify(c.nottakenCount, 8);
                String addr = addrToString(l.address);
                String name;
                if ( l.name != null )
                    name = "    "+l.name+" @ "+addr;
                else
                    name = "    "+addr;
                reportQuantity(name, tcnt+" "+ntcnt, "taken/not taken");
            }

        }

        void processTotal() {
            if ( TOTAL.get() ) {
                simulator.insertProbe(total = new Counter());
            }
        }

        void reportTotal() {
            if ( total != null )
                reportQuantity("Total instructions executed", total.count, "");
        }

        void processTimeout() {
            long timeout = TIMEOUT.get();
            if ( timeout > 0 )
                simulator.insertProbe(new Simulator.InstructionCountTimeout(timeout));
        }

        void reportTime() {
            long diff = endms - startms;
            if ( TIME.get() ) {
                reportQuantity("Time for simulation", ((float)diff)/1000, "seconds");
                if ( total != null ) {
                    float thru = ((float)total.count) / (diff*1000);
                    reportQuantity("Average throughput", thru, "mips");
                }
            }
        }

        String addrToString(int address) {
            return VPCBase.toPaddedUpperHex(address, 4);
        }

        void reportQuantity(String name, long val, String units) {
            reportQuantity(name, Long.toString(val), units);
        }

        void reportQuantity(String name, float val, String units) {
            reportQuantity(name, Float.toString(val), units);
        }

        void reportQuantity(String name, String val, String units) {
            ColorTerminal.printGreen(name);
            ColorTerminal.print(": ");
            ColorTerminal.printBrightCyan(val);
            ColorTerminal.println(" "+units);
        }


        HashSet getLocationList(String v) {
            HashSet locations = new HashSet();

            StringCharacterIterator i = new StringCharacterIterator(v);

            while ( i.current() != CharacterIterator.DONE ) {
                if ( Character.isDigit(StringUtil.peek(i)) )
                    locations.add(new Location(StringUtil.readHexValue(i, 5)));
                else if ( Character.isJavaIdentifierStart(StringUtil.peek(i))) {
                    String ident = StringUtil.readIdentifier(i);
                    Program.Label l = program.getLabel(ident);
                    if ( l == null ) userError("cannot find label "+quote(ident)+" in specified program");
                    locations.add(new Location(l.name, l.address));
                } else {
                    formatError(v, i.getIndex());
                }
                if ( StringUtil.peek(i) == CharacterIterator.DONE ) break;
                if ( !StringUtil.peekAndEat(i, ',') ) {
                    formatError(v, i.getIndex());
                }
            }

            return locations;
        }
    }

    static void formatError(String f, int i) {
        userError("format error for program location(s) "+quote(f)+" @ character "+i);

    }

    static class AnalyzeStackAction extends Action {
        public void run(String[] args) throws Exception {
            ProgramReader r = (ProgramReader)inputs.get(INPUT.get());
            Program p = r.read(args);
            Analyzer a = new Analyzer(p);
            a.run();
        }
    }

    static class AssembleAction extends Action {
        public void run(String[] args) {
            throw VPCBase.unimplemented();
        }

    }

    static class ListAction extends Action {
        public void run(String[] args) throws Exception {
            ProgramReader r = (ProgramReader)inputs.get(INPUT.get());
            Program p = r.read(args);
            p.dump();
        }

    }

    static abstract class ProgramReader {
        public abstract Program read(String[] args) throws Exception;
    }

    static class GASInput extends ProgramReader {
        public Program read(String[] args) throws Exception {
            if ( args.length == 0 )
                userError("no input files");
            // TODO: handle multiple GAS files and link them
            if ( args.length != 1 )
                userError("input type \"gas\" accepts only one file at a time.");

            File f = new File(args[0]);
            Module module = new Module();
            FileInputStream fis = new FileInputStream(f);
            GASParser parser = new GASParser(fis, module, f.getName());
            parser.Module();
            return module.build();
        }
    }

    static class AtmelInput extends ProgramReader {
        public Program read(String[] args) throws Exception {
            if ( args.length == 0 )
                userError("no input files");
            if ( args.length != 1 )
                userError("input type \"atmel\" accepts only one file at a time.");

            File f = new File(args[0]);
            Module module = new Module();
            FileInputStream fis = new FileInputStream(f);
            AtmelParser parser = new AtmelParser(fis, module, f.getName());
            parser.Module();
            return module.build();
        }
    }


    public static void main(String[] args) {
        try {
            options.parseCommandLine(args);

            if ( BANNER.get() ) banner();

            Action a = (Action)actions.get(ACTION.get());
            if ( a == null )
                userError("unknown action", ACTION.get());

            args = options.getArguments();
            a.run(options.getArguments());

        } catch ( VPCError e ) {
            e.report();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    static void banner() {
        ColorTerminal.printBrightBlue("Avrora " + VERSION);
        ColorTerminal.print(" - (c) 2003-2004 Ben L. Titzer\n\n");
        ColorTerminal.println("This is a prototype simulator and analysis tool intended for evaluation");
        ColorTerminal.println("and experimentation purposes only. It is provided with absolutely no");
        ColorTerminal.println("warranty, expressed or implied.\n");
    }

}
