/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.utils;

import javafx.scene.control.Alert;

/**
 *
 * @author Tomo
 */
public class AlertUtils {

    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        ShowAlert(alert, title, message);
    }
     public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        ShowAlert(alert, title, message);
    }

    private static void ShowAlert(Alert alert, String title, String message) {
        alert.setTitle("Minesweeper multiplayer");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.show();
    }
}
