package common.src.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jspace.*;
import java.io.IOException;


public class Player extends Application {
    public static void main(String[] args){
        //launch(args);

        String port = "9000";
        String host = "localhost";

        String serverToPlayerURI = "tcp://" + host + ":" + port + "/serverToPlayer?conn";
        String playerToServerURI = "tcp://" + host + ":" + port + "/playerToServer?conn";
        String idURI = "tcp://" + host + ":" + port + "/id?conn";

        int id;
        try {
            RemoteSpace idSpace = new RemoteSpace(idURI);
            RemoteSpace serverToPlayer = new RemoteSpace(serverToPlayerURI);
            RemoteSpace playerToServer = new RemoteSpace(playerToServerURI);

            try {
                Object[] res = idSpace.get(new FormalField(Integer.class));
                id = (int) res[0];
                playerToServer.put("here is a message from a user", id);

            } catch (InterruptedException e) {}
        } catch (IOException e) {}
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

