package avrora;

import vpc.Option;
import vpc.VPCBase;
import vpc.VPCError;
import vpc.test.AutomatedTester;
import vpc.util.Options;
import vpc.util.ColorTerminal;
import vpc.util.StringUtil;

import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import avrora.core.Program;
import avrora.core.Instr;
import avrora.sim.Simulator;
import avrora.sim.ATMega128L;
import avrora.sim.State;
import avrora.sim.Counter;
import avrora.syntax.atmel.AtmelParser;
import avrora.syntax.gas.GASParser;
import avrora.stack.Analyzer;

/**
 * This is the main entrypoint to Avrora.
 *
 * @author Ben L. Titzer
 */
public class Main extends VPCBase {

    static final String VERSION = "0.9.1";

    static final HashMap actions = new HashMap();
    static final HashMap inputs = new HashMap();
    static final Options options = new Options();

    static final Option.Str INPUT     = options.newOption("input", "atmel");
    static final Option.Str ACTION    = options.newOption("action", "simulate");
    static final Option.Str OUTPUT    = options.newOption("output", "");
    static final Option.Str BREAKS    = options.newOption("breakpoint", "");
    static final Option.Str COUNTS    = options.newOption("count", "");
    static final Option.Bool TIME     = options.newOption("time", false);
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

    static class SimulateAction extends Action {
        public void run(String[] args) throws Exception {
            ProgramReader r = (ProgramReader)inputs.get(INPUT.get());
            Program p = r.read(args);
            Simulator s = new ATMega128L().loadProgram(p);

            String breaks = BREAKS.get();

            if ( !breaks.equals("") ) {
                CharacterIterator i = new StringCharacterIterator(breaks);
                int brk = StringUtil.readDecimalValue(i, 10);
                s.insertBreakPoint(brk);
            }

            String counts = COUNTS.get();

            Counter c = null;
            int cbrk = 0;
            if ( !counts.equals("") ) {
                CharacterIterator i = new StringCharacterIterator(counts);
                cbrk = StringUtil.readDecimalValue(i, 10);
                s.insertProbe(c = new Counter(), cbrk);
            }


            if ( TRACE.get() ) {
                s.insertProbe(Simulator.TRACEPROBE);
            }

            Counter total = null;
            if ( TOTAL.get() ) {
                s.insertProbe(total = new Counter());
            }

            long ms = System.currentTimeMillis();
            try {
                s.start();
            } finally {
                long diff = System.currentTimeMillis() - ms;

                if ( c != null )
                    ColorTerminal.println("Count for "+VPCBase.toPaddedUpperHex(cbrk, 4) + " = "+c.count);
                if ( total != null )
                    ColorTerminal.println("Total instruction count = "+total.count);
                if ( TIME.get() ) {
                    ColorTerminal.println("Time for simulation = "+((float)diff)/1000+" seconds");
                    if ( total != null ) {
                        float thru = ((float)total.count) / (diff*1000);
                        ColorTerminal.println("Average instruction throughput = "+thru+" MIPS");
                    }
                }
            }
        }
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
        ColorTerminal.println("This is a prototype simulator and analsyis tool intended for evaluation");
        ColorTerminal.println("and experimentation purposes only. It is provided with absolutely no");
        ColorTerminal.println("warranty, expressed or implied.\n");
    }

}
