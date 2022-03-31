/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.controller;

import chatContract.data.ChatMessage;
import hr.algebra.Main;
import hr.algebra.model.MinefieldCell;
import hr.algebra.model.Player;
import hr.algebra.utils.AlertUtils;
import hr.algebra.utils.ReflectionUtils;
import hr.algebra.utils.SerializationUtils;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Tomo 1111
 */
public class MainController implements Initializable {

    @FXML
    private Label lbl_player1;
    @FXML
    private Label lbl_player2;
    @FXML
    private Label lbl_pointsPlayer1;
    @FXML
    private Label lbl_minesPlayer2;
    @FXML
    private Label lbl_pointsPlayer2;
    @FXML
    private Label lbl_minesPlayer1;
    @FXML
    private GridPane table_minefied;
    @FXML
    private TextArea ta_chat;
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vbox_background;
    @FXML
    private Label lbl_title;
    @FXML
    private Button btn_newGame;
    @FXML
    private Button btn_sendChatMessage;

    private static final String ZERO = "0";
    private int numberOfMines;
    private boolean fileLoad = false; // Is data being loaded from serialized files or not. Only true when initializating.
    private int randomInt = 0;
    private static boolean fixScoreboard = false;
    public static Thread scoreboardThread = new Thread();

    public static List<MinefieldCell> allMinefieldCells = new ArrayList<>();
    public static Player player1 = new Player("Player 1");
    public static Player player2 = new Player("Player 2");

    private final String RED_TEXT_STYLE = "-fx-text-fill: red;";
    private final String BLUE_TEXT_STYLE = "-fx-text-fill: blue;";
    private final String NORMAL_TEXT_STYLE = "-fx-text-decoration: normal";
    private final String BUTTON_STYLE = "-fx-text-fill: white; -fx-background-color: blue;";
    private final String BACKGROUND_STYLE = "-fx-text-fill: blue; -fx-background-color: yellow;";

    private final Path SAVED_GAME_FILE_1 = Paths.get("Minefield.ser");
    private final Path SAVED_GAME_FILE_2 = Paths.get("Results.ser");

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        if (!Files.exists(SAVED_GAME_FILE_1) || !Files.exists(SAVED_GAME_FILE_2)) {
            return;
        }
        try {
            fileLoad = true;
            
            //Load data from saved files
            allMinefieldCells = (List<MinefieldCell>) SerializationUtils.read(SAVED_GAME_FILE_1.toString());
            List<Player> players = (List<Player>) SerializationUtils.read(SAVED_GAME_FILE_2.toString());

            //Load players
            player1 = players.get(0);
            player2 = players.get(1);

            loadMinefield();
            loadPlayersData();
            applySettings();

            fileLoad = false;

        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public MainController() {
    }

    //MENU BAR OPTIONS
    @FXML
    private void changePlayerNames(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/hr/algebra/view/changePlayersNames.fxml"));

        Stage stage = new Stage();
        stage.setTitle("Change players names!");
        stage.setScene(new Scene(root));
        stage.showAndWait();

        Main.connectToServer();

        lbl_player1.setText(player1.getName());
        lbl_player2.setText(player2.getName());

    }

    @FXML
    private void generateDocumentation(ActionEvent event) {
        StringBuilder builder = new StringBuilder();
        List<Class<?>> list = Arrays.asList(MinefieldCell.class, Player.class, MainController.class, ChangePlayersNamesController.class);

        for (Class<?> class1 : list) {
            ReflectionUtils.readClassAndMembersInfo(class1, builder);
            builder.append("\n\n_____________________________________________\n\n");
        }

        try {
            Files.write(Paths.get("Documentation.txt"), builder.toString().getBytes());
            AlertUtils.showInfoAlert("Success", "The documentation has been sucessfully created and stored in the application directory.");
        } catch (IOException ex) {
            AlertUtils.showErrorAlert("ERROR", "There has been an error and the documentation has not been generated.");
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void settings(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/hr/algebra/view/settings.fxml"));

        Stage stage = new Stage();
        stage.setTitle("Settings!");
        stage.setScene(new Scene(root));
        stage.showAndWait();

        applySettings();

    }

    //CREATING AND UPDATING THE GAME
    @FXML
    private void createNewGame() {
        setDefaultValues();
        initializeMinefield();
        Main.connectToServer();
    }

    private void setDefaultValues() {
        lbl_pointsPlayer1.setText(ZERO);
        lbl_pointsPlayer2.setText(ZERO);
        lbl_minesPlayer1.setText(ZERO);
        lbl_minesPlayer2.setText(ZERO);
        numberOfMines = 25;
        allMinefieldCells = new ArrayList<>();
        player1.setCurrentlyPlaying(true);
        player2.setCurrentlyPlaying(false);
        player1.setPoints(0);
        player2.setPoints(0);
        player1.setMines(0);
        player2.setMines(0);
        lbl_player1.setStyle(RED_TEXT_STYLE);
        lbl_player2.setStyle(NORMAL_TEXT_STYLE);

    }

    private void initializeMinefield() {
        createMinefieldCells();
        setMinesIntoMinefield();
        countSurroundingMines();
        setMouseClickEvents();
    }

    private void createMinefieldCells() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                MinefieldCell mfc = new MinefieldCell(j + i);
                table_minefied.add(mfc, j, i);
                allMinefieldCells.add(mfc);
            }
        }
    }

    private void setMinesIntoMinefield() {
        while (numberOfMines != 0) {
            for (MinefieldCell mfc : allMinefieldCells) {
                if (numberOfMines == 0) {
                    return;
                }
                Random random = new Random();
                if (random.nextInt(100) < 15 && !mfc.isContainsMine()) {
                    mfc.setContainsMine(true);
                    numberOfMines--;
                }
            }
        }
    }

    private void setMouseClickEvents() {
        for (MinefieldCell mfc : allMinefieldCells) {
            mfc.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    mfc.setClicked(true);
                    mfc.setOnAction(null);
                    if (mfc.isContainsMine()) {
                        mfc.setStyle("-fx-background-color: red;");
                        mfc.setText("X");
                        if (!fileLoad) {
                            updateScore(mfc);
                            switchPlayer();
                        }
                    } else {
                        mfc.setStyle("-fx-background-color: white;");
                        mfc.setText(Integer.toString(mfc.getSurroundingMines()));
                        if (!fileLoad) {
                            updateScore(mfc);
                        }
                    }
                    endGameIfAllFieldsClicked();
                    if (!fileLoad) {
                        Main.connectToServer();

                    }
                }
            });
        }
    }

    private void countSurroundingMines() {
        for (int i = 0; i < allMinefieldCells.size(); i++) {
            MinefieldCell cell = allMinefieldCells.get(i);

            List<Integer> leftSideIndexes = Arrays.asList(0, 10, 20, 30, 40, 50, 60, 70, 80, 90);
            List<Integer> rightSideIndexes = Arrays.asList(9, 19, 29, 39, 49, 59, 69, 79, 89, 99);

            // If cell is not LAST in table check NEXT cell
            if (i != allMinefieldCells.size() - 1 && !rightSideIndexes.contains(i) && allMinefieldCells.get(i + 1).isContainsMine()) {
                cell.setSurroundingMines(cell.getSurroundingMines() + 1);
            }
            // If cell is not FIRST in table check PREVIOUS cell
            if (i != 0 && !leftSideIndexes.contains(i) && allMinefieldCells.get(i - 1).isContainsMine()) {
                cell.setSurroundingMines(cell.getSurroundingMines() + 1);
            }
            // If cell is not in the FIRST ROW check PREVIOUS 9/10/11
            if (i - 9 >= 0 && !rightSideIndexes.contains(i) && allMinefieldCells.get(i - 9).isContainsMine()) {
                cell.setSurroundingMines(cell.getSurroundingMines() + 1);
            }
            if (i - 10 >= 0 && allMinefieldCells.get(i - 10).isContainsMine()) {
                cell.setSurroundingMines(cell.getSurroundingMines() + 1);
            }
            if (i - 11 >= 0 && !leftSideIndexes.contains(i) && allMinefieldCells.get(i - 11).isContainsMine()) {
                cell.setSurroundingMines(cell.getSurroundingMines() + 1);
            }
            // If cell is not in the LAST ROW check NEXT 9/10/11
            if (i + 9 <= allMinefieldCells.size() - 1 && !leftSideIndexes.contains(i) && allMinefieldCells.get(i + 9).isContainsMine()) {
                cell.setSurroundingMines(cell.getSurroundingMines() + 1);
            }
            if (i + 10 <= allMinefieldCells.size() - 1 && allMinefieldCells.get(i + 10).isContainsMine()) {
                cell.setSurroundingMines(cell.getSurroundingMines() + 1);
            }
            if (i + 11 <= allMinefieldCells.size() - 1 && !rightSideIndexes.contains(i) && allMinefieldCells.get(i + 11).isContainsMine()) {
                cell.setSurroundingMines(cell.getSurroundingMines() + 1);
            }
        }
    }

    private void updateScore(MinefieldCell mfc) {
        if (player1.isCurrentlyPlaying()) {

            if (mfc.isContainsMine()) {
                player1.setMines(player1.getMines() + 1);
                player1.setPoints(player1.getPoints() - 5);
            } else {
                player1.setPoints(player1.getPoints() + mfc.getSurroundingMines());

            }
            lbl_minesPlayer1.setText(Integer.toString(player1.getMines()));
            lbl_pointsPlayer1.setText(Integer.toString(player1.getPoints()));

        } else {

            if (mfc.isContainsMine()) {
                player2.setMines(player2.getMines() + 1);
                player2.setPoints(player2.getPoints() - 5);
            } else {
                player2.setPoints(player2.getPoints() + mfc.getSurroundingMines());
            }
            lbl_minesPlayer2.setText(Integer.toString(player2.getMines()));
            lbl_pointsPlayer2.setText(Integer.toString(player2.getPoints()));
        }
    }

    private void endGameIfAllFieldsClicked() {
        boolean end = true;
        for (MinefieldCell mfc : allMinefieldCells) {
            if (!mfc.isClicked()) {
                end = false;
            }
        }
        if (end) {
            Player winner = player1.getPoints() > player2.getPoints() ? player1 : player2;
            AlertUtils.showInfoAlert("The end!", "The winner is player " + winner.getName());
        }
    }

    private void switchPlayer() {
        if (player1.isCurrentlyPlaying()) {

            player1.setCurrentlyPlaying(false);
            player2.setCurrentlyPlaying(true);
            lbl_player1.setStyle(NORMAL_TEXT_STYLE);
            lbl_player2.setStyle(RED_TEXT_STYLE);
            enableClickOnMinefield(false);

        } else {
            player2.setCurrentlyPlaying(false);
            player1.setCurrentlyPlaying(true);
            lbl_player2.setStyle(NORMAL_TEXT_STYLE);
            lbl_player1.setStyle(RED_TEXT_STYLE);
            enableClickOnMinefield(true);
        }

    }

    public void loadPlayersData() {
        lbl_player1.setText(player1.getName());
        lbl_player2.setText(player2.getName());
        lbl_minesPlayer1.setText(Integer.toString(player1.getMines()));
        lbl_minesPlayer2.setText(Integer.toString(player2.getMines()));
        lbl_pointsPlayer1.setText(Integer.toString(player1.getPoints()));
        lbl_pointsPlayer2.setText(Integer.toString(player2.getPoints()));

        if (player1.isCurrentlyPlaying()) {
            lbl_player1.setStyle(RED_TEXT_STYLE);
            lbl_player2.setStyle(NORMAL_TEXT_STYLE);
            enableClickOnMinefield(true);
        } else {
            lbl_player1.setStyle(NORMAL_TEXT_STYLE);
            lbl_player2.setStyle(RED_TEXT_STYLE);
            enableClickOnMinefield(false);
        }

    }

    private void loadMinefield() {
        int counter = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                MinefieldCell cell = allMinefieldCells.get(counter++);
                cell.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
                table_minefied.add(cell, j, i);
            }
        }
        setMouseClickEvents();
        for (MinefieldCell cell : allMinefieldCells) {
            if (cell.isClicked()) {
                cell.fire();
            }
        }
    }

    public void updateMinefield() {
        fileLoad = true;
        loadMinefield();
        fileLoad = false;
    }

    private void enableClickOnMinefield(boolean allowedToPlay) {
        if (!allowedToPlay) {
            for (MinefieldCell mfc : allMinefieldCells) {
                mfc.setOnAction(null);
            }
        } else {
            setMouseClickEvents();
        }
    }

    //CHAT
    @FXML
    private void sendMessage(ActionEvent event) throws RemoteException {
        ChatMessage cm = new ChatMessage(player1.getName(), tf_message.getText(), LocalDateTime.now());
        List<ChatMessage> allMessages = Main.sendMessage(cm);
        refreshChat(allMessages);
        tf_message.clear();
    }

    public void refreshChat(List<ChatMessage> chatHistory) {
        ta_chat.clear();

        for (ChatMessage msg : chatHistory) {
            ta_chat.setText(ta_chat.getText() + msg.getTime() + "\n" + msg.getUsername() + ": " + msg.getMessage() + "\n" + "\n");
        }

    }

    //SETTINGS
    private void applySettings() {
        SettingsController.loadSettings();
        setUiTheme(SettingsController.useTheme);
        breakScoreboard(SettingsController.breakScoreboard);
    }

    private void setUiTheme(boolean useTheme) {

        if (useTheme) {
            btn_newGame.setStyle(BUTTON_STYLE);
            btn_sendChatMessage.setStyle(BUTTON_STYLE);
            lbl_title.setStyle(BLUE_TEXT_STYLE);
            vbox_background.setStyle(BACKGROUND_STYLE);
        } else {
            btn_newGame.setStyle(null);
            btn_sendChatMessage.setStyle(null);
            lbl_title.setStyle(null);
            vbox_background.setStyle(null);
        }
    }

    private void breakScoreboard(boolean brokenScoreboard) {

        if (brokenScoreboard) {
            Random random = new Random();
            fixScoreboard = false;
            scoreboardThread = new Thread(() -> {
                while (true) {
                    int i = randomInt;
                    randomInt = random.nextInt();
                    Platform.runLater(()
                            -> lbl_pointsPlayer1.setText(Integer.toString(randomInt)));
                    Platform.runLater(()
                            -> lbl_pointsPlayer2.setText(Integer.toString(randomInt)));
                    Platform.runLater(()
                            -> lbl_minesPlayer1.setText(Integer.toString(randomInt)));
                    Platform.runLater(()
                            -> lbl_minesPlayer2.setText(Integer.toString(randomInt)));
                    try {
                        Thread.sleep(100);

                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainController.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    if (fixScoreboard) {
                        Thread.currentThread().stop();
                    }
                }
            });
            scoreboardThread.start();
        } else {
            fixScoreboard = true;
            loadPlayersData();
        }
    }
}
