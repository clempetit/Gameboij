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
        this(ram, startAddress, startAddress + ram.size());
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= start && address < end) {
            return ctrldRam.read(address - start);
        }
        else {
            return Component.NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) {
        int index = Preconditions.checkBits16(address) - start;
        int verifData = Preconditions.checkBits8(data);
        if (address >= start && address < end) {
            ctrldRam.write(index, verifData);
        }

    }

}
