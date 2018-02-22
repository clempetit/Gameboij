/*
 *	Author:      Clément Petit
 *	Date:        22 Feb 2018      
 */

package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

public final class RamController implements Component {
    
    private Ram ctrldRam;
    private int start;
    private int end;
    
    RamController(Ram ram, int startAddress, int endAddress) {
        ctrldRam = Objects.requireNonNull(ram);
        start = Preconditions.checkBits16(startAddress);
        end = Preconditions.checkBits16(endAddress);
        if ((endAddress - startAddress) > ram.size()) {
            throw new IllegalArgumentException();
        }
    }
    
    RamController(Ram ram, int startAddress) {
        this(ram, startAddress, startAddress + ram.size() - 1);
    }

    @Override
    public int read(int address) {
        int index = Preconditions.checkBits16(address) - start;
        if (ctrldRam.read(index) != 0) {
            return ctrldRam.read(index);
        }
        else {
            return Component.NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) {
        int index = Preconditions.checkBits16(address) - start;
        if (address > start && address < end) {
            ctrldRam.write(index, data);
        }

    }

}
