/**
 * Copyright (c) 2004, Regents of the University of California
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

package avrora.core.isdl.gen;

import avrora.core.isdl.Architecture;
import avrora.core.isdl.CodeRegion;
import avrora.core.isdl.InstrDecl;
import avrora.core.isdl.ast.VarAssignStmt;
import avrora.core.isdl.parser.Token;
import avrora.util.Printer;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Ben L. Titzer
 */
public class FIFInterpreterGenerator extends InterpreterGenerator {

    public FIFInterpreterGenerator(Architecture a, Printer p) {
        super(a, p);
    }

    public void generateCode() {
        printer.indent();
        new FIFBuilderEmitter().generate(architecture);
        architecture.accept((Architecture.SubroutineVisitor)this);
        generateExecuteMethod();
        printer.unindent();
    }

    private void generateExecuteMethod() {
        architecture.accept((Architecture.InstrVisitor)this);
    }

    public void visit(InstrDecl d) {
        printer.startblock("protected class FIFInstr_" + d.getInnerClassName() + " extends FIFInstr");
        printer.println("FIFInstr_" + d.getInnerClassName() + "(Instr i, int pc) { super(i, pc); }");
        printer.startblock("public void execute(FIFInterpreter interp) ");

        // initialize the map of local variables to operands
        initializeOperandMap(d);
        // emit the code of the body
        visitStmtList(d.getCode());
        // emit the cycle count update
        printer.println("cyclesConsumed += " + d.cycles + ";");
        printer.endblock();
        printer.endblock();
    }

    public void visit(VarAssignStmt s) {
        String var = getVariable(s.variable);
        if (var.equals("nextInstr.pc")) {
            printer.print("nextInstr = fifMap[");
            s.expr.accept(codeGen);
            printer.println("];");

        } else {
            printer.print(var + " = ");
            s.expr.accept(codeGen);
            printer.println(";");
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

            printer.startblock("public void visit(" + d.getClassName() + " i)");

            printer.println("instr = new FIFInstr_" + d.getInnerClassName() + "(i, pc);");
            Iterator i = d.getOperandIterator();
            while (i.hasNext()) {
                CodeRegion.Operand o = (CodeRegion.Operand)i.next();
                String n, s = "";
                if (o.isRegister()) {
                    n = "r" + (++regcount);
                    s = ".getNumber()";
                } else
                    n = "imm" + (++immcount);

                printer.println("instr." + n + " = i." + n + s + ";");
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
            CodeRegion.Operand o = (CodeRegion.Operand)i.next();

            String image = o.name.image;
            if (cr instanceof InstrDecl) {
                if (o.isRegister())
                    image = "r" + (++regcount);
                else
                    image = "imm" + (++immcount);
            }

            operandMap.put(o.name.image, image);
        }

        operandMap.put("nextPC", "nextInstr.pc");
    }

    protected String getVariable(Token var) {
        if (var.image.startsWith("tmp_"))
            return var.image;
        else if (operandMap.get(var.image) != null) {
            return (String)operandMap.get(var.image);
        } else {
            return "interp." + var.image;
        }
    }
}
