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

		playerToServer.get(new ActualField("Board"), new ActualField(1), new FormalField(GameBoard.class));
		playerToServer.get(new ActualField("Board"), new ActualField(2), new FormalField(GameBoard.class));

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



