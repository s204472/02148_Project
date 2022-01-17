package common.src.main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
    public static int SIZE;
    public static boolean shipsPlaced = false;
    public static boolean rotated = false;
    public static GameBoard board;
    public static int shipNumber = 2;

    @FXML
    public GridPane pGrid;
    @FXML
    public GridPane[] opponentBoards;
    @FXML
    public GridPane opponentBoardSpace;

    private Button[][] pButtons;
    private Button[][][] oButtons;

    private static int id;
    private static int numberOfPlayers;
    private static RemoteSpace idSpace;
    private static RemoteSpace serverToPlayer;
    private static RemoteSpace playerToServer;
    private boolean turn = false;
    private boolean gameOver = false;
    private ArrayList<Integer> otherPlayers = new ArrayList<Integer>();


    @Override
    public void initialize(URL url, ResourceBundle resources) {
        String port = "9000"; String host = "localhost";

        try {
            idSpace        = new RemoteSpace("tcp://" + host + ":" + port + "/id?conn");
            serverToPlayer = new RemoteSpace("tcp://" + host + ":" + port + "/serverToPlayer?conn");
            playerToServer = new RemoteSpace("tcp://" + host + ":" + port + "/playerToServer?conn");

            try {
                Object[] idAndPlayersAndSize = idSpace.get(new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
                id = (int) idAndPlayersAndSize[0];
                numberOfPlayers = (int) idAndPlayersAndSize[1];
                SIZE = (int)idAndPlayersAndSize[2];
                playerToServer.put("User", id);
                board = new GameBoard(SIZE);
                opponentBoards = new GridPane[numberOfPlayers];
                serverToPlayer.query(new ActualField("Place ships"));
                genPlayerBoard(SIZE, SIZE);
            } catch (InterruptedException e) {}
        } catch (IOException e) {}
        waitForOpnBoard();
        listenForTurn();
        listenForShots();
        listenForGameOver();
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

    public void genOpnBoard(int size, int numberOfPlayers){
        oButtons = new Button[numberOfPlayers][size][size];
        for (int i = 0; i < numberOfPlayers; i++) {
            if (i == id) {
                continue;
            } else {
                otherPlayers.add(i);
            }
        }
        int m = 0;
        for (int i : otherPlayers)  {
            opponentBoards[i] = new GridPane();
            opponentBoardSpace.add(opponentBoards[i], 0, m);
            m++;
        }
        for (int i : otherPlayers) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    oButtons[i][j][k] = new Button();
                    int x = i;
                    int u = j;
                    int v = k;
                    oButtons[i][j][k].setOnAction(event -> handleOpnClick(x, u, v));
                    opponentBoards[i].add(oButtons[i][j][k], j, k);
                }
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
    void handleOpnClick(int board, int x, int y) {
        try {
            if (this.turn && !gameOver){ // TODO: DON'T SHOOT AT SAME FIELD MORE THAN ONCE
                System.out.println("Shot by player on " + x + ":" + y);
                playerToServer.put("Shot", id, x, y, board);
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
                            genOpnBoard(SIZE, numberOfPlayers);
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
    public void listenForGameOver(){
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                serverToPlayer.query(new ActualField("Gameover"));
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        setGameOver();
                    }
                });
                return 1;
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
                    Object[] res = serverToPlayer.get(new ActualField("Shot"),  new ActualField(id), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Boolean.class));
                    int x = (int) res[2];
                    int y = (int) res[3];
                    int boardShotAt = (int) res[4];
                    boolean hit = (boolean) res[5];

                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            if (boardShotAt == id){
                                setOwnBoardHit(x, y, hit);
                            } else {
                                setOtherPlayerHit(boardShotAt, x, y, hit);
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
    public void setOwnBoardHit(int x, int y, boolean hit){
        if (hit){
            pButtons[x][y].setStyle("-fx-background-color: #913133");
        } else {
            pButtons[x][y].setStyle("-fx-background-color: #7cbef4");
        }
    }
    public void setOtherPlayerHit(int boardShotAt, int x, int y, boolean hit){
        if (hit){
            oButtons[boardShotAt][x][y].setStyle("-fx-background-color: #f2686a");
        } else {
            oButtons[boardShotAt][x][y].setStyle("-fx-background-color: #7cbef4");
        }
    }
    public void setGameOver(){
        gameOver = true;
    }
    public void setTurn(){ this.turn = true; }

    public int setShip(int x, int y, int i) {
        if (rotated) {
            if(SIZE < x + i || board.shipInTheway(x, y, i, rotated)) {
                System.out.println("ship out of bound");
                return i;
            }
            else {
                for (int j = 0; j < i; j++) {
                    board.placeShip(x + j, y);
                    showShip(x + j, y);

                }
                return i + 1;
            }
        } else {
            if(SIZE < y + i || board.shipInTheway(x, y, i, rotated)) {
                System.out.println("ship out of bound or ships overlap");
                return i;
            }
            else {
                for (int j = 0; j < i; j++) {
                    board.placeShip(x, y + j);
                    showShip(x, y + j);
                }
                return i + 1;
            }
        }
    }
    public void showShip(int x, int y){
        pButtons[x][y].setStyle("-fx-background-color: #4f4f4f");

    }
}


