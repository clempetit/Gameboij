/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

public interface Register {
    
    /**
     * automatically given by the enum type.
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
    
}