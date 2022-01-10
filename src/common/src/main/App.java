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
			idSpace.put(1);
			idSpace.put(2);
		} catch (Exception e){}

		// Serve id's
		for (int i = 0; i < 2; i++){
			try {
				Object[] objects = playerToServer.get(new ActualField("User"), new FormalField(Integer.class));
				System.out.println(objects[0] + (objects[1].toString()) + " connected");
			} catch (InterruptedException e) {}
		}

		// Read shots
		while(true){
			// TODO: implement game start.
			try {
				Object[] res = playerToServer.get(new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
				int id = (int) res[0], x = (int) res[1], y = (int) res[2];
				System.out.println("U" + id + ": (" + x + "," + y + ")");
				serverToPlayer.put(1, x, y, isHit(id, x, y) ? 1 : 0);
				serverToPlayer.put(2, x, y, isHit(id, x, y) ? 1 : 0);
				// TODO: game logic

			} catch (InterruptedException e) {}
		}

	}
	// TODO: delete
	public static boolean isHit(int id, int x, int y){
		return true;
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



