package avrora;

import vpc.Option;
import vpc.VPCBase;
import vpc.VPCError;
import vpc.test.AutomatedTester;
import vpc.util.Options;

import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;

import avrora.sir.Program;
import avrora.sim.Simulator;
import avrora.sim.ATMega128L;
import avrora.syntax.atmel.AtmelParser;

/**
 * This is the main entrypoint to Avrora.
 *
 * @author Ben L. Titzer
 */
public class Main extends VPCBase {

    static final String VERSION = "0.9.0";

    static final HashMap actions = new HashMap();
    static final HashMap inputs = new HashMap();
    static final Options options = new Options();

    static final Option.Str INPUT    = options.newOption("input", "atmel");
    static final Option.Str ACTION   = options.newOption("action", "simulate");
    static final Option.Str OUTPUT   = options.newOption("output", "");
    static final Option.Bool COLORS   = options.newOption("colors", true);
    static final Option.Bool BANNER   = options.newOption("banner", true);
    static final Option.Bool VERBOSE  = options.newOption("verbose", false);

    static {
        newAction("simulate", new SimulateAction());
        newAction("analyze-stack", new StackAction());
        newAction("assemble", new AssembleAction());
        newAction("test", new TestAction());
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
            s.start();
        }

    }

    static class StackAction extends Action {
        public void run(String[] args) {
            throw VPCBase.unimplemented();
        }
    }

    static class AssembleAction extends Action {
        public void run(String[] args) {
            throw VPCBase.unimplemented();
        }

    }

    static abstract class ProgramReader {
        public abstract Program read(String[] args) throws Exception;
    }

    static class GASInput extends ProgramReader {
        public Program read(String[] args) {
            throw VPCBase.unimplemented();
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
}
