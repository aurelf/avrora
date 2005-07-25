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

package avrora.syntax.gas;

import avrora.util.Util;
import avrora.util.Util;
import avrora.core.Program;
import avrora.core.ProgramReader;
import avrora.syntax.Module;

import java.io.File;
import java.io.FileInputStream;

/**
 * The <code>GASProgramReader</code> is an implementation of the <code>ProgramReader</code> that reads a
 * source program in the GAS-style syntax and builds a program from it.
 *
 * @author Ben L. Titzer
 */
public class GASProgramReader extends ProgramReader {

    /**
     * The <code>read()</code> method accepts a list of filenames as strings, loads them, resolves symbols,
     * and produces a simplified program.
     *
     * @param args the string names of the files to load
     * @return a program built from the specified source files
     * @throws avrora.syntax.gas.ParseException
     *                             if a parse error is encountered
     * @throws java.io.IOException if there is a problem reading from one of the files
     */
    public Program read(String[] args) throws Exception {
        if (args.length == 0)
            Util.userError("no input files");
        // TODO: handle multiple GAS files and link them
        if (args.length != 1)
            Util.userError("input type \"gas\" accepts only one file at a time.");

        File f = new File(args[0]);
        Module module = new Module(true, true);
        FileInputStream fis = new FileInputStream(f);
        GASParser parser = new GASParser(fis, module, f.getName());
        parser.Module();
        Program p = module.build();
        addIndirectEdges(p);
        return p;
    }

    public GASProgramReader() {
        super("The \"gas\" input format reads programs that are written in " +
                "GAS format assembly language. A subset of the directives and " +
                "syntax is supported. No linking functionality is currently " +
                "implemented; all symbol references must be defined in one file. ");
    }

}
