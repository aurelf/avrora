package avrora.util;

import avrora.Avrora;

/**
 * The <code>Option</code> class represents an option that has been supplied
 * to the compiler through the command or other means.
 * @author Ben L. Titzer
 */
public abstract class Option {
    protected String name;

    public abstract void set(String val);

    public abstract String stringValue();

    public static class Int extends Option {
        int value;

        public Int(String nm) {
            name = nm;
        }

        public Int(String nm, int val) {
            name = nm;
            value = val;
        }

        public boolean set(int val) {
            value = val;
            return true;
        }

        public void set(String val) {
            try {
                value = (new Integer(val)).intValue();
            } catch (Exception e) {
                throw new Avrora.Error("Invalid value for integer option", "-" + name + "=" + val);
            }
        }

        public int get() {
            return value;
        }

        public String stringValue() {
            return "" + value;
        }
    }

    public static class Long extends Option {
        long value;

        public Long(String nm) {
            name = nm;
        }

        public Long(String nm, long val) {
            name = nm;
            value = val;
        }

        public boolean set(long val) {
            value = val;
            return true;
        }

        public void set(String val) {
            try {
                value = java.lang.Long.parseLong(val);
            } catch (Exception e) {
                throw new Avrora.Error("Invalid value for long integer option", "-" + name + "=" + val);
            }
        }

        public long get() {
            return value;
        }

        public String stringValue() {
            return "" + value;
        }
    }

    public static class Str extends Option {
        String value;

        public Str(String nm) {
            name = nm;
        }

        public Str(String nm, String val) {
            name = nm;
            value = val;
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
    }

    public static class Bool extends Option {
        boolean value;

        public Bool(String nm) {
            name = nm;
        }

        public Bool(String nm, boolean val) {
            name = nm;
            value = val;
        }

        public void set(boolean val) {
            value = val;
        }

        public void set(String val) {
            if (val.equals("true") || val.equals("") ) {
                value = true;
            } else if (val.equals("false")) {
                value = false;
            } else
                throw new Avrora.Error("Invalid value for boolean option", "-" + name + "=" + val);
        }

        public boolean get() {
            return value;
        }

        public String stringValue() {
            return "" + value;
        }
    }
}
