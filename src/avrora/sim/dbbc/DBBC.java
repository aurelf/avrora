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
import avrora.core.isdl.ast.*;
import avrora.Avrora;
import avrora.util.*;
import avrora.sim.BaseInterpreter;
import avrora.sim.GenInterpreter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.io.*;
import java.lang.reflect.Method;

/**
 * @author Ben L. Titzer
 */
public class DBBC {

    public final Options options = new Options();

    protected final Verbose.Printer printer = Verbose.getVerbosePrinter("sim.dbbc");

    public final Option.Long MINIMUM_BLOCK_SIZE = options.newOption("minimum-block-size", 3,
            "This option specifies the minimum size of basic blocks that will be compiled " +
            "to Java source code. It is a compiler heuristic to tune performance; small " +
            "basic blocks may give no (or negative) performance benefit, while large blocks " +
            "might give high benefit.");
    public final Option.Bool USE_REGISTER_ARRAY = options.newOption("use-register-array", true,
            "This option specifies the to dynamic compiler to always emit code that directly " +
            "uses the register array, rather than generating method calls to the interpreter's " +
            "accessor functions.");
    public final Option.Bool CACHE_REGISTERS = options.newOption("cache-registers", false,
            "This option specifies the to dynamic compiler to perform constant and copy propagation " +
            "through the register file by \"caching\" results written into the register file in the " +
            "middle of a basic block.");
    public final Option.Bool ALLOW_REGISTER_UPDATES = options.newOption("allow-register-updates", false,
            "This option specifies the to dynamic compiler to emit defensive code that will ensure " +
            "correct program execution when the register file is updated by a probe or watch.");
    public final Option.Bool DEAD_CODE_ELIMINATION = options.newOption("dead-code-elimination", false,
            "This option specifies the to dynamic compiler to remove useless updates to status flags " +
            "and registers when it can show that they are overwritten by subsequent updates without any " +
            "intervening use.");
    public final Option.Bool CONSTANT_PROPAGATION = options.newOption("constant-propagation", false,
            "This option specifies the to dynamic compiler to propagate constants through the code and " +
            "reduce complicated expressions.");
    public final Option.Bool TEMP_COALLESCING = options.newOption("temp-coallescing", false,
            "This option specifies the to dynamic compiler to attempt to coallesce temporaries to reuse " +
            "stack space for better performance.");
    public final Option.Bool INTRA_BLOCK_PROBING = options.newOption("intra-block-probing", false,
            "This option specifies the to dynamic compiler to allow probes to be inserted inside " +
            "basic blocks and to generate the necessary defensive code to allow probes to run inside " +
            "of basic blocks.");
    public final Option.Bool REUSE_CACHE = options.newOption("reuse-cache", false,
            "This option specifies that the contents of the compiler cache directory should not be " +
            "overwritten, and any dynamically generated code already present in that directory should " +
            "be reused as is.");
    public final Option.Str CACHE_DIRECTORY = options.newOption("cache-directory", "",
            "This option specifies the directory to which to output the Java source code generated " +
            "by the dynamic compiler.");

    protected final Program program;
    protected final ControlFlowGraph cfg;
    protected final DBBCClassLoader loader;
    protected final String tmpDir;

    protected final HashMap codeBlockMap;
    protected final HashMap compiledCodeMap;

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
        public final int wcet; // worse case execution time

        protected CompiledBlock(int a, int wc) {
            beginAddr = a;
            wcet = wc;
        }

        public void execute(GenInterpreter interp) {
            throw Avrora.failure("cannot execute abstract basic block");
        }
    }

    public DBBC(Program p, Options o) {
        program = p;
        cfg = p.getCFG();
        loader = new DBBCClassLoader();
        options.process(o);
        String cd;
        if ( (cd = CACHE_DIRECTORY.get()).equals("") ) cd = "/tmp";
        tmpDir = cd;
        codeBlockMap = new HashMap();
        compiledCodeMap = new HashMap();
        printer.println("Created new compiler for "+program+" to "+tmpDir);
    }

    public Program getProgram() {
        return program;
    }

    protected class DBBCClassLoader extends ClassLoader {
        public Class defineClass(File c) throws Exception {
            FileInputStream fis = new FileInputStream(c);
            byte buf[] = new byte[fis.available()];
            fis.read(buf);
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
     * The <code>getCompiledBlock()</code> method instructs the DBBC_OPT to compile the basic
     * block that begins at the specified byte address.
     * @param addr the byte address of the beginning of the basic block to compile
     * @return a reference to the compiled block of code when complete
     */
    public CompiledBlock getCompiledBlock(int addr) throws Exception {
        printer.println("Getting CompiledBlock for "+StringUtil.addrToString(addr));
        CodeBlock block = getCodeBlock(addr);
        if ( block == null ) return null;
        return getCompiledBlock(block);
    }

    public CodeBlock getCodeBlock(int addr) {
        printer.println("Getting CodeBlock for "+StringUtil.addrToString(addr));
        int wcet = 0;
        ControlFlowGraph.Block b = cfg.getBlockStartingAt(addr);

        CodeBlock nblock = (CodeBlock)codeBlockMap.get(b);
        if ( nblock != null ) {
            printer.println("Cache hit.");
            return nblock;
        }

        if ( b.getSize() < MINIMUM_BLOCK_SIZE.get() ) {
            printer.println("Block "+StringUtil.addrToString(addr)+" is too small: "+b.getSize());
            return null;
        }

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
        nblock = new CodeBlock(addr, stmts, wcet);
        codeBlockMap.put(b, nblock);
        return nblock;
    }

    protected File generateClassForCode(int addr, List stmts, int wcet) throws Exception {
        String classname = "Block_"+StringUtil.addrToString(addr);
        String fname = javaName(classname);
        File f = new File(fname);
        FileOutputStream fos = new FileOutputStream(f);
        Printer p = new Printer(new PrintStream(fos));
        // neat trick: use the same package as BaseInterpreter to access state fields
        p.println("package avrora.sim;");
        p.println("import avrora.util.Arithmetic;");

        // generate the class
        p.startblock("public class "+classname+" extends avrora.sim.dbbc.DBBC.CompiledBlock");

        // generate constructor
        p.println("public "+classname+"() { super("+addr+", "+wcet+"); }");

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
        CompiledBlock cb = (CompiledBlock)compiledCodeMap.get(b);
        if ( cb != null ) return cb;
        File f = generateClassForCode(b.beginAddr, b.stmts, b.wcet);
        File c = compileGeneratedCode(f);
        Class cf = loader.defineClass(c);
        cb = (CompiledBlock)cf.newInstance();
        compiledCodeMap.put(b, cb);
        return cb;
    }

    protected File compileGeneratedCode(File f) throws Exception {
        String name = f.toString();
        String[] args = { name };

        // TODO: generate a nicer error message for failure to find Javac
        Class javac = Class.forName("com.sun.tools.javac.Main");
        Class sac = new String[0].getClass();
        Method m = javac.getMethod("compile", new Class[] { sac });
        Object r = m.invoke(null, new Object[] {args});
        int result = ((Integer)r).intValue();

        if ( result != 0 )
            throw Avrora.failure("DBBC_OPT failed to compile: "+f.toString());
        String cname = name.replaceAll(".java", ".class");
        File c = new File(cname);
        return c;
    }

    protected static HashMap varMap = new HashMap();
    static {
        add("I");
        add("T");
        add("H");
        add("S");
        add("V");
        add("N");
        add("Z");
        add("C");
        add("RAMPZ");
        add("cyclesConsumed");
        add("nextPC");
        add("innerLoop");
        add("justReturnedFromInterrupt");
    }

    protected static void add(String s) {
        varMap.put(s, "interpreter."+s);
    }


    protected class CodeGenerator extends InterpreterGenerator {
        int tmps;

        CodeGenerator(Printer p) {
            super(null, p);
            initializeVariableMap();
        }

        protected void initializeVariableMap() {
            variableMap = varMap;
        }

        public void visit(DeclStmt s) {
            variableMap.put(s.name.toString(), newTemp());
            super.visit(s);
        }

        protected String newTemp() {
            return "temp_"+(tmps++);
        }

        protected void add(String s) {
            variableMap.put(s, "interpreter."+s);
        }

        protected String getMethod(String s) {
            return "interpreter."+s;
        }
    }

    protected String javaName(String cname) {
        return tmpDir+"/"+cname+".java";
    }

    protected String className(String cname) {
        return tmpDir+"/"+cname+".class";
    }
}
