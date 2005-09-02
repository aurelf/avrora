/**
 * Copyright (c) 2005, Regents of the University of California
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

package avrora.syntax.elf;

import avrora.core.ProgramReader;
import avrora.core.Program;
import avrora.Main;
import avrora.util.*;

import java.io.FileInputStream;

/**
 * The <code>ELFLoader</code> class is capable of loading ELF (Executable and Linkable Format)
 * files and disassembling them into the simulator's internal format.
 *
 * @author Ben L. Titzer
 */
public class ELFLoader extends ProgramReader {

    public ELFLoader() {
        super("The \"elf\" format loader reads a program from an ELF (Executable and Linkable " +
                "Format) as a binary and disassembles the sections corresponding to executable code.");
    }

    public Program read(String[] args) throws Exception {
        if (args.length == 0)
            Util.userError("no input files");
        if (args.length != 1)
            Util.userError("input type \"elf\" accepts only one file at a time.");

        String fname = args[0];
        Main.checkFileExists(fname);

        FileInputStream fis = new FileInputStream(fname);

        ELFHeader header = new ELFHeader();
        header.read(fis);

        Terminal.nextln();
        TermUtil.reportQuantity("ELF version", header.e_version, "");
        TermUtil.reportQuantity("ELF machine", header.e_machine, "("+header.getArchitecture()+")");
        TermUtil.reportQuantity("ELF size", header.is64Bit() ? 64 : 32, "bits");
        TermUtil.reportQuantity("ELF endianness", header.isLittleEndian() ? "little" : "big", "");

        return null;
    }
}
