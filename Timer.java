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

    private final Cpu cpu;

    // Declaration of registers addresses.
    int DIV = 0;
    int TIMA = 0;
    int TMA = 0;
    int TAC = 0;

    /**
     * builds a timer associated to the given processor.
     * 
     * @param cpu
     *            the processor
     * @throws NullPointerException
     *             if the processor is null
     */
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
    }

    @Override
    /**
     * updates the main timer, and the secondary if needed.
     */
    public void cycle(long cycle) {
        boolean s0 = state();
        DIV = Bits.clip(16, DIV + 4);
        incIfChange(s0);
    }

    @Override
    /**
     * gives access to registers.
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        switch (address) {
        case AddressMap.REG_DIV:
            return Bits.extract(DIV, 8, 8);
        case AddressMap.REG_TIMA:
            return TIMA;
        case AddressMap.REG_TMA:
            return TMA;
        case AddressMap.REG_TAC:
            return TAC;
        default:
            return NO_DATA;
        }
    }

    @Override
    /**
     * gives access to registers and update the secondary timer if needed.
     */
    public void write(int address, int data) {
        Preconditions.checkBits8(data);
        Preconditions.checkBits16(address);
        boolean s0 = state();
        switch (address) {
        case AddressMap.REG_DIV:
            DIV = 0;
            break;
        case AddressMap.REG_TIMA:
            TIMA = data;
            break;
        case AddressMap.REG_TMA:
            TMA = data;
            break;
        case AddressMap.REG_TAC:
            TAC = data;
            break;
        }
        incIfChange(s0);
    }

    private boolean state() {
        int i = 0;

        switch (Bits.extract(TAC, 0, 2)) {
        case 0b00: {
            i = 9;
        }
            break;
        case 0b01: {
            i = 3;
        }
            break;
        case 0b10: {
            i = 5;
        }
            break;
        case 0b11: {
            i = 7;
        }
            break;
        default: {
        }
            break;
        }
        return (Bits.test(TAC, 2) && Bits.test(DIV, i));
    }

    private void incIfChange(boolean s0) {
        if (s0 && !state()) {
            if (TIMA == 0xFF) {
                cpu.requestInterrupt(Cpu.Interrupt.TIMER);
                TIMA = TMA;
            } else {
                TIMA += 1;
            }
        }
    }
}