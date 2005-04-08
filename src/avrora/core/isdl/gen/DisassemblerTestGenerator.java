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
import java.util.HashMap;
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
    HashMap operandGenerators;

    public DisassemblerTestGenerator(Architecture a, File dir) {
        architecture = a;
        if ( !dir.isDirectory() )
            Avrora.userError("must specify directory for testcases");
        dname = dir.getAbsolutePath();
        directory = dir;
        operandGenerators = new HashMap();
        operandGenerators.put("immediate", new ImmediateGenerator());
        operandGenerators.put("address", new ImmediateGenerator());
        operandGenerators.put("symbol", new SymbolGenerator());
        operandGenerators.put("relative", new RelativeGenerator());
        operandGenerators.put("word", new WordGenerator());
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

    abstract class OperandGenerator {
        abstract void generate(Generator g, InstrDecl decl, OperandDecl od, int op, String[] rep);
        abstract String getSomeMember(OperandDecl decl);
    }

    class SymbolGenerator extends OperandGenerator {
        void generate(Generator g, InstrDecl decl, OperandDecl od, int op, String[] rep) {
            // generate a test case for each possible register value of this operand,
            // substituting representative members for the rest of the operands
            OperandDecl.RegisterSet r = (OperandDecl.RegisterSet)od;
            Iterator ri = r.members.iterator();
            while ( ri.hasNext() ) {
                OperandDecl.RegisterEncoding re = (OperandDecl.RegisterEncoding)ri.next();
                g.output(op, re.name.toString(), rep);
            }
        }
        String getSomeMember(OperandDecl decl) {
            OperandDecl.RegisterSet r = (OperandDecl.RegisterSet)decl;
            OperandDecl.RegisterEncoding enc = (OperandDecl.RegisterEncoding)r.members.get(0);
            return enc.name.toString();
        }
    }

    class ImmediateGenerator extends OperandGenerator {
        void generate(Generator g, InstrDecl decl, OperandDecl od, int op, String[] rep) {
            OperandDecl.Immediate imm = (OperandDecl.Immediate)od;
            HashSet hs = new HashSet();
            outputImm(g, imm.high,  imm, hs, op,rep);
            outputImm(g, imm.low,  imm, hs, op,rep);
            outputImmediate(g, 0xffffffff, hs, imm, op, rep);
            outputImmediate(g, 0xff00ff00, hs, imm, op, rep);
            outputImmediate(g, 0xf0f0f0f0, hs, imm, op, rep);
            outputImmediate(g, 0xcccccccc, hs, imm, op, rep);
            outputImmediate(g, 0xaaaaaaaa, hs, imm, op, rep);
        }

        protected void outputImmediate(Generator g, int value_l, HashSet hs, OperandDecl.Immediate imm, int cntr, String[] rep) {
            int value_h = value_l;
            for ( int bit = 0; bit < 32; bit++) {
                outputImm(g, value_l, imm, hs, cntr, rep);
                outputImm(g, value_h, imm, hs, cntr, rep);
                value_l = value_l >>> 1;
                value_h = value_h << 1;
            }
        }

        protected void outputImm(Generator g, int val, OperandDecl.Immediate imm, HashSet hs, int cntr, String[] rep) {
            if ( val <= imm.high && val >= imm.low ) {
                String value = "0x"+StringUtil.toHex(val,2);
                if ( !hs.contains(value)) {
                    g.output(cntr, value, rep);
                    hs.add(value);
                }
            }
        }

        String getSomeMember(OperandDecl decl) {
            OperandDecl.Immediate imm = (OperandDecl.Immediate)decl;
            // return the average value
            return "0x"+StringUtil.toHex((imm.low+((imm.high-imm.low)/2)), 2);
        }
    }

    class RelativeGenerator extends OperandGenerator {
        void generate(Generator g, InstrDecl decl, OperandDecl od, int op, String[] rep) {
            OperandDecl.Immediate imm = (OperandDecl.Immediate)od;
            int pc = 0;
            // for relative, we will dumbly generate all possibilities.
            for ( int cntr = imm.high; cntr >= imm.low; cntr-- ) {
                g.output(op, ".+"+cntr*2, rep);
                pc += decl.getEncodingSize() / 8;
            }
//            throw Avrora.unimplemented();
        }

        String getSomeMember(OperandDecl decl) {
            return "0x00";
        }
    }

    class WordGenerator extends ImmediateGenerator {
        protected void outputImm(Generator g, int val, OperandDecl.Immediate imm, HashSet hs, int cntr, String[] rep) {
            if ( val <= imm.high && val >= imm.low ) {
                val = val * 2;
                String value = "0x"+StringUtil.toHex(val,2);
                if ( !hs.contains(value)) {
                    g.output(cntr, value, rep);
                    hs.add(value);
                }
            }
        }

        String getSomeMember(OperandDecl decl) {
            OperandDecl.Immediate imm = (OperandDecl.Immediate)decl;
            // return the average value
            int avg = (imm.low+((imm.high-imm.low)/2));
            return "0x"+StringUtil.toHex(avg * 2, 2);
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
                OperandGenerator g = getOperandGenerator(decl);
                g.generate(this, this.decl, decl, cntr, rep);
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
            OperandGenerator g = getOperandGenerator(decl);
            rep[cntr++] = g.getSomeMember(decl);
        }
        return rep;
    }

    private OperandGenerator getOperandGenerator(OperandDecl decl) {
        OperandGenerator g = (OperandGenerator)operandGenerators.get(decl.kind.image);
        if ( g == null )
            throw Avrora.failure("cannot generate representative values for operand of type "+decl.kind.image);
        return g;
    }

}
