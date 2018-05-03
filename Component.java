/**
 *  @author Clément Petit (282626)
 *  @author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

public interface Component {

    /**
     * constant used in the method read of a component to indicate there is no
     * data at the given address as it is an invalid byte.
     */
    public static final int NO_DATA = 0x100;

    /**
     * return the byte stored at the given address by the component or NO_DATA
     * if the component doesn't contain any value at this address.
     * 
     * @param address
     *            the address (must be a value of 16 bits)
     * @throws IllegalArgumentException
     *             if the address is invalid
     * @return the byte stored at the given address by the component or NO_DATA
     *         if the component doesn't contain any value at this address
     */
    public abstract int read(int address);

    /**
     * stores the value given at the given address in the component or does
     * nothing if the component doesn't allow to store values at this address.
     * 
     * @param address
     *            the address (must be a value of 16 bits)
     * @param data
     *            the data (must be a value of 8 bits)
     * @throws IllegalArgumentException
     *             if the address or the data are invalid
     */
    public abstract void write(int address, int data);

    /**
     * attaches the component to the given bus.
     * 
     * @param bus
     *            the bus
     */
    public default void attachTo(Bus bus) {
        bus.attach(this);
    }

}