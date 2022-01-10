package common.src.main;

import org.jspace.*;

public class App {
	private static int currentTurn;
	private static PlayerApp workingPlayer;
	private static Object[] workingPlayerObject;
	private static Space idSpace;
	private static Space serverToPlayer;
	private static Space playerToServer;
	private static int workingId;


	public static void main(String[] argv) throws InterruptedException {
		currentTurn = 0;
		String port = "9000";
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


		try {
			idSpace.put(0);
			idSpace.put(1);
		} catch (Exception e){}


		while(true){
			//Inside this while loop we will place ships


		}

	}

}

class Game {
	private int size;

	public Game(int size) {
		this.size = size;
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
					System.out.println("Ship out of bounce");
					break;
				}
			} else {
				if (!(y < 0 || y + shipLength >= board.getSize())) {
					board.placeShip(x + i, y);
				} else {
					System.out.println("Ship out of bounce");
					break;
				}
			}
		}
	}
	//Implement next players turn
	public boolean shooting(int x, int y, GameBoard board) {
		if (board.getField(x, y) instanceof Ship && !((Ship) board.getField(x, y)).getHit()) {
			((Ship) board.getField(x, y)).toggleHit();

			return true;
		} else if (board.getField(x, y) instanceof Field && !((Field) board.getField(x, y)).getHit()){
			((Field) board.getField(x, y)).toggleHit();
			return false;
		} else {
			return false; //Dont switch to next player
		}
	}


	public boolean gameover(GameBoard board){return board.isGameover();}



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



