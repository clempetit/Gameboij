/*
 *	Author:      Clément Petit
 *	Date:        22 Feb 2018      
 */

package ch.epfl.gameboj.bits;

public interface Bit {
    
    public abstract int ordinal();
    
    public default int index() {
        return ordinal();
    }
    
    public default int mask() {
        int mask = 1 << index();
        return mask;
    }

}
