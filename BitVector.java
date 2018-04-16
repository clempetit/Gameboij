/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.bits;

import java.util.Arrays;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {
    private int[] vector;
    
    private enum extensionType{zero, wrapped};

    /**
     * 
     * @param sizeInBits
     * @param initValue
     */
    public BitVector(int sizeInBits, boolean initValue) {
     Preconditions.checkArgument(sizeInBits > 0 && (sizeInBits % 32 == 0));
     vector = new int[sizeInBits / 32];
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
        return Bits.test(vector[index / 32], index % 32);
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
     * @param vector2
     * @return
     */
    public BitVector and(int[] vector2) {
        Preconditions.checkArgument(vector2.length == vector.length);
        int[] and = new int[vector.length];
        for (int i = 0; i < and.length; i++) {
            and[i] = vector[i] & vector2[i];
        }
        return new BitVector(and);
    }
    
    /**
     * 
     * @param vector2
     * @return
     */
    public BitVector or(int[] vector2) {
        Preconditions.checkArgument(vector2.length == vector.length);
        int[] or = new int[vector.length];
        for (int i = 0; i < or.length; i++) {
            or[i] = vector[i] | vector2[i];
        }
        return new BitVector(or);
    }
    
    private int extensionElement(int index, extensionType type)  { // ???
       if (index < vector.length) {
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
    private BitVector extract(int start, int sizeInBits, extensionType type) { // ???
        int[] extracted = new int[sizeInBits / 32];
        int startMod32 = Math.floorMod(start, 32);
        int b = Math.floorDiv(start, 32);
        //TO IMPLEMENT
        if (startMod32 == 0) { // floormod
            for (int i = 0; i < extracted.length; i++) {
                extracted[i] = extensionElement(start +i, type);
            }
        } else {
            for (int i = 0; i < extracted.length; i++) {
                extracted[i] = extensionElement(b + i, type) >>> start | extensionElement(b + i + 1, type) << 32 - start;
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
    
    public boolean equals(BitVector that) {
        return Arrays.equals(vector, that.vector);
    }
    
    public final static class Builder {
        
        private int[] vectorBuilder;
        
        public Builder(int sizeInBits) {
            Preconditions.checkArgument(sizeInBits > 0 && sizeInBits % 32 == 0);
            vectorBuilder = new int[sizeInBits / 32];
        }
        
        public Builder setByte(int index, int newValue) { 
            if (vectorBuilder == null) {
                throw new IllegalStateException();
            }
            Preconditions.checkArgument(index >= 0 && index < (4*vectorBuilder.length));
            int nbOfBytesInt = Integer.SIZE / Byte.SIZE;
            int mask = ~(0b1111_1111 << 8*(index % nbOfBytesInt));
            vectorBuilder[index/nbOfBytesInt] =  (vectorBuilder[index/nbOfBytesInt] & mask) | newValue << 8 *(index % nbOfBytesInt);
            return this;
        }
        
        public BitVector Build() {
            if (vectorBuilder == null) {
                throw new IllegalStateException();
            }
            BitVector bitVector = new BitVector(vectorBuilder);
            vectorBuilder = null;
            return bitVector;
        }
    }
}
