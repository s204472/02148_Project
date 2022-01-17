package common.src.main;

import org.jspace.*;

public class App {
	private static Space idSpace;
	private static Space serverToPlayer;
	private static Space playerToServer;

	private static Space chat;

	private static int numPlayers = 2;


	public static void main(String[] argv) throws InterruptedException {
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

		try {
			idSpace.put(1, numPlayers);
			idSpace.put(2, numPlayers);
		} catch (Exception e){}

		// Serve id's
		for (int i = 0; i < 2; i++){
			try {
				Object[] objects = playerToServer.get(new ActualField("User"), new FormalField(Integer.class));
				System.out.println(objects[0] + (objects[1].toString()) + " connected");
			} catch (InterruptedException e) {}
		}

		System.out.println("Players connected");
		serverToPlayer.put("Placeships");

		GameBoard b1 = (GameBoard) playerToServer.get(new ActualField("Board"), new ActualField(1), new FormalField(GameBoard.class))[2];
		GameBoard b2 = (GameBoard) playerToServer.get(new ActualField("Board"), new ActualField(2), new FormalField(GameBoard.class))[2];

		serverToPlayer.put("Start");

		// Read shots
		Object[] res;
		int id, x = 0, y = 0;
		boolean hit, gameover;
		while(true){
			try {
				boolean samePlace1 = true; boolean samePlace2 = true; boolean shootAgain = true;
				// Player 1
				while (shootAgain){
					while(samePlace1){
						serverToPlayer.put("Turn", 1);
						res = playerToServer.get(new ActualField("Shot"), new ActualField(1), new FormalField(Integer.class), new FormalField(Integer.class)); // Shot from player i
						id = (int) res[1]; x = (int) res[2]; y = (int) res[3];
						samePlace1 = b2.getHit(x, y);
					}
					samePlace1 = true;
					hit = b2.setHit(x, y);
					shootAgain = hit;
					serverToPlayer.put("Shot", 1, 1, x, y, hit); // Shot by player 1, message for player 1, x, y, was a hit?
					serverToPlayer.put("Shot", 1, 2, x, y, hit); // Shot by player 1, message for player 2, x, y, was a hit?
					if( b2.isGameover()) {
						serverToPlayer.put("Gameover");
					}
				}
				shootAgain = true;




				// Player 2
				while(shootAgain){
					while(samePlace2){
						serverToPlayer.put("Turn", 2);
						res = playerToServer.get(new ActualField("Shot"), new ActualField(2), new FormalField(Integer.class), new FormalField(Integer.class)); // Shot from player i
						id = (int) res[1]; x = (int) res[2]; y = (int) res[3];
						samePlace2 = b1.getHit(x, y);
					}
					samePlace2 = true;
					hit = b1.setHit(x, y);
					shootAgain = hit;
					serverToPlayer.put("Shot", 2, 1, x, y, hit);
					serverToPlayer.put("Shot", 2, 2, x, y, hit);
					if(b1.isGameover()){
						serverToPlayer.put("Gameover");
					}
				}


			} catch (InterruptedException e) {}
		}
	}
}



