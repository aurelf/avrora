/**
 * Copyright (c) 2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jintgen.gen.disassembler;

import avrora.util.*;

import java.util.*;

import jintgen.isdl.EncodingDecl;
import jintgen.isdl.AddrModeDecl;
import jintgen.isdl.OperandTypeDecl;

/**
 * The <code>DecodingTree</code> class represents a node in the decoding tree for a particular
 * architecture. Each node in the decoding tree matches on a contiguous field of bits within
 * the instruction's encoding. The value of these bits are used to determine which child
 * node the matching should continue to.
 * Each node has a starting bit (left_bit) and an ending bit (right_bit) that
 * denote the start and end of the bit field that this node matches on.
 *
 * @author Ben L. Titzer
 */
public class DecodingTree {
    HashSet<EncodingInfo> lowPrio = new HashSet<EncodingInfo>();
    HashSet<EncodingInfo> highPrio = new HashSet<EncodingInfo>();
    private String label;
    private int minlength = 128;
    private int minprio = 128;
    private int maxprio = 0;
    private int hashCode = -1;
    int left_bit;
    int right_bit;
    int node_num;

    HashMap<Integer, DecodingTree> children = new HashMap<Integer, DecodingTree>();
    private Verbose.Printer verbose;

    public DecodingTree() {
        verbose = Verbose.getVerbosePrinter("jintgen.disassem");
    }

    public void setLabel(String l) {
        label = l;
    }

    public String getLabel() {
        return label;
    }

    public DecodingTree shallowCopy() {
        DecodingTree dt = new DecodingTree();
        dt.label = label;
        dt.minlength = minlength;
        dt.minprio = minprio;
        dt.maxprio = maxprio;
        dt.left_bit = left_bit;
        dt.right_bit = right_bit;
        return dt;
    }

    void computeRange(int depth) {
        // there should be at least one encoding in every decoding tree
        assert lowPrio.size() > 0;

        int[] prio = newPriorityArray();
        int[] length = newLengthArray();
        int[] counts = new int[minlength];
        verbose.println("--> scanning...");
        // for each encoding, increment the count of each concrete bit
        for ( EncodingInfo ei : lowPrio ) {
            if ( ei.encoding.getPriority() == minprio ) highPrio.add(ei);
            ei.printVerbose(depth, verbose);
            // scan for the most lucrative bit range
            scanForBitRange(ei, prio, counts, length);
        }

        // scan from the left for the bit that is most often concrete
        int max = getLeftBit(prio, counts, length);

        verbose.println("result: decode["+left_bit+":"+right_bit+"]");

        // problem: no encodings have any concrete bits left
        if ( max == 0 ) {
            if ( highPrio.size() > 1 ) {
                DGUtil.ambiguous(highPrio);
            } else {
                left_bit = right_bit = 0;
            }
        }

        // remove all the high priority encodings from the main encoding set
        lowPrio.removeAll(highPrio);
    }

    public int hashCode() {
        if ( hashCode == -1 ) {
            hashCode = label.hashCode();
            // if we have a child for zero, add to hash code
            DecodingTree zdt = children.get(0);
            if ( zdt != null ) hashCode += zdt.hashCode();
        }
        return hashCode;
    }

    public boolean equals(Object o) {
        if ( !(o instanceof DecodingTree) ) return false;
        DecodingTree dt = (DecodingTree)o;
        if ( dt.left_bit != left_bit ) return false;
        if ( dt.right_bit != right_bit ) return false;
        if ( !dt.label.equals(label) ) return false;
        if ( children.size() != dt.children.size() ) return false;
        for ( Map.Entry<Integer, DecodingTree> e : children.entrySet() ) {
            int value = e.getKey();
            DecodingTree cdt = e.getValue();
            DecodingTree odt = dt.children.get(value);
            if ( cdt != odt ) return false;
        }
        return true;
    }

    private int[] newPriorityArray() {
        int[] prio = new int[minlength];
        Arrays.fill(prio, maxprio);
        return prio;
    }

    private int getLeftBit(int[] prio, int[] counts, int[] length) {
        int max = 0;
        for ( int cntr = 0; cntr < minlength; cntr++ ) {
            int count = counts[cntr];
            // only select bit ranges that are concrete in the highest priority level
            if ( prio[cntr] == minprio && count > max ) {
                left_bit = cntr;
                max = count;
            }
        }
        assert length[left_bit] > 0;
        right_bit = left_bit + length[left_bit] - 1;
        return max;
    }

    private void scanForBitRange(EncodingInfo ei, int[] prio, int[] counts, int[] length) {
        int len = 1;
        int p = ei.encoding.getPriority();
        // scan backwards through the bit states. for each
        // concrete bit range, record the number of bits until it meets a non-concrete bit.
        // this limits the length of a concrete bit match so that all encodings with
        // that concrete bit set have the entire bit range set
        for ( int cntr = minlength - 1; cntr >= 0; cntr--, len++ ) {
            if ( ei.isConcrete(cntr) ) {
                if ( len < length[cntr] ) length[cntr] = len;
                counts[cntr]++;
                if ( p < prio[cntr] ) prio[cntr] = p;
            } else {
                len = 0;
            }
        }
    }

    private int[] newLengthArray() {
        int[] la = new int[minlength];
        for ( int cntr = 0; cntr < minlength; cntr++ ) {
            la[cntr] = minlength - cntr;
        }
        return la;
    }

    public void addEncoding(EncodingInfo ei) {
        lowPrio.add(ei);
        int prio = ei.encoding.getPriority();
        int length = ei.getLength();
        if ( length < minlength ) minlength = length;
        if ( prio < minprio ) minprio = prio;
        if ( prio > maxprio ) maxprio = prio;
    }

    void createChildren() {
        List<EncodingInfo> unmatched = new LinkedList<EncodingInfo>();

        // if this node is a singleton, remove all but highest encoding
        if ( createSingleton() ) return;

        // create the main branches
        createMainBranches(unmatched);

        // create the default branch
        createDefaultBranch(unmatched);

        assert children.size() > 0;
    }

    private boolean createSingleton() {
        if ( highPrio.size() == 1 ) {
            // get the encoding info of the singleton
            EncodingInfo ei = highPrio.iterator().next();
            // if the left bit is not concrete, there are no bits left
            if ( !ei.isConcrete(left_bit) ) {
                // this node represents a terminal node (no children)
                // and overrides all of the lower priority encodings
                lowPrio.clear();
                return true;
            }
        }
        return false;
    }

    private void createMainBranches(List<EncodingInfo> unmatched) {
        // all of the encodings at the high priority must have the bits set
        for ( EncodingInfo ei : highPrio ) {
            if ( ei.isConcrete(left_bit) ) createChild(ei);
            else  DGUtil.ambiguous(highPrio);
        }
        // for the rest of the encodings, add them to children or unmatched list
        for ( EncodingInfo ei : lowPrio ) {
            if ( ei.isConcrete(left_bit) ) createChild(ei);
            else unmatched.add(ei);
        }
    }

    private void createDefaultBranch(List<EncodingInfo> unmatched) {
        if ( unmatched.size() > 0 ) {
            // replicate the unmatched encodings over all branches
            for ( EncodingInfo ei : unmatched ) {
                for ( DecodingTree dt : children.values() )
                    dt.addEncoding(ei.copy());
            }
            // if the tree is not complete, add a default branch
            if ( children.size() < 1 << (right_bit - left_bit + 1) ) {
                DecodingTree dt = new DecodingTree();
                children.put(new Integer(-1), dt);
                for ( EncodingInfo ei : unmatched ) {
                    dt.addEncoding(ei.copy());
                }
            }
        }
    }

    private void createChild(EncodingInfo ei) {
        // get the value of the bits and add to corresponding subtree
        Integer iv = new Integer(extractValue(ei));
        DecodingTree dt = children.get(iv);
        if ( dt == null ) {
            dt = new DecodingTree();
            children.put(iv, dt);
        }
        dt.addEncoding(ei);
    }

    private int extractValue(EncodingInfo ei) {
        int value = 0;
        for ( int cntr = left_bit; cntr <= right_bit; cntr++ ) {
            byte bitState = ei.bitStates[cntr];
            switch (bitState) {
                case EncodingInfo.ENC_ZERO:
                    value = value << 1;
                    ei.bitStates[cntr] = EncodingInfo.ENC_MATCHED_ZERO;
                    break;
                case EncodingInfo.ENC_ONE:
                    value = value << 1 | 1;
                    ei.bitStates[cntr] = EncodingInfo.ENC_MATCHED_ONE;
                    break;
                default:
                    throw Util.failure("invalid bit state at "+cntr+" in "+ei);
            }
        }
        return value;
    }

    void compute(int depth) {
        computeRange(depth);
        createChildren();
        for ( DecodingTree dt : children.values() ) {
            dt.compute(depth+1);
        }
    }

    private void generateDecodeStatements(Printer p, EncodingInfo ei) {
        for ( EncodingField e : ei.simplifiedExprs ) {
            e.generateDecoder(p);
        }
    }

    private void generateConstructorCall(Printer p, EncodingInfo ei) {
        p.print("return new "+DisassemblerGenerator.getInstrClassName(ei)+"(pc");
        for ( AddrModeDecl.Operand o : ei.instr.getOperands() ) {
            p.print(", ");
            String getexpr = generateDecode(ei, o);
            p.print(getexpr);
        }
        p.println(");");
    }

    private void declareOperands(Printer p, EncodingInfo ei) {
        int size = ei.bitStates.length / 8;
        for ( int cntr = 2; size > 2; cntr += 2, size -= 2 ) {
            int wordnum = (cntr / 2) + 1;
            if ( size > 3 )
                p.println("int word"+wordnum+" = getWord("+(wordnum-1)+");");
            else
                p.println("int word"+wordnum+" = getByte("+(wordnum-1)+");");
        }
        for ( AddrModeDecl.Operand o : ei.instr.getOperands() ) {
            if ( !isFixed(ei, o) )
                p.println("int "+o.name+" = 0;");
        }
    }

    private boolean isFixed(EncodingInfo ei, AddrModeDecl.Operand o) {
        if ( ei.encoding.isConditional() ) {
            EncodingDecl.Cond c = ei.encoding.getCond();
            if ( o.name.image.equals(c.name.image) )
                return true;
        }
        // if this is a register, we have to look it up in the table
        return false;
    }

    private String generateDecode(EncodingInfo ei, AddrModeDecl.Operand o) {
        OperandTypeDecl ot = o.getOperandType();
        if ( ei.encoding.isConditional() ) {
            EncodingDecl.Cond c = ei.encoding.getCond();
            if ( o.name.image.equals(c.name.image) )
                return c.expr.toString();
        }
        // TODO: fix symbol lookup
        return o.name.image;
    }
}
