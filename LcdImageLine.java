/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * represents a Game Boy image line.
 */
public final class LcdImageLine {

    private static final int STANDARD_PALETTE = 0b11100100;

    private final BitVector msb;
    private final BitVector lsb;
    private final BitVector opacity;

    /**
     * initializes the bit vectors msb, lsb and opacity.
     * 
     * @param msb
     *            the bit vector containing the most significant bits
     * @param lsb
     *            the bit vector containing the least significant bits
     * @param opacity
     *            the bit vector containing the opacity
     * @throws IllegalArgumentException
     *             if the bit vectors are not all the same size
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && lsb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    /**
     * returns the length of the line in pixels.
     * 
     * @return the length of the line in pixels
     */
    public int size() {
        return msb.size();
    }

    /**
     * returns the bit vector containing the most significant bits.
     * 
     * @return the bit vector msb
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * returns the bit vector containing the least significant bits.
     * 
     * @return the bit vector lsb
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * returns the bit vector containing the opacity.
     * 
     * @return the bit vector opacity
     */
    public BitVector opacity() {
        return opacity;
    }

    /**
     * shifts the line by the given number of pixels, preserving its length.
     * 
     * @param distance
     *            the distance to shift expressed in pixels
     * @return the shifted line
     */
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance),
                opacity.shift(distance));
    }

    /**
     * extracts a line of given size from the infinite extension by wrapping,
     * from a given pixel.
     * 
     * @param start
     *            the start pixel
     * @param size
     *            the size
     * @return a line of given size from the infinite extension by wrapping from
     *         a given pixel
     */
    public LcdImageLine extractWrapped(int size, int start) {
        return new LcdImageLine(msb.extractWrapped(size, start),
                lsb.extractWrapped(size, start),
                opacity.extractWrapped(size, start));
    }

    /**
     * transforms the colors of the line according to the palette, given in the
     * form of an encoded byte.
     * 
     * @param palette
     *            the palette (must be an 8 bits value)
     * @throws IllegalArgumentException
     *             if the palette is invalid
     * @return the line with transformed colors
     */
    public LcdImageLine mapColors(int palette) {
        Preconditions.checkBits8(palette);
        if (palette == STANDARD_PALETTE) {
            return this;
        }
        BitVector newLsb = new BitVector(size(), false);
        BitVector newMsb = new BitVector(size(), false);

        BitVector notMsb = msb.not();
        BitVector notLsb = lsb.not();
        for (int i = 0; i < 4; i++) {
            BitVector l = Bits.test(i, 0) ? lsb : notLsb;
            BitVector m = Bits.test(i, 1) ? msb : notMsb;
            BitVector mask = m.and(l);
            newMsb = newMsb.or(mask
                    .and(new BitVector(size(), Bits.test(palette, 2 * i + 1))));
            newLsb = newLsb.or(
                    mask.and(new BitVector(size(), Bits.test(palette, 2 * i))));
        }
        return new LcdImageLine(newMsb, newLsb, opacity);
    }

    /**
     * composes this line with another of the same length, placed above it,
     * using the given opacity vector to perform the composition, the one of the
     * upper line being ignored.
     * 
     * @param that
     *            the other line (must be of the same size as this line)
     * @param opacity
     *            the opacity vector
     * @throws IllegalArgumentException
     *             if the other line is invalid
     * @return the composed line
     */
    public LcdImageLine below(LcdImageLine that, BitVector opacity) {
        Preconditions.checkArgument(that.size() == this.size());
        BitVector newMsb = (that.msb.and(opacity))
                .or(this.msb.and(opacity.not()));
        BitVector newLsb = (that.lsb.and(opacity))
                .or(this.lsb.and(opacity.not()));
        BitVector newOpacity = this.opacity.or(opacity);
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }

    /**
     * composes this line with another of the same length, placed above it,
     * using the opacity of the upper line to make the composition.
     * 
     * @param that
     *            the other line (must be of the same size as this line)
     * @throws IllegalArgumentException
     *             if the other line is invalid
     * @return the composed line
     */
    public LcdImageLine below(LcdImageLine that) { // (op & sup) | (!op & inf)
        return below(that, that.opacity);
    }

    /**
     * joins this line with another of the same length, from a pixel of given
     * index.
     * 
     * @param that
     *            the other line (must be of the same size as this line)
     * @param index
     *            the index (must be positive and strictly inferior to this
     *            line's size)
     * @throws IndexOutOfBoundsException
     *             if the index is invalid
     * @throws IllegalArgumentException
     *             if the other line is invalid
     * @return the joined line
     */
    public LcdImageLine join(LcdImageLine that, int index) {
        Objects.checkIndex(index, this.size());
        Preconditions.checkArgument(that.size() == this.size());
        BitVector maskLeft = new BitVector(size(), true).shift(index);
        BitVector maskRight = maskLeft.not();

        BitVector newMsb = (this.msb.and(maskRight)).or(that.msb.and(maskLeft));
        BitVector newLsb = (this.lsb.and(maskRight)).or(that.lsb.and(maskLeft));
        BitVector newOpacity = (this.opacity.and(maskRight))
                .or(that.opacity.and(maskLeft));
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }

    /**
     * checks if the other line is a LcdImageLine and if its three bit vectors
     * are equals to those of this line.
     * 
     * @param that
     *            the other line
     * @return true if the other line is a LcdImageLine and if its three bit
     *         vectors are equals to those of this line and false otherwise
     */
    public boolean equals(Object that) {
        return that instanceof LcdImageLine
                && msb.equals(((LcdImageLine) that).msb)
                && lsb.equals(((LcdImageLine) that).lsb)
                && opacity.equals(((LcdImageLine) that).opacity);
    }

    /**
     * returns the hashcode of the line.
     * 
     * @return the hashcode of the line
     */
    public int hashcode() {
        return Objects.hash(msb, lsb, opacity);
    }

    /**
     * represents a builder of image lines.
     */
    public final static class Builder {

        private final BitVector.Builder msb;
        private final BitVector.Builder lsb;

        /**
         * initializes the builders msb and lsb with a given size.
         * 
         * @param size
         *            the size (must be a strictly positive multiple of 32)
         */
        public Builder(int size) { // taille max ?
            Preconditions.checkArgument(size > 0);
            msb = new BitVector.Builder(size);
            lsb = new BitVector.Builder(size);
        }

        /**
         * sets the value of the high and low bytes of the line at a given
         * index.
         * 
         * @param index
         *            the index
         * @param msbByte
         *            the high bytes
         * @param lsbByte
         *            the low bytes
         * @return the builder
         */
        public Builder setBytes(int index, int msbByte, int lsbByte) {
            msb.setByte(index, msbByte);
            lsb.setByte(index, lsbByte);
            return this;
        }

        /**
         * builds the line with the bytes defined so far, in which all pixels of
         * color 0 are transparent, and the others opaque.
         * 
         * @return the line with the bytes defined so far in which all pixels of
         *         color 0 are transparent and the others opaque
         */
        public LcdImageLine build() {
            BitVector m = msb.build();
            BitVector l = lsb.build();
            BitVector opacity = m.or(l);
            return new LcdImageLine(m, l, opacity);
        }
    }
}
