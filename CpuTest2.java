/**
 *  @autor Clément Petit (282626)
 *  @autor Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class CpuTest2 {
    
    private int opcodes[] = {
            0b00111110,
            123,
            
            0b00101110,
            124,
            
            0b00100110,
            125,
            
            0b00011110,
            201,
            
            0b00010110,
            255,
            
            0b00001110,
            202,
            
            0b00000110,
            203,
            
            0b00000001,
            0xe8,
            0xfd,
            
            0b00010001,
            0xb3,
            0xa9,
            
            0b00100001,
            0xc4,
            0xb1,
            
            00000000,
            
            0b00110001,
            0xe4,
            0xc1,
            
            0b00100001,
            0x03,
            0x00,
            
            0b01010110,
            
            0b00100001,
            0x00,
            0x00,
            
            0b00111010,
            
            0b01110000,
            
            0b11101010,
            0x12,
            0x34,
            
            0b11110101

    };

    @Test
    void test() {
        
        Bus bus = new GameBoy(null).bus();
        
        Ram memory = new Ram(1<<16);
        RamController controller = new RamController(memory, 0);
        controller.attachTo(bus);
        
        Cpu cpu = new Cpu();
        cpu.attachTo(bus);
        
        for(int i=0;i<opcodes.length;i++) {
            memory.write(i, opcodes[i]);
        }
        
        int c=0;
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(123,cpu._testGetPcSpAFBCDEHL()[2]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(124,cpu._testGetPcSpAFBCDEHL()[9]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(125,cpu._testGetPcSpAFBCDEHL()[8]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(201,cpu._testGetPcSpAFBCDEHL()[7]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(255,cpu._testGetPcSpAFBCDEHL()[6]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(202,cpu._testGetPcSpAFBCDEHL()[5]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(203,cpu._testGetPcSpAFBCDEHL()[4]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(65000,Bits.make16(cpu._testGetPcSpAFBCDEHL()[4], cpu._testGetPcSpAFBCDEHL()[5]));
        assertEquals(255,cpu._testGetPcSpAFBCDEHL()[6]);
        assertEquals(201,cpu._testGetPcSpAFBCDEHL()[7]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(0xa9b3,Bits.make16(cpu._testGetPcSpAFBCDEHL()[6], cpu._testGetPcSpAFBCDEHL()[7]));
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(0xb1c4,Bits.make16(cpu._testGetPcSpAFBCDEHL()[8], cpu._testGetPcSpAFBCDEHL()[9]));
   
        int tmp[] = cpu._testGetPcSpAFBCDEHL();
        cpu.cycle(c++);
        for(int i=1;i<10;i++) {
            assertEquals(tmp[i],cpu._testGetPcSpAFBCDEHL()[i]);
        }
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(0xc1e4,cpu._testGetPcSpAFBCDEHL()[1]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(3,Bits.make16(cpu._testGetPcSpAFBCDEHL()[8], cpu._testGetPcSpAFBCDEHL()[9]));
        
        tmp = cpu._testGetPcSpAFBCDEHL();
        cpu.cycle(c++);
        assertEquals(124,cpu._testGetPcSpAFBCDEHL()[6]);
        
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(0,Bits.make16(cpu._testGetPcSpAFBCDEHL()[8], cpu._testGetPcSpAFBCDEHL()[9]));
        
        cpu.cycle(c++);
        assertEquals(0xffff,Bits.make16(cpu._testGetPcSpAFBCDEHL()[8], cpu._testGetPcSpAFBCDEHL()[9]));
        
        tmp = cpu._testGetPcSpAFBCDEHL();
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(tmp[4],cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(tmp[4],bus.read(Bits.make16(tmp[8], tmp[9])));
        
        tmp = cpu._testGetPcSpAFBCDEHL();
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(tmp[2],bus.read(0x3412));
        
        tmp = cpu._testGetPcSpAFBCDEHL();
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        cpu.cycle(c++);
        assertEquals(Bits.make16(tmp[2], tmp[3]),Bits.make16(bus.read(cpu._testGetPcSpAFBCDEHL()[1]+1), bus.read(cpu._testGetPcSpAFBCDEHL()[1])));
        assertEquals(Bits.make16(tmp[2], tmp[3]),Bits.make16(bus.read(tmp[1]-1), bus.read(tmp[1]-2)));
        assertEquals(tmp[1]-2,cpu._testGetPcSpAFBCDEHL()[1]);
        
    }

}