package common.src.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class PlayerMain extends Application {
    public static void main(String[] args) {
        launch();
    }

    // Basic window settings and initializing.
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Controller.class.getResource("view.fxml"));
        loader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> aClass) {
                return new Controller();
            }
        });

        VBox root = loader.load();
        primaryStage.setTitle("Battleship");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
