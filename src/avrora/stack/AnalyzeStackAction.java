package avrora.stack;

import avrora.core.Program;
import avrora.stack.Analyzer;
import avrora.Main;

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
}
