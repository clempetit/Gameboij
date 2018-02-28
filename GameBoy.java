/**
 *  @autor Clément Petit (282626)
 *  @autor Yanis Berkani (271348)
 */

package ch.epfl.gameboj;

import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
    private Ram workRam;
    private RamController ramController1;
    private RamController ramController2;
    public GameBoy(Object cartridge){
        workRam = new Ram(AddressMap.WORK_RAM_SIZE);
        ramController1 = new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        ramController2 = new RamController(workRam, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        
        bus.attach(ramController1);
        bus.attach(ramController2);
    }
    
    public Bus bus() {
        return bus;
    }
}
