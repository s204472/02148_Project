package common.src.main;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.concurrent.Task;

import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

public class Controller implements Initializable {
    public static final int SIZE = 10;
    public static boolean shipsPlaced = false;
    public static boolean rotated = false;
    public static GameBoard board = new GameBoard(SIZE);
    public static int shipNumber = 2;

    @FXML
    private Scene gameScene;
    @FXML
    public GridPane pGrid;
    @FXML
    public GridPane oGrid;
    @FXML
    public Label lPlayer;


    UiHelper ui = new UiHelper(SIZE);

    private Button[][] pButtons;
    private Button[][] oButtons;

    private static int id;
    private static RemoteSpace idSpace;
    private static RemoteSpace serverToPlayer;
    private static RemoteSpace playerToServer;
    private boolean turn = false;
    private boolean gameover = false;
    private static String selectedPlayers;




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
                lPlayer.setText("Player " + id);

                serverToPlayer.query(new ActualField("Placeships"));
                genPlayerBoard(SIZE, SIZE);


            } catch (InterruptedException e) {}
        } catch (IOException e) {}
        waitForOpnBoard();
        listenForTurn();
        listenForShots();
        listenForGameover();
    }

    public static void setGame(GameBoard gameBoard) {board = gameBoard;}
    public static void selectPlayers(String players) {selectedPlayers = players;}
    private void newGame() {
        Scene tableViewScene = null;
        try {
            tableViewScene = (Scene) FXMLLoader.load(PlayerMain.class.getResource("/common.src/main/view.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage)this.gameScene.getWindow();
        stage.setScene(tableViewScene);
        stage.setX(stage.getX());
        stage.show();
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
                pButtons[i][j].hoverProperty().addListener(event -> showShipHover(u, v));

                pGrid.add(pButtons[i][j], i, j);
            }
        }
    }



    public void genOpnBoard(int x, int y){
        oButtons = new Button[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                oButtons[i][j] = new Button();
                oButtons[i][j].getStyleClass().add("fields");
                oButtons[i][j].getStyleClass().add("opn");

                int u = i;
                int v = j;
                oButtons[i][j].setOnAction(event -> handleOpnClick(u, v));
                oGrid.add(oButtons[i][j], i, j);
            }
        }
        ui.setInactive(oButtons);
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
            if (this.turn && !gameover){ // TODO: DON'T SHOOT AT SAME FIELD MORE THAN ONCE
                System.out.println("Shot by player on " + x + ":" + y);
                playerToServer.put("Shot", id, x, y);
                this.turn = false;
                ui.setInactive(oButtons);
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
                        if (id == 1){
                            ui.setActive(oButtons);
                        }
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
                            ui.setActive(oButtons);
                        }
                    });
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    public void listenForGameover(){
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                serverToPlayer.query(new ActualField("Gameover"));
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        setGameover();
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
                    Object[] res = serverToPlayer.get(new ActualField("Shot"), new FormalField(Integer.class), new ActualField(id), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Boolean.class));
                    int id_ = (int) res[1];
                    int x = (int) res[3];
                    int y = (int) res[4];
                    boolean hit = (boolean) res[5];

                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            if (id_ == id){
                                ui.setOpnHit(oButtons, x, y, hit);
                            } else {
                                ui.setPlayerHit(pButtons, x, y, hit);
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




    public void setGameover(){ gameover = true; }

    public void setTurn(){ this.turn = true; }

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

    public void showShipHover(int x, int y) throws IOException {
        int l = shipNumber;
        if (!shipsPlaced){
            if(!(SIZE < x + (rotated ? l : 0) || SIZE < y + (rotated ? 0 : l) || board.shipInTheway(x, y, l, rotated))) {
                for (int i = 0; i < l; i++) {
                    ui.toggleShipHover(pButtons[x + (rotated ? i : 0)][y + (rotated ? 0 : i)]);
                }
            }
        }
    public void showShip(int x, int y){
        pButtons[x][y].setStyle("-fx-background-color: #4f4f4f");
    }


}}


