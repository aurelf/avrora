
package avrora.core;

import avrora.Avrora;

import java.util.List;

/**
 * The <code>ControlFlowGraph</code> represents a control flow graph
 * for an entire program, including all basic blocks and all procedures.
 *
 * @author Ben L. Titzer
 */
public class ControlFlowGraph {

    public class Block {
        final int address;
        List instrs;

        Block next;
        Block other;

        Block(int addr) {
            address = addr;
        }

        public Block splitAt(int addr) {

            throw Avrora.unimplemented();
        }

        public void addInstr(Instr i) {
            instrs.add(i);
        }
    }


    public Block getBlockStartingAt(int address) {
        throw Avrora.unimplemented();
    }

    public Block getBlockContaining(int address) {
        throw Avrora.unimplemented();
    }

}
