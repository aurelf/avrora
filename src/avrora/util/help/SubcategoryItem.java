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

package avrora.util.help;

import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.util.ClassMap;
import avrora.Defaults;

/**
 * @author Ben L. Titzer
 */
public class SubcategoryItem implements HelpItem {

    public final int indent;
    public final HelpCategory helpCat;
    protected String help;

    public SubcategoryItem(int indent, HelpCategory hc) {
        this.helpCat = hc;
        this.indent = indent;
    }

    public String getHelp() {
        return helpCat.getHelp();
    }

    public void printHelp() {
        String h = getHelp();
        Terminal.print(StringUtil.dup(' ', indent));
        Terminal.printPair(Terminal.COLOR_BRIGHT_GREEN, Terminal.COLOR_YELLOW, "-help", " ", helpCat.name);
        Terminal.nextln();
        Terminal.println(StringUtil.makeParagraphs(h, indent+4, 0, Terminal.MAXLINE));
    }
}
