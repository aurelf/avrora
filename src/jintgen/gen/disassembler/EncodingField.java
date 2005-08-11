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

import jintgen.jigir.*;
import jintgen.gen.disassembler.DisassemblerGenerator;
import avrora.util.Util;
import avrora.util.Printer;

/**
 * The <code>EncodingField</code> class represents a single expression within the encoding
 * of an instruction. The field has a length and an offset and is used in the disassembler
 * generator to generate the code to extract the operand from the bits of the instruction.
 *
 * @author Ben L. Titzer
 */
public class EncodingField extends CodeVisitor.Default {
    final EncodingInfo ei;
    final int bitsize;
    final Expr expr;
    final int offset;
    Printer printer;

    EncodingField(EncodingInfo ei, int o, int s, Expr e) {
        this.ei = ei;
        offset = o;
        expr = e;
        bitsize = s;
    }

    void generateDecoder(Printer p) {
        printer = p;
        printer.print("// logical["+offset+":"+(offset+bitsize-1)+"] -> ");
        expr.accept(this);
    }

    public void visit(VarExpr ve) {
        printer.println(ve.variable.toString());
        printer.println(ve.variable+" = ");
        DisassemblerGenerator.generateRead(printer, offset, offset+bitsize-1);
        printer.println(";");
    }

    public void visit(BitExpr bre) {
        if ( !bre.expr.isVariable() ) {
            throw Util.failure("bit range use not invertible: value is not a variable or constant");
        } else if ( !bre.bit.isLiteral() ) {
            throw Util.failure("bit range use not invertible: bit is not a constant");
        }
        VarExpr ve = (VarExpr)bre.expr;
        int bit = ((Literal.IntExpr)bre.bit).value;
        printer.println(ve.variable+"["+bit+"]");
        printer.println(ve.variable+" = Arithmetic.setBit("+ve.variable+", "+bit+", Arithmetic.getBit(word1, "+DisassemblerGenerator.nativeBitOrder(offset)+"));");
    }

    public void visit(BitRangeExpr bre) {
        if ( bre.operand.isVariable() ) {
            VarExpr ve = (VarExpr)bre.operand;
            String vname = ve.variable.image;
            generateBitRangeRead(vname, bre);
        } else if (bre.operand instanceof DotExpr ) {
            DotExpr de = (DotExpr)bre.operand;
            String vname = de.operand+"_"+de.field;
            generateBitRangeRead(vname, bre);
        } else {

            throw Util.failure("bit range use not invertible");
        }
    }

    private void generateBitRangeRead(String vname, BitRangeExpr bre) {
        printer.println(vname+"["+bre.high_bit+":"+bre.low_bit+"]");
        printer.print(vname+" |= ");
        DisassemblerGenerator.generateRead(printer, offset, offset+bitsize-1);
        if ( bre.low_bit > 0)
            printer.println(" << "+bre.low_bit+";");
        else printer.println(";");
    }

    public void visit(Literal.IntExpr e) {
        printer.nextln();
        // do nothing for literals.
    }

    public void visit(Literal.BoolExpr e) {
        printer.nextln();
        // do nothing for literals.
    }

    public void error(Expr e) {
        // this method is called when the expression does not match any of the overridden methods
        throw Util.failure("expression not invertible");
    }
}
