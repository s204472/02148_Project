package common.src.main;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import org.jspace.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerController {
    private static Space idSpace;
    private static Space serverToPlayer;
    private static Space playerToServer;
    private static Space chat;
    private static int numberOfPlayers = 2;
    private static int numberOfShips, sizeOfMap;
    private static ArrayList<Integer> alivePlayers = new ArrayList<>();
    private static boolean[] playerXAlive;

    @FXML
    private TextField customSize, customShips;
    @FXML
    private Button createBtn, exitBtn;
    @FXML
    private RadioButton players2, players3, players4, players5;
    @FXML
    private Label status;
    @FXML
    public void handleExit(){
        System.exit(0);
    }
    @FXML
    public void playerSelector(ActionEvent e) {
        numberOfPlayers = Integer.parseInt(((RadioButton) e.getSource()).getId());
    }

    public void createCustomGame() {
        try {
            sizeOfMap = Integer.parseInt(customSize.getText());
            numberOfShips = Integer.parseInt(customShips.getText());
            if (legalShipCount(numberOfShips) && legalBoardSize(sizeOfMap)){
                throw new IllegalArgumentException("Illegal board size and number of ships. Must be in range (7 - 13) and (2 - 6)");
            } else if (legalShipCount(numberOfShips)){
                throw new IllegalArgumentException("Illegal number of ships. Must be in range (2 - 6)");
            } else if (legalBoardSize(sizeOfMap)){
                throw new IllegalArgumentException("Illegal board size. Must be in range (7 - 13)");
            }

            setServerRunning();
            playerXAlive = new boolean[numberOfPlayers];
            startGame();

        } catch (NumberFormatException e){
            status.setText("Please input integers only");
        } catch (IllegalArgumentException e){
            status.setText(e.getMessage());
        }
    }
    public void setServerRunning(){
        customSize.setDisable(true);
        customShips.setDisable(true);
        createBtn.setText("Server started...");
        createBtn.setDisable(true);
        players2.setDisable(true); players3.setDisable(true); players4.setDisable(true); players5.setDisable(true);
        exitBtn.setText("Close connection");
        status.setText("Server started. Please connect players.");
    }

    public boolean legalBoardSize(int x){
        return x < 7 || x > 13;
    }
    public boolean legalShipCount(int x){
        return x < 2 || x > 6;
    }

    public void startGame() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                initTupleSpaces();
                initPlayers(numberOfPlayers);
                initIds(numberOfPlayers);
                GameBoard[] gameBoardArray = getShips();
                runGame(gameBoardArray);
                return null;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
    public static void initTupleSpaces(){
        SpaceRepository repo = new SpaceRepository();
        repo.addGate(Config.getURI());
        serverToPlayer = new SequentialSpace();
        playerToServer = new SequentialSpace();
        idSpace = new SequentialSpace();
        chat = new SequentialSpace();
        repo.add("serverToPlayer", serverToPlayer);
        repo.add("playerToServer", playerToServer);
        repo.add("id", idSpace);
        repo.add("chat", chat);
    }

    public static void initPlayers(int numberOfPlayers){
        for (int i = 0; i < numberOfPlayers; i++) {
            alivePlayers.add(i);
        }
        Arrays.fill(playerXAlive, true);
    }

    public static void initIds(int numberOfPlayers){
        try{
            for(int i = 0; i < numberOfPlayers; i++) {
                idSpace.put(i, numberOfPlayers, sizeOfMap, numberOfShips);
            }
            for(int i = 0; i < numberOfPlayers; i++){
                alivePlayers.add((Integer) playerToServer.get(new ActualField("User"), new FormalField(Integer.class))[1]);
            }
        } catch (Exception ignored){}
    }

    public static GameBoard[] getShips(){
        try {
            serverToPlayer.put("Place ships");
            GameBoard[] gameBoardArray = new GameBoard[numberOfPlayers];
            for (int i = 0; i < numberOfPlayers; i++) {
                gameBoardArray[i] = (GameBoard) playerToServer.get(new ActualField("Board"), new ActualField(i), new FormalField(GameBoard.class))[2];
            }
            return gameBoardArray;
        } catch (InterruptedException e) {return null;}
    }

    public static void runGame(GameBoard[] gameBoardArray) throws InterruptedException{
        serverToPlayer.put("Start");

        Object[] res;
        int x, y, playerHit;
        boolean hit, shootAgain;
        while(true){
            if (alivePlayers.size() == 1) {
                serverToPlayer.put("Win", alivePlayers.get(0));
                break;
            }
            for (int i = 0; i < numberOfPlayers; i++) {
                if (playerXAlive[i]) {
                    do {
                        do {
                            serverToPlayer.put("Turn", i);
                            res = playerToServer.get(new ActualField("Shot"), new ActualField(i), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
                            x = (int) res[2]; y = (int) res[3]; playerHit = (int) res[4];
                        } while (gameBoardArray[playerHit].getHit(x, y) || !playerXAlive[playerHit]);
                        hit = gameBoardArray[playerHit].setHit(x, y);
                        shootAgain = hit;
                        for (int j = 0; j < numberOfPlayers; j++) {
                            serverToPlayer.put("Shot", j, x, y, playerHit, hit);
                        }
                        if(gameBoardArray[playerHit].isGameover()) {
                            playerXAlive[playerHit] = false;
                            int tempPos = alivePlayers.indexOf(playerHit);
                            alivePlayers.remove(tempPos);
                            for (int j = 0; j < numberOfPlayers; j++){
                                serverToPlayer.put("Gameover", j,  playerHit);
                            }
                            if (alivePlayers.size() == 1){
                                shootAgain = false;
                            }
                        }
                    } while (shootAgain);
                }
            }
        }
    }
}
