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

public final class LcdController implements Component, Clocked {

    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;
    
    private Cpu cpu;
    
    private long nextNonIdleCycle = 0;
    
    private long lcdOnCycle = 0;
    
    private enum Reg implements Register{
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    };
    
    private final RegisterFile<Reg> lcrBench = new RegisterFile<>(Reg.values());
    
    private enum LcdcBits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    };
    
    private enum StatBits implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC 
    }; 
    
    private static Ram videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
    
    public LcdController(Cpu cpu) {
        this.cpu = cpu;
    }
    
    public LcdImage currentImage() {
        return new LcdImage.Builder(160, 144).build();
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            return videoRam.read(address - AddressMap.VIDEO_RAM_START);
        } else if (address > AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END){ // Comment obtenir la valeur d'un registre ?
            Reg r = Reg.values()[address - AddressMap.REGS_LCDC_START];
            return lcrBench.get(r); // procéder comme ça ?
        } else {
        return NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) { // INT_LYC ?
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            videoRam.write(address - AddressMap.VIDEO_RAM_START, data);
        } else if (address > AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            Reg r = Reg.values()[address - AddressMap.REGS_LCDC_START];
            if (!(r == Reg.LY)) {
            lcrBench.set(r, data);
            }
            
            if (r == Reg.LCDC) {
                if (!(lcrBench.testBit(Reg.LCDC, LcdcBits.LCD_STATUS))) {
                    lcrBench.setBit(Reg.STAT, StatBits.MODE0, false);
                    lcrBench.setBit(Reg.STAT, StatBits.MODE1, false);
                    lcrBench.set(Reg.LY, 0);
                    LycEqLy();                             // utiliser ici ?
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
            } else if (r == Reg.STAT) {
                int mask = (data >>> 3) << 3; // Améliorer code ?
                lcrBench.set(Reg.STAT, mask | Bits.clip(3, (lcrBench.get(Reg.STAT))));   
            } else if (r == Reg.LYC) {
                LycEqLy();
            } 
        }
    }
    
    private void LycEqLy() {
        boolean prevState = lcrBench.testBit(Reg.STAT, StatBits.LYC_EQ_LY); // égaux initialement ?
        if (!prevState && lcrBench.get(Reg.LY) == lcrBench.get(Reg.LYC)) {
            lcrBench.setBit(Reg.STAT, StatBits.LYC_EQ_LY, true);
            if (lcrBench.testBit(Reg.STAT, StatBits.INT_LYC)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        } else {
            lcrBench.setBit(Reg.STAT, StatBits.LYC_EQ_LY, false);
        }
    }
    
    private int getMode() {
        return Bits.clip(2, lcrBench.get(Reg.STAT));
    }
    
    private void setMode(int mode) {
        Preconditions.checkArgument(mode >= 0 && mode <= 3);
        int mask = (~0 << 2) | mode;
        lcrBench.set(Reg.STAT, lcrBench.get(Reg.STAT) & mask);
    }
    
    @Override
    public void cycle(long cycle) {
        if(nextNonIdleCycle == Long.MAX_VALUE && lcrBench.testBit(Reg.LCDC, LcdcBits.LCD_STATUS)) {
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
            lcrBench.set(Reg.LY, lcrBench.get(Reg.LY) + 1);
            LycEqLy();
            nextNonIdleCycle += 43;
        }
            break;
        case 3: {
            setMode(0);
            if (lcrBench.testBit(Reg.STAT, StatBits.INT_MODE0)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
            nextNonIdleCycle += 51;
        }
            break;
        case 0: {
            if (lcrBench.get(Reg.LY) == 144) {
                System.out.println(lcrBench.get(Reg.LY) + "         144"); ///////
                setMode(1);
                lcrBench.set(Reg.LY, 0);
                LycEqLy();
                if (lcrBench.testBit(Reg.STAT, StatBits.INT_MODE1)) {
                    cpu.requestInterrupt(Interrupt.LCD_STAT);
                }
                nextNonIdleCycle += 1140;
            } else {
                //System.out.println(lcrBench.get(Reg.LY) + "         autre");///////
                setMode(2);
                if (lcrBench.testBit(Reg.STAT, StatBits.INT_MODE2)) {
                    cpu.requestInterrupt(Interrupt.LCD_STAT);
                }
                nextNonIdleCycle += 20;
            }
        }
            break;
        case 1: {
            cpu.requestInterrupt(Interrupt.VBLANK); // ici ?
            System.out.println("VBLANK at " + cycle);
            setMode(2);
            if (lcrBench.testBit(Reg.STAT, StatBits.INT_MODE2)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
            nextNonIdleCycle += 20;
        }
        }
        
    }

}
