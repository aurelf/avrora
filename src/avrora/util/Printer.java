package avrora.util;

import vpc.VPCBase;

import java.io.PrintStream;

public class Printer extends VPCBase {

    private final PrintStream o;
    private boolean begLine;
    private int indent;

    public static final Printer STDOUT = new Printer(System.out);
    public static final Printer STDERR = new Printer(System.out);

    public Printer(PrintStream o) {
        this.o = o;
        this.begLine = true;
    }

    public void println(String s) {
        spaces();
        o.println(s);
        begLine = true;
    }

    public void print(String s) {
        spaces();
        o.print(s);
    }

    public void nextln() {
        if (!begLine) {
            o.print("\n");
            begLine = true;
        }
    }

    public void indent() {
        indent++;
    }

    public void spaces() {
        if (begLine) {
            for (int cntr = 0; cntr < indent; cntr++)
                o.print("    ");
            begLine = false;
        }
    }

    public void unindent() {
        indent--;
        if (indent < 0) indent = 0;
    }

    public void startblock() {
        println("{");
        indent();
    }

    public void startblock(String name) {
        println(name + " {");
        indent();
    }

    public void endblock() {
        unindent();
        println("}");
    }
}
