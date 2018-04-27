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
    
    private long lcdOnCycle = 0; // valeur initiale ???
    
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
    
    public LcdController(Cpu cpu) { // interruptions ?
        this.cpu = cpu;
    }
    
    public LcdImage currentImage() { // Comment faire image noire non nulle ?
        return new LcdImage(160, 144, null); // Builder
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            return videoRam.read(address - AddressMap.VIDEO_RAM_START);
        } else if (address > AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END){ // Comment obtenir la valeur d'un registre ?
            switch(address - AddressMap.REGS_LCDC_START) {
            case ((Reg.LCDC).values[0]): // ne marche pas
                return lcrBench.get(Reg.LCDC); // magic numbers
            case 0xFF41:
                return lcrBench.get(Reg.STAT);
            case 0xFF42:
                return lcrBench.get(Reg.SCY);
            case 0xFF43:
                return lcrBench.get(Reg.SCX);
            case 0xFF44:
                return lcrBench.get(Reg.LY);
            case 0xFF45:
                return lcrBench.get(Reg.LYC);
            case 0xFF46:
                return lcrBench.get(Reg.DMA);
            case 0xFF47:
                return lcrBench.get(Reg.BGP);
            case 0xFF48:
                return lcrBench.get(Reg.OBP0);
            case 0xFF49:
                return lcrBench.get(Reg.OBP1);
            case 0xFF4A:
                return lcrBench.get(Reg.WY);
            case 0xFF4B:
                return lcrBench.get(Reg.WX);
            default:
                return NO_DATA;
            }
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
        } else if (address > AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) { // Comment obtenir la valeur d'un registre ?
            
            Reg r = Reg.values()[address - AddressMap.REGS_LCDC_START];
            if (!(r == Reg.LY)) {
            lcrBench.set(r, data);
            }
            
            if (r == Reg.LCDC) {
                if (!(lcrBench.testBit(Reg.LCDC, LcdcBits.LCD_STATUS))) { // voir si la demarche est bonne
                    lcrBench.setBit(Reg.STAT, StatBits.MODE0, false);
                    lcrBench.setBit(Reg.STAT, StatBits.MODE1, false);
                    lcrBench.set(Reg.LY, 0);
                    LycEqLy();                             // utiliser ici ?
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
            }
            
            if (r == Reg.STAT) {
                int mask = (data >>> 3) << 3; // Améliorer code ?
                lcrBench.set(Reg.STAT, mask | Bits.clip(3, (lcrBench.get(Reg.STAT))));   
            }
            
            //if (!(lcrBench.testBit(Reg.STAT, StatBits.MODE0) && lcrBench.testBit(Reg.STAT, StatBits.MODE1))) { // 1.2.8 interruption LCD_STAT, mode 0, 1 ou 2 conditions ?
                
            //}
            //if (!(lcrBench.testBit(Reg.STAT, StatBits.MODE0)) && lcrBench.testBit(Reg.STAT, StatBits.MODE1)) { // ??? lorsqu'il entre en mode 1, il lève de manière inconditionnelle l'interruption VBLANK
           //     cpu.requestInterrupt(Interrupt.VBLANK);
            //}
        } // QUAND traiter ces cas ??
    }
    
    private void modifLYOrLYC(Reg r, int data) {
        Preconditions.checkArgument(r == Reg.LY || r == Reg.LYC);
        if ( r == Reg.LY) {
            lcrBench.set(Reg.LY, data);
            
        } else { // valable aussi pour LYC ?
            lcrBench.set(Reg.LYC, data);
            LycEqLy();
        }
    }
    
    private void LycEqLy() { // utiliser cette méthode ?
        if (lcrBench.get(Reg.LY) == lcrBench.get(Reg.LYC)) {
            lcrBench.setBit(Reg.STAT, StatBits.LYC_EQ_LY, true); // mettre à 1 le bit LYC_EQ_LY de STAT ???
            if (lcrBench.testBit(Reg.STAT, StatBits.INT_LYC)) {  // placer ici ?
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        } else {
            lcrBench.setBit(Reg.STAT, StatBits.LYC_EQ_LY, false); // mettre à 0 le bit LYC_EQ_LY de STAT ???
        }
    }
    
    @Override
    public void cycle(long cycle) {
        if(nextNonIdleCycle == Long.MAX_VALUE && lcrBench.testBit(Reg.LCDC, LcdcBits.LCD_STATUS)) {
            lcdOnCycle = cycle;
        }
        if (cycle == nextNonIdleCycle) {
            reallyCycle(cycle);
        }

    }
    
    private void reallyCycle(long cycle) {
        
    }

}
