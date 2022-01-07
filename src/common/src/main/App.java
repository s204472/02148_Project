package common.src.main;

import org.jspace.*;


public class App {
	public static void main(String[] args) {


		String port = "9000";
		String host = "localhost";

		String uri = "tcp://" + host + ":" + port + "/?conn";

		SpaceRepository repo = new SpaceRepository();
		repo.addGate(uri);
		Space serverToPlayer = new SequentialSpace();
		Space playerToServer = new SequentialSpace();
		Space id = new SequentialSpace();

		try {
			id.put(1);
			id.put(2);
		} catch (InterruptedException e) {}

		repo.add("serverToPlayer", serverToPlayer);
		repo.add("playerToServer", playerToServer);
		repo.add("id", id);


		while(true){
			try {

				Object[] objects = playerToServer.get(new FormalField(String.class), new FormalField(Integer.class));
				System.out.println(objects[0] + (objects[1].toString()));

				//here the board is created
			} catch (InterruptedException e) {}
		}
	}
}

class Game {
	private int size;
	private GameObjects[][] board;
	private boolean yourTurn = true;
	private boolean isHit;
	private int shipsLeft;
	private boolean gameOver;
	private boolean ready;

	public Game(int size) {
		this.size = size;
		this.board = new GameObjects[size][size];
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				board[x][y] = new Field();
			}
		}
	}

	public void printBoard(){
		for(int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (board[i][j] instanceof Field) {
					System.out.print("X ");
				} else if (board[i][j] instanceof Ship) {
					if (((Ship)board[i][j]).getShipHit()) {
						System.out.print("H ");
					} else {
						System.out.print("S ");
					}
				}

			}
			System.out.println();
		}
	}

	//Implement out of bounce detection
	public void placeShips(int x, int y, int shipLength, boolean orientation) {

		if(!ready){
			shipsLeft += shipLength;

			for (int i = 0; i < shipLength; i++) {
				if (orientation) {
					if (!(x < 0 || x+shipLength >= size)){
						board[i + x][y] = new Ship(shipLength);
					} else {
						System.out.println("Ship out of bounce");
						break;
					}

				} else {
					if(!(y < 0 || y+shipLength>= size)){
						board[x][i + y] = new Ship(shipLength);
					} else {
						System.out.println("Ship out of bounce");
						break;
					}
				}
			}
		} else {
			System.out.println("You can't place ships after you're ready");
		}
	}


	public boolean shooting(int x, int y) {
		if(ready) {
			if (board[x][y] instanceof Ship) {
				((Ship) board[x][y]).setShipHit();
				shipsLeft--;
				if (gameOver()){
					System.exit(0);
				}
				isHit = true;
			} else {
				yourTurn = false;
				System.out.println("You missed");
			}
			return isHit;
		} else {
			System.out.println("Player is not ready");
			return isHit;
		}

	}

	//Move this to the individual player
	public void setReady(){
		ready = true;
	}

	public boolean gameOver(){
		if(shipsLeft == 0){
			gameOver = true;
			System.out.println("Game Over!");
		}
		return gameOver;
	}

}




//		Game game = new Game(10);
//		game.printBoard();
//		game.placeShips(2, 2, 3, true);
//		game.placeShips(4, 4, 4, false);
//		System.out.println();
//		game.printBoard();
//		game.shooting(2, 2);
//		System.out.println();
//		game.setReady();
//		game.shooting(2, 2);
//		game.printBoard();
//		game.placeShips(2,3, 3, true);
//		System.out.println();
//		game.shooting(4, 4);
//		game.printBoard();
//		game.shooting(5, 5);
//
//		Game game1 = new Game(10);
//		game1.placeShips(1, 1, 1, true);
//		game1.setReady();
//		game1.shooting(1, 1);
//
//		Game game2 = new Game(10);
//		game2.placeShips(9, 9 , 5, true);
//		game2.placeShips(11, 11, 3, false);



