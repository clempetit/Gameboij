/**
 *  @author Clément Petit (282626)
 *  @author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.bits;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

public final class Bits {

    private static final int[] tab = new int[] { 0x00, 0x80, 0x40, 0xC0, 0x20,
            0xA0, 0x60, 0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0,
            0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58,
            0xD8, 0x38, 0xB8, 0x78, 0xF8, 0x04, 0x84, 0x44, 0xC4, 0x24, 0xA4,
            0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74, 0xF4, 0x0C,
            0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C, 0x9C, 0x5C, 0xDC,
            0x3C, 0xBC, 0x7C, 0xFC, 0x02, 0x82, 0x42, 0xC2, 0x22, 0xA2, 0x62,
            0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2, 0x72, 0xF2, 0x0A, 0x8A,
            0x4A, 0xCA, 0x2A, 0xAA, 0x6A, 0xEA, 0x1A, 0x9A, 0x5A, 0xDA, 0x3A,
            0xBA, 0x7A, 0xFA, 0x06, 0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6,
            0x16, 0x96, 0x56, 0xD6, 0x36, 0xB6, 0x76, 0xF6, 0x0E, 0x8E, 0x4E,
            0xCE, 0x2E, 0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE,
            0x7E, 0xFE, 0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1, 0x11,
            0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89, 0x49, 0xC9,
            0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39, 0xB9, 0x79,
            0xF9, 0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65, 0xE5, 0x15, 0x95,
            0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D, 0x8D, 0x4D, 0xCD, 0x2D,
            0xAD, 0x6D, 0xED, 0x1D, 0x9D, 0x5D, 0xDD, 0x3D, 0xBD, 0x7D, 0xFD,
            0x03, 0x83, 0x43, 0xC3, 0x23, 0xA3, 0x63, 0xE3, 0x13, 0x93, 0x53,
            0xD3, 0x33, 0xB3, 0x73, 0xF3, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB,
            0x6B, 0xEB, 0x1B, 0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB, 0x07,
            0x87, 0x47, 0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57, 0xD7,
            0x37, 0xB7, 0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F,
            0xEF, 0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF, };

    private Bits() {
    }

    /**
     * return an integer which has only the bit of given index equal to 1.
     * 
     * @param index
     *            the index (must be included between 0 and 31)
     * @throws IndexOutOfBoundsException
     *             if the index is invalid
     * @return an integer which has only the bit of index given equal to 1
     */
    public static int mask(int index) {
        index = Objects.checkIndex(index, Integer.SIZE);
        return 1 << index;
    }

    /**
     * return true if and only if the bit of given index of the integer bits
     * equals 1.
     * 
     * @param bits
     *            the integer
     * @param index
     *            the index (must be included between 0 and 31)
     * @throws IndexOutOfBoundsException
     *             if the index is invalid
     * @return true if and only if the bit of given index of the integer bits
     *         equals 1
     */
    public static boolean test(int bits, int index) {
        index = Objects.checkIndex(index, Integer.SIZE);
        int mask = mask(index);
        return (bits & mask) == mask;
    }

    /**
     * gets the index to test of the given bit and return true if and only if
     * it's equal to 1.
     * 
     * @param bits
     *            the integer
     * @param bit
     *            the bit
     * @return true if and only if the bit of the found index of the integer
     *         bits equals 1
     */
    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.index());
    }

    /**
     * return an integer which has the same bits than the given integer bits,
     * except the one of given index which takes a value depending on newValue.
     * 
     * @param bits
     *            the integer
     * @param index
     *            the index (must be included between 0 and 31)
     * @param newValue
     *            the new value (false is 0 and true is 1)
     * @throws IndexOutOfBoundsException
     *             if the index is invalid
     * @return an integer that has the same bits than the given integer bits
     *         except the one of given index which takes a value depending on
     *         the boolean given
     */
    public static int set(int bits, int index, boolean newValue) {
        index = Objects.checkIndex(index, Integer.SIZE);
        int mask = mask(index);
        if (newValue) {
            return bits | mask;
        } else {
            return bits & ~mask;
        }
    }

    /**
     * return an integer whose size least significant bits are equal to those of
     * the given integer bits, and the others are 0.
     * 
     * @param size
     *            the number of bits to copy from the given integer bits (must
     *            be included between 0 and 32)
     * @param bits
     *            the integer
     * @throws IllegalArgumentException
     *             if size is invalid
     * @return an integer whose size least significant bits are equal to those of
     *         the given integer bits and the others are 0
     */
    public static int clip(int size, int bits) {
        if (size == Integer.SIZE) {
            return bits;
        }
        Preconditions.checkArgument(size >= 0 && size <= Integer.SIZE);
        return bits & (mask(size) - 1);
    }

    /**
     * return an integer whose size least significant bits are equal to those
     * from the index start (included) to the index start+size (excluded) of the
     * given integer bits, and the others are 0.
     * 
     * @param bits
     *            the integer
     * @param start
     *            the index from which we start to copy the given integer bits
     * @param size
     *            the number of bits to copy from the given integer bits
     * @throws IndexOutOfBoundsException
     *             if start+size is not included between 0 and 32
     * @return an integer whose size least significant bits are equal to those
     *         from the index start (included) to the index start+size
     *         (excluded) of the given integer bits
     */
    public static int extract(int bits, int start, int size) {
        start = Objects.checkFromIndexSize(start, size, Integer.SIZE);
        if (size == Integer.SIZE) {
            return bits;
        }
        return clip(size, (bits >> start));
    }

    /**
     * return an integer whose size least significant bits are equal to those of
     * the given integer bits, but to which a rotation of the distance has been
     * applied; if the distance is positive the rotation is to the left, and to
     * the right otherwise .
     * 
     * @param size
     *            the number of bits to copy from the given integer bits (must
     *            be included between 0 and 32)
     * @param bits
     *            the integer
     * @param distance
     *            the distance (must be a size bits value)
     * @throws IllegalArgumentException
     *             if size or distance is invalid
     * @return return an integer whose size least significant bits are equal to
     *         those of the given integer bits but to which a rotation of the
     *         distance has been applied
     */
    public static int rotate(int size, int bits, int distance) {
        Preconditions.checkArgument(size > 0 && size <= Integer.SIZE);
        Preconditions.checkArgument(clip(size, bits) == bits);
        int reducedDistance = Math.floorMod(distance, size);
        int rotatedBits = (bits << reducedDistance)
                | (bits >>> (size - reducedDistance));
        return clip(size, rotatedBits);
    }

    /**
     * extends the sign of the given 8 bits value.
     * 
     * @param b
     *            the integer (must be an 8 bits value)
     * @throws IllegalArgumentException
     *             if b is invalid
     * @return an integer whose bits from the index 8 to 31 are equals to the
     *         bit of index 7 of the given integer
     */
    public static int signExtend8(int b) {
        byte a = (byte)Preconditions.checkBits8(b);
        return (int) a;
    }

    /**
     * return a value equal to the given integer b, on which the 8 least
     * significant bits has been reversed : the bits of index 0 and 7 has been
     * exchanged, as well as those of index 1 and 6, 2 and 5, 3 and 4.
     * 
     * @param b
     *            the integer (must be an 8 bits value)
     * @throws IllegalArgumentException
     *             if b is invalid
     * @return a value equal to the given integer b on which the 8 least
     *         significant bits has been reversed
     */
    public static int reverse8(int b) {
        Preconditions.checkBits8(b);
        return tab[b];
    }

    /**
     * return a value equal to the given integer b, on which the 8 least
     * significant bits has been replaced by their complement : the 0 and the 1
     * has been exchanged.
     * 
     * @param b
     *            the integer (must be an 8 bits value)
     * @throws IllegalArgumentException
     *             if b is invalid
     * @return return a value equal to the given integer b on which the 8 least
     *         significant bits has been replaced by their complement
     */
    public static int complement8(int b) {
        Preconditions.checkBits8(b);
        return clip(8, ~b);
    }

    /**
     * return a 16 bits value whose 8 most significant bits are the 8 least
     * significant bits of the given integer highB, and the 8 least significant
     * bits are those of lowB.
     * 
     * @param highB
     *            the integer (must be an 8 bits value)
     * @param lowB
     *            the integer (must be an 8 bits value)
     * @throws IllegalArgumentException
     *             if highB or lowB is invalid
     * @return a 16 bits value whose 8 most significant bits are the 8 least
     *         significant bits of the given integer highB and the 8 least
     *         significant bits are those of lowB
     */
    public static int make16(int highB, int lowB) {
        Preconditions.checkBits8(highB);
        Preconditions.checkBits8(lowB);
        return highB << 8 | lowB;
    }

}