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
    public static final int SIZE = 8;
    public static boolean shipsPlaced = false;
    public static boolean rotated = false;
    public static GameBoard board = new GameBoard(SIZE);
    public static int shipNumber = 2;

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
            idSpace        = new RemoteSpace("tcp://" + host + ":" + port + "/id?conn");
            serverToPlayer = new RemoteSpace("tcp://" + host + ":" + port + "/serverToPlayer?conn");
            playerToServer = new RemoteSpace("tcp://" + host + ":" + port + "/playerToServer?conn");

            try {
                id = (int) idSpace.get(new FormalField(Integer.class))[0];
                playerToServer.put("User", id);
                serverToPlayer.query(new ActualField("Placeships"));

                genButtons(SIZE, SIZE);

                //serverToPlayer.get(new ActualField("Start"));

            } catch (InterruptedException e) {}
        } catch (IOException e) {}
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
            if(!shipsPlaced) {
                shipNumber = setShip(x, y, shipNumber);
                if (shipNumber == 6) {
                    playerToServer.put("Board", id, board);
                    shipsPlaced = true;
                }
            } else {
                System.out.println("Shot by player on " + x + ":" + y);
                playerToServer.put(id, x, y);
            }
        } catch (InterruptedException e) {}
    }

    @FXML
    void rotate() {
        rotated = !rotated;
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

    public int setShip(int x, int y, int i) {
        if (rotated) {
            if(SIZE <= x + i) {
                System.out.printf("ship out of bound");
                return i;
            }
            else {
                for (int j = 0; j < i; j++) {
                    board.placeShip(x + j, y);
                    buttons[x + j][y].setStyle("-fx-background-color: blue");
                }
                return i + 1;
            }
        } else {
            if(SIZE <= y + i) {
                System.out.printf("ship out of bound");
                return i;
            }
            else {
                for (int j = 0; j < i; j++) {
                    board.placeShip(x, y + j);
                    buttons[x][y+j].setStyle("-fx-background-color: blue");
                }
                return i + 1;
            }
        }
    }
}


