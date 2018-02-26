/**
 *	@autor Clément Petit (282626)
 *	@autor Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.bits.Bit;

public final class Alu {
    
    public enum Flag implements Bit{ UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, Z, N, H, C };
    
    public enum RotDir { LEFT, RIGHT };
    
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        return 0;
    }
    
    public static int unpackValue(int valueFlags) {
        return 0;
    }
    
    public static int unpackFlags(int valueFlags) {
        return 0;
    }
    
    public static int add(int l, int r, boolean c0) {
        return 0;
    }
    
    public static int add(int l, int r) {
        return 0;
    }
    
    public static int add16L(int l, int r) {
        return 0;
    }
    
    public static int add16H(int l, int r) {
        return 0;
    }
    
    public static int sub(int l, int r, boolean b0) {
        return 0;
    }
    
    public static int sub(int l, int r) {
        return 0;
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
