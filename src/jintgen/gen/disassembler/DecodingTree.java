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
import java.util.Map;

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
    int minlength = 128;
    int left_bit;
    int right_bit;

    HashMap<Integer, DecodingTree> children = new HashMap<Integer, DecodingTree>();
    private Verbose.Printer verbose;

    public DecodingTree() {
        verbose = Verbose.getVerbosePrinter("jintgen.disassem");
    }

    void computeRange(int depth) {
        assert encodings.size() > 0;

        if ( encodings.size() == 1 ) {
            // this encoding set has only one member; it will have no branches,
            // but there might be bits that still need to be checked
            verbose.println("singleton: ");
        }

        int length[] = newLengthArray();
        int counts[] = new int[minlength];
        verbose.println("--> scanning...");
        // for each encoding, increment the count of each concrete bit
        for ( EncodingInfo ei : encodings ) {
            ei.printVerbose(depth, verbose);
            // for each bit, increment the count if it is concrete
            incrementCounts(ei, counts);
            // for each non-concrete bit, trim the possible field length
            trimLengths(ei, length);
        }

        // scan from the left for the bit that is most often concrete
        int max = 0;
        for ( int cntr = 0; cntr < minlength; cntr++ ) {
            int count = counts[cntr];
            if ( count > max ) {
                left_bit = cntr;
                max = count;
            }
        }
        assert length[left_bit] > 0;
        right_bit = left_bit + length[left_bit] - 1;

        // problem: no encodings have any concrete bits left
        if ( max == 0 && encodings.size() > 1 ) ambiguous();
    }

    private void trimLengths(EncodingInfo ei, int[] length) {
        int len = 1;
        // scan backwards through the bit states. for each
        // concrete bit range, record the number of bits until it meets a non-concrete bit.
        // this limits the length of a concrete bit match so that all encodings with
        // that concrete bit set have the entire bit range set
        for ( int cntr = minlength - 1; cntr >= 0; cntr--, len++ ) {
            byte bitState = ei.bitStates[cntr];
            if ( bitState != EncodingInfo.ENC_ONE && bitState != EncodingInfo.ENC_ZERO )
                len = 0;
            else if ( length[cntr] < len ) length[cntr] = len;
        }
    }

    private void incrementCounts(EncodingInfo ei, int[] counts) {
        for ( int cntr = 0; cntr < minlength; cntr++ ) {
            byte bitState = ei.bitStates[cntr];
            if ( bitState == EncodingInfo.ENC_ONE || bitState == EncodingInfo.ENC_ZERO )
                counts[cntr]++;
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
        encodings.add(ei);
        int length = ei.getLength();
        if ( length < minlength ) minlength = length;
    }

    void ambiguous() {
        Terminal.nextln();
        Terminal.printRed("ERROR");
        Terminal.println(": encodings are ambiguous");
        verbose.enabled = true;
        Terminal.println("-- The following encodings cannot be distinguished --");
        for ( EncodingInfo el : encodings )
            el.printVerbose(0, verbose);
        throw Util.failure("Disassembler generator cannot continue");
    }

    void createChildren() {
        if ( encodings.size() <= 1) return;
        for ( EncodingInfo ei : encodings ) {
            // iterate through the bit states of this encoding for this bit range
            // set the bit states to either MATCHED_ONE or MATCHED_ZERO
            int value = extractValue(ei);

            // add the instruction to the encoding set corresponding to the value of
            // the bits in this range
            Integer iv = new Integer(value);
            DecodingTree es = children.get(iv);
            if ( es == null ) {
                es = new DecodingTree();
                children.put(iv, es);
            }

            es.addEncoding(ei);
        }
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
        recurse(depth+1);
    }

    void recurse(int depth) {
        for ( DecodingTree es : children.values() ) {
            es.compute(depth);
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

    public void dump(Printer p, int depth) {
        if ( children.size() == 0 ) {
            for ( EncodingInfo ei : encodings ) ei.print(0, p);
            return;
        }
        int length = right_bit - left_bit + 1;
        p.println("decode["+left_bit+":"+right_bit+"]: ");
        for ( Map.Entry<Integer, DecodingTree> e : children.entrySet() ) {
            int val = e.getKey();
            indent(depth+1, p);
            p.print(StringUtil.toBin(val, length)+" -> ");
            e.getValue().dump(p, depth+1);
        }
    }

    private void indent(int depth, Printer p) {
        for ( int cntr = 0; cntr < depth; cntr++ ) p.print("    ");
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
            e.generateDecoder(p);
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
