/**
 *	@author Clément Petit (282626)
 *	@author Yanis Berkani (271348)
 */

package ch.epfl.gameboj.gui;

import java.io.File;
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
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * contains the simulator's main program.
 */
public final class Main extends Application {

    private static Map<KeyCode, Key> keys = Map.of(
            KeyCode.D, Key.RIGHT,
            KeyCode.A, Key.LEFT,
            KeyCode.W, Key.UP,
            KeyCode.S, Key.DOWN,
            KeyCode.O, Key.A,
            KeyCode.K, Key.B,
            KeyCode.B, Key.SELECT,
            KeyCode.N, Key.START
            );

     /**
     * calls the method launch of Application with the given arguments.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Checks that only one argument has been passed to the program-the name of
     * the ROM file-and finish the execution otherwise. 
     * Creates a Game Boy whose cartridge is obtained from the ROM file passed 
     * in argument. 
     * Creates the graphical interface and then displays it on the screen.
     * Simulates the Game Boy by periodically updating the image displayed 
     * on the screen and reacting to the key presses corresponding to those 
     * of the Game Boy.
     */
    @Override
    public void start(Stage stage) throws Exception {

        List<String> argList = getParameters().getRaw();

        if (argList.size() != 1)
            System.exit(1);

        File romFile = new File("SuperMarioLand.gb");
                //argList.get(0));
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(LcdController.LCD_WIDTH * 2);
        imageView.setFitHeight(LcdController.LCD_HEIGHT * 2);   // comme ça ?

        Image image = ImageConverter.convert(gb.lcdController().currentImage()); // nécessaire de le mettre ici aussi ?
        imageView.setImage(image);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(imageView);
        Scene scene = new Scene(borderPane);

        imageView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keys.get(event.getCode()) != null)
                    gb.joypad().keyPressed(keys.get(event.getCode()));
            }
        });

        imageView.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keys.get(event.getCode()) != null)
                    gb.joypad().keyReleased(keys.get(event.getCode()));
            }
        });

        stage.setScene(scene);
        stage.show();
        imageView.requestFocus();

        long start = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gb.runUntil(
                        (long) ((now - start) * GameBoy.CYCLES_PER_NANOSECOND));
                Image image = ImageConverter
                        .convert(gb.lcdController().currentImage());
                imageView.setImage(image);
            }
        };
        timer.start();
    }
    
}
