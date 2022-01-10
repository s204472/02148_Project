package common.src.main;

import java.util.ArrayList;
import java.util.List;

public class PlayerApp {
    private GameBoard board;
    private int id;
    private boolean ready;
    private int otherPlayers;
    private boolean yourTurn;

    public PlayerApp(GameBoard board, int id, int playerId){
        this.board = board;
        this.id = id;
        this.otherPlayers = playerId;
    }

    public void toggleTurn(){
        yourTurn = !yourTurn;
    }

    public boolean getYourTurn() {
        return yourTurn;
    }

    public int getOtherPlayers() {return otherPlayers;}

    public void addOtherPlayers(int playerId){otherPlayers = playerId;}

    public int getId() {return id;}

    public GameBoard getBoard() {return board;}

    public void updateGameBoard(GameBoard newBoard){board = newBoard;}

    public void toggleReady(){ready = true;}

    public boolean getReady(){return ready;}
}
