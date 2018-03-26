/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
    private Ram workRam;
    private RamController workRamCtrl;
    private RamController echoRamCtrl;
    private Cpu cpu;
    private BootRomController bcr;
    private Timer timer;
    private int SimulatedCycles = 0;
    
    public GameBoy(Cartridge cartridge){
        Objects.requireNonNull(cartridge);
        bus = new Bus();
        workRam = new Ram(AddressMap.WORK_RAM_SIZE);
        
        workRamCtrl = new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        workRamCtrl.attachTo(bus);
        
        echoRamCtrl = new RamController(workRam, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        echoRamCtrl.attachTo(bus);
        
        cpu = new Cpu();
        cpu.attachTo(bus);
        
        bcr = new BootRomController(cartridge);
        bcr.attachTo(bus);
        
        timer = new Timer(cpu);
        timer.attachTo(bus);
    }
    
    public Bus bus() {
        return bus;
    }
    
    public Cpu cpu() {
        return cpu;
    }
    
    public Timer timer() {
        return timer;
    }
    
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycle >= cycles());
        for (int i = 0; i<cycle; i++) {
            timer.cycle(i);
            cpu.cycle(i);
            SimulatedCycles++;
        }
    }
    
    public long cycles() {
        return SimulatedCycles;
    }
}