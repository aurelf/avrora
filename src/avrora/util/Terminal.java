package avrora.util;

import java.io.PrintStream;

/**
 * The <code>Terminal</code> class provides Avrora with the
 * ability to print color on the terminal by using control characters. The
 * portability of these particular control sequences is not guaranteed,
 * but seem to work most places a color terminal is supported.
 *
 * @author Ben L. Titzer
 */
public final class Terminal {

    public static boolean useColors = true;
    private static PrintStream out = System.out;

    public static final int COLOR_BLACK = 0;
    public static final int COLOR_RED = 1;
    public static final int COLOR_GREEN = 2;
    public static final int COLOR_BROWN = 3;
    public static final int COLOR_BLUE = 4;
    public static final int COLOR_PURPLE = 5;
    public static final int COLOR_CYAN = 6;
    public static final int COLOR_LIGHTGRAY = 7;

    public static final int COLOR_DARKGRAY = 8;
    public static final int COLOR_BRIGHT_RED = 9;
    public static final int COLOR_BRIGHT_GREEN = 10;
    public static final int COLOR_YELLOW = 11;
    public static final int COLOR_BRIGHT_BLUE = 12;
    public static final int COLOR_MAGENTA = 13;
    public static final int COLOR_BRIGHT_CYAN = 14;
    public static final int COLOR_WHITE = 15;

    public static final int MAXCOLORS = 16;

    private static final String CTRL_BLACK = "[0;30m";
    private static final String CTRL_RED = "[0;31m";
    private static final String CTRL_GREEN = "[0;32m";
    private static final String CTRL_BROWN = "[0;33m";
    private static final String CTRL_BLUE = "[0;34m";
    private static final String CTRL_PURPLE = "[0;35m";
    private static final String CTRL_CYAN = "[0;36m";
    private static final String CTRL_LIGHTGRAY = "[0;37m";

    private static final String CTRL_DARKGRAY = "[1;30m";
    private static final String CTRL_BRIGHT_RED = "[1;31m";
    private static final String CTRL_BRIGHT_GREEN = "[1;32m";
    private static final String CTRL_YELLOW = "[1;33m";
    private static final String CTRL_BRIGHT_BLUE = "[1;34m";
    private static final String CTRL_MAGENTA = "[1;35m";
    private static final String CTRL_BRIGHT_CYAN = "[1;36m";
    private static final String CTRL_WHITE = "[1;37m";

    private static final String[] COLORS = {
        CTRL_BLACK,
        CTRL_RED,
        CTRL_GREEN,
        CTRL_BROWN,
        CTRL_BLUE,
        CTRL_PURPLE,
        CTRL_CYAN,
        CTRL_LIGHTGRAY,
        CTRL_DARKGRAY,
        CTRL_BRIGHT_RED,
        CTRL_BRIGHT_GREEN,
        CTRL_YELLOW,
        CTRL_BRIGHT_BLUE,
        CTRL_MAGENTA,
        CTRL_BRIGHT_CYAN,
        CTRL_WHITE
    };


    public static final String ERROR_COLOR = CTRL_RED;

    public static void print(int color, String s) {
        if (color >= MAXCOLORS) throw new IllegalArgumentException("invalid color");
        outputColor(COLORS[color], s);
    }

    public static void println(int color, String s) {
        if (color >= MAXCOLORS) throw new IllegalArgumentException("invalid color");
        outputColor(COLORS[color], s);
        out.print('\n');
    }

    public static void print(String s) {
        out.print(s);
    }

    public static void println(String s) {
        out.println(s);
    }

    public static void nextln() {
        out.print("\n");
    }

    public static void setOutput(PrintStream s) {
        out = s;
    }

    public static void printRed(String s) {
        outputColor(CTRL_RED, s);
    }

    public static void printBlue(String s) {
        outputColor(CTRL_BLUE, s);
    }

    public static void printGreen(String s) {
        outputColor(CTRL_GREEN, s);
    }

    public static void printYellow(String s) {
        outputColor(CTRL_YELLOW, s);
    }

    public static void printCyan(String s) {
        outputColor(CTRL_CYAN, s);
    }

    public static void printBrightRed(String s) {
        outputColor(CTRL_BRIGHT_RED, s);
    }

    public static void printBrightBlue(String s) {
        outputColor(CTRL_BRIGHT_BLUE, s);
    }

    public static void printBrightGreen(String s) {
        outputColor(CTRL_BRIGHT_GREEN, s);
    }

    public static void printBrightCyan(String s) {
        outputColor(CTRL_BRIGHT_CYAN, s);
    }

    public static void printColor(String s, String c) {
        outputColor(c, s);
    }

    public static void printSeparator(int width) {
        while (width > 0) {
            Terminal.print("=");
            width--;
        }
        Terminal.nextln();
    }

    private static void outputColor(String termchars, String s) {
        if (useColors) {
            out.print(termchars);
            out.print(s);
            // TODO: get correct begin color from terminal somehow
            out.print(CTRL_LIGHTGRAY);
        } else
            out.print(s);
    }

}
