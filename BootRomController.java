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
    private boolean disabled = false;
    
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.cartridge = cartridge;
    }
    @Override
    public int read(int address) {
        Preconditions.checkArgument(address >= 0 && address < 0xFFFF);
        if (!disabled && address < 0xFF) {
            return BootRom.DATA[address];
        } else {
            return cartridge.read(address);
        }     
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if (!disabled && address == AddressMap.REG_BOOT_ROM_DISABLE) {
            disabled = true;
        } else {
            cartridge.write(address, data);
        }

    }

}
