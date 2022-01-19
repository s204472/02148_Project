package common.src.main;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

public class ChatHelper {
    int id;
    int numberOfPlayers;
    RemoteSpace chat;
    VBox msgArea;

    public ChatHelper(int id, int numberOfPlayers, RemoteSpace chat, VBox msgArea){
        this.id = id;
        this.numberOfPlayers = numberOfPlayers;
        this.chat = chat;
        this.msgArea = msgArea;
    }

    public void listen(){
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while(true){
                    Object[] res = chat.get(new FormalField(Integer.class), new ActualField(id), new FormalField(String.class));
                    int from = (int) res[0];
                    String msg = res[2].toString();
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            HBox msgContainer = new HBox();
                            msgContainer.getStyleClass().add("msgBox");
                            Text sender = new Text("Player " + (from + 1) + ": ");
                            sender.getStyleClass().add("msgOpn");
                            Text txt = new Text(msg + "\n");
                            msgContainer.getChildren().add(sender);
                            msgContainer.getChildren().add(txt);
                            msgArea.getChildren().add(msgContainer);
                        }
                    });
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
    public void send(String msg) {
        try {
            HBox msgContainer = new HBox();
            Text sender = new Text("You: ");
            sender.getStyleClass().add("msgYou");
            Text txt = new Text(msg + "\n");
            msgContainer.getChildren().add(sender);
            msgContainer.getChildren().add(txt);
            msgContainer.setPrefHeight(10);
            msgArea.getChildren().add(msgContainer);
            for (int i = 0; i < numberOfPlayers; i++){
                if (i != id){
                    chat.put(id, i, msg);
                }
            }
        } catch (InterruptedException e) {}
    }
}
