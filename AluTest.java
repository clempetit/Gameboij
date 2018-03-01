/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.Bits;

public class AluTest {

    @Test
    void MaskZNHCWorks() {
        assertEquals(0x70, Alu.maskZNHC(false, true, true, true));
    }
    
    @Test
    void unpackValueWorks() {
        assertEquals(0xFF, Alu.unpackValue(0xFF70));
    }
    
    @Test
    void unpackFlagsWorks() {
        assertEquals(0x70, Alu.unpackFlags(0xFF70));
    }
    
    @Test
    void addFailsForInvalidValue() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.add(0, -1, true));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.add(0x100, 0, false));
    }
    
    @Test
    void addWorks() {
        assertEquals(0x00B0, Alu.add(0x80, 0x7F, true));
        assertEquals(0x2500, Alu.add(0x10, 0x15));
        assertEquals(0x1020, Alu.add(0x08, 0x08));      
    }
    
    @Test
    void add16FailsForInvalidValue() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.add16L(0, -1));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.add16L(0x10000, 0));
    }
    
    @Test
    void add16LWorks() {
        assertEquals(0x120030, Alu.add16L(0x11FF, 0x0001));
    }
    
    @Test
    void add16HWorks() {
        assertEquals(0x120000 , Alu.add16H(0x11FF, 0x0001));
    }
    
    @Test 
    void subFailsForInvalidValue() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.sub(0, -1, false));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.sub(0x100, 0, true));
    }
    
    @Test
    void subWorks() {
        assertEquals(0xFF70 ,Alu.sub(0x01, 0x01, true));
        assertEquals(0x00C0 ,Alu.sub(0x10, 0x10));
        assertEquals(0x9050 ,Alu.sub(0x10, 0x80));
    }
    
    @Test
    void bcdAdjustFailsForInvalidValue() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.bcdAdjust(0x111, false, false, false));
    }
    
    @Test
    void bcdAdjustWorks() {
        assertEquals(0x7300, Alu.bcdAdjust(0x6D, false, false, false));
        assertEquals(0x0940, Alu.bcdAdjust(0x0F, true, true, false));
    }
    
    @Test
    void andWorks() {
        assertEquals(0x0320, Alu.and(0x53, 0xA7));
    }
    
    @Test
    void orWorks() {
        assertEquals(0xF700, Alu.or(0x53, 0xA7));
    }
    
    @Test
    void xorWorks() {
        assertEquals(0xF400, Alu.xor(0x53, 0xA7));
    }
    
    @Test
    void shiftLeftWorks() {
        assertEquals(0x90, Alu.shiftLeft(0x80));
    }
    
    @Test
    void shiftRightLWorks() {
        assertEquals(0x4000, Alu.shiftRightL(0x80));
    }
    
    @Test
    void shiftRightAWorks() {
        assertEquals(0xC000, Alu.shiftRightA(0x80));
    }
    
    @Test
    void RotateWorks() {
        assertEquals(0x0110, Alu.rotate(Alu.RotDir.LEFT, 0x80));
        assertEquals(0x90, Alu.rotate(Alu.RotDir.LEFT, 0x80, false));
        assertEquals(0x0100, Alu.rotate(Alu.RotDir.LEFT, 0x00, true));
    }
    
    
    
    
}