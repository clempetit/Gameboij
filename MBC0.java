/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class MBC0 implements Component {

    private final Rom rom;

    private static final int MBC0_ROM_SIZE = 32768;

    /**
     * builds a controller of type 0 for the given rom.
     * 
     * @param rom
     *            the rom
     * @throws NullPointerException
     *             if the rom is null
     * @throws IllegalArgumentException
     *             if the rom does not contain exactly 32768 bytes
     */
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == MBC0_ROM_SIZE);
        this.rom = rom;
    }

    /**
     * Give access to the mbc0 rom.
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address < MBC0_ROM_SIZE) {
            return rom.read(address);
        } else {
            return NO_DATA;
        }
    }

    /**
     * Give access to the mbc0 rom.
     */
    @Override
    public void write(int address, int data) {
        // Does nothing, not possible to write in a ROM.
    }

}