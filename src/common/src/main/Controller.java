package common.src.main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.concurrent.Task;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

public class Controller implements Initializable {


    @FXML
    public GridPane pGrid, opponentBoardGrid;
    @FXML
    public GridPane[] opponentBoards;
    @FXML
    public Label lPlayer, lStatusbar;
    @FXML
    public VBox msgArea;
    @FXML
    public TextField msgInput;
    @FXML
    public Button rotateBtn;
    @FXML
    public HBox widgetContainer;

    private UiHelper ui = new UiHelper();
    private ChatHelper ch;

    public static boolean shipsPlaced = false;
    public static boolean rotated = false;
    public static GameBoard board;
    public static int[][] shipConfig = {{2, 4}, {2, 3, 4}, {2, 3, 3, 4}, {2, 3, 3, 4, 5}, {2, 3, 3, 4, 4, 5}};
    private Button[][] pButtons;
    private Button[][][] oButtons;

    private static int id, numberOfShipsToPlace, numberOfPlayers, size;
    private static RemoteSpace idSpace, serverToPlayer, playerToServer, chat;
    private boolean turn = false, gameOver = false;
    private int numberOfShipsPlaced = 0;
    private ArrayList<Integer> otherPlayers = new ArrayList<Integer>();

    @Override
    public void initialize(URL url, ResourceBundle resources) {

        try {
            idSpace        = new RemoteSpace(Config.getURI("id"));
            serverToPlayer = new RemoteSpace(Config.getURI("serverToPlayer"));
            playerToServer = new RemoteSpace(Config.getURI("playerToServer"));
            chat           = new RemoteSpace(Config.getURI("chat"));

            try {
                Object[] objects = idSpace.get(new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
                id = (int) objects[0];
                numberOfPlayers = (int) objects[1];
                size = (int) objects[2];
                numberOfShipsToPlace = (int) objects[3];

                playerToServer.put("User", id);
                lPlayer.setText("Player " + id);
                board = new GameBoard(size);
                opponentBoards = new GridPane[numberOfPlayers];

                serverToPlayer.query(new ActualField("Place ships"));

                ch = new ChatHelper(id, numberOfPlayers, chat, msgArea);
                ch.listen();
                genPlayerBoard(size, size);
            } catch (InterruptedException e) {}
        } catch (IOException e) {}

        waitForOpnBoard();
        startListeners();

    }
    public void startListeners(){
        listenForTurn();
        listenForShots();
        listenForGameOver();
        listenForWin();
    }

    public void genPlayerBoard(int x, int y){
        pButtons = new Button[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                pButtons[i][j] = new Button();
                pButtons[i][j].getStyleClass().add("fields");
                int u = i, v = j;
                pButtons[i][j].setOnAction(event -> handlePlayerClick(u, v));
                pButtons[i][j].hoverProperty().addListener(event -> showShipHover(u, v));

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
        int playerCounter = 0;
        for (int i : otherPlayers)  {
            opponentBoards[i] = new GridPane();
            opponentBoards[i].getStyleClass().add("opnBoard");
            opponentBoardGrid.add(opponentBoards[i], playerCounter % 2, playerCounter / 2);
            playerCounter++;
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    oButtons[i][j][k] = new Button();
                    oButtons[i][j][k].getStyleClass().addAll("fields", "opn");
                    int x = i, u = j, v = k;
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
                numberOfShipsPlaced += setShip(x, y, shipConfig[(numberOfShipsToPlace-2)][numberOfShipsPlaced]);
                if (numberOfShipsToPlace == numberOfShipsPlaced) {
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
                playerToServer.put("Shot", id, x, y, board);
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
        String msg = msgInput.getText();
        msgInput.clear();
        if (!msg.equals("")) {
            ch.send(msg);
        }
    }

    @FXML
    void handleRotate() {
        rotated = !rotated;
    }

    public void waitForOpnBoard(){
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                serverToPlayer.query(new ActualField("Start"));
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            widgetContainer.getChildren().remove(rotateBtn);
                            lStatusbar.setText(id == 0 ? "Your turn" : "Opponents turn");
                            genOpnBoard(size, numberOfPlayers);
                        }
                    });
                return null;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    public void listenForTurn(){
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                serverToPlayer.query(new ActualField("Start"));
                while(true){
                    serverToPlayer.get(new ActualField("Turn"), new ActualField(id));
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            lStatusbar.setText("Your turn");
                            turn = true;
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
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                while(true){
                    Object[] res = serverToPlayer.get(new ActualField("Gameover"), new ActualField(id), new FormalField(Integer.class));
                    int playerHit = (int) res[2];

                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            if (playerHit == id){
                                gameOver = true;
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
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                while(true){
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
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                serverToPlayer.get(new ActualField("Win"),  new ActualField(id));
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        lStatusbar.setText("Winner");
                    }
                });
            return null;
            }

        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }


    public int setShip(int x, int y, int i) {
        if(size < x + (rotated ? i : 0) || size < y + (rotated ? 0 : i) || board.shipInTheWay(x, y, i, rotated)) {
            return 0;
        } else {
            for (int j = 0; j < i; j++) {
                board.placeShip(x + (rotated ? j : 0), y + (rotated ? 0 : j));
                ui.showShip(pButtons, x + (rotated ? j : 0), y + (rotated ? 0 : j));
            }
            return 1;
        }
    }

    public void showShipHover(int x, int y){
        if (!shipsPlaced){
            int l = shipConfig[numberOfShipsToPlace - 2][numberOfShipsPlaced];
            if(!(size < x + (rotated ? l : 0) || size < y + (rotated ? 0 : l) || board.shipInTheWay(x, y, l, rotated))) {
                for (int i = 0; i < l; i++) {
                    ui.toggleShipHover(pButtons[x + (rotated ? i : 0)][y + (rotated ? 0 : i)]);
                }
            }
        }
    }
}


