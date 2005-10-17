/**
 * Copyright (c) 2004-2005, Regents of the University of California
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
import jintgen.isdl.parser.Token;
import jintgen.jigir.JIGIRTypeEnv;
import jintgen.types.TypeRef;

import java.util.*;

/**
 * The <code>Architecture</code> class represents a collection of instructions, encodings, operands, and
 * subroutines that describe an instruction set architecture.
 *
 * @author Ben L. Titzer
 */
public class Architecture {

    public static boolean INLINE = true;

    Verbose.Printer printer = Verbose.getVerbosePrinter("isdl");

    public final Token name;

    public final HashMap<String, SubroutineDecl> subroutines;
    public final HashMap<String, InstrDecl> instructions;
    public final HashMap<String, OperandTypeDecl> operandTypes;
    public final HashMap<String, FormatDecl> formats;
    public final HashMap<String, AddrModeDecl> addrModes;
    public final HashMap<String, AddrModeSetDecl> addrSets;
    public final HashMap<String, EnumDecl> enums;
    public final HashMap<String, TypeRef> globals;
    public final List<Item> userTypes;

    public final JIGIRTypeEnv typeEnv;
    public final JIGIRErrorReporter ERROR;

    /**
     * The constructor for the <code>Architecture</code> class creates an instance with the specified
     * name that is empty and ready to receive new instruction declarations, encodings, etc.
     * @param n
     */
    public Architecture(Token n) {
        name = n;

        subroutines = new LinkedHashMap<String, SubroutineDecl>();
        instructions = new LinkedHashMap<String, InstrDecl>();
        operandTypes = new LinkedHashMap<String, OperandTypeDecl>();
        formats = new LinkedHashMap<String, FormatDecl>();
        addrModes = new LinkedHashMap<String, AddrModeDecl>();
        addrSets = new LinkedHashMap<String, AddrModeSetDecl>();
        enums = new LinkedHashMap<String, EnumDecl>();
        globals = new LinkedHashMap<String, TypeRef>();
        userTypes = new LinkedList<Item>();
        ERROR = new JIGIRErrorReporter();

        typeEnv = new JIGIRTypeEnv(ERROR);
    }

    public String getName() {
        return name.image;
    }

    public void addSubroutine(SubroutineDecl d) {
        printer.println("loading subroutine " + d.name.image + "...");
        subroutines.put(d.name.image, d);
    }

    public void addInstruction(InstrDecl i) {
        printer.println("loading instruction " + i.name.image + "...");
        instructions.put(i.name.image, i);
    }

    public void addOperand(OperandTypeDecl d) {
        printer.println("loading operand declaration " + d.name.image + "...");
        userTypes.add(d);
        operandTypes.put(d.name.image, d);
    }

    public void addEncoding(FormatDecl d) {
        printer.println("loading encoding format " + d.name.image + "...");
        formats.put(d.name.image, d);
    }

    public void addAddressingMode(AddrModeDecl d) {
        printer.println("loading addressing mode " + d + "...");
        addrModes.put(d.name.image, d);
    }

    public void addAddressingModeSet(AddrModeSetDecl d) {
        printer.println("loading addressing mode set " + d + "...");
        addrSets.put(d.name.image, d);
    }

    public void addEnum(EnumDecl m) {
        printer.println("loading enum " + m + "...");
        userTypes.add(m);
        enums.put(m.name.image, m);
    }

    public void addGlobal(Token n, TypeRef t) {
        globals.put(n.image, t);
    }

    public InstrDecl getInstruction(String name) {
        return instructions.get(name);
    }

    public SubroutineDecl getSubroutine(String name) {
        return subroutines.get(name);
    }

    public OperandTypeDecl getOperandDecl(String name) {
        return operandTypes.get(name);
    }

    public FormatDecl getEncoding(String name) {
        return formats.get(name);
    }

    public AddrModeDecl getAddressingMode(String name) {
        return addrModes.get(name);
    }

    public AddrModeSetDecl getAddressingModeSet(String name) {
        return addrSets.get(name);
    }

    public EnumDecl getEnum(String name) {
        return enums.get(name);
    }
}
