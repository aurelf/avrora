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
import avrora.core.isdl.parser.ISDLParser;
import avrora.core.isdl.gen.InterpreterGenerator;
import avrora.core.isdl.gen.ClassGenerator;
import avrora.core.isdl.gen.FIFInterpreterGenerator;
import avrora.util.Printer;
import avrora.util.SectionFile;
import avrora.util.Option;

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

    public final Option.Bool CLASSES = newOption("classes", true,
                                                 "This option controls whether the ISDL generation tool will emit the code " +
                                                 "for instruction classes.");
    public final Option.Bool INTERPRETER = newOption("interpreter", true,
                                                     "This option controls whether the ISDL generation tool will emit the code " +
                                                     "for the interpreter for this architecture.");
    public final Option.Bool DBBC = newOption("dbbc", true,
                                              "This option controls the generation of the dynamic basic block compiler " +
                                              "(DBBC).");

    public ISDLAction() {
        super("isdl", HELP);
    }

    public void run(String[] args) throws Exception {
        if (args.length != 4)
            Avrora.userError("isdl tool usage: avrora -action=isdl <arch.isdl> <interpreter.java> <fif_interpreter.java> <instr.java>");

        File archfile = new File(args[0]);
        FileInputStream fis = new FileInputStream(archfile);
        ISDLParser parser = new ISDLParser(fis);
        Architecture a = parser.Architecture();

        // generate vanilla interpreter
        SectionFile f = new SectionFile(args[1], "INTERPRETER GENERATOR");
        new InterpreterGenerator(a, new Printer(new PrintStream(f))).generateCode();
        f.close();

        // generate FIF interpreter
        f = new SectionFile(args[2], "FIF GENERATOR");
        new FIFInterpreterGenerator(a, new Printer(new PrintStream(f))).generateCode();
        f.close();

        // generate instruction classes
        f = new SectionFile(args[3], "INSTR GENERATOR");
        new ClassGenerator(a, new Printer(new PrintStream(f))).generate();
        f.close();
    }
}
