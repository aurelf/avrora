
package avrora.core;

import avrora.Avrora;

import java.util.*;

/**
 * The <code>ControlFlowGraph</code> represents a control flow graph
 * for an entire program, including all basic blocks and all procedures.
 *
 * @author Ben L. Titzer
 */
public class ControlFlowGraph {

    /**
     * The <code>Block</code> class represents a basic block of code within
     * the program. A basic block contains a straight-line piece of code that
     * ends with a control instruction (e.g. a skip, a jump, a branch, or a call) or
     * an implicit fall-through to the next basic block. It contains at most
     * two references to successor basic blocks.
     *
     * For <b>fallthroughs</b> (no ending
     * control instruction), the <code>next</code> field refers to the block
     * immediately following this block, and the <code>other</code> field is null.
     *
     * For <b>jumps</b>, the <code>other</code> field refers to the block that
     * is the target of the jump, and the <code>next</code> field is null.
     *
     * For <b>skips</b>, <b>branches</b>, and <b>calls</b>, the <code>next</code>
     * field refers to the block immediately following this block (i.e. not-taken
     * for branches, the return address for calls). The <code>other</code> field
     * refers to the target address of the branch if it is taken or the address
     * to be called.
     *
     * For <b>indirect jumps</b> both the <code>next</code>
     * and <code>other</code> fields are null.
     *
     * For <b>indirect calls</b> the <code>next</code> field refers to the block
     * immediately following this block (i.e. the return address). The
     * <code>other</code> field is null because the target of the call cannot
     * be known.
     */
    public class Block {

        private final int address;
        private int size;
        private int length;

        private List list;

        private Block next;
        private Block other;

        Block(int addr) {
            address = addr;
            list = new LinkedList();
        }


        /**
         * The <code>addInstr()</code> method adds an instruction to the end of this
         * basic block. It is not recommended for general use: it is generally used
         * by the <code>CFGBuilder</code> class. No enforcement of invariants is made:
         * this method does not check whether the instruction being added changes the
         * control flow or branches to another block, etc.
         *
         * @param i the instruction to add to this basic block
         */
        public void addInstr(Instr i) {
            list.add(i);

            size += i.getSize();
            length++;
        }

        /**
         * The <code>hashCode()</code> method computes the hash code of this block.
         * In the initial implementation, the hash code is simply the byte address of
         * the block
         * @return an integer value that is the hash code of this object
         */
        public int hashCode() {
            return address;
        }

        /**
         * The <code>equals()</code> method computes object equality for basic blocks.
         * It simply compares the addresses of the basic blocks and returns true if they
         * match.
         * @param o the other object to compare to
         * @return true if these two basic blocks are equivalent; false otherwise
         */
        public boolean equals(Object o) {
            if ( this == o ) return true;
            if ( !(o instanceof Block) ) return false;
            return ((Block)o).address == this.address;
        }

        /**
         * The <code>getNextBlock()</code> method allows access to the next block following
         * this basic block. The next block is defined as the block to which this block
         * falls through when a branch is not taken (or when a call returns). Therefore,
         * for jump, return, and break instructions that end a basic block, the next block
         * is null.
         * @return a reference to the next basic block
         */
        public Block getNextBlock() {
            return next;
        }

        /**
         * The <code>getOtherBlock()</code> method allows access to the block which could
         * be entered as a result of a call, a jump, a branch, or a skip at the end of this
         * block. For example, the other block for a jump is basic block which the
         * instruction jumps to. For branches, this is the block where control will go
         * when the branch is taken. For calls, this is the address being called.
         * @return a reference to the other basic block
         */
        public Block getOtherBlock() {
            return other;
        }

        /**
         * The <code>getAddress()</code> method gets the starting byte address of this
         * basic block.
         * @return the starting address of this basic block
         */
        public int getAddress() {
            return address;
        }

        /**
         * The <code>getSize()</code> method returns the size of the basic block
         * in bytes.
         * @return the number of bytes in this basic block
         */
        public int getSize() {
            return size;
        }

        /**
         * The <code>getLength()</code> returns the length of this basic block in
         * terms of the number of instructions
         * @return the number of instructions in this basic block
         */
        public int getLength() {
            return length;
        }

        /**
         * The <code>setNext()</code> method sets the reference to the next block.
         * This is not recommended for general use: it is used by the <code>CFGBuilder</code>
         * class during construction of a control flow graph.
         * @param n the new next block
         */
        public void setNext(Block n) {
            next = n;
        }

        /**
         * The <code>setOther()</code> method sets the reference to the other block.
         * This is not recommended for general use: it is used by the <code>CFGBuilder</code>
         * class during construction of a control flow graph.
         * @param o the new other block
         */
        public void setOther(Block o) {
            other = o;
        }

        /**
         * The <code>getInstrIterator()</code> method returns an iterator over the
         * instructions in this basic block. The resulting iterator can be used to
         * iterate over the instructions in the basic block in order.
         * @return an iterator over the instructions in this block.
         */
        public Iterator getInstrIterator() {
            return list.iterator();
        }
    }

    private class BlockComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Block b1 = (Block)o1;
            Block b2 = (Block)o2;

            return b1.address - b2.address;
        }
    }

    /**
     * The <code>blocks</code> field contains a reference to a map from <code>Integer</code>
     * to <code>Block</code> this map is used to lookup the basic block that starts at
     * a particular address.
     */
    protected final HashMap blocks;


    ControlFlowGraph() {
        blocks = new HashMap();
    }

    /**
     * The <code>newBlock()</code> method creates a new block within the control flow
     * graph, starting at the specified address. No checking is done by this method
     * as to whether the address overlaps with another block. This is primarily intended
     * for use within the <code>CFGBuilder</code> class.
     * @param address the byte address at which this block begins
     * @return an instance of <code>Block</code> representing the new block
     */
    public Block newBlock(int address) {
        Block b = new Block(address);
        blocks.put(new Integer(address), b);
        return b;
    }

    /**
     * The <code>getBlockStartingAt()</code> method looks up a basic block based
     * on its starting address. If a basic block contains the address, but does not
     * begin at that address, that basic block is ignored.
     * @param address the byte address at which the block begins
     * @return a reference to the <code>Block</code> instance that starts at the
     * address specified, if such a block exists; null otherwise
     */
    public Block getBlockStartingAt(int address) {
        return (Block)blocks.get(new Integer(address));
    }

    /**
     * The <code>getBlockContaining()</code> method looks up the basic block that
     * contains the address specified. The basic blocks are assumed to not overlap.
     * @return a reference to the <code>Block</code> instance that contains the
     * address specified, if such a block exists; null otherwise
     */
    public Block getBlockContaining(int address) {
        throw Avrora.unimplemented();
    }

    /**
     * The <code>getBlockIterator()</code> method constructs an interator over all
     * of the blocks in the control flow graph, regardless of connectivity. No order
     * is guaranteed.
     * @return an instance of <code>Iterator</code> that can be used to iterate over
     * all blocks in the control flow graph
     */
    public Iterator getBlockIterator() {
        return blocks.values().iterator();
    }

    /**
     * The <code>getBlockIterator()</code> method constructs an interator over all
     * of the blocks in the control flow graph, regardless of connectivity. The order
     * is guaranteed to be in ascending order.
     * @return an instance of <code>Iterator</code> that can be used to iterate over
     * all blocks in the control flow graph in ascending order
     */
    public Iterator getSortedBlockIterator() {
        List l = Collections.list(Collections.enumeration(blocks.values()));
        Collections.sort(l, new BlockComparator());
        return l.iterator();
    }
}
