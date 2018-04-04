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
    
    /**
     * builds a Boot Rom to which the given cartridge is attached
     * @param cartridge the cartridge
     * @throws NullPointerException if the cartridge is null.
     */
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.cartridge = cartridge;
    }
    
    @Override
    /**
     * intercepts the readings in the range 0 to OxFF as long as the start memory has not been disabled,
     * and responds with the corresponding byte of the Boot Rom. All other reads are passed to the 
     * cartridge, calling its read method.
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (!disabled && address < bootRom.size()) {
            return bootRom.read(address);
        } else {
            return cartridge.read(address);
        }     
    }

    @Override
    /**
     * detects the writings to the address 0xFF50 and deactivates the Boot Rom at the first of them,
     * regardless of the written value. All the other writings are passed to the cartridge, calling 
     * its write method.
     */
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
