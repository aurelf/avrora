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

package jintgen.gen;

import jintgen.isdl.Architecture;
import jintgen.Main;
import avrora.util.Options;
import avrora.util.SectionFile;
import avrora.util.Option;
import avrora.util.Status;

import java.io.IOException;

/**
 * The <code>Generator</code> class represents a class that generates
 * some tool from an instruction set description, such as an interpreter, disassembler,
 * assembler, etc.
 *
 * @author Ben L. Titzer
 */
public abstract class Generator {

    public Architecture arch;
    protected final Options options = new Options();

    public final Option.Str DEST_PACKAGE = options.newOption("package", "",
            "This option specifies the name of the destination java package for generators.");
    public final Option.Str CLASS_PREFIX = options.newOption("class-prefix", "*",
            "This option specifies a prefix for each class name to be generated. When this option " +
            "is set to \"*\", the generators will append the name of the architecture to the beginning " +
            "of each class generated; otherwise generators will append the specified string.");

    public void setArchitecture(Architecture a) {
        arch = a;
    }

    public void processOptions(Options o) {
        options.process(o);
    }

    public abstract void generate() throws Exception;

    protected SectionFile createSectionFile(String fname, String sect) throws IOException {
        Main.checkFileExists(fname);
        return new SectionFile(fname, sect);
    }

    protected String className(String cls) {
        String prefix = CLASS_PREFIX.get();
        if ( "*".equals(prefix) ) return arch.getName().toUpperCase()+cls;
        else return prefix+cls;
    }
}
