package avrora.actions;

import avrora.stack.isea.ISEAnalyzer;
import avrora.core.Program;
import avrora.core.SourceMapping;
import avrora.Main;
import avrora.Avrora;
import avrora.util.Option;

/**
 * @author Ben L. Titzer
 */
public class ISEAAction extends Action {

    protected final Option.Str START = options.newOption("procedure", "0x0000",
            "This option specifies the location of the procedure for which to compute the ISEA information.");

    public ISEAAction() {
        super("This action invokes the inter-procedural side-effect analysis tool.");
    }

    public void run(String[] args) throws Exception {
        Program p = Main.loadProgram(args);
        ISEAnalyzer a = new ISEAnalyzer(p);
        SourceMapping.Location location = p.getSourceMapping().getLocation(START.get());
        if ( location == null )
            Avrora.userError("Cannot find program location "+START.get());
        a.analyze(location.address);
    }
}
