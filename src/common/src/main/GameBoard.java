package common.src.main;

public class GameBoard {
    private int size;
    private Field[][] board;

    public GameBoard(int size){
        this.size = size;
        this.board = new Field[size][size];
        for (int i = 0; i < board.length; i++){
            for (int j = 0; j < board[i].length; j++){
                board[i][j] = new Field();
            }
        }
    }

    public void placeShip(int x, int y){ board[x][y].setShip(); }

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

    public boolean setHit(int x, int y){
        return board[x][y].setHit();
    }

    public boolean getHit(int x, int y){
        return board[x][y].getHit();
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

