package avrora.sim;

import avrora.Main;
import avrora.Avrora;

/**
 * @author Simon Han
 */
public class MultiSimulateAction extends Main.Action {

    public void run(String[] args) {
        throw Avrora.unimplemented();
    }

    public String getHelp() {
        return "The \"multi-simulate\" action launches a set of simulators with " +
                " the specified program.";

    }
}
