/**
 *	@autor Clément Petit (282626)
 *	@autor Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class Alu {
    
    public enum Flag implements Bit{ UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z };
    
    public enum RotDir { LEFT, RIGHT };
    
    private static int packValueZNHC(int v,
            boolean z,
            boolean n,
            boolean h,
            boolean c) {
        Preconditions.checkBits16(v);
        return v << 8 | maskZNHC(z, n, h, c);
    }
    
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        int mask = 0;
        if(z) 
            mask += Flag.Z.mask();
        if(n)
            mask += Flag.N.mask();
        if(h)
            mask += Flag.H.mask();
        if(c)
            mask += Flag.C.mask();
        return mask;
    }
    
    public static int unpackValue(int valueFlags) {
        if(Bits.extract(valueFlags, 3, 12) << 3 != valueFlags &&
                Bits.extract(valueFlags, 3, 20) << 3 != valueFlags) {
            throw new IllegalArgumentException();
        }
        return valueFlags >>> 8;
    }
    
    public static int unpackFlags(int valueFlags) {
        if(Bits.extract(valueFlags, 3, 12) << 3 != valueFlags &&
                Bits.extract(valueFlags, 3, 20) << 3 != valueFlags) {
            throw new IllegalArgumentException();
        }
        return Bits.clip(8, valueFlags);
    }
    
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int initCarry = c0 ? 1 : 0;
        int sum = l + r + initCarry;
        int sum4 = Bits.clip(4, l) + Bits.clip(4, r) + initCarry;
        boolean h = sum4 > 0xF;
        boolean c = sum > 0xFF;
        sum = Bits.clip(8,sum);
        return packValueZNHC(sum, sum == 0, false, h, c);
    }
    
    public static int add(int l, int r) {
        return add(l,r,false);
    }
    
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int sum = l + r;
        int sum4 = Bits.clip(4, l) + Bits.clip(4, r);
        int sum8 = Bits.clip(8, l) + Bits.clip(8, r);
        boolean h = sum4 > 0xF;
        boolean c = sum8 > 0xFF;
        return packValueZNHC(Bits.clip(16,sum), false, false, h, c);
    }
    
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int sum = l + r;
        int sum12 = Bits.clip(12, l) + Bits.clip(12, r);
        boolean h = sum12 > 0xFFF;
        boolean c = sum > 0xFFFF;
        return packValueZNHC(Bits.clip(16,sum), false, false, h, c);
    }
    
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int initBorrow = b0 ? 1 : 0;
        int sum = l - r - initBorrow;
        int sum4 = Bits.clip(4, l) - Bits.clip(4, r) - initBorrow;
        boolean h = sum4 < 0;
        boolean c = sum < 0;
        sum = Bits.clip(8,sum);
        return packValueZNHC(sum, sum == 0, true, h, c);
    }
    
    public static int sub(int l, int r) {
        return sub(l,r,false);
    }
    
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(v);
        boolean fixL = h || (!n && Bits.clip(4, v) > 0x9);
        boolean fixH = c || (!n && v > 0x99);
        int fix = 0x60 * (fixH ? 1 : 0) + 0x06 * (fixL ? 1 : 0);
        int va = n ? (v - fix) : (v + fix);
        return packValueZNHC(va, va == 0, n, false, fixH);
    }
    
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int inter = l & r;
        return packValueZNHC(inter, inter == 0, false, true, false);
    }
    
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int union = l | r;
        return packValueZNHC(union, union == 0, false, false, false);
    }
    
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int xUnion = l ^ r;
        return packValueZNHC(xUnion, xUnion == 0, false, false, false);
    }
    
    public static int shiftLeft(int v) {
        return 0;
    }
    
    public static int shiftRightA(int v) {
        return 0;
    }
    
    public static int shiftRightL(int v) {
        return 0;
    }
    
    public static int rotate(RotDir d, int v) {
        return 0;
    }
    
    public static int rotate(RotDir d, int v, boolean c) {
        return 0;
    }
    
    public static int swap(int v) {
        return 0;
    }
    
    public static int testBit(int v, int bitIndex) {
        return 0;
    }
}
