package common.src.main;

import javafx.scene.control.Button;

public class UiHelper {
    private int size;
    public UiHelper(int size){
        this.size = size;
    }
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
        for (int i = 0; i < this.size; i++){
            for (int j = 0; j < this.size; j++){
                b[i][j].getStyleClass().add("inactive");
            }
        }
    }

    public void setActive(Button[][] b){
        for (int i = 0; i < this.size; i++){
            for (int j = 0; j < this.size; j++){
                b[i][j].getStyleClass().remove("inactive");
            }
        }
    }
    public void showShip(Button[][] b, int x, int y){
        b[x][y].setStyle("-fx-background-color: #4f4f4f");
    }
}