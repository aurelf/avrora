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
import jintgen.types.TypeRef;
import jintgen.types.TypeCon;
import jintgen.types.Type;

import java.util.*;

/**
 * The <code>Verifier</code> class performs some consistency checks on an architecture description
 * such as checking for redeclared instructions, mismatched types, ambiguous encodings, etc.
 *
 * @author Ben L. Titzer
 */
public class Verifier {

    public final Architecture arch;
    public final JIGIRErrorReporter ERROR;

    Verbose.Printer printer = Verbose.getVerbosePrinter("jintgen.verifier");

    public Verifier(Architecture a) {
        arch = a;
        ERROR = arch.ERROR;
    }

    public void verify() {
        verifyUniqueness();
        verifyEnums();
        verifyEncodings();
        verifyOperands();
        verifyAddrModes();
        verifyAddrSets();
        verifySubroutines();
        verifyInstructions();
        typeCheck();
    }

    private void verifyUniqueness() {
        uniqueCheck("UserType", "User type", arch.userTypes);
        uniqueCheck("Format", "Encoding format", arch.formats);
        uniqueCheck("Subroutine", "Subroutine", arch.subroutines);
        uniqueCheck("AddrMode", "Addressing mode", arch.addrModes);
        uniqueCheck("AddrModeSet", "AddressingModeSet", arch.addrSets);
        uniqueCheck("Instr", "Instruction", arch.instructions);
    }

    private void typeCheck() {
        TypeChecker tc = new TypeChecker(ERROR, arch.typeEnv);
        Environment env = new Environment(arch);

        typeCheckAccessMethods(env, tc);
        typeCheckSubroutines(env, tc);
        typeCheckInstrBodies(env, tc);
    }

    private void typeCheckAccessMethods(Environment env, TypeChecker tc ) {
        // first step: resolve all read methods by type
        JIGIRTypeEnv te = arch.typeEnv;
        for ( OperandTypeDecl ot: arch.operandTypes ) {
            for ( OperandTypeDecl.AccessMethod m : ot.readDecls )
                ot.readAccessors.put(resolveAccessMethodType(m, te), m);
            for ( OperandTypeDecl.AccessMethod m : ot.writeDecls )
                ot.writeAccessors.put(resolveAccessMethodType(m, te), m);
        }
        // now typecheck the bodies
        for ( OperandTypeDecl ot: arch.operandTypes ) {
            // add each of the sub operands to the environment
            Environment oenv = new Environment(env);
            oenv.addVariable("this", operandRepresentationType(ot));
            for ( AddrModeDecl.Operand o : ot.subOperands )
                oenv.addVariable(o.name.image, arch.typeEnv.resolveType(o.type));

            for ( OperandTypeDecl.AccessMethod m : ot.readDecls ) {
                tc.typeCheck(m.code.getStmts(), resolveAccessMethodType(m, arch.typeEnv), new Environment(oenv));
            }
            for ( OperandTypeDecl.AccessMethod m : ot.writeDecls ) {
                Environment senv = new Environment(oenv);
                senv.addVariable("value", resolveAccessMethodType(m, arch.typeEnv));
                tc.typeCheck(m.code.getStmts(), null, senv);
            }
        }

        // build the accessor methods for unions
        for ( AddrModeSetDecl as : arch.addrSets ) {
            for ( AddrModeDecl.Operand o : as.unionOperands )
                computeAccessorUnion((OperandTypeDecl.Union)o.operandType);
        }

    }

    private Type operandRepresentationType(OperandTypeDecl ot) {
        if ( ot.isValue() ) {
            TypeRef kind = ((OperandTypeDecl.Value)ot).kind;
            return kind.resolve(arch.typeEnv);
        } else
            return arch.typeEnv.resolveOperandType(ot);
    }

    private Type resolveAccessMethodType(OperandTypeDecl.AccessMethod m, JIGIRTypeEnv te) {
        Type type = m.typeRef.resolve(te);
        m.type = type;
        return type;
    }

    private void typeCheckInstrBodies(Environment env, TypeChecker tc) {
        for ( InstrDecl d : arch.instructions ) {
            if ( !d.code.hasBody() ) continue;
            Environment senv = new Environment(env);
            for ( AddrModeDecl.Operand p : d.getOperands() ) {
                senv.addVariable(p.name.image, new TypeRef(p.type).resolve(arch.typeEnv));
            }
            tc.typeCheck(d.code.getStmts(), null, senv);
        }
    }

    private void typeCheckSubroutines(Environment env, TypeChecker tc) {
        for ( SubroutineDecl d : arch.subroutines ) {
            if ( !d.code.hasBody() ) continue;
            Environment senv = new Environment(env);
            for ( SubroutineDecl.Parameter p : d.getParams() ) {
                senv.addVariable(p.name.image, p.type.resolve(arch.typeEnv));
            }
            Type t = d.ret.resolve(arch.typeEnv);
            tc.typeCheck(d.code.getStmts(), t, senv);
        }
    }

    private void verifyEnums() {
        for ( EnumDecl ed : arch.enums )
            arch.typeEnv.addEnum(ed);

        for ( EnumDecl ed : arch.enums ) {
            if (printer.enabled) {
                printer.print("verifying enum " + ed.name.image + ' ');
            }
            verifyEnum(ed);
        }
    }

    private void verifyEnum(EnumDecl ed) {
        if (ed instanceof EnumDecl.Subset) {
            EnumDecl.Subset dd = (EnumDecl.Subset)ed;
            EnumDecl parent = arch.getEnum(dd.ptype.getTypeConName());
            if ( parent == null )
                ERROR.UnresolvedEnum(dd.ptype.getToken());
            dd.setParent(parent);
            TypeCon tc = arch.typeEnv.resolveTypeCon(ed.name.image);
            TypeCon ptc = arch.typeEnv.resolveTypeCon(parent.name.image);
            arch.typeEnv.ASSIGNABLE.add(tc, ptc);
            arch.typeEnv.COMPARABLE.add(tc, ptc);
            arch.typeEnv.COMPARABLE.add(ptc, tc);
        }
    }

    private void verifyEncodings() {

        for ( FormatDecl ed : arch.formats ) {
            if (printer.enabled) {
                printer.print("verifying encoding " + ed.name.image + ' ');
            }

            // TODO: what checks should be done for encodings?
            printer.println("-> result: " + ed.getBitWidth() + " bits");
        }
    }

    private void verifyOperands() {
        for ( OperandTypeDecl od : arch.operandTypes ) {
            arch.typeEnv.addOperandType(od);
            if ( od.isCompound() ) {
                OperandTypeDecl.Compound cd = (OperandTypeDecl.Compound)od;
                verifyOperands(cd.subOperands);
            } else {
                OperandTypeDecl.Value vd = (OperandTypeDecl.Value)od;
                TypeCon tc = vd.kind.resolveTypeCon(arch.typeEnv);
                if ( tc == null ) ERROR.UnresolvedType(vd.kind.getToken());
                if ( tc instanceof JIGIRTypeEnv.TYPE_operand )
                    ERROR.ValueTypeExpected(vd.kind);
            }
        }
    }

    private void verifySubroutines() {

        for ( SubroutineDecl sd : arch.subroutines ) {
            printer.print("verifying subroutine " + sd.name + ' ');

            if (printer.enabled) {
                new PrettyPrinter(arch, printer).visitStmtList(sd.code.getStmts());
            }
        }
    }

    private void verifyAddrModes() {

        for ( AddrModeDecl am : arch.addrModes ) {
            printer.println("verifying addressing mode " + am.name + ' ');
            verifyOperands(am.operands);
            verifyEncodings(am.encodings, am);
        }
    }

    private void verifyOperands(List<AddrModeDecl.Operand> operands) {
        HashMap<String, Token> set = new HashMap<String, Token>();
        for ( AddrModeDecl.Operand o : operands ) {
            if ( o.getOperandType() == null ) {
                // if the operand type has not been resolved yet
                String tname = o.type.image;
                if ( set.containsKey(o.name.image) )
                    ERROR.RedefinedOperand(o.name, set.get(o.name.image));
                set.put(o.name.image, o.name);
                OperandTypeDecl td = arch.getOperandDecl(tname);
                if ( td == null )
                    ERROR.UnresolvedOperandType(o.type);
                o.setOperandType(td);
            }
        }
    }

    private void verifyAddrSets() {

        for ( AddrModeSetDecl as: arch.addrSets ) {
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

    private void computeAccessorUnion(OperandTypeDecl.Union union) {
        int goal = union.types.size();
        HashMap<Type, HashSet<OperandTypeDecl.AccessMethod>> rmeths = new HashMap<Type, HashSet<OperandTypeDecl.AccessMethod>>();
        HashMap<Type, HashSet<OperandTypeDecl.AccessMethod>> wmeths = new HashMap<Type, HashSet<OperandTypeDecl.AccessMethod>>();
        for ( OperandTypeDecl ot : union.types ) {
            addAccessorMethods(ot.readDecls, rmeths);
            addAccessorMethods(ot.writeDecls, wmeths);
        }
        addAccessors(rmeths, union.readAccessors, goal);
        addAccessors(wmeths, union.writeAccessors, goal);
    }

    private void addAccessors(HashMap<Type, HashSet<OperandTypeDecl.AccessMethod>> rmeths, HashMap<Type, OperandTypeDecl.Accessor> accessors, int goal) {
        for ( Map.Entry<Type, HashSet<OperandTypeDecl.AccessMethod>> e : rmeths.entrySet() ) {
            if (e.getValue().size() == goal) {
                // only unify "complete" accessor method sets
                OperandTypeDecl.PolymorphicAccessor polymorphicAccessor = new OperandTypeDecl.PolymorphicAccessor(e.getKey());
                for ( OperandTypeDecl.AccessMethod m : e.getValue() )
                    polymorphicAccessor.addTarget(m);
                accessors.put(e.getKey(), polymorphicAccessor);
            }
        }
    }

    private void addAccessorMethods(List<OperandTypeDecl.AccessMethod> entries, HashMap<Type, HashSet<OperandTypeDecl.AccessMethod>> rmeths) {
        for ( OperandTypeDecl.AccessMethod m : entries ) {
            Type key = m.type;
            HashSet<OperandTypeDecl.AccessMethod> set = rmeths.get(key);
            if ( set == null ) {
                set = new HashSet<OperandTypeDecl.AccessMethod>();
                rmeths.put(key, set);
            }
            set.add(m);
        }
    }

    private void unifyAddressingMode(HashMap<String, OperandTypeDecl.Union> unions, AddrModeDecl am, AddrModeSetDecl as, HashSet<String> alloperands, Token t) {
        if ( as.addrModes.size() == 1 ) {
            // for the first addressing mode, put the union types in the map
            for ( AddrModeDecl.Operand o : am.operands ) {
                Token tok = new Token();
                tok.image = as.name.image+"_"+o.name.image+"_union";
                OperandTypeDecl.Union ut = new OperandTypeDecl.Union(tok);
                OperandTypeDecl d = arch.getOperandDecl(o.type.image);
                ut.addType(d);
                unions.put(o.name.image, ut);
                alloperands.add(o.name.image);
                arch.typeEnv.addOperandType(ut);
            }
        } else {
            // for each operand in this addressing mode, check that it exists and add
            // it to the types to be unified.
            HashSet<String> operands = new HashSet<String>();
            for ( AddrModeDecl.Operand o : am.operands ) {
                OperandTypeDecl.Union ut = unions.get(o.name.image);
                if ( ut == null )
                    ERROR.ExtraOperandInAddrModeUnification(as.name, t, o.name);

                OperandTypeDecl d = arch.getOperandDecl(o.type.image);
                ut.addType(d);
                operands.add(o.name.image);
            }
            if ( !operands.containsAll(alloperands) ) {
                alloperands.removeAll(operands);
                String oneop = alloperands.iterator().next();
                ERROR.MissingOperandInAddrModeUnification(as.name, t, oneop);
            }
        }
    }

    private void verifyInstructions() {
        for ( InstrDecl id : arch.instructions ) {
            printer.print("verifying instruction " + id.name + ' ');
            // verify the addressing mode use
            verifyAddrModeUse(id.addrMode);
            //optimizeCode(id);

            if (printer.enabled) {
                new PrettyPrinter(arch, printer).visitStmtList(id.code.getStmts());
            }

        }
    }

    private void verifyAddrModeUse(AddrModeUse am) {
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

    private void verifyEncodings(Iterable<FormatDecl> el, AddrModeDecl am) {
        // for each of the declared encodings, find the parent and verify the size
        for ( FormatDecl encoding : el ) {
            verifyEncoding(encoding, am);
        }
    }

    private void verifyEncoding(FormatDecl ed, AddrModeDecl am) {
        if (ed instanceof FormatDecl.Derived) {
            FormatDecl.Derived dd = (FormatDecl.Derived)ed;
            FormatDecl parent = arch.getEncoding(dd.pname.image);
            if ( parent == null )
                ERROR.UnresolvedFormat(dd.pname);
            dd.setParent(parent);
        }

        int encodingSize = computeEncodingSize(ed, am);
        if (encodingSize <= 0 || encodingSize % 16 != 0)
            throw Util.failure("encoding not word aligned: " + ed.name + " is " + encodingSize + " bits, at:  "
                    +ed.name.beginLine+":"+ed.name.beginColumn);
    }

    private int computeEncodingSize(FormatDecl encoding, AddrModeDecl am) {
        BitWidthComputer bwc = new BitWidthComputer(am);
        int size = 0;
        List<FormatDecl.BitField> fields;
        if ( encoding instanceof FormatDecl.Derived ) {
            FormatDecl.Derived ed = (FormatDecl.Derived)encoding;
            for ( FormatDecl.Substitution s : ed.subst ) {
                bwc.addSubstitution(s.name.image, s.expr);
            }
            fields = ed.parent.fields;
        } else {
            fields = encoding.fields;
        }
        for ( FormatDecl.BitField e : fields ) {
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

        public void visit(FixedRangeExpr e) {
            int diff = (e.high_bit - e.low_bit);
            if (diff < 0) diff = -diff;
            width = diff + 1;
        }

        public void visit(IndexExpr e) {
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

    private void uniqueCheck(String type, String name, Iterable<? extends Item> it) {
        HashMap<String, Token> items = new HashMap<String, Token>();
        for ( Item i : it ) {
            String nm = i.name.image;
            if ( items.containsKey(nm) )
                ERROR.redefined(type, name, items.get(nm), i.name);
            items.put(nm, i.name);
        }
    }
}
