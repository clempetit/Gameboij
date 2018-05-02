/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
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

    private Cpu cpu;

    private long nextNonIdleCycle = 0;

    private long lcdOnCycle = 0;

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

    private static Ram videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
    private static RamController videoRamCtrlr = new RamController(videoRam,
            AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END);

    public LcdController(Cpu cpu) {
        this.cpu = cpu;
    }

    public LcdImage currentImage() {
        if (currentImage == null) {
            return new LcdImage.Builder(160, 144).build();
        } else {
            return currentImage;
        }
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
        } else if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            Reg r = Reg.values()[address - AddressMap.REGS_LCDC_START];
            
            if (!((r == Reg.LY) | (r == Reg.STAT))) {
                lcdBank.set(r, data);
                if (r == Reg.LCDC) {
                    if (prevLcdStatus && !(lcdBank.testBit(Reg.LCDC, LcdcBits.LCD_STATUS))) {
                        setMode(0);
                        lcdBank.set(Reg.LY, 0);
                        LycEqLy();
                        nextNonIdleCycle = Long.MAX_VALUE;
                    }
                } else if (r == Reg.LYC) {
                    LycEqLy();
                }
            } else if (r == Reg.STAT) {
                int mask = data & (~0 << 3);
                lcdBank.set(Reg.STAT, mask | Bits.clip(3, (lcdBank.get(Reg.STAT))));
            } 
        }
    }
    
    @Override
    public void cycle(long cycle) {
        if (cycle == 0) {
            nextNonIdleCycle = Long.MAX_VALUE;
        }
        if (nextNonIdleCycle == Long.MAX_VALUE
                && lcdBank.testBit(Reg.LCDC, LcdcBits.LCD_STATUS)) {
            lcdOnCycle = cycle;                                    // remplacer par autre chose ?
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
                //System.out.println("VBLANK at cycle " + cycle);
                currentImage = nextImageBuilder.build();
                lcdBank.set(Reg.LY, (lcdBank.get(Reg.LY) + 1)); // nécessaire ici ?
                LycEqLy();
                nextNonIdleCycle += 114;
            } else {
                setMode(2);
                if (lcdBank.get(Reg.LY) == 0) {// à l'allumage
                    nextImageBuilder = new LcdImage.Builder(160, 144);
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
                nextImageBuilder = new LcdImage.Builder(160, 144); // magic numbers
                nextNonIdleCycle += 20;
            }
        }
        }

    }

    private void LycEqLy() { // VERIFIER
        boolean prevState = lcdBank.testBit(Reg.STAT, StatBits.LYC_EQ_LY); // égaux initialement ?
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
        Preconditions.checkArgument(mode >= 0 && mode <= 3);
        int mask = (~0 << 2) | mode;
        lcdBank.set(Reg.STAT, (lcdBank.get(Reg.STAT) | 0b11) & mask);
        
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
    }

    private void computeLine(int y) {
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(256); // magic numbers

        int bgp = lcdBank.get(Reg.BGP);

        int tileSource = lcdBank.testBit(Reg.LCDC, LcdcBits.TILE_SOURCE) ? 1 : 0;
        int tileSourceStart = AddressMap.TILE_SOURCE[tileSource];
        
        int lineIndex = (lcdBank.get(Reg.SCY) + y) % 256;
        
        LcdImageLine line = bgLine(lineIndex);
        
        nextImageBuilder.setLine(y, line); // mapColors ici ?
    }
    
    private int memoryStart(Bit area) {
        int start = lcdBank.testBit(Reg.LCDC, area) ? 1 : 0;
        return AddressMap.BG_DISPLAY_DATA[start];
    }
    
    private LcdImageLine bgLine(int lineIndex) {
        if ((lcdBank.testBit(Reg.LCDC, LcdcBits.BG))) {
            return extractLine(lineIndex, LcdcBits.BG_AREA);
        } else {
            return emptyLine();
        }
    }
    
    private LcdImageLine winLine(int lineIndex) {
        if ((lcdBank.testBit(Reg.LCDC, LcdcBits.WIN))) {
            return extractLine(lineIndex, LcdcBits.WIN_AREA);
        } else {
            return emptyLine();
        }
    }
    
    private LcdImageLine extractLine(int lineIndex, Bit area) {
        
        int tileSource = lcdBank.testBit(Reg.LCDC, LcdcBits.TILE_SOURCE) ? 1 : 0;
        int tileSourceStart = AddressMap.TILE_SOURCE[tileSource];
        
        int bgp = lcdBank.get(Reg.BGP);
        
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(256); // magic numbers
        for (int i = 0; i < 32; i++) { 
            int tileIndex = videoRamCtrlr.read(32 * (lineIndex/ 8) + memoryStart(area) + i); // magic numbers
            lineBuilder.setBytes(i,
                    Bits.reverse8(videoRamCtrlr.read(tileSourceStart + 16 * tileIndex + 2 * (lineIndex % 8) + 1)),
                    Bits.reverse8(videoRamCtrlr.read(tileSourceStart + 16 * tileIndex + 2 * (lineIndex % 8))));
        }
        return lineBuilder.build().mapColors(bgp).extractWrapped(lcdBank.get(Reg.SCX), 160);
    }
    
    private LcdImageLine emptyLine() {
        return new LcdImageLine.Builder(256).build().extractWrapped(lcdBank.get(Reg.SCX), 160);
    }

}
