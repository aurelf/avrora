package avrora.actions;

import avrora.stack.isea.ISEAnalyzer;
import avrora.core.Program;
import avrora.core.SourceMapping;
import avrora.Main;
import avrora.Avrora;
import avrora.util.Option;

/**
 * The <code>ISEAAction</code> class implements interprocedural side-effect analysis. This class
 * implements an action that allows the user to invoke the analysis on a program from the command
 * line.
 *
 * @author Ben L. Titzer
 */
public class ISEAAction extends Action {

    protected final Option.Str START = options.newOption("procedure", "0x0000",
            "When this option is specified, the ISE analyzer will analyze only the specified procedure, rather " +
            "than the entire program.");

    public ISEAAction() {
        super("This action invokes the inter-procedural side-effect analysis tool.");
    }

    public void run(String[] args) throws Exception {
        Program p = Main.loadProgram(args);
        ISEAnalyzer a = new ISEAnalyzer(p);
        if ( !"".equals(START.get())) {
            SourceMapping.Location location = p.getSourceMapping().getLocation(START.get());
            if ( location == null )
                Avrora.userError("Cannot find program location "+START.get());
            a.analyze(location.address);
        } else {
            a.analyze();
        }
    }
}
