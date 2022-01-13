package common.src.main;

public class Game {
    private int size;

    public Game(int size) {
        this.size = size;
    }
    public Game(){
        this.size = 10;
    }

    public int getSize() {
        return size;
    }

    public void placeShips(int x, int y, int shipLength, boolean orientation, GameBoard board) {
        for (int i = 0; i < shipLength; i++) {
            if (orientation) {
                if (!(x < 0 || x + shipLength >= board.getSize())) {
                    board.placeShip(x + i, y);
                } else {
                    System.out.println("Ship out of bounds");
                    break;
                }
            } else {
                if (!(y < 0 || y + shipLength >= board.getSize())) {
                    board.placeShip(x + i, y);
                } else {
                    System.out.println("Ship out of bounds");
                    break;
                }
            }
        }
    }

    //Implement next players turn
    public boolean shooting(int x, int y, GameBoard board) {
        if (board.getField(x, y).isShip() && !board.getField(x, y).getHit()) {
            board.getField(x, y).toggleHit();
            return true;
        } else if (!board.getField(x, y).isShip() && !board.getField(x, y).getHit()){
            board.getField(x, y).toggleHit();
            return false;
        } else {
            return false; //Don't switch to next player
        }
    }

    public boolean gameover(GameBoard board){ return board.isGameover(); }
}
