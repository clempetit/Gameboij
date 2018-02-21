/*
 *	Author:      Clément Petit 
 *	Date:        20 Feb 2018      
 */

package ch.epfl.gameboj;

public interface Preconditions {
	
	public static void checkArgument(boolean b) {
		if (!b) {
			throw new IllegalArgumentException();
		}
	}
	
	public static int checkBits8(int v) {
		if (v<0 && v>0xFF) {
			throw new IllegalArgumentException();
		}
		return v;
	}
	
	public static int checkBits16(int v) {
		if (v<0 && v>0xFFFF) {
			throw new IllegalArgumentException();
		}
		return v;
	}
}
