/**
 *  @author Clément Petit (282626)
 *  @author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.bits;

public interface Bit {
    
    /**
     * automatically given by the type enum.
     * @return the index of the receptor
     */
    public abstract int ordinal();
    
    /**
     * return the same value as the method ordinal
     * but has a more understandable name.
     * @return the same value as the method ordinal
     */
    public default int index() {
        return ordinal();
    }
    
    /**
     * @return the mask corresponding to the bit
     */
    public default int mask() {
        return 1 << index();
    }

}