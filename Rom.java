/*
 *	Author:      Clément Petit
 *	Date:        20 Feb 2018      
 */

package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

/**
 * 
 * @author Clément Petit (282626)
 * @author Yanis Berkani (271348)
 * 
 */
public final class Rom {
    private byte[] rom;
    
    /**
     * builds the read-only memory which content and size are those of the parameter
     * @param data the array that provides the size and the content of the ROM (must not be null)
     * @throw the exception NullPointerException if it is null
     */
    public Rom(byte[] data) {
        Objects.requireNonNull(data);
        rom = Arrays.copyOf(data, data.length);
    }
    
    /**
     * return the size of the memory in bytes
     * @return the size of the memory in bytes
     */
    public int size() {
        return rom.length;
    }
    
    /**
     * return the byte located at the index given 
     * @param index the index (must be included between 0 and FF)
     * @throws IndexOutOfBoundsException if the index is invalid
     * @return the byte located at the index given 
     */
    public int read(int index) {
        if (index<0 || index >= rom.length) {
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(rom[index]);
    }
    
}
