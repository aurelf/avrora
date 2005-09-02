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

import avrora.util.Util;
import java.io.IOException;
import java.io.InputStream;

/**
 * The <code>ELFHeader</code> class represents the header of an ELF file.
 * It can load the header from a file and check the identification, identify
 * the version, endianness, and detect which architecture the file
 * has been created for.
 *
 * @author Ben L. Titzer
 */
public class ELFHeader {

    protected static final int ELFCLASSNONE = 0;
    protected static final int ELFCLASS32   = 1;
    protected static final int ELFCLASS64   = 2;
    protected static final int ELFCLASSNUM  = 3;

    protected static final int EI_NIDENT = 16;

    // constants for information within e_ident section
    protected static final int EI_CLASS = 4;
    protected static final int EI_DATA = 5;
    protected static final int EI_VERSION = 6;
    protected static final int EI_PAD = 7;

    // constants for data format
    protected static final int ELFDATA2LSB = 1;
    protected static final int ELFDATA2MSB = 2;

    public final byte e_ident[];
    public short e_type;
    public short e_machine;
    public int e_version;
    public int e_entry;
    public int e_phoff;
    public int e_shoff;
    public int e_flags;
    public short e_ehsize;
    public short e_phentsize;
    public short e_phnum;
    public short e_shentsize;
    public short e_shnum;
    public short e_shstrndx;

    boolean bigEndian;

    /**
     * The default constructor for the <code>ELFHeader</code> class simply creates a new, unitialized
     * instance of this class that is ready to load.
     */ 
    public ELFHeader() {
        e_ident = new byte[EI_NIDENT];
    }

    /**
     * The <code>read()</code> method reads the header from the specified input stream.
     * It loads the identification section and checks that the header is present by testing against
     * the magic ELF values, and reads the rests of the data section, initializes the ELF section.
     * @param fs the input stream from which to read the ELF header
     * @throws IOException if there is a problem reading from the input stream
     */
    public void read(InputStream fs) throws IOException {
        // read the indentification string
        for ( int index = 0; index < EI_NIDENT; )
            index += fs.read(e_ident, index, EI_NIDENT - index);
        checkIdent();
        e_type = readShort(fs);
        e_machine = readShort(fs);
        e_version = readInt(fs);
        e_entry = readInt(fs);
        e_phoff = readInt(fs);
        e_shoff = readInt(fs);
        e_flags = readInt(fs);
        e_ehsize = readShort(fs);
        e_phentsize = readShort(fs);
        e_phnum = readShort(fs);
        e_shentsize = readShort(fs);
        e_shnum = readShort(fs);
        e_shstrndx = readShort(fs);
    }

    private void checkIdent() {
        checkIndentByte(0, 0x7f);
        checkIndentByte(1, 'E');
        checkIndentByte(2, 'L');
        checkIndentByte(3, 'F');
        bigEndian = isBigEndian();
    }

    private void checkIndentByte(int ind, int val) {
        if ( e_ident[ind] != val ) throw Util.failure("ELF Magic mismatch at byte "+ind);
    }

    private short readShort(InputStream fs) throws IOException {
        int b1 = readByte(fs);
        int b2 = readByte(fs);
        if ( bigEndian ) return asShort(b2, b1);
        return asShort(b1, b2);
    }

    private int readByte(InputStream fs) throws IOException {
        return fs.read() & 0xff;
    }

    private short asShort(int bl, int bh) {
        return (short)((bh << 8) | bl);
    }

    private int readInt(InputStream fs) throws IOException {
        int b1 = readByte(fs);
        int b2 = readByte(fs);
        int b3 = readByte(fs);
        int b4 = readByte(fs);
        if ( bigEndian ) return asInt(b4, b3, b2, b1);
        return asInt(b1, b2, b3, b4);
    }

    private int asInt(int b1, int b2, int b3, int b4) {
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    /**
     * The <code>getVersion()</code> method returns the version of this ELF file. The version
     * number is stored in the identification section of the ELF file at the beginning.
     * @return the version of this ELF file as an integer
     */
    public int getVersion() {
        return e_ident[EI_VERSION];
    }

    /**
     * The <code>getArchitecture()</code> method resolves the name of the architecture
     * from the <code>e_machine</code> file of the structure, using an internal map for
     * well-known architectures.
     * @return a String representation of the architecture name
     */
    public String getArchitecture() {
        return ELFIdentifier.getArchitecture(e_machine);
    }

    /**
     * The <code>isLittleEndian()</code> method checks whether this ELF file is encoded
     * in the little endian format (i.e. least signficant byte first). This information is
     * present in the identification section of the ELF file
     * @return true if this file has the little endian data format; false otherwise
     */
    public boolean isLittleEndian() {
        return e_ident[EI_VERSION] == ELFDATA2LSB;
    }

    /**
     * The <code>isBigEndian()</code> method checks whether this ELF file is encoded
     * in big endian format (i.e. most signficant byte first). This information is present
     * in the identification section of the ELF file.
     * @return true if this file has the big endian data format; false otherwise
     */
    public boolean isBigEndian() {
        return e_ident[EI_VERSION] == ELFDATA2MSB;
    }

    public boolean is32Bit()  {
        return e_ident[EI_CLASS] == ELFCLASS32;
    }

    public boolean is64Bit()  {
        return e_ident[EI_CLASS] == ELFCLASS64;
    }
}
