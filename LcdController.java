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
    
    private enum Reg implements Register{
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    };
    
    private final RegisterFile<Reg> lcrBank = new RegisterFile<>(Reg.values());
    
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
            return new LcdImage.Builder(160, 144).build(); // Procéder comme ça ?
        } else {
            return currentImage;
        }
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            return videoRam.read(address - AddressMap.VIDEO_RAM_START);
        } else if (address > AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END){
            Reg r = Reg.values()[address - AddressMap.REGS_LCDC_START];
            return lcrBank.get(r);
        } else {
        return NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            videoRam.write(address - AddressMap.VIDEO_RAM_START, data);
        } else if (address > AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            Reg r = Reg.values()[address - AddressMap.REGS_LCDC_START];
            if (!(r == Reg.LY)) {
            lcrBank.set(r, data);
            }
            
            if (r == Reg.LCDC) {
                if (!(lcrBank.testBit(Reg.LCDC, LcdcBits.LCD_STATUS))) {
                    lcrBank.setBit(Reg.STAT, StatBits.MODE0, false);
                    lcrBank.setBit(Reg.STAT, StatBits.MODE1, false);
                    lcrBank.set(Reg.LY, 0);
                    LycEqLy();                             // utiliser ici ?
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
            } else if (r == Reg.STAT) {
                int mask = (data >>> 3) << 3; // Améliorer code ?
                lcrBank.set(Reg.STAT, mask | Bits.clip(3, (lcrBank.get(Reg.STAT))));   
            } else if (r == Reg.LYC) {
                LycEqLy();
            } 
        }
    }
    
    private void LycEqLy() {
        boolean prevState = lcrBank.testBit(Reg.STAT, StatBits.LYC_EQ_LY); // égaux initialement ?
        if (!prevState && lcrBank.get(Reg.LY) == lcrBank.get(Reg.LYC)) {
            lcrBank.setBit(Reg.STAT, StatBits.LYC_EQ_LY, true);
            if (lcrBank.testBit(Reg.STAT, StatBits.INT_LYC)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        } else {
            lcrBank.setBit(Reg.STAT, StatBits.LYC_EQ_LY, false);
        }
    }
    
    private int getMode() {
        return Bits.clip(2, lcrBank.get(Reg.STAT));
    }
    
    private void setMode(int mode) {
        Preconditions.checkArgument(mode >= 0 && mode <= 3);
        int mask = (~0 << 2) | mode;
        lcrBank.set(Reg.STAT, (lcrBank.get(Reg.STAT) |3) & mask);
    }
    
    @Override
    public void cycle(long cycle) {
        if(nextNonIdleCycle == Long.MAX_VALUE && lcrBank.testBit(Reg.LCDC, LcdcBits.LCD_STATUS)) {
            lcdOnCycle = cycle;
            nextNonIdleCycle = cycle;
        }
        if (cycle == nextNonIdleCycle) {
            reallyCycle(cycle);
        }

    }
    
    private void reallyCycle(long cycle) {
        switch(getMode()) {
        case 2: {
            setMode(3);
            computeLine(lcrBank.get(Reg.LY));
            lcrBank.set(Reg.LY, lcrBank.get(Reg.LY) + 1);
            LycEqLy();
            nextNonIdleCycle += 43;
        }
            break;
        case 3: {
            setMode(0);
            if (lcrBank.testBit(Reg.STAT, StatBits.INT_MODE0)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
            nextNonIdleCycle += 51;
        }
            break;
        case 0: {
            if (lcrBank.get(Reg.LY) == 144) { // LY peut aller jusqu'à 153 !!!
                setMode(1);
                lcrBank.set(Reg.LY, 153); // ???
                LycEqLy();
                if (lcrBank.testBit(Reg.STAT, StatBits.INT_MODE1)) {
                    cpu.requestInterrupt(Interrupt.LCD_STAT);
                }
                currentImage = nextImageBuilder.build();
                nextNonIdleCycle += 1140;
            } else {
                setMode(2);
                if (lcrBank.testBit(Reg.STAT, StatBits.INT_MODE2)) {
                    cpu.requestInterrupt(Interrupt.LCD_STAT);
                }
                if (lcrBank.get(Reg.LY) == 0) {  // Placer ici ?
                    nextImageBuilder = new LcdImage.Builder(160, 144);
                }
                nextNonIdleCycle += 20;
            }
        }
            break;
        case 1: {
            cpu.requestInterrupt(Interrupt.VBLANK); // ici ?
            setMode(2);
            lcrBank.set(Reg.LY, 0);
            if (lcrBank.testBit(Reg.STAT, StatBits.INT_MODE2)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
            if (lcrBank.get(Reg.LY) == 0) {   // Placer ici ?
                nextImageBuilder = new LcdImage.Builder(256, 256); // magic numbers
            }
            nextNonIdleCycle += 20;
        }
        }
        
    }
    
    private void computeLine(int index) {
        if(!(lcrBank.testBit(Reg.LCDC, LcdcBits.BG))) { // activation image de fond
           // return;
        }
        int paletteBgWin = lcrBank.get(Reg.BGP);
       
        int bgArea = lcrBank.testBit(Reg.LCDC, LcdcBits.BG_AREA) ? 1 : 0;
        int bgDataStart = AddressMap.BG_DISPLAY_DATA[bgArea];
        
        //int winArea = lcrBank.testBit(Reg.LCDC, LcdcBits.WIN_AREA) ? 1 : 0;
        //int winDataStart = AddressMap.BG_DISPLAY_DATA[winArea];
        
        int tileSource = lcrBank.testBit(Reg.LCDC, LcdcBits.TILE_SOURCE) ? 1 : 0;
        int tileSourceStart = AddressMap.TILE_SOURCE[tileSource];
        
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(256); // magic numbers
        for (int i = 0; i < 32; i++) {                                   // nouvelle tuile tous les 8 lignes
            int tileIndex = videoRamCtrlr.read(32*(index/8) + bgDataStart + i);          // magic numbers
            lineBuilder.setBytes(i, Bits.reverse8(videoRamCtrlr.read(tileSourceStart + 2*(tileIndex + 1 + 32*(index % 8)))),
                    Bits.reverse8(videoRamCtrlr.read(tileSourceStart + 2*(tileIndex + 32*(index % 8))))); // inverser les bits ici ? 1.2.4
        }
        nextImageBuilder.setLine(index, lineBuilder.build().mapColors(paletteBgWin)); // mapColors ici ?
    }

}
