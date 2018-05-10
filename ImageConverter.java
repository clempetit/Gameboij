/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;

public final class ImageConverter {
    
    private static final int[] COLOR_MAP = new int[] { 0xFF_FF_FF_FF, 0xDD_D3_D3_D3,
            0xFF_A9_A9_A9, 0xFF_00_00_00 };                                                   // ???
    
    public static Image convert(LcdImage li) {
        
        WritableImage wi = new WritableImage(li.width(), li.height());
        PixelWriter pw = wi.getPixelWriter();
        
        for (int y = 0; y < li.height(); ++y)
            for (int x = 0; x < li.width(); ++x)
                pw.setArgb(x, y, COLOR_MAP[li.get(x, y)]);
        
        return wi;
    }
}
