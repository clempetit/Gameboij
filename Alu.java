/**
 *  @autor Clément Petit (282626)
 *  @autor Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class Alu {
    
    public enum Flag implements Bit{ UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z };
    
    public enum RotDir { LEFT, RIGHT };
    
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
        return valueFlags >>> 8;
    }
    
    public static int unpackFlags(int valueFlags) {
        return Bits.clip(8, valueFlags);
    }
    
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        return 0;
    }
    
    public static int add(int l, int r) {
        return add(l,r,false);
    }
    
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        return 0;
    }
    
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        
        return 0;
    }
    
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        return 0;
    }
    
    public static int sub(int l, int r) {
        return sub(l,r,false);
    }
    
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        return 0;
    }
    
    public static int and(int l, int r) {
        return 0;
    }
    
    public static int or(int l, int r) {
        return 0;
    }
    
    public static int xor(int l, int r) {
        return 0;
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
