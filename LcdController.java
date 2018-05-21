/**
 *  @author Clément Petit (282626)
 *  @author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.AddressMap;
import static ch.epfl.gameboj.AddressMap.VIDEO_RAM_START;
import static ch.epfl.gameboj.AddressMap.VIDEO_RAM_END;
import static ch.epfl.gameboj.AddressMap.OAM_START;
import static ch.epfl.gameboj.AddressMap.OAM_END;
import static ch.epfl.gameboj.AddressMap.REGS_LCDC_START;
import static ch.epfl.gameboj.AddressMap.REGS_LCDC_END;

/**
 * represents a Liquid Crystal Display controller.
 */
public final class LcdController implements Component, Clocked {

    private final Cpu cpu;
    private final Ram OAM;
    private Bus bus;
    
    private LcdImage.Builder nextImageBuilder;
    private LcdImage currentImage;
    
    private static final int TILE_EDGE_SIZE = 8;
    private static final int NB_OF_SPRITES = 40;
    private static final int BYTES_PER_TILE = 16;
    private static final int ATTRIBUTE_BYTES_PER_SPRITE = 4;
    private static final int TILES_PER_LINE = 32;

    /**
     * the screen width in pixels
     */
    public static final int LCD_WIDTH = 160;
    /**
     * the screen height in pixels
     */
    public static final int LCD_HEIGHT = 144;

    private static final int BG_LINE_SIZE = 256;
    private static final int WIN_LINE_SIZE = LCD_WIDTH;

    private long nextNonIdleCycle;

    private int winY;
    
    private int copyDestination;
    private int copySource;

    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    };

    private static final RegisterFile<Reg> lcdBank = new RegisterFile<>(
            Reg.values());

    private static enum LcdcBits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    };

    private static enum StatBits implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC
    };

    private static enum spritesAttributes {
        Y_COORD, X_COORD, INDEX, SPECS
    };

    private static enum SpriteSpec implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    };

    private static Ram videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);

   /**
    * Constructs the LCD controller.
    * 
    * @param cpu
    *            the Game Boy processor to which the LCD controller
    */
    public LcdController(Cpu cpu) {
        this.cpu = cpu;
        OAM = new Ram(AddressMap.OAM_RAM_SIZE);
        nextNonIdleCycle = Long.MAX_VALUE;
        copyDestination = AddressMap.OAM_END;
    }

    /**
     * returns the image currently displayed on the screen, or an empty image
     * if the first image has not been drawn yet.
     * 
     * @return the image currently displayed on the screen or an empty image
     */
    public LcdImage currentImage() {
        return currentImage == null ? new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT).build()
                : currentImage;
    }

    /**
     * Attaches the LcdController to the given bus and stores the bus in the
     * LcdController.
     * 
     * @param bus
     *            the bus
     */
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    /**
     * gives access to the video ram, the lcd controller registers and
     * the object attributes memory.
     * 
     * @param address
     *            the address (must be a 16 bits value)
     * @throws IllegalArgumentException
     *             if the address is invalid
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (address >= VIDEO_RAM_START && address < VIDEO_RAM_END) {
            return videoRam.read(address - VIDEO_RAM_START);
        }
        else if (address >= REGS_LCDC_START && address < REGS_LCDC_END) {
            Reg r = Reg.values()[address - REGS_LCDC_START];
            return lcdBank.get(r);
        }
        else if (address >= OAM_START && address < OAM_END) {
            return OAM.read(address - OAM_START);
        }
        else {
            return NO_DATA;
        }
    }

    /**
     * gives access to the video ram, the lcd controller registers and
     * the object attributes memory.
     * Initiates the copy process if something is written in the
     * DMA register.
     * 
     * @param address
     *            the address (must be a 16 bits value)
     * @param data
     *            the data (must be an 8 bits value)
     * @throws IllegalArgumentException
     *             if the address or the data is invalid
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        boolean prevLcdStatus = (lcdBank.testBit(Reg.LCDC,
                LcdcBits.LCD_STATUS));

        if (address >= VIDEO_RAM_START && address < VIDEO_RAM_END) {
            videoRam.write(address - VIDEO_RAM_START, data);
        } else if (address >= OAM_START && address < OAM_END) {
            OAM.write(address - OAM_START, data);

        } else if (address >= REGS_LCDC_START && address < REGS_LCDC_END) {
            Reg r = Reg.values()[address - REGS_LCDC_START];

            if (!((r == Reg.LY) | (r == Reg.STAT))) {
                
                lcdBank.set(r, data);
                
                if (r == Reg.LCDC && prevLcdStatus
                        && !(lcdBank.testBit(Reg.LCDC, LcdcBits.LCD_STATUS))) {
                    setMode(0);
                    lcdBank.set(Reg.LY, 0);
                    LycEqLy();
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
                else if (r == Reg.LYC) {
                    LycEqLy();
                }
                else if (r == Reg.DMA) {
                    copyDestination = OAM_START;
                    copySource = lcdBank.get(Reg.DMA) << Byte.SIZE;
                }
            } else if (r == Reg.STAT) {
                int mask = data & (~0 << 3);
                lcdBank.set(Reg.STAT,
                        mask | Bits.clip(3, (lcdBank.get(Reg.STAT))));
            }
        }
    }

    /**
     * manages the writings in the object attributes memory when a copy is in progress.
     * Manages the activation of the screen.
     * Determines if the lcd controller needs to do something during the given cycle, and
     * if so, calls the method reallyCycle.
     * 
     * @param cycle
     *            the cycle
     */
    @Override
    public void cycle(long cycle) {
        if (copyDestination != OAM_END)
            write(copyDestination++, bus.read(copySource++));

        if (nextNonIdleCycle == Long.MAX_VALUE
                && lcdBank.testBit(Reg.LCDC, LcdcBits.LCD_STATUS)) {
            nextNonIdleCycle = cycle;
            lcdBank.set(Reg.LY, 153);
        }

        if (cycle == nextNonIdleCycle)
            reallyCycle(cycle);
    }

    private void reallyCycle(long cycle) {
        switch (getMode()) {
        case 2: {
            setMode(3);
            computeLine(lcdBank.get(Reg.LY));
            nextNonIdleCycle += 43;
        }
            break;
        case 3: {
            setMode(0);
            nextNonIdleCycle += 51;
        }
            break;
        case 0: {
            if (lcdBank.get(Reg.LY) == 143) {
                setMode(1);
                cpu.requestInterrupt(Interrupt.VBLANK);
                currentImage = nextImageBuilder.build();
                
                lcdBank.set(Reg.LY, (lcdBank.get(Reg.LY) + 1));
                LycEqLy();
                
                nextNonIdleCycle += 114;
            } else {
                lcdBank.set(Reg.LY, (lcdBank.get(Reg.LY) + 1) % 154);
                LycEqLy();
                
                setMode(2);
                if (lcdBank.get(Reg.LY) == 0) {
                    nextImageBuilder = new LcdImage.Builder(LCD_WIDTH,
                            LCD_HEIGHT);
                    winY = 0;
                }
                nextNonIdleCycle += 20;
            }
        }
            break;
        case 1: {
            if (lcdBank.get(Reg.LY) >= 144) {
                lcdBank.set(Reg.LY, ((lcdBank.get(Reg.LY) + 1)) % 153);
                LycEqLy();
                nextNonIdleCycle += 114;
            } else {
                setMode(2);
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
                winY = 0;
                nextNonIdleCycle += 20;
            }
        }
        }

    }

    private void LycEqLy() {
        if (lcdBank.get(Reg.LY) == lcdBank.get(Reg.LYC)) {
            lcdBank.setBit(Reg.STAT, StatBits.LYC_EQ_LY, true);
            if (lcdBank.testBit(Reg.STAT, StatBits.INT_LYC)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        } else {
            lcdBank.setBit(Reg.STAT, StatBits.LYC_EQ_LY, false);
        }
    }

    private int getMode() {
        return Bits.clip(2, lcdBank.get(Reg.STAT));
    }

    private void setMode(int mode) {
        Objects.checkIndex(mode, 4);
        switch (mode) {
        case 0: {
            if (lcdBank.testBit(Reg.STAT, StatBits.INT_MODE0))
                cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
            break;
        case 1: {
            if (lcdBank.testBit(Reg.STAT, StatBits.INT_MODE1))
                cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
            break;
        case 2: {
            if (lcdBank.testBit(Reg.STAT, StatBits.INT_MODE2))
                cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
        }
        lcdBank.set(Reg.STAT, ((lcdBank.get(Reg.STAT) >>> 2) << 2) | mode);
    }

    private void computeLine(int y) {

        int bgLineIndex = Bits.clip(8, lcdBank.get(Reg.SCY) + y);
        LcdImageLine bgLine = extractBgLine(bgLineIndex);

        int WX = Math.max(0, lcdBank.get(Reg.WX) - 7);
        
        if ((lcdBank.testBit(Reg.LCDC, LcdcBits.WIN)) && WX >= 0
                && WX < LCD_WIDTH && y >= lcdBank.get(Reg.WY)) {
            
            LcdImageLine winLine = extractLine(winY, WIN_LINE_SIZE,
                    LcdcBits.WIN_AREA);
            winY++;
            
            bgLine = bgLine.join(winLine.shift(WX), WX);
        }

        if (lcdBank.testBit(Reg.LCDC, LcdcBits.OBJ)) {
            int[] spritesList = spritesIntersectingLine(y);
            LcdImageLine spritesBGLine = extractSpritesLine(y, spritesList,
                    true);
            LcdImageLine spritesFGLine = extractSpritesLine(y, spritesList,
                    false);
            BitVector bgOpacity = bgLine.opacity()
                    .or(spritesBGLine.opacity().not());
            
            bgLine = spritesBGLine.below(bgLine, bgOpacity)
                    .below(spritesFGLine);
        }
        nextImageBuilder.setLine(y, bgLine);
    }

    private LcdImageLine extractBgLine(int lineIndex) {
        if ((lcdBank.testBit(Reg.LCDC, LcdcBits.BG))) {
            return extractLine(lineIndex, BG_LINE_SIZE, LcdcBits.BG_AREA)
                    .extractWrapped(LCD_WIDTH, lcdBank.get(Reg.SCX));
        } else
            return emptyLine();
    }

    private LcdImageLine extractLine(int lineIndex, int size, Bit area) {
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(size);

        int tileSource = lcdBank.testBit(Reg.LCDC, LcdcBits.TILE_SOURCE) ? 1
                : 0;
        int tileSourceStart = AddressMap.TILE_SOURCE[tileSource];
        int tileLineIndex = lineIndex % TILE_EDGE_SIZE;
        int address = memoryStart(area)
                + TILES_PER_LINE * (lineIndex / TILE_EDGE_SIZE);

        for (int i = 0; i < size / Byte.SIZE; i++) {
            
            int tileIndex = read(address + i);
            
            if (tileSource == 0)
                tileIndex = Bits.clip(8, tileIndex + 0x80);
            
            lineBuilder.setBytes(i,
                    getTileLineVector(tileSourceStart, tileIndex, tileLineIndex,
                            true),
                    getTileLineVector(tileSourceStart, tileIndex, tileLineIndex,
                            false));
        }
        return lineBuilder.build().mapColors(lcdBank.get(Reg.BGP));
    }

    private int memoryStart(Bit area) {
        int start = lcdBank.testBit(Reg.LCDC, area) ? 1 : 0;
        return AddressMap.BG_DISPLAY_DATA[start];
    }

    private LcdImageLine extractSpritesLine(int y, int[] spritesList,
            boolean background) {
        
        LcdImageLine fullSpriteLine = emptyLine();
        
        int spriteTileSourceStart = AddressMap.TILE_SOURCE[1];

        for (int spriteIndex : spritesList) {
            
            int spriteMemoryIndex = OAM_START
                    + ATTRIBUTE_BYTES_PER_SPRITE * spriteIndex;
            
            int spriteSpec = read(
                    spriteMemoryIndex + spritesAttributes.SPECS.ordinal());

            if (Bits.test(spriteSpec, SpriteSpec.BEHIND_BG) == background) {
                
                int spriteOrdinate = read(spriteMemoryIndex) - 16;
                
                int tileIndex = read(
                        spriteMemoryIndex + spritesAttributes.INDEX.ordinal());

                int size = Bits.test(lcdBank.get(Reg.LCDC), LcdcBits.OBJ_SIZE)
                        ? 16
                        : 8;
                int tileLineIndex = ((Bits.test(spriteSpec, SpriteSpec.FLIP_V)
                        ? size - 1 - (y - spriteOrdinate)
                        : (y - spriteOrdinate)));
                int tileLineMsb = getTileLineVector(spriteTileSourceStart,
                        tileIndex, tileLineIndex, true);
                int tileLineLsb = getTileLineVector(spriteTileSourceStart,
                        tileIndex, tileLineIndex, false);

                if (Bits.test(spriteSpec, SpriteSpec.FLIP_H)) {
                    tileLineMsb = Bits.reverse8(tileLineMsb);
                    tileLineLsb = Bits.reverse8(tileLineLsb);
                }

                Reg paletteReg = Bits.test(spriteSpec, SpriteSpec.PALETTE)
                        ? Reg.OBP1
                        : Reg.OBP0;
                int palette = lcdBank.get(paletteReg);

                LcdImageLine spriteLine = new LcdImageLine.Builder(LCD_WIDTH)
                        .setBytes(0, tileLineMsb, tileLineLsb).build()
                        .shift(read(spriteMemoryIndex
                                + spritesAttributes.X_COORD.ordinal()) - 8)
                        .mapColors(palette);
                
                fullSpriteLine = spriteLine.below(fullSpriteLine);
            }
        }
        return fullSpriteLine;
    }

    private int[] spritesIntersectingLine(int y) {
        int size = Bits.test(lcdBank.get(Reg.LCDC), LcdcBits.OBJ_SIZE) ? 16 : 8;
        int[] sprites = new int[10];
        int nbOfSprites = 0;

        for (int i = 0; i < NB_OF_SPRITES; i++) {
            
            int spriteMemoryIndex = OAM_START + ATTRIBUTE_BYTES_PER_SPRITE * i;
            int spriteOrdinate = read(spriteMemoryIndex) - 16;
            
            if (nbOfSprites < sprites.length && y >= spriteOrdinate
                    && y < spriteOrdinate + size)
                sprites[nbOfSprites++] = (read(spriteMemoryIndex
                        + spritesAttributes.X_COORD.ordinal()) << Byte.SIZE)
                        | i;
        }
        Arrays.sort(sprites, 0, nbOfSprites);

        int[] spriteIndex = new int[nbOfSprites];
        
        for (int i = 0; i < spriteIndex.length; i++)
            spriteIndex[i] = Bits.clip(8, sprites[i]);
            
        return spriteIndex;
    }

    private int getTileLineVector(int tileSourceStart, int tileIndex,
            int tileLineIndex, boolean msb) {
        
        int address = tileSourceStart + BYTES_PER_TILE * tileIndex
                + 2 * tileLineIndex;
        if (msb)
            address++;

        return Bits.reverse8(read(address));
    }

    private LcdImageLine emptyLine() {
        return new LcdImageLine.Builder(LCD_WIDTH).build();
    }

}