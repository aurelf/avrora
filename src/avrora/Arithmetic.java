package avrora;

/**
 * The <code>Arithmetic</code> class implements a set of useful methods that
 * are used by the simulator and assembler for converting java types to
 * various data types used by the machine.
 * @author Ben L. Titzer
 */
public class Arithmetic {
    public static short word(byte b1, byte b2) {
        return (short) ((b1 & 0xff) | (b2 << 8));
    }

    public static char uword(byte b1, byte b2) {
        return (char) ((b1 & 0xff) | ((b2 & 0xff) << 8));
    }

    public static char ubyte(byte b1) {
        return (char) (b1 & 0xff);
    }

    public static byte low(short val) {
        return (byte) val;
    }

    public static byte high(short val) {
        return (byte) (val >> 8);
    }

    public static byte low(int val) {
        return (byte) val;
    }

    public static byte high(int val) {
        return (byte) ((val & 0xff00) >> 8);
    }

    public static char ulow(char val) {
        return (char) (val & 0xff);
    }

    public static char uhigh(char val) {
        return (char) (val >> 8);
    }

    public static char ulow(short val) {
        return (char) (val & 0xff);
    }

    public static char uhigh(short val) {
        return (char) ((val & 0xff00) >> 8);
    }

    public static boolean getBit(byte val, int bit) {
        return (val & (1 << bit)) != 0;
    }

    public static boolean getBit(int val, int bit) {
        return (val & (1 << bit)) != 0;
    }

    public static byte setBit(byte val, int bit) {
        return (byte) (val | (1 << bit));
    }

    public static byte setBit(byte val, int bit, boolean on) {
        if (on)
            return setBit(val, bit);
        else
            return clearBit(val, bit);
    }

    public static byte clearBit(byte val, int bit) {
        return (byte) (val & ~(1 << bit));
    }

    public static int lowestBit(long value) {
        int low = 0;
        
        if ( (value & 0xFFFFFFFF) == 0 ) { low += 32; value = value >> 32; }
        if ( (value & 0xFFFF) == 0 ) { low += 16; value = value >> 16; }
        if ( (value & 0xFF) == 0 ) { low += 8; value = value >> 8; }
        if ( (value & 0xF) == 0 ) { low += 4; value = value >> 4; }
        if ( (value & 0x3) == 0 ) { low += 2; value = value >> 2; }
        if ( (value & 0x1) == 0 ) { low += 1; value = value >> 1; }

        return (value == 0) ? -1 : low;
    }
}
