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
 *
 * Creation date: Dec 5, 2005
 */

package avrora.syntax.raw;

import cck.text.StringUtil;
import cck.util.Util;
import cck.util.Arithmetic;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.io.*;
import avrora.core.ProgramReader;
import avrora.core.Program;
import avrora.arch.*;
import avrora.Main;

/**
 * @author Ben L. Titzer
 */
public class RAWReader extends ProgramReader {

    protected class Record {
        protected final int addr;
        protected List bytes;
        protected List strings;

        protected Record(int addr) {
            this.addr = addr;
            bytes = new ArrayList(4);
            strings = new ArrayList(1);
        }
    }

    public RAWReader() {
        super("The \"raw\" program format reader reads programs that consist of small records of " +
                "bytes and instructions.");
    }

    public Program read(String[] args) throws Exception {
        if (args.length == 0)
            Util.userError("no input files");
        if (args.length != 1)
            Util.userError("input type \"objdump\" accepts only one file at a time.");
        AbstractArchitecture arch = getArchitecture();
        String fname = args[0];
        List records = parseFile(fname);
        Program p = createProgram(arch, records);
        loadProgram(p, records);
        return p;
    }

    private List parseFile(String fname) throws Exception {
        Main.checkFileExists(fname);
        BufferedReader reader = new BufferedReader(new FileReader(fname));
        List records = new LinkedList();
        while ( true ) {
            String line = reader.readLine();
            if ( line == null ) break;
            Record r = parse(line);
            if ( r != null ) records.add(r);
        }
        return records;
    }

    private Program createProgram(AbstractArchitecture arch, List records) {
        boolean init = false;
        int min = 0;
        int max = 0;
        Iterator i = records.iterator();
        while ( i.hasNext() ) {
            Record r = (Record)i.next();
            if ( !init ) {
                init = true;
                min = r.addr;
                max = r.addr + r.bytes.size();
            } else {
                min = Arithmetic.min(min, r.addr);
                max = Arithmetic.max(max, r.addr + r.bytes.size());
            }
        }
        return new Program(arch, min, max);
    }

    private void loadProgram(Program p, List records) {
        Iterator i = records.iterator();
        while ( i.hasNext() ) {
            Record r = (Record)i.next();
            loadBytes(r, p);
            loadInstr(r, p);
        }
    }

    private void loadBytes(Record r, Program p) {
        int pos = r.addr;
        Iterator b = r.bytes.iterator();
        while ( b.hasNext() ) {
            Byte by = (Byte)b.next();
            p.writeProgramByte(by.byteValue(), pos++);
        }
    }

    private void loadInstr(Record r, Program p) {
        if ( r.strings.size() > 0 ) {
            p.disassembleInstr(r.addr);
        }
    }

    // parses lines such as: 0x0000: 01 02 "instr" ; comment
    protected Record parse(String line) throws Exception {
        CharacterIterator i = new StringCharacterIterator(line);
        StringUtil.skipWhiteSpace(i);
        char ch = i.current();

        if ( ch == CharacterIterator.DONE ) return null; // empty line
        if ( ch == ';' ) return null; // line consists of comment only
        if ( ch != '0' ) Util.userError("syntax error");
        i.next();
        StringUtil.expectChar(i, 'x'); // read the 0x of the address
        int addr = StringUtil.readHexValue(i, 8); // read in the address
        Record record = new Record(addr);

        StringUtil.expectChar(i, ':'); // expect a colon

        while ( true ) { // read in the bytes and strings one by one
            StringUtil.skipWhiteSpace(i);
            ch = i.current();
            if ( StringUtil.isHexDigit(ch) ) readByte(record, i);
            else if ( ch == '"') readString(record, i);
            else if ( ch == ';') break;
            else Util.userError("syntax error");
        }
        return record;
    }

    private void readByte(Record record, CharacterIterator i) {
        record.bytes.add(new Byte((byte)StringUtil.readHexValue(i, 2)));
        if ( !Character.isWhitespace(i.current()) ) Util.userError("constant too long");
    }

    private void readString(Record record, CharacterIterator i) {
        char ch;
        StringBuffer buf = new StringBuffer();
        while ( (ch = i.next()) != CharacterIterator.DONE ) {
            if ( ch == '"' ) { i.next(); break; }
            buf.append(ch);
        }
        record.strings.add(buf.toString());
    }

}
