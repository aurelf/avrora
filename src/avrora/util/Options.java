package avrora.util;

import java.util.*;

/**
 * The <code>Options</code> class represents a collection of command
 * line options and utility methods for parsing the command line. Very
 * useful for getting cheap and powerful parsing of command line options.
 *
 * @author Ben L. Titzer
 */
public class Options {

    protected final HashMap knownValues;
    protected final HashMap unknownValues;

    protected String firstUnknownOption;

    protected String[] arguments;

    public Options() {
        knownValues = new HashMap();
        unknownValues = new HashMap();
    }

    public Option.Bool newOption(String name, boolean val, String desc) {
        Option.Bool o = new Option.Bool(name, val, desc);
        knownValues.put(name, o);
        return o;
    }

    public Option.Str newOption(String name, String val, String desc) {
        Option.Str o = new Option.Str(name, val, desc);
        knownValues.put(name, o);
        return o;
    }

    public Option.List newOptionList(String name, String val, String desc) {
        Option.List o = new Option.List(name, val, desc);
        knownValues.put(name, o);
        return o;
    }

    public Option.Long newOption(String name, long val, String desc) {
        Option.Long o = new Option.Long(name, val, desc);
        knownValues.put(name, o);
        return o;
    }

    public String getOptionValue(String name) {
        Option o = (Option) knownValues.get(name);
        if (o != null) return o.stringValue();
        return (String) unknownValues.get(name);
    }

    public Option getOption(String name) {
        return (Option) knownValues.get(name);
    }

    public boolean hasOption(String name) {
        return knownValues.get(name) != null || unknownValues.get(name) != null;
    }

    public String[] getArguments() {
        return arguments;
    }

    public boolean unknownOptions() {
        return unknownValues.size() > 0;
    }

    public String getFirstUnknownOption() {
        return firstUnknownOption;
    }

    public void parseCommandLine(String[] args) {
        // parse the options
        int cntr = 0;
        for (; cntr < args.length; cntr++) {
            if (args[cntr].charAt(0) != '-') break;
            parseOption(args[cntr]);
        }

        int left = args.length - cntr;

        arguments = new String[left];
        System.arraycopy(args, cntr, arguments, 0, left);
    }

    protected void parseOption(String opt) {
        String optname, value;

        int index = opt.indexOf('=');
        if (index < 0) { // naked option
            optname = opt.substring(1, opt.length());
            value = "";
        } else {
            value = opt.substring(index + 1);
            optname = opt.substring(1, index);
        }

        Option option = (Option) knownValues.get(optname);

        if (option == null) {
            unknownValues.put(optname, value);
            if (firstUnknownOption == null) firstUnknownOption = optname;
        } else
            option.set(value);
    }

    public Collection getAllOptions() {
        return knownValues.values();
    }

    public void dump(String title, Printer p) {
        List opts = Collections.list(Collections.enumeration(knownValues.keySet()));
        Collections.sort(opts, String.CASE_INSENSITIVE_ORDER);
        p.startblock(title);

        int max = 0;

        Iterator i = opts.iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            if (key.length() > max) max = key.length();
        }


        i = opts.iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            p.println(StringUtil.leftJustify(key, max) + " : " + getOptionValue(key));
        }

        p.endblock();
    }
}