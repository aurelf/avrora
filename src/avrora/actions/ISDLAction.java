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

package avrora.actions;

import avrora.util.Util;
import avrora.Main;
import avrora.core.isdl.Architecture;
import avrora.core.isdl.gen.*;
import avrora.core.isdl.parser.ISDLParser;
import avrora.util.Option;
import avrora.util.Printer;
import avrora.util.SectionFile;
import avrora.util.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
            "(ISDL) processing tool, which is used internally in Avrora to describe the AVR " +
            "instruction set and generate the interpreter and disassembler.";

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
    public final Option.Str DISASSEM = newOption("disassembler", "",
            "This option specifies the destination file into which to generate the code for the " +
            "disassembler. The disassembler decodes a binary stream into source-level instructions.");
    public final Option.Str DISTEST = newOption("disassembler-tests", "",
            "This option specifies the directory into which to generate disassembler test cases. " +
            "These test cases will attempt to cover a reasonable portion of the encoding space to " +
            "test the correctness of the disassembler generator.");

    public ISDLAction() {
        super(HELP);
    }

    /**
     * The <code>run()</code> method executes the ISDL processor action. The command line arguments are
     * passed. The ISDL processor expects the first argument to be the name of a file that contains
     * the description of an instruction set.
     * @param args the arguments on the command line
     * @throws Exception if there is a problem loading the file or reading or verifying the ISDL
     */
    public void run(String[] args) throws Exception {
        if (args.length < 1)
            Util.userError("isdl tool usage: avrora -action=isdl <arch.isdl>");

        Architecture.INLINE = INLINE.get();

        String fname = args[0];
        Main.checkFileExists(fname);
        File archfile = new File(fname);
        FileInputStream fis = new FileInputStream(archfile);
        ISDLParser parser = new ISDLParser(fis);
        Status.begin("Parsing "+fname);
        try {
            Architecture a = parser.Architecture();
            Status.success();

            SectionFile sf = createSectionFile("interpreter", "INTERPRETER GENERATOR", INTERPRETER);
            if (sf != null) {
                // generate vanilla interpreter
                new InterpreterGenerator(a, np(sf)).generate();
                sf.close();
                Status.success();
            }

            sf = createSectionFile("Instr.* inner classes", "INSTR GENERATOR", CLASSES);
            if ( sf != null) {
                // generate instruction classes
                new ClassGenerator(a, np(sf)).generate();
                sf.close();
                Status.success();
            }

            sf = createSectionFile("codemap", "CODEBUILDER GENERATOR", CODEMAP);
            if (sf != null) {
                // generate instruction classes
                new CodemapGenerator(a, np(sf)).generate();
                sf.close();
                Status.success();
            }

            sf = createSectionFile("disassembler", "DISASSEM GENERATOR", DISASSEM);
            if (sf != null) {
                // generate the disassembler
                new DisassemblerGenerator(a, np(sf)).generate();
                sf.close();
                Status.success();
            }

            String distest = DISTEST.get();
            if ( !"".equals(distest) ) {
                Status.begin("Generating disassembler tests to " + distest);
                File f = new File(distest);
                new DisassemblerTestGenerator(a, f).generate();
                Status.success();
            }
        } catch ( Util.Error e) {
            Status.error(e);
            return;
        } catch ( Throwable t) {
            Status.error(t);
            return;
        }
    }

    private Printer np(SectionFile f) {
        return new Printer(new PrintStream(f));
    }

    private SectionFile createSectionFile(String gen, String sect, Option.Str opt) throws IOException {
        String s = opt.get();
        if ( !"".equals(s) ) {
            Status.begin("Generating "+gen+" to " + s);
            return new SectionFile(s, sect);
        } else
            return null;
    }
}
