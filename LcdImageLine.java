/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

public final class LcdImageLine {

    private final BitVector msb;
    private final BitVector lsb;
    private final BitVector opacity;
    
    /**
     * 
     * @param msb
     * @param lsb
     * @param opacity
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(msb.size() == lsb.size() && lsb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }
    
    /**
     * 
     * @return
     */
    public int size() {
        return msb.size();
    }
    
    /**
     * 
     * @return
     */
    public BitVector msb() {
        return msb;
    }
    
    /**
     * 
     * @return
     */
    public BitVector lsb() {
        return lsb;
    }
    
    /**
     * 
     * @return
     */
    public BitVector opacity() {
        return opacity;
    }
    
    /**
     * 
     * @param distance
     */
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance), opacity.shift(distance));
    }
    
    /**
     * 
     * @param start
     * @param size
     * @return
     */
    public LcdImageLine extractWrapped(int start, int size) {
        return new LcdImageLine(msb.extractWrapped(start, size),
                lsb.extractWrapped(start, size),
                opacity.extractWrapped(start, size));
    }
    
    /**
     * 
     * @param palette
     */
    public void mapColors(byte palette) { // ???
        for (int i = 0; i < msb.size(); i++) {
            if(msb.testBit(i) && lsb.testBit(i)) {
                
            }
        }
    }
    
    /**
     * 
     * @param that
     * @return
     */
    public LcdImageLine below(LcdImageLine that) { // (op & sup) | (!op & inf)
        Preconditions.checkArgument(that.size()==this.size());
        BitVector newMsb = (that.msb.and(that.opacity)).or(this.msb.and(that.opacity.not()));
        BitVector newLsb = (that.lsb.and(that.opacity)).or(this.lsb.and(that.opacity.not()));
        BitVector newOpacity = that.opacity.or(this.opacity);
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }
    
    /**
     * 
     * @param that
     * @param opacity
     * @return
     */
    public LcdImageLine below(LcdImageLine that, BitVector opacity) {
        Preconditions.checkArgument(that.size()==this.size());
        BitVector newMsb = (that.msb.and(opacity)).or(this.msb.and(opacity.not()));
        BitVector newLsb = (that.lsb.and(opacity)).or(this.lsb.and(opacity.not()));
        BitVector newOpacity = opacity.or(this.opacity);
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }
    
    /**
     * 
     * @param that
     * @param index
     * @return
     */
    public LcdImageLine join(LcdImageLine that, int index) { 
        Preconditions.checkArgument(that.size()==this.size());
        BitVector maskLeft = new BitVector(size(), true).shift(index);
        BitVector maskRight = new BitVector(size(), true).shift(index).not();
        
        BitVector newMsb = (this.msb.and(maskRight)).or(that.msb.and(maskLeft));
        BitVector newLsb = (this.lsb.and(maskRight)).or(that.lsb.and(maskLeft));
        BitVector newOpacity = (this.opacity.and(maskRight)).or(that.opacity.and(maskLeft));
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }
    
    /**
     * 
     * @param that
     * @return
     */
    public boolean equals(Object that) {
        return that instanceof LcdImageLine
                && msb.equals(((LcdImageLine)that).msb)
                && lsb.equals(((LcdImageLine)that).lsb)
                && opacity.equals(((LcdImageLine)that).opacity);
    }
    
    /**
     * 
     * @return
     */
    public int hashcode() {
        return Objects.hash(msb, lsb, opacity);
    }
    
    public final class Builder {
        
        private final BitVector.Builder msb;
        private final BitVector.Builder lsb;
        
        public Builder(int size) {
            Preconditions.checkArgument(size > 0);
            msb = new BitVector.Builder(size);
            lsb = new BitVector.Builder(size);
        }
        
        public Builder setBytes(int index, int msbByte, int lsbByte) {
            msb.setByte(index, msbByte);
            lsb.setByte(index, lsbByte);
            return this;
        }
        
        public LcdImageLine build() {
            BitVector m = msb.build();
            BitVector l = lsb.build();
            BitVector opacity = m.or(l);
            return new LcdImageLine(m, l, opacity);
        }
    }
}
