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
        
    }
    
    public static boolean test(int bits, Bit bit) {
        
    }

}
