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
    
    private final Component mbc0;
    private static Rom rom;
    
    private Cartridge(Component mbc) {
       this.mbc0 = mbc;
    }
    
    public static Cartridge ofFile(File romFile) throws IOException { // A VOIR  COMMENT METTRE romFile  DANS UN []Byte.
        
        try (InputStream in = new FileInputStream(romFile)) {
        byte[] b = new byte[(int) romFile.length()];
        
        int read = in.read(b);
        if (read != (int) romFile.length()) {
            throw new IOException("abcdefg");
        }
        rom = new Rom(b);
        Preconditions.checkArgument(b[0x147] == 0);
        
        return new Cartridge(new MBC0(rom));
        }
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkArgument(address >= 0 && address < 32768);
        return mbc0.read(address);
    }

    @Override
    public void write(int address, int data) { // CAS OU LA CARTOUCHE POSSEDE MEMOIRE VIVE A CERTAINES ADRESSES ?
        Preconditions.checkArgument(address >= 0 && address < 32768);
        Preconditions.checkBits8(data);
        mbc0.write(address, data);
    }

}
