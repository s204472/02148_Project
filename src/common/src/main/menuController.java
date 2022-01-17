package common.src.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class menuController {

    @FXML
    private TextField customWidth, customBombs;
    @FXML
    private Label bombLabel, sizeLabel;
    @FXML
    private Scene menuScene;
    @FXML
    private RadioButton player2, player3, player4;

    private int savedWidth, savedBombs;

private static String selectedPlayers;

    public void playerSelecter(ActionEvent e) {
        selectedPlayers = "" + ((RadioButton) e.getSource()).getId();
    }


    public void createCustomGame() throws IOException {
        bombLabel.setVisible(false);
        sizeLabel.setVisible(false);
        savedWidth = Integer.parseInt(customWidth.getText());
        savedBombs = Integer.parseInt(customBombs.getText());

        if (savedBombs > savedWidth * savedWidth) {
            bombLabel.setText("To many bombs. Must be less than (width x height)");
            bombLabel.setVisible(true);
            if (  savedWidth < 4 || savedWidth > 100) {
                sizeLabel.setVisible(true);
            }
            if (savedBombs <0) {
                bombLabel.setText("Amount of bombs must be a positive integer");
                bombLabel.setVisible(true);
            }
            return;
        }

        if ( savedWidth < 4 || savedWidth > 100) {
            sizeLabel.setVisible(true);
            if(savedBombs >= savedWidth * savedWidth) {
                bombLabel.setText("To many bombs. Must be less than (width x height)");
                bombLabel.setVisible(true);
            }
            if (savedBombs <0) {
                bombLabel.setText("Amount of bombs must be a positive integer");
                bombLabel.setVisible(true);
            }
            return;
        }
        if (savedBombs <0) {
            bombLabel.setText("Amount of bombs must be a positive integer");
            bombLabel.setVisible(true);
            return;
        }
        Controller.setGame(new GameBoard(savedWidth));
        Controller.selectPlayers(selectedPlayers);
        Scene game = FXMLLoader.load(PlayerMain.class.getResource("/common.src/main/view.fxml"));

        Stage stage = (Stage) menuScene.getWindow();
        stage.setScene(game);
        //stage.getScene().getStylesheets().add("public/css/"+selectedPlayers+"/buttonStyle.css");
        // Views dosen't update on Linux when you don't set size, until you move the view.
        // To fix that we need to update the view somehow. This was the solution...
        stage.setX(stage.getX());
        stage.show();
    }

}
