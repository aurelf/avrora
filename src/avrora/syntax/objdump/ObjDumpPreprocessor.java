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


package avrora.syntax.objdump;

import java.io.*;
import java.util.StringTokenizer;

import avrora.util.StringUtil;

/**
 * The <code>ObjDumpPreprocessor</code> class is a utility class that takes the output
 * from the <code>avr-objdump</code> utility and produces a cleaned up version that is
 * more suitable for parsing into the internal format of Avrora.
 * @author Ben L. Titzer
 * @author Vids Samanta
 */
public class ObjDumpPreprocessor {


    BufferedReader in;
    StringBuffer out;

    /**
     *
     * @param inFile of file with avr-objdump format code
     */
    public ObjDumpPreprocessor(String inFile) {
        try {
            in = new BufferedReader(new FileReader(inFile));
            out = new StringBuffer();
            cleanFile();
            in.close();

        } catch (Exception e) {
            System.err.println("Error reading file \"" + inFile + "\":\n" + e);
            System.exit(-1);
        }
    }

    /**
     * @return output StringBuffer of well formated
     * cleaned up objdump file contents
     */
    public StringBuffer getCleanObjDumpCode() {
        return out;
    }


    private void cleanFile() throws IOException {

        String line = in.readLine();

        //clean up first section
        while (line != null) {
            if (line.indexOf("Disassembly of section .text") != -1) {
                break;
            }
            if (line.indexOf("main.exe") != -1)
                out.append("program \"main.exe\":\n\n");
            else if (line.indexOf(".text") != -1) {
                out.append("  section .text ");
                StringTokenizer st = new StringTokenizer(line);
                st.nextToken(); // 0
                st.nextToken(); //.text
                out.append(" size=0x" + st.nextToken());
                out.append(" vma=0x" + st.nextToken());
                out.append(" lma=0x" + st.nextToken());
                out.append(" offset=0x" + st.nextToken());
                out.append(" ;" + st.nextToken());
                out.append(" \n");
            } else if (line.indexOf(".data") != -1) {
                out.append("  section .data ");
                StringTokenizer st = new StringTokenizer(line);
                st.nextToken(); // 0
                st.nextToken(); //.text
                out.append(" size=0x" + st.nextToken());
                out.append(" vma=0x" + st.nextToken());
                out.append(" lma=0x" + st.nextToken());
                out.append(" offset=0x" + st.nextToken());
                out.append(" ;" + st.nextToken());
                out.append(" \n");
            }
            line = in.readLine();
        }

        while (line != null) {
            // ignore ... in output
            if (line.indexOf("...") != - 1) {
                line = in.readLine();
                continue;
            }

            if ( line.indexOf("Address ") != -1 ) {
                line = line.substring(0, line.indexOf("Address "));
                line += in.readLine();
            }

            int offset = line.indexOf("Disassembly of section");
            if (offset != -1) {
                String section = line.substring(line.indexOf('.'), line.indexOf(':'));
                out.append("\nstart "+section+":\n\n");

            }
            else {

                if (isLabel(line)) {
                    out.append("\nlabel 0x");
                    StringTokenizer st = new StringTokenizer(line);
                    out.append(st.nextToken());
                    out.append("  " + (st.nextToken()).replaceAll("[<,>]", "\"") + "\n");
                } else {

                    String tok;
                    StringTokenizer st = new StringTokenizer(line);
                    if (st.hasMoreTokens()) {
                        tok = st.nextToken();
                        out.append(StringUtil.rightJustify("0x" + tok, 10));
                        while (st.hasMoreTokens()) {
                            tok = st.nextToken();

                            if (tok.matches("\\p{XDigit}\\p{XDigit}"))
                                out.append(" 0x" + tok);
                            else
                                out.append("  " + tok);

                        }
                        out.append("\n");
                    }
                }
            }
            line = in.readLine();
        }
    }

    /**
     * @param s
     * @return true if statement is of the form: <hexdig> <\<LABEL\>:>
     */
    private boolean isLabel(String s) {
        if (s.indexOf("<") == -1)
            return false;
        if (s.indexOf(">:") == -1)
            return false;
        return true;
    }

    //test main
    public static void main(String a[]) {
        ObjDumpPreprocessor p = new ObjDumpPreprocessor(a[0]);
        System.out.println(p.getCleanObjDumpCode());
    }
}
