package avrora.core.isdl.gen;

import avrora.core.isdl.Architecture;
import avrora.core.isdl.InstrDecl;
import avrora.core.isdl.CodeRegion;
import avrora.core.isdl.OperandDecl;
import avrora.util.Printer;
import avrora.util.StringUtil;

import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Ben L. Titzer
 */
public class DisassemblerTestGenerator implements Architecture.InstrVisitor {

    Architecture architecture;
    Printer printer;

    public DisassemblerTestGenerator(Architecture a, Printer p) {
        architecture = a;
        printer = p;
    }

    public void generate() {
        architecture.accept(this);
    }

    public void visit(InstrDecl d) {
        if ( !d.pseudo ) {
            String[] rep = getRepresentatives(d);
            String name = StringUtil.trimquotes(d.name.toString());

            Iterator i = d.getOperandIterator();
            int cntr = 0;
            while ( i.hasNext() ) {
                CodeRegion.Operand op = (CodeRegion.Operand)i.next();
                OperandDecl decl = op.getOperandDecl();
                if ( decl.isRegister() ) {
                    OperandDecl.RegisterSet r = (OperandDecl.RegisterSet)decl;
                    Iterator ri = r.members.iterator();
                    while ( ri.hasNext() ) {
                        OperandDecl.RegisterEncoding re = (OperandDecl.RegisterEncoding)ri.next();
                        outputInstr(name, cntr, re.name.toString(), rep);
                    }
                }
                cntr++;
            }
        }
    }

    private void outputInstr(String name, int op, String v, String[] rep) {
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
