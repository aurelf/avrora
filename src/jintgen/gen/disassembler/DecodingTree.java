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

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

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
    HashSet<EncodingInfo> encodings = new HashSet<EncodingInfo>();
    String methodname;
    int left_bit;
    int right_bit = DisassemblerGenerator.LARGEST_INSTR;
    int value;
    int depth;

    HashMap<Integer, DecodingTree> children = new HashMap<Integer, DecodingTree>();
    private Verbose.Printer verbose;

    public DecodingTree() {
        verbose = Verbose.getVerbosePrinter("jintgen.disassem");
    }

    void computeRange() {
        if ( encodings.size() == 0) {
            // this should not happen. how is it possible to create a new encoding set with no members?
            verbose.println("scanning...[empty]");
            return;
        } else if ( encodings.size() == 1 ) {
            // this encoding set has only one member, meaning that it is a leaf and needs no further
            // children.
            Iterator i = encodings.iterator();
            EncodingInfo ei = (EncodingInfo)i.next();
            verbose.println("singleton: ");
            ei.print(verbose);
            return;
        }

        // scan for the leftmost concrete bit range common to all encodings in this set.
        verbose.println("scanning...");
        for ( EncodingInfo ei : encodings ) {
            ei.print(verbose);

            int lb = scanForLeftBit(ei);
            if ( lb >= ei.bitStates.length ) {
                // there are no concrete bits in this encoding!
                // It cannot be disambiguated from the other members of the set!
                ambiguous(ei);
            }

            int rb = scanForRightBit(lb, ei);

            if ( lb > rb ) {
                // there is no common bit among all of the instructions of this set!
                // there is an ambiguity that needs to be resolved.
                ambiguous(ei);
            }

            left_bit = lb;
            right_bit = rb;
        }
    }

    void ambiguous(EncodingInfo ei) {
        Terminal.nextln();
        Terminal.printRed("ERROR");
        Terminal.println(": encodings are ambiguous");
        verbose.enabled = true;
        ei.print(verbose);
        Terminal.println("-- cannot be distinguished from --");
        for ( EncodingInfo el : encodings )
            if ( el != ei ) el.print(verbose);
        throw Util.failure("Disassembler generator cannot continue");
    }

    private int scanForRightBit(int lb, EncodingInfo ei) {
        int rb = right_bit;
        // scan from the left_bit (known to be concrete) to the first unknown bit
        // move right_bit if necessary
        for ( int cntr = lb; cntr <= rb; cntr++ ) {
            byte bitState = ei.bitStates[cntr];
            if ( bitState != EncodingInfo.ENC_ZERO && bitState != EncodingInfo.ENC_ONE ) {
                rb = cntr-1;
                break;
            }
        }
        return rb;
    }

    private int scanForLeftBit(EncodingInfo ei) {
        int lb = left_bit;
        // start at left bit and scan until a concrete bit is found
        while ( lb < ei.bitStates.length ) {
            byte bitState = ei.bitStates[lb];
            if ( bitState == EncodingInfo.ENC_ZERO ) break;
            if ( bitState == EncodingInfo.ENC_ONE ) break;
            lb++;
        }
        return lb;
    }

    void createChildren() {
        if ( encodings.size() <= 1) return;
        for ( EncodingInfo ei : encodings ) {
            // iterate through the bit states of this encoding for this bit range
            // set the bit states to either MATCHED_ONE or MATCHED_ZERO
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

            // add the instruction to the encoding set corresponding to the value of
            // the bits in this range
            Integer iv = new Integer(value);
            DecodingTree es = children.get(iv);
            if ( es == null ) {
                es = new DecodingTree();
                es.depth = depth+1;
                children.put(iv, es);
            }

            es.encodings.add(ei);
        }
    }

    void compute() {
        computeRange();
        createChildren();
        recurse();
    }

    void recurse() {
        for ( DecodingTree es : children.values() ) {
            es.compute();
        }
    }

    void generateCode(int meth_num, Printer p) {
        // recursively generate code for each of the children
        for ( DecodingTree es : children.values() ) {
            es.generateCode(meth_num++, p);
        }

        if ( methodname == null ) {
            if ( children.size() > 0)
                methodname = "decode_"+meth_num;
            else {
                EncodingInfo ei = encodings.iterator().next();
                methodname = "decode_"+ei.getName();
            }
        }

        p.startblock("private "+DisassemblerGenerator.instrClassName+" "+methodname+"(int word1) throws InvalidInstruction");

        if ( children.size() > 0 ) {
            // if there are any children, we need to generate a switch statement over
            // the possible values of this bit range
            generateSwitch(p);
        } else {
            // this encoding set has no children; it therefore decodes to one and only
            // one instruction.
            generateLeaf(p);
        }
        p.endblock();
    }

    private void generateSwitch(Printer p) {
        int high_bit = DisassemblerGenerator.nativeBitOrder(left_bit);
        int low_bit = DisassemblerGenerator.nativeBitOrder(right_bit);
        int mask = Arithmetic.getBitRangeMask(low_bit, high_bit);
        p.println("// get value of bits logical["+left_bit+":"+right_bit+"]");
        p.println("int value = (word1 >> "+low_bit+") & "+StringUtil.to0xHex(mask, 5)+";");
        p.startblock("switch ( value )");

        for ( Integer value : children.keySet() ) {
            int val = value.intValue();
            // generate a case for each value of the bits in this test.
            p.print("case "+StringUtil.to0xHex(val, 5)+": ");
            DecodingTree child = children.get(value);
            p.println("return "+child.methodname+"(word1);");
        }

        p.println("default: return null;");
        p.endblock();
    }

    private void generateLeaf(Printer p) {
        boolean check = false;
        int mask = 0;
        int value = 0;
        // first check for any left over concrete bits that must match
        EncodingInfo ei = encodings.iterator().next();

        if ( ei.encoding.isConditional() ) {
            EncodingDecl.Cond c = ei.encoding.getCond();
            p.println("// this method decodes "+ei.instr.innerClassName+" when "+c.name+" == "+c.expr);
        }

        // go through each of the bits in the bit states. if any of the bits have
        // not been matched yet, then they need to be checked to make sure that
        // they match.
        for ( int cntr = 0; cntr < ei.bitStates.length; cntr++ ) {
            byte bitState = ei.bitStates[cntr];
            if ( bitState == EncodingInfo.ENC_ZERO ) {
                check = true;
                value = value << 1;
                mask = mask << 1 | 1;
            } else if ( bitState == EncodingInfo.ENC_ONE ) {
                check = true;
                value = value << 1 | 1;
                mask = mask << 1 | 1;
            } else {
                value = value << 1;
                mask = mask << 1;
            }
        }

        if ( check ) {
            // generate a check on the left over bits to verify they match this encoding.
            p.startblock("if ( (word1 & "+StringUtil.to0xHex(mask, 5)+") != "+StringUtil.to0xHex(value, 5)+" )");
            p.println("return null;");
            p.endblock();
        }

        // declare each operand
        declareOperands(p, ei);

        // generate the code that reads the operands from the instruction encoding
        generateDecodeStatements(p, ei);

        // generate the call to the Instr class constructor
        generateConstructorCall(p, ei);
    }

    private void generateDecodeStatements(Printer p, EncodingInfo ei) {
        for ( EncodingField e : ei.simplifiedExprs ) {
            e.generateDecoder(verbose);
        }
    }

    private void generateConstructorCall(Printer p, EncodingInfo ei) {
        p.print("return new "+getClassName(ei)+"(pc");
        for ( AddrModeDecl.Operand o : ei.instr.getOperands() ) {
            p.print(", ");
            String getexpr = generateDecode(ei, o);
            p.print(getexpr);
        }
        p.println(");");
    }

    private String getClassName(EncodingInfo ei) {
        return DisassemblerGenerator.instrClassName+"."+ei.instr.innerClassName;
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
