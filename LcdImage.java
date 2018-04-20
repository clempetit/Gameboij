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
    private final List<LcdImageLine> lineList; //list de java.util ?
    
    public LcdImage(int width, int height, List<LcdImageLine> lineList) { // Verifications ?
        Preconditions.checkArgument(!(lineList.isEmpty()) && height == lineList.size());
        this.width = width;
        this.height = height;
        this.lineList = Collections.unmodifiableList(new ArrayList<>(lineList)); // copie de la liste ? ArrayList ?
    }
    
    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
    
    public int get(int x, int y) { // procéder comme ça ? definir color ou mettre des return directement ?
        boolean msbColor = lineList.get(y).msb().testBit(x);
        boolean lsbColor = lineList.get(y).lsb().testBit(x);
        int color;
        if (msbColor) {
            if (lsbColor) {
                color = 0b11;
            } else {
                color = 0b10;
            }
        } else {
            if (lsbColor) {
                color = 0b01;
            } else {
                color = 0b00;
            }
        }
        return color;
    }
    
    public boolean equals(LcdImage that) {
        if (this.lineList.size() != that.lineList.size()) {
            return false;
        }
        boolean equals = true;
        for(int i = 0; i< this.lineList.size(); i++) {
            if (!(this.lineList.get(i).equals(that.lineList.get(i)))) {
                equals = false;
            }
        }
        return equals;
    }
    
    public int hashcode() { // mettre width et height ?
        return Objects.hash(lineList);
    }
    
    public final class Builder {
        
        private int width, height;
        private List<LcdImageLine> lineList;
        
        public Builder(int width, int heigth) {
            this.width = width;
            this.height = height;
            lineList = new ArrayList<>(height); // ArrayList ?
        }
        
        public Builder setLine(int index, LcdImageLine newValue) {
            lineList.set(index, newValue);
            return this;
        }
        
        public LcdImage build() {
            return new LcdImage(width, height, lineList);
        }
    }
}
