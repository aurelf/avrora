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

package avrora.sim.dbbc;

import avrora.core.Program;
import avrora.core.ControlFlowGraph;
import avrora.core.Instr;
import avrora.core.isdl.CodeRegion;
import avrora.core.isdl.gen.InterpreterGenerator;
import avrora.core.isdl.ast.VarAssignStmt;
import avrora.core.isdl.ast.Literal;
import avrora.core.isdl.ast.Arith;
import avrora.core.isdl.ast.VarExpr;
import avrora.Avrora;
import avrora.util.StringUtil;
import avrora.util.Printer;
import avrora.sim.BaseInterpreter;
import avrora.sim.GenInterpreter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.io.*;

/**
 * @author Ben L. Titzer
 */
public class DBBC {

    protected final Program program;
    protected final ControlFlowGraph cfg;
    protected final DBBCClassLoader loader;

    public static class CodeBlock {
        public final int beginAddr;
        public final LinkedList stmts;
        public final int wcet; // worse case execution time

        protected CodeBlock(int a, LinkedList l, int wc) {
            beginAddr = a;
            stmts = l;
            wcet = wc;
        }
    }

    public static class CompiledBlock {
        public final int beginAddr;
        public final LinkedList stmts;
        public final int wcet; // worse case execution time

        protected CompiledBlock(int a, LinkedList l, int wc) {
            beginAddr = a;
            stmts = l;
            wcet = wc;
        }

        public void execute(GenInterpreter interp) {
            throw Avrora.failure("cannot execute abstract basic block");
        }
    }

    public DBBC(Program p) {
        program = p;
        cfg = p.getCFG();
        loader = new DBBCClassLoader();
    }

    public Program getProgram() {
        return program;
    }

    protected class DBBCClassLoader extends ClassLoader {
        public Class defineClass(byte[] buf) {
            return super.defineClass(null, buf, 0, buf.length);
        }
    }

    /**
     * The <code>invalidateBlock()</code> method invalidates any compiled copies of
     * the block that contains the specified program address. This might be called
     * as the result of a write to program memory (flash update) or insertion of
     * a probe into the middle of a basic block.
     * @param addr the byte address for which the enclosing block should be invalidated
     */
    public void invalidateBlock(int addr) {
        throw Avrora.unimplemented();
    }

    /**
     * The <code>getCompiledBlock()</code> method instructs the DBBC to compile the basic
     * block that begins at the specified byte address.
     * @param addr the byte address of the beginning of the basic block to compile
     * @return a reference to the compiled block of code when complete
     */
    public CompiledBlock getCompiledBlock(int addr) throws Exception {
        CodeBlock block = getCodeBlock(addr);
        return getCompiledBlock(block);
    }

    public CodeBlock getCodeBlock(int addr) {
        int wcet = 0;
        ControlFlowGraph.Block b = cfg.getBlockStartingAt(addr);
        LinkedList stmts = new LinkedList();
        Iterator i = b.getInstrIterator();
        int curPC = addr;
        while (i.hasNext()) {
            Instr instr = (Instr)i.next();
            wcet += instr.getCycles();
            CodeRegion r = CodeMap.getCodeForInstr(curPC, instr);
            curPC += instr.getSize();
            if ( !i.hasNext() ) { // is this the last instruction?
                // inject an assignment to nextPC
                stmts.add(new VarAssignStmt("nextPC", new Literal.IntExpr(curPC)));
                stmts.addAll(r.getCode());
                stmts.add(new VarAssignStmt("cyclesConsumed",
                        new Arith.BinOp.AddExpr(
                                new VarExpr("cyclesConsumed"),
                                new Literal.IntExpr(wcet))));
            } else {
                stmts.addAll(r.getCode());
            }

        }
        return new CodeBlock(addr, stmts, wcet);
    }

    protected File generateCode(int addr, List stmts, int wcet) throws Exception {
        String classname = "Block_"+StringUtil.addrToString(addr);
        String fname = javaName(classname);
        File f = new File(fname);
        FileOutputStream fos = new FileOutputStream(f);
        Printer p = new Printer(new PrintStream(fos));
        // neat trick: use the same package as BaseInterpreter to access state fields
        p.println("package avrora.sim;");

        // generate the class
        p.startblock("public class "+classname+" extends avrora.sim.dbbc.DBBC.CompiledBlock");

        // generate constructor
        p.println("public "+classname+"() { super("+addr+", null, "+wcet+"); }");

        // generate the execute method
        p.startblock("public void execute(avrora.sim.GenInterpreter interpreter)");

        CodeGenerator gen = new CodeGenerator(p);
        gen.visitStmtList(stmts);

        // end execute method
        p.endblock();

        // end class
        p.endblock();
        fos.close();
        return f;
    }

    public CompiledBlock getCompiledBlock(CodeBlock b) throws Exception {
        File f = generateCode(b.beginAddr, b.stmts, b.wcet);
        File c = compileGeneratedCode(f);
        Class cf = getClass(c);
        return (CompiledBlock)cf.newInstance();
    }

    protected Class getClass(File c) throws Exception {
        FileInputStream fis = new FileInputStream(c);
        byte buf[] = new byte[fis.available()];
        fis.read(buf);
        return loader.defineClass(buf);
    }

    protected File compileGeneratedCode(File f) {
        String name = f.toString();
        String[] args = { name };
        // TODO: remove static dependency on javac by using reflection
        int result = com.sun.tools.javac.Main.compile(args);
        if ( result != 0 )
            throw Avrora.failure("DBBC failed to compile: "+f.toString());
        String cname = name.replaceAll(".java", ".class");
        File c = new File(cname);
        return c;
    }

    protected class CodeGenerator extends InterpreterGenerator {
        CodeGenerator(Printer p) {
            super(null, p);
            initializeVariableMap();
        }

        protected void initializeVariableMap() {
            variableMap = new HashMap();
            // TODO: this is sort of a hack, it depends on I, T, etc
            add("I");
            add("T");
            add("H");
            add("S");
            add("V");
            add("N");
            add("Z");
            add("C");
            add("cyclesConsumed");
            add("nextPC");
            add("innerLoop");
        }

        protected void add(String s) {
            variableMap.put(s, "interpreter."+s);
        }

        protected String getMethod(String s) {
            return "interpreter."+s;
        }
    }

    protected String javaName(String cname) {
        return "/tmp/"+cname+".java";
    }

    protected String className(String cname) {
        return "/tmp/"+cname+".class";
    }
}
