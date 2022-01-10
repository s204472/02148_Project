package common.src.main;
import org.jspace.*;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.Scanner;

public class Player {
    private static RemoteSpace idSpace;
    private static RemoteSpace serverToPlayer;
    private static RemoteSpace playerToServer;
    private static int id;
    private static int otherId;
    private static Object[] workingPlayerObject;
    private static PlayerApp workingPlayer;

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        String port = "9000";
        String host = "localhost";
        String serverToPlayerURI = "tcp://" + host + ":" + port + "/serverToPlayer?conn";
        String playerToServerURI = "tcp://" + host + ":" + port + "/playerToServer?conn";
        String idURI = "tcp://" + host + ":" + port + "/id?conn";
        try {
            idSpace = new RemoteSpace(idURI);
            serverToPlayer = new RemoteSpace(serverToPlayerURI);
            playerToServer = new RemoteSpace(playerToServerURI);

            try {
                Object[] workingPlayerObject = idSpace.get(new FormalField(Integer.class), new FormalField(PlayerApp.class));
                id = (int) workingPlayerObject[0];

                while(true){




                }


            } catch (InterruptedException e) {}
        } catch (IOException e) {}

    }


}
