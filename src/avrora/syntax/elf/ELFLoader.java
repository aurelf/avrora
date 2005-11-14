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

import avrora.Main;
import avrora.arch.legacy.LegacyDisassembler;
import avrora.arch.legacy.LegacyInstr;
import avrora.core.Program;
import avrora.core.ProgramReader;
import cck.text.*;
import cck.util.Option;
import cck.util.Util;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>ELFLoader</code> class is capable of loading ELF (Executable and Linkable Format)
 * files and disassembling them into the simulator's internal format.
 *
 * @author Ben L. Titzer
 */
public class ELFLoader extends ProgramReader {

    ELFHeader header;
    ELFProgramHeaderTable pht;
    ELFSectionHeaderTable sht;
    List symbolTables;
    ELFStringTable shstrtab;

    protected final Option.Bool DUMP = options.newOption("dump", false,
            "This option causes the ELF loader to dump information about the ELF file being loaded, " +
            "including information the sections and the symbol table.");
    protected final Option.Bool SYMBOLS = options.newOption("load-symbols", true,
            "This option causes the ELF loader to load the symbol table (if it exists) from " +
                    "the ELF file. The symbol table contains information about the names and sizes of " +
                    "data items and functions within the executable. Enabling this option allows for " +
                    "more source-level information during simulation, but disabling it speeds up loading " +
                    "of ELF files.");

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

        RandomAccessFile fis = new RandomAccessFile(fname, "r");

        // read the ELF header
        try {
            readHeader(fis);
        } catch ( ELFHeader.FormatError e) {
            Util.userError(fname, "invalid ELF header");
        }

        // read the program header table (if it exists)
        readProgramHeader(fis);

        // read the section header table (if it exists)
        readSectionHeader(fis);

        // read the symbol tables (if they exist)
        readSymbolTables(fis);

        // load the sections from the ELF file
        return loadSections(fis);
    }

    private Program loadSections(RandomAccessFile fis) throws IOException {
        int minp = Integer.MAX_VALUE;
        int maxp = 0;
        // find the dimensions of the program
        for ( int cntr = 0; cntr < pht.entries.length; cntr++ ) {
            ELFProgramHeaderTable.Entry32 e = pht.entries[cntr];
            if ( e.isLoadable()  && e.p_filesz > 0 ) {
                int start = e.p_paddr;
                int end = start + e.p_filesz;
                if ( start < minp ) minp = start;
                if ( end > maxp ) maxp = end;
            }
        }
        // load each section
        ELFDataInputStream is = new ELFDataInputStream(header, fis);
        Program p = new Program(minp, maxp, 0, 0, 0, 0);
        for ( int cntr = 0; cntr < pht.entries.length; cntr++ ) {
            ELFProgramHeaderTable.Entry32 e = pht.entries[cntr];
            if ( e.isLoadable() && e.p_filesz > 0 ) {
                fis.seek(e.p_offset);
                byte[] sect = is.read_section(e.p_offset, e.p_filesz);
                p.writeProgramBytes(sect, e.p_paddr);
                if ( e.isExecutable() )
                    disassembleSection(sect, e, p);
            }
        }
        return p;
    }

    private void disassembleSection(byte[] sect, ELFProgramHeaderTable.Entry32 e, Program p) {
        LegacyDisassembler d = new LegacyDisassembler();
        for ( int off = 0; off < sect.length; off += 2 ) {
            LegacyInstr i = d.disassembleLegacy(sect, e.p_paddr, off);
            if ( i != null ) p.writeInstr(i, e.p_paddr + off);
        }
    }

    private void readSymbolTables(RandomAccessFile fis) throws IOException {
        symbolTables = new LinkedList();
        if ( !DUMP.get() && SYMBOLS.get() ) return;

        for ( int cntr = 0; cntr < sht.entries.length; cntr++ ) {
            ELFSectionHeaderTable.Entry32 e = sht.entries[cntr];
            if ( e.isSymbolTable() ) {
                ELFSymbolTable stab = new ELFSymbolTable(header, e);
                stab.read(fis);
                symbolTables.add(stab);
                ELFSectionHeaderTable.Entry32 strent = sht.entries[e.sh_link];
                ELFStringTable str = null;
                if ( strent.isStringTable() ) {
                    str = new ELFStringTable(header, strent);
                    str.read(fis);
                    stab.setStringTable(str);
                }
                if ( DUMP.get() ) printSymbolTable(stab, str);
            }
        }
    }

    private void readSectionHeader(RandomAccessFile fis) throws IOException {
        if ( !DUMP.get() && SYMBOLS.get() ) return;
        sht = new ELFSectionHeaderTable(header);
        sht.read(fis);

        // read the ELF string table that contains the section names
        if ( header.e_shstrndx < sht.entries.length ) {
            ELFSectionHeaderTable.Entry32 e = sht.entries[header.e_shstrndx];
            shstrtab = new ELFStringTable(header, e);
            shstrtab.read(fis);
            sht.setStringTable(shstrtab);
        }
        if ( DUMP.get() ) printSHT();
    }

    private void readProgramHeader(RandomAccessFile fis) throws IOException {
        pht = new ELFProgramHeaderTable(header);
        pht.read(fis);
        if ( DUMP.get() ) printPHT();
    }

    private void readHeader(RandomAccessFile fis) throws IOException, ELFHeader.FormatError {
        header = new ELFHeader();
        header.read(fis);

        if ( DUMP.get() ) printHeader(header);
    }

    private void printHeader(ELFHeader header) {
        Terminal.nextln();
        TermUtil.printSeparator();
        Terminal.printGreen("Ver Machine     Arch     Size  Endian");
        Terminal.nextln();
        TermUtil.printThinSeparator();
        Terminal.print(StringUtil.rightJustify(header.e_version, 3));
        Terminal.print(StringUtil.rightJustify(header.e_machine, 8));
        Terminal.print(StringUtil.rightJustify(header.getArchitecture(), 9));
        Terminal.print(StringUtil.rightJustify(header.is64Bit() ? "64 bits" : "32 bits", 9));
        Terminal.print(header.isLittleEndian() ? "  little" : "  big");
        Terminal.nextln();
    }

    private void printSHT() {
        TermUtil.printSeparator(Terminal.MAXLINE, "Section Header Table");
        Terminal.printGreen("Ent  Name                        Type   Address  Offset    Size  Flags");
        Terminal.nextln();
        TermUtil.printThinSeparator();
        for ( int cntr = 0; cntr < sht.entries.length; cntr++ ) {
            ELFSectionHeaderTable.Entry32 e = sht.entries[cntr];
            Terminal.print(StringUtil.rightJustify(cntr, 3));
            Terminal.print("  "+StringUtil.leftJustify(e.getName(), 24));
            Terminal.print(StringUtil.rightJustify(e.getType(), 8));
            Terminal.print("  "+StringUtil.toHex(e.sh_addr, 8));
            Terminal.print(StringUtil.rightJustify(e.sh_offset, 8));
            Terminal.print(StringUtil.rightJustify(e.sh_size, 8));
            Terminal.print("  "+e.getFlags());
            Terminal.nextln();
        }
    }

    private String getName(ELFStringTable st, int ind) {
        if ( st == null ) return "";
        return st.getString(ind);
    }

    private void printPHT() {
        TermUtil.printSeparator(Terminal.MAXLINE, "Program Header Table");
        Terminal.printGreen("Ent     Type  Virtual   Physical  Offset  Filesize  Memsize  Flags");
        Terminal.nextln();
        TermUtil.printThinSeparator();
        for ( int cntr = 0; cntr < pht.entries.length; cntr++ ) {
            ELFProgramHeaderTable.Entry32 e = pht.entries[cntr];
            Terminal.print(StringUtil.rightJustify(cntr, 3));
            Terminal.print(StringUtil.rightJustify(ELFProgramHeaderTable.getType(e), 9));
            Terminal.print("  "+StringUtil.toHex(e.p_vaddr, 8));
            Terminal.print("  "+StringUtil.toHex(e.p_paddr, 8));
            Terminal.print(StringUtil.rightJustify(e.p_offset, 8));
            Terminal.print(StringUtil.rightJustify(e.p_filesz, 10));
            Terminal.print(StringUtil.rightJustify(e.p_memsz, 9));
            Terminal.print("  "+e.getFlags());
            Terminal.nextln();
        }
    }

    private void printSymbolTable(ELFSymbolTable stab, ELFStringTable str) {
        TermUtil.printSeparator(Terminal.MAXLINE, "Symbol Table");
        Terminal.printGreen("Ent  Type     Section     Bind    Name                     Address      Size");
        Terminal.nextln();
        TermUtil.printThinSeparator();
        for ( int cntr = 0; cntr < stab.entries.length; cntr++ ) {
            ELFSymbolTable.Entry e = stab.entries[cntr];
            Terminal.print(StringUtil.rightJustify(cntr, 3));
            Terminal.print("  "+StringUtil.leftJustify(e.getType(), 7));
            Terminal.print("  "+StringUtil.leftJustify(sectionName(e.st_shndx), 12));
            Terminal.print(StringUtil.leftJustify(e.getBinding(), 8));
            Terminal.print(StringUtil.leftJustify(getName(str, e.st_name), 22));
            Terminal.print("  "+StringUtil.toHex(e.st_value, 8));
            Terminal.print("  "+StringUtil.rightJustify(e.st_size, 8));
            Terminal.nextln();
        }
    }

    private String sectionName(int ind) {
        if ( ind < 0 || ind >= sht.entries.length ) return "";
        ELFSectionHeaderTable.Entry32 e = sht.entries[ind];
        return e.getName();
    }
}
