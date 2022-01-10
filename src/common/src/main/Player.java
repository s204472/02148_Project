package common.src.main;

import org.jspace.*;
import java.io.IOException;

public class Player {
    private static int id;
    private static RemoteSpace idSpace;
    private static RemoteSpace serverToPlayer;
    private static RemoteSpace playerToServer;

    public static void main(String[] args) {
        String port = "9000";
        String host = "localhost";

        String playerToServerURI = "tcp://" + host + ":" + port + "/playerToServer?conn";
        String serverToPlayerURI = "tcp://" + host + ":" + port + "/serverToPlayer?conn";
        String idURI = "tcp://" + host + ":" + port + "/id?conn";
        Ui ui = new Ui();
        new Thread(ui).start();

        try {
            idSpace = new RemoteSpace(idURI);
            serverToPlayer = new RemoteSpace(serverToPlayerURI);
            playerToServer = new RemoteSpace(playerToServerURI);


            try {
                id = (int) idSpace.get(new FormalField(Integer.class))[0];
                playerToServer.put("User", id);

            } catch (InterruptedException e) {}
        } catch (IOException e) {}

        new Thread(new ResponseListener(serverToPlayer, id, ui)).start();
    }

    public static void getClick(int x, int y) {
        System.out.println("U" + id + ": (" + x + "," + y + ")");
        try {
            playerToServer.put(id, x, y);
        } catch (InterruptedException e) {}
    }
}

class ResponseListener implements Runnable {
    RemoteSpace s;
    Ui ui;
    int id;

    public ResponseListener(RemoteSpace s, int id, Ui ui){
        this.s = s;
        this.id = id;
        this.ui = ui;

    }
    @Override
    public void run() {
        while(true){
            try {
                System.out.println("Waiting");
                Object[] res = s.get(new ActualField(id), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
                System.out.println("(" + res[1] + "," + res[2] + ") " + res[3]);
                ui.setHit();
            } catch (InterruptedException e) {}
        }
    }
}

