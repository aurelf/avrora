package avrora.util;

import avrora.Avrora;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.LinkedList;

/**
 * The <code>Option</code> class represents an option that has been given on the
 * command line. The inner classes represent specific types of options such
 * as integers, booleans, and strings.
 *
 * @author Ben L. Titzer
 * @see Options
 */
public abstract class Option {
    protected final String name;
    protected final String description;

    public Option(String n) {
        name = n;
        description = "";
    }

    public Option(String n, String d) {
        name = n;
        description = d;
    }

    public String getName() {
        return name;
    }

    public abstract void set(String val);

    public abstract String stringValue();

    public abstract void printHelp();

    public void printDescription() {
        Terminal.print(StringUtil.makeJustifiedLines(description, 8, 78));
    }

    public void printHeader(String type, String defvalue) {
        Terminal.printBrightGreen("    -" + name);
        Terminal.print(": ");
        Terminal.printBrightCyan(type);
        Terminal.print(" = ");
        Terminal.printYellow(defvalue);
        Terminal.nextln();
    }

    /**
     * The <code>Option.Long</code> class is an implementation of the
     * <code>Option</code> class that encapsulates a long integer value.
     */
    public static class Long extends Option {
        final long defvalue;
        long value;

        public Long(String nm, long val, String desc) {
            super(nm, desc);
            value = val;
            defvalue = val;
        }

        public boolean set(long val) {
            value = val;
            return true;
        }

        public void set(String val) {
            try {
                value = java.lang.Long.parseLong(val);
            } catch (Exception e) {
                Avrora.userError("Invalid value for long integer option", "-" + name + "=" + val);
            }
        }

        public long get() {
            return value;
        }

        public String stringValue() {
            return "" + value;
        }

        public void printHelp() {
            printHeader("long", "" + defvalue);
            printDescription();
        }
    }

    /**
     * The <code>Option.Str</code> class is an implementation of the
     * <code>Option</code> class that encapsulates a string.
     */
    public static class Str extends Option {
        final String defvalue;
        String value;

        public Str(String nm, String val, String desc) {
            super(nm, desc);
            value = val;
            defvalue = value;
        }

        public void set(String val) {
            value = val;
        }

        public String get() {
            return value;
        }

        public String stringValue() {
            return value;
        }

        public void printHelp() {
            printHeader("string", "" + defvalue);
            printDescription();
        }
    }

    /**
     * The <code>Option.List</code> class is an implementation of the
     * <code>Option</code> class that encapsulates a string.
     */
    public static class List extends Option {
        java.util.List value;
        String orig;

        public List(String nm, String val, String desc) {
            super(nm, desc);
            parseString(val);
        }

        public void set(String val) {
            parseString(val);
        }

        public java.util.List get() {
            return value;
        }

        public String stringValue() {
            return orig;
        }

        private void parseString(String val) {
            orig = val;
            value = new LinkedList();

            if (val.equals("")) return;

            CharacterIterator i = new StringCharacterIterator(val);
            StringBuffer buf = new StringBuffer();
            while (i.current() != CharacterIterator.DONE) {
                if (i.current() == ',') {
                    value.add(buf.toString().trim());
                    buf = new StringBuffer();
                } else {
                    buf.append(i.current());
                }
                i.next();
            }
            value.add(buf.toString().trim());
        }

        public void printHelp() {
            String defvalue = orig.equals("") ? "(null)" : orig;
            printHeader("list", "" + defvalue);
            printDescription();
        }
    }

    /**
     * The <code>Option.Bool</code> class is an implementation of the
     * <code>Option</code> class that encapsulates a boolean.
     */
    public static class Bool extends Option {
        final boolean defvalue;
        boolean value;

        public Bool(String nm, boolean val, String desc) {
            super(nm, desc);
            value = val;
            defvalue = val;
        }

        public void set(boolean val) {
            value = val;
        }

        public void set(String val) {
            if (val.equals("true") || val.equals("")) {
                value = true;
            } else if (val.equals("false")) {
                value = false;
            } else
                Avrora.userError("Invalid value for boolean option", "-" + name + "=" + val);
        }

        public boolean get() {
            return value;
        }

        public String stringValue() {
            return "" + value;
        }

        public void printHelp() {
            printHeader("boolean", "" + defvalue);
            printDescription();
        }
    }
}