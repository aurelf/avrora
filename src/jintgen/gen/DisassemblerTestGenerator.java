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

package jintgen.gen;

import avrora.util.Util;
import jintgen.jigir.CodeRegion;
import jintgen.isdl.*;
import jintgen.jigir.CodeRegion;
import avrora.util.Printer;
import avrora.util.StringUtil;
import avrora.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Ben L. Titzer
 */
public class DisassemblerTestGenerator{

    Architecture architecture;
    File directory;
    String dname;
    Printer printer;
    HashMap<String, OperandGenerator> operandGenerators;

    public DisassemblerTestGenerator(Architecture a, File dir) {
        architecture = a;
        if ( !dir.isDirectory() )
            Util.userError("must specify directory for testcases");
        dname = dir.getAbsolutePath();
        directory = dir;
        operandGenerators = new HashMap<String, OperandGenerator>();
        operandGenerators.put("immediate", new ImmediateGenerator());
        operandGenerators.put("address", new ImmediateGenerator());
        operandGenerators.put("symbol", new SymbolGenerator());
        operandGenerators.put("relative", new RelativeGenerator());
        operandGenerators.put("word", new WordGenerator());
    }

    public void generate() {
        for ( InstrDecl d : architecture.getInstructions() ) visit(d);
    }

    public void visit(InstrDecl d) {
        if ( d.getSyntax() != null ) {
            new SyntaxGenerator(d).generate();
        } else {
            new SimpleGenerator(d).generate();
        }
    }

    abstract class OperandGenerator {
        abstract void generate(Generator g, InstrDecl decl, OperandTypeDecl od, int op, String[] rep);
        abstract String getSomeMember(OperandTypeDecl decl);
    }

    class SymbolGenerator extends OperandGenerator {
        void generate(Generator g, InstrDecl decl, OperandTypeDecl od, int op, String[] rep) {
            // generate a test case for each possible register value of this operand,
            // substituting representative members for the rest of the operands
            OperandTypeDecl.SymbolSet r = (OperandTypeDecl.SymbolSet)od;
            for ( SymbolMapping.Entry re : r.map.getEntries() ) {
                g.output(op, re.name, rep);
            }
        }
        String getSomeMember(OperandTypeDecl decl) {
            OperandTypeDecl.SymbolSet r = (OperandTypeDecl.SymbolSet)decl;
            SymbolMapping.Entry enc = r.map.getEntries().iterator().next();
            return enc.name;
        }
    }

    class ImmediateGenerator extends OperandGenerator {
        void generate(Generator g, InstrDecl decl, OperandTypeDecl od, int op, String[] rep) {
            OperandTypeDecl.Value imm = (OperandTypeDecl.Value)od;
            HashSet<String> hs = new HashSet<String>();
            outputImm(g, imm.high,  imm, hs, op,rep);
            outputImm(g, imm.low,  imm, hs, op,rep);
            outputImmediate(g, 0xffffffff, hs, imm, op, rep);
            outputImmediate(g, 0xff00ff00, hs, imm, op, rep);
            outputImmediate(g, 0xf0f0f0f0, hs, imm, op, rep);
            outputImmediate(g, 0xcccccccc, hs, imm, op, rep);
            outputImmediate(g, 0xaaaaaaaa, hs, imm, op, rep);
        }

        protected void outputImmediate(Generator g, int value_l, HashSet<String> hs, OperandTypeDecl.Value imm, int cntr, String[] rep) {
            int value_h = value_l;
            for ( int bit = 0; bit < 32; bit++) {
                outputImm(g, value_l, imm, hs, cntr, rep);
                outputImm(g, value_h, imm, hs, cntr, rep);
                value_l = value_l >>> 1;
                value_h = value_h << 1;
            }
        }

        protected void outputImm(Generator g, int val, OperandTypeDecl.Value imm, HashSet<String> hs, int cntr, String[] rep) {
            if ( val <= imm.high && val >= imm.low ) {
                String value = StringUtil.to0xHex(val,2);
                if ( !hs.contains(value)) {
                    g.output(cntr, value, rep);
                    hs.add(value);
                }
            }
        }

        String getSomeMember(OperandTypeDecl decl) {
            OperandTypeDecl.Value imm = (OperandTypeDecl.Value)decl;
            // return the average value
            return StringUtil.to0xHex((imm.low+((imm.high-imm.low)/2)), 2);
        }
    }

    class RelativeGenerator extends OperandGenerator {
        void generate(Generator g, InstrDecl decl, OperandTypeDecl od, int op, String[] rep) {
            OperandTypeDecl.Value imm = (OperandTypeDecl.Value)od;
            int pc = 0;
            // for relative, we will dumbly generate all possibilities.
            for ( int cntr = imm.high; cntr >= imm.low; cntr-- ) {
                g.output(op, ".+"+cntr*2, rep);
                pc += decl.getEncodingSize() / 8;
            }
//            throw Avrora.unimplemented();
        }

        String getSomeMember(OperandTypeDecl decl) {
            return "0x00";
        }
    }

    class WordGenerator extends ImmediateGenerator {
        protected void outputImm(Generator g, int val, OperandTypeDecl.Value imm, HashSet<String> hs, int cntr, String[] rep) {
            if ( val <= imm.high && val >= imm.low ) {
                val = val * 2;
                String value = StringUtil.to0xHex(val,2);
                if ( !hs.contains(value)) {
                    g.output(cntr, value, rep);
                    hs.add(value);
                }
            }
        }

        String getSomeMember(OperandTypeDecl decl) {
            OperandTypeDecl.Value imm = (OperandTypeDecl.Value)decl;
            // return the average value
            int avg = (imm.low+((imm.high-imm.low)/2));
            return StringUtil.to0xHex(avg * 2, 2);
        }
    }

    abstract class Generator {
        InstrDecl decl;
        String name;
        Printer printer;

        Generator(InstrDecl decl) {
            this.name = StringUtil.trimquotes(decl.name.toString());
            this.decl = decl;
            printer = createPrinter(name);
        }

        void generate() {
            if ( decl.getOperands().size() == 0 ) {
                output(0, "", StringUtil.EMPTY_STRING_ARRAY);
            }

            String[] rep = getRepresentatives(decl);

            // for each operand, generate many different test cases, with the rest of
            // the operands set to "representative" values
            int cntr = 0;
            for ( AddressingModeDecl.Operand op : decl.getOperands() ) {
                OperandTypeDecl decl = op.getOperandType();
                OperandGenerator g = getOperandGenerator(decl);
                g.generate(this, this.decl, decl, cntr, rep);
                cntr++;
            }
        }

        abstract void output(int op, String v, String[] rep);
    }

    class SimpleGenerator extends Generator {
        SimpleGenerator(InstrDecl decl) {
            super(decl);
        }

        void output(int op, String v, String[] rep) {
            printer.print(name+" ");
            for ( int cntr = 0; cntr < rep.length; cntr++ ) {
                if ( cntr == op )
                    printer.print(v);
                else
                    printer.print(rep[cntr]);
                if ( cntr != rep.length-1 )
                    printer.print(", ");
            }
            printer.nextln();
        }
    }

    class SyntaxGenerator extends Generator {
        String[] opnames;
        String syntax;

        SyntaxGenerator(InstrDecl decl) {
            super(decl);

            syntax = decl.getSyntax();

            int numops = decl.getOperands().size();
            opnames = new String[numops];
            int cntr = 0;
            for ( AddressingModeDecl.Operand op : decl.getOperands() ) {
                opnames[cntr++] = op.name.image;
            }
        }

        void output(int op, String v, String[] rep) {
            String result = syntax;
            result = result.replaceAll("%"+opnames[op], v);
            for ( int cntr = 0; cntr < opnames.length; cntr++ )
                result = result.replaceAll("%"+opnames[cntr], rep[cntr]);
            printer.println(result);
        }
    }

    private Printer createPrinter(String name) {
        String fname = dname + File.separatorChar +name+".instr";
        Printer p = null;
        try {
            File file = new File(fname);
            p = new Printer(new PrintStream(new FileOutputStream(file)));
        } catch ( IOException e) {
            Util.userError("Cannot create test file", fname);
        }
        return p;
    }

    private String[] getRepresentatives(InstrDecl d) {
        String[] rep = new String[d.getOperands().size()];
        int cntr = 0;
        for ( AddressingModeDecl.Operand op : d.getOperands() ) {
            OperandTypeDecl decl = op.getOperandType();
            OperandGenerator g = getOperandGenerator(decl);
            rep[cntr++] = g.getSomeMember(decl);
        }
        return rep;
    }

    private OperandGenerator getOperandGenerator(OperandTypeDecl decl) {
        throw Util.unimplemented();
/*
        OperandGenerator g = (OperandGenerator)operandGenerators.get(decl.kind.image);
        if ( g == null )
            throw Util.failure("cannot generate representative values for operand of type "+decl.kind.image);
        return g;
*/
    }

}
