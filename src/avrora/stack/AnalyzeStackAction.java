package avrora.stack;

import avrora.Main;
import avrora.core.Program;

/**
 * @author Ben L. Titzer
 */
public class AnalyzeStackAction extends Main.Action {
    public void run(String[] args) throws Exception {
        Main.ProgramReader r = Main.getProgramReader();
        Program p = r.read(args);
        Analyzer a = new Analyzer(p);

        if ( Main.TRACE.get() ) Analyzer.TRACE = true;

        a.run();
        a.report();
    }

    public String getHelp() {
        return "The \"analyze-stack\" option invokes the built-in stack analysis tool " +
                "on the specified program.";
    }
}
