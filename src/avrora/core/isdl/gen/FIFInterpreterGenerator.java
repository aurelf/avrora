package avrora.core.isdl.gen;

import avrora.core.isdl.Architecture;
import avrora.util.Printer;
import avrora.core.isdl.InstrDecl;
import avrora.core.isdl.CodeRegion;
import avrora.core.isdl.SubroutineDecl;
import avrora.core.isdl.ast.VarAssignStmt;

import java.util.Iterator;
import java.util.HashMap;

/**
 * @author Ben L. Titzer
 */
public class FIFInterpreterGenerator extends InterpreterGenerator {

    public FIFInterpreterGenerator(Architecture a, Printer p) {
        super(a, p);
    }

    public void generateCode() {
        printer.indent();
        architecture.accept(new ConstantEmitter());
        new FIFBuilderEmitter().generate(architecture);
        architecture.accept((Architecture.SubroutineVisitor)this);
        generateExecuteMethod();
        printer.unindent();
    }

    private void generateExecuteMethod() {
        printer.startblock("protected void execute(FIFInstr i)");
        printer.println("nextInstr = nextInstr.next;");
        printer.startblock("switch ( i.opcode ) ");
        architecture.accept((Architecture.InstrVisitor)this);
        printer.endblock();
        printer.endblock();
    }

    public void visit(InstrDecl d) {
        printer.startblock("case " + d.getInnerClassName() + "_code: ");

        // initialize the map of local variables to operands
        initializeOperandMap(d);
        // emit the code of the body
        visitStmtList(d.getCode());
        // emit the cycle count update
        printer.println("cyclesConsumed += " + d.cycles + ";");
        printer.println("break;");
        printer.endblock();
    }

    public void visit(VarAssignStmt s) {
        String var = getVariable(s.variable);
        if ( var.equals("nextInstr.pc") ) {
            printer.print("nextInstr = fifMap[");
            s.expr.accept(codeGen);
            printer.println("];");

        } else {
            printer.print(var + " = ");
            s.expr.accept(codeGen);
            printer.println(";");
        }
    }


    protected class ConstantEmitter implements Architecture.InstrVisitor {
        int count;

        public void visit(InstrDecl d) {
            printer.println("public static final int "+d.getInnerClassName()+"_code = "+count+";");
            count++;
        }
    }

    protected class FIFBuilderEmitter implements Architecture.InstrVisitor {
        public void generate(Architecture a) {
            printer.startblock("protected class FIFBuilder implements InstrVisitor");
            printer.println("private FIFInstr instr;");
            printer.println("private int pc;");
            printer.startblock("protected FIFInstr build(int pc, Instr i)");
            printer.println("this.pc = pc;");
            printer.println("i.accept(this);");
            printer.println("return instr;");
            printer.endblock();
            a.accept(this);
            printer.endblock();
        }

        public void visit(InstrDecl d) {
            int regcount = 0;
            int immcount = 0;

            printer.startblock("public void visit("+d.getClassName()+" i)");

            printer.println("instr = new FIFInstr(i, pc, "+d.getInnerClassName()+"_code);");
            Iterator i = d.getOperandIterator();
            while ( i.hasNext() ) {
                CodeRegion.Operand o = (CodeRegion.Operand)i.next();
                String n, s = "";
                if ( o.isRegister() ) {
                    n = "r"+(++regcount);
                    s = ".getNumber()";
                }
                else
                    n = "imm"+(++immcount);

                printer.println("instr."+n+" = i."+n+s+";");
            }
            printer.endblock();
        }
    }

    protected void initializeOperandMap(CodeRegion cr) {
        operandMap = new HashMap();
        Iterator i = cr.getOperandIterator();
        int regcount = 0;
        int immcount = 0;

        while (i.hasNext()) {
            CodeRegion.Operand o = (CodeRegion.Operand) i.next();

            String image = o.name.image;
            if ( cr instanceof InstrDecl ) {
                if ( o.isRegister() ) image = "i.r"+(++regcount);
                else image = "i.imm"+(++immcount);
            }

            operandMap.put(o.name.image, image);
        }

        operandMap.put("nextPC", "nextInstr.pc");
    }

}
