/*
 *	Author:      Yanis Berkani
 *	Date:        22 févr. 2018
 */

package ch.epfl.gameboj.bits;

public interface Bit {
    
    public abstract int ordinal();
    
    public default int index() {
        return ordinal();
    }
    
    public default int mask() {
        
    }
}
