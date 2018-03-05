/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class RegisterFile<E extends Register> {
    
    private byte[] banc;
    
    public RegisterFile(E[] allRegs) {
        banc = new byte[allRegs.length];
    }
    
    public int get(E reg) {
        return Byte.toUnsignedInt(banc[reg.index()]);
    }
    
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        banc[reg.index()] = (byte)newValue;
    }
    
    public boolean testBit(E reg, Bit b) {
        return Bits.test((byte)reg.index(), b.index());
    }
    
    public void setBit(E reg, Bit bit, boolean newValue) {
        Bits.set((byte)reg.index(), bit.index(), newValue);
    }
}
