package common.src.main;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Menu extends Application {
    public static void main(String[] args) throws InterruptedException {launch(args);}

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Battleships");
        Scene game = FXMLLoader.load(Menu.class.getResource("customGame.fxml"));

        primaryStage.setScene(game);
        primaryStage.setHeight(600);
        primaryStage.setWidth(800);

        primaryStage.show();
    }
}
