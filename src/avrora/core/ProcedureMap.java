package avrora.core;

import java.util.Set;

/**
 * @author Ben L. Titzer
 */
public class ProcedureMap {

    public boolean isInAnyProcedure(ControlFlowGraph.Block b) {
        return false;
    }

    public ControlFlowGraph.Block getProcedureContaining(ControlFlowGraph.Block b) {
        return null;
    }

    public Set getReachableBlocks(ControlFlowGraph.Block b) {
        return null;
    }

    public Set getProcedureEntrypoints() {
        return null;
    }
}
