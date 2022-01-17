package common.src.main;

import org.jspace.*;

import java.util.*;

public class App {
	private static Space idSpace;
	private static Space serverToPlayer;
	private static Space playerToServer;
	//number of players should be between 2 and 4.
	private static int numberOfPlayers = 4;
	private static int sizeOfMap = 10;
	private static ArrayList<Integer> alivePlayers = new ArrayList<Integer>();
	private static boolean[] playerXAlive = new boolean[numberOfPlayers];

	public static void main(String[] argv) throws InterruptedException {
		for (int i=0; i < numberOfPlayers; i++) {
		    alivePlayers.add(i);
		}
		Arrays.fill(playerXAlive, true);
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
		    for (int i=0; i<numberOfPlayers; i++) {
				idSpace.put(i, numberOfPlayers, sizeOfMap);
			}
		} catch (Exception e){}

		// Serve id's
		for (int i = 0; i < numberOfPlayers; i++) {
			try {
				Object[] objects = playerToServer.get(new ActualField("User"), new FormalField(Integer.class));
				System.out.println(objects[0] + (objects[1].toString()) + " connected");
			} catch (InterruptedException e) {}
		}

		System.out.println("Players connected");
		serverToPlayer.put("Place ships");

		GameBoard[] gameBoardArray = new GameBoard[numberOfPlayers];

		for (int i=0; i< numberOfPlayers; i++) {
			gameBoardArray[i] = (GameBoard) playerToServer.get(new ActualField("Board"), new ActualField(i), new FormalField(GameBoard.class))[2];
		}

		serverToPlayer.put("Start");

		// Read shots
		Object[] res;
		int id, x, y, shotAtBoard;
		boolean hit, gameOver;
		while(true){
			try {
				// Player 1
				if (alivePlayers.size() == 1) {
					serverToPlayer.put("GameOver");
					break;
				}
                for (int i = 0; i < numberOfPlayers; i++) {
                	if (playerXAlive[i]) {
						serverToPlayer.put("Turn", i);
						res = playerToServer.get(new ActualField("Shot"), new ActualField(i), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
						id = (int) res[1]; x = (int) res[2]; y = (int) res[3]; shotAtBoard = (int) res[4];
						hit = gameBoardArray[shotAtBoard].setHit(x, y);
						for (int j : alivePlayers) {
							serverToPlayer.put("Shot", j, x, y, shotAtBoard, hit);
						}
						if(gameBoardArray[shotAtBoard].isGameover()) {
							playerXAlive[shotAtBoard] = false;
						    int tempIndexOfPlayer = alivePlayers.indexOf(shotAtBoard);
						    alivePlayers.remove(tempIndexOfPlayer);
							System.out.println("player: " + shotAtBoard + " is dead");
						}
					}
				}
			} catch (InterruptedException e) {}
		}
	}
}



