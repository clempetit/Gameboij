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
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.memory.Ram;

public final class Cpu implements Component, Clocked {
    
    private Bus bus;
    private long nextNonIdleCycle = 0;
    
    private Ram highRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    private int hRamStart = AddressMap.HIGH_RAM_START;
    private int hRamEnd = AddressMap.HIGH_RAM_END;
    
    private int PC = 0;
    private int SP = 0;
    private boolean IME = false;
    private int IE = 0;
    private int IF = 0;
    
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
      }
    private final RegisterFile<Reg> bench8 = new RegisterFile<>(Reg.values());
    
    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }
    
    private enum FlagSrc {
        V0, V1, ALU, CPU
    }
    
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
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
    
    @Override
    public void cycle(long cycle) {
        if ((nextNonIdleCycle == Long.MAX_VALUE) && (interruptionNumber() >= 0)) {
            nextNonIdleCycle = cycle;
        }
        if (cycle == nextNonIdleCycle ) {
            reallyCycle(cycle);
        }  
    }
    
    public void reallyCycle(long cycle) {
        int i = interruptionNumber();
        if (IME & i >= 0) {
            IME = false;
            Bits.set(IF, i, false);
            nextNonIdleCycle += 5;
            push16(PC);
            PC = AddressMap.INTERRUPTS[i];
        } else {
        Opcode opcode;
        if (read8(PC) != 0xCB) {
            opcode = DIRECT_OPCODE_TABLE[read8(PC)];
            } else {
                opcode = PREFIXED_OPCODE_TABLE[read8(PC + 1)];
            }
        dispatch(opcode);
        }
    }
    
    
    private void dispatch(Opcode op) {
       boolean condition = false;
       int PC2 = PC + op.totalBytes;
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
           int e = Bits.clip(16, Bits.signExtend8(read8AfterOpcode()));
           int vf = Alu.add16L(SP, e);
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
           setRegFlags(Reg.A, vf);
       } break;
       case SUB_A_N8: {
           int vf = Alu.sub(bench8.get(Reg.A), read8AfterOpcode(), carryASH(op, true));
           setRegFlags(Reg.A, vf);
       } break;
       case SUB_A_HLR: {
           int vf = Alu.sub(bench8.get(Reg.A), read8AtHl(), carryASH(op, true));
           setRegFlags(Reg.A, vf);
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
           int vf = Alu.sub(bench8.get(Reg.A), bench8.get(extractReg(op, 0)));
           setFlags(vf);
       } break;
       case CP_A_N8: {
           int vf = Alu.sub(bench8.get(Reg.A), read8AfterOpcode());
           setFlags(vf);
       } break;
       case CP_A_HLR: {
           int vf = Alu.sub(bench8.get(Reg.A), read8AtHl());
           setFlags(vf);
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
           bench8.set(Reg.A, Bits.complement8(bench8.get(Reg.A)));
           combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1, FlagSrc.CPU);
       } break;

       // Rotate, shift
       case ROTCA: {
           RotDir d = extractDirRot(op) ? RotDir.RIGHT : RotDir.LEFT;
           int vf = Alu.rotate(d, bench8.get(Reg.A));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf,FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case ROTA: {
           RotDir d = extractDirRot(op) ? RotDir.RIGHT : RotDir.LEFT;
           int vf = Alu.rotate(d, bench8.get(Reg.A), bench8.testBit(Reg.F, Alu.Flag.C));
           setRegFromAlu(Reg.A, vf);
           combineAluFlags(vf,FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case ROTC_R8: {
           Reg r = extractReg(op, 0);
           RotDir d = extractDirRot(op) ? RotDir.RIGHT : RotDir.LEFT;
           int vf = Alu.rotate(d, bench8.get(r));
           setRegFromAlu(r, vf);
           combineAluFlags(vf,FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case ROT_R8: {
           Reg r = extractReg(op, 0);
           RotDir d = extractDirRot(op) ? RotDir.RIGHT : RotDir.LEFT;
           int vf = Alu.rotate(d, bench8.get(r), bench8.testBit(Reg.F, Alu.Flag.C));
           setRegFromAlu(r, vf);
           combineAluFlags(vf,FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case ROTC_HLR: {
           RotDir d = extractDirRot(op) ? RotDir.RIGHT : RotDir.LEFT;
           int vf = Alu.rotate(d, read8AtHl());
           write8AtHl(Alu.unpackValue(vf));
           combineAluFlags(vf,FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
       } break;
       case ROT_HLR: {
           RotDir d = extractDirRot(op) ? RotDir.RIGHT : RotDir.LEFT;
           int vf = Alu.rotate(d, read8AtHl(), bench8.testBit(Reg.F, Alu.Flag.C));
           write8AtHl(Alu.unpackValue(vf));
           combineAluFlags(vf,FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
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
           write8AtHl(SetOrRes(op, extractIndexBRS(op), read8AtHl()));
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
       
       // Jumps
       case JP_HL: {
           PC2 = reg16(Reg16.HL);
       } break;
       case JP_N16: {
           PC2 = read16AfterOpcode();
       } break;
       case JP_CC_N16: {
           if (extractCondition(op)) {
               condition = true;
               PC2 = read16AfterOpcode();
           }
       } break;
       case JR_E8: {
           PC2 += Bits.signExtend8(read8AfterOpcode());
       } break;
       case JR_CC_E8: {
           if (extractCondition(op)) {
               condition = true;
               PC2 += Bits.signExtend8(read8AfterOpcode());
           }
       } break;

       // Calls and returns
       case CALL_N16: {
           push16(PC2);
           PC2 = read16AfterOpcode();
       } break;
       case CALL_CC_N16: {
           if (extractCondition(op)) {
               condition = true;
               push16(PC2);
               PC2 = read16AfterOpcode();
           }
       } break;
       case RST_U3: {
           push16(PC + op.totalBytes);
           PC2 = AddressMap.RESETS[Bits.extract(op.encoding, 3, 3)];
       } break;
       case RET: {
           PC2 = pop16();
       } break;
       case RET_CC: {
           if (extractCondition(op)) {
               condition = true;
               PC2 = pop16();
           }
       } break;

       // Interrupts
       case EDI: {
           if (Bits.test(op.encoding, 3)) {
               IME = true;
           } else {
               IME = false;
           }
       } break;
       case RETI: {
           IME = true;
           PC2 = pop16();
       } break;

       // Misc control
       case HALT: {
           nextNonIdleCycle = Long.MAX_VALUE;
       } break;
       case STOP:
         throw new Error("STOP is not implemented");
       default: {
       } break;
       }
       PC = PC2;
       nextNonIdleCycle += op.cycles;
       if (condition) {
           nextNonIdleCycle += op.additionalCycles;
       }
    }
    
    public void requestInterrupt(Interrupt i) {
        IF = Bits.set(IF, i.index(), true);
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address == AddressMap.REG_IE) {
            return IE;
        } else if (address == AddressMap.REG_IF) {
            return IF;
        }
        else if (address >= hRamStart && address < hRamEnd) {
            return highRam.read(address - hRamStart);
        } else {
            return NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address == AddressMap.REG_IE) {
            IE = data;
        } else if (address == AddressMap.REG_IF) {
            IF = data;
        }
        else if (address >= hRamStart && address < hRamEnd) {
            highRam.write(address - hRamStart, data);
        }
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
        int lsb = read8(PC + 1);
        int msb = read8(PC + 2);
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
    
    private boolean extractCondition(Opcode opcode) {
        boolean z = bench8.testBit(Reg.F, Alu.Flag.Z);
        boolean c = bench8.testBit(Reg.F, Alu.Flag.C);
        boolean[] conditions = {!z, z, !c, c};
        return conditions[Bits.extract(opcode.encoding, 3, 2)];
    }
    
    private int interruptionNumber() {
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(Integer.lowestOneBit(IF & IE));
    }
}

