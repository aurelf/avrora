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

package avrora.actions;

import avrora.Avrora;
import avrora.core.isdl.Architecture;
import avrora.core.isdl.gen.ClassGenerator;
import avrora.core.isdl.gen.CodemapGenerator;
import avrora.core.isdl.gen.InterpreterGenerator;
import avrora.core.isdl.parser.ISDLParser;
import avrora.util.Option;
import avrora.util.Printer;
import avrora.util.SectionFile;
import avrora.util.Terminal;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;

/**
 * The <code>ISDLAction</code> class implements an action to load an instruction set description from a file
 * and perform various actions with it, including generating the <code>Instr</code> classes and generating an
 * interpreter.
 *
 * @author Ben L. Titzer
 */
public class ISDLAction extends Action {
    public static final String HELP = "The \"isdl\" action invokes the instruction set description language " +
            "(ISDL) tool, which is used internally in Avrora to describe the AVR " +
            "instruction set.";

    public final Option.Str CLASSES = newOption("instr-file", "",
            "This option specifies the destination file into which to generate the Instr classes. " +
            "If this option is not set, the instruction classes will not be generated.");
    public final Option.Str INTERPRETER = newOption("interpreter", "",
            "This option specifies the destination file into which to generate the code for interpreting " +
            "each instruction. If this option is not set, the code for the interpreter will not be " +
            "generated.");
    public final Option.Str CODEMAP = newOption("codemap", "",
            "This option specifies the file to generate the codemap into. The codemap is used in a" +
            "dynamic basic block compiler and dependency analysis of instructions.");
    public final Option.Bool INLINE = newOption("inline", true,
            "This option controls whether the ISDL processor will inline all subroutines marked as " +
            "\"inline\" in their declaration.");

    public ISDLAction() {
        super("isdl", HELP);
    }

    public void run(String[] args) throws Exception {
        if (args.length < 1)
            Avrora.userError("isdl tool usage: avrora -action=isdl <arch.isdl>");

        Architecture.INLINE = INLINE.get();

        File archfile = new File(args[0]);
        FileInputStream fis = new FileInputStream(archfile);
        ISDLParser parser = new ISDLParser(fis);
        Architecture a = parser.Architecture();

        String interpreter = INTERPRETER.get();
        if (!interpreter.equals("")) {
            // generate vanilla interpreter
            Terminal.println("Generating interpreter to " + interpreter + "...");
            SectionFile f = new SectionFile(INTERPRETER.get(), "INTERPRETER GENERATOR");
            new InterpreterGenerator(a, new Printer(new PrintStream(f))).generateCode();
            f.close();
        }

        String classes = CLASSES.get();
        if (!classes.equals("")) {
            // generate instruction classes
            Terminal.println("Generating Instr inner classes to " + classes + "...");
            SectionFile f = new SectionFile(classes, "INSTR GENERATOR");
            new ClassGenerator(a, new Printer(new PrintStream(f))).generate();
            f.close();
        }

        String codemap = CODEMAP.get();
        if (!codemap.equals("")) {
            // generate instruction classes
            Terminal.println("Generating codemap to " + codemap + "...");
            SectionFile f = new SectionFile(codemap, "CODEBUILDER GENERATOR");
            new CodemapGenerator(a, new Printer(new PrintStream(f))).generate();
            f.close();
        }
    }
}
