/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;

import ch.epfl.gameboj.AddressMap;
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
import ch.epfl.gameboj.component.memory.RamController;

public final class LcdController implements Component, Clocked {
    
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;
    
    private static final int BG_LINE_SIZE = 256;
    private static final int WIN_LINE_SIZE = LCD_WIDTH;
    
    private static final int MODE_MASK = 0b11;

    private long nextNonIdleCycle = Long.MAX_VALUE;
    
    private int winY;
    
    private final Cpu cpu;
    private Bus bus;                // pas possible de mettre en final ?
    private final Ram OAM;
    private int copyDestination;
    private int copySource;

    private LcdImage.Builder nextImageBuilder;
    private LcdImage currentImage;

    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    };

    private final RegisterFile<Reg> lcdBank = new RegisterFile<>(Reg.values());

    private enum LcdcBits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    };

    private enum StatBits implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC
    };
    
    private enum SpriteSpec implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    };

    private static Ram videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
    private static RamController videoRamCtrlr = new RamController(videoRam,
            AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END);

    public LcdController(Cpu cpu) {
        this.cpu = cpu;
        OAM = new Ram(AddressMap.OAM_RAM_SIZE);
        copyDestination = AddressMap.OAM_END;
    }

    public LcdImage currentImage() {
        if (currentImage == null) {
            return new LcdImage.Builder(160, 144).build();
        } else {
            return currentImage;
        }
    }
    
    /**
     * Attach the LcdController to the given bus and and stores the bus in the LcdController.
     */
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            return videoRamCtrlr.read(address);
        } else if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            Reg r = Reg.values()[address - AddressMap.REGS_LCDC_START];
            return lcdBank.get(r);
        } else if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            return OAM.read(address - AddressMap.OAM_START);
        } else {
            return NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        boolean prevLcdStatus = (lcdBank.testBit(Reg.LCDC, LcdcBits.LCD_STATUS));
        
        if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            videoRamCtrlr.write(address, data); 
        }
        else if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            OAM.write(address - AddressMap.OAM_START, data);
            
        }
        else if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            Reg r = Reg.values()[address - AddressMap.REGS_LCDC_START];
            
            if (!((r == Reg.LY) | (r == Reg.STAT))) {
                lcdBank.set(r, data);
                if (r == Reg.LCDC && prevLcdStatus && !(lcdBank.testBit(Reg.LCDC, LcdcBits.LCD_STATUS))) {
                        setMode(0);
                        lcdBank.set(Reg.LY, 0);
                        LycEqLy();
                        nextNonIdleCycle = Long.MAX_VALUE;
                } else if (r == Reg.LYC) {
                    LycEqLy();
                } else if (r == Reg.DMA) { // 8 ou 16 chiffres écrits dans DMA ?
                     copyDestination = AddressMap.OAM_START;
                     copySource = lcdBank.get(Reg.DMA);
                }
            } else if (r == Reg.STAT) {
                int mask = data & (~0 << 3);
                lcdBank.set(Reg.STAT, mask | Bits.clip(3, (lcdBank.get(Reg.STAT))));
            } 
        }
    }
    
    @Override
    public void cycle(long cycle) {
        if (copyDestination != AddressMap.OAM_END) {                                        // beaucoup de copies ?
            OAM.write(copyDestination++ - AddressMap.OAM_START, bus.read(copySource++));
        }
        if (nextNonIdleCycle == Long.MAX_VALUE
                && lcdBank.testBit(Reg.LCDC, LcdcBits.LCD_STATUS)) {
            nextNonIdleCycle = cycle;
        }
        if (cycle == nextNonIdleCycle) {
            reallyCycle(cycle);
        }
    }

    private void reallyCycle(long cycle) {
        switch (getMode()) {
        case 2: {
            setMode(3);
            computeLine(lcdBank.get(Reg.LY));
            lcdBank.set(Reg.LY, lcdBank.get(Reg.LY) + 1);
            LycEqLy();
            nextNonIdleCycle += 43;
        }
            break;
        case 3: {
            setMode(0);
            nextNonIdleCycle += 51;
        }
            break;
        case 0: {
            if (lcdBank.get(Reg.LY) == 144) {
                setMode(1);
                cpu.requestInterrupt(Interrupt.VBLANK);
                currentImage = nextImageBuilder.build();
                lcdBank.set(Reg.LY, (lcdBank.get(Reg.LY) + 1));
                LycEqLy();
                nextNonIdleCycle += 114;
            } else {
                setMode(2);
                if (lcdBank.get(Reg.LY) == 0) { // à l'allumage
                    nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
                    winY = 0;
                }
                nextNonIdleCycle += 20;
            }
        }
            break;
        case 1: {
            if (lcdBank.get(Reg.LY) > 144 && lcdBank.get(Reg.LY) <= 153) {
                lcdBank.set(Reg.LY, ((lcdBank.get(Reg.LY) + 1)) % 154);
                LycEqLy();
                nextNonIdleCycle += 114;
            } else { // puis quand on arrive à 154
                setMode(2);
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
                winY = 0;
                nextNonIdleCycle += 20;
            }
        }
        }

    }

    private void LycEqLy() { // VERIFIER
        boolean prevState = lcdBank.testBit(Reg.STAT, StatBits.LYC_EQ_LY);           // égaux initialement ?
        if (!prevState && lcdBank.get(Reg.LY) == lcdBank.get(Reg.LYC)) {
            lcdBank.setBit(Reg.STAT, StatBits.LYC_EQ_LY, true);
            if (lcdBank.testBit(Reg.STAT, StatBits.INT_LYC)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        } else if (prevState && lcdBank.get(Reg.LY) != lcdBank.get(Reg.LYC)){
            lcdBank.setBit(Reg.STAT, StatBits.LYC_EQ_LY, false);
        }
    }

    private int getMode() {
        return Bits.clip(2, lcdBank.get(Reg.STAT));
    }

    private void setMode(int mode) { // magic numbers ?
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
            break;
        case 3: {
        }
            break;
        default: {
            throw new IllegalArgumentException();
        }
        }
        lcdBank.set(Reg.STAT, (lcdBank.get(Reg.STAT) | MODE_MASK) & (mode | ~MODE_MASK));
    }

    private void computeLine(int y) {
        
        int bgLineIndex = Bits.clip(8, lcdBank.get(Reg.SCY) + y);
        LcdImageLine bgLine = bgLine(bgLineIndex);
        
        int WX = lcdBank.get(Reg.WX) - 7;
        if ((lcdBank.testBit(Reg.LCDC, LcdcBits.WIN)) && WX >= 0 && WX < LCD_WIDTH) {   // Window
            LcdImageLine winLine = extractLine(winY, WIN_LINE_SIZE, LcdcBits.WIN_AREA);
            winY++;
            bgLine = bgLine.join(winLine.shift(WX), WX);
        }
        
        if (lcdBank.testBit(Reg.LCDC, LcdcBits.OBJ)) {   // Sprites
            int[] spritesList = spritesIntersectingLine(y);
            LcdImageLine spritesBGLine = spritesLine(y, spritesList, true);
            LcdImageLine spritesFGLine = spritesLine(y, spritesList, false);
            BitVector opacity = bgLine.opacity().or(spritesBGLine.opacity().not());
            bgLine = spritesBGLine.below(bgLine, opacity);
            bgLine = bgLine.below(spritesFGLine);
        }
        
        nextImageBuilder.setLine(y, bgLine);
    }
    
    private LcdImageLine bgLine(int lineIndex) {
        if ((lcdBank.testBit(Reg.LCDC, LcdcBits.BG))) {
            return extractLine(lineIndex, BG_LINE_SIZE, LcdcBits.BG_AREA).extractWrapped(LCD_WIDTH, lcdBank.get(Reg.SCX));
        } else {
            return emptyLine();
        }
    }
    
    private LcdImageLine spritesLine(int y, int[] spritesList, boolean background) {
        LcdImageLine spritesLine = emptyLine();
        int spriteTileSourceStart = AddressMap.TILE_SOURCE[1];
        
        for (int i = 0; i < spritesList.length; i++) {
            int spriteMemoryIndex = AddressMap.OAM_START + 4 * spritesList[spritesList.length - 1 - i];
            int ordinate = (read(spriteMemoryIndex) - 16);
            int tileIndex = read(spriteMemoryIndex + 2);  // verifier
            int spriteSpec = read(spriteMemoryIndex + 3);
            
            if(Bits.test(spriteSpec, SpriteSpec.BEHIND_BG) == !background) { // verifier
                
                Reg paletteReg = Bits.test(spriteSpec, SpriteSpec.PALETTE) ? Reg.OBP1 : Reg.OBP0;
                int palette = lcdBank.get(paletteReg);
                
                int size = Bits.test(lcdBank.get(Reg.LCDC), LcdcBits.OBJ_SIZE) ? 16 : 8;
                int tileLineIndex = (Bits.test(spriteSpec, SpriteSpec.FLIP_V) ?   // Inversion verticale
                        size - 1 - (y - ordinate) : (y - ordinate));
                int tileLineMsb = getTileLineVector(spriteTileSourceStart, tileIndex, tileLineIndex, true);
                int tileLineLsb = getTileLineVector(spriteTileSourceStart, tileIndex, tileLineIndex, false);
                
                if (Bits.test(spriteSpec, SpriteSpec.FLIP_H)) { // Inversion horizontale
                    tileLineMsb = Bits.reverse8(tileLineMsb);
                    tileLineLsb = Bits.reverse8(tileLineLsb);
                }
                
                LcdImageLine spriteLine = new LcdImageLine.Builder(LCD_WIDTH).setBytes(0,
                        tileLineMsb,
                        tileLineLsb)
                        .build()
                        .shift(read(spriteMemoryIndex + 1) - 8) // verifier
                        .mapColors(palette);
                spritesLine = spritesLine.below(spriteLine);
            }
        }
        return spritesLine;
    }
    
    private LcdImageLine extractLine(int lineIndex, int size, Bit area) {
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(size);
        
        int memoryStart = memoryStart(area);
        int tileSource = lcdBank.testBit(Reg.LCDC, LcdcBits.TILE_SOURCE) ? 1 : 0; // declarer ici ?
        int tileSourceStart = AddressMap.TILE_SOURCE[tileSource];
        int tileLineIndex = lineIndex % 8;
        
        for (int i = 0; i < size / Byte.SIZE; i++) { 
            int tileIndex = Bits.clip(8, videoRamCtrlr.read(32 * (lineIndex / 8) + memoryStart + i)); // magic numbers, clip ?
            if (tileSource == 0) 
                tileIndex = Bits.clip(8, tileIndex + 0x80); // magic numbers ?
            
            lineBuilder.setBytes(i, getTileLineVector(tileSourceStart, tileIndex, tileLineIndex, true),
                    getTileLineVector(tileSourceStart, tileIndex, tileLineIndex, false));
        }
        return lineBuilder.build().mapColors(lcdBank.get(Reg.BGP));
    }
    
    private int memoryStart(Bit area) {
        int start = lcdBank.testBit(Reg.LCDC, area) ? 1 : 0;
        return AddressMap.BG_DISPLAY_DATA[start];
    }
    
    private int[] spritesIntersectingLine(int lineIndex) { // COMMENT FAIRE POUR TRIER ?
        int size = Bits.test(lcdBank.get(Reg.LCDC), LcdcBits.OBJ_SIZE) ? 16 : 8;
        int[] sprites = new int[10];
        int nbOfSprites = 0;
        for (int i = 0; i < 40; i++) { // magic numbers
            int spriteMemoryIndex = AddressMap.OAM_START + 4 * i;
            int spriteOrdinate = read(spriteMemoryIndex) - 16;
            if (nbOfSprites < sprites.length && lineIndex >= spriteOrdinate && lineIndex < spriteOrdinate + size)
                    sprites[nbOfSprites++] = read(spriteMemoryIndex + 1) << 8 | i;
        }
        Arrays.sort(sprites, 0, nbOfSprites);
        int[] spriteIndex = new int[nbOfSprites];
        for (int i = 0; i < spriteIndex.length; i++) {
            spriteIndex[i] = Bits.clip(8, sprites[i]);
        }
        return spriteIndex;
    }
    
    private int getTileLineVector(int tileSourceStart, int tileIndex, int tileLineIndex, boolean msb) { // UTILISER UNE METHODE COMME CA ?
        int address = tileSourceStart + 16 * tileIndex + 2 * tileLineIndex; // magicNumbers
        if (msb)
            address++;
        return Bits.reverse8(videoRamCtrlr.read(address));
    }
    
    private LcdImageLine emptyLine() {
        return new LcdImageLine.Builder(LCD_WIDTH).build();
    }

}
