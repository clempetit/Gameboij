/**
 *  @author Clément Petit (282626)
 *  @author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

public final class RamController implements Component {

    private final Ram ctrldRam;
    private final int start;
    private final int end;

    /**
     * builds a controller for the given RAM, accessible between the start
     * address and the end address.
     * 
     * @param ram
     *            the ram (must not be null)
     * @param startAddress
     *            the start address (must be a 16 bits value)
     * @param endAddress
     *            the end address (must be a 16 bits value)
     * @throws NullPointerException
     *             if the ram is null
     * @throws IllegalArgumentException
     *             if the start or the end address are invalid or if the
     *             interval they form is negative or superior to the size of the
     *             ram
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        ctrldRam = Objects.requireNonNull(ram);
        start = Preconditions.checkBits16(startAddress);
        end = Preconditions.checkBits16(endAddress);
        Preconditions.checkArgument((endAddress - startAddress) >= 0 && 
                (endAddress - startAddress) <= ram.size());
    }

    /**
     * calls the first constructor with an end address such that the whole RAM
     * is accessible through the controller.
     * 
     * @param ram
     *            the ram (must be a 16 bits value)
     * @param startAddress
     *            (must be a 16 bits value)
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, startAddress + ram.size());
    }

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
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= start && address < end) {
            return ctrldRam.read(address - start);
        } else {
            return NO_DATA;
        }
    }

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
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address >= start && address < end) {
            ctrldRam.write(address - start, data);
        }

    }

}
