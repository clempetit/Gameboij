/*
 *	Author:      Clément Petit
 *	Date:        20 Feb 2018      
 */

package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;

public final class Ram {
    
    private byte[] ram;
    
    public Ram(int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        ram = new byte[size];
    }
    
    public int size() {
        return ram.length;
    }
    
    public int read(int index) {
        if (index<0 || index >= ram.length) {
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(ram[index]);
    }
    
    public void write(int index, int value) {
        if (index<0 || index >= ram.length) {
            throw new IndexOutOfBoundsException();
        }
        ram[index] = (byte)Preconditions.checkBits8(value);
    }
    
}
