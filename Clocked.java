/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component;

public interface Clocked {
    /**
     * asks to the component to evolve by executing all the 
     * operations that it has to execute during the given cycle
     * @param cycle the cycle
     */
    public abstract void cycle(long cycle);
}