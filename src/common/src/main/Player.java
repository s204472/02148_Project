package common.src.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Player extends Application {
    public static void main(String[] args){
        launch(args);





    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World");
        Button button = new Button();
        button.setText("click me");

        StackPane layout = new StackPane();
        layout.getChildren().add(button);

        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
