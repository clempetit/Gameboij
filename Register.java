/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

public interface Register {
    
    public abstract int ordinal();
    
    public default int index() {
        return ordinal();
    }
    
}
