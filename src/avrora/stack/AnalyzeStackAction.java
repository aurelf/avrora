package avrora.stack;

import avrora.Main;
import avrora.core.Program;

/**
 * The <code>AnalyzeStackAction</code> class is an extension of the <code>Main.Action</code>
 * class that allows the stack tool to be reached from the command line.
 *
 * @author Ben L. Titzer
 */
public class AnalyzeStackAction extends Main.Action {
    /**
     * The <code>run()</code> method runs the stack analysis by loading the program from
     * the command line options specified, creating an instance of the <code>Analyzer</code>
     * class, and running the analysis.
     * @param args the string arguments that are the files containing the program
     * @throws Exception if the program cannot be loaded correctly
     */
    public void run(String[] args) throws Exception {
        Main.ProgramReader r = Main.getProgramReader();
        Program p = r.read(args);
        Analyzer a = new Analyzer(p);

        if (Main.TRACE.get()) Analyzer.TRACE = true;

        a.run();
        a.report();
    }

    /**
     * The <code>getHelp()</code> method returns a string that is used in reporting
     * the command line help to the user.
     * @return an unformatted paragraph that contains the text explanation of what the
     * stack analysis tool is and does.
     */
    public String getHelp() {
        return "The \"analyze-stack\" option invokes the built-in stack analysis tool " +
                "on the specified program.";
    }
}
