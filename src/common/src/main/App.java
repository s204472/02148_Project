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
		Space idSpace = new SequentialSpace();

		try {
			idSpace.put(1);
			idSpace.put(2);
		} catch (InterruptedException e) {}

		repo.add("serverToPlayer", serverToPlayer);
		repo.add("playerToServer", playerToServer);
		repo.add("id", idSpace);


		// Serve id's
		for (int i = 0; i < 2; i++){
			try {
				Object[] objects = playerToServer.get(new ActualField("User"), new FormalField(Integer.class));
				System.out.println(objects[0] + (objects[1].toString()) + " connected");
			} catch (InterruptedException e) {}
		}
		// Read shots
		while(true){
			try {
				Object[] res = playerToServer.get(new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
				int id = (int) res[0], x = (int) res[1], y = (int) res[2];
				System.out.println("U" + id + ": (" + x + "," + y + ")");
				System.out.println("Put on server test1");
				serverToPlayer.put(id, x, y, isHit(id, x, y) ? 1 : 0);

			} catch (InterruptedException e) {}

		}
	}
	public static boolean isHit(int id, int x, int y){
		return true;
	}
}



class A implements Runnable {
	public A() {

	}

	public void run() {

	}
}