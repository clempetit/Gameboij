/*
 *	Author:      Clément Petit
 *	Date:        20 Feb 2018      
 */

package ch.epfl.gameboj.component.memory;

import java.util.Arrays;

public final class Rom {
	private byte[] rom;
	
	public Rom(byte[] data) {
		if (data.length == 0) {
			throw new NullPointerException();
		}
		rom = Arrays.copyOf(data, data.length);
	}
	
	public int size() {
		return rom.length;
	}
	
	public int read(int index) {
		if (index<0 || index >= rom.length) {
			throw new IndexOutOfBoundsException();
		}
		return Byte.toUnsignedInt(rom[index]);
	}
	
}
