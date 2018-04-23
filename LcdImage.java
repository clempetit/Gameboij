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

public final class LcdImage {
    
    private final int width, height;
    private final List<LcdImageLine> lineList;
    
    public LcdImage(int width, int height, List<LcdImageLine> lineList) {
        Preconditions.checkArgument(!(lineList.isEmpty()) && (height == lineList.size()));
        this.width = width;
        this.height = height;
        this.lineList = Collections.unmodifiableList(new ArrayList<>(lineList));
    }
    
    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
    
    public int get(int x, int y) {
        boolean msbColor = lineList.get(y).msb().testBit(x);
        boolean lsbColor = lineList.get(y).lsb().testBit(x);
        if (msbColor) {
            if (lsbColor) {
                return 0b11;
            } else {
                return 0b10;
            }
        } else {
            if (lsbColor) {
                return 0b01;
            } else {
                return 0b00;
            }
        }
    }
    
    public boolean equals(Object that) {
        if (!(that instanceof LcdImage)) {
            return false;
        }
        return this.lineList.equals(((LcdImage)that).lineList);
    }
    
    public int hashcode() {
        return Objects.hash(lineList);
    }
    
    public final static class Builder {
        
        private int width, height;
        private List<LcdImageLine> lineList;
        
        public Builder(int width, int height) {
            this.width = width;
            this.height = height;
            lineList = new ArrayList<>(Collections.nCopies(height, null));
        }
        
        public Builder setLine(int index, LcdImageLine newValue) {
            Preconditions.checkArgument(index >= 0 && index < lineList.size());
            lineList.set(index, newValue);
            return this;
        }
        
        public LcdImage build() {
            return new LcdImage(width, height, lineList);
        }
    }
}
