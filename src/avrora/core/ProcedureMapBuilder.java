package avrora.core;

import java.util.*;

/**
 * @author Ben L. Titzer
 */
public class ProcedureMapBuilder {

    private final Program program;
    private final ControlFlowGraph cfg;

    public ProcedureMapBuilder(Program p) {
        program = p;
        cfg = p.getCFG();
    }

    public ProcedureMap buildMap() {
        discoverProcedures();
        return null;
    }

    private HashSet ENTRYPOINTS;
    private HashMap PROCMAP;

    // this object marks a block as shared between two procedures
    private final Object SHARED = new Object();

    private void discoverProcedures() {

        discoverEntrypoints();
        PROCMAP = new HashMap();

        Iterator iter = ENTRYPOINTS.iterator();
        while (iter.hasNext()) {
            Object block = iter.next();
            PROCMAP.put(block, block);
        }

        iter = ENTRYPOINTS.iterator();
        while (iter.hasNext()) {
            ControlFlowGraph.Block block = (ControlFlowGraph.Block) iter.next();
            propagate(block, block, new HashSet());
        }

    }

    private void discoverEntrypoints() {
        ENTRYPOINTS = new HashSet();

        // discover edges that have incoming call edges
        Iterator edges = cfg.getEdgeIterator();
        while (edges.hasNext()) {
            ControlFlowGraph.Edge edge = (ControlFlowGraph.Edge) edges.next();
            if (edge.getType().equals("CALL")) {
                if (edge.getTarget() == null) {
                    addIndirectEntrypoints(edge, cfg);
                } else
                    ENTRYPOINTS.add(edge.getTarget());
            }
        }
    }

    private void addIndirectEntrypoints(ControlFlowGraph.Edge edge, ControlFlowGraph cfg) {
        List l = program.getIndirectEdges(edge.getSource().getLastAddress());
        if ( l == null ) return;
        Iterator i = l.iterator();
        while ( i.hasNext() ) {
            int target_addr = ((Integer)i.next()).intValue();
            ControlFlowGraph.Block target = cfg.getBlockStartingAt(target_addr);
            if ( target != null ) {
                ENTRYPOINTS.add(target);
            }
        }
    }

    private void propagate(ControlFlowGraph.Block entry, ControlFlowGraph.Block block, HashSet seen) {
        if (PROCMAP.get(block) == SHARED) return;

        Iterator edges = block.getEdgeIterator();
        while (edges.hasNext()) {
            ControlFlowGraph.Edge edge = (ControlFlowGraph.Edge) edges.next();
            if (edge.getType().equals("CALL")) continue;
            ControlFlowGraph.Block target = edge.getTarget();
            if (target == null) continue;
            mark(entry, target);

            if (!seen.contains(target)) {
                seen.add(target);
                propagate(entry, target, seen);
            } else {
                seen.add(target);
            }
        }
    }

    private void mark(ControlFlowGraph.Block entry, ControlFlowGraph.Block block) {
        Object c = PROCMAP.get(block);
        if (c == SHARED) return;
        if (c == null) c = entry;
        if (c != entry)
            markShared(block);
        else
            PROCMAP.put(block, entry);
    }

    private void markShared(ControlFlowGraph.Block block) {
        if (PROCMAP.get(block) == SHARED) return;
        PROCMAP.put(block, SHARED);

        Iterator edges = block.getEdgeIterator();
        while (edges.hasNext()) {
            ControlFlowGraph.Edge edge = (ControlFlowGraph.Edge) edges.next();
            if (edge.getType().equals("CALL")) continue;
            ControlFlowGraph.Block target = edge.getTarget();
            if (target == null) continue;
            markShared(target);
        }
    }


}
