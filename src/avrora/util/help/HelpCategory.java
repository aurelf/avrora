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

import avrora.Avrora;
import avrora.util.*;

import java.util.*;

/**
 * The <code>HelpCategory</code> class implements a category that provides help from the
 * command line. It can contain multiple sections, including sections on options, and a list
 * of other help items (such as what the values of each option does).
 *
 * @author Ben L. Titzer
 */
public class HelpCategory implements HelpItem {

    public final String name;
    public final String help;

    private LinkedList sections;

    private abstract class Section {
        abstract void printHelp();
    }

    private class ParagraphSection extends Section {
        final String title;
        final String para;

        ParagraphSection(String t, String p) {
            title = t;
            para = p;
        }

        void printHelp() {
            if ( title != null ) {
                Terminal.printBrightBlue(title);
                Terminal.println("\n");
            }

            Terminal.println(StringUtil.makeParagraphs(para, 0, 4, Terminal.MAXLINE));
        }
    }

    private class OptionsSection extends Section {
        final String para;
        final Options options;

        OptionsSection(String para, Options opts) {
            this.para = para;
            this.options = opts;
        }

        void printHelp() {
            Terminal.printBrightBlue("OPTIONS");
            Terminal.println("\n");

            Terminal.println(StringUtil.makeParagraphs(para, 0, 4, Terminal.MAXLINE));

            Collection c = options.getAllOptions();
            List l = Collections.list(Collections.enumeration(c));
            Collections.sort(l, Option.COMPARATOR);

            Iterator i = l.iterator();
            while (i.hasNext()) {
                Option opt = (Option)i.next();
                opt.printHelp();
            }

            Terminal.println("");
        }
    }

    private class ListSection extends Section {
        final String title;
        final String para;
        final List list;

        ListSection(String t, String p, List l) {
            title = t;
            para = p;
            list = l;
        }

        void printHelp() {
            if ( title != null ) {
                Terminal.printBrightBlue(title);
                Terminal.println("\n");
            }

            Terminal.println(StringUtil.makeParagraphs(para, 0, 4, Terminal.MAXLINE));

            Iterator i = list.iterator();
            while (i.hasNext()) {
                HelpItem hi = (HelpItem)i.next();
                hi.printHelp();
            }

            Terminal.println("");
        }
    }

    public HelpCategory(String name, String help) {
        this.name = name;
        this.help = help;
    }

    public String getHelp() {
        return help;
    }

    public void addSection(String title, String paragraph) {
        sections.addLast(new ParagraphSection(title, paragraph));
    }

    public void addOptionSection(String para, Options opts) {
        sections.addLast(new OptionsSection(para, opts));
    }

    public void addListSection(String title, String para, List l) {
        sections.addLast(new ListSection(title, para, l));
    }

    public void addCommandExample(String prefix, String command) {
        throw Avrora.unimplemented();
    }

    public void printHelp() {
        Iterator i = sections.iterator();
        while ( i.hasNext() ) {
            Section s = (Section)i.next();
            s.printHelp();
        }
    }

    public HelpCategory getHelpCategory() {
        return this;
    }
}
