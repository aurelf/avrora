package avrora.util.help;

import avrora.util.Terminal;
import avrora.util.StringUtil;
import avrora.util.ClassMap;
import avrora.Defaults;

/**
 * @author Ben L. Titzer
 */
public class ClassMapValueItem implements HelpItem {

    public final int indent;
    public final String optname;
    public final String optvalue;
    public final ClassMap map;
    protected String help;

    public ClassMapValueItem(int indent, String optname, String optvalue, ClassMap map) {
        this.optname = optname;
        this.optvalue = optvalue;
        this.map = map;
        this.indent = indent;
    }

    public String getHelp() {
        if ( help != null )
            return help;
        else return computeHelp();
    }

    public void printHelp() {
        String h = getHelp();
        Terminal.print(StringUtil.dup(' ', indent));
        Terminal.printPair(Terminal.COLOR_BRIGHT_GREEN, Terminal.COLOR_YELLOW, optname, "=", optvalue);
        Terminal.nextln();
        Terminal.println(StringUtil.makeParagraphs(h, indent+4, 0, Terminal.MAXLINE));
    }

    private String computeHelp() {
        try {
            help = ((HelpItem)map.getObjectOfClass(optvalue)).getHelp();
        } catch ( Throwable t ) {
            return "(No help available for this item.)";
        }
        return help;
    }

    public HelpCategory getHelpCategory() {
        return null;
    }
}
