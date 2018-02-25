/*
 *	Author:      Clément Petit 
 *	Date:        20 Feb 2018      
 */

package ch.epfl.gameboj;

/**
 * 
 * @author Yanis Berkani (
 *@author Clément Petit (
 */
public interface Preconditions {
    
    /**
     * throws the exception IllegalArgumentException if its parameter  is false
     * do nothing otherwise
     * @param b the condition
     */
    public static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * 
     * @param v
     * @return
     */
    public static int checkBits8(int v) {
        if (v<0 || v>0xFF) {
            throw new IllegalArgumentException();
        }
        return v;
    }
    
    /**
     * 
     * @param v
     * @return
     */
    public static int checkBits16(int v) {
        if (v<0 || v>0xFFFF) {
            throw new IllegalArgumentException();
        }
        return v;
    }
    
}
