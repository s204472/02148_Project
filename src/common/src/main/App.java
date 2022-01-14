package common.src.main;

import org.jspace.*;

public class App {
	private static Space idSpace;
	private static Space serverToPlayer;
	private static Space playerToServer;

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

		System.out.println("Players connected");
		serverToPlayer.put("Placeships");

		// TODO: SET BOARDS
		playerToServer.get(new ActualField("Board"), new ActualField(1), new FormalField(GameBoard.class));
		playerToServer.get(new ActualField("Board"), new ActualField(2), new FormalField(GameBoard.class));

		serverToPlayer.put("Start");

		// Read shots
		Object[] res;
		int id, x, y;
		while(true){
			try {
				// Player 1
				serverToPlayer.put("Turn", 1);
				res = playerToServer.get(new ActualField("Shot"), new ActualField(1), new FormalField(Integer.class), new FormalField(Integer.class)); // Shot from player i
				id = (int) res[1]; x = (int) res[2]; y = (int) res[3];

				serverToPlayer.put("Shot", 1, 1, x, y, isHit(id, x, y)); // Shot by player 1, message for player 1, x, y, was a hit?
				serverToPlayer.put("Shot", 1, 2, x, y, isHit(id, x, y)); // Shot by player 1, message for player 2, x, y, was a hit?
				// TODO: UPDATE BOARDS


				// Player 2
				serverToPlayer.put("Turn", 2);
				res = playerToServer.get(new ActualField("Shot"), new ActualField(2), new FormalField(Integer.class), new FormalField(Integer.class)); // Shot from player i
				id = (int) res[1]; x = (int) res[2]; y = (int) res[3];

				serverToPlayer.put("Shot", 2, 1, x, y, isHit(id, x, y));
				serverToPlayer.put("Shot", 2, 2, x, y, isHit(id, x, y));
				// TODO: UPDATE BOARDS

			} catch (InterruptedException e) {}
		}
	}
	// TODO: delete
	public static boolean isHit(int id, int x, int y){
		return true;
	}

}



