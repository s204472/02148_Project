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
    public GridPane pGrid;
    @FXML
    public GridPane oGrid;

    private Button[][] pButtons;
    private Button[][] oButtons;

    private static int id;
    private static RemoteSpace idSpace;
    private static RemoteSpace serverToPlayer;
    private static RemoteSpace playerToServer;
    private boolean turn = false;

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
                genPlayerBoard(SIZE, SIZE);


            } catch (InterruptedException e) {}
        } catch (IOException e) {}
        waitForOpnBoard();
        listenForTurn();
        listenForShots();

    }


    public void genPlayerBoard(int x, int y){
        pButtons = new Button[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                pButtons[i][j] = new Button();
                int u = i;
                int v = j;
                pButtons[i][j].setOnAction(event -> handlePlayerClick(u, v));
                pGrid.add(pButtons[i][j], i, j);
            }
        }
    }
    public void genOpnBoard(int x, int y){
        oButtons = new Button[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                oButtons[i][j] = new Button();
                int u = i;
                int v = j;
                oButtons[i][j].setOnAction(event -> handleOpnClick(u, v));
                oGrid.add(oButtons[i][j], i, j);
            }
        }
    }


    @FXML
    void handlePlayerClick(int x, int y) {
        try {
            if(!shipsPlaced) {
                shipNumber = setShip(x, y, shipNumber);
                if (shipNumber == 6) {
                    playerToServer.put("Board", id, board);
                    shipsPlaced = true;
                }
            }
        } catch (InterruptedException e) {}
    }
    @FXML
    void handleOpnClick(int x, int y) {
        try {
            if (this.turn){ // TODO: DON'T SHOOT AT SAME FIELD MORE THAN ONCE
                System.out.println("Shot by player on " + x + ":" + y);
                playerToServer.put("Shot", id, x, y);
                this.turn = false;
            }

        } catch (InterruptedException e) {}
    }

    @FXML
    void rotate() {
        rotated = !rotated;
    }


    public void waitForOpnBoard(){
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                serverToPlayer.query(new ActualField("Start"));
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        genOpnBoard(SIZE, SIZE);
                    }
                });
                return 1;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
    public void listenForTurn(){
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                while(true){
                    serverToPlayer.get(new ActualField("Turn"), new ActualField(id));
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            setTurn();
                        }
                    });
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    public void listenForShots(){
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                while(true){
                    System.out.println("Waiting for response");
                    Object[] res = serverToPlayer.get(new ActualField("Shot"), new FormalField(Integer.class), new ActualField(id), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Boolean.class));
                    int id_ = (int) res[1];
                    int x = (int) res[3];
                    int y = (int) res[4];
                    boolean hit = (boolean) res[5];

                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            if (id_ == id){
                                setOpnHit(x, y, hit);
                            } else {
                                setPlayerHit(x, y, hit);
                            }
                        }
                    });
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
    public void setPlayerHit(int x, int y, boolean hit){
        pButtons[x][y].setStyle(hit ? "-fx-background-color: Red" : "-fx-background-color: Yellow");
    }
    public void setOpnHit(int x, int y, boolean hit){
        oButtons[x][y].setStyle(hit ? "-fx-background-color: Red" : "-fx-background-color: Blue");
    }
    public void setTurn(){ this.turn = true; }
    public int setShip(int x, int y, int i) {
        if (rotated) {
            if(SIZE <= x + i) {
                System.out.printf("ship out of bound");
                return i;
            }
            else {
                for (int j = 0; j < i; j++) {
                    board.placeShip(x + j, y);
                    pButtons[x + j][y].setStyle("-fx-background-color: blue");
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
                    pButtons[x][y+j].setStyle("-fx-background-color: blue");
                }
                return i + 1;
            }
        }
    }
}


