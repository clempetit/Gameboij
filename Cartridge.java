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
    
    public static Cartridge ofFile(File romFile) throws IOException { // A VOIR  COMMENT METTRE romFile  DANS UN []Byte.
        
        try (InputStream in = new FileInputStream(romFile)) {
        byte[] b = new byte[(int) romFile.length()];
        
        int read = in.read(b);
        if (read != (int) romFile.length()) {
            throw new IOException();
        }
        rom = new Rom(b);
        Preconditions.checkArgument(b[0x147] == 0);
        
        return new Cartridge(new MBC0(rom));
        }
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkArgument(address >= 0 && address < 32768);
        return Preconditions.checkBits8(mbc.read(address));
    }

    @Override
    public void write(int address, int data) { // CAS OU LA CARTOUCHE POSSEDE MEMOIRE VIVE A CERTAINES ADRESSES ?
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        mbc.write(address, data);
    }

}