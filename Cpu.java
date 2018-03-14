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
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {
    
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
      }
    private final RegisterFile<Reg> bench8 = new RegisterFile<>(Reg.values());
    
    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }
    
    private int PC = 0;
    private int SP = 0;
    
    private int nextNonIdleCycle = 0;
    private Bus bus;
    
    private enum FlagSrc {
        V0, V1, ALU, CPU
    }
    
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);
    
    private static final Opcode[] PREFIXED_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.PREFIXED);
    
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
           bench8.set(extractReg(op, 3), read8AtHl());
       } break;
       case LD_A_HLRU: {
           bench8.set(Reg.A, read8AtHl());
           setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL) + extractHlIncrement(op)));
       } break;
       case LD_A_N8R: {
           bench8.set(Reg.A, read8(AddressMap.REGS_START + read8AfterOpcode()));
       } break;
       case LD_A_CR: {
           bench8.set(Reg.A, read8(AddressMap.REGS_START + bench8.get(Reg.C)));
       } break;
       case LD_A_N16R: {
           bench8.set(Reg.A, read8(read16AfterOpcode()));
       } break;
       case LD_A_BCR: {
           bench8.set(Reg.A, read8(reg16(Reg16.BC)));
       } break;
       case LD_A_DER: {
           bench8.set(Reg.A, read8(reg16(Reg16.DE)));
       } break;
       case LD_R8_N8: {
           bench8.set(extractReg(op, 3), read8AfterOpcode());
       } break;
       case LD_R16SP_N16: {
           setReg16SP(extractReg16(op), read16AfterOpcode());
       } break;
       case POP_R16: {
           setReg16(extractReg16(op), pop16());
       } break;
       case LD_HLR_R8: {
           write8AtHl(bench8.get(extractReg(op, 0)));
       } break;
       case LD_HLRU_A: {
           write8AtHl(bench8.get(Reg.A));
           setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL) + extractHlIncrement(op)));
       } break;
       case LD_N8R_A: {
           write8(AddressMap.REGS_START + read8AfterOpcode(), bench8.get(Reg.A));
       } break;
       case LD_CR_A: {
           write8(AddressMap.REGS_START + bench8.get(Reg.C), bench8.get(Reg.A));
       } break;
       case LD_N16R_A: {
           write8(read16AfterOpcode(), bench8.get(Reg.A));
       } break;
       case LD_BCR_A: {
           write8(reg16(Reg16.BC), bench8.get(Reg.A));
       } break;
       case LD_DER_A: {
           write8(reg16(Reg16.DE), bench8.get(Reg.A));
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
               bench8.set(r, bench8.get(s));
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
           int vf = Alu.add(bench8.get(Reg.A), bench8.get(extractReg(op, 0)), carryASH(op, true));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case ADD_A_N8: {
           int vf = Alu.add(bench8.get(Reg.A), read8AfterOpcode(), carryASH(op, true));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case ADD_A_HLR: {
           int vf = Alu.add(bench8.get(Reg.A), read8AtHl(), carryASH(op, true));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case INC_R8: {
           Reg r = extractReg(op, 3);
           int vf = Alu.add(bench8.get(r), 1);
           setRegFromAlu(r, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
       } break;
       case INC_HLR: {
           int vf = Alu.add(read8AtHl(), 1);
           write8AtHl(Alu.unpackValue(vf));
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
       } break;
       case INC_R16SP: {
           Reg16 r = extractReg16(op);
           incrementReg16SP(r);
       } break;
       case ADD_HL_R16SP: {
           int vf = Alu.add16H(reg16(Reg16.HL), reg16SP(extractReg16(op)));
           setReg16(Reg16.HL, Alu.unpackValue(vf));
           combineAluFlags(vf, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case LD_HLSP_S8: {
           int vf = Alu.add16L(SP, (byte)read8AfterOpcode());
           combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
           int v = Alu.unpackValue(vf);
           if (Bits.test(op.encoding, 4)) {
               setReg16(Reg16.HL, v);
           } else {
               SP = v;
           }
       } break;

       // Subtract
       case SUB_A_R8: {
           int vf = Alu.sub(bench8.get(Reg.A), bench8.get(extractReg(op, 0)), carryASH(op, true));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case SUB_A_N8: {
           int vf = Alu.sub(bench8.get(Reg.A), read8AfterOpcode(), carryASH(op, true));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case SUB_A_HLR: {
           int vf = Alu.sub(bench8.get(Reg.A), read8AtHl(), carryASH(op, true));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case DEC_R8: {
           Reg r = extractReg(op, 3);
           int vf = Alu.sub(bench8.get(r), 1);
           setRegFromAlu(r,vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
       } break;
       case DEC_HLR: {
           int vf = Alu.sub(read8AtHl(), 1);
           write8AtHl(Alu.unpackValue(vf));
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
       } break;
       case CP_A_R8: {
           int vf = Alu.sub(bench8.get(Reg.A), bench8.get(extractReg(op, 0)), carryASH(op, true));
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case CP_A_N8: {
           int vf = Alu.sub(bench8.get(Reg.A), read8AfterOpcode(), carryASH(op, true));
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case CP_A_HLR: {
           int vf = Alu.sub(bench8.get(Reg.A), read8AtHl(), carryASH(op, true));
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
       } break;
       case DEC_R16SP: {
           Reg16 r = extractReg16(op);
           decrementReg16SP(r);
       } break;

       // And, or, xor, complement
       case AND_A_N8: {
           int vf = Alu.and(bench8.get(Reg.A), read8AfterOpcode());
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.V0);
       } break;
       case AND_A_R8: {
           int vf = Alu.and(bench8.get(Reg.A), bench8.get(extractReg(op, 0)));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.V0);
       } break;
       case AND_A_HLR: {
           int vf = Alu.and(bench8.get(Reg.A), read8AtHl());
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.V0);
       } break;
       case OR_A_R8: {
           int vf = Alu.or(bench8.get(Reg.A), bench8.get(extractReg(op, 0)));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
       } break;
       case OR_A_N8: {
           int vf = Alu.or(bench8.get(Reg.A), read8AfterOpcode());
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
       } break;
       case OR_A_HLR: {
           int vf = Alu.or(bench8.get(Reg.A), read8AtHl());
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
       } break;
       case XOR_A_R8: {
           int vf = Alu.xor(bench8.get(Reg.A), bench8.get(extractReg(op, 0)));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
       } break;
       case XOR_A_N8: {
           int vf = Alu.xor(bench8.get(Reg.A), read8AfterOpcode());
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
       } break;
       case XOR_A_HLR: {
           int vf = Alu.xor(bench8.get(Reg.A), read8AtHl());
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
       } break;
       case CPL: {
           setRegFromAlu(Reg.A, Bits.complement8(bench8.get(Reg.A)));
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
           Reg r = extractReg(op, 0);
           int vf = Alu.swap(bench8.get(r));
           setRegFromAlu(r, vf);
           combineAluFlags(vf,FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
       } break;
       case SWAP_HLR: {
           int vf = Alu.swap(read8AtHl());
           write8AtHl(Alu.unpackValue(vf));
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
       } break;
       case SLA_R8: {
           Reg r = extractReg(op, 0);
           int vf = Alu.shiftLeft(bench8.get(r));
           setRegFromAlu(r, vf);
           combineAluFlags(vf,FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case SRA_R8: {
           Reg r = extractReg(op, 0);
           int vf = Alu.shiftRightA(bench8.get(r));
           setRegFromAlu(r, vf);
           combineAluFlags(vf,FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case SRL_R8: {
           Reg r = extractReg(op, 0);
           int vf = Alu.shiftRightL(bench8.get(r));
           setRegFromAlu(r, vf);
           combineAluFlags(vf,FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case SLA_HLR: {
           int vf = Alu.shiftLeft(read8AtHl());
           write8AtHl(Alu.unpackValue(vf));
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case SRA_HLR: {
           int vf = Alu.shiftRightA(read8AtHl());
           write8AtHl(Alu.unpackValue(vf));
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case SRL_HLR: {
           int vf = Alu.shiftRightL(read8AtHl());
           write8AtHl(Alu.unpackValue(vf));
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;

       // Bit test and set
       case BIT_U3_R8: {
           int f = Alu.testBit(bench8.get(extractReg(op, 0)), extractIndexBRS(op));
           combineAluFlags(f, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
       } break;
       case BIT_U3_HLR: {
           int f = Alu.testBit(read8AtHl(), extractIndexBRS(op));
           combineAluFlags(f, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
       } break;
       case CHG_U3_R8: {
           Reg r = extractReg(op, 0);
           bench8.set(r, SetOrRes(op, extractIndexBRS(op), bench8.get(r)));
       } break;
       case CHG_U3_HLR: {
           Reg r = extractReg(op, 0);
           write8AtHl(SetOrRes(op, extractIndexBRS(op), bench8.get(r)));
       } break;

       // Misc. ALU
       case DAA: {
           int vf = Alu.bcdAdjust(bench8.get(Reg.A), testFlag(Alu.Flag.N), testFlag(Alu.Flag.H), testFlag(Alu.Flag.C));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case SCCF: {
           bench8.setBit(Reg.F, Alu.Flag.C, carryASH(op, false));
           bench8.setBit(Reg.F, Alu.Flag.N, false);
           bench8.setBit(Reg.F, Alu.Flag.H, false);
       } break;
       default: {
       } break;
       }
       PC += op.totalBytes;
       nextNonIdleCycle += op.cycles;
    }
            
    @Override
    public void cycle(long cycle) {
        
        if (cycle == nextNonIdleCycle ) {
            if (read8(PC) != 0xCB) {
            dispatch(DIRECT_OPCODE_TABLE[read8(PC)]);
            } else {
                dispatch(PREFIXED_OPCODE_TABLE[read8(PC + 1)]);
            }
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
            tab[i+2] = bench8.get(Reg.values()[i]);
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
        return Preconditions.checkBits8(read8(Bits.make16(bench8.get(Reg.H), bench8.get(Reg.L))));
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
        bus.write(Bits.make16(bench8.get(Reg.H), bench8.get(Reg.L)), v);
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
        int msb = Preconditions.checkBits8(bench8.get(Reg.values()[2 * r.index()]));
        int lsb = Preconditions.checkBits8(bench8.get(Reg.values()[2 * r.index() + 1]));
        return Bits.make16(msb, lsb);
    }
    
    /**
     * return the value contained in the given 16 bits register just like reg16, but return SP if the register AF is given.
     * @param r the 1§ bits register 
     * @return the value contained in the 16 bits register, or SP if the register AF is given.
     */
    private int reg16SP(Reg16 r) {
        if (r == Reg16.AF) {
            return SP;
        } else {
            return reg16(r);
        }
    }
    
    private void setReg16(Reg16 r, int newV) {
        if (r == Reg16.AF) {
            Preconditions.checkBits16(newV);
            bench8.set(Reg.values()[2 * r.index()], Bits.extract(newV, 8, 8));
            bench8.set(Reg.values()[2 * r.index() + 1], Bits.clip(8, newV & (-1 << 4)));
        } else {
            Preconditions.checkBits16(newV);
            bench8.set(Reg.values()[2 * r.index()], Bits.extract(newV, 8, 8));
            bench8.set(Reg.values()[2 * r.index() + 1], Bits.clip(8, newV));
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
    /**
     * increment the given 16 bits register. If the latter is AF, then the register SP is incremented instead.
     * @param r the 16 bits register to increment
     */
    private void incrementReg16SP(Reg16 r) {
        if (r == Reg16.AF) {
            SP = Bits.clip(16, SP + 1);
        } else {
        setReg16(r, Bits.clip(16, reg16(r)+1));
        }
    }
    
    /**
     * Decrement the given 16 bits register. If the latter is AF, then the register SP is incremented instead.
     * @param r the 16 bits register to decrement
     */
    private void decrementReg16SP(Reg16 r) {
        if (r == Reg16.AF) {
            SP = Bits.clip(16, SP - 1);
        } else {
        setReg16(r, Bits.clip(16, reg16(r)-1));
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
    
    // GESTION  DES FANIONS
    
    private void setRegFromAlu(Reg r, int vf) {
        bench8.set(r, Alu.unpackValue(vf));
    }
    
    private void setFlags(int valueFlags) {
        bench8.set(Reg.F, Alu.unpackFlags(valueFlags));
    }
    
    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }
    
    private void write8AtHlAndSetFlags(int vf) {
        write8AtHl(Alu.unpackValue(vf));
        setFlags(vf);
    }
    
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        int flags = Alu.unpackFlags(vf);
        int v1Vector = flagVector(FlagSrc.V1, z, n, h, c);
        int aluVector = flagVector(FlagSrc.ALU, z, n, h, c) & flags;
        int cpuVector = flagVector(FlagSrc.CPU, z, n, h, c) & bench8.get(Reg.F);
        bench8.set(Reg.F, v1Vector | aluVector | cpuVector);
    }
    /**
     * return a mask representing the values of the flags corresponding to a given flag source.
     * @param ref the flag source
     * @param z the flag source of flag Z
     * @param n the flag source of flag N
     * @param h the flag source of flag H
     * @param c the flag source of flag C
     * @return a mask representing the values the flags corresponding to a given flag source.
     */
    private int flagVector (FlagSrc ref, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        return Alu.maskZNHC(ref == z, ref == n, ref == h, ref == c);
    }
    
    // EXTRACTION DES PARAMETRES
    private boolean extractDirRot(Opcode opcode) {
        return Bits.test(opcode.encoding, 3);
    }
    
    private int extractIndexBRS(Opcode opcode) {
        return Bits.extract(opcode.encoding, 3, 3);
    }
    
    private int SetOrRes(Opcode opcode, int index, int regValue) {
        if(Bits.test(opcode.encoding, 6)) {
           return regValue | Bits.mask(index); 
        } else {
           return regValue & Bits.complement8(Bits.mask(index));
        }
    }
    
    private boolean carryASH(Opcode opcode, boolean addOrSub) {
        boolean c = Bits.test(opcode.encoding, 3) && bench8.testBit(Reg.F, Alu.Flag.C);
        if (addOrSub) { 
        return c;
        } else {
            return !c;
        }
    }
    
    private boolean testFlag(Alu.Flag f) {
        return bench8.testBit(Reg.F, f);
    }
}
