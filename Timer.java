/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class Timer implements Component, Clocked {

    private Cpu cpu;
    private Bus bus; // PROCEDER DE CETTE MANIERE ?
    private boolean v0 = false;
    private int mainCounter = 0;
    
    // Declaration of registers addresses.
    int DIV = AddressMap.REG_DIV;
    int TIMA = AddressMap.REG_TIMA;
    int TMA = AddressMap.REG_TMA;
    int TAC = AddressMap.REG_TAC;
    
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
    }
    
    @Override
    public void cycle(long cycle) { // COMMENT S'INCREMENTE LA TOTALITE DU COMPTEUR PRINCIPAL ?
        v0 = state();
        
        mainCounter = (mainCounter + 4) % 0xFFFF;
        bus.write(DIV, Bits.clip(8, mainCounter));
        
        incIfChange(v0);
    }

    @Override
    public int read(int address) { // VERIFIER QUE L'ADRESSE ET CELLE D'UN REGISTRE OU NON ?
        Preconditions.checkBits16(address);
        
        return bus.read(address);
    }

    @Override
    public void write(int address, int data) { //QUELLES VERIFICATIONS EFFECTUER ?
        Preconditions.checkBits8(data);
        Preconditions.checkBits16(address);
        if (address == TIMA || address == TAC) {
            v0 = state();
            bus.write(address, data);
            incIfChange(v0);
        }
        else {
            bus.write(address, data);
        }

    }
    
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    private boolean state() {
        int i = 0;
        
        switch (Bits.extract(bus.read(TAC), 0, 2)) {
        case 0b00:
            i = 9;
        case 0b01:
            i = 3;
        case 0b10:
            i = 5;
        case 0b11:
            i = 7;
        }
        return Bits.test(bus.read(TAC), 2) & Bits.test(bus.read(DIV), i);
    }
    
    private void incIfChange(boolean v0) {
        if (v0 && !state()) {
            int c2 = bus.read(TIMA);
            if (c2 == 0xFFFF) {
                cpu.requestInterrupt(Cpu.Interrupt.TIMER);
                bus.write(TIMA, bus.read(TMA));
            } else {
                bus.write(TIMA, c2 + 1);
            }
        }
    }
}
