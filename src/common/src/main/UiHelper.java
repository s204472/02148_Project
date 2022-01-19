package common.src.main;

import javafx.scene.control.Button;

public class UiHelper {
    public void toggleShipHover(Button b) {
        if (b.getStyleClass().contains("hover")) {
            b.getStyleClass().remove("hover");
        } else {
            b.getStyleClass().add("hover");
        }
    }

    public void setPlayerHit(Button[][] b, int x, int y, boolean hit) {
        if (hit) {
            b[x][y].setStyle("-fx-background-color: #913133");
        } else {
            b[x][y].setStyle("-fx-background-color: #7cbef4");
        }
    }

    public void setOpnHit(Button[][] b, int x, int y, boolean hit) {
        if (hit) {
            b[x][y].setStyle("-fx-background-color: #f2686a");
        } else {
            b[x][y].setStyle("-fx-background-color: #7cbef4");
        }
    }

    public void setInactive(Button[][] b){
        for (int i = 0; i < b.length; i++){
            for (int j = 0; j < b[i].length; j++){
                if (b[i][j] == null){
                    break;
                }
                b[i][j].getStyleClass().remove("active");
            }
        }
    }

    public void setActive(Button[][] b){
        for (int i = 0; i < b.length; i++){
            for (int j = 0; j < b[i].length; j++){
                if (b[i][j] == null){
                    break;
                }
                b[i][j].getStyleClass().add("active");
            }
        }
    }

    public void setGameover(Button[][] b){
        for (int i = 0; i < b.length; i++){
            for (int j = 0; j < b[i].length; j++){
                b[i][j].getStyleClass().add("dead-field");
            }
        }
    }

    public void showShip(Button[][] b, int x, int y){
        b[x][y].setStyle("-fx-background-color: #4f4f4f");
    }
}