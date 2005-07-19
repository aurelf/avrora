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

import avrora.syntax.*;
import avrora.util.StringUtil;

import java.util.HashMap;

/**
 * The <code>RawModule</code> is a subclass of <code>Module</code> that allows random access when creating a
 * program from an input source. This is needed since the object dump format is special in that each item it
 * prints has its own address, and can have multiple sections.
 *
 * @author Ben L. Titzer
 */
public class RawModule extends Module {

    private class Section {
        final AbstractToken name;
        final int vma_start;
        final int lma_start;

        Section(AbstractToken n, AbstractToken vma, AbstractToken lma) {
            name = n;
            vma_start = StringUtil.evaluateIntegerLiteral(vma.image);
            lma_start = StringUtil.evaluateIntegerLiteral(lma.image);
        }
    }

    protected Section section;
    protected HashMap sectionMap;

    public RawModule(boolean cs, boolean ba) {
        super(cs, ba);
        sectionMap = new HashMap();
    }

    public void newSection(AbstractToken name, AbstractToken vma, AbstractToken lma) {
        Section s = new Section(name, vma, lma);
        sectionMap.put(name.image, s);
    }

    public void enterSection(AbstractToken sect) {
        section = (Section)sectionMap.get(sect.image);
        segment.setOrigin(section.lma_start);
    }

    public void addBytes(AbstractToken b1, AbstractToken b2) {
        ExprList list = new ExprList();
        list.add(new Expr.Constant(b1));
        list.add(new Expr.Constant(b2));
        addDataBytes(list);
    }

    public void addBytes(AbstractToken b1, AbstractToken b2, AbstractToken b3, AbstractToken b4) {
        ExprList list = new ExprList();
        list.add(new Expr.Constant(b1));
        list.add(new Expr.Constant(b2));
        list.add(new Expr.Constant(b3));
        list.add(new Expr.Constant(b4));
        addDataBytes(list);
    }

    public void setAddress(AbstractToken addr) {
        int address = StringUtil.evaluateIntegerLiteral(addr.image);
        address = (address - section.vma_start) + section.lma_start;
        segment.setOrigin(address);
    }


    protected void simplify(Item i) {
        try {

            i.simplify();

        } catch (Throwable t) {
            // since this is a raw module, we ignore assembling errors
            // such as mismatched instruction problems--these are due to
            // objdump attempting to disassemble all data within the file,
            // even misaligned instructions and raw machine code that might
            // not be valid according to the instruction set specification
        }
    }

}
