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

import jintgen.isdl.InstrDecl;
import jintgen.isdl.AddrModeDecl;
import jintgen.isdl.EncodingDecl;
import jintgen.isdl.Property;
import jintgen.jigir.Expr;
import jintgen.jigir.Literal;
import jintgen.jigir.BitRangeExpr;
import jintgen.gen.disassembler.DisassemblerGenerator;
import jintgen.gen.disassembler.EncodingField;
import jintgen.gen.ConstantPropagator;

import java.util.List;
import java.util.LinkedList;

import avrora.util.Arithmetic;
import avrora.util.StringUtil;
import avrora.util.Verbose;
import avrora.util.Printer;

/**
 * @author Ben L. Titzer
 */
public class EncodingInst {
    final InstrDecl instr;
    final AddrModeDecl addrMode;
    final EncodingDecl encoding;
    final int encodingNumber;
    final byte[] bitStates;
    final List<EncodingField> simplifiedExprs;
    public static final byte ENC_ONE  = 1;
    public static final byte ENC_ZERO = 2;
    public static final byte ENC_MATCHED_ONE = 3;
    public static final byte ENC_MATCHED_ZERO = 4;
    public static final byte ENC_VAR  = 0;

    EncodingInst(InstrDecl id, AddrModeDecl am, int encNum, EncodingDecl ed) {
        instr = id;
        addrMode = am;
        bitStates = new byte[ed.bitWidth];
        simplifiedExprs = new LinkedList<EncodingField>();
        encodingNumber = encNum;
        encoding = ed;

        initializeBitStates();
    }

    EncodingInst(EncodingInst prev) {
        instr = prev.instr;
        addrMode = prev.addrMode;
        bitStates = new byte[prev.bitStates.length];
        simplifiedExprs = prev.simplifiedExprs;
        encodingNumber = prev.encodingNumber;
        encoding = prev.encoding;
        System.arraycopy(prev.bitStates, 0, bitStates, 0, bitStates.length);
    }

    public String toString() {
        return instr.name + " x "+addrMode.name;
    }

    private void initializeBitStates() {
        EncodingDecl ed = encoding;
        // create a constant propagator needed to evaluate integer literals and operands
        ConstantPropagator cp = new ConstantPropagator();
        ConstantPropagator.Environ ce = cp.createEnvironment();

        List<EncodingDecl.BitField> fields = DGUtil.initConstantEnviron(ce, instr, addrMode, ed);

        // scan through the expressions corresponding to the fields that make up this encoding
        // and initialize the bitState array to either ENC_ONE, ENC_ZERO, or ENC_VAR

        int offset = 0;
        for ( EncodingDecl.BitField e : fields ) {
            // get the bit width of the parent encoding field
            int size = e.getWidth();

            int endbit = offset + size - 1;
            if ( (offset / DisassemblerGenerator.WORD_SIZE) != (endbit / DisassemblerGenerator.WORD_SIZE) ) {
                // this field spans a word boundary; we will need to split it up
                splitOnWordBoundary(offset, size, endbit, e, cp, ce);
            } else {
                // evaluate the parent encoding expression, given values for operands
                Expr simpleExpr = e.field.accept(cp,ce);

                addExpr(offset, size, simpleExpr);
            }

            offset += size;
        }
    }

    private void splitOnWordBoundary(int offset, int size, int endbit, EncodingDecl.BitField e, ConstantPropagator cp, ConstantPropagator.Environ ce) {
        int f_offset = offset;
        int h_bit = size - 1;
        while ( f_offset < endbit ) {
            int bits = DisassemblerGenerator.WORD_SIZE - (f_offset % DisassemblerGenerator.WORD_SIZE);
            if ( bits > DisassemblerGenerator.WORD_SIZE ) bits = DisassemblerGenerator.WORD_SIZE;
            // evaluate the expression with a smaller bit interval
            Expr simpleExpr = eval(e.field, cp, ce, h_bit, h_bit-bits+1);
            addExpr(f_offset, bits, simpleExpr);

            f_offset += bits;
            h_bit -= bits;
        }
    }

    private void addExpr(int offset, int size, Expr simpleExpr) {
        // store the expression for future use
        EncodingField ee = new EncodingField(this, offset, size, simpleExpr);
        simplifiedExprs.add(ee);

        setBitStates(simpleExpr, size, offset);
    }

    Expr eval(Expr e, ConstantPropagator cp, ConstantPropagator.Environ ce, int h_bit, int l_bit) {
        if ( e.isBitRangeExpr() ) {
            BitRangeExpr orig = (BitRangeExpr)e;
            int nmax = h_bit + orig.low_bit;
            if ( orig.high_bit - orig.low_bit < h_bit - l_bit )
                nmax = orig.high_bit;
            int nmin = l_bit + orig.low_bit;
            e = new BitRangeExpr(orig.operand, nmin, nmax);
        }

        return e.accept(cp, ce);
    }

    private void setBitStates(Expr simpleExpr, int size, int offset) {
        // if this field corresponds to an integer literal, initialize each bit to
        // either ENC_ZERO or ENC_ONE
        if ( simpleExpr instanceof Literal.IntExpr ) {
            Literal.IntExpr l = (Literal.IntExpr)simpleExpr;
            for ( int cntr = 0; cntr < size; cntr++) {
                boolean bit = Arithmetic.getBit(l.value, size-cntr-1);
                bitStates[offset++] = bit ? ENC_ONE : ENC_ZERO;
            }
        } else if (simpleExpr instanceof Literal.BoolExpr) {
            // if it is a boolean literal, initialize one bit
            Literal.BoolExpr l = (Literal.BoolExpr)simpleExpr;
            bitStates[offset] = l.value ? ENC_ONE : ENC_ZERO;
        } else {
            // not a known value; initialize each bit to variable
            for ( int cntr = 0; cntr < size; cntr++) {
                bitStates[offset++] = ENC_VAR;
            }
        }
    }

    void print(int indent, Printer p) {
        for ( int cntr = 0; cntr < indent; cntr++ )
            p.print("    ");
        p.print(DGUtil.toString(this));
        p.nextln();
    }

    void printVerbose(int indent, Verbose.Printer p) {
        if ( !p.enabled ) return;
        print(indent, p);
    }

    public int getLength() {
        return bitStates.length;
    }

    public EncodingInst copy() {
        return new EncodingInst(this);
    }

    public boolean isConcrete(int bit) {
        byte bitState = bitStates[bit];
        return bitState == EncodingInst.ENC_ONE || bitState == EncodingInst.ENC_ZERO;
    }
}
