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
    
    private Register PC;
    private Register SP;
    
    private int nextNonIdleCycle;
    private Bus bus;
    
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);
    
    private static final Opcode[] buildOpcodeTable(Opcode.Kind k) {
        Opcode[] table = new Opcode[256];
        int i = 0;
        for (Opcode o: Opcode.values()) {
            if (o.kind == k) {
                table[i++] = o;
            }
        }
        return table;
    }
    private void dispatch() {
       switch (DIRECT_OPCODE_TABLE[PC.index()].family) {
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
            dispatch();
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
            tab[i+2] = Reg.values()[i];
        }
        return new int[0];
    }
    
    // ACCES AU BUS 
    
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }
    
    private int read8(int address) {
        return 0;
    }
    
    private int read8AtHl() {
        return 0;
    }
    
    private int read8AfterOpcode() {
        return 0;
    }
    
    private int read16(int address) {
        return 0;
    }
    
    private int read16AfterOpcode() {
        return 0;
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
        
    }
    
    private Reg16 extractReg16(Opcode opcode) {
        
    }
    
    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? 1 : -1;
    }
    
}
