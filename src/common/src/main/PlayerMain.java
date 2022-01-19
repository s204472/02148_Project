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
        primaryStage.setTitle("Battleship");
        VBox root = loader.load();
        Scene scene = new Scene(root);
        //Scene scene = FXMLLoader.load(PlayerMain.class.getResource("common/src/main/customGame.fxml"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
