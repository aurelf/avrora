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

package jintgen.isdl;

import avrora.util.Verbose;
import avrora.util.Util;
import avrora.util.StringUtil;
import jintgen.jigir.CodeRegion;
import jintgen.jigir.Stmt;
import jintgen.gen.Inliner;

import java.util.List;
import java.util.Iterator;

/**
 * @author Ben L. Titzer
 */
public class Verifier {

    public final Architecture arch;

    Verbose.Printer printer = Verbose.getVerbosePrinter("jintgen.verifier");

    public Verifier(Architecture a) {
        arch = a;
    }

    public void verify() {
        verifyEncodings();
        verifySubroutines();
        verifyInstructions();
    }

    private void verifyEncodings() {
        for ( EncodingDecl ed : arch.getEncodings() ) {
            if (printer.enabled) {
                printer.print("processing encoding " + ed.name.image + ' ');
            }

            if (ed instanceof EncodingDecl.Derived) {
                EncodingDecl.Derived dd = (EncodingDecl.Derived)ed;
                EncodingDecl parent = arch.getEncoding(dd.pname.image);
                dd.setParent(parent);
            }

            printer.println("-> result: " + ed.getBitWidth() + " bits");
        }
    }

    private void verifySubroutines() {
        for ( SubroutineDecl sd : arch.getSubroutines() ) {
            printer.print("processing subroutine " + sd.name + ' ');

            // find operand decl
            for ( CodeRegion.Operand od : sd.getOperands() ) {
                OperandTypeDecl opdec = arch.getOperandDecl(od.type.image);
                if (opdec != null)
                    od.setOperandType(opdec);
            }

            if (printer.enabled) {
                new PrettyPrinter(arch, printer).visitStmtList(sd.getCode());
            }

        }
    }

    private void verifyInstructions() {
        for ( InstrDecl id : arch.getInstructions() ) {
            printer.print("processing instruction " + id.name + ' ');

            optimizeCode(id);
            verifyEncodings(id);
            verifyOperandTypes(id);
            verifyTiming(id);

            if (printer.enabled) {
                new PrettyPrinter(arch, printer).visitStmtList(id.getCode());
            }

        }
    }

    private void verifyTiming(InstrDecl id) {
        // check that cycles make sense
        if (id.getCycles() < 0)
            throw Util.failure("instruction " + id.name.image + " has negative cycle count");
    }

    private void optimizeCode(InstrDecl id) {
        // inline and optimize the body of the instruction
        List<Stmt> code = id.getCode();
        code = new Inliner(arch).process(code);

        id.setCode(code);
    }

    private void verifyOperandTypes(InstrDecl id) {
        // find operand decl
        for ( CodeRegion.Operand od : id.getOperands() ) {
            OperandTypeDecl opdec = arch.getOperandDecl(od.type.image);
            if (opdec == null)
                throw Util.failure("operand type undefined " + StringUtil.quote(od.type.image));
            od.setOperandType(opdec);
        }
    }

    private void verifyEncodings(InstrDecl id) {
        // for each of the declared encodings, find the parent and verify the size
        for ( EncodingDecl encoding : id.getEncodings() ) {
            if (encoding instanceof EncodingDecl.Derived) {
                // find parent encoding
                EncodingDecl.Derived dd = (EncodingDecl.Derived)encoding;
                EncodingDecl parent = arch.getEncoding(dd.pname.image);
                dd.setParent(parent);
            }

            int encodingSize = encoding.getBitWidth();
            if (encodingSize <= 0 || encodingSize % 16 != 0)
                throw Util.failure("encoding not word aligned: " + id.name.image + " is " + encodingSize + " bits");
            id.setEncodingSize(encodingSize);
        }
    }

}
