/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {
    
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
      }
    private final RegisterFile<Reg> banc8 = new RegisterFile<>(Reg.values());
    
    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }
    
    private int PC = 0;
    private int SP = 0;
    
    private int nextNonIdleCycle = 0;
    private Bus bus;
    
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);
    
    private static Opcode[] buildOpcodeTable(Opcode.Kind k) {
        Opcode[] table = new Opcode[256];
        for (Opcode o: Opcode.values()) {
            if (o.kind == k) {
                table[o.encoding] = o;
            }
        }
        return table;
    }
    private void dispatch(Opcode op) {
        
       switch (op.family) {
       case NOP: {
       } break;
       case LD_R8_HLR: {
           banc8.set(extractReg(op, 3), read8AtHl());
       } break;
       case LD_A_HLRU: {
           banc8.set(Reg.A, read8AtHl());
           setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL) + extractHlIncrement(op)));
       } break;
       case LD_A_N8R: {
           banc8.set(Reg.A, read8(AddressMap.REGS_START + read8AfterOpcode()));
       } break;
       case LD_A_CR: {
           banc8.set(Reg.A, read8(AddressMap.REGS_START + banc8.get(Reg.C)));
       } break;
       case LD_A_N16R: {
           banc8.set(Reg.A, read8(read16AfterOpcode()));
       } break;
       case LD_A_BCR: {
           banc8.set(Reg.A, read8(reg16(Reg16.BC)));
       } break;
       case LD_A_DER: {
           banc8.set(Reg.A, read8(reg16(Reg16.DE)));
       } break;
       case LD_R8_N8: {
           banc8.set(extractReg(op, 3), read8AfterOpcode());
       } break;
       case LD_R16SP_N16: {
           setReg16SP(extractReg16(op), read16AfterOpcode());
       } break;
       case POP_R16: {
           setReg16(extractReg16(op), pop16());
       } break;
       case LD_HLR_R8: {
           write8AtHl(banc8.get(extractReg(op, 0)));
       } break;
       case LD_HLRU_A: {
           write8AtHl(banc8.get(Reg.A));
           setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL) + extractHlIncrement(op)));
       } break;
       case LD_N8R_A: {
           write8(AddressMap.REGS_START + read8AfterOpcode(), banc8.get(Reg.A));
       } break;
       case LD_CR_A: {
           write8(AddressMap.REGS_START + banc8.get(Reg.C), banc8.get(Reg.A));
       } break;
       case LD_N16R_A: {
           write8(read16AfterOpcode(), banc8.get(Reg.A));
       } break;
       case LD_BCR_A: {
           write8(reg16(Reg16.BC), banc8.get(Reg.A));
       } break;
       case LD_DER_A: {
           write8(reg16(Reg16.DE), banc8.get(Reg.A));
       } break;
       case LD_HLR_N8: {
           write8(reg16(Reg16.HL), read8AfterOpcode());
       } break;
       case LD_N16R_SP: {
           write16(read16AfterOpcode(), SP);
       } break;
       case LD_R8_R8: {
           Reg r = extractReg(op, 3);
           Reg s = extractReg(op, 0);
           if (r != s) {
               banc8.set(r, banc8.get(s));
           }
       } break;
       case LD_SP_HL: {
           SP = reg16(Reg16.HL);
       } break;
       case PUSH_R16: {
           push16(reg16(extractReg16(op)));
       } break;
       
       // Add
       case ADD_A_R8: {
       } break;
       case ADD_A_N8: {
       } break;
       case ADD_A_HLR: {
       } break;
       case INC_R8: {
       } break;
       case INC_HLR: {
       } break;
       case INC_R16SP: {
       } break;
       case ADD_HL_R16SP: {
       } break;
       case LD_HLSP_S8: {
       } break;

       // Subtract
       case SUB_A_R8: {
       } break;
       case SUB_A_N8: {
       } break;
       case SUB_A_HLR: {
       } break;
       case DEC_R8: {
       } break;
       case DEC_HLR: {
       } break;
       case CP_A_R8: {
       } break;
       case CP_A_N8: {
       } break;
       case CP_A_HLR: {
       } break;
       case DEC_R16SP: {
       } break;

       // And, or, xor, complement
       case AND_A_N8: {
       } break;
       case AND_A_R8: {
       } break;
       case AND_A_HLR: {
       } break;
       case OR_A_R8: {
       } break;
       case OR_A_N8: {
       } break;
       case OR_A_HLR: {
       } break;
       case XOR_A_R8: {
       } break;
       case XOR_A_N8: {
       } break;
       case XOR_A_HLR: {
       } break;
       case CPL: {
       } break;

       // Rotate, shift
       case ROTCA: {
       } break;
       case ROTA: {
       } break;
       case ROTC_R8: {
       } break;
       case ROT_R8: {
       } break;
       case ROTC_HLR: {
       } break;
       case ROT_HLR: {
       } break;
       case SWAP_R8: {
       } break;
       case SWAP_HLR: {
       } break;
       case SLA_R8: {
       } break;
       case SRA_R8: {
       } break;
       case SRL_R8: {
       } break;
       case SLA_HLR: {
       } break;
       case SRA_HLR: {
       } break;
       case SRL_HLR: {
       } break;

       // Bit test and set
       case BIT_U3_R8: {
       } break;
       case BIT_U3_HLR: {
       } break;
       case CHG_U3_R8: {
       } break;
       case CHG_U3_HLR: {
       } break;

       // Misc. ALU
       case DAA: {
       } break;
       case SCCF: {
       } break;
       }
       PC += op.totalBytes;
       nextNonIdleCycle += op.cycles;
    }
            
    @Override
    public void cycle(long cycle) {
        
        if (cycle == nextNonIdleCycle ) {
            dispatch(DIRECT_OPCODE_TABLE[read8(PC)]);  
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
        for (int i = 0; i < 8; i++) {
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
        return Preconditions.checkBits8(bus.read(address));
    }
    
    private int read8AtHl() {
        return Preconditions.checkBits8(read8(Bits.make16(banc8.get(Reg.H), banc8.get(Reg.L))));
    }
    
    private int read8AfterOpcode() {
        return Preconditions.checkBits8(read8(PC + 1));
    }
    
    private int read16(int address) {
        assert address != 0xFFFF;
        int lsb = Preconditions.checkBits8(read8(address));
        int msb = Preconditions.checkBits8(read8(address + 1));
        return Bits.make16(msb, lsb);
    }
    
    private int read16AfterOpcode() {
        assert PC < 0xFFFE;
        int lsb = Preconditions.checkBits8(read8(PC + 1));
        int msb = Preconditions.checkBits8(read8(PC + 2));
        return Bits.make16(msb, lsb);
    }
    
    private void write8(int address, int v) {
        Preconditions.checkBits8(v);
        bus.write(address, v);
    }
    
    private void write16(int address, int v) {
        assert address != 0xFFFF;
        Preconditions.checkBits16(v);
        bus.write(address, Bits.clip(8, v));
        bus.write(address + 1, Bits.extract(v, 8, 8));
    }
    
    private void write8AtHl(int v) {
        Preconditions.checkBits8(v);
        bus.write(Bits.make16(banc8.get(Reg.H), banc8.get(Reg.L)), v);
    }
    
    private void push16(int v) {
        assert SP != 0x1;
        Preconditions.checkBits16(v);
        SP = Bits.clip(16, SP - 2);
        bus.write(SP, Bits.clip(8, v));
        bus.write(SP + 1, Bits.extract(v, 8, 8));
    }
    
    private int pop16() {
        assert SP != 0xFFFF;
        int address = SP;
        SP = Bits.clip(16, SP + 2);
        return read16(address);
    }
    
    // GESTION DES PAIRES DE REGISTRES
    
    private int reg16(Reg16 r) {
        int msb = Preconditions.checkBits8(banc8.get(Reg.values()[2 * r.index()]));
        int lsb = Preconditions.checkBits8(banc8.get(Reg.values()[2 * r.index() + 1]));
        return Bits.make16(msb, lsb);
    }
    
    private void setReg16(Reg16 r, int newV) {
        if (r == Reg16.AF) {
            Preconditions.checkBits16(newV);
            banc8.set(Reg.values()[2 * r.index()], Bits.extract(newV, 8, 8));
            banc8.set(Reg.values()[2 * r.index() + 1], Bits.clip(8, newV & (-1 << 4)));
        } else {
            Preconditions.checkBits16(newV);
            banc8.set(Reg.values()[2 * r.index()], Bits.extract(newV, 8, 8));
            banc8.set(Reg.values()[2 * r.index() + 1], Bits.clip(8, newV));
            }
    }
    
    private void setReg16SP(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        if (r == Reg16.AF) {
            SP = newV;
        } else {
            setReg16(r, newV);
        }
    }
    
    // EXTRACTION DES PARAMETRES
    
    private Reg extractReg(Opcode opcode, int startBit) {
        Reg[] regTab = {Reg.B, Reg.C, Reg.D, Reg.E, Reg.H, Reg.L, null, Reg.A};
        return regTab[Bits.extract(opcode.encoding, startBit, 3)];
    }
    
    private Reg16 extractReg16(Opcode opcode) {
        Reg16[] reg16Tab = {Reg16.BC, Reg16.DE, Reg16.HL, Reg16.AF};
        return reg16Tab[Bits.extract(opcode.encoding, 4, 2)];
    }
    
    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? -1 : 1;
    }
    
}
