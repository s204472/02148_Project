package common.src.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Ui extends Application implements Runnable {
    GridPane map;
    @Override
    public void run() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        StackPane root = new StackPane();

        map = new GridPane();
        genMap(15,15, map);

        root.getChildren().add(map);

        primaryStage.setScene(new Scene(root, 700, 400));
        primaryStage.show();
    }

    public void setHit(){
        System.out.println("Whatup i am hit");
    }

    public void genMap(int u, int v, GridPane g){
        for (int i = 0; i < u; i++) {
            for (int j = 0; j < v; j++) {
                Button btn = new Button();
                btn.setText(i + "," + j);
                g.add(btn, i, j);

                int x = i;
                int y = j;
                btn.setOnAction(event -> Player.getClick(x, y));
            }
        }
    }
}
