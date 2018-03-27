/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class Timer implements Component, Clocked {

    private Cpu cpu;
    private boolean v0 = false;
    
    // Declaration of registers addresses.
    int DIV = 0;
    int TIMA = 0;
    int TMA = 0;
    int TAC = 0;
    
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
    }
    
    @Override
    public void cycle(long cycle) { 
        v0 = state();              
        
        DIV = Bits.clip(16, DIV + 4);
        
        incIfChange(v0);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address == AddressMap.REG_DIV) {
            return Bits.extract(DIV, 8, 8);
        } else if(address == AddressMap.REG_TIMA) {
            return TIMA;
        } else if(address == AddressMap.REG_TMA) {
            return TMA;
        } else if(address == AddressMap.REG_TAC) {
            return TAC;
        } else {
            return NO_DATA;
        }
        
    }

    @Override
    public void write(int address, int data) { // ECRITURE EN FF04 PROVOQUE LA MISE A ZERO DU COMPTEUR PRINCIPAL
        Preconditions.checkBits8(data);
        Preconditions.checkBits16(address);
        if (address == AddressMap.REG_DIV) {
            v0 = state();
            DIV = 0;
            incIfChange(v0);
        } else if(address == AddressMap.REG_TIMA) {
            TIMA = data;
        } else if(address == AddressMap.REG_TMA) {
            TMA = data;
        } else if(address == AddressMap.REG_TAC) {
            v0 = state();
            TAC = data;
            incIfChange(v0);
        }
    }
    
    private boolean state() {
        int i = 0;
        
        switch (Bits.extract(TAC, 0, 2)) {
        case 0b00: {
            i = 9;
        } break;
        case 0b01: {
            i = 3;
        } break;
        case 0b10: {
            i = 5;
        } break;
        case 0b11: {
            i = 7;
        } break;
        default: {
        } break;
        }
        return (Bits.test(TAC, 2) && Bits.test(DIV, i));
    }
    
    private void incIfChange(boolean v0) {
        if (v0 && !state()) {;
            if (TIMA == 0xFF) {
                cpu.requestInterrupt(Cpu.Interrupt.TIMER);
                TIMA = TMA;
            } else {
                TIMA += 1;
            }
        }
    }
}
