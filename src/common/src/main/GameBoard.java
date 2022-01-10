package common.src.main;

public class GameBoard {
    private int size;
    private GameBoard[][] board;

    public GameBoard(int size){
        this.size = size;
        this.board = new GameBoard[size][size];
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++){
                board[i][j] = new Field();
            }
        }
    }

    public GameBoard() {

    }

    public int getSize() {return size;}

    public GameBoard[][] getBoard(){return board;}

    public GameBoard getField(int x, int y){return board[x][y];}

    public void placeShip(int x, int y){board[x][y] = new Ship();}

    public boolean isGameover() {
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(board[i][j] instanceof Ship && !((Ship) board[i][j]).getHit()){
                    return false;
                }
            }
        }
        return true;
    }
}

