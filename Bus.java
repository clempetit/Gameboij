/**
 *  @author Clément Petit (282626)
 *  @author Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

public final class Bus {
    /** ArrayList containing components attached to the bus */
    private ArrayList<Component> attachedComp = new ArrayList<Component>();
    
     /** attaches the given component to the bus
     * @param component the component to be attached (must not be null)
     * @throws NullPointerException if the component is null
     */
    public void attach(Component component) {
        attachedComp.add(Objects.requireNonNull(component));
    }
    
    /** return the value stored at the given address if at least one component has a value at this address
     *  or return 0xFF if not
     * @param address the address to look for (must be a 16 bits value)
     * @throws IllegalArgumentException if the address is invalid
     * @return the value stored at the given address if at least one component has a value at this address
     * or return 0xFF if not
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
     * @param address the address where to write (must be a 16 bits value)
     * @param data the data to write at the given address (must be an 8 bits value)
     * @throws IllegalArgumentException if the address or the data are invalid
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for (Component c : attachedComp) {
            c.write(address, data);
        }
    }

}
