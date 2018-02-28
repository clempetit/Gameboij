/**
 *  @autor Clément Petit (282626)
 *  @autor Yanis Berkani (271348)
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
    
    
    
}
