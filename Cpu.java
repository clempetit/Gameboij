/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {
    
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
      }
    private RegisterFile<Reg> banc8 = new RegisterFile<>(Reg.values());
    
    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }
    
    private int PC;
    private int SP;
    
    private int nextNonIdleCycle;
    private Bus bus;
    
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);
    
    private static final Opcode[] buildOpcodeTable(Opcode.Kind k) {
        Opcode[] table = new Opcode[256];
        int i = 0;
        for (Opcode o: Opcode.values()) {
            if (o.kind == k) {
                table[o.encoding] = o;
            }
        }
        return table;
    }
    private void dispatch(Opcode op) {
        PC += op.totalBytes;
        nextNonIdleCycle += op.cycles;
       switch (op.family) {
       case NOP: {
       } break;
       case LD_R8_HLR: {
       } break;
       case LD_A_HLRU: {
       } break;
       case LD_A_N8R: {
       } break;
       case LD_A_CR: {
       } break;
       case LD_A_N16R: {
       } break;
       case LD_A_BCR: {
       } break;
       case LD_A_DER: {
       } break;
       case LD_R8_N8: {
       } break;
       case LD_R16SP_N16: {
       } break;
       case POP_R16: {
       } break;
       case LD_HLR_R8: {
       } break;
       case LD_HLRU_A: {
       } break;
       case LD_N8R_A: {
       } break;
       case LD_CR_A: {
       } break;
       case LD_N16R_A: {
       } break;
       case LD_BCR_A: {
       } break;
       case LD_DER_A: {
       } break;
       case LD_HLR_N8: {
       } break;
       case LD_N16R_SP: {
       } break;
       case LD_R8_R8: {
       } break;
       case LD_SP_HL: {
       } break;
       case PUSH_R16: {
       } break;
       }
    }
            
    @Override
    public void cycle(long cycle) {
        if (cycle == nextNonIdleCycle ) {
            dispatch(DIRECT_OPCODE_TABLE[bus.read(PC)]);  
        }
    }
    
    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        // TODO Auto-generated method stub

    }
    
    public int[] _testGetPcSpAFBCDEHL() {
        int[] tab = new int[10];
        tab[0] = PC;
        tab[1] = SP;
        for (int i = 0; i < 10; i++) {
            tab[i+2] = banc8.get(Reg.values()[i]);
        }
        return tab;
    }
    
    // ACCES AU BUS 
    
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }
    
    private int read8(int address) {
        return bus.read(address);
    }
    
    private int read8AtHl() {
        int address16 = (banc8.get(Reg.H)) << 8 | (banc8.get(Reg.L));
        return bus.read(address16);
    }
    
    private int read8AfterOpcode() {
        return bus.read(PC + 1);
    }
    
    private int read16(int address) {
        return bus.read(address) | bus.read(address + 1) << 8;
    }
    
    private int read16AfterOpcode() {
        return bus.read(PC + 1) | bus.read(PC + 2);
    }
    
    private void write8(int address, int v) {
        
    }
    
    private void write16(int address, int v) {
        
    }
    
    private void write8AtHl(int v) {
        
    }
    
    private void push16(int v) {
        
    }
    
    private int pop16() {
        return 0;
    }
    
    // GESTION DES PAIRES DE REGISTRES
    
    private int reg16(Reg16 r) {
        return 0;
    }
    
    private void setReg16(Reg16 r, int newV) {
        
    }
    
    private void setReg16SP(Reg16 r, int newV) {
        
    }
    
    // EXTRACTION DES PARAMETRES
    
    private Reg extractReg(Opcode opcode, int startBit) {
        return Reg.A;
    }
    
    private Reg16 extractReg16(Opcode opcode) {
        return Reg16.AF;
    }
    
    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? 1 : -1;
    }
    
}