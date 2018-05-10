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
    
    private int line0 = 0;
    private int line1 = 0;
    private int lineSelection = 0;
    
    public static enum Key {RIGHT, LEFT, UP, DOWN, A, B, SELECT, START}
    
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }
int cycles = 0;
    @Override
    public int read(int address) {
        cycles++;
        if (address == AddressMap.REG_P1) {
            //System.out.println(lineSelection + "  " + line0 + "  " + line1);
            int keys = Bits.test(lineSelection, 0) ? line0 : 0;
            keys |= Bits.test(lineSelection, 1) ? line1 : 0;
            return Bits.complement8((lineSelection << 4) | keys);
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {   // écrit sans arret, normal ?
        cycles++;
        if (address == AddressMap.REG_P1) {
            int prevSelection = lineSelection;
            lineSelection = Bits.extract(Bits.complement8(data), 4, 2);
            //System.out.println("write :   " + cycles +" :   "+ lineSelection);
            //System.out.println(prevSelection + "        "+ lineSelection);
            if (prevSelection != lineSelection) {
               boolean line0Interrupt = !Bits.test(prevSelection, 0) && Bits.test(lineSelection, 0) && line0 != 0;
               boolean line1Interrupt = !Bits.test(prevSelection, 1) && Bits.test(lineSelection, 1) && line1 != 0;
               if (line0Interrupt || line1Interrupt) {
                   //System.out.println("write :    " + cycles +" :     "+ lineSelection + "                interrupt");
                   cpu.requestInterrupt(Interrupt.JOYPAD);
               }
            }
        }
    }
    
    public void keyPressed(Key k) {
        keyChange(k, true);
        //System.out.println(lineSelection + "      keyPressed :  " + k);
        int line = k.ordinal() < 4 ? 0 : 1;
        if(Bits.test(lineSelection, line))
            //System.out.println(777);
            cpu.requestInterrupt(Interrupt.JOYPAD);
    }
    
    public void keyReleased(Key k) {
        keyChange(k, false);
    }
    
    private void keyChange(Key k, boolean pressed) {
        if (k.ordinal() < 4)
            line0 = Bits.set(line0, k.ordinal(), pressed);
        else
            line1 = Bits.set(line1, k.ordinal() - 4, pressed);
    }
}
