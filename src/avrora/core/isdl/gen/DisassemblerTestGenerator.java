package avrora.core.isdl.gen;

import avrora.core.isdl.Architecture;
import avrora.core.isdl.InstrDecl;
import avrora.core.isdl.CodeRegion;
import avrora.core.isdl.OperandDecl;
import avrora.util.Printer;
import avrora.util.StringUtil;
import avrora.Avrora;

import java.util.HashSet;
import java.util.Iterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Ben L. Titzer
 */
public class DisassemblerTestGenerator implements Architecture.InstrVisitor {

    Architecture architecture;
    File directory;
    String dname;
    Printer printer;

    public DisassemblerTestGenerator(Architecture a, File dir) {
        architecture = a;
        if ( !dir.isDirectory() )
            Avrora.userError("must specify directory for testcases");
        dname = dir.getAbsolutePath();
        directory = dir;
    }

    public void generate() {
        architecture.accept(this);
    }

    public void visit(InstrDecl d) {
        if ( d.syntax != null ) {
            new SyntaxGenerator(d).generate();
        } else {
            new SimpleGenerator(d).generate();
        }
    }

    abstract class SourceRep {
        abstract int sourceRep(int pc, int bits);
    }

    class IntegerRep extends SourceRep {
        int sourceRep(int pc, int bits) { return bits; }
    }

    class WordRep extends SourceRep {
        int sourceRep(int pc, int bits) { return bits * 2; }
    }

    class RelativeRep extends SourceRep {
        int sourceRep(int pc, int bits) {
            int address = pc+2 + bits*2;
            if ( address < 0 ) address = 0;
            return address;
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
            Iterator i = decl.getOperandIterator();
            // for each operand, generate many different test cases, with the rest of
            // the operands set to "representative" values
            for ( int cntr = 0; i.hasNext(); cntr++ ) {
                CodeRegion.Operand op = (CodeRegion.Operand)i.next();
                OperandDecl decl = op.getOperandDecl();
                if ( decl.isRegister() ) {
                    // generate a test case for each possible register value of this operand,
                    // substituting representative members for the rest of the operands
                    OperandDecl.RegisterSet r = (OperandDecl.RegisterSet)decl;
                    Iterator ri = r.members.iterator();
                    while ( ri.hasNext() ) {
                        OperandDecl.RegisterEncoding re = (OperandDecl.RegisterEncoding)ri.next();
                        output(cntr, re.name.toString(), rep);
                    }
                } else {
                    // generate a test case for several different values of the immediate of this operand,
                    // substituting representative members for the rest of the operands
                    // this is done by trying several bit patterns shifted left and right, in an attempt
                    // to cover all bits of the operand on and off (while detecting bit reversals)
                    OperandDecl.Immediate imm = (OperandDecl.Immediate)decl;
                    HashSet hs = new HashSet();
                    outputImm(imm.high,  imm, hs, name, cntr,rep);
                    outputImm(imm.low,  imm, hs, name, cntr,rep);
                    boolean word = imm.kind.image.equals("word");
                    outputImmediate(0xffffffff, hs, imm, name, cntr, rep, word);
                    outputImmediate(0xff00ff00, hs, imm, name, cntr, rep, word);
                    outputImmediate(0xf0f0f0f0, hs, imm, name, cntr, rep, word);
                    outputImmediate(0xcccccccc, hs, imm, name, cntr, rep, word);
                    outputImmediate(0xaaaaaaaa, hs, imm, name, cntr, rep, word);
                }
            }
        }

        private void outputImmediate(int value_l, HashSet hs, OperandDecl.Immediate imm, String name, int cntr, String[] rep, boolean word) {
            int value_h = value_l;
            for ( int bit = 0; bit < 32; bit++) {
                outputImm(word ? value_l * 2 : value_l, imm, hs, name, cntr, rep);
                outputImm(word ? value_h * 2 : value_h, imm, hs, name, cntr, rep);
                value_l = value_l >>> 1;
                value_h = value_h << 1;
            }
        }

        private void outputImm(int value_l, OperandDecl.Immediate imm, HashSet hs, String name, int cntr, String[] rep) {
            if ( value_l <= imm.high && value_l >= imm.low ) {
                String value = "0x"+StringUtil.toHex(value_l,2);
                if ( !hs.contains(value)) {
                    output(cntr, value, rep);
                    hs.add(value);
                }
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

            syntax = StringUtil.trimquotes(decl.syntax.image);

            int numops = decl.getOperands().size();
            opnames = new String[numops];
            int cntr = 0;
            Iterator i = decl.getOperandIterator();
            while ( i.hasNext() ) {
                CodeRegion.Operand op = (CodeRegion.Operand)i.next();
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
            Avrora.userError("Cannot create test file", fname);
        }
        return p;
    }

    private String[] getRepresentatives(InstrDecl d) {
        String[] rep = new String[d.getOperands().size()];
        Iterator i = d.getOperandIterator();
        int cntr = 0;
        while ( i.hasNext() ) {
            CodeRegion.Operand op = (CodeRegion.Operand)i.next();
            OperandDecl decl = op.getOperandDecl();
            rep[cntr++] = decl.getSomeMember();
        }
        return rep;
    }
}
