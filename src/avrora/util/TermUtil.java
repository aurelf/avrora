
package avrora.util;

/**
 * @author Ben L. Titzer
 */
public class TermUtil {
    /**
     * The <code>reportQuantity()</code> method is a simply utility to print out a quantity's name
     * (such as "Number of instructions executed", the value (such as 2002), and the units (such as
     * cycles) in a colorized and standardized way.
     * @param name the name of the quantity as a string
     * @param val the value of the quantity as a long integer
     * @param units the name of the units as a string
     */
    public static void reportQuantity(String name, long val, String units) {
        reportQuantity(name, Long.toString(val), units);
    }

    /**
     * The <code>reportProportion()</code> method is a simply utility to print out a quantity's name
     * (such as "Number of instructions executed", the value (such as 2002), and the units (such as
     * cycles) in a colorized and standardized way.
     * @param name the name of the quantity as a string
     * @param val the value of the quantity as a long integer
     * @param units the name of the units as a string
     */
    public static void reportProportion(String name, long val, long total, String units) {
        String sval = Long.toString(val);
        Terminal.printGreen(name);
        Terminal.print(": ");
        Terminal.printBrightCyan(sval);
        if ( units != null && units.length() > 0)
            Terminal.print(' ' + units + ' ');
        else
            Terminal.print(" ");
        float pcnt = (100 * (float)val / total);

        Terminal.printBrightCyan(StringUtil.toFixedFloat(pcnt, 4));
        Terminal.println(" %");
    }

    /**
     * The <code>reportQuantity()</code> method is a simply utility to print out a quantity's name
     * (such as "Number of instructions executed", the value (such as 2002), and the units (such as
     * cycles) in a colorized and standardized way.
     * @param name the name of the quantity as a string
     * @param val the value of the quantity as a floating point number
     * @param units the name of the units as a string
     */
    public static void reportQuantity(String name, float val, String units) {
        reportQuantity(name, Float.toString(val), units);
    }

    /**
     * The <code>reportQuantity()</code> method is a simply utility to print out a quantity's name
     * (such as "Number of instructions executed", the value (such as 2002), and the units (such as
     * cycles) in a colorized and standardized way.
     * @param name the name of the quantity as a string
     * @param val the value of the quantity as a string
     * @param units the name of the units as a string
     */
    public static void reportQuantity(String name, String val, String units) {
        Terminal.printGreen(name);
        Terminal.print(": ");
        Terminal.printBrightCyan(val);
        Terminal.println(' ' + units);
    }

    public static void printSeparator(int width) {
        Terminal.println(StringUtil.dup('=', width));
    }

    public static void printThinSeparator(int width) {
        Terminal.println(StringUtil.dup('-', width));
    }

    public static void printSeparator(int width, String header) {
        Terminal.print("=={ ");
        Terminal.print(header);
        Terminal.print(" }");
        Terminal.print(StringUtil.dup('=', width - 6 - header.length()));
        Terminal.nextln();
    }


}
