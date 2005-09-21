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

import cck.text.Verbose;
import cck.util.Util;
import jintgen.gen.Inliner;
import jintgen.isdl.parser.Token;
import jintgen.jigir.*;
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
        verifyEnums();
        verifyEncodings();
        verifyOperands();
        verifyAddrModes();
        verifyAddrSets();
        verifySubroutines();
        verifyInstructions();
    }

    private void verifyEnums() {
        HashMap<String, Token> previous = new HashMap<String, Token>();

        for ( EnumDecl ed : arch.enums ) {
            if (printer.enabled) {
                printer.print("verifying enum " + ed.name.image + ' ');
            }

            if ( previous.containsKey(ed.name.image) )
                ERROR.RedefinedEnum(previous.get(ed.name));

            previous.put(ed.name.image, ed.name);

            verifyEnum(ed);
        }
    }

    private void verifyEnum(EnumDecl ed) {
        if (ed instanceof EnumDecl.Subset) {
            EnumDecl.Subset dd = (EnumDecl.Subset)ed;
            EnumDecl parent = arch.getEnum(dd.ptype.getBaseName());
            if ( parent == null )
                ERROR.UnresolvedEnum(dd.ptype.getBaseType());
            dd.setParent(parent);
        }
    }

    private void verifyEncodings() {
        HashMap<String, Token> previous = new HashMap<String, Token>();

        for ( EncodingDecl ed : arch.encodings ) {
            if (printer.enabled) {
                printer.print("verifying encoding " + ed.name.image + ' ');
            }

            if ( previous.containsKey(ed.name.image) )
                ERROR.RedefinedEncoding(previous.get(ed.name));

            previous.put(ed.name.image, ed.name);

            printer.println("-> result: " + ed.getBitWidth() + " bits");
        }
    }

    private void verifyOperands() {
        HashMap<String, Token> previous = new HashMap<String, Token>();
        for ( OperandTypeDecl od : arch.operandTypes ) {
            if ( previous.containsKey(od.name.image) )
                ERROR.RedefinedOperandType(previous.get(od.name));
            previous.put(od.name.image, od.name);

            if ( od.isCompound() ) {
                OperandTypeDecl.Compound cd = (OperandTypeDecl.Compound)od;
                verifyOperands(cd.subOperands);
            }
            /*else if ( od.isSymbol() ) {
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
            }*/
        }
    }

    private void verifySubroutines() {
        HashMap<String, Token> previous = new HashMap<String, Token>();

        for ( SubroutineDecl sd : arch.subroutines ) {
            printer.print("verifying subroutine " + sd.name + ' ');

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

        for ( AddrModeDecl am : arch.addrModes ) {
            printer.println("verifying addressing mode " + am.name + ' ');
            if ( previous.containsKey(am.name.image) )
                ERROR.RedefinedAddressingMode(previous.get(am.name));

            previous.put(am.name.image, am.name);
            verifyOperands(am.operands);
            verifyEncodings(am.encodings, am);
        }
    }

    private void verifyOperands(List<AddrModeDecl.Operand> operands) {
        for ( AddrModeDecl.Operand o : operands ) {
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

        for ( AddrModeSetDecl as: arch.addrSets ) {
            if ( previous.containsKey(as.name.image) )
                ERROR.RedefinedAddressingModeSet(previous.get(as.name));

            previous.put(as.name.image, as.name);

            HashMap<String, OperandTypeDecl.Union> unions = new HashMap<String, OperandTypeDecl.Union>();
            HashSet<String> alloperands = new HashSet<String>();
            for ( Token t : as.list ) {
                AddrModeDecl am = arch.getAddressingMode(t.image);
                if ( am == null )
                    ERROR.UnresolvedAddressingMode(t);
                as.addrModes.add(am);
                am.joinSet(as);
                unifyAddressingMode(unions, am, as, alloperands, t);
            }

            buildOperandList(unions, as);
        }
    }

    private void buildOperandList(HashMap<String, OperandTypeDecl.Union> unions, AddrModeSetDecl as) {
        // now that we verified the unification of the operands, create a list of operands for
        // this addressing mode with names and union types
        List<AddrModeDecl.Operand> operands = new LinkedList<AddrModeDecl.Operand>();
        for ( Map.Entry<String, OperandTypeDecl.Union> e : unions.entrySet() ) {
            Token n = new Token();
            n.image = e.getKey();
            OperandTypeDecl.Union unionType = e.getValue();
            AddrModeDecl.Operand operand = new AddrModeDecl.Operand(n, unionType.name);
            operand.setOperandType(unionType);
            operands.add(operand);
        }

        as.unionOperands = operands;
    }

    private void unifyAddressingMode(HashMap<String, OperandTypeDecl.Union> unions, AddrModeDecl am, AddrModeSetDecl as, HashSet<String> alloperands, Token t) {
        if ( unions.size() == 0 ) {
            // for the first addressing mode, put the union types in the map
            for ( AddrModeDecl.Operand o : am.operands ) {
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
            for ( AddrModeDecl.Operand o : am.operands ) {
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
        for ( InstrDecl id : arch.instructions ) {
            printer.print("verifying instruction " + id.name + ' ');
            if ( previous.containsKey(id.name.image) )
                ERROR.RedefinedAddressingMode(previous.get(id.name));

            previous.put(id.name.image, id.name);

            verifyAddressingMode(id);
            optimizeCode(id);

            if (printer.enabled) {
                new PrettyPrinter(arch, printer).visitStmtList(id.code.getStmts());
            }

        }
    }

    private void verifyAddressingMode(InstrDecl id) {
        AddrModeUse am = id.addrMode;
        if ( am.ref != null ) {
            resolveAddressingModeRef(am);
        } else {
            verifyOperands(am.localDecl.operands);
            verifyEncodings(am.localDecl.encodings, am.localDecl);
        }
    }

    private void resolveAddressingModeRef(AddrModeUse am) {
        AddrModeSetDecl asd = arch.getAddressingModeSet(am.ref.image);
        if ( asd == null ) {
            AddrModeDecl amd = arch.getAddressingMode(am.ref.image);
            if ( amd == null ) {
                ERROR.UnresolvedAddressingMode(am.ref);
            } else {
                am.operands = amd.operands;
                am.addrModes = new LinkedList<AddrModeDecl>();
                am.addrModes.add(amd);
            }
        } else {
            am.operands = asd.unionOperands;
            am.addrModes = asd.addrModes;
        }
    }

    private void optimizeCode(InstrDecl id) {
        // inline and optimize the body of the instruction
        List<Stmt> code = id.code.getStmts();
        code = new Inliner(arch).process(code);

        id.code.setStmts(code);
    }

    private void verifyEncodings(Iterable<EncodingDecl> el, AddrModeDecl am) {
        // for each of the declared encodings, find the parent and verify the size
        for ( EncodingDecl encoding : el ) {
            verifyEncoding(encoding, am);
        }
    }

    private void verifyEncoding(EncodingDecl ed, AddrModeDecl am) {
        if (ed instanceof EncodingDecl.Derived) {
            EncodingDecl.Derived dd = (EncodingDecl.Derived)ed;
            EncodingDecl parent = arch.getEncoding(dd.pname.image);
            if ( parent == null )
                ERROR.UnresolvedEncodingFormat(dd.pname);
            dd.setParent(parent);
        }

        int encodingSize = computeEncodingSize(ed, am);
        if (encodingSize <= 0 || encodingSize % 16 != 0)
            throw Util.failure("encoding not word aligned: " + ed.name + " is " + encodingSize + " bits, at:  "
                    +ed.name.beginLine+":"+ed.name.beginColumn);
    }

    private int computeEncodingSize(EncodingDecl encoding, AddrModeDecl am) {
        BitWidthComputer bwc = new BitWidthComputer(am);
        int size = 0;
        List<EncodingDecl.BitField> fields;
        if ( encoding instanceof EncodingDecl.Derived ) {
            EncodingDecl.Derived ed = (EncodingDecl.Derived)encoding;
            for ( EncodingDecl.Substitution s : ed.subst ) {
                bwc.addSubstitution(s.name.image, s.expr);
            }
            fields = ed.parent.fields;
        } else {
            fields = encoding.fields;
        }
        for ( EncodingDecl.BitField e : fields ) {
            e.field.accept(bwc);
            e.width = bwc.width;
            size += bwc.width;
        }
        encoding.bitWidth = size;
        return size;
    }

    class BitWidthComputer extends CodeVisitor.Default {

        int width = -1;
        HashMap<String, Integer> operandWidthMap;
        HashMap<String, Expr> substMap;

        BitWidthComputer(AddrModeDecl d) {
            substMap = new HashMap<String, Expr>();
            operandWidthMap = new HashMap<String, Integer>();
            for ( AddrModeDecl.Operand o : d.operands ) {
                addSubOperands(o, o.name.image);
            }
        }

        void addSubstitution(String str, Expr e) {
            substMap.put(str, e);
        }

        void addSubOperands(AddrModeDecl.Operand op, String prefix) {
            OperandTypeDecl ot = op.getOperandType();
            if ( ot == null ) ERROR.UnresolvedOperandType(op.type);
            if ( ot.isCompound() ) {
                OperandTypeDecl.Compound cd = (OperandTypeDecl.Compound)ot;
                for ( AddrModeDecl.Operand o : cd.subOperands )
                addSubOperands(o, prefix+"."+o.name.image);
            } else if ( ot.isValue() ) {
                OperandTypeDecl.Value sd = (OperandTypeDecl.Value)ot;
                operandWidthMap.put(prefix, sd.size);
            } else if ( ot.isUnion() ) {
                // do nothing
            }
        }

        public void visit(BitRangeExpr e) {
            int diff = (e.high_bit - e.low_bit);
            if (diff < 0) diff = -diff;
            width = diff + 1;
        }

        public void visit(BitExpr e) {
            width = 1;
        }

        public void visit(Literal.IntExpr e) {
            if (e.token.image.charAt(1) == 'b') {
                width = e.token.image.length() - 2;
            } else {
                ERROR.CannotComputeSizeOfLiteral(e);
            }
        }

        public void visit(Literal.BoolExpr e) {
            width = 1;
        }

        public void visit(VarExpr e) {
            Expr se = substMap.get(e.variable.image);
            if ( se != null ) {
                se.accept(this);
                return;
            }

            Integer i = operandWidthMap.get(e.variable.image);
            if ( i != null )
                width = i.intValue();
            else
                ERROR.CannotComputeSizeOfVariable(e.variable);
        }

        public void visit(DotExpr e) {
            String str = e.operand+"."+e.field;
            Integer i = operandWidthMap.get(str);
            if ( i != null )
                width = i.intValue();
            else
                ERROR.CannotComputeSizeOfVariable(e.field);
        }

        public void error(Expr e) {
            ERROR.CannotComputeSizeOfExpression(e);
        }
    }

    private String pos(Token t) {
        return t.beginLine+":"+t.beginColumn;
    }
}
