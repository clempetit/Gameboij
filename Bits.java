/*
 *	Author:      Clément Petit
 *	Date:        22 Feb 2018      
 */

package ch.epfl.gameboj.bits;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

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
    
    public static int set(int bits, int index, boolean newValue) {
        index = Objects.checkIndex(index, Integer.SIZE);
        int mask = mask(index);
        if (newValue) {
            return bits | mask;
        }
        else {
            return bits & ~mask;
        }
    }
    
    public static int clip(int size, int bits) {
        if (size == Integer.SIZE) {
            return bits;
        }
        Preconditions.checkArgument(size >= 0 && size <= 32);
        return bits & (mask(size) - 1);
    }
    
    public static int extract(int bits, int start, int size) {
        start = Objects.checkFromIndexSize(start, size, Integer.SIZE);
        if (size == Integer.SIZE) {
            return bits;
        }
        return (bits >> start) & (mask(size) - 1);
    }
    
    public static int rotate(int size, int bits, int distance) {
        Preconditions.checkArgument(size > 0 && size <= 32 && bits < Math.pow(2,size));
        int reducedDistance = Math.floorMod(distance,size);
        int rotatedBits = (bits << reducedDistance) | (bits >>> (size - reducedDistance));
        return rotatedBits & (~(-1 << size));
    }
    
    public static int signExtend8(int b) {
        Preconditions.checkBits8(b);
        byte a = (byte)b;
        return (int)a;
    }
    
    public static int reverse8(int b) {
        Preconditions.checkBits8(b);
        return 0;
    }
    
    public static int complement8(int b) {
        Preconditions.checkBits8(b);
        return 0;
    }
    
    public static int make16(int highB, int lowB) {
        Preconditions.checkBits8(highB);
        Preconditions.checkBits8(lowB);
        return 0;
    }

}
