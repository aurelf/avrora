package avrora.util;

import vpc.Option;

import java.util.HashMap;
import java.util.HashSet;

/**
 * The <code>Options</code> class represents a collection of command
 * line options and utility methods for parsing the command line. Very
 * useful for getting cheap and powerful parsing of command line options.
 *
 * @author Ben L. Titzer
 */
public class Options {

    protected final HashSet knowns;
    protected final HashMap knownValues;
    protected final HashMap unknownValues;

    protected String firstUnknownOption;

    protected String[] arguments;

    public Options() {
        knowns = new HashSet();
        knownValues = new HashMap();
        unknownValues = new HashMap();
    }

    public Option.Str newOption(String name) {
        knowns.add(name);
        Option.Str o = new Option.Str(name);
        knownValues.put(name, o);
        return o;
    }

    public Option newOption(String name, Option trigger) {
        knowns.add(name);
        knownValues.put(name, trigger);
        return trigger;
    }

    public Option.Bool newOption(String name, boolean val) {
        knowns.add(name);
        Option.Bool o = new Option.Bool(name, val);
        knownValues.put(name, o);
        return o;
    }

    public Option.Str newOption(String name, String val) {
        knowns.add(name);
        Option.Str o = new Option.Str(name, val);
        knownValues.put(name, o);
        return o;
    }

    public Option.Int newOption(String name, int val) {
        knowns.add(name);
        Option.Int o = new Option.Int(name, val);
        knownValues.put(name, o);
        return o;
    }

    public Option.Long newOption(String name, long val) {
        knowns.add(name);
        Option.Long o = new Option.Long(name, val);
        knownValues.put(name, o);
        return o;
    }

    public String getOptionValue(String name) {
        Option o = (Option)knownValues.get(name);
        if ( o != null ) return o.stringValue();
        return (String)unknownValues.get(name);
    }

    public Option getOption(String name) {
        return (Option)knownValues.get(name);
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

        Option option = (Option)knownValues.get(optname);

        if (option == null) {
            unknownValues.put(optname, value);
            if ( firstUnknownOption == null ) firstUnknownOption = optname;
        } else
            option.set(value);
    }
}
