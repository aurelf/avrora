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


package avrora.syntax.objdump;

import avrora.util.StringUtil;
import avrora.util.Verbose;
import avrora.util.Option;
import avrora.Avrora;
import avrora.actions.Action;

import java.io.*;
import java.util.StringTokenizer;

/**
 * The <code>ObjDumpPreprocessor</code> class is a utility class that takes the output from the
 * <code>avr-objdump</code> utility and produces a cleaned up version that is more suitable for parsing into
 * the internal format of Avrora.
 *
 * @author Ben L. Titzer
 * @author Vids Samanta
 */
public class ObjDumpPreprocessor extends Action {

    protected final Option.Str FILE = options.newOption("file", "", 
            "The \"file\" option, when set, indicates the file to which to output the " +
            "preprocessed objdump output.");
    
    protected final Verbose.Printer printer = Verbose.getVerbosePrinter("reader.objdump");

    public ObjDumpPreprocessor() {
        super("The \"odpp\" action tests the functionality of the objdump preprocessor that " +
              "cleans up the format output by objdump into something more suitable for automated " +
              "parsing.");
    }

    public StringBuffer cleanCode(String inFile) {
        try {
            StringBuffer out = new StringBuffer(10000);
            BufferedReader in = new BufferedReader(new FileReader(inFile));
            cleanFile(in, out);
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            throw Avrora.failure("Failure reading objdump format: "+e);
        }
    }

    private void cleanFile(BufferedReader in, StringBuffer out) throws IOException {

        line_count = 0;
        String line = nextLine(in);

        //clean up first section
        line = readHeader(in, out, line);

        while (line != null) {
            String section = getSectionName(line);
            if (section != null) {
                // read the whole section
                line = readSection(in, out, section);
            } else {
                // ignore this line if it is between sections
                line = nextLine(in);
            }
        }
    }

    private String getSectionName(String line) {
        int offset = line.indexOf("Disassembly of section");
        if (offset != -1) {
            return line.substring(line.indexOf('.'), line.indexOf(':'));
        }
        return null;
    }

    private String readHeader(BufferedReader in, StringBuffer out, String line) throws IOException {
        while (line != null) {
            if (line.indexOf("Disassembly of section") != -1) {
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
            line = nextLine(in);
        }
        return line;
    }

    private String readSection(BufferedReader in, StringBuffer out, String section) throws IOException {

        if ( section.equals(".data") || section.equals(".text") )
            return convertSection(in, out, section);
        else
            return ignoreSection(in, out, section);
    }

    private String ignoreSection(BufferedReader in, StringBuffer out, String section) throws IOException {
        out.append("; section "+section+" removed");
        String line = nextLine(in);
        while ( line != null) {
            out.append("; "+line+"\n");
            if ( getSectionName(line) != null )
                return line;
            line = nextLine(in);
        }
        return line;
    }

    private String convertSection(BufferedReader in, StringBuffer out, String section) throws IOException {
        // add the start of the section name
        out.append("\nstart " + section + ":\n");

        // read the next line
        String line = nextLine(in);

        while (line != null) {

            // beginning of new section
            if (getSectionName(line) != null)
                return line;

            // ignore ... in output
            if (line.indexOf("...") != -1) {
                line = nextLine(in);
                out.append("; ...");
            }

            if (line.indexOf("Address ") != -1) {
                line = line.substring(0, line.indexOf("Address "));
                line += nextLine(in);
            }

            if (isLabel(line)) {
                out.append("\nlabel 0x");
                StringTokenizer st = new StringTokenizer(line);
                out.append(st.nextToken());
                String name = st.nextToken();
                out.append("  " + name.replaceAll("[<,>]", "\"") + '\n');
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
                    out.append('\n');
                }
            }
            line = nextLine(in);
        }
        return line;
    }

    int line_count;

    private String nextLine(BufferedReader in) throws IOException {
        line_count++;
        String line = in.readLine();
        if ( printer.enabled )
            printer.println(StringUtil.leftJustify(line_count,5)+": "+line);
        return line;
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

    public void run(String[] args) throws Exception {
        String fname = FILE.get();
        if ( !"".equals(fname) ) {
            FileOutputStream outf = new FileOutputStream(fname);
            PrintWriter p = new PrintWriter(outf);
            p.write(cleanCode(args[0]).toString());
            p.close();
        } else {
            System.out.println(cleanCode(args[0]));
        }
    }
}
