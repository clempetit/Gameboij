/*
 *	Author:      Clément Petit
 *	Date:        22 Feb 2018      
 */

package ch.epfl.gameboj.bits;

import java.util.Objects;

public final class Bits {
    
    private Bits() {}
    
    public static int mask(int index) {
        index = Objects.checkIndex(index, Integer.SIZE);
        int mask = 1 << index;
        return mask;
    }
    
    public static boolean test(int bits, int index) {
        index = Objects.checkIndex(index, Integer.SIZE);
        int mask = mask(index);
        boolean bitSet = (bits & mask) == mask;
        return bitSet;
    }
    
    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.index());
    }
    
    public int set(int bits, int index, boolean newValue) {
        index = Objects.checkIndex(index, Integer.SIZE);
        int mask = mask(index);
        if (newValue) {
            return bits | mask;
        }
        else {
            return bits & ~mask;
        }
    }
    
    public int clip(int size, int bits) {
        size = Objects.checkIndex(size, 33);
        int clipBits = bits << size;
        clipBits = bits >>> size;
        return clipBits;
    }
    
    public int extract(int bits, int start, int size) {
        start = Objects.checkFromIndexSize(start, size, Integer.SIZE);
        int extractBits = bits << start;
        extractBits = bits >>> start + size;
        return extractBits;
    }
    
    public int rotate(int size, int bits, int distance) {
        return 0;
    }
    
    public int signExtend8(int b) {
        return 0;
    }
    
    public int reverse8(int b) {
        return 0;
    }
    
    public int complement8(int b) {
        return 0;
    }
    
    public int make16(int highB, int lowB) {
        return 0;
    }

}
