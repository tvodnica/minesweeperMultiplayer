/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Tomo
 */
public class ChangePlayersNamesController implements Initializable {

    @FXML
    private TextField tf_namePlayer1;
    @FXML
    private TextField tf_namePlayer2;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tf_namePlayer1.setText(MainController.player1.getName());
        tf_namePlayer2.setText(MainController.player2.getName());
    }

    @FXML
    private void savePlayerNames(ActionEvent event) {
        MainController.player1.setName(tf_namePlayer1.getText());
        MainController.player2.setName(tf_namePlayer2.getText());
        
        ((Stage)tf_namePlayer1.getScene().getWindow()).close();
    }

}
