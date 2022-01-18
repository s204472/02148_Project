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
    private TextField customSize, customShips;
    @FXML
    private Label shipLabel, sizeLabel;
    @FXML
    private Scene menuScene;
    @FXML
    private RadioButton players2, players3, players4, players5;

    private int savedWidth = 0, savedShips = 0;

private static int selectedPlayers = 2;

    public void initialize() {

        switch (selectedPlayers) {
            case 2:
                players2.setSelected(true);
                break;

            case 3:
                players3.setSelected(true);
                break;

            case 4:
                players4.setSelected(true);
                break;

            case 5:
                players5.setSelected(true);
                break;
        }
    }

    public void playerSelecter(ActionEvent e) {
        selectedPlayers = Integer.parseInt(((RadioButton) e.getSource()).getId());
    }


    public void createCustomGame() throws IOException, InterruptedException {
        shipLabel.setVisible(false);
        sizeLabel.setVisible(false);
        try {
            savedWidth = Integer.parseInt(customSize.getText());
            savedShips = Integer.parseInt(customShips.getText());
        } catch (NumberFormatException e){
            shipLabel.setText("Must enter integers into text fields");
            shipLabel.setVisible(true);
            return;
        }
        if (savedShips >= (savedWidth/2)) {
            shipLabel.setText("To many Ships. Must be less than Size/2");
            shipLabel.setVisible(true);
            if (  savedWidth < 9 || savedWidth > 50) {
                sizeLabel.setVisible(true);
            }
            if (savedShips <1) {
                shipLabel.setText("Amount of Ships must be greater than 0");
                shipLabel.setVisible(true);
            }
            return;
        }

        if ( savedWidth < 9 || savedWidth > 50) {
            sizeLabel.setVisible(true);
            if(savedShips >= (savedWidth * savedWidth)/2) {
                shipLabel.setText("To many Ships. Must be less than Size/2");
                shipLabel.setVisible(true);
            }
            if (savedShips <1) {
                shipLabel.setText("Amount of Ships must be a greater than 0");
                shipLabel.setVisible(true);
            }
            return;
        }
        if (savedShips <1) {
            shipLabel.setText("Amount of ships must be greater than 0");
            shipLabel.setVisible(true);
            return;
        }

        App.setGameSize(savedWidth);
        App.selectPlayers(selectedPlayers);
        App.selectNumberOfShips(savedShips);

        //Launching the game itself bugs out.
        /*App.main(null);
        Scene game = FXMLLoader.load(PlayerMain.class.getResource("view.fxml"));

        Stage stage = (Stage) menuScene.getWindow();
        stage.setScene(game);
        // Views dosen't update on Linux when you don't set size, until you move the view.
        // To fix that we need to update the view somehow. This was the solution...
        stage.setX(stage.getX());
        stage.show();*/
    }

}
