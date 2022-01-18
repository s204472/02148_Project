package common.src.main;

import org.jspace.*;

import java.util.*;

public class App {
	private static Space idSpace;
	private static Space serverToPlayer;
	private static Space playerToServer;
	private static int numberOfPlayers = 4;
	private static int sizeOfMap = 10;
	private static ArrayList<Integer> alivePlayers = new ArrayList<Integer>();
	private static boolean[] playerXAlive = new boolean[numberOfPlayers];
	private static Space chat;


	public static void main(String[] argv) throws InterruptedException {
		initTupleSpaces();
		initPlayers();
		initIds();

		GameBoard[] gameBoardArray = getShips();
		runGame(gameBoardArray);
	}


	public static void initTupleSpaces(){
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

		// Chat init
		chat = new SequentialSpace();
		repo.add("chat", chat);
	}

	public static void initPlayers(){
		for (int i = 0; i < numberOfPlayers; i++) {
			alivePlayers.add(i);
		}
		Arrays.fill(playerXAlive, true);
	}

	public static void initIds(){
		for (int i = 0; i < numberOfPlayers; i++) {
			try {
				idSpace.put(i, numberOfPlayers, sizeOfMap);
			} catch (Exception e){}
		}
		for (int i = 0; i < numberOfPlayers; i++) {
			try {
				Object[] objects = playerToServer.get(new ActualField("User"), new FormalField(Integer.class));
				System.out.println(objects[0] + (objects[1].toString()) + " connected");
			} catch (InterruptedException e) {}
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
		boolean hit, shootAgain, samePlace;
		while(true){
			if (alivePlayers.size() == 1) {
				serverToPlayer.put("Gameover");
				break;
			}
			for (int i = 0; i < numberOfPlayers; i++) {
				if (playerXAlive[i]) {
					do {
						do {
							serverToPlayer.put("Turn", i);
							res = playerToServer.get(new ActualField("Shot"), new ActualField(i), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
							x = (int) res[2]; y = (int) res[3]; playerHit = (int) res[4];
							samePlace = gameBoardArray[playerHit].getHit(x, y);
						} while (samePlace);
						hit = gameBoardArray[playerHit].setHit(x, y);
						shootAgain = hit;
						for (int j : alivePlayers) {
							serverToPlayer.put("Shot", j, x, y, playerHit, hit);
						}
						if(gameBoardArray[playerHit].isGameover()) {
							playerXAlive[playerHit] = false;
							alivePlayers.remove(playerHit);
							serverToPlayer.put("Gameover", playerHit);
						}
					} while (shootAgain);

				}
			}
		}
	}
}



