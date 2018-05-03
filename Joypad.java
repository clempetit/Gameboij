/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Joypad implements Component {
    
    private final Cpu cpu;
    
    private int P1 = 0b1111_1111; // int ?
    private int line0 = 0;
    private int line1 = 0;
    
    public static enum Key {RIGHT, LEFT, UP, DOWN, A, B, SELECT, START}
    
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }

    @Override
    public int read(int address) {   // Precondition address ?
        if (address == AddressMap.REG_P1)
            return Bits.complement8(P1);
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        if (address == AddressMap.REG_P1) {
            int mask = 0b11 << 4;
            P1 = (P1 | mask) & (Bits.complement8(data) | ~mask);
        }
    }
    
    public void keyPressed(Key k) {
        keyChange(k, true);
    }
    
    public void keyReleased(Key k) {
        keyChange(k, false);
    }
    
    private void keyChange(Key k, boolean pressed) {
        if (k.ordinal() <= 4)
            Bits.set(line0, k.ordinal(), pressed);
        else
            Bits.set(line1, k.ordinal() - 4, pressed);
        updateP1(pressed);
    }
    
    private void updateP1(boolean pressed) { // A CHANGER : " ^= "
        int prevP1 = P1;
        if ((!Bits.test(P1, 4)) && (!Bits.test(P1, 5)))
            P1 ^= line0 | line1;
        else if (!(Bits.test(P1, 4)))
            P1 ^= line0;
        else if (!(Bits.test(P1, 5)))
            P1 ^= line1;
        if (P1 != prevP1 && pressed)
            cpu.requestInterrupt(Interrupt.JOYPAD); // où le mettre
            
    }
}
