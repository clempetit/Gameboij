/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public final class GameBoy {

    private final Bus bus;
    private final Ram workRam;
    private final RamController workRamCtrl;
    private final RamController echoRamCtrl;
    private final Cpu cpu;
    private final BootRomController bcr;
    private final Timer timer;
    private LcdController lcdc;
    private long SimulatedCycles = 0;

    /**
     * builds a Game Boy creating the necessary components and attaching them to
     * a common bus.
     * 
     * @param cartridge
     *            the cartridge
     * @throws NullPointerException
     *             if the cartridge is null
     */
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        bus = new Bus();
        workRam = new Ram(AddressMap.WORK_RAM_SIZE);

        workRamCtrl = new RamController(workRam, AddressMap.WORK_RAM_START,
                AddressMap.WORK_RAM_END);
        workRamCtrl.attachTo(bus);

        echoRamCtrl = new RamController(workRam, AddressMap.ECHO_RAM_START,
                AddressMap.ECHO_RAM_END);
        echoRamCtrl.attachTo(bus);

        cpu = new Cpu();
        cpu.attachTo(bus);

        bcr = new BootRomController(cartridge);
        bcr.attachTo(bus);

        timer = new Timer(cpu);
        timer.attachTo(bus);
        
        lcdc = new LcdController(cpu);
        lcdc.attachTo(bus);
        
    }

    /**
     * @return the bus of the gameboy
     */
    public Bus bus() {
        return bus;
    }

    /**
     * @return the cpu of the gameboy
     */
    public Cpu cpu() {
        return cpu;
    }

    /**
     * @return the timer of the gameboy
     */
    public Timer timer() {
        return timer;
    }
    
    public LcdController lcdController() {
        return lcdc;
    }

    /**
     * runs the simulated gameboy until the given cycle minus 1, calling the
     * method cycle of the timer and then of the processor.
     * 
     * @param cycle
     *            the cycle
     */
    public void runUntil(long cycle) { // vérifier gestion cycles
        Preconditions.checkArgument(cycle >= cycles());
        while (cycles() < cycle) {
            timer.cycle(SimulatedCycles);
            lcdc.cycle(SimulatedCycles); 
            cpu.cycle(SimulatedCycles++);
        }
    }

    /**
     * @return the number of cycles already simulated
     */
    public long cycles() {
        return SimulatedCycles;
    }
}