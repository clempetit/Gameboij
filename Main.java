/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.gui;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public final class Main extends Application{
    
    private static Map<KeyCode, Key> keys = new HashMap<>();

    public static void main(String[] args) {
        keys.put(KeyCode.D, Key.RIGHT);             // Faire comme ça pour remplir la HashMap ?
        keys.put(KeyCode.A, Key.LEFT);
        keys.put(KeyCode.W, Key.UP);
        keys.put(KeyCode.S, Key.DOWN);
        keys.put(KeyCode.O, Key.A);
        keys.put(KeyCode.K, Key.B);
        keys.put(KeyCode.B, Key.SELECT);
        keys.put(KeyCode.N, Key.START);
        
        Application.launch(args);
      }

    @Override
    public void start(Stage stage) throws Exception {
        
        List<String> argList = getParameters().getRaw();
        
        if (argList.size() != 1)
            System.exit(1);
        
        File romFile = new File("LegendOfZelda.gb");
                //argList.get(0));
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(LcdController.LCD_WIDTH * 2);
        imageView.setFitHeight(LcdController.LCD_HEIGHT * 2);
        
        Image image = ImageConverter.convert(gb.lcdController().currentImage());
        imageView.setImage(image);
        
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(imageView);
        Scene scene = new Scene(borderPane);
        
        imageView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keys.get(event.getCode()) != null)
                    gb.joypad().keyPressed(keys.get(event.getCode()));
                else if (event.getCode() == KeyCode.C) {                            //Extension Capture d'écran
                    screenshot(gb);                                                //
                }
            }});
        
        imageView.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keys.get(event.getCode()) != null)
                    gb.joypad().keyReleased(keys.get(event.getCode()));
            }});
        
        stage.setScene(scene);
        stage.show();
        imageView.requestFocus();
        
        long start = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gb.runUntil((long)((now - start) * GameBoy.CYCLES_PER_NANOSECOND));    // procéder comme ça ?
                Image image = ImageConverter.convert(gb.lcdController().currentImage());
                imageView.setImage(image);
            }
        };
        timer.start();
    }
    
    private static void screenshot(GameBoy gb) {
        int[] COLOR_MAP = new int[] {
                0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
              };
        try {
        LcdImage li = gb.lcdController().currentImage();
        BufferedImage i =
          new BufferedImage(li.width(),
                li.height(),
                BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < li.height(); ++y)
          for (int x = 0; x < li.width(); ++x)
        i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
        ImageIO.write(i, "png", new File("gb.png"));
        } catch (IOException e) {
            System.out.println("Screenshot could not be saved");
        }
    }
}
