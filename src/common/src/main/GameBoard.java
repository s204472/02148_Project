package common.src.main;

public class GameBoard {
    private final int SIZE;
    private Field[][] board;

    public GameBoard(int size){
        this.SIZE = size;
        this.board = new Field[size][size];
        for (int i = 0; i < board.length; i++){
            for (int j = 0; j < board[i].length; j++){
                board[i][j] = new Field();
            }
        }
    }

    public int getSize() { return SIZE; }

    public Field[][] getBoard(){ return board; }

    public Field getField(int x, int y){ return board[x][y]; }

    public void placeShip(int x, int y){ board[x][y].setShip(); }

    public boolean isGameover() {
        for(int i = 0; i < SIZE; i++){
            for(int j = 0; j < SIZE; j++){
                if(board[i][j].isShip() && !(board[i][j]).getHit()){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean shipInTheway(int x, int y, int shipLength, boolean rotated) {
        boolean inTheWay = false;
        for(int i = 0; i < shipLength; i++){
            if(rotated) {
                inTheWay = board[x + i][y].isShip();
                if(inTheWay){
                    return inTheWay;
                }
            } else {
                inTheWay = board[x][y + i].isShip();
                if(inTheWay){
                    return inTheWay;
                }
            }
        }
        return inTheWay;
    }
}

