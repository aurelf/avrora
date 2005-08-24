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
import avrora.arch.avr.AVRDisassembler;
import avrora.arch.avr.AVRInstr;
import avrora.arch.msp430.MSP430Instr;
import avrora.arch.msp430.MSP430Disassembler;
import avrora.util.StringUtil;
import avrora.util.Terminal;
import avrora.util.Option;

import java.io.File;
import java.io.FileInputStream;

/**
 * The <code>DisassembleAction</code> class represents an action that allows the user to disassemble
 * a binary file and display the instructions. This is useful for debugging the disassembler and also
 * for inspecting binaries.
 *
 * @author Ben L. Titzer
 */
public class DisassembleAction extends Action {

    Option.Str ARCH = options.newOption("arch", "avr", 
            "This option selects the architecture for the disassembler.");
    
    public DisassembleAction() {
        super("The \"disassemble\" action disassembles a binary file into source level instructions.");
    }

    /**
     * The <code>run()</code> method executes the action. The arguments on the command line are passed.
     * The <code>Disassemble</code> action expects the first argument to be the name of the file to
     * disassemble.
     * @param args the command line arguments
     * @throws Exception if there is a problem reading the file or disassembling the instructions in the
     * file
     */
    public void run(String[] args) throws Exception {
        if ( args.length < 1 )
            Util.userError("no input data");

        byte[] buf = new byte[128];

        for ( int cntr = 0; cntr < args.length; cntr++ ) {
            buf[cntr] = (byte)StringUtil.evaluateIntegerLiteral(args[cntr]);
        }

        String arch = ARCH.get();
        if ( "avr".equals(arch)) {
            AVRDisassembler da = new AVRDisassembler();
            AVRInstr i = da.decode(0, 0, buf);
            String str = i.toString();
            print(buf, str);
        } else if ( "msp430".equals(arch)) {
            MSP430Disassembler da = new MSP430Disassembler();
            MSP430Instr i = da.decode(0, 0, buf);
            String str = i.toString();
            print(buf, str);
        } else {
            Util.userError("Unknown architecture", arch);
        }
    }

    private void print(byte[] buf, String str) {
        Terminal.println(StringUtil.addrToString(0)+": "+hb(buf, 0)+" "+hb(buf, 0+1)+"        "+str);
    }

    private String hb(byte[] buf, int index) {
        return StringUtil.toHex(buf[index],2);
    }
}
