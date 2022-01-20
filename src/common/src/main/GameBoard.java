package common.src.main;

public class GameBoard {
    private int size;
    private Field[][] board;

    //Constructer to create the GameBoard for the game.
    public GameBoard(int size){
        this.size = size;
        this.board = new Field[size][size];
        for (int i = 0; i < board.length; i++){
            for (int j = 0; j < board[i].length; j++){
                board[i][j] = new Field();
            }
        }
    }
    //Method to convert the fields into ship-fields.
    public void placeShip(int x, int y){ board[x][y].setShip(); }

    //Controls if the game is over for the individual player
    public boolean isGameover() {
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(board[i][j].isShip() && !board[i][j].getHit()){
                    return false;
                }
            }
        }
        return true;
    }

    //Marks the field the players have shot at
    public boolean setHit(int x, int y){
        return board[x][y].setHit();
    }

    //Returns the status of the field
    public boolean getHit(int x, int y){
        return board[x][y].getHit();
    }

    //Method to control placement of ships. It secures ships isn't placed on top of each other.
    public boolean shipInTheWay(int x, int y, int shipLength, boolean rotated) {
        boolean inTheWay;
        for(int i = 0; i < shipLength; i++){
            if(rotated) {
                inTheWay = board[x + i][y].isShip();
            } else {
                inTheWay = board[x][y + i].isShip();
            }
            if(inTheWay){
                return true;
            }
        }
        return false;
    }
}

