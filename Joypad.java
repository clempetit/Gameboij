/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * represents the Game Boy's joypad.
 */
public final class Joypad implements Component {

    private final Cpu cpu;

    private int line0 = 0;
    private int line1 = 0;
    private int lineSelection = 0;

    /**
     * enumeration representing the joypad keys.
     */
    public static enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }

    /**
     * constructs the joypad with an assigned cpu.
     * 
     * @param cpu
     *            the Game Boy processor from which the joypad can
     *            request interruptions
     */
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }

    /**
     * gives access to the register P1.
     */
    @Override
    public int read(int address) {
        if (address == AddressMap.REG_P1) {
            int keys = Bits.test(lineSelection, 0) ? line0 : 0;
            keys |= Bits.test(lineSelection, 1) ? line1 : 0;
            return Bits.complement8((lineSelection << 4) | keys);
        }
        return NO_DATA;
    }

    /**
     * gives access to the register P1.
     */
    @Override
    public void write(int address, int data) {
        if (address == AddressMap.REG_P1) {
            int prevKeysPressed = Bits.clip(4, read(AddressMap.REG_P1));

            lineSelection = Bits.extract(Bits.complement8(data), 4, 2);

            int currentKeysPressed = Bits.clip(4, read(AddressMap.REG_P1));
            if ((prevKeysPressed & currentKeysPressed) != prevKeysPressed)
                cpu.requestInterrupt(Interrupt.JOYPAD);
        }
    }

    /**
     * simulates the pressure on the given key.
     * 
     * @param k
     *            the key
     */
    public void keyPressed(Key k) {
        keyChange(k, true);
        int line = k.ordinal() < 4 ? 0 : 1;
        if (Bits.test(lineSelection, line))
            cpu.requestInterrupt(Interrupt.JOYPAD);
    }

    /**
     * simulates the release of the given key.
     * 
     * @param k
     *            the key
     */
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