/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * represents a GameBoy image.
 */
public final class LcdImage {
    
    private final int width, height;
    private final List<LcdImageLine> lineList;
    
    /**
     * initializes the width and the height of the image and the list of its
     * lines.
     * 
     * @param width
     *            the image width
     * @param height
     *            the image height (must be equal to the size of the image line
     *            list)
     * @param lineList
     *            the list of the image lines
     * @throws IllegalArgumentException
     *             if lineList is empty or the height is invalid
     */
    public LcdImage(int width, int height, List<LcdImageLine> lineList) {
        Preconditions.checkArgument(!(lineList.isEmpty()) && (height == lineList.size()));
        this.width = width;
        this.height = height;
        this.lineList = Collections.unmodifiableList(new ArrayList<>(lineList));
    }
    
    /**
     * returns the width of the image.
     * 
     * @return the image width
     */
    public int width() {
        return width;
    }
    
    /**
     * returns the height of the image.
     * 
     * @return the image height.
     */
    public int height() {
        return height;
    }
    
    /**
     * gets, in the form of an integer included between 0 and 3, the color of
     * the pixel of given index (x,y).
     * 
     * @param x
     *            the pixel abscissa (must be positive)
     * @param y
     *            the pixel ordinate (must be positive and strictly inferior to
     *            the size of this image line list)
     * @throws IllegalArgumentException
     *             if x or y is invalid
     * @return an integer included between 0 and 3 that represents the color of
     *         the pixel of given index (x,y)
     */
    public int get(int x, int y) {
        Preconditions.checkArgument(x >= 0 && y >= 0 && y < lineList.size());
        boolean msbColor = lineList.get(y).msb().testBit(x);
        boolean lsbColor = lineList.get(y).lsb().testBit(x);
        if (msbColor) {
            if (lsbColor) {
                return 3;
            } else {
                return 2;
            }
        } else {
            if (lsbColor) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    /**
     * checks if the given object is an LcdImage and if its image line list is
     * equal to this image line list.
     * 
     * @param that
     *            the object
     * @return true if the given object is an LcdImage and if its image line
     *         list is equal to this image line list and false otherwise
     */
    public boolean equals(Object that) {
        if (!(that instanceof LcdImage)) {
            return false;
        }
        return this.lineList.equals(((LcdImage)that).lineList);
    }
    
    /**
     * returns the hashcode of the list of the image lines .
     * 
     * @return the hashcode of the image lines list
     */
    public int hashcode() {
        return Objects.hash(lineList);
    }
    
    /**
     * represents an LcdImage builder.
     */
    public final static class Builder {
        
        private int width, height;
        private List<LcdImageLine> lineList;
        
        /**
         * initializes the width and the height of the image to build that is
         * initially empty, that is that all its pixels have the color 0.
         * 
         * @param width
         *            the image width (must be positive)
         * @param height
         *            the image height (must be positive)
         * @throws IllegalArgumentException
         *             if the width or the height is invalid
         */
        public Builder(int width, int height) {
            Preconditions.checkArgument(width >= 0 && height >= 0);
            this.width = width;
            this.height = height;
            lineList = new ArrayList<>(Collections.nCopies(height, new LcdImageLine.Builder(width).build()));
        }
        
        /**
         * sets the line of given index to the given value.
         * 
         * @param index
         *            the index (must be positive and strictly inferior to the
         *            size of the image line list)
         * @param newValue
         *            the value
         * @throws IndexOutOfBoundsException
         *             if the index is invalid
         * @return the builder
         */
        public Builder setLine(int index, LcdImageLine newValue) {
            Objects.checkIndex(index, lineList.size());
            Preconditions.checkArgument(newValue.size() == width);
            lineList.set(index, newValue);
            return this;
        }
        
        /**
         * returns the image built from that builder.
         * 
         * @return the built image
         */
        public LcdImage build() {
            return new LcdImage(width, height, lineList);
        }
    }
}
