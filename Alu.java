/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
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
    
  /**
   * return a value whose bits corresponding to the different flags
   * are equal to 1 if and only if the argument is true and 0 otherwise.
   * @param z the boolean associated to the flag Z
   * @param n the boolean associated to the flag N
   * @param h the boolean associated to the flag H
   * @param c the boolean associated to the flag C
   * @return a value whose bits corresponding to the different flags
   * are equal to 1 if and only if the argument is true and 0 otherwise
   */
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
    
    /**
     * return the value contained in the package value/flags given.
     * @param valueFlags the package value/flags
     * @return the value contained in the package value/flags given
     */
    public static int unpackValue(int valueFlags) {
        return Bits.extract(valueFlags, 8, 24);
    }
    
    /**
     * return the flags contained in the package value/flags given.
     * @param valueFlags the package value/flags
     * @return the flags contained in the package value/flags given
     */
    public static int unpackFlags(int valueFlags) {
        return Bits.clip(8, valueFlags);
    }
    
    /**
     * return the sum of the two 8 bits values with the initial carry bit c0, 
     * and the flags Z0HC.
     * @param l the integer (must be an 8 bits value)
     * @param r the integer (must be an 8 bits value)
     * @param c0 the initial carry bit (0 if false and 1 if true)
     * @throws IllegalArgumentException if l or r is invalid
     * @return the sum of the two 8 bits values with the initial carry bit c0 
     * and the flags Z0HC
     */
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
    
    /**
     * return the sum of the two 8 bits values and the flags Z0HC.
     * @param l the integer (must be an 8 bits value)
     * @param r the integer (must be an 8 bits value)
     * @return the sum of the two 8 bits values and the flags Z0HC
     */
    public static int add(int l, int r) {
        return add(l,r,false);
    }
    
    /**
     * return the sum of the two 16 bits values and the flags 00HC where H and C
     * are the flags corresponding to the addition of the 8 less significant bits.
     * @param l the integer (must be a 16 bits value)
     * @param r the integer (must be a 16 bits value)
     * @throws IllegalArgumentException if l or r is invalid
     * @return the sum of the two 16 bits values and the flags 00HC where H and C
     * are the flags corresponding to the addition of the 8 less significant bits
     */
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int sum = l + r;
        int sum4 = Bits.clip(4, l) + Bits.clip(4, r);
        int sum8 = Bits.clip(8, l) + Bits.clip(8, r);
        boolean h = sum4 > 0xF;
        boolean c = sum8 > 0xFF;
        sum = Bits.clip(16,sum);
        return packValueZNHC(sum, false, false, h, c);
    }
    
    /**
     * return the sum of the two 16 bits values and the flags 00HC where H and C
     * are the flags corresponding to the addition of the 8 most significant bits.
     * @param l the integer (must be a 16 bits value)
     * @param r the integer (must be a 16 bits value)
     * @throws IllegalArgumentException if l or r is invalid
     * @return the sum of the two 16 bits values and the flags 00HC where H and C
     * are the flags corresponding to the addition of the 8 most significant bits
     */
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int sum = l + r;
        int sum12 = Bits.clip(12, l) + Bits.clip(12, r);
        boolean h = sum12 > 0xFFF;
        boolean c = sum > 0xFFFF;
        sum = Bits.clip(16,sum);
        return packValueZNHC(sum, false, false, h, c);
    }
    
    /**
     * return the difference between the two 8 bits values with the initial borrow b0,
     * and the flags Z1HC.
     * @param l the integer (must be an 8 bits value)
     * @param r the integer (must be an 8 bits value)
     * @param b0 the initial borrow (false is 0 and true is 1)
     * @throws IllegalArgumentException if l or r is invalid
     * @return the difference between the two 8 bits values with the initial borrow b0
     * and the flags Z1HC
     */
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int initBorrow = b0 ? 1 : 0;
        int sub = l - r - initBorrow;
        int sub4 = Bits.clip(4, l) - Bits.clip(4, r) - initBorrow;
        boolean h = sub4 < 0;
        boolean c = sub < 0;
        sub = Bits.clip(8,sub);
        return packValueZNHC(sub, sub == 0, true, h, c);
    }
    
    /**
     * return the difference between the two 8 bits values and the flags Z1HC.
     * @param l the integer (must be an 8 bits value)
     * @param r the integer (must be an 8 bits value)
     * @return the difference between the two 8 bits values and the flags Z1HC
     */
    public static int sub(int l, int r) {
        return sub(l,r,false);
    }
    
    /**
     * adjusts the given 8 bits value to represent it in binary coded decimal.
     * @param v the integer (must be an 8 bits value)
     * @param n the boolean associated to the flag N
     * @param h the boolean associated to the flag H
     * @param c the boolean associated to the flag C
     * @throws IllegalArgumentException if v is invalid
     * @return the BCD form of the given 8 bits value
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(v);
        boolean fixL = h || (!n && Bits.clip(4, v) > 0x9);
        boolean fixH = c || (!n && v > 0x99);
        int fix = 0x60 * (fixH ? 1 : 0) + 0x06 * (fixL ? 1 : 0);
        int va = Bits.clip(8, n ? (v - fix) : (v + fix));
        return packValueZNHC(va, va == 0, n, false, fixH);
    }
    
    /**
     * return the "and" of the two given 8 bits values and the flags Z010. 
     * @param l the integer (must be an 8 bits value)
     * @param r the integer (must be an 8 bits value)
     * @throws IllegalArgumentException if l or r is invalid
     * @return the "and" of the two given 8 bits values and the flags Z010
     */
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int inter = l & r;
        return packValueZNHC(inter, inter == 0, false, true, false);
    }
    
    /**
     * return the "or"(inclusive) of the two given 8 bits values and the flags Z000. 
     * @param l the integer (must be an 8 bits value)
     * @param r the integer (must be an 8 bits value)
     * @throws IllegalArgumentException if l or r is invalid
     * @return the "or"(inclusive) of the two given 8 bits values and the flags Z000
     */
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int union = l | r;
        return packValueZNHC(union, union == 0, false, false, false);
    }
    
    /**
     * return the "xor"(exclusive) of the two given 8 bits values and the flags Z000. 
     * @param l the integer (must be an 8 bits value)
     * @param r the integer (must be an 8 bits value)
     * @throws IllegalArgumentException if l or r is invalid
     * @return the "xor"(exclusive) of the two given 8 bits values and the flags Z000
     */
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int xUnion = l ^ r;
        return packValueZNHC(xUnion, xUnion == 0, false, false, false);
    }
    
    /**
     * return the given 8 bits value shifted of one bit to the left, and the
     * flags Z00C where C contains the bit ejected (false is 0 and true is 1).
     * @param v the integer (must be an 8 bits value)
     * @throws IllegalArgumentException if v is invalid
     * @return the given 8 bits value shifted of one bit to the left
     * and the flags Z00C where C contains the bit ejected
     */
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);
        boolean c = Bits.test(v, 7);
        int shifted = Bits.clip(8, v << 1);
        return packValueZNHC(shifted, shifted == 0, false, false, c);
    }
    
    /**
     * return the given 8 bits value shifted of one bit to the right in the arithmetic way,
     * and the flags Z00C where C contains the bit ejected (false is 0 and true is 1).
     * @param v the integer (must be an 8 bits value)
     * @throws IllegalArgumentException if v is invalid
     * @return the given 8 bits value shifted of one bit to the right in the arithmetic way
     * and the flags Z00C where C contains the bit ejected
     */
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        boolean c = Bits.test(v, 0);
        int shifted = Bits.set(v >>> 1, 7, Bits.test(v, 7));
        return packValueZNHC(shifted, shifted == 0, false, false, c);
    }
    
    /**
    * return the given 8 bits value shifted of one bit to the right in the logic way,
     * and the flags Z00C where C contains the bit ejected (false is 0 and true is 1).
     * @param v the integer (must be an 8 bits value)
     * @throws IllegalArgumentException if v is invalid
     * @return the given 8 bits value shifted of one bit to the right in the logic way
     * and the flags Z00C where C contains the bit ejected
     */
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);
        boolean c = Bits.test(v, 0);
        int shifted = v >>> 1;
        return packValueZNHC(shifted, shifted == 0, false, false, c);
    }
    
    /**
     * return the rotation of one bit in the given direction of the given 8 bits value, 
     * and the flags ZOOC where C contains the bit that went from an extremity to the other
     * during the rotation (false is 0 and true is 1).
     * @param d the direction
     * @param v the integer (must be an 8 bits value)
     * @throws IllegalArgumentException if v is invalid
     * @return the rotation of one bit in the given direction of the given 8 bits value
     * and the flags ZOOC where C contains the bit that went from an extremity to the other
     */
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);
        boolean throughLEFT = d.equals(RotDir.LEFT);
        boolean c = throughLEFT ? Bits.test(v, 7) : Bits.test(v, 0);
        int rotated = Bits.rotate(8, v, (throughLEFT ? 1 : -1));
        return packValueZNHC(rotated, rotated == 0, false, false, c);
    }
    
    /**
     * return the rotation through the carry in the given direction of the combination of 
     * the given 8 bits value with the carry flag C, and the flags Z00C where C contains the 
     * most significant bit.
     * @param d the direction
     * @param v the integer (must be an 8 bits value)
     * @param c the boolean associated to the flag C 
     * @throws IllegalArgumentException if v is invalid
     * @return the rotation through the carry in the given direction of the combination of 
     * the given 8 bits value with the given carry c and the flags Z00C
     */
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);
        int rotated = Bits.rotate(9, Bits.set(v, 8, c), (d.equals(RotDir.LEFT) ? 1 : -1));
        boolean carry = rotated > 0xFF;
        rotated = Bits.clip(8, rotated);
        return packValueZNHC(rotated, rotated == 0, false, false, carry);
    }
    
    /**
     * return the value obtained exchanging the four less significant bits with 
     * the four most significant bits of the given 8 bits value, and the flags Z000.
     * @param v the integer (must be an 8 bits value)
     * @throws IllegalArgumentException if v is invalid
     * @return the value obtained exchanging the four less significant bits with 
     * the four most significant bits of the given 8 bits value and the flags Z000.
     */
    public static int swap(int v) {
        Preconditions.checkBits8(v);
        return packValueZNHC(Bits.rotate(8, v, 4), v == 0, false, false, false);
    }
    
    /**
     * return the value 0 and the flags Z010 where Z is true if and only if
     * the bit of the given index of the given 8 bits value is 0.
     * @param v the integer (must be an 8 bits value)
     * @param bitIndex the index (must be included between 0 and 7)
     * @throws IllegalArgumentException if v is invalid
     * @throws IndexOutOfBoundsException if bitIndex is invalid
     * @return the value 0 and the flags Z010 where Z is true if and only if
     * the bit of the given index of the given 8 bits value is 0
     */
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        if (bitIndex < 0 || bitIndex > 7) {
            throw new IndexOutOfBoundsException();
        }
        return packValueZNHC(0, !Bits.test(v, bitIndex), false, true, false);
    }
}
