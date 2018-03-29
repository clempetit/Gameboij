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
    
    private Rom rom;

    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == 32768);
        this.rom = rom;
    }
    
    @Override
    public int read(int address) {
        if (address >= 0 && address < 32768) {
        return rom.read(address);
        } else {
            return NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) {
        // Does nothing, not possible to write in a ROM.
    }

}
