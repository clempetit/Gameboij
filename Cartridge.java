/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class Cartridge implements Component {
    
    private Component mbc;
    
    private Cartridge(Component mbc) {
       this.mbc = mbc;
    }
    
    public static Cartridge ofFile(File romFile) { // A VOIR  COMMENT METTRE romFile  DANS UN []Byte.
        byte[] b = new byte[(int)romFile.length()];
        try {
        FileInputStream in = new FileInputStream(romFile);
        in.read(b);
        } catch(IOException e) { //GESTION IOException
            
        }
        Rom rom = new Rom(b);
        return new Cartridge(new MBC0(rom));
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkArgument(address >= 0 && address < 32768);
        return mbc.read(address);
    }

    @Override
    public void write(int address, int data) { // CAS OU LA CARTOUCHE POSSEDE MEMOIRE VIVE A CERTAINES ADDRESSES ?
        Preconditions.checkArgument(address >= 0 && address < 32768);
        Preconditions.checkBits8(data);
        mbc.write(address, data);
    }

}
