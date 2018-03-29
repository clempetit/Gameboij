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
    private Rom bootRom = new Rom(BootRom.DATA);
    private boolean disabled = false;
    
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.cartridge = cartridge;
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (!disabled &&  address < bootRom.size()) {
            return bootRom.read(address);
        } else {
            return cartridge.read(address);
        }     
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address == AddressMap.REG_BOOT_ROM_DISABLE) {
            disabled = true;
        } else {
        cartridge.write(address, data);
        }
        
    }

}
