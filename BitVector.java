/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.bits;

import java.util.Arrays;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {
    
    private static final int intSize = Integer.SIZE;
    private static final int nbOfBytesInInt = Integer.SIZE / Byte.SIZE;
    
    private final int[] vector;
    
    private enum extensionType{zero, wrapped};

    /**
     * 
     * @param sizeInBits
     * @param initValue
     */
    public BitVector(int sizeInBits, boolean initValue) {
     Preconditions.checkArgument(sizeInBits > 0 && (sizeInBits % intSize == 0));
     vector = new int[sizeInBits / intSize];
     if (!initValue)
         Arrays.fill(vector, ~0);
    }
    
    /**
     * 
     * @param sizeInBits
     */
    public BitVector(int sizeInBits) {
        this(sizeInBits, false);
    }
    
    /**
     * 
     * @param vector
     */
    private BitVector(int[] vector) {
        this.vector = vector;
    }
    
    /**
     * @return the size in bits of the vector
     */
    public int size() {
        return 32 * vector.length;
    }
    
    /**
     * 
     * @param index
     * @return
     */
    public boolean testBit(int index) {
        Preconditions.checkArgument(index >= 0 && index < size());
        return Bits.test(vector[index / intSize], index % intSize);
    }
    
    /**
     * 
     * @return
     */
    public BitVector not() {
        int[] not = new int[vector.length];
        for (int i = 0; i < not.length; i++) {
            not[i] = ~vector[i];
        }
        return new BitVector(not);
    }
    
    /**
     * 
     * @param that
     * @return
     */
    public BitVector and(BitVector that) {
        int[] vector2 = that.vector;
        Preconditions.checkArgument(vector2.length == vector.length);
        int[] and = new int[vector.length];
        for (int i = 0; i < and.length; i++) {
            and[i] = vector[i] & vector2[i];
        }
        return new BitVector(and);
    }
    
    /**
     * 
     * @param that
     * @return
     */
    public BitVector or(BitVector that) {
        int[] vector2 = that.vector;
        Preconditions.checkArgument(vector2.length == vector.length);
        int[] or = new int[vector.length];
        for (int i = 0; i < or.length; i++) {
            or[i] = vector[i] | vector2[i];
        }
        return new BitVector(or);
    }
    
    private int extensionElement(int index, extensionType type)  {
       if (index >= 0 && index < vector.length) {
           return vector[index];
       } else {
           if (type == extensionType.zero) {
               return 0;
           } else {
               return vector[Math.floorMod(index, vector.length)];
           }
       }
    }
    
    /**
     * 
     * @param start
     * @param sizeInBits
     * @param Wrapped
     * @return
     */
    private BitVector extract(int start, int sizeInBits, extensionType type) {
        int[] extracted = new int[sizeInBits / intSize];
        int startMod32 = Math.floorMod(start, intSize);
        int startDiv32 = Math.floorDiv(start, intSize);
        
        if (startMod32 == 0) {
            for (int i = 0; i < extracted.length; i++) {
                extracted[i] = extensionElement(start +i, type);
            }
        } else {
            for (int i = 0; i < extracted.length; i++) {
                extracted[i] = extensionElement(startDiv32 + i, type) >>> start
                    | extensionElement(startDiv32 + i + 1, type) << intSize - start;
            }
        }
        return new BitVector(extracted);
    }
    
    /**
     * 
     * @param start
     * @param sizeInBits
     * @return
     */
    public BitVector extractZeroExtended(int start, int sizeInBits) {
        return extract(start, sizeInBits, extensionType.zero);
    }
    
    /**
     * 
     * @param start
     * @param sizeInBits
     * @return
     */
    public BitVector extractWrapped(int start, int sizeInBits) {
        return extract(start, sizeInBits, extensionType.wrapped);
    }
    
    /**
     * 
     * @param distance
     * @return
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(-distance, size());
    }
    
    public int hashcode() {
        return Arrays.hashCode(vector);
    }
    
    public boolean equals(Object that) {
        return (that instanceof BitVector)
                && Arrays.equals(vector, ((BitVector)that).vector);
    }
    
    public final static class Builder {
        
        private int[] vector;
        
        public Builder(int sizeInBits) {
            Preconditions.checkArgument(sizeInBits > 0 && sizeInBits % intSize == 0);
            vector = new int[sizeInBits / intSize];
        }
        
        public Builder setByte(int index, int newValue) {
            if (vector == null) {
                throw new IllegalStateException();
            }
            Preconditions.checkBits8(newValue);
            if (!(index >= 0 && index < (nbOfBytesInInt*vector.length))) {
                throw new IndexOutOfBoundsException();
            }
            int indexInInt = index % nbOfBytesInInt;
            int indexInTab = index / nbOfBytesInInt;
            
            int mask = ~(0b1111_1111 << Byte.SIZE*(indexInInt));
            vector[indexInTab] =  (vector[indexInTab] & mask)
                    | newValue << Byte.SIZE*(indexInInt);
            return this;
        }
        
        public BitVector build() {
            if (vector == null) {
                throw new IllegalStateException();
            }
            BitVector bitVector = new BitVector(vector);
            vector = null;
            return bitVector;
        }
    }
}
