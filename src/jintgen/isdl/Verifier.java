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
import jintgen.isdl.parser.Token;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

/**
 * The <code>Verifier</code> class performs some consistency checks on an architecture description
 * such as checking for redeclared instructions, mismatched types, ambiguous encodings, etc.
 *
 * @author Ben L. Titzer
 */
public class Verifier {

    public final Architecture arch;
    public final ErrorReporter ERROR;

    Verbose.Printer printer = Verbose.getVerbosePrinter("jintgen.verifier");

    public Verifier(Architecture a) {
        arch = a;
        ERROR = new ErrorReporter();
    }

    public void verify() {
        verifyEncodings();
        verifyOperands();
        verifyAddrModes();
        verifyAddrSets();
        verifySubroutines();
        verifyInstructions();
    }

    private void verifyEncodings() {
        HashMap<String, Token> previous = new HashMap<String, Token>();

        for ( EncodingDecl ed : arch.getEncodings() ) {
            if (printer.enabled) {
                printer.print("processing encoding " + ed.name.image + ' ');
            }

            if ( previous.containsKey(ed.name.image) )
                ERROR.RedefinedEncoding(previous.get(ed.name));

            previous.put(ed.name.image, ed.name);

            verifyEncoding(ed);

            printer.println("-> result: " + ed.getBitWidth() + " bits");
        }
    }

    private void verifyEncoding(EncodingDecl ed) {
        if (ed instanceof EncodingDecl.Derived) {
            EncodingDecl.Derived dd = (EncodingDecl.Derived)ed;
            EncodingDecl parent = arch.getEncoding(dd.pname.image);
            if ( parent == null )
                ERROR.UnresolvedEncodingFormat(dd.pname);
            dd.setParent(parent);
        }
    }

    private void verifyOperands() {
        HashMap<String, Token> previous = new HashMap<String, Token>();
        for ( OperandTypeDecl od : arch.getOperandTypes() ) {
            if ( previous.containsKey(od.name.image) )
                ERROR.RedefinedOperandType(previous.get(od.name));
            previous.put(od.name.image, od.name);

            if ( od.isCompound() ) {
                OperandTypeDecl.Compound cd = (OperandTypeDecl.Compound)od;
                verifyOperands(cd.subOperands);
            } else if ( od.isSymbol() ) {
                OperandTypeDecl.SymbolSet sd = (OperandTypeDecl.SymbolSet)od;
                HashMap<String, Token> symbols = new HashMap<String, Token>();
                HashMap<String, Token> values = new HashMap<String, Token>();
                for ( SymbolMapping.Entry e : sd.map.getEntries() ) {
                    if ( symbols.containsKey(e.ntoken) )
                        ERROR.RedefinedSymbol(symbols.get(e.ntoken.image));
                    if ( values.containsKey(""+e.value) )
                        ERROR.RedefinedValue(values.get(e.vtoken.image));
                    symbols.put(e.ntoken.image, e.ntoken);
                    values.put(""+e.value, e.vtoken);
                }
            }
        }
    }

    private void verifySubroutines() {
        HashMap<String, Token> previous = new HashMap<String, Token>();

        for ( SubroutineDecl sd : arch.getSubroutines() ) {
            printer.print("processing subroutine " + sd.name + ' ');

            if ( previous.containsKey(sd.name.image) )
                ERROR.RedefinedSubroutine(previous.get(sd.name));

            previous.put(sd.name.image, sd.name);

            // find operand decl
            for ( CodeRegion.Operand od : sd.getOperands() ) {
                OperandTypeDecl opdec = arch.getOperandDecl(od.type.image);
                if (opdec != null)
                    od.setOperandType(opdec);
                // ERROR.UnresolvedType(od.type);
            }

            if (printer.enabled) {
                new PrettyPrinter(arch, printer).visitStmtList(sd.getCode());
            }

        }
    }

    private void verifyAddrModes() {
        HashMap<String, Token> previous = new HashMap<String, Token>();

        for ( AddressingModeDecl am : arch.getAddressingModes() ) {
            if ( previous.containsKey(am.name.image) )
                ERROR.RedefinedAddressingMode(previous.get(am.name));

            previous.put(am.name.image, am.name);
            verifyOperands(am.operands);
        }
    }

    private void verifyOperands(List<CodeRegion.Operand> operands) {
        HashMap<String, Token> previous = new HashMap<String, Token>();
        for ( CodeRegion.Operand o : operands ) {
            if ( previous.containsKey(o.name.image) )
                ERROR.RedefinedEncoding(previous.get(o.name));

            previous.put(o.name.image, o.name);

            String tname = o.getType();
            OperandTypeDecl td = arch.getOperandDecl(tname);
            if ( td == null )
                ERROR.UnresolvedOperandType(o.type);
            o.setOperandType(td);
        }
    }

    private void verifyAddrSets() {
        HashMap<String, Token> previous = new HashMap<String, Token>();

        for ( AddressingModeSetDecl as: arch.getAddressingModeSets() ) {
            if ( previous.containsKey(as.name.image) )
                ERROR.RedefinedAddressingModeSet(previous.get(as.name));

            previous.put(as.name.image, as.name);

            for ( Token t : as.list ) {
                AddressingModeDecl d = arch.getAddressingMode(t.image);
                if ( d == null )
                    ERROR.UnresolvedAddressingMode(t);
            }
        }
    }

    private void verifyInstructions() {
        HashMap<String, Token> previous = new HashMap<String, Token>();
        for ( InstrDecl id : arch.getInstructions() ) {
            printer.print("processing instruction " + id.name + ' ');
            if ( previous.containsKey(id.name.image) )
                ERROR.RedefinedAddressingMode(previous.get(id.name));

            previous.put(id.name.image, id.name);

            optimizeCode(id);
            verifyAddressingMode(id);
            verifyEncodings(id);
            verifyOperands(id.getOperands());

            if (printer.enabled) {
                new PrettyPrinter(arch, printer).visitStmtList(id.getCode());
            }

        }
    }

    private void verifyAddressingMode(InstrDecl id) {
        AddrModeUse am = id.addrMode;
        if ( am.decl == null ) {
            Object decl = arch.getAddressingModeSet(am.name.image);
            if ( decl == null )
                decl = arch.getAddressingMode(am.name.image);
            if ( decl == null )
                ERROR.UnresolvedAddressingMode(am.name);
            // TODO: set the reference to the addressing mode set
            //am.decl = decl;
        }
    }

    private void optimizeCode(InstrDecl id) {
        // inline and optimize the body of the instruction
        List<Stmt> code = id.getCode();
        code = new Inliner(arch).process(code);

        id.setCode(code);
    }

    private void verifyEncodings(InstrDecl id) {
        // for each of the declared encodings, find the parent and verify the size
        for ( EncodingDecl encoding : id.getEncodings() ) {
            verifyEncoding(encoding);

            int encodingSize = encoding.getBitWidth();
            if (encodingSize <= 0 || encodingSize % 16 != 0)
                throw Util.failure("encoding not word aligned: " + id.name.image + " is " + encodingSize + " bits");
            id.setEncodingSize(encodingSize);
        }
    }

}
