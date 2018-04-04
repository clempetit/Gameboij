/*
 *	Author:      Clément Petit
 *	Date:        20 Feb 2018      
 */

package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;

/**
 * 
 * @author Clément Petit (282626)
 * @author Yanis Berkani (271348)
 * 
 */
public final class Ram {
    
    private byte[] ram;
    
    /**
     * builds a new random-access memory of given size
     * @param size the size of the RAM (must be positive)
     * @throws IllegalArgumentException if the size is strictly negative
     */
    public Ram(int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        ram = new byte[size];
    }
    
    /**
     * return the size of the memory in bytes
     * @return the size of the memory in bytes
     */
    public int size() {
        return ram.length;
    }
    
    /**
     * return the byte located at the index given 
     * @param index the index (must be included between 0 and FF)
     * @throws IndexOutOfBoundsException if the index is invalid
     * @return the byte located at the index given 
     */
    public int read(int index) {
        if (index<0 || index >= ram.length) {
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(ram[index]);
    }
    
    /**
     * modifies the content of the memory at the given index for the given value
     * @param index the index (must be included between 0 and FF)
     * @param value the new value (must be an 8 bits value) 
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws IllegalArgumentException if the value is invalid
     */
    public void write(int index, int value) {
        if (index<0 || index >= ram.length) {
            throw new IndexOutOfBoundsException();
        }
        ram[index] = (byte)Preconditions.checkBits8(value);
    }
    
}