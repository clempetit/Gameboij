/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.gui;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public final class Main extends Application{
    
    private static Map<KeyCode, Key> keys = new HashMap<>();

    public static void main(String[] args) {
        keys.put(KeyCode.D, Key.RIGHT);
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
        
        File romFile = new File(argList.get(0));
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
        
        long start = System.nanoTime();                         // comment faire tourner avec un minuteur ?
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long start) {
                gb.runUntil(gb.cycles() + 17_556);
                Image image = ImageConverter.convert(gb.lcdController().currentImage());
                imageView.setImage(image);
            }
        };
        timer.start();
    }
}
