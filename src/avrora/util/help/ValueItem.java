package avrora.util.help;

import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.Defaults;

/**
 * @author Ben L. Titzer
 */
public class ValueItem implements HelpItem {

    public final int indent;
    public final String optname;
    public final String optvalue;
    public final String help;

    public ValueItem(int indent, String optname, String optvalue, String help) {
        this.optname = optname;
        this.optvalue = optvalue;
        this.help = help;
        this.indent = indent;
    }

    public String getHelp() {
        return help;
    }

    public void printHelp() {
        Terminal.print(StringUtil.dup(' ', indent));
        Terminal.printPair(Terminal.COLOR_BRIGHT_GREEN, Terminal.COLOR_YELLOW, optname, "=", optvalue);
        Terminal.nextln();
        Terminal.println(StringUtil.makeParagraphs(help, indent+4, 0, Terminal.MAXLINE));
    }

    public HelpCategory getHelpCategory() {
        return null;
    }
}
