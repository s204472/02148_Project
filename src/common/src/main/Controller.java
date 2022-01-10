package common.src.main;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.concurrent.Task;
import javafx.scene.layout.GridPane;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

public class Controller implements Initializable {
    public static final int SIZE = 4;

    @FXML
    public GridPane gameGrid;

    private Button[][] buttons;

    private static int id;
    private static RemoteSpace idSpace;
    private static RemoteSpace serverToPlayer;
    private static RemoteSpace playerToServer;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        String port = "9000"; String host = "localhost";

        try {
            idSpace        = new RemoteSpace("tcp://" + host + ":" + port + "/playerToServer?conn");
            serverToPlayer = new RemoteSpace("tcp://" + host + ":" + port + "/serverToPlayer?conn");
            playerToServer = new RemoteSpace("tcp://" + host + ":" + port + "/id?conn");

            try {
                id = (int) idSpace.get(new FormalField(Integer.class))[0];
                playerToServer.put("User", id);

            } catch (InterruptedException e) {}
        } catch (IOException e) {}

        genButtons(SIZE, SIZE);
        startListener();
    }
    public void genButtons(int x, int y){
        buttons = new Button[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                buttons[i][j] = new Button();
                int u = i;
                int v = j;
                buttons[i][j].setOnAction(event -> handleClick(u, v));
                gameGrid.add(buttons[i][j], i, j);
            }
        }
    }

    @FXML
    void handleClick(int x, int y) {
        try {
            System.out.println("Shot by player on " + x + ":" + y);
            playerToServer.put(id, x, y);
        } catch (InterruptedException e) {}

    }
    public void startListener(){
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                while(true){
                    System.out.println("Waiting for response");
                    Object[] res = serverToPlayer.get(new ActualField(id), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
                    System.out.println("Got response (" + res[1] + "," + res[2] + ") " + res[3]);
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            setHit((int) res[1], (int) res[2]);
                        }
                    });
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
    public void setHit(int x, int y){
        buttons[x][y].setStyle("-fx-background-color: MediumSeaGreen");
    }
}

