package avrora.stack;

/**
 * The <code>AbstractArithmetic</code> arithmetic class implements operations that
 * are useful for working on abstract integers which are represented as characters.
 * @author Ben L. Titzer
 */
public class AbstractArithmetic {
    private static final char KNOWN_MASK = 0xFF00;
    private static final char BIT_MASK = 0x00FF;
    private static final int SHIFT = 8;
    public static final char ZERO = KNOWN_MASK;
    public static final char ON = 0x101;
    public static final char OFF = 0x100;
    public static final char UNKNOWN = 0;

    /**
     *  O P E R A T I O N S   O N   A B S T R A C T   V A L U E S
     * -----------------------------------------------------------------
     *
     *    Abstract values are represented as characters. These utility
     * functions allow operations on abstract values to be expressed
     * more clearly.
     *
     */

    public static char merge(byte val1, byte val2) {
        int mm = ~(val1 ^ val2);
        return canon((char)mm, (char)val1);
    }

    public static char merge(char val1, byte val2) {
        int mm = ~(knownBitsOf(val1) ^ val2);
        return canon((char)(mm & maskOf(val1)), val1);
    }

    public static char merge(byte val1, byte val2, byte val3) {
        return merge(merge(val1, val2), val3);
    }

    public static char merge(byte val1, byte val2, byte val3, byte val4) {
        return merge(merge(val1, val2), merge(val3, val4));
    }

    public static char merge(char val1, char val2) {
        if ( val1 == val2 ) return val1;

        char v1k = maskOf(val1); // known mask of val1
        char v2k = maskOf(val2); // known mask of val2

        int mm = ~(knownBitsOf(val1) ^ knownBitsOf(val2)); // matched bits
        int rk = v1k & v2k & mm & 0xff; // known bits of result

        return canon((char)rk, val1);
    }

    public static boolean isUnknown(char val) {
        return (val & KNOWN_MASK) != KNOWN_MASK;
    }

    public static boolean areEqual(char val1, char val2) {
        if ( val1 == val2 ) return true;
        if ( canon(val1) == canon(val2) ) return true;
        return false;
    }

    public static char canon(char val) {
        char vk = maskOf(val);
        return (char)(vk | (val & (vk >> SHIFT)));
    }

    public static char canon(char vk, char val) {
        return (char)((vk << SHIFT) | (val & vk));
    }

    public static char knownVal(byte val) {
        return (char)(KNOWN_MASK | (val & 0xff));
    }

    public static byte knownBitsOf(char c) {
        return (byte)(((c & KNOWN_MASK) >> SHIFT) & c);
    }

    public static char bitsOf(char c) {
        return (char)(c & BIT_MASK);
    }

    public static char maskOf(char c) {
        return (char)((c & KNOWN_MASK) >> SHIFT);
    }

    public static char getBit(char val, int bit) {
        int mask = 0x101 << bit;
        return (char)(val & mask);
    }

    public static char setBit(char val, int bit, char on) {
        int mask = ~(ON << bit);
        return (char)((val & mask) | (on << bit));
    }

    public static char couldBeZero(char val) {
        if ( val == ZERO ) return ON;
        if ( knownBitsOf(val) != 0 ) return OFF;
        return UNKNOWN;
    }

    public static char couldBeEqual(char v1, char v2) {
        if ( v1 == v2 ) return ON;
        if ( knownBitsOf(v1) != knownBitsOf(v2) ) return OFF;
        return UNKNOWN;
    }

    public static char commonMask(char c, char d) {
        return (char)(maskOf(c) & maskOf(d));
    }

    public static char logicalOr(char c, char d) {
        return canon(commonMask(c, d), (char)(c | d));
    }

    public static char logicalAnd(char c, char d) {
        return (char)(c & d & ON);
    }
}
