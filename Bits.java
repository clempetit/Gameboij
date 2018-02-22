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
        test(bits, bit.index());
    }
    
    public int set(int bits, int index, boolean newValue) {
        index = Objects.checkIndex(index, Integer.SIZE);
        int mask = mask(index);
        if (newValue) {
            bits = bits | mask;
        }
        else {
            bits = bits & ~mask;
        }
        return bits;
    }
    
    public int clip(int size, int bits) {
        return 0;
    }
    
    public int extract(int bits, int start, int size) {
        return 0;
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
