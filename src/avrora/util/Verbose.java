package avrora.util;

import java.util.HashMap;

/**
 * @author Ben L. Titzer
 */
public class Verbose {

    final static HashMap printerMap = new HashMap();

    public static Printer getVerbosePrinter(String category) {
        Printer p = getPrinter(category);
        return p;
    }

    public static void setVerbose(String category, boolean on) {
        Printer p = getPrinter(category);
        p.enabled = on;
    }

    private static Printer getPrinter(String category) {
        Printer p = (Printer)printerMap.get(category);
        if ( p == null ) {
            p = new Printer();
            printerMap.put(category, p);
        }
        return p;
    }

    public static class Printer extends avrora.util.Printer {

        public boolean enabled;

        Printer() {
            super(System.out);
        }

        public void println(String s) {
            if ( enabled ) super.println(s);
        }

        public void print(String s) {
            if ( enabled ) super.print(s);
        }

        public void nextln() {
            if ( enabled ) super.nextln();
        }

        public void indent() {
            if ( enabled ) super.indent();
        }

        public void spaces() {
            if ( enabled ) super.spaces();
        }

        public void unindent() {
            if ( enabled ) super.unindent();
        }

        public void startblock() {
            if ( enabled ) super.startblock();
        }

        public void startblock(String name) {
            if ( enabled ) super.startblock(name);
        }

        public void endblock() {
            if ( enabled ) super.endblock();
        }
    }

}

