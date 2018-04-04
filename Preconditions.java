/*
 *	Author:      Clément Petit 
 *	Date:        20 Feb 2018      
 */

package ch.epfl.gameboj;

/**
 * 
 * @author Clément Petit (282626)
 * @author Yanis Berkani (271348)
 * 
 */
public interface Preconditions {
    
    /**
     * throws the exception IllegalArgumentException if its argument is false
     * do nothing otherwise
     * @param b the condition
     * @throws IllegalArgumentException if the argument is false
     */
    public static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * return the argument if it is included between 0 and FF
     * @param v the integer checked (must be included between 0 and FF)
     * @throws IllegalArgumentException if the argument is invalid
     * @return the argument 
     */
    public static int checkBits8(int v) {
        if (v<0 || v>0xFF) {
            throw new IllegalArgumentException();
        }
        return v;
    }
    
    /**
     * return the argument if it is included between 0 and FFFF
     * @param v the integer checked (must be included between 0 and FFFF)
     * @throws IllegalArgumentException if the argument is invalid
     * @return the argument
     */
    public static int checkBits16(int v) {
        if (v<0 || v>0xFFFF) {
            throw new IllegalArgumentException();
        }
        return v;
    }
    
}