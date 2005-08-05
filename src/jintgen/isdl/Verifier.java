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

import java.util.*;

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
            for ( SubroutineDecl.Parameter p : sd.getParams() ) {
                // TODO: check the type of each parameter
                // ERROR.UnresolvedType(od.type);
            }

            if (printer.enabled) {
                new PrettyPrinter(arch, printer).visitStmtList(sd.code.getStmts());
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

    private void verifyOperands(List<AddressingModeDecl.Operand> operands) {
        for ( AddressingModeDecl.Operand o : operands ) {
            if ( o.getOperandType() == null ) {
                // if the operand type has not been resolved yet
                String tname = o.type.image;
                OperandTypeDecl td = arch.getOperandDecl(tname);
                if ( td == null )
                    ERROR.UnresolvedOperandType(o.type);
                o.setOperandType(td);
            }
        }
    }

    private void verifyAddrSets() {
        HashMap<String, Token> previous = new HashMap<String, Token>();

        for ( AddressingModeSetDecl as: arch.getAddressingModeSets() ) {
            if ( previous.containsKey(as.name.image) )
                ERROR.RedefinedAddressingModeSet(previous.get(as.name));

            previous.put(as.name.image, as.name);

            HashMap<String, OperandTypeDecl.Union> unions = new HashMap<String, OperandTypeDecl.Union>();
            HashSet<String> alloperands = new HashSet<String>();
            for ( Token t : as.list ) {
                AddressingModeDecl am = arch.getAddressingMode(t.image);
                if ( am == null )
                    ERROR.UnresolvedAddressingMode(t);
                as.addrModes.add(am);
                unifyAddressingMode(unions, am, as, alloperands, t);
            }

            buildOperandList(unions, as);
        }
    }

    private void buildOperandList(HashMap<String, OperandTypeDecl.Union> unions, AddressingModeSetDecl as) {
        // now that we verified the unification of the operands, create a list of operands for
        // this addressing mode with names and union types
        List<AddressingModeDecl.Operand> operands = new LinkedList<AddressingModeDecl.Operand>();
        for ( Map.Entry<String, OperandTypeDecl.Union> e : unions.entrySet() ) {
            Token n = new Token();
            n.image = e.getKey();
            OperandTypeDecl.Union unionType = e.getValue();
            AddressingModeDecl.Operand operand = new AddressingModeDecl.Operand(n, unionType.name);
            operand.setOperandType(unionType);
            operands.add(operand);
        }

        as.unionOperands = operands;
    }

    private void unifyAddressingMode(HashMap<String, OperandTypeDecl.Union> unions, AddressingModeDecl am, AddressingModeSetDecl as, HashSet<String> alloperands, Token t) {
        if ( unions.size() == 0 ) {
            // for the first addressing mode, put the union types in the map
            for ( AddressingModeDecl.Operand o : am.operands ) {
                Token tok = new Token();
                tok.image = as.name.image+"_"+o.name.image+"_union";
                OperandTypeDecl.Union ut = new OperandTypeDecl.Union(tok);
                OperandTypeDecl d = arch.getOperandDecl(o.type.image);
                ut.addType(d);
                unions.put(o.name.image, ut);
                alloperands.add(o.name.image);
            }
        } else {
            // for each operand in this addressing mode, check that it exists and add
            // it to the types to be unified.
            HashSet<String> operands = new HashSet<String>();
            for ( AddressingModeDecl.Operand o : am.operands ) {
                OperandTypeDecl.Union ut = unions.get(o.name.image);
                if ( ut == null )
                    ERROR.ExtraOperandInAddressingModeUnification(as.name, t, o.name);

                OperandTypeDecl d = arch.getOperandDecl(o.type.image);
                ut.addType(d);
                operands.add(o.name.image);
            }
            if ( !operands.containsAll(alloperands) ) {
                alloperands.removeAll(operands);
                String oneop = alloperands.iterator().next();
                ERROR.MissingOperandInAddressingModeUnification(as.name, t, oneop);
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
                new PrettyPrinter(arch, printer).visitStmtList(id.code.getStmts());
            }

        }
    }

    private void verifyAddressingMode(InstrDecl id) {
        AddrModeUse am = id.addrMode;
        if ( am.ref != null ) {
            AddressingModeSetDecl asd = arch.getAddressingModeSet(am.ref.image);
            if ( asd == null ) {
                resolveAddressingMode(am);
            } else {
                am.operands = asd.unionOperands;
                am.addrModes = asd.addrModes;
            }
        }
    }

    private void resolveAddressingMode(AddrModeUse am) {
        AddressingModeDecl amd = arch.getAddressingMode(am.ref.image);
        if ( amd == null ) {
            ERROR.UnresolvedAddressingMode(am.ref);
        } else {
            am.operands = amd.operands;
            am.addrModes = new LinkedList<AddressingModeDecl>();
            am.addrModes.add(amd);
        }
    }

    private void optimizeCode(InstrDecl id) {
        // inline and optimize the body of the instruction
        List<Stmt> code = id.code.getStmts();
        code = new Inliner(arch).process(code);

        id.code.setStmts(code);
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
