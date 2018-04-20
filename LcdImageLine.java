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
    public void shift(int distance) {
        msb.shift(distance);
        lsb.shift(distance);
        opacity.shift(distance);
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
    public LcdImageLine below(LcdImageLine that, BitVector opacity) { // pour le calcul de la nouvelle opacité, prendre that.opacity ?
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
    public LcdImageLine join(LcdImageLine that, int index) { // Quelle ligne en premier ?
        Preconditions.checkArgument(that.size()==this.size());
        BitVector newMsb = this.msb.extractWrapped(0, index).or(that.msb.extractWrapped(index, that.msb.size() - index).shift(index));
        BitVector newLsb = this.lsb.extractWrapped(0, index).or(that.lsb.extractWrapped(index, that.lsb.size() - index).shift(index));
        BitVector newOpacity = this.opacity.extractWrapped(0, index).or(that.opacity.extractWrapped(index, that.opacity.size() - index).shift(index));
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }
    
    /**
     * 
     * @param that
     * @return
     */
    public boolean equals(LcdImageLine that) { //verifier : instance de LcdImageLine ?
        return msb.equals(that.msb) && lsb.equals(that.lsb) && opacity.equals(that.opacity);
    }
    
    /**
     * 
     * @return
     */
    public int hashcode() {
        return Objects.hash(msb, lsb, opacity); // correct ?
    }
    
    public final class Builder { // attributs BitVector ou BitVector.Builder ?
        
        private BitVector.Builder msb;
        private BitVector.Builder lsb;
        
        public Builder(BitVector.Builder msb, BitVector.Builder lsb) { // ??? Verifier que leur taille sont égales
            this.msb = msb;
            this.lsb = lsb;
        }
        
        public Builder setByte(int index, int msbByte, int lsbByte) { // int ou byte ? checkBits8 ?
            msb.setByte(index, msbByte);
            lsb.setByte(index, lsbByte);
            return this;
        }
        
        public LcdImageLine Build() { // comment calculer opacité ?
            BitVector opacity = new BitVector(0000000, true);
            return new LcdImageLine(msb.build(), lsb.build(), opacity);
        }
    }
}
