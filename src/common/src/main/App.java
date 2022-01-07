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



class A implements Runnable {
	public A() {

	}

	public void run() {

	}
}