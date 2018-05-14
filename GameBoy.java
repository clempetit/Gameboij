/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * represents a Game Boy.
 */
public final class GameBoy {

    /**
     * the number of cycles executed per second.
     */
    public static final long CYCLES_PER_SECOND = 1 << 20;
    
    /**
     * the number of cycles executed per nanosecond.
     */
    public static final double CYCLES_PER_NANOSECOND = CYCLES_PER_SECOND / 1e9;
    
    private final Bus bus;
    private final Ram workRam;
    private final Cpu cpu;
    private final BootRomController bcr;
    private final Timer timer;
    private final LcdController lcdc;
    private final Joypad joypad;
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

        RamController workRamCtrl = new RamController(workRam, AddressMap.WORK_RAM_START,
                AddressMap.WORK_RAM_END);
        workRamCtrl.attachTo(bus);

        RamController echoRamCtrl = new RamController(workRam, AddressMap.ECHO_RAM_START,
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
        
        joypad = new Joypad(cpu);
        joypad.attachTo(bus);
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
    
    /**
     * @return the lcd controller
     */
    public LcdController lcdController() {
        return lcdc;
    }

    /**
     * @return the joypad
     */
    public Joypad joypad() {
        return joypad;
    }

    /**
     * runs the simulated gameboy until the given cycle minus 1, calling the
     * method cycle of the timer, then of the lcd controller and then of the
     * processor.
     * 
     * @param cycle
     *            the cycle
     */
    public void runUntil(long cycle) {
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