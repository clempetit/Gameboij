/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class Cartridge implements Component {

    private final Component mbc;

    private static final int CARTRIDGE_TYPE = 0x147;
    
    private static final int RAM_SIZE = 0x149;
    private static final int[] SIZES_TAB = {0, 2048, 8192, 32768};
    private static final int RAM_SIZE0 = 0;
    private static final int RAM_SIZE1 = 2048;
    private static final int RAM_SIZE2 = 8192;
    private static final int RAM_SIZE3 = 32768;
    

    private Cartridge(Component mbc) {
        this.mbc = mbc;
    }

    /**
     * return a cartridge whose ROM contains the bytes of the given file.
     * 
     * @param romFile
     *            the file
     * @throws IOException
     *             in case of input-output error
     * @throws IndexOutOfBoundException()
     *             if the file does not contain a value between 0 and 3 at the position 0x147
     * @throws IndexOutOfBoundException()
     *             if the msb is type 1, 2 or 3, and the file does not contain a value between 0 and 3 at the position 0x149 
     * @return a cartridge whose ROM contains the bytes of the given file
     */
    public static Cartridge ofFile(File romFile) throws IOException {

        try (InputStream in = new FileInputStream(romFile)) {
            byte[] b = in.readAllBytes();
            in.close();
            Objects.checkIndex(b[CARTRIDGE_TYPE], 4);
            Rom rom = new Rom(b);
            
            if (b[CARTRIDGE_TYPE] == 0)
                return new Cartridge(new MBC0(rom));
            else {
                int size = Objects.checkIndex(b[RAM_SIZE], 4);
                return new Cartridge(new MBC1(rom, SIZES_TAB[size]));
            }
        }
    }

    @Override
    /**
     * check the argument and calls the mbc's corresponding method.
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        return mbc.read(address);
    }

    @Override
    /**
     * check the arguments and calls the mbc's corresponding method.
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        mbc.write(address, data);
    }

}