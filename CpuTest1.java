/**
 *  @autor Clément Petit (282626)
 *  @autor Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class CpuTest1 {

    @Test
    void LD_R8_HLR() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.write(0, 0b01_000_110);
        cpu.cycle(0);

        assertEquals(0b01_000_110, cpu._testGetPcSpAFBCDEHL()[4]);
    }

    @Test
    void LD_A_HLRU_Atest() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        bus.write(0, 0b00101010);
        cpu.cycle(0);

        assertEquals(0b00101010, cpu._testGetPcSpAFBCDEHL()[2]);
    }

    @Test
    void LD_A_HLRU_HLtest() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);
        cpu.cycle(0);

        bus.write(0, 0b00111010);

        assertEquals(0b00101010, cpu._testGetPcSpAFBCDEHL()[2]);
    }

    @Test
    void LD_A_N8R() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        bus.write(0, 0b11110000);
        bus.write(1, 0b00000100);
        bus.write(AddressMap.REGS_START + 0b00000100, 3);

        cpu.cycle(0);

        assertEquals(3, cpu._testGetPcSpAFBCDEHL()[2]);
    }

    @Test
    void LD_A_CR() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        bus.write(0, 0b01_001_110); // write 0b01_001_110 in C
        cpu.cycle(0);

        bus.write(1, 0b11110010);

        bus.write(AddressMap.REGS_START + 0b01_001_110, 8);

        cpu.cycle(2);

        assertEquals(8, cpu._testGetPcSpAFBCDEHL()[2]);
    }

    @Test
    void LD_A_N16R() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        bus.write(0, 0b11111010);
        bus.write(1, 0b00000100);
        bus.write(2, 0b10000000);
        bus.write(0b1000000000000100, 5);
        cpu.cycle(0);

        assertEquals(5, cpu._testGetPcSpAFBCDEHL()[2]);
    }

    @Test
    void LD_R16SP_N16() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        bus.write(0, 0b00_00_0001); // load n16 in BC
        bus.write(1, 0b00000100);
        bus.write(2, 0b00000010);

        cpu.cycle(0);

        assertEquals(2, cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(4, cpu._testGetPcSpAFBCDEHL()[5]);
    }
    
    
    @Test
    void  LD_A_BCR() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        bus.write(0, 0b00_00_0001); // load n16 in BC
        bus.write(1, 0b00000100);
        bus.write(2, 0b00000010);

        cpu.cycle(0); //BC = 0b0000001000000100
        
        bus.write(3, 0b00001010);
        bus.write(0b0000001000000100,8);
        
        
        
        cpu.cycle(3);
    
        assertEquals(8, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(2, cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(4, cpu._testGetPcSpAFBCDEHL()[5]);
    }
    
    @Test
    void   LD_A_DER() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        bus.write(0, 0b00_01_0001); // load n16 in DE
        bus.write(1, 0b10000000);
        bus.write(2, 0b01000000);

        cpu.cycle(0); //DE = 0b0100000010000000
        
        bus.write(3, 0b00011010);
        bus.write(0b0100000010000000,99);
        
        
        
        cpu.cycle(3);
    
        
        
        assertEquals(0b10000000, cpu._testGetPcSpAFBCDEHL()[7]);
        assertEquals(0b01000000, cpu._testGetPcSpAFBCDEHL()[6]);
        assertEquals(99, cpu._testGetPcSpAFBCDEHL()[2]);
    }

    @Test
    void LD_R8_N8() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        bus.write(0, 0b00_101_110); // load n8 in L
        bus.write(1, 128);

        cpu.cycle(0);

        
        assertEquals(128, cpu._testGetPcSpAFBCDEHL()[9]);
    }
    
    @Test
    void POP_R16() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        bus.write(0, 0b11_10_0001); // pop HL    HL = BUS[SP] , SP = SP + 2


        cpu.cycle(0);
        
        
        assertEquals(0b11_10_0001, cpu._testGetPcSpAFBCDEHL()[9]);
        assertEquals(2, cpu._testGetPcSpAFBCDEHL()[1]);
    }
    
    @Test
    void LD_HLR_R8() {
        Cpu cpu = new Cpu();
        Bus bus = new Bus();
        Ram ram = new Ram(0xFFFF);
        RamController rc = new RamController(ram, 0);

        cpu.attachTo(bus);
        rc.attachTo(bus);

        
        bus.write(0, 0b00_10_0001); // load n16 in BC
        
        bus.write(1, 0b00000100);
        bus.write(2, 0b00000010);

     //HL = 0b0000001000000100
        
        bus.write(3, 0b00_010_110); // load n8 in D
        bus.write(4, 128);

        
        bus.write(5, 0b01110_010); // load D in bus[HL]
    
        
        
        
run(cpu);
afficher(cpu);


        assertEquals(128, bus.read(0b0000001000000100));
        assertEquals(128, cpu._testGetPcSpAFBCDEHL()[6]);
    }
    
    
    
    
    
    
    
    void run (Cpu cpu)
    {
        for (int i = 0 ; i <20 ; ++i)
        {
            cpu.cycle(i);
        }
    }

    void afficher(Cpu cpu) {
        int[] tab = cpu._testGetPcSpAFBCDEHL();
        System.out.println("PC : " + tab[0]);
        System.out.println("SP: " + tab[1]);
        System.out.println("A : " + tab[2]);
        System.out.println("F : " + tab[3]);
        System.out.println("B : " + tab[4]);
        System.out.println("C : " + tab[5]);
        System.out.println("D : " + tab[6]);
        System.out.println("E : " + tab[7]);
        System.out.println("H : " + tab[8]);
        System.out.println("L : " + tab[9]);

    }

}