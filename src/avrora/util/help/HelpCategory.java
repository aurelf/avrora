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

    public String name;
    public final String help;

    private final LinkedList sections;

    public static final Comparator COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            HelpCategory c1 = (HelpCategory)o1;
            HelpCategory c2 = (HelpCategory)o2;
            return String.CASE_INSENSITIVE_ORDER.compare(c1.name, c2.name);
        }
    };

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
            Terminal.println("");
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
            Terminal.println("");

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
            Terminal.println("");

            Iterator i = list.iterator();
            while (i.hasNext()) {
                HelpItem hi = (HelpItem)i.next();
                hi.printHelp();
            }

            Terminal.println("");
        }
    }

    /**
     * The constructor for the <code>HelpCategory</code> class creates a new help category with the specified
     * short name and the specified default help.
     * @param name the short name of this category (where it is accessible from the command line)
     * @param help the help description for this category
     */
    public HelpCategory(String name, String help) {
        this.name = name;
        this.help = help;
        this.sections = new LinkedList();
    }

    /**
     * The <code>getHelp()</code> method returns a string representing help for this help item.
     * @return a help string for this item
     */
    public String getHelp() {
        return help;
    }

    /**
     * The <code>getName()</code> method returns the short name for this help category. This short name
     * is used to add this help category to the global help category database.
     * @return the short name of this help category
     */
    public String getName() {
        return name;
    }

    /**
     * The <code>setName()</code> method is used to set the short name for this help category.
     * @param nm the new short name for this category
     */
    public void setName(String nm) {
        name = nm;
    }

    /**
     * The <code>addSection()</code> method adds a new section to this help category with the specified title
     * and a paragraph that is automatically formatted when printed out.
     * @param title the title of the new section
     * @param paragraph a long string representing the text for this section
     */
    public void addSection(String title, String paragraph) {
        sections.addLast(new ParagraphSection(title, paragraph));
    }

    /**
     * The <code>addOptionSection()</code> method adds a new section to this help category with the specified
     * options. The new section will contain a paragraph description of the options and list the options in
     * alphabetical order.
     * @param para a summary of the options
     * @param opts the options for this help category
     */
    public void addOptionSection(String para, Options opts) {
        sections.addLast(new OptionsSection(para, opts));
    }

    /**
     * The <code>addListSection()</code> method adds a new section to this help category with the specified
     * list of help items.
     * @param title the title of the new sectiobn
     * @param para a paragraph description of the section
     * @param l a list of <code>HelpItem</code> instances that will be added to the end of the section
     */
    public void addListSection(String title, String para, List l) {
        sections.addLast(new ListSection(title, para, l));
    }

    /**
     * The <code>addSubcategorySection</code> method adds a new section that is a list of subcategories
     * under this main category.
     * @param title the title of the section
     * @param para a paragraph description of this section
     * @param l a list of subcategories
     */
    public void addSubcategorySection(String title, String para, List l) {
        Iterator i = l.iterator();
        LinkedList sl = new LinkedList();
        while ( i.hasNext() ) {
            HelpCategory hc = (HelpCategory)i.next();
            sl.addLast(new SubcategoryItem(4, hc));
        }
        addListSection(title, para, sl);
    }

    /**
     * The <code>addCommandExample()</code> method adds a command example, properly formatted, to
     * this section.
     * @param prefix  a string that is the prefix to the command line
     * @param command the command line example to add
     */
    public void addCommandExample(String prefix, String command) {
        throw Util.unimplemented();
    }

    /**
     * The <code>printHelp()</code> method prints out all of the help sections in order for this category.
     */
    public void printHelp() {
        Iterator i = sections.iterator();
        while ( i.hasNext() ) {
            Section s = (Section)i.next();
            s.printHelp();
        }
    }

}
