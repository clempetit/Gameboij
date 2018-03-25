/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class Timer implements Component, Clocked {

    private Cpu cpu;
    private boolean v0 = false;
    
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
    }
    
    @Override
    public void cycle(long cycle) {
        // TODO Auto-generated method stub

    }

    @Override
    public int read(int address) {
        int a = AddressMap.REG_DIV;
        int b = AddressMap.REG_TIMA;
        int c = AddressMap.REG_TMA;
        int d = AddressMap.REG_TAC;
        
        return 0;
    }

    @Override
    public void write(int address, int data) {
        // TODO Auto-generated method stub

    }

    private boolean state() {
        int i = 0;
        switch (1) {
        case 0b00:
            i = 9;
        case 0b01:
            i = 3;
        case 0b10:
            i = 5;
        case 0b11:
            i = 7;
        }
        int c1 = 0; // CHANGER AVEC VALEUR A L'ADRESSE REG.DIV
        int c2 = 0; // CHANGER AVEC VALEUR A L'ADRESSE REG.TIMA
        return Bits.test(c2, 2) & Bits.test(c1, i);
    }
    
    private void incIfChange(boolean v0) {
        if (v0 && !state()) {
            // INCREMENTE COMPTEUR SECONDAIRE
        }
    }
}
