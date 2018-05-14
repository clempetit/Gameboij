/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import static ch.epfl.gameboj.Preconditions.checkArgument;

/**
 * represents a bit vector whose size is a strictly positive multiple of 32.
 */
public final class BitVector {

    private static final int nbOfBytesInInt = Integer.SIZE / Byte.SIZE;
    private static final int BYTE_MASK = 0b1111_1111;

    private final int[] vector;

    private enum extensionType {
        zero, wrapped
    };

    /**
     * builds a bit vector of the given size and of which all bits have the
     * value 0 if initValue is false and 1 otherwise.
     * 
     * @param sizeInBits
     *            the size in bits (must be a strictly positive multiple of 32)
     * 
     * @param initValue
     *            the initial value
     * @throws IllegalArgumentException
     *             if sizeInBits is invalid
     */
    public BitVector(int sizeInBits, boolean initValue) {
        checkArgument(
                sizeInBits > 0 && (sizeInBits % Integer.SIZE == 0));
        vector = new int[sizeInBits / Integer.SIZE];
        if (initValue)
            Arrays.fill(vector, ~0);
    }

    /**
     * builds a bit vector of the given size and of which all bits have the
     * value 0.
     * 
     * @param sizeInBits
     *            the size in bits (must be a multiple of 32 strictly positive)
     * @throws IllegalArgumentException
     *             if sizeInBits is invalid
     */
    public BitVector(int sizeInBits) {
        this(sizeInBits, false);
    }

    private BitVector(int[] vector) {
        this.vector = vector;
    }

    /**
     * @return the size of the vector in bits.
     */
    public int size() {
        return 32 * vector.length;
    }

    /**
     * determines if the bit of given index is true or false.
     * 
     * @param index
     *            the index (must be positive and strictly inferior to the size
     *            of the vector)
     * @throws IndexOutOfBoundsException
     *             if the index is invalid
     * @return true if the bit of given index is 1 and false otherwise
     */
    public boolean testBit(int index) {
        Objects.checkIndex(index, size());
        return Bits.test(vector[index / Integer.SIZE], index % Integer.SIZE);
    }

    /**
     * computes the complement of this bit vector.
     * 
     * @return a new bit vector which is the complement of this bit vector
     */
    public BitVector not() {
        int[] not = new int[vector.length];
        for (int i = 0; i < not.length; i++) {
            not[i] = ~vector[i];
        }
        return new BitVector(not);
    }

    /**
     * computes the bitwise conjunction with another vector of the same size.
     * 
     * @param that
     *            the other vector (must have the same length as this vector)
     * @throws IllegalArgumentException
     *             if the other vector is invalid
     * @return a new bit vector which is the bitwise conjunction between this
     *         vector and the other vector
     */
    public BitVector and(BitVector that) {
        int[] vector2 = that.vector;
        checkArgument(vector2.length == vector.length);
        int[] and = new int[vector.length];
        for (int i = 0; i < and.length; i++) {
            and[i] = vector[i] & vector2[i];
        }
        return new BitVector(and);
    }

    /**
     * computes the bitwise disjunction with another vector of the same size.
     * 
     * @param that
     *            the other vector (must have the same length as this vector)
     * @throws IllegalArgumentException
     *             if the other vector is invalid
     * @return a new bit vector which is the bitwise disjunction between this
     *         vector and another vector
     */
    public BitVector or(BitVector that) {
        int[] vector2 = that.vector;
        checkArgument(vector2.length == vector.length);
        int[] or = new int[vector.length];
        for (int i = 0; i < or.length; i++) {
            or[i] = vector[i] | vector2[i];
        }
        return new BitVector(or);
    }

    private int extensionElement(int index, extensionType type) {
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

    private BitVector extract(int sizeInBits, int start, extensionType type) {
        checkArgument(
                sizeInBits % Integer.SIZE == 0 && sizeInBits > 0);
        int[] extracted = new int[sizeInBits / Integer.SIZE];
        int startMod32 = Math.floorMod(start, Integer.SIZE);
        int startDiv32 = Math.floorDiv(start, Integer.SIZE);

        if (startMod32 == 0) {
            for (int i = 0; i < extracted.length; i++) {
                extracted[i] = extensionElement(startDiv32 + i, type);
            }
        } else {
            for (int i = 0; i < extracted.length; i++) {
                extracted[i] = extensionElement(startDiv32 + i, type) >>> start
                        | extensionElement(startDiv32 + i + 1,
                                type) << Integer.SIZE - start;
            }
        }
        return new BitVector(extracted);
    }

    /**
     * extracts a vector of given size from the extension by 0 of this vector.
     * 
     * @param start
     *            the start bit
     * @param sizeInBits
     *            the size in bits (must be a strictly positive multiple of 32)
     * @throws IllegalArgumentException
     *             if sizeInBits is invalid
     * @return a new bit vector which is the extracted vector of given size from
     *         the extension by 0 of this vector
     */
    public BitVector extractZeroExtended(int sizeInBits, int start) {
        return extract(sizeInBits, start, extensionType.zero);
    }

    /**
     * extracts a vector of given size from the extension by wrapping of this
     * vector.
     * 
     * @param start
     *            the start bit
     * @param sizeInBits
     *            the size in bits (must be a strictly positive multiple of 32)
     * @throws IllegalArgumentException
     *             if sizeInBits is invalid
     * @return a new bit vector which is the extracted vector of given size from
     *         the extension by wrapping of this vector
     */
    public BitVector extractWrapped(int sizeInBits, int start) {
        return extract(sizeInBits, start, extensionType.wrapped);
    }

    /**
     * shifts the vector by the given distance, using the usual convention that
     * a positive distance represents a shift to the left and a negative
     * distance a shift to the right.
     * 
     * @param distance
     *            the distance
     * @return a new bit vector which is this vector shifted by the given
     *         distance using the usual convention
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(size(), -distance);
    }

    /**
     * return the hash code of the vector.
     * 
     * @return the hash code of the vector
     */
    public int hashcode() {
        return Arrays.hashCode(vector);
    }

    /**
     * checks if the given object is a BitVector and if it is the same size and
     * has the same bits as this vector.
     * 
     * @param that
     *            the object
     * @return true if the given object is a BitVector and is equal to this
     *         vector and false otherwise
     */
    public boolean equals(Object that) {
        return (that instanceof BitVector)
                && Arrays.equals(vector, ((BitVector) that).vector);
    }

    /**
     * returns a representation of the vector as a string consisting only of
     * characters 0 and 1.
     * 
     * @return a representation of the vector as a string consisting only of
     *         characters 0 and 1
     */
    public String toString() {
        StringBuilder b = new StringBuilder(size());
        for (int i = 0; i < size(); i++) {
            b.append(testBit(i) ? 1 : 0);
        }
        return b.reverse().toString();
    }

    /**
     * represents a bit vector builder.
     */
    public final static class Builder {

        private int[] vector;

        /**
         * builds a bit vector of given size and of which all bits have the
         * value 0.
         * 
         * @param sizeInBits
         *            the size in bits (must be a strictly positive multiple of
         *            32)
         * @throws IllegalArgumentException
         *             if sizeInBits is invalid
         */
        public Builder(int sizeInBits) {
            checkArgument(
                    sizeInBits > 0 && sizeInBits % Integer.SIZE == 0);
            vector = new int[sizeInBits / Integer.SIZE];
        }

        /**
         * sets the value of a byte designated by its index.
         * 
         * @param index
         *            the index (must be positive and strictly inferior to the
         *            number of bytes in an integer (4) times the length of the
         *            vector)
         * @param newValue
         *            the new value (must be an 8 bits value)
         * @throws IllegalStateException
         *             if the vector is null
         * @throws IllegalArgumentException
         *             if newValue is invalid
         * @throws IndexOutOfBoundsException
         *             if the index is invalid
         * @return the builder
         */
        public Builder setByte(int index, int newValue) {
            if (vector == null) {
                throw new IllegalStateException();
            }
            Preconditions.checkBits8(newValue);
            if (!(index >= 0 && index < (nbOfBytesInInt * vector.length))) {
                throw new IndexOutOfBoundsException();
            }
            int indexInInt = index % nbOfBytesInInt;
            int indexInTab = index / nbOfBytesInInt;

            int mask = ~(BYTE_MASK << Byte.SIZE * (indexInInt));
            vector[indexInTab] = (vector[indexInTab] & mask)
                    | newValue << Byte.SIZE * (indexInInt);
            return this;
        }

        /**
         * builds the bit vector.
         * 
         * @throws IllegalStateException
         *             if the vector is null
         * @return the built bit vector
         */
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