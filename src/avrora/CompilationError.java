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

package avrora;

import avrora.syntax.ProgramPoint;
import avrora.util.Terminal;

/**
 * The <code>CompilationError</code> class represents an error in a user program, including the module
 * contents and line and column numbers.
 *
 * @author Ben L. Titzer
 */
public class CompilationError extends Avrora.Error {
    public final ProgramPoint point;
    public final String errclass;
    public final String[] errparams;

    public static boolean CLASSES;

    public CompilationError(ProgramPoint p, String msg, String ec, String ps[]) {
        super(msg, null);
        point = p;
        errclass = ec;
        errparams = ps;
    }

    public void report() {
        Terminal.printRed(point.file);
        Terminal.print(" ");
        Terminal.printBrightBlue(point.line + ":" + point.beginColumn);
        Terminal.print(": ");
        if (CLASSES)
            Terminal.print(errclass + ": ");
        Terminal.print(message);
        Terminal.print("\n");
        if (STACKTRACES) {
            printStackTrace();
        }
    }

    public String getErrorClass() {
        return errclass;
    }

    public String[] getErrorParams() {
        return errparams;
    }
}
