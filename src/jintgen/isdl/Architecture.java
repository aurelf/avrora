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

import avrora.util.Util;
import jintgen.jigir.*;
import jintgen.gen.Inliner;
import jintgen.isdl.parser.Token;
import avrora.util.Printer;
import avrora.util.StringUtil;
import avrora.util.Verbose;
import avrora.util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

    protected final HashList<String, SubroutineDecl> subroutineMap;
    protected final HashList<String, InstrDecl> instructionMap;
    protected final HashList<String, OperandTypeDecl> operandMap;
    protected final HashList<String, EncodingDecl> encodingMap;
    protected final HashList<String, AddressingModeDecl> addrMap;
    protected final HashList<String, AddressingModeSetDecl> addrSetMap;

    /**
     * The constructor for the <code>Architecture</code> class creates an instance with the specified
     * name that is empty and ready to receive new instruction declarations, encodings, etc.
     * @param n
     */
    public Architecture(Token n) {
        name = n;

        subroutineMap = new HashList<String, SubroutineDecl>();
        instructionMap = new HashList<String, InstrDecl>();
        operandMap = new HashList<String, OperandTypeDecl>();
        encodingMap = new HashList<String, EncodingDecl>();
        addrMap = new HashList<String, AddressingModeDecl>();
        addrSetMap = new HashList<String, AddressingModeSetDecl>();
    }

    public String getName() {
        return name.image;
    }

    public Iterable<SubroutineDecl> getSubroutines() {
        return subroutineMap;
    }

    public Iterable<InstrDecl> getInstructions() {
        return instructionMap;
    }

    public Iterable<OperandTypeDecl> getOperandTypes() {
        return operandMap;
    }

    public Iterable<EncodingDecl> getEncodings() {
        return encodingMap;
    }

    public Iterable<AddressingModeDecl> getAddressingModes() {
        return addrMap;
    }

    public Iterable<AddressingModeSetDecl> getAddressingModeSets() {
        return addrSetMap;
    }

    public void addSubroutine(SubroutineDecl d) {
        printer.println("loading subroutine " + d.name.image + "...");
        subroutineMap.add(d.name.image, d);
    }

    public void addInstruction(InstrDecl i) {
        printer.println("loading instruction " + i.name.image + "...");
        instructionMap.add(i.name.image, i);
    }

    public void addOperand(OperandTypeDecl d) {
        printer.println("loading operand declaration " + d.name.image + "...");
        operandMap.add(d.name.image, d);
    }

    public void addEncoding(EncodingDecl d) {
        printer.println("loading encoding format " + d.name.image + "...");
        encodingMap.add(d.name.image, d);
    }

    public void addAddressingMode(AddressingModeDecl d) {
        printer.println("loading addressing mode " + d + "...");
        addrMap.add(d.name.image, d);
    }

    public void addAddressingModeSet(AddressingModeSetDecl d) {
        printer.println("loading addressing mode set " + d + "...");
        addrSetMap.add(d.name.image, d);
    }

    public InstrDecl getInstruction(String name) {
        return instructionMap.get(name);
    }

    public SubroutineDecl getSubroutine(String name) {
        return subroutineMap.get(name);
    }

    public OperandTypeDecl getOperandDecl(String name) {
        return operandMap.get(name);
    }

    public EncodingDecl getEncoding(String name) {
        return encodingMap.get(name);
    }

    public AddressingModeDecl getAddressingMode(String name) {
        return addrMap.get(name);
    }

    public AddressingModeSetDecl getAddressingModeSet(String name) {
        return addrSetMap.get(name);
    }
}
