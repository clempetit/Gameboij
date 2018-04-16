/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.bits;

import java.util.Arrays;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {
    private int[] vector;

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
    
    private int extensionElement(int index, boolean wrapped) { // ???
        
       return 0; 
    }
    
    /**
     * 
     * @param start
     * @param sizeInBits
     * @param Wrapped
     * @return
     */
    private BitVector extract(int start, int sizeInBits, boolean wrapped) { // ???
        int[] extracted = new int[sizeInBits / 32];
        //TO IMPLEMENT
        if (start % 32 == 0) {
            
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
        return extract(start, sizeInBits, false);
    }
    
    /**
     * 
     * @param start
     * @param sizeInBits
     * @return
     */
    public BitVector extractWrapped(int start, int sizeInBits) {
        return extract(start, sizeInBits, true);
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
        return vector.hashCode();
    }
    
    public boolean equals(BitVector that) { // A VERIFIER
        return vector.equals(that.vector);
    }
    
    public final static class Builder {
        
        private int[] vectorBuilder;
        
        public Builder(int sizeInBits) {
            Preconditions.checkArgument(sizeInBits > 0 && sizeInBits % 32 == 0);
            vectorBuilder = new int[sizeInBits / 32];
        }
        
        public Builder setByte(int index, int newValue) { // ???
            if (vectorBuilder == null) {
                throw new IllegalStateException();
            }
            Preconditions.checkArgument(index >= 0 && index <= (32*vectorBuilder.length - Byte.SIZE) && index % 8 == 0);
            vectorBuilder[index/32] =  vectorBuilder[index/32] | newValue << index % 4;
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
