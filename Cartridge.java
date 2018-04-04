/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class Cartridge implements Component {
    
    private final Component mbc;
    private static Rom rom;
    
    private Cartridge(Component mbc) {
       this.mbc = mbc;
    }
    
    /**
     * return a cartridge whose ROM contains the bytes of the given file.
     * @param romFile the file
     * @throws IOException in case of input-output error
     * @throws IllegalArgumentException if the file does not contain 0 at the position 0x147
     * @return a cartridge whose ROM contains the bytes of the given file
     */
    public static Cartridge ofFile(File romFile) throws IOException {
        
        try (InputStream in = new FileInputStream(romFile)) {
        byte[] b = in.readAllBytes();
        in.close();
        rom = new Rom(b);
        Preconditions.checkArgument(b[0x147] == 0);
        return new Cartridge(new MBC0(rom));
        }
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return mbc.read(address);
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        mbc.write(address, data);
    }

}
