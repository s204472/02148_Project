package common.src.main;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.concurrent.Task;


import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

public class Controller implements Initializable {
    public static int SIZE;
    public static boolean shipsPlaced = false;
    public static boolean rotated = false;
    public static GameBoard board;
    public static int lengthOfCurrentShip = 2;


    @FXML
    private Scene gameScene;

    @FXML
    public GridPane pGrid;
    @FXML
    public GridPane[] opponentBoards;
    @FXML
    public GridPane opponentBoardSpace;
    @FXML
    public Label lPlayer;
    @FXML
    public Label lStatusbar;
    @FXML
    public VBox msgArea;
    @FXML
    public TextField msgInput;
    @FXML
    public Button rotateBtn;
    @FXML
    public HBox widgetContainer;

    UiHelper ui = new UiHelper(SIZE);

    private Button[][] pButtons;
    private Button[][][] oButtons;

    private static int id;
    private static int numberOfPlayers;
    private static RemoteSpace idSpace;
    private static RemoteSpace serverToPlayer;
    private static RemoteSpace playerToServer;
    private static RemoteSpace chat;
    private boolean turn = false;
    private boolean gameover = false;


    private boolean gameOver = false;
    private int numberOfShips;
    private ArrayList<Integer> otherPlayers = new ArrayList<Integer>();


    @Override
    public void initialize(URL url, ResourceBundle resources) {
        String port = "9001"; String host = "localhost";

        try {
            idSpace        = new RemoteSpace("tcp://" + host + ":" + port + "/id?conn");
            serverToPlayer = new RemoteSpace("tcp://" + host + ":" + port + "/serverToPlayer?conn");
            playerToServer = new RemoteSpace("tcp://" + host + ":" + port + "/playerToServer?conn");
            chat           = new RemoteSpace("tcp://" + host + ":" + port + "/chat?conn");

            try {

                Object[] objects = idSpace.get(new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
                id = (int) objects[0];
                numberOfPlayers = (int) objects[1];
                SIZE = (int)objects[2];
                numberOfShips = (int) objects[3];
                playerToServer.put("User", id);
                lPlayer.setText("Player " + id);
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
        listenForWin();
        chatListener();
    }


    public void genPlayerBoard(int x, int y){
        pButtons = new Button[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                pButtons[i][j] = new Button();
                pButtons[i][j].getStyleClass().add("fields");
                int u = i;
                int v = j;
                pButtons[i][j].setOnAction(event -> handlePlayerClick(u, v));
                pButtons[i][j].hoverProperty().addListener(event -> {
                    showShipHover(u, v);
                });

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
            opponentBoards[i].getStyleClass().add("opnBoard");
            opponentBoardSpace.add(opponentBoards[i], m % 2, m / 2);
            m++;
        }
        for (int i : otherPlayers) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    oButtons[i][j][k] = new Button();
                    oButtons[i][j][k].getStyleClass().add("fields");
                    oButtons[i][j][k].getStyleClass().add("opn");
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
                lStatusbar.setText("Place ships");
                lengthOfCurrentShip = setShip(x, y, lengthOfCurrentShip);
                if (lengthOfCurrentShip == (numberOfShips+2)) {
                    lStatusbar.setText("Waiting for opponent to place ships");
                    playerToServer.put("Board", id, board);
                    shipsPlaced = true;
                }
            }
        } catch (InterruptedException e) {}
    }

    @FXML
    void handleOpnClick(int board, int x, int y) {
        try {
            if (this.turn && !gameOver){
                System.out.println("Player " + id + " shot at player " + board);
                playerToServer.put("Shot", id, x, y, board);
                System.out.println("Test");
                this.turn = false;
                lStatusbar.setText("Opponents turn");
                for (int i : otherPlayers){
                    ui.setInactive(oButtons[i]);
                }

            }
        } catch (InterruptedException e) {}
    }

    @FXML
    void handleSendClick() {
        try {
            String msg = msgInput.getText();
            if (!msg.equals("")){
                msgInput.clear();
                HBox msgContainer = new HBox();
                Text sender = new Text("You: ");
                sender.getStyleClass().add("msgYou");
                Text txt = new Text(msg + "\n");
                msgContainer.getChildren().add(sender);
                msgContainer.getChildren().add(txt);
                msgContainer.setPrefHeight(10);
                msgArea.getChildren().add(msgContainer);
                for (int i = 0; i < numberOfPlayers; i++){
                    if (i != id){
                        chat.put(id, i, msg);
                    }
                }
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
                            widgetContainer.getChildren().remove(rotateBtn);
                            lStatusbar.setText(id == 0 ? "Your turn" : "Opponents turn");
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
                serverToPlayer.query(new ActualField("Start"));
                while(true){
                    serverToPlayer.get(new ActualField("Turn"), new ActualField(id));
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            lStatusbar.setText("Your turn");
                            setTurn();
                            for (int i : otherPlayers){
                                ui.setActive(oButtons[i]);
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
    public void listenForGameOver(){
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                while(true){
                    Object[] res = serverToPlayer.get(new ActualField("Gameover"), new ActualField(id), new FormalField(Integer.class));
                    int playerHit = (int) res[2];

                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            if (playerHit == id){
                                setGameOver();
                                lStatusbar.setText("Gameover");
                                ui.setGameover(pButtons);
                            } else {
                                ui.setGameover(oButtons[playerHit]);
                                ui.setInactive(oButtons[playerHit]);
                                int tempIndex = otherPlayers.indexOf(playerHit);
                                otherPlayers.remove(tempIndex);
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
                                ui.setPlayerHit(pButtons, x, y, hit);
                            } else {
                                ui.setOpnHit(oButtons[boardShotAt], x, y, hit);
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
    public void listenForWin(){
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                serverToPlayer.get(new ActualField("Win"),  new ActualField(id));
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        lStatusbar.setText("Winner");
                    }
                });
            return 1;
            }

        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    public void setGameOver(){
        gameOver = true;
    }
    public void setTurn(){ this.turn = true; }

    public void chatListener(){
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                while(true){
                    Object[] res = chat.get(new FormalField(Integer.class), new ActualField(id), new FormalField(String.class));
                    int from = (int) res[0];
                    String msg = res[2].toString();
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            HBox msgContainer = new HBox();
                            msgContainer.getStyleClass().add("msgBox");
                            Text sender = new Text("Player " + from + ": ");
                            sender.getStyleClass().add("msgOpn");
                            Text txt = new Text(msg + "\n");
                            msgContainer.getChildren().add(sender);
                            msgContainer.getChildren().add(txt);
                            msgArea.getChildren().add(msgContainer);
                        }
                    });
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }


    public int setShip(int x, int y, int i) {
        if(SIZE < x + (rotated ? i : 0) || SIZE < y + (rotated ? 0 : i) || board.shipInTheway(x, y, i, rotated)) {
            return i;
        } else {
            for (int j = 0; j < i; j++) {
                board.placeShip(x + (rotated ? j : 0), y + (rotated ? 0 : j));
                ui.showShip(pButtons, x + (rotated ? j : 0), y + (rotated ? 0 : j));
            }
            return i + 1;
        }
    }


    public void showShipHover(int x, int y){
        int l = lengthOfCurrentShip;
        if (!shipsPlaced){
            if(!(SIZE < x + (rotated ? l : 0) || SIZE < y + (rotated ? 0 : l) || board.shipInTheway(x, y, l, rotated))) {
                for (int i = 0; i < l; i++) {
                    ui.toggleShipHover(pButtons[x + (rotated ? i : 0)][y + (rotated ? 0 : i)]);
                }
            }
        }
    /*public void showShip(int x, int y){
        pButtons[x][y].setStyle("-fx-background-color: #4f4f4f");
    }*/


}}


