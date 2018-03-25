/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

public final class BootRomController implements Component {

    private Cartridge cartridge;
    private boolean desactivated = false;
    
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.cartridge = cartridge;
    }
    @Override
    public int read(int address) {
        Preconditions.checkArgument(address >= 0 && address < 0xFF);
        if (desactivated) {
            return cartridge.read(address);
        } else {
            return BootRom.DATA[address];
        }
    }

    @Override
    public void write(int address, int data) {
        if (address== AddressMap.REG_BOOT_ROM_DISABLE) {
            desactivated = true;
        }
        if (desactivated) {
            cartridge.write(address, data);
        }

    }

}
