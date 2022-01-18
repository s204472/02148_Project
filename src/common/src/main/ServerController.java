package common.src.main;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import org.jspace.*;

import javax.sound.midi.Soundbank;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerController {
    private static Space idSpace;
    private static Space serverToPlayer;
    private static Space playerToServer;
    private static Space chat;

    private static int numberOfPlayers = 2, numberOfShips, sizeOfMap;

    private static ArrayList<Integer> alivePlayers = new ArrayList<Integer>();
    private static boolean[] playerXAlive = new boolean[numberOfPlayers];;

    @FXML
    private TextField customSize, customShips;
    @FXML
    private Label status;

    public void createCustomGame() throws Exception {
        try {
            sizeOfMap = Integer.parseInt(customSize.getText());
            numberOfShips = Integer.parseInt(customShips.getText());
            if (!legalShipCount(numberOfShips) && !legalBoardSize(sizeOfMap)){
                throw new IllegalArgumentException("Illegal board size and number of ships");
            } else if (!legalShipCount(numberOfShips)){
                throw new IllegalArgumentException("Illegal number of ships");
            } else if (!legalBoardSize(sizeOfMap)){
                throw new IllegalArgumentException("Illegal board size");
            }
            status.setText("Server started...");
            startGame();
        } catch (NumberFormatException e){
            status.setText("Illegal input.");
        } catch (IllegalArgumentException e){
            status.setText(e.getMessage());
        }
    }
    public void playerSelecter(ActionEvent e) {
        numberOfPlayers = Integer.parseInt(((RadioButton) e.getSource()).getId());
    }
    public boolean legalBoardSize(int x){
        return x >= 7 && x <= 13;
    }
    public boolean legalShipCount(int x){
        return x >= 2 && x <= 6;
    }

    public void startGame() throws InterruptedException {
        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                initTupleSpaces();
                initPlayers(numberOfPlayers);
                initIds(numberOfPlayers);

                GameBoard[] gameBoardArray = getShips();

                runGame(gameBoardArray);
                return 1;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
    public static void initTupleSpaces(){
        String port = "9001";
        String host = "localhost";
        String uri = "tcp://" + host + ":" + port + "/?conn";
        SpaceRepository repo = new SpaceRepository();
        repo.addGate(uri);
        serverToPlayer = new SequentialSpace();
        playerToServer = new SequentialSpace();
        idSpace = new SequentialSpace();

        repo.add("serverToPlayer", serverToPlayer);
        repo.add("playerToServer", playerToServer);
        repo.add("id", idSpace);

        chat = new SequentialSpace();
        repo.add("chat", chat);
    }

    public static void initPlayers(int numberOfPlayers){
        for (int i = 0; i < numberOfPlayers; i++) {
            alivePlayers.add(i);
        }
        Arrays.fill(playerXAlive, true);
    }

    public static void initIds(int numberOfPlayers){
        for (int i = 0; i < numberOfPlayers; i++) {
            try {
                idSpace.put(i, numberOfPlayers, sizeOfMap, numberOfShips);
            } catch (Exception e){
            }
        }
        for (int i = 0; i < numberOfPlayers; i++) {
            try {
                Object[] objects = playerToServer.get(new ActualField("User"), new FormalField(Integer.class));
                System.out.println(objects[0] + (objects[1].toString()) + " connected");
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Players connected");
    }

    public static GameBoard[] getShips(){
        try {
            serverToPlayer.put("Place ships");
        } catch (InterruptedException e) {}

        GameBoard[] gameBoardArray = new GameBoard[numberOfPlayers];

        for (int i = 0; i < numberOfPlayers; i++) {
            try {
                gameBoardArray[i] = (GameBoard) playerToServer.get(new ActualField("Board"), new ActualField(i), new FormalField(GameBoard.class))[2];
            } catch (InterruptedException e) {}
        }
        return gameBoardArray;
    }

    public static void runGame(GameBoard[] gameBoardArray) throws InterruptedException{
        serverToPlayer.put("Start");

        Object[] res;
        int x, y, playerHit;
        boolean hit, shootAgain, samePlace, deadPlayer;
        while(true){
            if (alivePlayers.size() == 1) {
                serverToPlayer.put("Win", alivePlayers.get(0));
                break;
            }
            for (int i = 0; i < numberOfPlayers; i++) {
                System.out.println(playerXAlive[i]);
                if (playerXAlive[i]) {
                    do {
                        do {
                            System.out.println(numberOfPlayers);
                            System.out.println("1: " + i);
                            serverToPlayer.put("Turn", i);
                            System.out.println("2: " + i);
                            res = playerToServer.get(new ActualField("Shot"), new ActualField(i), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
                            System.out.println("3: " + i);
                            x = (int) res[2]; y = (int) res[3]; playerHit = (int) res[4];
                            samePlace = gameBoardArray[playerHit].getHit(x, y);
                            deadPlayer = playerXAlive[playerHit];
                            System.out.println("4: " + i);
                        } while (samePlace || !deadPlayer);

                        System.out.println("5: " + i);
                        hit = gameBoardArray[playerHit].setHit(x, y);
                        shootAgain = hit;
                        for (int j = 0; j < numberOfPlayers; j++) {
                            System.out.println("6: " + i + " " + j);
                            serverToPlayer.put("Shot", j, x, y, playerHit, hit);
                        }
                        if(gameBoardArray[playerHit].isGameover()) {
                            System.out.println("6.5: " + i + " ");
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
