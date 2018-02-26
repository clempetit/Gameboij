/**
 *  @autor Clément Petit (282626)
 *  @autor Yanis Berkani (271348)
 */

package ch.epfl.gameboj.bits;

public interface Bit {
    
    /**
     * automatically given by the type enum
     * @return
     */
    public abstract int ordinal();
    
    /**
     * return the same value as the method ordinal
     * but has a name more understandable
     * @return the same value as the method ordinal
     */
    public default int index() {
        return ordinal();
    }
    
    /**
     * 
     * @return the mask corresponding to the bit
     */
    public default int mask() {
        int mask = 1 << index();
        return mask;
    }

}
