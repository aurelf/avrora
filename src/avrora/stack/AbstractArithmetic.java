package avrora.stack;

/**
 * The <code>AbstractArithmetic</code> arithmetic class implements operations that
 * are useful for working on abstract integers which are represented as characters.
 *
 * The abstract values (e.g. register values) are represented as
 * characters. Thus, an 8 bit register is modelled using a 16-bit
 * character. The upper 8 bits represent the "mask", those bits
 * which are known. The lower 8 bits represent the known bits
 * of the value. Thus, if bit(regs[R], i+8) is set, then bit(R, i)
 * is known and its value is bit(regs[R], i). If bit(regs[R], i+8)
 * is clear, then the value of bit(regs[R], i) is unknown in
 * this abstract value.
 *
 * Since there are 3 possible values (on, off, unknown) for each
 * bit in the abstract state and there are two bits reserved for
 * representing each of these states, there are 4 bit states
 * to represent 3 values. We canonicalize the values when the
 * bit value is unknown, i.e. when the known mask bit is clear,
 * then the value bit is clear as well. This makes comparison
 * of canonical abstract values the same as character equality.
 * All abstract values stored within <code>AbstractState</code>
 * are canonical for efficiency and clarity.
 *
 * @author Ben L. Titzer
 */
public class AbstractArithmetic {
    private static final char KNOWN_MASK = 0xFF00;
    private static final char BIT_MASK = 0x00FF;
    private static final int SHIFT = 8;

    /**
     * The <code>ZERO</code> field represents the abstract value where all bits
     * are known to be zero.
     */
    public static final char ZERO = KNOWN_MASK;

    /**
     * The <code>TRUE</code> field represents the abstract bit that is known
     * to be true.
     */
    public static final char TRUE = 0x101;

    /**
     * The <code>FALSE</code> field represents the abstract bit that is known
     * to be false.
     */
    public static final char FALSE = 0x100;

    /**
     * The <code>UNKNOWN</code> field represents the abstract value where
     * none of the bits are known.
     */
    public static final char UNKNOWN = 0;


    /**
     * The <code>merge()</code> method merges abstract values. The merge of two
     * abstract values is defined intuitively as the intersection of the known
     * bits of the two values that agree, and all other bits are unknown. This
     * variant of the method accepts two concrete values to merge.
     * @param cv1 the first (concrete) value to merge
     * @param cv2 the second (concrete) value to merge
     * @return the abstract value representing the results of merging the two values
     */
    public static char merge(byte cv1, byte cv2) {
        int mm = ~(cv1 ^ cv2);
        return canon((char)mm, (char)cv1);
    }

    /**
     * The <code>merge()</code> method merges abstract values. The merge of two
     * abstract values is defined intuitively as the intersection of the known
     * bits of the two values that agree, and all other bits are unknown. This
     * variant of the method accepts one abstract value and one concrete value
     * to merge together.
     * @param av1 the first (abstract) value to merge
     * @param cv2 the second (concrete) value to merge
     * @return the abstract value representing the results of merging the two values
     */
    public static char merge(char av1, byte cv2) {
        int mm = ~(knownBitsOf(av1) ^ cv2);
        return canon((char)(mm & maskOf(av1)), av1);
    }

    /**
     * The <code>merge()</code> method merges abstract values. The merge of two
     * abstract values is defined intuitively as the intersection of the known
     * bits of the two values that agree, and all other bits are unknown. This
     * variant of the method accepts three concrete values to merge.
     * @param cv1 the first (concrete) value to merge
     * @param cv2 the second (concrete) value to merge
     * @param cv3 the third (concrete) value to merge
     * @return the abstract value representing the results of merging the two values
     */
    public static char merge(byte cv1, byte cv2, byte cv3) {
        return merge(merge(cv1, cv2), cv3);
    }

    /**
     * The <code>merge()</code> method merges abstract values. The merge of two
     * abstract values is defined intuitively as the intersection of the known
     * bits of the two values that agree, and all other bits are unknown. This
     * variant of the method accepts four concrete values to merge.
     * @param cv1 the first (concrete) value to merge
     * @param cv2 the second (concrete) value to merge
     * @param cv3 the third (concrete) value to merge
     * @param cv4 the fourth (concrete) value to merge
     * @return the abstract value representing the results of merging the two values
     */
    public static char merge(byte cv1, byte cv2, byte cv3, byte cv4) {
        return merge(merge(cv1, cv2), merge(cv3, cv4));
    }

    /**
     * The <code>merge()</code> method merges abstract values. The merge of two
     * abstract values is defined intuitively as the intersection of the known
     * bits of the two values that agree, and all other bits are unknown. This
     * variant of the method accepts two abstract values to merge.
     * @param av1 the first (abstract) value to merge
     * @param av2 the second (abstract) value to merge
     * @return the abstract value representing the results of merging the two values
     */
    public static char merge(char av1, char av2) {
        if ( av1 == av2 ) return av1;

        char v1k = maskOf(av1); // known mask of av1
        char v2k = maskOf(av2); // known mask of av2

        int mm = ~(knownBitsOf(av1) ^ knownBitsOf(av2)); // matched bits
        int rk = v1k & v2k & mm & 0xff; // known bits of result

        return canon((char)rk, av1);
    }

    /**
     * The <code>isKnown()</code> method tests whether an abstract value represents
     * a single, fully known value.
     * @param av1 the abstract value to test
     * @return true if all of the bits of the abstract value are known
     * @return false if any bits are unknown
     */
    public static boolean isUnknown(char av1) {
        return (av1 & KNOWN_MASK) != KNOWN_MASK;
    }

    /**
     * The <code>areKnown()</code> method tests whether two abstract values each
     * represent a single, fully known value.
     * @param av1 the first abstract value to test
     * @param av2 the second abstract value to test
     * @return true if all of the bits of the both abstract values are known
     * @return false if any bits are unknown
     */
    public static boolean areKnown(char av1, char av2) {
        return (av1 & av2 & KNOWN_MASK) == KNOWN_MASK;
    }

    /**
     * The <code>areEqual()</code> method tests whether two abstract values are
     * equivalent in the "abstract value" sense. Two abstract values are equivalent
     * if their known bits are equal and their known masks are equal
     * @param val1 the first abstract value
     * @param val2 the second abstract value
     * @return true if the abstract values are equal
     * @return false if teh abstract values are not equal
     */
    public static boolean areEqual(char val1, char val2) {
        if ( val1 == val2 ) return true;
        if ( canon(val1) == canon(val2) ) return true;
        return false;
    }

    /**
     * The <code>canon()</code> method canonicalizes an abstract value. An abstract
     * value is canonical if all of its unknown bits are set to zero. This variant
     * takes a single abstract value and ensures that it is canonical.
     * @param av1 the abstract value to canonicalize
     * @return the canonicalized representation of this abstract value
     */
    public static char canon(char av1) {
        char vk = maskOf(av1);
        return (char)(vk | (av1 & (vk >> SHIFT)));
    }

    /**
     * The <code>canon()</code> method canonicalizes an abstract value. An abstract
     * value is canonical if all of its unknown bits are set to zero. This variant
     * takes a mask and an abstract value and returns an abstract value that is
     * canonical with the specified known bit mask.
     * @param mask the known bit mask to canonicalize with respect to
     * @param av1 the abstract value to canonicalize
     * @return the canonicalized representation of this abstract value
     */
    public static char canon(char mask, char av1) {
        return (char)((mask << SHIFT) | (av1 & mask));
    }

    /**
     * The <code>knownVal()</code> method creates a canonical abstract value from the
     * given concrete value.
     * @param cv1 the concrete value to create an abstract value for
     * @return a canonical abstract value representing the concrete value.
     */
    public static char knownVal(byte cv1) {
        return (char)(KNOWN_MASK | (cv1 & 0xff));
    }

    /**
     * The <code>knownBitsOf()</code> method returns computes the concrete value
     * from the given abstract value where all unknown bits of the abstract value
     * are set to zero.
     * @param val the abstract value to get the known bits of
     * @return a concrete value such that all unknown bits are set to zero
     */
    public static byte knownBitsOf(char val) {
        return (byte)(((val & KNOWN_MASK) >> SHIFT) & val);
    }

    /**
     * The <code>bitsOf()</code> method returns the lower 8 bits (the value bits)
     * of the abstract value, ignoring the known bit mask. For a canonical abstract
     * value, this method will return the same result as <code>knownBitsOf</code>,
     * because, by definition, the unknown bits of a canonical abstract value are set
     * to zero.
     * @param av1 the abstract value
     * @return the lower bits of the abstract value as a concrete value
     */
    public static char bitsOf(char av1) {
        return (char)(av1 & BIT_MASK);
    }

    /**
     * The <code>maskOf()</code> method returns the upper 8 bits of the abstract (the
     * mask bits) of the abstract value. This mask represents those bits that are
     * known.
     * @param av1 the abstract value
     * @return the mask of known bits of the abstract value
     */
    public static char maskOf(char av1) {
        return (char)((av1 & KNOWN_MASK) >> SHIFT);
    }

    /**
     * The <code>getBit()</code> method extracts the specified abstract bit from
     * the specified abstract value.
     * @param av1 the abstract value
     * @param bit the bit number
     * @return <code>AbstractArithmetic.TRUE</code> if the bit is known to be on
     * @return <code>AbstractArithmetic.FALSE</code> if the bit is known to be off
     * @return <code>AbstractArithmetic.UNKNOWN</code> otherwise
     */
    public static char getBit(char av1, int bit) {
        int mask = 0x101 << bit;
        return (char)(av1 & mask);
    }

    /**
     * The <code>setBit()</code> method updates the specified abstract bit within
     * the specified abstract value.
     * @param av1 the abstract value
     * @param bit the bit number
     * @param on the new abstract value of the bit
     * @return a new abstract value where the specified bit has been replaced with
     * the specified abstract value
     */
    public static char setBit(char av1, int bit, char on) {
        int mask = ~(TRUE << bit);
        return (char)((av1 & mask) | (on << bit));
    }

    /**
     * The <code>couldBeZero</code> method performs a "fuzzy" equality test against
     * zero for an abstract value. It will return one of three values, depending on whether
     * the specified abstract value is definately zero, definately not zero, or unknown.
     * @param av1 the abstract value
     * @return <code>AbstractArithmetic.TRUE</code> if the specified abstract value is definately
     * zero
     * @return <code>AbstractArithmetic.FALSE</code> if the specified abstract value cannot
     * possibly be zero (it contains one bit that is known to be on)
     * @return <code>AbstractArithmetic.UNKNOWN</code> otherwise
     */
    public static char couldBeZero(char av1) {
        if ( av1 == ZERO ) return TRUE;
        if ( knownBitsOf(av1) != 0 ) return FALSE;
        return UNKNOWN;
    }

    /**
     * The <code>couldBeZero</code> method performs a "fuzzy" equality test against
     * zero for two abstract values. It will return one of three values, depending on whether
     * the specified abstract values are definately zero, definately not zero, or unknown.
     * @param av1 the first abstract value
     * @param av2 the second abstract value
     * @return <code>AbstractArithmetic.TRUE</code> if the both abstract values are definately
     * zero
     * @return <code>AbstractArithmetic.FALSE</code> if either the specified abstract values cannot
     * possibly be zero (it contains one bit that is known to be on)
     * @return <code>AbstractArithmetic.UNKNOWN</code> otherwise
     */
    public static char couldBeZero(char av1, char av2) {
        if ( av1 == ZERO && av2 == ZERO ) return TRUE;
        if ( knownBitsOf(av1) != 0 || knownBitsOf(av2) != 0 ) return FALSE;
        return UNKNOWN;
    }

    public static char couldBeEqual(char av1, char av2) {
        if ( areKnown(av1, av2) && av1 == av2 ) return TRUE;
        if ( knownBitsOf(av1) != knownBitsOf(av2) ) return FALSE;
        return UNKNOWN;
    }

    public static char commonMask(char av1, char av2) {
        return (char)(maskOf(av1) & maskOf(av2));
    }

    public static char logicalOr(char av1, char av2) {
        return canon(commonMask(av1, av2), (char)(av1 | av2));
    }

    public static char logicalAnd(char av1, char av2) {
        return (char)(av1 & av2 & TRUE);
    }

    public static char add(char av1, char av2) {
        char common = commonMask(av1, av2);
        // TODO: optimize for common case of known / unknown values.
        int resultA = ceiling(av1)+ceiling(av2);
        int resultB = floor(av1)+floor(av2);

        return mergeMask(common, merge((byte)resultA, (byte)resultB));
    }

    public static char subtract(char av1, char av2) {
        char common = commonMask(av1, av2);
        // TODO: optimize for common case of known / unknown values.
        int resultA = ceiling(av1) - ceiling(av2);
        int resultB = floor(av1) - floor(av2);

        return mergeMask(common, merge((byte)resultA, (byte)resultB));
    }

    public static char increment(char av1) {
        char mask = maskOf(av1);
        // TODO: optimize for common case of known / unknown values.
        int resultA = ceiling(av1) + 1;
        int resultB = floor(av1) + 1;
        return mergeMask(mask, merge((byte)resultA, (byte)resultB));
    }

    public static char decrement(char av1) {
        char mask = maskOf(av1);
        // TODO: optimize for common case of known / unknown values.
        int resultA = ceiling(av1) - 1;
        int resultB = floor(av1) - 1;
        return mergeMask(mask, merge((byte)resultA, (byte)resultB));
    }

    public static char mergeMask(char mask, char av1) {
        char common = (char)(mask & maskOf(av1));
        return canon(common, av1);
    }

    public static char xor(char av1, char av2) {
        char mask = AbstractArithmetic.commonMask(av1, av2);
        return AbstractArithmetic.canon(mask, (char)(av1 ^ av2));
    }

    public static char and(char av1, char av2) {
        return AbstractArithmetic.logicalAnd(av1, av2);
    }

    public static char or(char av1, char av2) {
        return AbstractArithmetic.logicalOr(av1, av2);
    }

    public static char and(char av1, char av2, char av3) {
        return AbstractArithmetic.logicalAnd(AbstractArithmetic.logicalAnd(av1, av2), av3);
    }

    public static char or(char av1, char av2, char av3) {
        return AbstractArithmetic.logicalOr(AbstractArithmetic.logicalOr(av1, av2), av3);
    }

    public static char not(char av1) {
        return (char)(av1 ^ 0x01);
    }

    public static int ceiling(char av1) {
        int invmask = (~AbstractArithmetic.maskOf(av1)) & 0xff;
        return bitsOf(av1) | invmask;
    }

    public static int ceiling(char av1, char av2) {
        return ceiling(av1) | (ceiling(av2) << 8);
    }

    public static int floor(char av1) {
        return bitsOf(av1);
    }

    public static int floor(char av1, char av2) {
        return bitsOf(av1) | (bitsOf(av2) << 8);
    }

    public static char shiftLeftOne(char av1) {
        return (char) ((av1 & 0x7f7f) << 1);
    }

    public static char shiftLeftOne(char av1, char lowbit) {
        return (char) (((av1 & 0x7f7f) << 1) | (lowbit & TRUE));
    }
}
