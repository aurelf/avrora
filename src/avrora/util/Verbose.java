package avrora.util;

import java.util.HashMap;

/**
 * @author Ben L. Titzer
 */
public class Verbose {

    final static HashMap printerMap = new HashMap();

    public static VerbosePrinter getVerbosePrinter(String category) {
        VerbosePrinter p = getPrinter(category);
        return p;
    }

    public static void setVerbose(String category, boolean on) {
        VerbosePrinter p = getPrinter(category);
        p.verbose = on;
    }

    private static VerbosePrinter getPrinter(String category) {
        VerbosePrinter p = (VerbosePrinter)printerMap.get(category);
        if ( p == null ) {
            p = new VerbosePrinter();
            printerMap.put(category, p);
        }
        return p;
    }

    public static class VerbosePrinter extends Printer {

        boolean verbose;

        VerbosePrinter() {
            super(System.out);
        }

        public void println(String s) {
            if ( verbose ) super.println(s);
        }

        public void print(String s) {
            if ( verbose ) super.print(s);
        }

        public void nextln() {
            if ( verbose ) super.nextln();
        }

        public void indent() {
            if ( verbose ) super.indent();
        }

        public void spaces() {
            if ( verbose ) super.spaces();
        }

        public void unindent() {
            if ( verbose ) super.unindent();
        }

        public void startblock() {
            if ( verbose ) super.startblock();
        }

        public void startblock(String name) {
            if ( verbose ) super.startblock(name);
        }

        public void endblock() {
            if ( verbose ) super.endblock();
        }
    }

}

