/*
 *	Author:      Clément Petit
 *	Date:        21 Feb 2018      
 */

package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

public final class Bus {
    /** ArrayList containing components attached to the bus */
    private ArrayList<Component> attachedComp = new ArrayList<Component>();
    
    /** attaches a new component to the bus
	 * @param component the new component to be attached
	 */
    public void attach(Component component) {
        attachedComp.add(Objects.requireNonNull(component));
    }
    
    /** return the value stored at the given address if at least one component has a value at this address
	 *  return 0x100 if not
	 * @param address the address to look for
	 */
    public int read(int address) {
        Preconditions.checkBits16(address);
        for (Component c : attachedComp) {
            if (c.read(address) != Component.NO_DATA) {
                return c.read(address);
            }
        }
        return 0xFF;
    }
    
    /** writes the given value at the given address for each component attached to the bus
	 * @param address the address where to write
	 * @param data the data to write at the given address
	 */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for (Component c : attachedComp) {
            c.write(address, data);
        }
    }

}
