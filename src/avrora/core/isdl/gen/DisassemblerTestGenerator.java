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
        if ( !d.pseudo ) {
            // generate a new file for the test case
            String name = StringUtil.trimquotes(d.name.toString());
            Printer p = createPrinter(name);
            String[] rep = getRepresentatives(d);

            Iterator i = d.getOperandIterator();
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
                        outputInstr(p, name, cntr, re.name.toString(), rep);
                    }
                } else {
                    // generate a test case for several different values of the immediate of this operand,
                    // substituting representative members for the rest of the operands
                    // this is done by trying several bit patterns shifted left and right, in an attempt
                    // to cover all bits of the operand on and off (while detecting bit reversals)
                    OperandDecl.Immediate imm = (OperandDecl.Immediate)decl;
                    HashSet hs = new HashSet();
                    outputImm(imm.high,  imm, hs, p, name, cntr,rep);
                    outputImm(imm.low,  imm, hs, p, name, cntr,rep);
                    outputImmediate(0xffffffff, hs, imm, p, name, cntr, rep);
                    outputImmediate(0xf0f0f0f0, hs, imm, p, name, cntr, rep);
                    outputImmediate(0xcccccccc, hs, imm, p, name, cntr, rep);
                    outputImmediate(0xaaaaaaaa, hs, imm, p, name, cntr, rep);
                }
            }
        }
    }

    private Printer createPrinter(String name) {
        String fname = dname + File.pathSeparatorChar +name+".tst";
        Printer p = null;
        try {
            File file = new File(fname);
            p = new Printer(new PrintStream(new FileOutputStream(file)));
        } catch ( IOException e) {
            Avrora.userError("Cannot create test file", fname);
        }
        return p;
    }

    private void outputImmediate(int value_l, HashSet hs, OperandDecl.Immediate imm, Printer p, String name, int cntr, String[] rep) {
        int value_h = value_l;
        for ( int bit = 0; bit < 32; bit++) {
            outputImm(value_l, imm, hs, p, name, cntr, rep);
            outputImm(value_h, imm, hs, p, name, cntr, rep);
            value_l = value_l >>> 1;
            value_h = value_h << 1;
        }
    }

    private void outputImm(int value_l, OperandDecl.Immediate imm, HashSet hs, Printer p, String name, int cntr, String[] rep) {
        if ( value_l <= imm.high && value_l >= imm.low ) {
            String value = "0x"+StringUtil.toHex(value_l,2);
            if ( !hs.contains(value)) {
                outputInstr(p, name, cntr, value, rep);
                hs.add(value);
            }
        }
    }


    private void outputInstr(Printer p, String name, int op, String v, String[] rep) {
        p.print(name+" ");
        for ( int cntr = 0; cntr < rep.length; cntr++ ) {
            if ( cntr == op )
                p.print(v);
            else
                p.print(rep[cntr]);
            if ( cntr != rep.length-1 )
                p.print(", ");
        }
        p.nextln();
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
