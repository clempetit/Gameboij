/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class RegisterFile<E extends Register> {
    
    private byte[] banc;
    
    /**
     * builds a bench of 8 bits registers which size is the same 
     * as the size of the given array.
     * @param allRegs the array
     */
    public RegisterFile(E[] allRegs) {
        banc = new byte[allRegs.length];
    }
    
    /**
     * return the 8 bits value contained in the given register, 
     * in the form of an integer included between 0 and FF.
     * @param reg the register
     * @return the 8 bits value contained in the given register
     * in the form of an integer included between 0 and FF
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(banc[reg.index()]);
    }
    
    /**
     * modifies the given register's content so that it is 
     * equal to the given 8 bits value.
     * @param reg the register
     * @param newValue the integer (must be an 8 bits value)
     * @throws IllegalArgumentException if newValue is invalid
     */
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        banc[reg.index()] = (byte)newValue;
    }
    
    /**
     * return true if and only if the given bit of the register 
     * is equal to 1.
     * @param reg the register
     * @param b the bit
     * @return true if and only if the given bit of the register 
     * is equal to 1
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }
    
    /**
     * modifies the value contained in the given register so that 
     * the given bit has the given new value.
     * @param reg the register
     * @param bit the bit
     * @param newValue the new value
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }
}