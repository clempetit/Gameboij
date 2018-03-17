/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
    private Ram workRam;
    private RamController workRamCtrl;
    private RamController echoRamCtrl;
    private Cpu cpu;
    private int SimulatedCycles;
    
    public GameBoy(Object cartridge){
        bus = new Bus();
        workRam = new Ram(AddressMap.WORK_RAM_SIZE);
        workRamCtrl = new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        echoRamCtrl = new RamController(workRam, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        cpu = new Cpu();
        workRamCtrl.attachTo(bus);
        echoRamCtrl.attachTo(bus);
        cpu.attachTo(bus);
    }
    
    public Bus bus() {
        return bus;
    }
    
    public Cpu cpu() {
        return cpu;
    }
    
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycle <= cycles());
        int i = 0;
        while (i<cycle) {
            cpu.cycle(i++);
            SimulatedCycles++;
        }
    }
    
    public long cycles() {
        return SimulatedCycles;
    }
}