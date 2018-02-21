/*
 *	Author:      Clément Petit
 *	Date:        21 Feb 2018      
 */

package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

public interface Component {
    public static final int NO_DATA = 0x100;
    
    public abstract int read(int address);
    
    public abstract void write(int address, int data);
    
    public default void attachTo(Bus bus) {
        bus.attach(this);
    }
    
}
